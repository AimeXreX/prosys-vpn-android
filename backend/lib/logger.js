const secretKeys = ['authorization', 'token', 'password', 'secret', 'cookie', 'api_key'];

function redact(value, depth = 0) {
  if (depth > 4) return '[depth-limit]';
  if (Array.isArray(value)) return value.map(item => redact(item, depth + 1));
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([key, item]) => [
      key,
      secretKeys.some(secret => key.toLowerCase().includes(secret)) ? '[REDACTED]' : redact(item, depth + 1)
    ]));
  }
  return value;
}

function write(level, event, message, meta = {}) {
  const entry = { at: new Date().toISOString(), level, event, message, meta: redact(meta) };
  const output = JSON.stringify(entry);
  if (level === 'error') console.error(output);
  else console.log(output);
}

export const info = (event, message, meta) => write('info', event, message, meta);
export const warn = (event, message, meta) => write('warn', event, message, meta);
export const error = (event, message, meta) => write('error', event, message, meta);
