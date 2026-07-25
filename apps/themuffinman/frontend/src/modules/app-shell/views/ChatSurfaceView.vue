<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {ChatAttachmentUploadDTO, ChatConversationSummaryDTO, ChatMessageDTO, CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppIconButton from "../components/AppIconButton.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import {useChatRealtime} from "../composables/useChatRealtime.ts"
import {formatDateTime} from "../../../services/formatters.ts"
import TaskSurface from "../components/TaskSurface.vue"

const route = useRoute()
const router = useRouter()
const conversations = ref<ChatConversationSummaryDTO[]>([])
const messages = ref<ChatMessageDTO[]>([])
const hasMoreConversations = ref(false)
const hasMoreMessages = ref(false)
const conversationPage = ref(0)
const nextMessageId = ref<number | null>(null)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
const syncStatus = ref("")
const actionFeedback = ref("")
const retryAction = ref<"load" | "sync" | "send" | null>(null)
const retryLabel = "Retry"
const realtimeStatus = ref("DISCONNECTED")
const isSyncing = ref(false)
const draft = ref("")
const editingId = ref<number | null>(null)
const editingDraft = ref("")
const replyingTo = ref<ChatMessageDTO | null>(null)
const attachment = ref<ChatAttachmentUploadDTO | null>(null)
const attachmentInput = ref<HTMLInputElement | null>(null)
const attachmentPreviewUrl = ref<string | null>(null)
const isUploadingAttachment = ref(false)
const isSending = ref(false)
const groupTitle = ref("")
const participantQuery = ref("")
const participantCandidates = ref<CircleSearchResultDTO[]>([])
const selectedParticipantIds = ref<number[]>([])
const isCreatingGroup = ref(false)
const isSearchingParticipants = ref(false)
const directQuery = ref("")
const directCandidates = ref<CircleSearchResultDTO[]>([])
const isOpeningDirect = ref(false)
const selectedId = computed(() => Number(route.params.conversationId) || null)
const selectedConversation = computed(() => conversations.value.find(item => item.conversationId === selectedId.value) ?? null)
const isUnread = (conversation: ChatConversationSummaryDTO) => conversation.lastMessageId !== null && conversation.lastSeenMessageId !== conversation.lastMessageId
const isPriority = (conversation: ChatConversationSummaryDTO) => Boolean(conversation.contextType && ["BUSINESS", "BOOKING", "WORK"].includes(conversation.contextType))
const conversationSections = computed(() => [
  {id: "priority", label: "Priority", items: conversations.value.filter(item => !item.archived && !item.muted && isPriority(item))},
  {id: "unread", label: "Unread", items: conversations.value.filter(item => !item.archived && !item.muted && isUnread(item) && !isPriority(item))},
  {id: "other", label: "All conversations", items: conversations.value.filter(item => !item.archived && !item.muted && !isUnread(item) && !isPriority(item))},
  {id: "muted", label: "Muted", items: conversations.value.filter(item => !item.archived && item.muted)},
  {id: "archived", label: "Archived", items: conversations.value.filter(item => item.archived)}
].filter(section => section.items.length))
const relatedContext = computed(() => {
  const safePath = (value: unknown) => typeof value === "string" && /^\/(business|work|chat|bookings)(\/|$)/.test(value) ? value : null
  return {business: safePath(route.query.businessPath), booking: safePath(route.query.bookingPath), work: safePath(route.query.workPath)}
})
const conversationContextRoute = computed(() => {
  const type = selectedConversation.value?.contextType
  if (type === "THING") return {label: "Open related thing", to: "/share/things"}
  if (type === "CIRCLE") return {label: "Open related circle", to: "/circles"}
  if (type === "BUSINESS") return {label: "Open related business", to: "/business"}
  if (type === "BOOKING") return {label: "Open related booking", to: "/business/my-bookings"}
  if (type === "WORK") return {label: "Open related work", to: "/work"}
  return null
})
const attachmentRequested = computed(() => route.query.attach === "1")
const formatDate = (value: string | null | undefined) => formatDateTime(value, "")
const describeRequestError = (errorValue: unknown, fallback: string) => {
  const response = (errorValue as {response?: {status?: number}} | null)?.response
  return response?.status ? `${fallback} (HTTP ${response.status}).` : fallback
}
const recoverUnavailableConversation = async (errorValue: unknown) => {
  const status = (errorValue as {response?: {status?: number}} | null)?.response?.status
  if (!selectedId.value || (status !== 403 && status !== 404)) return false
  messages.value = []
  syncStatus.value = "Conversation access changed. Refreshing your inbox…"
  await loadConversations()
  if (!selectedConversation.value) await router.push("/chat")
  return true
}

const loadConversations = async (append = false) => {
  if (!append) { isLoading.value = true; conversationPage.value = 0; conversations.value = [] } else { isLoadingMore.value = true }
  error.value = ""
  try {
    const page = await userShellApi.getChatConversations(conversationPage.value, 20)
    conversations.value = append ? [...conversations.value, ...page.conversations] : page.conversations
    hasMoreConversations.value = page.hasMore
  } catch (requestError) { error.value = describeRequestError(requestError, "Could not load conversations."); retryAction.value = "load" }
  finally { isLoading.value = false; isLoadingMore.value = false }
}
const loadMessages = async () => {
  if (!selectedId.value) { messages.value = []; return }
  try {
    const sync = await userShellApi.getChatConversationSync(selectedId.value)
    messages.value = sync.messages
    await userShellApi.markChatConversationRead(selectedId.value, sync.messages.at(-1)?.id)
    hasMoreMessages.value = false
    nextMessageId.value = null
    syncStatus.value = "Conversation synced."
  } catch (requestError) {
    if (await recoverUnavailableConversation(requestError)) return
    try {
      const page = await userShellApi.getChatMessages(selectedId.value, 30)
      messages.value = page.messages
      hasMoreMessages.value = page.hasMore
      nextMessageId.value = page.nextBeforeMessageId
      syncStatus.value = ""
    } catch (requestError) { error.value = describeRequestError(requestError, "Could not load messages."); retryAction.value = "sync" }
  }
}
const syncConversation = async () => {
  if (!selectedId.value) return
  isSyncing.value = true
  syncStatus.value = "Syncing conversation…"
  try {
    const hint = await userShellApi.getChatRefreshHint(selectedId.value, messages.value.at(-1)?.id)
    if (!hint.refreshRequired) { syncStatus.value = "Conversation is current."; return }
    const sync = await userShellApi.getChatConversationSync(selectedId.value)
    const existing = new Map(messages.value.map(message => [message.id, message]))
    sync.messages.forEach(message => existing.set(message.id, message))
    messages.value = [...existing.values()].sort((left, right) => left.id - right.id)
    syncStatus.value = "Conversation refreshed from the server."
    error.value = ""
  } catch (requestError) {
    if (!await recoverUnavailableConversation(requestError)) { syncStatus.value = "Could not sync conversation."; retryAction.value = "sync" }
  } finally { isSyncing.value = false }
}
const handleRealtimeEvent = (event: import("../../../contracts/index.ts").ChatSocketEventDTO) => {
  if (event.type === "chat.connection") {
    const recovering = realtimeStatus.value === "RECONNECTING" || realtimeStatus.value === "DISCONNECTED"
    realtimeStatus.value = event.connectionState ?? "CONNECTED"
    if ((event.resyncRequired || recovering) && selectedId.value) void syncConversation()
    return
  }
  if (event.conversationId === selectedId.value && event.message) {
    const existing = new Map(messages.value.map(message => [message.id, message]))
    existing.set(event.message.id, event.message)
    messages.value = [...existing.values()].sort((left, right) => left.id - right.id)
  }
  void loadConversations()
  if (event.conversationId === selectedId.value && !event.message) void syncConversation()
}
const chatRealtime = useChatRealtime(handleRealtimeEvent)
watch(chatRealtime.state, value => { realtimeStatus.value = value })
const realtimeLabel = computed(() => ({
  CONNECTED: "Connected",
  CONNECTING: "Connecting…",
  RECONNECTING: "Reconnecting…",
  DISCONNECTED: "Disconnected"
}[realtimeStatus.value] ?? "Unavailable"))
const realtimeNeedsAction = computed(() => realtimeStatus.value === "DISCONNECTED" || realtimeStatus.value === "RECONNECTING")
const retry = async () => {
  const action = retryAction.value
  retryAction.value = null
  if (action === "load") await loadConversations()
  if (action === "sync") await syncConversation()
  if (action === "send") await send()
}
const loadMoreConversations = async () => { if (!hasMoreConversations.value || isLoadingMore.value) return; conversationPage.value += 1; await loadConversations(true) }
const searchParticipants = async () => {
  const query = participantQuery.value.trim()
  if (query.length < 2) { participantCandidates.value = []; return }
  isSearchingParticipants.value = true
  try { participantCandidates.value = (await userShellApi.searchCircleUsers(query)).items } catch { error.value = "Could not search people."; retryAction.value = null } finally { isSearchingParticipants.value = false }
}
const toggleParticipant = (userId: number) => {
  selectedParticipantIds.value = selectedParticipantIds.value.includes(userId)
    ? selectedParticipantIds.value.filter(id => id !== userId)
    : [...selectedParticipantIds.value, userId]
}
const createGroup = async () => {
  if (!groupTitle.value.trim() || selectedParticipantIds.value.length === 0) return
  isCreatingGroup.value = true
  error.value = ""
  try {
    const created = await userShellApi.createChatGroup({title: groupTitle.value.trim(), participantUserIds: selectedParticipantIds.value})
    groupTitle.value = ""
    participantQuery.value = ""
    participantCandidates.value = []
    selectedParticipantIds.value = []
    await loadConversations()
    await router.push(`/chat/${created.conversationId}`)
  } catch { error.value = "Could not create the group conversation." } finally { isCreatingGroup.value = false }
}
const searchDirectParticipants = async () => {
  const query = directQuery.value.trim()
  if (query.length < 2) { directCandidates.value = []; return }
  isSearchingParticipants.value = true
  try { directCandidates.value = (await userShellApi.searchCircleUsers(query)).items } catch { error.value = "Could not search people." } finally { isSearchingParticipants.value = false }
}
const openDirectChat = async (userId: number) => {
  isOpeningDirect.value = true; error.value = ""
  try { const conversation = await userShellApi.openDirectChat(userId); directQuery.value = ""; directCandidates.value = []; await loadConversations(); await router.push(`/chat/${conversation.conversationId}`) } catch { error.value = "Could not open this conversation." } finally { isOpeningDirect.value = false }
}
const leaveGroup = async () => {
  if (!selectedId.value || selectedConversation.value?.conversationType !== "GROUP" || !await confirmAction("Leave this group conversation?", "Leave conversation")) return
  try {
    const transition = await userShellApi.leaveChatConversation(selectedId.value)
    actionFeedback.value = transition.replacementOwnerUserId
      ? `You left the group. Ownership moved to participant #${transition.replacementOwnerUserId}.`
      : (transition.message || "You left the group conversation.")
    await loadConversations()
    await router.push("/chat")
  } catch (requestError) { error.value = describeRequestError(requestError, "Could not leave the group conversation."); retryAction.value = "load"; await loadConversations() }
}
const loadOlderMessages = async () => {
  if (!selectedId.value || !hasMoreMessages.value || !nextMessageId.value) return
  const page = await userShellApi.getChatMessages(selectedId.value, 30, nextMessageId.value)
  messages.value = [...page.messages, ...messages.value]
  hasMoreMessages.value = page.hasMore
  nextMessageId.value = page.nextBeforeMessageId
}
const replaceMessage = (message: ChatMessageDTO) => { messages.value = messages.value.map(item => item.id === message.id ? message : item) }
const clearAttachmentPreview = () => { if (attachmentPreviewUrl.value) URL.revokeObjectURL(attachmentPreviewUrl.value); attachmentPreviewUrl.value = null }
const uploadAttachment = async (event: Event) => { const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return; clearAttachmentPreview(); attachmentPreviewUrl.value = file.type.startsWith("image/") ? URL.createObjectURL(file) : null; isUploadingAttachment.value = true; error.value = ""; try { attachment.value = await userShellApi.uploadChatAttachment(file); actionFeedback.value = "Attachment ready to send." } catch { clearAttachmentPreview(); error.value = "Could not upload this attachment." } finally { isUploadingAttachment.value = false } }
const removeAttachment = async () => {
  const uploadId = attachment.value?.uploadId
  attachment.value = null
  clearAttachmentPreview()
  if (uploadId) {
    try { await userShellApi.cancelChatAttachment(uploadId) } catch { /* local removal remains safe; server expiry is the fallback */ }
  }
}
const send = async () => { if (!selectedId.value || isSending.value || (!draft.value.trim() && !attachment.value)) return; isSending.value = true; error.value = ""; try { const message = await userShellApi.sendChatMessage(selectedId.value, draft.value.trim(), attachment.value, replyingTo.value?.id); messages.value = [...messages.value, message]; draft.value = ""; replyingTo.value = null; retryAction.value = null; actionFeedback.value = "Message sent."; removeAttachment() } catch { error.value = "Could not send this message. Your draft is still here."; retryAction.value = "send" } finally { isSending.value = false } }
const beginEdit = (message: ChatMessageDTO) => { editingId.value = message.id; editingDraft.value = message.messageBody ?? "" }
const saveEdit = async (message: ChatMessageDTO) => { if (!selectedId.value || !editingDraft.value.trim()) return; try { replaceMessage(await userShellApi.updateChatMessage(selectedId.value, message.id, editingDraft.value.trim())); editingId.value = null; actionFeedback.value = "Message updated." } catch { error.value = "Could not edit this message." } }
const remove = async (message: ChatMessageDTO) => { if (!selectedId.value || !await confirmAction("Delete this message?", "Delete message")) return; try { await userShellApi.deleteChatMessage(selectedId.value, message.id); messages.value = messages.value.map(item => item.id === message.id ? {...item, deleted: true, messageBody: null} : item); actionFeedback.value = "Message deleted." } catch { error.value = "Could not delete this message." } }
const toggleReaction = async (message: ChatMessageDTO) => { if (!selectedId.value) return; const own = message.reactions.find(reaction => reaction.ownReaction && reaction.emoji === "👍"); try { replaceMessage(own ? await userShellApi.removeChatReaction(selectedId.value, message.id, "👍") : await userShellApi.addChatReaction(selectedId.value, message.id, "👍")); actionFeedback.value = own ? "Reaction removed." : "Reaction added." } catch { error.value = "Could not update this reaction." } }
const recoverConversation = () => { if (document.visibilityState === "visible" && selectedId.value) void syncConversation() }
const recoverWhenOnline = () => { if (selectedId.value) void syncConversation() }
const draftKey = computed(() => selectedId.value ? `chat-draft:v2:${selectedId.value}` : "chat-draft:v2:new")
const persistDraft = () => { if (typeof window === "undefined") return; if (draft.value.trim()) window.localStorage.setItem(draftKey.value, draft.value); else window.localStorage.removeItem(draftKey.value) }
watch(draft, persistDraft)
watch(selectedId, () => {
  draft.value = typeof window === "undefined" ? "" : window.localStorage.getItem(draftKey.value) || ""
  void loadMessages()
  if (selectedId.value && attachmentRequested.value) window.setTimeout(() => attachmentInput.value?.focus(), 0)
})
onMounted(() => {
  void loadConversations()
  void loadMessages()
  const requestedUserId = Number(route.query.userId)
  if (Number.isInteger(requestedUserId) && requestedUserId > 0) void openDirectChat(requestedUserId)
  chatRealtime.connect()
  if (attachmentRequested.value && !selectedId.value) syncStatus.value = "Select a conversation to attach a file."
  document.addEventListener("visibilitychange", recoverConversation)
  window.addEventListener("online", recoverWhenOnline)
})
onBeforeUnmount(() => {
  document.removeEventListener("visibilitychange", recoverConversation)
  window.removeEventListener("online", recoverWhenOnline)
  clearAttachmentPreview()
})
</script>

<template>
  <!-- UX simplification: conversation selection is the first action; circle clubs reuse this group-chat surface. -->
  <!-- Post-start hardening marker: realtime recovery remains visible in the same task surface. -->
  <TaskSurface mode="workspace" label="Chat workspace"><section class="chat-surface" data-preview-model="shared-adjacent-preview" data-mental-model="browse-select-converse" :aria-busy="isLoading || isLoadingMore || isSyncing || isSending || undefined">
  <header class="chat-surface__header" aria-labelledby="chat-surface-title"><div><p class="chat-surface__eyebrow">Chat</p><h1 id="chat-surface-title">{{ selectedId ? "Conversation" : "Chat" }}</h1><small class="chat-surface__realtime-status" :class="`chat-surface__realtime-status--${realtimeStatus.toLowerCase()}`" role="status" aria-live="polite">{{ realtimeLabel }}</small></div><div class="chat-surface__header-actions"><RouterLink v-if="selectedId" to="/chat" class="chat-surface__back">Back</RouterLink><AppButton v-if="realtimeNeedsAction" type="button" tone="secondary" :loading="realtimeStatus === 'RECONNECTING'" @click="chatRealtime.reconnect">Reconnect</AppButton><details class="chat-surface__new-menu"><summary>New</summary><div><AppButton type="button" tone="secondary" @click="directQuery = directQuery ? '' : ' '" >Message</AppButton><AppButton type="button" tone="secondary" @click="groupTitle = groupTitle ? '' : ' '" >Group</AppButton></div></details></div></header>
    <form v-if="directQuery !== ''" class="chat-surface__direct-create" @submit.prevent><AppFormField label="Person"><input v-model="directQuery" placeholder="Find someone to chat with" aria-label="Find someone to chat with" @input="searchDirectParticipants"></AppFormField><span v-if="isSearchingParticipants">Searching…</span><div class="chat-surface__direct-candidates"><AppButton v-for="candidate in directCandidates" :key="candidate.id" type="button" tone="secondary" :loading="isOpeningDirect" @click="openDirectChat(candidate.id)">{{ candidate.username }}</AppButton></div></form>
    <form v-if="groupTitle !== '' || participantQuery !== ''" class="chat-surface__group-create" @submit.prevent="createGroup">
      <AppFormField label="Group name" required><input v-model="groupTitle" placeholder="Group name" maxlength="120" aria-label="Group name"></AppFormField>
      <AppFormField label="Participants"><input v-model="participantQuery" placeholder="Find people" aria-label="Find people" @input="searchParticipants"></AppFormField>
      <span v-if="isSearchingParticipants">Searching…</span>
      <label v-for="candidate in participantCandidates" :key="candidate.id" class="chat-surface__candidate"><input type="checkbox" :checked="selectedParticipantIds.includes(candidate.id)" @change="toggleParticipant(candidate.id)"><span>{{ candidate.username }}</span></label>
      <AppFormFooter><template #primary><AppButton type="submit" tone="primary" :loading="isCreatingGroup" :disabled="!groupTitle.trim() || selectedParticipantIds.length === 0">{{ isCreatingGroup ? "Creating…" : "Create group" }}</AppButton></template></AppFormFooter>
    </form>
    <AppStatus v-if="error" :message="error" tone="error" retry :retry-label="retryLabel" @retry="retry" />
    <AppStatus v-if="actionFeedback" :message="actionFeedback" tone="success" />
    <AppStatus v-if="selectedId && syncStatus" :message="syncStatus" :tone="syncStatus.startsWith('Could not') ? 'stale' : 'neutral'" :busy="isSyncing" :retry="syncStatus.startsWith('Could not')" @retry="syncConversation" />
    <div class="chat-surface__layout" data-chat-layout="two-pane">
      <aside class="chat-surface__index" aria-label="Conversations">
        <section v-for="section in conversationSections" :key="section.id" class="chat-surface__conversation-section" :aria-label="section.label"><h2>{{ section.label }}</h2><RouterLink v-for="conversation in section.items" :key="conversation.conversationId" :to="`/chat/${conversation.conversationId}`" class="chat-surface__conversation" :class="{'chat-surface__conversation--active': selectedId === conversation.conversationId, 'chat-surface__conversation--unread': isUnread(conversation)}"><strong>{{ conversation.title || conversation.otherUsername || `Conversation #${conversation.conversationId}` }}</strong><span>{{ conversation.lastMessageAt ? formatDate(conversation.lastMessageAt) : "No messages" }}<template v-if="isUnread(conversation)"> · Unread</template><template v-if="conversation.muted"> · Muted</template></span></RouterLink></section>
        <p v-if="!isLoading && conversations.length === 0" class="chat-surface__status" role="status">No conversations yet. Start a chat to see it here.</p>
        <AppButton v-if="hasMoreConversations" type="button" tone="quiet" :loading="isLoadingMore" @click="loadMoreConversations">{{ isLoadingMore ? "Loading" : "Load more" }}</AppButton>
      </aside>
      <main class="chat-surface__thread" aria-live="polite">
        <AppButton v-if="selectedConversation?.conversationType === 'GROUP'" type="button" tone="danger" @click="leaveGroup">Leave group</AppButton>
        <AppButton v-if="hasMoreMessages" type="button" tone="quiet" @click="loadOlderMessages">Load older messages</AppButton>
        <p v-if="!selectedId" class="chat-surface__status" role="status">{{ attachmentRequested ? "Select a conversation to attach a file." : "Select a conversation." }}</p>
        <div v-else-if="messages.length === 0" class="chat-surface__status" role="status">No messages yet. You can send the first message.</div>
        <article v-for="message in messages" v-else :key="message.id" class="chat-surface__message" :class="{'chat-surface__message--own': message.ownMessage}">
          <span class="chat-surface__message-author">{{ message.senderUsername }} · {{ formatDate(message.createdAt) }}</span><form v-if="editingId === message.id" @submit.prevent="saveEdit(message)"><input v-model="editingDraft"><AppButton type="submit" tone="primary">Save</AppButton><AppButton type="button" tone="secondary" @click="editingId = null">Cancel</AppButton></form><template v-else><p v-if="message.replyToMessageId" class="chat-surface__reply">Reply to message #{{ message.replyToMessageId }}</p><p>{{ message.deleted ? "Message deleted" : message.messageBody || message.attachmentName || "Attachment" }}</p><a v-if="message.attachmentUrl && !message.deleted && message.attachmentAvailability !== 'UNAVAILABLE'" class="chat-surface__attachment-link" :href="message.attachmentUrl" target="_blank" rel="noreferrer">Open attachment</a><small v-if="message.attachmentAvailability === 'UNAVAILABLE'" class="chat-surface__attachment-unavailable">Attachment unavailable. It may have expired or storage is temporarily unavailable.</small></template><div v-if="selectedId && !message.deleted" class="chat-surface__message-actions"><AppButton type="button" tone="quiet" @click="toggleReaction(message)">{{ message.reactions.some(reaction => reaction.ownReaction && reaction.emoji === "👍") ? "👍" : "Like" }}</AppButton><AppButton type="button" tone="quiet" @click="replyingTo = message">Reply</AppButton><AppIconButton v-if="message.ownMessage" label="Edit message" @click="beginEdit(message)"><span aria-hidden="true">✎</span></AppIconButton><AppIconButton v-if="message.ownMessage" label="Delete message" tone="danger" @click="remove(message)"><span aria-hidden="true">⌫</span></AppIconButton></div>
        </article>
      </main>
      <nav v-if="selectedId && (relatedContext.business || relatedContext.booking || relatedContext.work)" class="chat-surface__related" aria-label="Related context"><strong>Related context</strong><RouterLink v-if="relatedContext.business" :to="relatedContext.business">Open business</RouterLink><RouterLink v-if="relatedContext.booking" :to="relatedContext.booking">Open booking</RouterLink><RouterLink v-if="relatedContext.work" :to="relatedContext.work">Open work</RouterLink></nav>
      <form v-if="selectedId" class="chat-surface__composer" aria-label="Conversation composer" @submit.prevent="send"><p v-if="replyingTo" class="chat-surface__replying">Replying to {{ replyingTo.senderUsername }} <AppButton type="button" tone="quiet" @click="replyingTo = null">Cancel</AppButton></p><input v-model="draft" placeholder="Write a message." aria-label="Message" maxlength="2000" :disabled="isSending"><label class="chat-surface__attachment">{{ isUploadingAttachment ? "Uploading…" : attachment ? attachment.attachmentName : "Attach" }}<input ref="attachmentInput" type="file" accept="image/*,.pdf,.txt" @change="uploadAttachment" :disabled="isUploadingAttachment || isSending"></label><img v-if="attachmentPreviewUrl" class="chat-surface__attachment-preview" :src="attachmentPreviewUrl" alt="Selected attachment preview"><AppButton type="button" v-if="attachment" tone="danger" @click="removeAttachment">Remove</AppButton><AppButton type="submit" tone="primary" :loading="isSending" :disabled="isUploadingAttachment">{{ isSending ? "Sending…" : "Send" }}</AppButton></form>
    </div>
    <nav v-if="selectedId && conversationContextRoute" class="chat-surface__related chat-surface__related--context" aria-label="Conversation related context"><strong>Related context</strong><RouterLink :to="conversationContextRoute.to">{{ conversationContextRoute.label }}</RouterLink></nav>
  </section></TaskSurface>
</template>

<style scoped>
.chat-surface{max-width:none;gap:var(--space-3)}.chat-surface__layout{grid-template-columns:var(--workspace-rail-width) minmax(0,1fr);gap:0;min-height:clamp(32rem,68vh,48rem);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden;background:var(--surface-base)}.chat-surface__index,.chat-surface__thread{border:0;border-radius:0;box-shadow:none}.chat-surface__index{border-right:1px solid var(--border-subtle);padding:var(--space-2);background:var(--rail-canvas)}.chat-surface__thread{padding:var(--space-4);background:var(--surface-base)}.chat-surface__conversation{border-radius:var(--radius-control);padding:var(--space-2)}.chat-surface__conversation--active{background:var(--surface-selected);box-shadow:inset 2px 0 var(--accent)}.chat-surface__message{border:1px solid var(--border-subtle);border-radius:var(--radius-surface);box-shadow:none;background:var(--surface-raised)}.chat-surface__message--own{border-color:var(--accent);background:var(--accent-muted);color:var(--text)}.chat-surface__composer{border-radius:var(--radius-surface);box-shadow:none;background:var(--surface-raised)}.chat-surface__composer input,.chat-surface__attachment,.chat-surface__create-toggle,.chat-surface__back{border-radius:var(--radius-control)}.chat-surface__create-toggle{background:var(--control-bg);color:var(--text-muted)}.chat-surface__composer button[type=submit]{background:var(--accent);color:var(--canvas)}
.chat-surface{display:grid;gap:var(--space-3);max-width:none}.chat-surface__header{display:flex;justify-content:space-between;align-items:center;gap:var(--space-3)}.chat-surface__header-actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.chat-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.chat-surface__direct-create,.chat-surface__group-create{display:flex;gap:var(--space-2);flex-wrap:wrap;align-items:center;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.chat-surface__direct-create input,.chat-surface__group-create input{min-width:14rem;flex:1;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.chat-surface__direct-create button,.chat-surface__group-create button{min-height:var(--control-height-default);border:1px solid var(--accent);border-radius:var(--radius-control);padding:var(--space-1) var(--space-3);background:var(--accent);color:var(--canvas);font:inherit;font-size:var(--text-size-meta);cursor:pointer}.chat-surface__layout{display:grid;grid-template-columns:var(--workspace-rail-width) minmax(0,1fr) var(--detail-rail-width);gap:0;min-height:clamp(32rem,68vh,48rem);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden;background:var(--surface-base)}.chat-surface__index,.chat-surface__thread{border:0;border-radius:0;background:var(--surface-base);padding:var(--space-3)}.chat-surface__index{display:grid;align-content:start;gap:var(--space-1);border-right:1px solid var(--border-subtle);background:var(--rail-canvas)}.chat-surface__conversation{display:grid;gap:var(--space-1);padding:var(--space-2);border-radius:var(--radius-control)}.chat-surface__conversation span,.chat-surface__message-author{color:var(--text-muted);font-size:var(--text-size-meta)}.chat-surface__conversation--active{background:var(--surface-selected);box-shadow:inset 2px 0 var(--accent)}.chat-surface__thread{display:grid;align-content:start;gap:var(--space-2);overflow:auto}.chat-surface__message{max-width:72%;padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.chat-surface__message--own{justify-self:end;border-color:var(--accent);background:var(--accent-muted);color:var(--text)}.chat-surface__message p{margin:var(--space-1) 0 0}.chat-surface__message--own .chat-surface__message-author{color:var(--text-muted)}.chat-surface__more,.chat-surface__older{justify-self:start;border:1px solid var(--control-border);border-radius:var(--radius-control);background:transparent;color:var(--control-ink);padding:var(--space-1) var(--space-2);cursor:pointer}.chat-surface__status{padding:var(--space-3) 0;color:var(--text-muted)}.chat-surface__status--error{color:var(--danger)}.chat-surface__status button{margin-left:var(--space-2);border:0;background:transparent;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:980px){.chat-surface__layout{grid-template-columns:var(--workspace-rail-width) minmax(0,1fr)}.chat-surface__context{grid-column:2;border-top:1px solid var(--border-subtle);border-left:0}}@media(max-width:700px){.chat-surface__layout{grid-template-columns:1fr;grid-template-rows:auto minmax(0,1fr) auto}.chat-surface__index{max-height:14rem;border-right:0;border-bottom:1px solid var(--border-subtle);overflow:auto}.chat-surface__message{max-width:100%}}
.chat-surface__message-actions{display:flex;gap:var(--space-1);margin-top:var(--space-1)}.chat-surface__message-actions button,.chat-surface__composer button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:transparent;color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);cursor:pointer}.chat-surface__composer{display:flex;gap:var(--space-2);grid-column:2 / 3;align-items:center;flex-wrap:wrap;position:sticky;bottom:0;z-index:2;padding:var(--space-2);border:1px solid var(--border-strong);border-radius:var(--radius-surface);background:var(--surface-raised)}.chat-surface__composer input{flex:1;min-width:12rem;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.chat-surface__composer button{background:var(--accent);color:var(--canvas)}.chat-surface__attachment{display:inline-flex;align-items:center;min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font-size:var(--text-size-meta);cursor:pointer}.chat-surface__attachment input{display:none}.chat-surface__attachment-preview{width:3.5rem;height:3.5rem;object-fit:cover;border-radius:var(--radius-control);border:1px solid var(--border-subtle)}
.chat-surface__layout { grid-template-columns: var(--workspace-rail-width) minmax(0, 1fr); border: 0; border-radius: 0; background: transparent; }
.chat-surface__context, .chat-surface__related { display: none; }
.chat-surface__composer { grid-column: 2; }
.chat-surface__new-menu { position: relative; }
.chat-surface__new-menu > summary { display: inline-flex; align-items: center; min-height: var(--control-height-default); padding: var(--space-1) var(--space-3); border-radius: var(--radius-control); background: var(--accent); color: var(--canvas); cursor: pointer; list-style: none; font-weight: var(--text-weight-semibold); }
.chat-surface__new-menu > summary::-webkit-details-marker { display: none; }
.chat-surface__new-menu > div { position: absolute; right: 0; z-index: var(--z-popover); display: grid; gap: var(--space-1); min-width: 9rem; margin-top: var(--space-1); padding: var(--space-1); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }
.chat-surface__new-menu .app-button { width: 100%; justify-content: flex-start; border: 0; background: transparent; color: var(--text); }
.chat-surface__direct-candidates { display: flex; gap: var(--space-1); flex-wrap: wrap; }
.chat-surface__reply { margin: var(--space-1) 0 0; color: var(--text-muted); font-size: var(--text-size-meta); }
.chat-surface__replying { flex-basis: 100%; margin: 0; color: var(--text-muted); font-size: var(--text-size-meta); }
.chat-surface__status--error { padding: var(--space-2) var(--space-3); border: 1px solid color-mix(in srgb, var(--danger) 40%, var(--border-subtle)); border-radius: var(--radius-control); background: var(--danger-muted); }
.chat-surface .app-button { border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: var(--control-bg); color: var(--control-ink); }
.chat-surface .app-button--primary { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
.chat-surface__conversation { border: 1px solid transparent; transition: background-color 140ms ease, border-color 140ms ease; }
.chat-surface__conversation:hover { background: var(--surface-hover); border-color: var(--border-subtle); }
.chat-surface__message-actions { opacity: .7; transition: opacity 140ms ease; }
.chat-surface__message:hover .chat-surface__message-actions, .chat-surface__message:focus-within .chat-surface__message-actions { opacity: 1; }
@media (max-width: 700px) {
  .chat-surface__layout { grid-template-columns: 1fr; grid-template-rows: auto minmax(0, 1fr) auto; min-height: 0; }
  .chat-surface__index { max-height: 12rem; border-right: 0; border-bottom: 1px solid var(--border-subtle); overflow: auto; }
  .chat-surface__thread { min-height: 24rem; padding: var(--space-3); }
  .chat-surface__composer { grid-column: 1; min-width: 0; width: 100%; box-sizing: border-box; }
  .chat-surface__message { max-width: 100%; overflow-wrap: anywhere; }
  .chat-surface__header { align-items: flex-start; flex-direction: column; }
  .chat-surface__header-actions { width: 100%; flex-wrap: wrap; }
  .chat-surface__direct-create input, .chat-surface__group-create input { min-width: 0; width: 100%; }
}
.chat-surface__layout { grid-template-columns: minmax(16rem, 20rem) minmax(0, 1fr); }
.chat-surface__conversation strong { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 700px) { .chat-surface__layout { grid-template-columns: 1fr; } }
</style>
