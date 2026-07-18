<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {ChatAttachmentUploadDTO, ChatConversationSummaryDTO, ChatMessageDTO, CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppIconButton from "../components/AppIconButton.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

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
const draft = ref("")
const editingId = ref<number | null>(null)
const editingDraft = ref("")
const replyingTo = ref<ChatMessageDTO | null>(null)
const attachment = ref<ChatAttachmentUploadDTO | null>(null)
const attachmentPreviewUrl = ref<string | null>(null)
const isUploadingAttachment = ref(false)
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
const formatDate = (value: string | null | undefined) => value ? new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value)) : ""
const describeRequestError = (errorValue: unknown, fallback: string) => {
  const response = (errorValue as {response?: {status?: number}} | null)?.response
  return response?.status ? `${fallback} (HTTP ${response.status}).` : fallback
}

const loadConversations = async (append = false) => {
  if (!append) { isLoading.value = true; conversationPage.value = 0; conversations.value = [] } else { isLoadingMore.value = true }
  error.value = ""
  try {
    const page = await userShellApi.getChatConversations(conversationPage.value, 20)
    conversations.value = append ? [...conversations.value, ...page.conversations] : page.conversations
    hasMoreConversations.value = page.hasMore
  } catch (requestError) { error.value = describeRequestError(requestError, "Could not load conversations.") }
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
  } catch {
    try {
      const page = await userShellApi.getChatMessages(selectedId.value, 30)
      messages.value = page.messages
      hasMoreMessages.value = page.hasMore
      nextMessageId.value = page.nextBeforeMessageId
      syncStatus.value = ""
    } catch (requestError) { error.value = describeRequestError(requestError, "Could not load messages.") }
  }
}
const syncConversation = async () => {
  if (!selectedId.value) return
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
  } catch { syncStatus.value = "Could not sync conversation." }
}
const loadMoreConversations = async () => { if (!hasMoreConversations.value || isLoadingMore.value) return; conversationPage.value += 1; await loadConversations(true) }
const searchParticipants = async () => {
  const query = participantQuery.value.trim()
  if (query.length < 2) { participantCandidates.value = []; return }
  isSearchingParticipants.value = true
  try { participantCandidates.value = (await userShellApi.searchCircleUsers(query)).items } catch { error.value = "Could not search people." } finally { isSearchingParticipants.value = false }
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
  try { const conversation = await userShellApi.openChat({otherUserId: userId}); directQuery.value = ""; directCandidates.value = []; await loadConversations(); await router.push(`/chat/${conversation.conversationId}`) } catch { error.value = "Could not open this conversation." } finally { isOpeningDirect.value = false }
}
const leaveGroup = async () => {
  if (!selectedId.value || selectedConversation.value?.conversationType !== "GROUP" || !await confirmAction("Leave this group conversation?", "Leave conversation")) return
  try {
    await userShellApi.leaveChatConversation(selectedId.value)
    await loadConversations()
    await router.push("/chat")
  } catch { error.value = "Could not leave the group conversation." }
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
const uploadAttachment = async (event: Event) => { const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return; clearAttachmentPreview(); attachmentPreviewUrl.value = file.type.startsWith("image/") ? URL.createObjectURL(file) : null; isUploadingAttachment.value = true; error.value = ""; try { attachment.value = await userShellApi.uploadChatAttachment(file) } catch { clearAttachmentPreview(); error.value = "Could not upload this attachment." } finally { isUploadingAttachment.value = false } }
const removeAttachment = () => { attachment.value = null; clearAttachmentPreview() }
const send = async () => { if (!selectedId.value || (!draft.value.trim() && !attachment.value)) return; error.value = ""; try { const message = await userShellApi.sendChatMessage(selectedId.value, draft.value.trim(), attachment.value, replyingTo.value?.id); messages.value = [...messages.value, message]; draft.value = ""; replyingTo.value = null; removeAttachment() } catch { error.value = "Could not send this message." } }
const beginEdit = (message: ChatMessageDTO) => { editingId.value = message.id; editingDraft.value = message.messageBody ?? "" }
const saveEdit = async (message: ChatMessageDTO) => { if (!selectedId.value || !editingDraft.value.trim()) return; try { replaceMessage(await userShellApi.updateChatMessage(selectedId.value, message.id, editingDraft.value.trim())); editingId.value = null } catch { error.value = "Could not edit this message." } }
const remove = async (message: ChatMessageDTO) => { if (!selectedId.value || !await confirmAction("Delete this message?", "Delete message")) return; try { await userShellApi.deleteChatMessage(selectedId.value, message.id); messages.value = messages.value.map(item => item.id === message.id ? {...item, deleted: true, messageBody: null} : item) } catch { error.value = "Could not delete this message." } }
const toggleReaction = async (message: ChatMessageDTO) => { if (!selectedId.value) return; const own = message.reactions.find(reaction => reaction.ownReaction && reaction.emoji === "👍"); try { replaceMessage(own ? await userShellApi.removeChatReaction(selectedId.value, message.id, "👍") : await userShellApi.addChatReaction(selectedId.value, message.id, "👍")) } catch { error.value = "Could not update this reaction." } }
const recoverConversation = () => { if (document.visibilityState === "visible" && selectedId.value) void syncConversation() }
const recoverWhenOnline = () => { if (selectedId.value) void syncConversation() }
watch(selectedId, () => void loadMessages())
onMounted(() => {
  void loadConversations()
  void loadMessages()
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
  <section class="chat-surface">
    <header class="chat-surface__header"><div><p class="chat-surface__eyebrow">Chat</p><h1>{{ selectedId ? "Conversation" : "Inbox" }}</h1></div><div class="chat-surface__header-actions"><RouterLink v-if="selectedId" to="/chat" class="chat-surface__back">Back to inbox</RouterLink><button type="button" class="chat-surface__create-toggle" @click="directQuery = directQuery ? '' : ' '" >New chat</button><button type="button" class="chat-surface__create-toggle" @click="groupTitle = groupTitle ? '' : ' '" >New group</button></div></header>
    <form v-if="directQuery !== ''" class="chat-surface__direct-create" @submit.prevent><input v-model="directQuery" placeholder="Find someone to chat with" aria-label="Find someone to chat with" @input="searchDirectParticipants"><span v-if="isSearchingParticipants">Searching…</span><button v-for="candidate in directCandidates" :key="candidate.id" type="button" :disabled="isOpeningDirect" @click="openDirectChat(candidate.id)">{{ candidate.username }}</button></form>
    <form v-if="groupTitle !== '' || participantQuery !== ''" class="chat-surface__group-create" @submit.prevent="createGroup">
      <input v-model="groupTitle" placeholder="Group name" maxlength="120" aria-label="Group name">
      <input v-model="participantQuery" placeholder="Find people" aria-label="Find people" @input="searchParticipants">
      <span v-if="isSearchingParticipants">Searching…</span>
      <label v-for="candidate in participantCandidates" :key="candidate.id" class="chat-surface__candidate"><input type="checkbox" :checked="selectedParticipantIds.includes(candidate.id)" @change="toggleParticipant(candidate.id)"><span>{{ candidate.username }}</span></label>
      <button type="submit" :disabled="isCreatingGroup || !groupTitle.trim() || selectedParticipantIds.length === 0">{{ isCreatingGroup ? "Creating…" : "Create group" }}</button>
    </form>
    <div v-if="error" class="chat-surface__status chat-surface__status--error" role="alert">{{ error }} <button type="button" @click="loadConversations()">Retry</button></div>
    <div v-if="selectedId && syncStatus" class="chat-surface__status" role="status">{{ syncStatus }} <button type="button" @click="syncConversation">Sync</button></div>
    <div class="chat-surface__layout">
      <aside class="chat-surface__index" aria-label="Conversations">
        <RouterLink v-for="conversation in conversations" :key="conversation.conversationId" :to="`/chat/${conversation.conversationId}`" class="chat-surface__conversation" :class="{'chat-surface__conversation--active': selectedId === conversation.conversationId}">
          <strong>{{ conversation.title || conversation.otherUsername || `Conversation #${conversation.conversationId}` }}</strong><span>{{ conversation.lastMessageAt ? formatDate(conversation.lastMessageAt) : "No messages" }}</span>
        </RouterLink>
        <button v-if="hasMoreConversations" type="button" class="chat-surface__more" :disabled="isLoadingMore" @click="loadMoreConversations">{{ isLoadingMore ? "Loading" : "Load more" }}</button>
      </aside>
      <main class="chat-surface__thread" aria-live="polite">
        <button v-if="selectedConversation?.conversationType === 'GROUP'" type="button" class="chat-surface__leave" @click="leaveGroup">Leave group</button>
        <button v-if="hasMoreMessages" type="button" class="chat-surface__older" @click="loadOlderMessages">Load older messages</button>
        <p v-if="!selectedId" class="chat-surface__status">Select a conversation.</p>
        <div v-else-if="messages.length === 0" class="chat-surface__status">No messages yet.</div>
        <article v-for="message in messages" v-else :key="message.id" class="chat-surface__message" :class="{'chat-surface__message--own': message.ownMessage}">
          <span class="chat-surface__message-author">{{ message.senderUsername }} · {{ formatDate(message.createdAt) }}</span><form v-if="editingId === message.id" @submit.prevent="saveEdit(message)"><input v-model="editingDraft"><button type="submit">Save</button><button type="button" @click="editingId = null">Cancel</button></form><template v-else><p v-if="message.replyToMessageId" class="chat-surface__reply">Reply to message #{{ message.replyToMessageId }}</p><p>{{ message.deleted ? "Message deleted" : message.messageBody || message.attachmentName || "Attachment" }}</p><a v-if="message.attachmentUrl && !message.deleted" class="chat-surface__attachment-link" :href="message.attachmentUrl" target="_blank" rel="noreferrer">Open attachment</a></template><div v-if="selectedId && !message.deleted" class="chat-surface__message-actions"><button type="button" @click="toggleReaction(message)">{{ message.reactions.some(reaction => reaction.ownReaction && reaction.emoji === "👍") ? "👍" : "Like" }}</button><button type="button" @click="replyingTo = message">Reply</button><AppIconButton v-if="message.ownMessage" label="Edit message" @click="beginEdit(message)"><span aria-hidden="true">✎</span></AppIconButton><AppIconButton v-if="message.ownMessage" label="Delete message" tone="danger" @click="remove(message)"><span aria-hidden="true">⌫</span></AppIconButton></div>
        </article>
      </main>
      <form v-if="selectedId" class="chat-surface__composer" aria-label="Conversation composer" @submit.prevent="send"><p v-if="replyingTo" class="chat-surface__replying">Replying to {{ replyingTo.senderUsername }} <button type="button" @click="replyingTo = null">Cancel</button></p><input v-model="draft" placeholder="Write a message." aria-label="Message" maxlength="2000"><label class="chat-surface__attachment">{{ isUploadingAttachment ? "Uploading…" : attachment ? attachment.attachmentName : "Attach" }}<input type="file" accept="image/*,.pdf,.txt" @change="uploadAttachment" :disabled="isUploadingAttachment"></label><img v-if="attachmentPreviewUrl" class="chat-surface__attachment-preview" :src="attachmentPreviewUrl" alt="Selected attachment preview"><button type="button" v-if="attachment" @click="removeAttachment">Remove</button><button type="submit" :disabled="isUploadingAttachment">Send</button></form>
    </div>
  </section>
</template>

<style scoped>
.chat-surface{display:grid;gap:1rem}.chat-surface__header{display:flex;justify-content:space-between}.chat-surface__header-actions{display:flex;gap:.4rem}.chat-surface__eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.chat-surface__direct-create,.chat-surface__group-create{display:flex;gap:.45rem;flex-wrap:wrap;align-items:center;padding:.7rem;border:1px solid var(--border-subtle);border-radius:.8rem;background:var(--surface)}.chat-surface__direct-create input,.chat-surface__group-create input{min-width:14rem;flex:1;border:1px solid var(--border-subtle);border-radius:999px;padding:.6rem .75rem;font:inherit}.chat-surface__direct-create button,.chat-surface__group-create button{border:1px solid var(--border-subtle);border-radius:999px;padding:.45rem .7rem;background:var(--accent);color:var(--text);font:inherit;font-size:.78rem;cursor:pointer}.chat-surface__layout{display:grid;grid-template-columns:minmax(14rem,20rem) minmax(0,1fr);gap:1rem;min-height:28rem}.chat-surface__index,.chat-surface__thread{border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface);padding:.8rem}.chat-surface__index{display:grid;align-content:start;gap:.25rem}.chat-surface__conversation{display:grid;gap:.2rem;padding:.7rem;border-radius:.7rem}.chat-surface__conversation span,.chat-surface__message-author{color:var(--text-muted);font-size:.78rem}.chat-surface__conversation--active{background:var(--surface-muted)}.chat-surface__thread{display:grid;align-content:start;gap:.55rem}.chat-surface__message{max-width:72%;padding:.65rem .8rem;border-radius:.8rem;background:rgba(23,34,26,.06)}.chat-surface__message--own{justify-self:end;background:var(--accent);color:var(--text)}.chat-surface__message p{margin:.25rem 0 0}.chat-surface__message--own .chat-surface__message-author{color:rgba(248,248,244,.65)}.chat-surface__more,.chat-surface__older{justify-self:start;border:1px solid var(--border-subtle);border-radius:999px;background:transparent;padding:.5rem .75rem;cursor:pointer}.chat-surface__status{padding:1rem 0;color:var(--text-muted)}.chat-surface__status--error{color:var(--danger)}.chat-surface__status button{margin-left:.6rem;border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:700px){.chat-surface__layout{grid-template-columns:1fr}.chat-surface__index{max-height:14rem;overflow:auto}.chat-surface__message{max-width:88%}}
.chat-surface__message-actions{display:flex;gap:.3rem;margin-top:.45rem}.chat-surface__message-actions button,.chat-surface__composer button{border:1px solid var(--border-subtle);border-radius:999px;padding:.35rem .6rem;background:transparent;font:inherit;font-size:.75rem;cursor:pointer}.chat-surface__composer{display:flex;gap:.45rem;grid-column:2;align-items:center;flex-wrap:wrap}.chat-surface__composer input{flex:1;min-width:12rem;border:1px solid var(--border-subtle);border-radius:999px;padding:.65rem .8rem;font:inherit}.chat-surface__composer button{background:var(--accent);color:var(--text)}.chat-surface__attachment{display:inline-flex;align-items:center;border:1px solid var(--border-subtle);border-radius:999px;padding:.35rem .6rem;font-size:.75rem;cursor:pointer}.chat-surface__attachment input{display:none}.chat-surface__attachment-preview{width:3.5rem;height:3.5rem;object-fit:cover;border-radius:.55rem;border:1px solid var(--border-subtle)}
</style>
<style scoped>
.chat-surface{max-width:78rem}.chat-surface__layout{min-height:clamp(32rem,68vh,48rem);grid-template-rows:minmax(0,1fr) auto}.chat-surface__index,.chat-surface__thread{box-shadow:0 14px 34px rgba(23,34,26,.05)}.chat-surface__index{background:rgba(248,250,245,.78)}.chat-surface__conversation{border:1px solid transparent;transition:background-color 140ms ease,border-color 140ms ease}.chat-surface__conversation:hover{background:var(--surface);border-color:var(--border-subtle)}.chat-surface__conversation strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.chat-surface__thread{overflow:auto;align-content:end;align-items:start;background:linear-gradient(180deg,rgba(255,255,255,.72),rgba(236,241,233,.58))}.chat-surface__message{position:relative;border-radius:1rem 1rem 1rem .25rem;box-shadow:0 4px 14px rgba(23,34,26,.04)}.chat-surface__message--own{border-radius:1rem 1rem .25rem 1rem}.chat-surface__message-actions{opacity:.7;transition:opacity 140ms ease}.chat-surface__message:hover .chat-surface__message-actions,.chat-surface__message:focus-within .chat-surface__message-actions{opacity:1}.chat-surface__composer{position:sticky;bottom:0;z-index:2;padding:.65rem;border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface);backdrop-filter:blur(12px);box-shadow:0 12px 30px rgba(23,34,26,.08)}.chat-surface__composer input{background:transparent}.chat-surface__composer button[type=submit]{padding-inline:1rem}.chat-surface__attachment{background:var(--surface-muted)}.chat-surface__reply{margin:.25rem 0 0;color:var(--text-muted);font-size:.75rem}.chat-surface__attachment-link{display:inline-block;margin-top:.25rem;font-size:.78rem}.chat-surface__replying{flex-basis:100%;margin:0;color:var(--text-muted);font-size:.78rem}.chat-surface__replying button{margin-left:.4rem}
.chat-surface__header{align-items:center}.chat-surface__create-toggle,.chat-surface__leave{border:1px solid var(--border-subtle);border-radius:999px;padding:.5rem .75rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}.chat-surface__group-create{display:grid;grid-template-columns:1fr 1fr auto;gap:.45rem;align-items:center;padding:.8rem;border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface)}.chat-surface__group-create input{border:1px solid var(--border-subtle);border-radius:.65rem;padding:.6rem;font:inherit}.chat-surface__group-create button{border:0;border-radius:999px;padding:.6rem .8rem;background:var(--accent);color:var(--text);font:inherit;cursor:pointer}.chat-surface__group-create button:disabled{opacity:.5;cursor:wait}.chat-surface__candidate{display:flex;gap:.35rem;align-items:center;font-size:.85rem}.chat-surface__candidate input{width:auto}.chat-surface__leave{justify-self:end;background:transparent;color:var(--danger);font-size:.8rem}@media(max-width:700px){.chat-surface__group-create{grid-template-columns:1fr}.chat-surface__candidate{grid-column:1}}
.chat-surface__back{display:inline-flex;align-items:center;border:1px solid var(--border-subtle);border-radius:999px;padding:.5rem .75rem;font-size:.8rem}
@media(max-width:700px){.chat-surface__header{align-items:flex-start;gap:.75rem;flex-direction:column}.chat-surface__header-actions{width:100%;flex-wrap:wrap}.chat-surface__composer{grid-column:1}.chat-surface__thread{min-height:24rem}.chat-surface__back{order:-1}}
</style>
