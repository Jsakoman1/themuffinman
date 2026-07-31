<script setup lang="ts">
import type {ChatAttachmentUploadDTO, ChatMessageDTO} from "../../../contracts/index.ts"
import AppButton from "./AppButton.vue"
const props = defineProps<{draft: string; attachment: ChatAttachmentUploadDTO | null; attachmentPreviewUrl: string | null; replyingTo: ChatMessageDTO | null; sending: boolean; uploading: boolean}>()
const emit = defineEmits<{"update:draft": [string]; send: []; attach: [Event]; removeAttachment: []; cancelReply: []; typing: [boolean]}>()
const updateDraft = (event: Event) => {
  emit("update:draft", (event.target as HTMLTextAreaElement).value)
  emit("typing", true)
}
</script>

<template>
  <form class="chat-composer" aria-label="Conversation composer" data-composer="multiline-sticky" @submit.prevent="emit('send')"><p v-if="props.replyingTo">Replying to {{ props.replyingTo.senderUsername }} <button type="button" @click="emit('cancelReply')">Cancel</button></p><textarea :value="props.draft" placeholder="Write a message…" aria-label="Message" rows="2" maxlength="2000" :disabled="props.sending" @input="updateDraft" @blur="emit('typing', false)" /><label>{{ props.uploading ? "Uploading…" : props.attachment ? props.attachment.attachmentName : "Attach" }}<input type="file" accept="image/*,.pdf,.txt" :disabled="props.uploading || props.sending" @change="emit('attach', $event)"></label><img v-if="props.attachmentPreviewUrl" :src="props.attachmentPreviewUrl" alt="Selected attachment preview"><AppButton v-if="props.attachment" type="button" tone="quiet" @click="emit('removeAttachment')">Remove</AppButton><AppButton type="submit" tone="primary" :loading="props.sending" :disabled="props.uploading">Send</AppButton></form>
</template>

<style scoped>
.chat-composer{display:flex;align-items:end;gap:var(--space-2);flex-wrap:wrap;padding:var(--space-2);border-top:1px solid var(--border-subtle);background:var(--surface-raised)}.chat-composer p{flex-basis:100%;margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.chat-composer p button{border:0;background:transparent;color:var(--accent);font:inherit;cursor:pointer}.chat-composer textarea{flex:1;min-width:12rem;max-height:8rem;resize:vertical;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.chat-composer label{display:inline-flex;align-items:center;min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);color:var(--text-muted);font-size:var(--text-size-meta);cursor:pointer}.chat-composer label input{display:none}.chat-composer img{width:2.5rem;height:2.5rem;object-fit:cover;border-radius:var(--radius-control)}@media(max-width:700px){.chat-composer{position:sticky;bottom:0;padding-bottom:max(var(--space-2),env(safe-area-inset-bottom))}.chat-composer textarea{min-width:0;width:100%}}
</style>
