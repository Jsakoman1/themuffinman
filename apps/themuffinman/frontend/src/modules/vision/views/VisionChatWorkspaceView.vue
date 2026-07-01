<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRouter} from "vue-router"
import ProfileAvatar from "../../../components/profile/ProfileAvatar.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {chatApi} from "../../chat/api/chatApi.ts"
import type {ChatWorkspace} from "../../../contracts/index.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"

const workspace = ref<ChatWorkspace | null>(null)
const loading = ref(true)
const errorMessage = ref("")
const contactQuery = ref("")
const router = useRouter()

const filteredContacts = computed(() => {
  const query = contactQuery.value.trim().toLowerCase()
  const contacts = workspace.value?.contacts ?? []
  if (!query) {
    return contacts
  }
  return contacts.filter((contact) => contact.username.toLowerCase().includes(query)
    || contact.circleNames.join(" ").toLowerCase().includes(query))
})

const loadWorkspace = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    workspace.value = await chatApi.getWorkspace()
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Chat workspace could not be loaded.")
  } finally {
    loading.value = false
  }
}

onMounted(loadWorkspace)

const closeChatWorkspace = async () => {
  await router.push("/vision")
}
</script>

<template>
  <VisionDetailSurface
    title="Chat"
    @close="closeChatWorkspace"
  >
    <UiRequestError
      v-if="errorMessage"
      :message="errorMessage"
      :details="[errorMessage]"
      summary="Chat workspace error details"
      :copied="false"
    />

    <UiWorkspace variant="detail">
      <UiSurfaceSection title="Summary">
        <div class="chat-module-stats">
          <div class="stat-card">
            <strong>{{ workspace?.unreadConversationCount ?? 0 }}</strong>
            <span>Unread</span>
          </div>
          <div class="stat-card">
            <strong>{{ workspace?.onlineContactCount ?? 0 }}</strong>
            <span>Online</span>
          </div>
          <div class="stat-card">
            <strong>{{ workspace?.contacts.length ?? 0 }}</strong>
            <span>Contacts</span>
          </div>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="Recent">
        <div v-if="loading" class="empty-state empty-state--compact">Loading conversations.</div>
        <div v-else-if="!workspace?.conversations.length" class="empty-state empty-state--compact">No recent conversations.</div>
        <div v-else class="chat-module-list">
          <article v-for="conversation in workspace.conversations" :key="conversation.conversationId" class="chat-module-row">
            <ProfileAvatar :username="conversation.otherUsername" :avatar-data-url="conversation.otherUserAvatarDataUrl" :size="36" />
            <div>
              <strong>{{ conversation.otherUsername }}</strong>
              <p class="muted">{{ conversation.lastMessagePreview || "Start the conversation" }}</p>
            </div>
            <span v-if="conversation.unreadCount > 0" class="badge badge--accent">{{ conversation.unreadCount }}</span>
          </article>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="People">
        <input v-model="contactQuery" class="input" placeholder="Search contacts" />
        <div v-if="loading" class="empty-state empty-state--compact">Loading contacts.</div>
        <div v-else-if="filteredContacts.length === 0" class="empty-state empty-state--compact">No contacts match this search.</div>
        <div v-else class="chat-module-list">
          <article v-for="contact in filteredContacts" :key="contact.userId" class="chat-module-row">
            <ProfileAvatar :username="contact.username" :avatar-data-url="contact.profileAvatarDataUrl" :size="36" />
            <div>
              <strong>{{ contact.username }}</strong>
              <p class="muted">{{ contact.circleNames.join(", ") || "No circle label" }}</p>
            </div>
            <span :class="['chat-module-status', { 'chat-module-status--online': contact.online }]" />
          </article>
        </div>
      </UiSurfaceSection>
    </UiWorkspace>
  </VisionDetailSurface>
</template>
