const SERVER_HOST_KEY = 'campus_server_host'

function getStoredServerHost(): string {
  return normalizeServerHost(localStorage.getItem(SERVER_HOST_KEY) || '')
}

function getBrowserHost(): string {
  if (typeof window === 'undefined') return ''
  return normalizeServerHost(window.location.host || '')
}

function getBrowserOrigin(): string {
  if (typeof window === 'undefined') return ''
  return window.location.origin || ''
}

export function normalizeServerHost(host: string): string {
  if (!host) return ''

  return host
    .trim()
    .replace(/^https?:\/\//, '')
    .replace(/^ws?:\/\//, '')
    .replace(/\/+$/, '')
}

export function validateServerHost(host: string): boolean {
  const value = normalizeServerHost(host)
  return /^([a-zA-Z0-9.-]+):(\d{2,5})$/.test(value)
}

export function getServerHost(): string {
  return getBrowserHost() || getStoredServerHost()
}

export function setServerHost(host: string): string {
  const normalized = normalizeServerHost(host)
  localStorage.setItem(SERVER_HOST_KEY, normalized)
  return normalized
}

export function clearServerHost(): void {
  localStorage.removeItem(SERVER_HOST_KEY)
}

export function getApiBaseUrl(): string {
  const browserOrigin = getBrowserOrigin()
  const browserHost = getBrowserHost()
  const storedHost = getStoredServerHost()

  if (browserOrigin) {
    return `${browserOrigin}/api`
  }

  return browserHost ? `http://${browserHost}/api` : storedHost ? `http://${storedHost}/api` : ''
}

export function getWsUrl(token = ''): string {
  const browserHost = getBrowserHost()
  const storedHost = getStoredServerHost()
  const host = browserHost || storedHost
  if (!host) return ''
  const query = token ? `?token=${encodeURIComponent(token)}` : ''

  if (browserHost && typeof window !== 'undefined') {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${protocol}//${browserHost}/ws/notifications${query}`
  }

  return `ws://${storedHost}/ws/notifications${query}`
}

export function getAccessEntryUrl(): string {
  const origin = getBrowserOrigin()
  if (origin) return origin

  const host = getServerHost()
  return host ? `http://${host}` : ''
}
