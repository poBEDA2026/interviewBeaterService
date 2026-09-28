const REFRESH_LEAD_MS = 60_000; // refresh за 60 сек до exp

export function scheduleRefresh(
  expSeconds: number,
  onExpire: () => void,
): () => void {
  const nowMs = Date.now();
  const expMs = expSeconds * 1000;
  const delay = expMs - nowMs - REFRESH_LEAD_MS;

  if (delay <= 0) {
    // exp уже близко или в прошлом — стреляем немедленно (на следующем тике, чтобы не stack-overflow)
    const handle = setTimeout(onExpire, 0);
    return () => clearTimeout(handle);
  }

  const handle = setTimeout(onExpire, delay);
  return () => clearTimeout(handle);
}
