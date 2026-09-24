import 'dotenv/config';
import fs from 'node:fs/promises';
import path from 'node:path';

const source = path.resolve(process.env.FREE_DEVICE_DB_PATH || './data/free-devices.json');
const backupDir = path.resolve(process.env.FREE_DEVICE_BACKUP_DIR || './backups');
const stamp = new Date().toISOString().replaceAll(':', '-').replaceAll('.', '-');
const destination = path.join(backupDir, `free-devices-${stamp}.json`);

await fs.mkdir(backupDir, { recursive: true });
await fs.copyFile(source, destination);
console.log(JSON.stringify({ ok: true, source, destination }));
