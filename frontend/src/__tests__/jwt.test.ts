import { describe, it, expect } from 'vitest';
import { parseExp } from '@/lib/jwt';

const base64url = (obj: object) =>
  btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

const makeJwt = (payload: object) =>
  `header.${base64url(payload)}.signature`;

describe('parseExp', () => {
  it('returns exp seconds when valid', () => {
    const token = makeJwt({ exp: 1_700_000_000 });
    expect(parseExp(token)).toBe(1_700_000_000);
  });

  it('returns null when exp missing', () => {
    const token = makeJwt({ sub: '1' });
    expect(parseExp(token)).toBeNull();
  });

  it('returns null for malformed token', () => {
    expect(parseExp('not.a.jwt')).toBeNull();
    expect(parseExp('garbage')).toBeNull();
  });
});
