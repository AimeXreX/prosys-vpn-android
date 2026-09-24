import 'dotenv/config';
import path from 'node:path';
import crypto from 'node:crypto';
import express from 'express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { z } from 'zod';
import { getFreeStatus, getFreeSubscription, registerFreeDevice } from './lib/free-access.js';
import { error as logError, info as logInfo } from './lib/logger.js';
import { checkXuiHealth } from './lib/xui.js';

const app = express();
const port = Number(process.env.PORT || 3010);
const registrationSchema = z.object({
  installationId: z.string().regex(/^[a-f0-9]{64}$/),
  deviceToken: z.string().min(32).max(256).optional()
});

app.set('trust proxy', 1);
app.use(helmet());
app.use(express.json({ limit: '32kb' }));
app.use('/api/', rateLimit({ windowMs: 60_000, limit: 120, standardHeaders: true, legacyHeaders: false }));
const registrationLimiter = rateLimit({
  windowMs: 10 * 60_000,
  limit: 12,
  standardHeaders: true,
  legacyHeaders: false
});
app.use((req, res, next) => {
  const started = Date.now();
  const requestId = crypto.randomBytes(5).toString('hex');
  res.on('finish', () => logInfo('http.request', 'Android API request', {
    requestId, method: req.method, path: req.originalUrl, status: res.statusCode, elapsedMs: Date.now() - started
  }));
  next();
});

app.get('/health', async (_, res) => {
  const xui = await checkXuiHealth().then(() => true).catch(() => false);
  res.status(xui ? 200 : 503).json({
    ok: xui,
    service: 'prosysvpn-android-backend',
    dependencies: { xui }
  });
});
app.get('/privacy', (_, res) => res.sendFile(path.resolve('public/privacy.html')));
app.get('/api/app/notice', (_, res) => {
  const message = (process.env.APP_NOTICE || '').trim();
  res.set('Cache-Control', 'public, max-age=300').json({ enabled: Boolean(message), message });
});

app.post('/api/free/register', registrationLimiter, async (req, res) => {
  const parsed = registrationSchema.safeParse(req.body);
  if (!parsed.success) return res.status(400).json({ error: 'Invalid installation identifier' });
  try {
    res.status(201).json(await registerFreeDevice({
      installationId: parsed.data.installationId,
      existingToken: parsed.data.deviceToken
    }));
  } catch (error) {
    logError('free.registration_failed', 'Free client registration failed', { error: error.message });
    res.status(502).json({ error: 'Free service is temporarily unavailable' });
  }
});

app.get('/api/free/status', async (req, res) => {
  try {
    const status = await getFreeStatus(req.get('x-prosys-device'));
    if (!status) return res.status(401).json({ error: 'Unauthorized device' });
    res.json(status);
  } catch (error) {
    logError('free.status_failed', 'Free status request failed', { error: error.message });
    res.status(502).json({ error: 'Free service is temporarily unavailable' });
  }
});

app.get('/api/free/subscription', async (req, res) => {
  try {
    const subscription = await getFreeSubscription(req.get('x-prosys-device'));
    if (!subscription) return res.status(401).type('text/plain').send('Unauthorized device');
    res.set('Cache-Control', 'no-store, private').type('text/plain; charset=utf-8').send(subscription);
  } catch (error) {
    logError('free.subscription_failed', 'Free subscription request failed', { error: error.message });
    res.status(502).type('text/plain').send('Free subscription is temporarily unavailable');
  }
});

app.use((_, res) => res.status(404).json({ error: 'Not found' }));

app.listen(port, '127.0.0.1', () => {
  logInfo('server.started', 'ProSyS VPN Android backend started', { port, environment: process.env.NODE_ENV || 'development' });
});
