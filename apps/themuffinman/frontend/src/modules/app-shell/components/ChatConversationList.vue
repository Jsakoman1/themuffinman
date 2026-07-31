<script setup lang="ts">
import {RouterLink} from "vue-router"
import type {ChatConversationSummaryDTO} from "../../../contracts/index.ts"

type Section = {id: string; label: string; items: ChatConversationSummaryDTO[]}
const props = defineProps<{sections: Section[]; selectedId: number | null; empty: boolean; hasMore: boolean; loadingMore: boolean}>()
const emit = defineEmits<{loadMore: []}>()
const isUnread = (conversation: ChatConversationSummaryDTO) => conversation.lastMessageId !== null && conversation.lastSeenMessageId !== conversation.lastMessageId
const preview = (conversation: ChatConversationSummaryDTO) => {
  if (!conversation.lastMessageAt) return "No messages yet"
  const elapsedMinutes = Math.max(0, Math.round((Date.now() - new Date(conversation.lastMessageAt).getTime()) / 60_000))
  const relative = elapsedMinutes < 1 ? "now" : elapsedMinutes < 60 ? `${elapsedMinutes}m` : elapsedMinutes < 1_440 ? `${Math.round(elapsedMinutes / 60)}h` : `${Math.round(elapsedMinutes / 1_440)}d`
  return `Last message · ${relative}`
}
</script>

<template>
  <aside class="chat-conversation-list" aria-label="Conversations">
    <section v-for="section in props.sections" :key="section.id"><h2>{{ section.label }}</h2><RouterLink v-for="conversation in section.items" :key="conversation.conversationId" :to="`/chat/${conversation.conversationId}`" class="chat-conversation-list__row" :class="{'chat-conversation-list__row--active': props.selectedId === conversation.conversationId, 'chat-conversation-list__row--unread': isUnread(conversation)}"><span class="chat-conversation-list__avatar" aria-hidden="true">{{ (conversation.title || conversation.otherUsername || "?").slice(0, 1).toUpperCase() }}</span><span class="chat-conversation-list__copy"><strong>{{ conversation.title || conversation.otherUsername || "Conversation" }}</strong><small>{{ preview(conversation) }}<template v-if="conversation.muted"> · Muted</template></small></span><span v-if="isUnread(conversation)" class="chat-conversation-list__unread" aria-label="Unread" /></RouterLink></section>
    <p v-if="props.empty" class="chat-conversation-list__empty">No conversations yet. Start a message when you need one.</p>
    <button v-if="props.hasMore" type="button" :disabled="props.loadingMore" @click="emit('loadMore')">{{ props.loadingMore ? "Loading…" : "Load more" }}</button>
  </aside>
</template>

<style scoped>
.chat-conversation-list{display:grid;align-content:start;gap:var(--space-3);min-width:0;padding:var(--space-2);border-right:1px solid var(--border-subtle);background:var(--rail-canvas);overflow:auto}.chat-conversation-list section{display:grid;gap:var(--space-1)}.chat-conversation-list h2{margin:var(--space-2) var(--space-2) 0;color:var(--text-soft);font-size:var(--text-size-label);letter-spacing:var(--tracking-label);text-transform:uppercase}.chat-conversation-list__row{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:var(--space-2);padding:var(--space-2);border:1px solid transparent;border-radius:var(--radius-control);color:var(--text);text-decoration:none}.chat-conversation-list__row:hover{background:var(--surface-hover)}.chat-conversation-list__row--active{border-color:var(--border-subtle);background:var(--surface-base);box-shadow:var(--shadow-control)}.chat-conversation-list__avatar{display:grid;place-items:center;width:2.3rem;height:2.3rem;border-radius:50%;background:var(--launcher-people-bg);color:var(--launcher-people-ink);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.chat-conversation-list__copy{display:grid;gap:.15rem;min-width:0}.chat-conversation-list__copy strong,.chat-conversation-list__copy small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.chat-conversation-list__copy small,.chat-conversation-list__empty{color:var(--text-muted);font-size:var(--text-size-meta)}.chat-conversation-list__row--unread strong{font-weight:var(--text-weight-semibold)}.chat-conversation-list__unread{width:.5rem;height:.5rem;border-radius:50%;background:var(--accent)}.chat-conversation-list button{justify-self:start;border:0;background:transparent;color:var(--accent);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}@media(max-width:700px){.chat-conversation-list{border-right:0;padding:0;background:transparent}.chat-conversation-list__row{padding:var(--space-3)}}
</style>
