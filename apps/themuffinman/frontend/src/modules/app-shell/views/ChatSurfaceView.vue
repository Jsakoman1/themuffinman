<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {ChatConversationSummaryDTO, ChatMessageDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const route = useRoute()
const conversations = ref<ChatConversationSummaryDTO[]>([])
const messages = ref<ChatMessageDTO[]>([])
const hasMoreConversations = ref(false)
const hasMoreMessages = ref(false)
const conversationPage = ref(0)
const nextMessageId = ref<number | null>(null)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
const draft = ref("")
const editingId = ref<number | null>(null)
const editingDraft = ref("")
const selectedId = computed(() => Number(route.params.conversationId) || null)
const formatDate = (value: string | null | undefined) => value ? new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value)) : ""

const loadConversations = async (append = false) => {
  if (!append) { isLoading.value = true; conversationPage.value = 0; conversations.value = [] } else { isLoadingMore.value = true }
  try {
    const page = await userShellApi.getChatConversations(conversationPage.value, 20)
    conversations.value = append ? [...conversations.value, ...page.conversations] : page.conversations
    hasMoreConversations.value = page.hasMore
  } catch { error.value = "Could not load conversations." }
  finally { isLoading.value = false; isLoadingMore.value = false }
}
const loadMessages = async () => {
  if (!selectedId.value) { messages.value = []; return }
  try {
    const page = await userShellApi.getChatMessages(selectedId.value, 30)
    messages.value = page.messages
    hasMoreMessages.value = page.hasMore
    nextMessageId.value = page.nextBeforeMessageId
  } catch { error.value = "Could not load messages." }
}
const loadMoreConversations = async () => { if (!hasMoreConversations.value || isLoadingMore.value) return; conversationPage.value += 1; await loadConversations(true) }
const loadOlderMessages = async () => {
  if (!selectedId.value || !hasMoreMessages.value || !nextMessageId.value) return
  const page = await userShellApi.getChatMessages(selectedId.value, 30, nextMessageId.value)
  messages.value = [...page.messages, ...messages.value]
  hasMoreMessages.value = page.hasMore
  nextMessageId.value = page.nextBeforeMessageId
}
const replaceMessage = (message: ChatMessageDTO) => { messages.value = messages.value.map(item => item.id === message.id ? message : item) }
const send = async () => { if (!selectedId.value || !draft.value.trim()) return; error.value = ""; try { const message = await userShellApi.sendChatMessage(selectedId.value, draft.value.trim()); messages.value = [...messages.value, message]; draft.value = "" } catch { error.value = "Could not send this message." } }
const beginEdit = (message: ChatMessageDTO) => { editingId.value = message.id; editingDraft.value = message.messageBody ?? "" }
const saveEdit = async (message: ChatMessageDTO) => { if (!selectedId.value || !editingDraft.value.trim()) return; try { replaceMessage(await userShellApi.updateChatMessage(selectedId.value, message.id, editingDraft.value.trim())); editingId.value = null } catch { error.value = "Could not edit this message." } }
const remove = async (message: ChatMessageDTO) => { if (!selectedId.value || !window.confirm("Delete this message?")) return; try { await userShellApi.deleteChatMessage(selectedId.value, message.id); messages.value = messages.value.map(item => item.id === message.id ? {...item, deleted: true, messageBody: null} : item) } catch { error.value = "Could not delete this message." } }
const toggleReaction = async (message: ChatMessageDTO) => { if (!selectedId.value) return; const own = message.reactions.find(reaction => reaction.ownReaction && reaction.emoji === "👍"); try { replaceMessage(own ? await userShellApi.removeChatReaction(selectedId.value, message.id, "👍") : await userShellApi.addChatReaction(selectedId.value, message.id, "👍")) } catch { error.value = "Could not update this reaction." } }
watch(selectedId, () => void loadMessages())
onMounted(() => { void loadConversations(); void loadMessages() })
</script>

