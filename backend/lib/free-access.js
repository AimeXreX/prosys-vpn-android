import crypto from 'node:crypto';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fetchFreeSubscription, freeAccessDefaults, getFreeClientTraffic, provisionFreeClient } from './xui.js';
import { warn } from './logger.js';

const file = path.resolve(process.env.FREE_DEVICE_DB_PATH || './data/free-devices.json');
let writeQueue = Promise.resolve();
let registrationQueue = Promise.resolve();

const sha256 = value => crypto.createHash('sha256').update(value).digest('hex');

function installationHash(installationId) {
  const secret = process.env.FREE_DEVICE_HMAC_SECRET;
  if (!secret || secret.length < 32) throw new Error('FREE_DEVICE_HMAC_SECRET must contain at least 32 characters');
  return crypto.createHmac('sha256', secret).update(installationId).digest('hex');
}

async function readDb() {
  await fs.mkdir(path.dirname(file), { recursive: true });
  try {
    return JSON.parse(await fs.readFile(file, 'utf8'));
  } catch (error) {
    if (error.code !== 'ENOENT') throw error;
    return { version: 1, devices: [] };
  }
}

function mutateDb(operation) {
  writeQueue = writeQueue.catch(() => undefined).then(async () => {
    const db = await readDb();
    const result = await operation(db);
    const temporary = `${file}.${process.pid}.tmp`;
    await fs.writeFile(temporary, JSON.stringify(db, null, 2), 'utf8');
    await fs.rename(temporary, file);
    return result;
  });
  return writeQueue;
}

function subscriptionUrl() {
  return `${(process.env.APP_URL || 'http://localhost:3010').replace(/\/+$/, '')}/api/free/subscription`;
}

function publicStatus(record, traffic) {
  const defaults = freeAccessDefaults();
  const quotaBytes = Number(traffic?.total || record.quotaBytes || defaults.quotaBytes);
  const usedBytes = Math.max(0, Number(traffic?.up || 0) + Number(traffic?.down || 0));
  const resetAt = Number(traffic?.expiryTime || record.expiryTime || defaults.resetAt);
  return {
    quotaBytes,
    usedBytes,
    remainingBytes: Math.max(0, quotaBytes - usedBytes),
    resetAt,
    exhausted: traffic?.enable === false || usedBytes >= quotaBytes,
    siteUrl: process.env.PRO_SYS_SITE_URL || 'https://vpn.prosysvpn.top/',
    botUrl: process.env.PRO_SYS_BOT_URL || 'https://t.me/ProSySsellbot',
    channelUrl: process.env.PRO_SYS_CHANNEL_URL || 'https://t.me/Prosys_VPN'
  };
}

async function findByToken(token) {
  if (!token || token.length < 32 || token.length > 256) return null;
  const db = await readDb();
  return db.devices.find(item => item.tokenHash === sha256(token)) || null;
}

async function registerFreeDeviceInternal({ installationId, existingToken }) {
  const installHash = installationHash(installationId);
  let db = await readDb();
  let record = db.devices.find(item => item.installationHash === installHash);

  if (record && existingToken && sha256(existingToken) === record.tokenHash) {
    const traffic = await getFreeClientTraffic(record.email).catch(error => {
      warn('free.status_failed', 'Could not refresh existing free client', { email: record.email, error: error.message });
      return null;
    });
    return { deviceToken: existingToken, subscriptionUrl: subscriptionUrl(), ...publicStatus(record, traffic) };
  }

  let deviceToken = crypto.randomBytes(32).toString('base64url');
  if (!record) {
    const provisioned = await provisionFreeClient(installHash);
    record = {
      installationHash: installHash,
      tokenHash: sha256(deviceToken),
      email: provisioned.email,
      subId: provisioned.subId,
      clientId: provisioned.clientId,
      inboundIds: provisioned.inboundIds,
      quotaBytes: provisioned.quotaBytes,
      expiryTime: provisioned.expiryTime,
      createdAt: new Date().toISOString(),
      lastSeenAt: new Date().toISOString()
    };
    await mutateDb(current => current.devices.push(record));
  } else {
    await mutateDb(current => {
      const target = current.devices.find(item => item.installationHash === installHash);
      target.tokenHash = sha256(deviceToken);
      target.lastSeenAt = new Date().toISOString();
    });
  }

  const traffic = await getFreeClientTraffic(record.email).catch(() => null);
  return { deviceToken, subscriptionUrl: subscriptionUrl(), ...publicStatus(record, traffic) };
}

export function registerFreeDevice(input) {
  const operation = registrationQueue.then(() => registerFreeDeviceInternal(input));
  registrationQueue = operation.catch(() => undefined);
  return operation;
}

export async function getFreeStatus(token) {
  const record = await findByToken(token);
  if (!record) return null;
  return publicStatus(record, await getFreeClientTraffic(record.email));
}

export async function getFreeSubscription(token) {
  const record = await findByToken(token);
  if (!record) return null;
  return fetchFreeSubscription(record.subId);
}
