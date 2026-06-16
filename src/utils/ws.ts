import { ElMessage } from 'element-plus'
import { getWsUrl } from '@/config/runtime'
import type { WsMessage } from '@/types'

type MessageHandler = (message: WsMessage) => void

class WsClient {
  private socket: WebSocket | null = null
  private heartbeatTimer: number | null = null
  private reconnectTimer: number | null = null
  private reconnectCount = 0
  private readonly maxReconnectCount = 10
  private manualClose = false
  private listeners = new Map<string, MessageHandler[]>()

  connect(token: string): void {
    const url = getWsUrl(token)
    if (!url) return

    if (this.socket?.readyState === WebSocket.OPEN) return

    this.manualClose = false
    this.socket = new WebSocket(url)

    this.socket.onopen = () => {
      this.reconnectCount = 0
      this.startHeartbeat()
    }

    this.socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data) as WsMessage
        const handlers = this.listeners.get(message.type) || []
        handlers.forEach((handler) => handler(message))
      } catch (error) {
        console.error('WebSocket 消息解析失败：', error)
      }
    }

    this.socket.onerror = () => {
      ElMessage.warning('实时连接异常，系统将自动重连')
    }

    this.socket.onclose = () => {
      this.stopHeartbeat()
      if (!this.manualClose) {
        this.reconnect(token)
      }
    }
  }

  on(type: string, handler: MessageHandler): void {
    const oldHandlers = this.listeners.get(type) || []
    this.listeners.set(type, [...oldHandlers, handler])
  }

  off(type: string, handler: MessageHandler): void {
    const oldHandlers = this.listeners.get(type) || []
    this.listeners.set(
      type,
      oldHandlers.filter((item) => item !== handler)
    )
  }

  send(payload: Record<string, unknown>): void {
    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(payload))
    }
  }

  close(): void {
    this.manualClose = true
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.socket?.close()
    this.socket = null
  }

  private reconnect(token: string): void {
    if (this.reconnectCount >= this.maxReconnectCount) return
    this.reconnectCount += 1
    this.reconnectTimer = window.setTimeout(() => this.connect(token), 3000)
  }

  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      this.send({ type: 'PING', timestamp: Date.now() })
    }, 20000)
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      window.clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
}

export const wsClient = new WsClient()
