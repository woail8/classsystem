import { beforeEach, describe, expect, it } from 'vitest'
import {
  clearServerHost,
  getApiBaseUrl,
  getAccessEntryUrl,
  getServerHost,
  getWsUrl,
  normalizeServerHost,
  setServerHost,
  validateServerHost,
} from '@/config/runtime'

describe('runtime config', () => {
  beforeEach(() => {
    clearServerHost()
    window.history.replaceState({}, '', 'http://localhost:3000/login')
  })

  it('应规范化服务端地址', () => {
    expect(normalizeServerHost('http://192.168.1.2:8080/')).toBe('192.168.1.2:8080')
    expect(normalizeServerHost('ws://demo.local:9000')).toBe('demo.local:9000')
  })

  it('应校验地址格式', () => {
    expect(validateServerHost('192.168.1.2:8080')).toBe(true)
    expect(validateServerHost('demo.local:8080')).toBe(true)
    expect(validateServerHost('192.168.1.2')).toBe(false)
  })

  it('应生成 API 与 WebSocket 地址', () => {
    setServerHost('192.168.1.2:8080')
    expect(getServerHost()).toBe('localhost:3000')
    expect(getApiBaseUrl()).toBe('http://localhost:3000/api')
    expect(getWsUrl('abc')).toBe('ws://localhost:3000/ws/notifications?token=abc')
  })

  it('应返回当前访问入口地址', () => {
    expect(getAccessEntryUrl()).toBe('http://localhost:3000')
  })
})
