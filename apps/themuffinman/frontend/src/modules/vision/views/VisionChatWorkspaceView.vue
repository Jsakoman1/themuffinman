<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRouter} from "vue-router"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {chatApi} from "../../chat/api/chatApi.ts"
import type {ChatWorkspace} from "../../../contracts/index.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"
import {getProfileInitials} from "../../../shared/profileFormatting.ts"

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

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})
</script>

<template>
  <VisionDetailSurface
    title="Chat"
    @close="closeChatWorkspace"
  >
    <div class="vision-terminal-feed">
      <p class="vision-terminal-feed__line">> chat</p>
      <p v-if="loading" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Loading conversations and contacts.</p>
      <p v-else-if="errorMessage" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ errorMessage }}</p>

      <template v-else>
        <p class="vision-terminal-feed__line">
          Unread: {{ workspace?.unreadConversationCount ?? 0 }} · Online: {{ workspace?.onlineContactCount ?? 0 }} · Contacts: {{ workspace?.contacts.length ?? 0 }}
        </p>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">recent</p>
          <p v-if="!workspace?.conversations.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No recent conversations.</p>
          <article v-for="conversation in workspace?.conversations ?? []" :key="conversation.conversationId" class="vision-terminal-feed__entry">
            <span class="profile-avatar" :style="profileAvatarStyle(28)">
              <img
                v-if="conversation.otherUserAvatarDataUrl"
                class="profile-avatar__image"
                :src="conversation.otherUserAvatarDataUrl"
                :alt="`${conversation.otherUsername || 'User'} avatar`"
              />
              <span v-else class="profile-avatar__fallback">{{ getProfileInitials(conversation.otherUsername) }}</span>
            </span>
            <div class="vision-terminal-feed__entry-body">
              <p class="vision-terminal-feed__line">
                {{ conversation.otherUsername }} <span v-if="conversation.unreadCount > 0" class="vision-terminal-feed__pulse">({{ conversation.unreadCount }} unread)</span>
              </p>
              <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">
                {{ conversation.lastMessagePreview || "Start the conversation" }}
              </p>
            </div>
          </article>
        </section>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">people</p>
          <input v-model="contactQuery" class="input vision-terminal-feed__input" placeholder="Search contacts" />
          <p v-if="!filteredContacts.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No contacts match this search.</p>
          <article v-for="contact in filteredContacts" :key="contact.userId" class="vision-terminal-feed__entry">
            <span class="profile-avatar" :style="profileAvatarStyle(28)">
              <img
                v-if="contact.profileAvatarDataUrl"
                class="profile-avatar__image"
                :src="contact.profileAvatarDataUrl"
                :alt="`${contact.username || 'User'} avatar`"
              />
              <span v-else class="profile-avatar__fallback">{{ getProfileInitials(contact.username) }}</span>
            </span>
            <div class="vision-terminal-feed__entry-body">
              <p class="vision-terminal-feed__line">{{ contact.username }}</p>
              <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">
                {{ contact.circleNames.join(", ") || "No circle label" }}
              </p>
            </div>
            <span :class="['vision-terminal-feed__status', { 'vision-terminal-feed__status--online': contact.online }]" />
          </article>
        </section>
      </template>
    </div>
  </VisionDetailSurface>
</template>
