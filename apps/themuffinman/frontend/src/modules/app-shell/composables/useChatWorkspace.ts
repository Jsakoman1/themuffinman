import {computed, ref, type ComputedRef} from "vue"
import type {ChatConversationSummaryDTO, ChatMessageDTO, ChatSocketEventDTO} from "../../../contracts/index.ts"

export type ChatConversationSection = {id: string; label: string; items: ChatConversationSummaryDTO[]}

export const useChatWorkspace = (selectedId: ComputedRef<number | null>) => {
  const conversations = ref<ChatConversationSummaryDTO[]>([])
  const messages = ref<ChatMessageDTO[]>([])
  const activeTypingUserIds = ref<number[]>([])
  const seenUpToMessageId = ref<number | null>(null)
  const isUnread = (conversation: ChatConversationSummaryDTO) => conversation.lastMessageId !== null && conversation.lastSeenMessageId !== conversation.lastMessageId
  const isPriority = (conversation: ChatConversationSummaryDTO) => Boolean(conversation.contextType && ["BUSINESS", "BOOKING", "WORK"].includes(conversation.contextType))
  const selectedConversation = computed(() => conversations.value.find(item => item.conversationId === selectedId.value) ?? null)
  const unreadCount = computed(() => conversations.value.filter(conversation => !conversation.archived && !conversation.muted && isUnread(conversation)).length)
  const conversationSections = computed<ChatConversationSection[]>(() => [
    {id: "priority", label: "Priority", items: conversations.value.filter(item => !item.archived && !item.muted && isPriority(item))},
    {id: "unread", label: "Unread", items: conversations.value.filter(item => !item.archived && !item.muted && isUnread(item) && !isPriority(item))},
    {id: "other", label: "All conversations", items: conversations.value.filter(item => !item.archived && !item.muted && !isUnread(item) && !isPriority(item))},
    {id: "muted", label: "Muted", items: conversations.value.filter(item => !item.archived && item.muted)},
    {id: "archived", label: "Archived", items: conversations.value.filter(item => item.archived)}
  ].filter(section => section.items.length))
  const replaceMessage = (message: ChatMessageDTO) => { messages.value = messages.value.map(item => item.id === message.id ? message : item) }
  const appendMessageIfMissing = (message: ChatMessageDTO) => {
    if (messages.value.some(item => item.id === message.id)) return
    messages.value = [...messages.value, message]
  }
  const typingParticipantNames = computed(() => selectedConversation.value?.participants
    .filter(participant => activeTypingUserIds.value.includes(participant.userId))
    .map(participant => participant.username) ?? [])
  const latestOwnMessageSeen = computed(() => {
    const latestOwnMessage = [...messages.value].reverse().find(message => message.ownMessage)
    return Boolean(latestOwnMessage && (latestOwnMessage.seenAt || (seenUpToMessageId.value !== null && seenUpToMessageId.value >= latestOwnMessage.id)))
  })
  const applyRealtimePresence = (event: ChatSocketEventDTO) => {
    if (event.conversationId !== selectedId.value) return
    if (event.type === "chat.typing.updated" && event.actorUserId) {
      activeTypingUserIds.value = event.typing
        ? [...new Set([...activeTypingUserIds.value, event.actorUserId])]
        : activeTypingUserIds.value.filter(userId => userId !== event.actorUserId)
    }
    if (event.seenUpToMessageId !== null && event.seenUpToMessageId !== undefined) {
      seenUpToMessageId.value = Math.max(seenUpToMessageId.value ?? 0, event.seenUpToMessageId)
    }
  }
  const setActiveTypingUsers = (userIds: number[]) => { activeTypingUserIds.value = userIds }

  return {conversations, messages, selectedConversation, unreadCount, conversationSections, isUnread, replaceMessage, appendMessageIfMissing, typingParticipantNames, latestOwnMessageSeen, applyRealtimePresence, setActiveTypingUsers}
}
