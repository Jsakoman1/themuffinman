import {onBeforeUnmount, ref} from "vue"
import type {ChatSocketEventDTO} from "../../../contracts/index.ts"
import {API_BASE_URL} from "../../../api/httpClient.ts"
import {token} from "../../../services/sessionService.ts"

export type ChatRealtimeState = "DISCONNECTED" | "CONNECTING" | "CONNECTED" | "RECONNECTING"

const websocketUrl = () => {
  const base = new URL(API_BASE_URL)
  base.protocol = base.protocol === "https:" ? "wss:" : "ws:"
  base.pathname = "/ws/chat"
  base.search = token.value ? `?token=${encodeURIComponent(token.value)}` : ""
  return base.toString()
}

export const useChatRealtime = (onEvent: (event: ChatSocketEventDTO) => void) => {
  const state = ref<ChatRealtimeState>("DISCONNECTED")
  const lastEvent = ref<ChatSocketEventDTO | null>(null)
  let socket: WebSocket | null = null
  let reconnectTimer: number | null = null
  let disposed = false
  let reconnectAttempt = 0

  const clearReconnectTimer = () => {
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const scheduleReconnect = () => {
    if (disposed || reconnectTimer !== null || !token.value || !navigator.onLine) return
    reconnectAttempt += 1
    const delay = Math.min(1000 * 2 ** Math.min(reconnectAttempt - 1, 4), 15000)
    state.value = "RECONNECTING"
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  const connect = () => {
    if (disposed || !token.value || !navigator.onLine || socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) return
    clearReconnectTimer()
    state.value = reconnectAttempt > 0 ? "RECONNECTING" : "CONNECTING"
    socket = new WebSocket(websocketUrl())
    socket.onopen = () => {
      reconnectAttempt = 0
      state.value = "CONNECTED"
    }
    socket.onmessage = (message) => {
      try {
        const event = JSON.parse(message.data) as ChatSocketEventDTO
        lastEvent.value = event
        onEvent(event)
      } catch {
        // Transport noise never replaces the authoritative HTTP sync path.
      }
    }
    socket.onerror = () => {
      state.value = "DISCONNECTED"
    }
    socket.onclose = () => {
      socket = null
      state.value = "DISCONNECTED"
      scheduleReconnect()
    }
  }

  const disconnect = () => {
    disposed = true
    clearReconnectTimer()
    socket?.close()
    socket = null
    state.value = "DISCONNECTED"
  }

  const handleOffline = () => {
    clearReconnectTimer()
    socket?.close()
    socket = null
    state.value = "DISCONNECTED"
  }

  const handleOnline = () => {
    if (!disposed) connect()
  }

  const reconnect = () => {
    if (disposed) return
    socket?.close()
    socket = null
    connect()
  }

  onBeforeUnmount(() => {
    window.removeEventListener("offline", handleOffline)
    window.removeEventListener("online", handleOnline)
    disconnect()
  })
  window.addEventListener("offline", handleOffline)
  window.addEventListener("online", handleOnline)

  return {state, lastEvent, connect, reconnect, disconnect}
}
