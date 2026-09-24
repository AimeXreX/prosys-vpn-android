import 'dotenv/config';
import test from 'node:test';
import assert from 'node:assert/strict';
import { freeAccessDefaults, nextLocalMidnight } from '../lib/xui.js';

test('0.5 GiB quota is exactly 512 MiB', () => {
  assert.equal(freeAccessDefaults().quotaBytes, 536_870_912);
});

test('reset is calculated at Tehran midnight', () => {
  const now = Date.UTC(2026, 6, 18, 12, 0, 0);
  assert.equal(nextLocalMidnight(210, now), Date.UTC(2026, 6, 18, 20, 30, 0));
});
