/**
 * Decode the `exp` (expiration, in seconds since epoch) claim from a JWT.
 * Returns null if the token is malformed or has no numeric `exp` claim.
 */
export function parseExp(token: string): number | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    const padded = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(padded);
    const obj = JSON.parse(json) as { exp?: unknown };
    return typeof obj.exp === 'number' ? obj.exp : null;
  } catch {
    return null;
  }
}