<template>
  <section class="chat-surface">
    <header class="chat-surface__header"><div><p class="chat-surface__eyebrow">Chat</p><h1>{{ selectedId ? "Conversation" : "Inbox" }}</h1></div></header>
    <div v-if="error" class="chat-surface__status chat-surface__status--error" role="alert">{{ error }} <button type="button" @click="loadConversations()">Retry</button></div>
    <div class="chat-surface__layout">
      <aside class="chat-surface__index" aria-label="Conversations">
        <RouterLink v-for="conversation in conversations" :key="conversation.conversationId" :to="`/chat/${conversation.conversationId}`" class="chat-surface__conversation" :class="{'chat-surface__conversation--active': selectedId === conversation.conversationId}">
          <strong>{{ conversation.title || conversation.otherUsername || `Conversation #${conversation.conversationId}` }}</strong><span>{{ conversation.lastMessageAt ? formatDate(conversation.lastMessageAt) : "No messages" }}</span>
        </RouterLink>
        <button v-if="hasMoreConversations" type="button" class="chat-surface__more" :disabled="isLoadingMore" @click="loadMoreConversations">{{ isLoadingMore ? "Loading" : "Load more" }}</button>
      </aside>
      <main class="chat-surface__thread" aria-live="polite">
        <button v-if="hasMoreMessages" type="button" class="chat-surface__older" @click="loadOlderMessages">Load older messages</button>
        <p v-if="!selectedId" class="chat-surface__status">Select a conversation.</p>
        <div v-else-if="messages.length === 0" class="chat-surface__status">No messages yet.</div>
        <article v-for="message in messages" v-else :key="message.id" class="chat-surface__message" :class="{'chat-surface__message--own': message.ownMessage}">
          <span class="chat-surface__message-author">{{ message.senderUsername }} · {{ formatDate(message.createdAt) }}</span><form v-if="editingId === message.id" @submit.prevent="saveEdit(message)"><input v-model="editingDraft"><button type="submit">Save</button><button type="button" @click="editingId = null">Cancel</button></form><p v-else>{{ message.deleted ? "Message deleted" : message.messageBody || message.attachmentName || "Attachment" }}</p><div v-if="selectedId && !message.deleted" class="chat-surface__message-actions"><button type="button" @click="toggleReaction(message)">{{ message.reactions.some(reaction => reaction.ownReaction && reaction.emoji === "👍") ? "👍" : "Like" }}</button><button v-if="message.ownMessage" type="button" @click="beginEdit(message)">Edit</button><button v-if="message.ownMessage" type="button" @click="remove(message)">Delete</button></div>
        </article>
      </main>
      <form v-if="selectedId" class="chat-surface__composer" @submit.prevent="send"><input v-model="draft" placeholder="Write a message." maxlength="4000"><button type="submit">Send</button></form>
    </div>
  </section>
</template>

<style scoped>
.chat-surface{display:grid;gap:1rem}.chat-surface__header{display:flex;justify-content:space-between}.chat-surface__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.chat-surface__layout{display:grid;grid-template-columns:minmax(14rem,20rem) minmax(0,1fr);gap:1rem;min-height:28rem}.chat-surface__index,.chat-surface__thread{border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62);padding:.8rem}.chat-surface__index{display:grid;align-content:start;gap:.25rem}.chat-surface__conversation{display:grid;gap:.2rem;padding:.7rem;border-radius:.7rem}.chat-surface__conversation span,.chat-surface__message-author{color:rgba(23,34,26,.55);font-size:.78rem}.chat-surface__conversation--active{background:rgba(214,228,218,.7)}.chat-surface__thread{display:grid;align-content:start;gap:.55rem}.chat-surface__message{max-width:72%;padding:.65rem .8rem;border-radius:.8rem;background:rgba(23,34,26,.06)}.chat-surface__message--own{justify-self:end;background:#17221a;color:#f8f8f4}.chat-surface__message p{margin:.25rem 0 0}.chat-surface__message--own .chat-surface__message-author{color:rgba(248,248,244,.65)}.chat-surface__more,.chat-surface__older{justify-self:start;border:1px solid rgba(23,34,26,.12);border-radius:999px;background:transparent;padding:.5rem .75rem;cursor:pointer}.chat-surface__status{padding:1rem 0;color:rgba(23,34,26,.62)}.chat-surface__status--error{color:#8d2f25}.chat-surface__status button{margin-left:.6rem;border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:700px){.chat-surface__layout{grid-template-columns:1fr}.chat-surface__index{max-height:14rem;overflow:auto}.chat-surface__message{max-width:88%}}
.chat-surface__message-actions{display:flex;gap:.3rem;margin-top:.45rem}.chat-surface__message-actions button,.chat-surface__composer button{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.35rem .6rem;background:transparent;font:inherit;font-size:.75rem;cursor:pointer}.chat-surface__composer{display:flex;gap:.45rem;grid-column:2}.chat-surface__composer input{flex:1;border:1px solid rgba(23,34,26,.14);border-radius:999px;padding:.65rem .8rem;font:inherit}.chat-surface__composer button{background:#17221a;color:#f8f8f4}
</style>
