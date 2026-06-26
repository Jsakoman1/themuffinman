import {computed, onBeforeUnmount, ref, watch} from "vue"
import {currentUser} from "../../../services/sessionService.ts"
import {API_BASE_URL} from "../../../api/httpClient.ts"
import {token} from "../../identity/auth.ts"
import type {ChatContact, ChatConversationSummary, ChatMessage, ChatWorkspace} from "../../../contracts/index.ts"
import {chatApi} from "../api/chatApi.ts"
import {compressQuestImageFile} from "../../../shared/imageCompression.ts"

type ChatWindowState = {
  conversationId: number
  otherUserId: number
  otherUsername: string
  otherUserProfileDescription: string | null
  otherUserAvatarDataUrl: string | null
  otherUserOnline: boolean
  otherUserLastActiveAt: string | null
  unreadCount: number
  messages: ChatMessage[]
  draftMessage: string
  draftImageDataUrl: string | null
  minimized: boolean
  isLoading: boolean
  isSending: boolean
  error: string
}

type ChatSocketEvent = {
  type?: string
  conversationId?: number | null
  actorUserId?: number | null
  reason?: string | null
}

const CHAT_SOCKET_PING_MS = 30000
const CHAT_SOCKET_RECONNECT_MS = 3000
const OPEN_WINDOWS_LIMIT = 3

