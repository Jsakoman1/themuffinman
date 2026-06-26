import {onBeforeUnmount, ref, watch} from "vue"
import type {Router} from "vue-router"
import {currentUser} from "../../../auth.ts"
import {token} from "../../../services/sessionService.ts"
import {API_BASE_URL} from "../../../api/httpClient.ts"
import type {QuestNewsItem} from "../../../modules/workmarket/api/workmarketApi.ts"
import {workmarketApi} from "../../../modules/workmarket/api/workmarketApi.ts"
import {routeForNavigationTarget} from "../../../modules/workmarket/shared/navigationTargets.ts"

export const useTopbarNotifications = (router: Router) => {
  const notificationsOpen = ref(false)
  const unreadNewsCount = ref(0)
  const notificationItems = ref<QuestNewsItem[]>([])
  const isLoadingNotifications = ref(false)
  const notificationsError = ref("")
  let newsSocket: WebSocket | null = null
  let reconnectTimer: number | null = null

  const clearReconnectTimer = () => {
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const closeSocket = () => {
    clearReconnectTimer()
    const socket = newsSocket
    newsSocket = null
    if (!socket) {
      return
    }

    socket.onopen = null
    socket.onmessage = null
    socket.onerror = null
    socket.onclose = null
    socket.close()
  }

  const buildSocketUrl = () => {
    if (!token.value) {
      return null
    }

    const url = new URL(API_BASE_URL)
    url.protocol = url.protocol === "https:" ? "wss:" : "ws:"
    url.pathname = "/ws/chat"
    url.search = ""
    url.searchParams.set("token", token.value)
    return url.toString()
  }

  const connectSocket = () => {
    closeSocket()

    const socketUrl = buildSocketUrl()
    if (!socketUrl || !currentUser.value) {
      return
    }

    const socket = new WebSocket(socketUrl)
    newsSocket = socket

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(String(event.data)) as {
          type?: string
          unreadNewsCount?: number | null
        }

        if (payload.type !== "news.updated") {
          return
        }

        unreadNewsCount.value = typeof payload.unreadNewsCount === "number" ? payload.unreadNewsCount : unreadNewsCount.value
        if (notificationsOpen.value) {
          void loadNotifications()
        }
      } catch {
        // Ignore malformed socket payloads.
      }
    }

    socket.onclose = () => {
      if (newsSocket === socket) {
        newsSocket = null
      }
      if (currentUser.value && token.value) {
        clearReconnectTimer()
        reconnectTimer = window.setTimeout(() => {
          connectSocket()
        }, 3000)
      }
    }
  }

  const resetNotificationsState = () => {
    notificationItems.value = []
    notificationsError.value = ""
  }

  const updateCachedItemAsRead = (itemId: number) => {
    notificationItems.value = notificationItems.value.map((candidate) => candidate.id === itemId
      ? {...candidate, readAt: candidate.readAt ?? new Date().toISOString()}
      : candidate
    )
  }

  const removeCachedItem = (itemId: number) => {
    notificationItems.value = notificationItems.value.filter((candidate) => candidate.id !== itemId)
  }

  const refreshUnreadNewsCount = async () => {
    if (!currentUser.value) {
      unreadNewsCount.value = 0
      return
    }

    try {
      const summary = await workmarketApi.getDashboardSummary()
      unreadNewsCount.value = summary.unreadNewsCount
    } catch {
      unreadNewsCount.value = 0
    }
  }

  const loadNotifications = async () => {
    if (!currentUser.value) {
      resetNotificationsState()
      return
    }

    isLoadingNotifications.value = true
    notificationsError.value = ""

    try {
      notificationItems.value = await workmarketApi.getMyNews()
    } catch {
      notificationsError.value = "Could not load notifications."
      notificationItems.value = []
    } finally {
      isLoadingNotifications.value = false
    }
  }

  const toggleNotifications = async () => {
    if (notificationsOpen.value) {
      notificationsOpen.value = false
      return
    }

    notificationsOpen.value = true
    await loadNotifications()
    await refreshUnreadNewsCount()
  }

  const markAllNotificationsRead = async () => {
    await workmarketApi.markMyNewsAsRead()
    unreadNewsCount.value = 0
    notificationItems.value = notificationItems.value.map((item) => ({
      ...item,
      readAt: item.readAt ?? new Date().toISOString()
    }))
  }

  const openNotificationItem = async (item: QuestNewsItem) => {
    await workmarketApi.markMyNewsItemAsRead(item.id)
    unreadNewsCount.value = Math.max(0, unreadNewsCount.value - (item.readAt === null ? 1 : 0))
    updateCachedItemAsRead(item.id)
    notificationsOpen.value = false
    await router.push(routeForNavigationTarget(item.navigation))
  }

  const acceptCircleRequestFromNotification = async (item: QuestNewsItem) => {
    if (item.circleRequestId === null || item.circleRequestId === undefined) {
      return
    }

    await workmarketApi.acceptCircleRequest(item.circleRequestId)
    await workmarketApi.markMyNewsItemAsRead(item.id)
    unreadNewsCount.value = Math.max(0, unreadNewsCount.value - (item.readAt === null ? 1 : 0))
    removeCachedItem(item.id)
    if (notificationsOpen.value) {
      await loadNotifications()
    }
  }

  const declineCircleRequestFromNotification = async (item: QuestNewsItem) => {
    if (item.circleRequestId === null || item.circleRequestId === undefined) {
      return
    }

    await workmarketApi.deleteCircleRequest(item.circleRequestId)
    await workmarketApi.markMyNewsItemAsRead(item.id)
    unreadNewsCount.value = Math.max(0, unreadNewsCount.value - (item.readAt === null ? 1 : 0))
    removeCachedItem(item.id)
    if (notificationsOpen.value) {
      await loadNotifications()
    }
  }

  watch(() => currentUser.value?.id, async (userId) => {
    resetNotificationsState()
    closeSocket()

    if (!userId) {
      unreadNewsCount.value = 0
      return
    }

    await refreshUnreadNewsCount()
    connectSocket()
  })

  onBeforeUnmount(() => {
    closeSocket()
  })

  return {
    notificationsOpen,
    unreadNewsCount,
    notificationItems,
    isLoadingNotifications,
    notificationsError,
    refreshUnreadNewsCount,
    toggleNotifications,
    markAllNotificationsRead,
    openNotificationItem,
    acceptCircleRequestFromNotification,
    declineCircleRequestFromNotification,
    resetNotificationsState
  }
}
