import crypto from 'node:crypto';
import https from 'node:https';
import { info } from './logger.js';

function config() {
  const inboundIds = (process.env.XUI_FREE_INBOUND_IDS || '1')
    .split(',').map(value => Number(value.trim())).filter(Number.isInteger);
  if (!inboundIds.length) throw new Error('XUI_FREE_INBOUND_IDS is invalid');
  const dailyGb = Number(process.env.FREE_DAILY_GB || 0.5);
  if (!Number.isFinite(dailyGb) || dailyGb < 0.1 || dailyGb > 100) {
    throw new Error('FREE_DAILY_GB must be between 0.1 and 100');
  }
  return {
    base: (process.env.XUI_BASE_URL || '').replace(/\/+$/, ''),
    token: process.env.XUI_API_TOKEN || '',
    inboundIds,
    subBase: (process.env.XUI_FREE_PUBLIC_SUB_URL || '').replace(/\/+$/, ''),
    dailyGb,
    resetOffsetMinutes: Math.min(Math.max(Number(process.env.FREE_RESET_OFFSET_MINUTES || 210), -720), 840)
  };
}

function isDemo() {
  return process.env.DEMO_MODE === 'true' && !process.env.XUI_API_TOKEN;
}

async function request(apiPath, options = {}) {
  const current = config();
  if (!current.base || !current.token) throw new Error('3x-ui connection is not configured');
  const allowSelfSigned = process.env.XUI_ALLOW_SELF_SIGNED === 'true';
  const dispatcher = allowSelfSigned ? new https.Agent({ rejectUnauthorized: false }) : undefined;
  const response = await fetch(`${current.base}${apiPath}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${current.token}`,
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...(options.headers || {})
    },
    dispatcher,
    signal: AbortSignal.timeout(15_000)
  });
  const text = await response.text();
  let body;
  try { body = text ? JSON.parse(text) : null; } catch { body = { message: text }; }
  if (!response.ok || body?.success === false) {
    throw new Error(`3x-ui request failed (${response.status}): ${body?.msg || body?.message || response.statusText}`);
  }
  return body?.obj ?? body?.data ?? body;
}

export function nextLocalMidnight(offsetMinutes = config().resetOffsetMinutes, now = Date.now()) {
  const dayMs = 86_400_000;
  const offsetMs = offsetMinutes * 60_000;
  return (Math.floor((now + offsetMs) / dayMs) + 1) * dayMs - offsetMs;
}

export function freeAccessDefaults() {
  const current = config();
  return {
    quotaBytes: Math.round(current.dailyGb * 1024 ** 3),
    resetAt: nextLocalMidnight(current.resetOffsetMinutes)
  };
}

export async function provisionFreeClient(installationHash) {
  const current = config();
  const clientId = crypto.randomUUID();
  const subId = crypto.randomBytes(16).toString('hex');
  const email = `free-${installationHash.slice(0, 24)}`;
  const expiryTime = nextLocalMidnight(current.resetOffsetMinutes);
  const quotaBytes = Math.round(current.dailyGb * 1024 ** 3);
  if (isDemo()) return { clientId, subId, email, inboundIds: current.inboundIds, expiryTime, quotaBytes, demo: true };

  const client = {
    id: clientId,
    email,
    enable: true,
    flow: '',
    tgId: 0,
    subId,
    limitIp: 1,
    totalGB: quotaBytes,
    expiryTime,
    reset: 1,
    group: 'prosys-free',
    comment: 'ProSyS VPN managed free device'
  };
  await request('/panel/api/clients/add', {
    method: 'POST',
    body: JSON.stringify({ client, inboundIds: current.inboundIds })
  });
  info('xui.free_client_created', 'Managed free client created', { email, inboundIds: current.inboundIds });
  return { clientId, subId, email, inboundIds: current.inboundIds, expiryTime, quotaBytes, demo: false };
}

export async function getFreeClientTraffic(email) {
  const current = config();
  if (isDemo()) {
    return { email, up: 0, down: 0, total: Math.round(current.dailyGb * 1024 ** 3), expiryTime: nextLocalMidnight(current.resetOffsetMinutes), enable: true };
  }
  return request(`/panel/api/clients/traffic/${encodeURIComponent(email)}`);
}

export async function fetchFreeSubscription(subId) {
  const current = config();
  if (!current.subBase) throw new Error('XUI_FREE_PUBLIC_SUB_URL is not configured');
  if (isDemo()) return `vless://${crypto.randomUUID()}@demo.invalid:443?security=tls&type=ws#ProSyS-Free-Demo`;
  const response = await fetch(`${current.subBase}/${encodeURIComponent(subId)}`, {
    headers: { Accept: 'text/plain,*/*', 'User-Agent': 'ProSyS-VPN/1.0' },
    signal: AbortSignal.timeout(15_000)
  });
  if (!response.ok) throw new Error(`Free subscription request failed (${response.status})`);
  return response.text();
}

export async function checkXuiHealth() {
  if (isDemo()) return { ok: true, demo: true };
  await request('/panel/api/inbounds/list');
  return { ok: true, demo: false };
}