export const useAppChat = () => {
  const workspace = ref<ChatWorkspace | null>(null)
  const launcherOpen = ref(false)
  const isLoadingWorkspace = ref(false)
  const workspaceError = ref("")
  const contactQuery = ref("")
  const activeCircleFilter = ref<number | "all">("all")
  const openWindows = ref<ChatWindowState[]>([])
  const activeConversationId = ref<number | null>(null)

  let heartbeatTimer: number | null = null
  let reconnectTimer: number | null = null
  let chatSocket: WebSocket | null = null
  let isRefreshingWorkspace = false
  let queuedWorkspaceRefresh = false

  const isAuthenticated = computed(() => !!currentUser.value)
  const contacts = computed(() => workspace.value?.contacts ?? [])
  const conversations = computed(() => workspace.value?.conversations ?? [])
  const circles = computed(() => workspace.value?.circles ?? [])
  const unreadConversationCount = computed(() => workspace.value?.unreadConversationCount ?? 0)
  const onlineContactCount = computed(() => workspace.value?.onlineContactCount ?? 0)
  const minimizedWindows = computed(() => openWindows.value.filter((windowState) => windowState.minimized))

  const findWindowState = (conversationId: number) => {
    return openWindows.value.find((windowState) => windowState.conversationId === conversationId) ?? null
  }

  const syncWorkspaceConversation = (
    conversationId: number,
    updater: (summary: ChatConversationSummary) => ChatConversationSummary
  ) => {
    if (!workspace.value) {
      return
    }

    workspace.value = {
      ...workspace.value,
      conversations: workspace.value.conversations.map((summary) => summary.conversationId === conversationId ? updater(summary) : summary)
    }
  }

  const filteredContacts = computed(() => {
    const normalizedQuery = contactQuery.value.trim().toLowerCase()
    return contacts.value.filter((contact) => {
      const matchesCircle = activeCircleFilter.value === "all" || contact.circleIds.includes(activeCircleFilter.value)
      const matchesQuery = !normalizedQuery
        || contact.username.toLowerCase().includes(normalizedQuery)
        || (contact.circleNames.join(" ").toLowerCase().includes(normalizedQuery))
      return matchesCircle && matchesQuery
    })
  })

  const refreshWorkspace = async () => {
    if (!isAuthenticated.value) {
      return
    }

    if (isRefreshingWorkspace) {
      queuedWorkspaceRefresh = true
      return
    }

    isRefreshingWorkspace = true
    isLoadingWorkspace.value = workspace.value === null
    try {
      workspace.value = await chatApi.getWorkspace()
      workspaceError.value = ""
      syncOpenWindowsFromWorkspace()
      await refreshOpenWindowsMessages()
    } catch (error) {
      workspaceError.value = error instanceof Error ? error.message : "Could not load chat."
    } finally {
      isRefreshingWorkspace = false
      isLoadingWorkspace.value = false
      if (queuedWorkspaceRefresh) {
        queuedWorkspaceRefresh = false
        void refreshWorkspace()
      }
    }
  }

  const refreshOpenWindowsMessages = async () => {
    await Promise.all(openWindows.value.map(async (windowState) => {
      try {
        const messages = await chatApi.getConversationMessages(windowState.conversationId)
        const currentWindowState = findWindowState(windowState.conversationId)
        if (!currentWindowState) {
          return
        }

        currentWindowState.messages = messages
        currentWindowState.error = ""
        if (currentWindowState.unreadCount > 0) {
          await chatApi.markConversationRead(currentWindowState.conversationId)
        }
      } catch {
        const currentWindowState = findWindowState(windowState.conversationId)
        if (currentWindowState) {
          currentWindowState.error = "Could not refresh this chat."
        }
      }
    }))
  }

  const refreshConversation = async (conversationId: number) => {
    const windowState = findWindowState(conversationId)
    if (!windowState) {
      return
    }

    try {
      const messages = await chatApi.getConversationMessages(conversationId)
      windowState.messages = messages
      windowState.error = ""
      if (windowState.unreadCount > 0) {
        await chatApi.markConversationRead(conversationId)
        windowState.unreadCount = 0
        syncWorkspaceConversation(conversationId, (conversation) => ({
          ...conversation,
          unreadCount: 0
        }))
      }
    } catch {
      windowState.error = "Could not refresh this chat."
    }
  }

  const sendPresencePing = () => {
    if (chatSocket?.readyState !== WebSocket.OPEN) {
      return
    }

    chatSocket.send(JSON.stringify({
      type: "chat.presence.ping"
    }))
  }

  const buildChatSocketUrl = () => {
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

  const clearReconnectTimer = () => {
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const closeSocket = () => {
    const socket = chatSocket
    chatSocket = null
    if (socket) {
      socket.onopen = null
      socket.onmessage = null
      socket.onerror = null
      socket.onclose = null
      socket.close()
    }
  }

  const handleSocketEvent = async (event: ChatSocketEvent) => {
    if (event.type !== "chat.workspace.updated") {
      return
    }

    await refreshWorkspace()
    if (typeof event.conversationId === "number") {
      await refreshConversation(event.conversationId)
    }
  }

  const connectSocket = () => {
    clearReconnectTimer()
    closeSocket()

    const url = buildChatSocketUrl()
    if (!url || !isAuthenticated.value) {
      return
    }

    const socket = new WebSocket(url)
    chatSocket = socket

    socket.onopen = () => {
      sendPresencePing()
    }

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(String(event.data)) as ChatSocketEvent
        void handleSocketEvent(payload)
      } catch {
        // Ignore malformed socket payloads.
      }
    }

    socket.onclose = () => {
      if (chatSocket === socket) {
        chatSocket = null
      }
      if (isAuthenticated.value) {
        clearReconnectTimer()
        reconnectTimer = window.setTimeout(() => {
          connectSocket()
        }, CHAT_SOCKET_RECONNECT_MS)
      }
    }
  }

  const stopRealtime = () => {
    clearReconnectTimer()
    if (heartbeatTimer !== null) {
      window.clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
    closeSocket()
  }

  const startRealtime = () => {
    stopRealtime()
    connectSocket()
    heartbeatTimer = window.setInterval(() => {
      sendPresencePing()
    }, CHAT_SOCKET_PING_MS)
  }

  const syncOpenWindowsFromWorkspace = () => {
    const summariesByConversationId = new Map(conversations.value.map((summary) => [summary.conversationId, summary]))
    openWindows.value.forEach((windowState) => {
      const summary = summariesByConversationId.get(windowState.conversationId)
      if (!summary) {
        return
      }

      windowState.otherUsername = summary.otherUsername
      windowState.otherUserProfileDescription = summary.otherUserProfileDescription ?? null
      windowState.otherUserAvatarDataUrl = summary.otherUserAvatarDataUrl ?? null
      windowState.otherUserOnline = summary.otherUserOnline
      windowState.otherUserLastActiveAt = summary.otherUserLastActiveAt ?? null
      windowState.unreadCount = summary.unreadCount
    })
  }

  const ensureChatWindow = (summary: ChatConversationSummary) => {
    const existingIndex = openWindows.value.findIndex((windowState) => windowState.conversationId === summary.conversationId)
    if (existingIndex >= 0) {
      const existingWindow = openWindows.value.splice(existingIndex, 1)[0]
      openWindows.value.push({
        ...existingWindow,
        otherUsername: summary.otherUsername,
        otherUserProfileDescription: summary.otherUserProfileDescription ?? null,
        otherUserAvatarDataUrl: summary.otherUserAvatarDataUrl ?? null,
        otherUserOnline: summary.otherUserOnline,
        otherUserLastActiveAt: summary.otherUserLastActiveAt ?? null,
        unreadCount: summary.unreadCount
      })
      activeConversationId.value = summary.conversationId
      return openWindows.value.at(-1) ?? null
    }

    const nextWindow: ChatWindowState = {
      conversationId: summary.conversationId,
      otherUserId: summary.otherUserId,
      otherUsername: summary.otherUsername,
      otherUserProfileDescription: summary.otherUserProfileDescription ?? null,
      otherUserAvatarDataUrl: summary.otherUserAvatarDataUrl ?? null,
      otherUserOnline: summary.otherUserOnline,
      otherUserLastActiveAt: summary.otherUserLastActiveAt ?? null,
      unreadCount: summary.unreadCount,
      messages: [],
      draftMessage: "",
      draftImageDataUrl: null,
      minimized: false,
      isLoading: false,
      isSending: false,
      error: ""
    }

    openWindows.value.push(nextWindow)
    if (openWindows.value.length > OPEN_WINDOWS_LIMIT) {
      openWindows.value.shift()
    }

    activeConversationId.value = summary.conversationId

    return nextWindow
  }

  const openConversationSummary = async (summary: ChatConversationSummary) => {
    const windowState = ensureChatWindow(summary)
    launcherOpen.value = false
    if (!windowState) {
      return
    }

    windowState.isLoading = true
    try {
      const messages = await chatApi.getConversationMessages(windowState.conversationId)
      const currentWindowState = findWindowState(windowState.conversationId)
      if (!currentWindowState) {
        return
      }

      currentWindowState.messages = messages
      currentWindowState.error = ""
      await chatApi.markConversationRead(currentWindowState.conversationId)
      currentWindowState.unreadCount = 0
      syncWorkspaceConversation(currentWindowState.conversationId, (conversation) => ({
        ...conversation,
        unreadCount: 0
      }))
    } catch (error) {
      const currentWindowState = findWindowState(windowState.conversationId)
      if (currentWindowState) {
        currentWindowState.error = error instanceof Error ? error.message : "Could not open this chat."
      }
    } finally {
      const currentWindowState = findWindowState(windowState.conversationId)
      if (currentWindowState) {
        currentWindowState.isLoading = false
      }
    }
  }

  const openConversationForContact = async (contact: ChatContact) => {
    try {
      const summary = await chatApi.openConversation({otherUserId: contact.userId})
      await openConversationSummary(summary)
    } catch (error) {
      workspaceError.value = error instanceof Error ? error.message : "Could not open this chat."
    }
  }

  const closeConversation = (conversationId: number) => {
    openWindows.value = openWindows.value.filter((windowState) => windowState.conversationId !== conversationId)
    if (activeConversationId.value === conversationId) {
      activeConversationId.value = openWindows.value.at(-1)?.conversationId ?? null
    }
  }

  const toggleWindowMinimized = (conversationId: number) => {
    openWindows.value = openWindows.value.map((windowState) => windowState.conversationId === conversationId
      ? {
        ...windowState,
        minimized: !windowState.minimized
      }
      : windowState)
    activeConversationId.value = conversationId
  }

  const reopenConversation = async (windowState: ChatWindowState) => {
    if (!windowState.minimized && activeConversationId.value === windowState.conversationId) {
      toggleWindowMinimized(windowState.conversationId)
      return
    }

    if (windowState.minimized) {
      toggleWindowMinimized(windowState.conversationId)
    }
    activeConversationId.value = windowState.conversationId
    await openConversationSummary({
      conversationId: windowState.conversationId,
      otherUserId: windowState.otherUserId,
      otherUsername: windowState.otherUsername,
      otherUserProfileDescription: windowState.otherUserProfileDescription,
      otherUserAvatarDataUrl: windowState.otherUserAvatarDataUrl,
      otherUserOnline: windowState.otherUserOnline,
      otherUserLastActiveAt: windowState.otherUserLastActiveAt,
      lastMessagePreview: null,
      lastMessageAt: null,
      lastMessageFromCurrentUser: false,
      lastMessageHasImage: false,
      unreadCount: windowState.unreadCount
    })
  }

  const uploadImage = async (windowState: ChatWindowState, event: Event) => {
    const input = event.target as HTMLInputElement | null
    const file = input?.files?.[0]
    if (!file) {
      return
    }

    try {
      windowState.draftImageDataUrl = await compressQuestImageFile(file)
      windowState.error = ""
    } catch (error) {
      windowState.error = error instanceof Error ? error.message : "Could not prepare the image."
    } finally {
      if (input) {
        input.value = ""
      }
    }
  }

  const removeDraftImage = (windowState: ChatWindowState) => {
    windowState.draftImageDataUrl = null
  }

  const sendMessage = async (windowState: ChatWindowState) => {
    const messageBody = windowState.draftMessage.trim()
    const imageDataUrl = windowState.draftImageDataUrl
    if (!messageBody && !imageDataUrl) {
      return
    }

    windowState.isSending = true
    try {
      const sentMessage = await chatApi.sendMessage(windowState.conversationId, {
        messageBody: messageBody || undefined,
        imageDataUrl: imageDataUrl ?? undefined
      })
      windowState.messages = [...windowState.messages, sentMessage]
      windowState.draftMessage = ""
      windowState.draftImageDataUrl = null
      windowState.error = ""
      windowState.unreadCount = 0
      syncWorkspaceConversation(windowState.conversationId, (conversation) => ({
        ...conversation,
        lastMessagePreview: sentMessage.messageBody ?? (sentMessage.imageDataUrl ? "Photo" : conversation.lastMessagePreview),
        lastMessageAt: sentMessage.createdAt,
        lastMessageFromCurrentUser: true,
        lastMessageHasImage: !!sentMessage.imageDataUrl,
        unreadCount: 0
      }))
    } catch (error) {
      windowState.error = error instanceof Error ? error.message : "Could not send the message."
    } finally {
      windowState.isSending = false
    }
  }

  watch(isAuthenticated, async (authenticated) => {
    stopRealtime()
    if (!authenticated) {
      workspace.value = null
      launcherOpen.value = false
      openWindows.value = []
      activeConversationId.value = null
      return
    }

    await refreshWorkspace()
    startRealtime()
  }, {immediate: true})

  onBeforeUnmount(() => {
    stopRealtime()
  })

  return {
    isAuthenticated,
    launcherOpen,
    isLoadingWorkspace,
    workspaceError,
    contactQuery,
    activeCircleFilter,
    filteredContacts,
    contacts,
    conversations,
    circles,
    openWindows,
    minimizedWindows,
    activeConversationId,
    unreadConversationCount,
    onlineContactCount,
    toggleLauncher: () => {
      launcherOpen.value = !launcherOpen.value
    },
    openConversationSummary,
    openConversationForContact,
    closeConversation,
    toggleWindowMinimized,
    reopenConversation,
    uploadImage,
    removeDraftImage,
    sendMessage
  }
}
