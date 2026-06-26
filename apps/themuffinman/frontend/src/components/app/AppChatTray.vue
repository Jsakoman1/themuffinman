<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, ref, watch} from "vue"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import {useAppChat} from "../../modules/chat/composables/useAppChat.ts"
import {formatInstantForDisplay} from "../../shared/questSchedule.ts"

const {
  isAuthenticated,
  launcherOpen,
  isLoadingWorkspace,
  workspaceError,
  contactQuery,
  activeCircleFilter,
  filteredContacts,
  conversations,
  circles,
  openWindows,
  activeConversationId,
  unreadConversationCount,
  toggleLauncher,
  openConversationSummary,
  openConversationForContact,
  closeConversation,
  toggleWindowMinimized,
  reopenConversation,
  uploadImage,
  removeDraftImage,
  sendMessage
} = useAppChat()

const imagePreviewUrl = ref<string | null>(null)
const messageContainers = new Map<number, HTMLElement>()

const footerWindows = computed(() => openWindows.value)

const chatWindowStyle = (windowState: typeof openWindows.value[number]) => {
  const footerIndex = footerWindows.value.findIndex((candidate) => candidate.conversationId === windowState.conversationId)
  const indexFromRight = footerIndex >= 0 ? footerWindows.value.length - 1 - footerIndex : 0

  return {
    "--chat-window-offset": `calc(var(--chat-launcher-reserved-width) + var(--chat-bar-gap) + ${indexFromRight} * (var(--chat-chip-width) + var(--chat-bar-gap)))`
  }
}

const setMessageContainer = (conversationId: number, element: Element | { $el?: Element } | null) => {
  const resolvedElement = element instanceof HTMLElement
    ? element
    : element && "$el" in element && element.$el instanceof HTMLElement
      ? element.$el
      : null

  if (!resolvedElement) {
    messageContainers.delete(conversationId)
    return
  }
  messageContainers.set(conversationId, resolvedElement)
}

const scrollWindowToBottom = (conversationId: number) => {
  const element = messageContainers.get(conversationId)
  if (!element) {
    return
  }
  element.scrollTop = element.scrollHeight
}

const handleComposerKeydown = (event: KeyboardEvent, windowState: typeof openWindows.value[number]) => {
  if (event.key !== "Enter" || event.shiftKey) {
    return
  }

  event.preventDefault()
  void sendMessage(windowState)
}

const sameCalendarDay = (left?: string | null, right?: string | null) => {
  if (!left || !right) {
    return false
  }

  const leftDate = new Date(left)
  const rightDate = new Date(right)
  return leftDate.getFullYear() === rightDate.getFullYear()
    && leftDate.getMonth() === rightDate.getMonth()
    && leftDate.getDate() === rightDate.getDate()
}

const showDateDivider = (messages: typeof openWindows.value[number]["messages"], index: number) => {
  if (index === 0) {
    return true
  }
  return !sameCalendarDay(messages[index - 1]?.createdAt, messages[index]?.createdAt)
}

const showUnreadDivider = (messages: typeof openWindows.value[number]["messages"], index: number) => {
  const message = messages[index]
  if (!message || message.ownMessage || message.readAt) {
    return false
  }

  return !messages.slice(0, index).some((candidate) => !candidate.ownMessage && !candidate.readAt)
}

const formatMessageTime = (value?: string | null) => {
  if (!value) {
    return ""
  }

  return new Intl.DateTimeFormat("en-GB", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value))
}

watch(() => openWindows.value.map((windowState) => `${windowState.conversationId}:${windowState.messages.length}`).join("|"), async () => {
  await nextTick()
  openWindows.value.forEach((windowState) => {
    scrollWindowToBottom(windowState.conversationId)
  })
})

onBeforeUnmount(() => {
  messageContainers.clear()
})
</script>

<template>
  <div v-if="isAuthenticated" class="app-chat-tray">
    <Transition name="chat-sheet">
      <section v-if="launcherOpen" class="chat-launcher">
        <div class="chat-launcher__filters">
          <input v-model="contactQuery" class="input" placeholder="Search people" />
          <select v-model="activeCircleFilter" class="input">
            <option value="all">All circles</option>
            <option v-for="circle in circles" :key="circle.id" :value="circle.id">
              {{ circle.name }}
            </option>
          </select>
        </div>

        <div v-if="workspaceError" class="chat-launcher__error">{{ workspaceError }}</div>
        <div v-else-if="isLoadingWorkspace" class="chat-launcher__empty">Loading chat...</div>
        <div v-else class="chat-launcher__body">
          <div v-if="conversations.length" class="chat-launcher__section">
            <div class="chat-launcher__section-bar">
              <div class="chat-launcher__section-title">Recent</div>
            </div>
            <div class="chat-launcher__compact-list">
              <button
                v-for="conversation in conversations"
                :key="conversation.conversationId"
                class="chat-launcher__item"
                type="button"
                @click="openConversationSummary(conversation)"
              >
                <ProfileAvatar
                  :username="conversation.otherUsername"
                  :avatar-data-url="conversation.otherUserAvatarDataUrl"
                  :size="34"
                />
                <div class="chat-launcher__item-main">
                  <div class="chat-launcher__item-top">
                    <strong>{{ conversation.otherUsername }}</strong>
                    <span class="chat-launcher__status-dot" :class="{ 'chat-launcher__status-dot--online': conversation.otherUserOnline }" />
                  </div>
                  <div class="chat-launcher__item-bottom">
                    <span>{{ conversation.lastMessagePreview || "Start the conversation" }}</span>
                    <span v-if="conversation.unreadCount > 0" class="badge badge--accent">{{ conversation.unreadCount }}</span>
                  </div>
                </div>
              </button>
            </div>
          </div>

          <div class="chat-launcher__section">
            <div class="chat-launcher__section-bar">
              <div class="chat-launcher__section-title">People</div>
            </div>
            <div v-if="filteredContacts.length" class="chat-launcher__list">
              <button
                v-for="contact in filteredContacts"
                :key="contact.userId"
                class="chat-launcher__item"
                type="button"
                @click="openConversationForContact(contact)"
              >
                <ProfileAvatar
                  :username="contact.username"
                  :avatar-data-url="contact.profileAvatarDataUrl"
                  :size="34"
                />
                <div class="chat-launcher__item-main">
                  <div class="chat-launcher__item-top">
                    <strong>{{ contact.username }}</strong>
                    <span class="chat-launcher__status-dot" :class="{ 'chat-launcher__status-dot--online': contact.online }" />
                  </div>
                </div>
              </button>
            </div>
            <div v-else class="chat-launcher__empty">
              No contacts match this filter.
            </div>
          </div>
        </div>
      </section>
    </Transition>

    <div class="chat-windows">
      <section
        v-for="windowState in openWindows.filter((candidate) => !candidate.minimized)"
        :key="windowState.conversationId"
        class="chat-window"
        :style="chatWindowStyle(windowState)"
      >
        <header class="chat-window__header">
          <button class="chat-window__identity chat-window__identity-button" type="button" @click="toggleWindowMinimized(windowState.conversationId)">
            <ProfileAvatar
              :username="windowState.otherUsername"
              :avatar-data-url="windowState.otherUserAvatarDataUrl"
              :size="28"
            />
            <div class="chat-window__identity-copy">
              <strong>{{ windowState.otherUsername }}</strong>
              <span class="chat-window__identity-status">
                <span class="chat-launcher__status-dot" :class="{ 'chat-launcher__status-dot--online': windowState.otherUserOnline }" />
              </span>
            </div>
          </button>
          <div class="chat-window__header-actions">
            <button class="chat-window__icon-button" type="button" @click="toggleWindowMinimized(windowState.conversationId)">—</button>
            <button class="chat-window__icon-button" type="button" @click="closeConversation(windowState.conversationId)">✕</button>
          </div>
        </header>

        <div v-if="!windowState.minimized" :ref="(element) => setMessageContainer(windowState.conversationId, element)" class="chat-window__messages">
          <div v-if="windowState.isLoading" class="chat-window__empty">Loading messages...</div>
          <div v-else-if="windowState.messages.length === 0" class="chat-window__empty">No messages yet.</div>
          <template v-for="(message, index) in windowState.messages" :key="message.id">
            <div v-if="showDateDivider(windowState.messages, index)" class="chat-window__divider">
              <span>{{ formatInstantForDisplay(message.createdAt) }}</span>
            </div>
            <div v-if="showUnreadDivider(windowState.messages, index)" class="chat-window__divider chat-window__divider--unread">
              <span>Unread messages</span>
            </div>
            <div
              class="chat-bubble"
              :class="{ 'chat-bubble--own': message.ownMessage }"
            >
            <div v-if="message.imageDataUrl" class="chat-bubble__image-shell">
              <img class="chat-bubble__image" :src="message.imageDataUrl" alt="Chat attachment" @click="imagePreviewUrl = message.imageDataUrl" />
            </div>
            <div v-if="message.messageBody" class="chat-bubble__text">{{ message.messageBody }}</div>
              <div class="chat-bubble__meta">
                <span>{{ formatMessageTime(message.createdAt) }}</span>
              </div>
            </div>
          </template>
        </div>

        <div v-if="!windowState.minimized && windowState.draftImageDataUrl" class="chat-window__preview">
          <img class="chat-window__preview-image" :src="windowState.draftImageDataUrl" alt="Draft attachment" />
          <button class="button button--ghost" type="button" @click="removeDraftImage(windowState)">Remove</button>
        </div>

        <div v-if="!windowState.minimized && windowState.error" class="chat-window__error">{{ windowState.error }}</div>

        <div v-if="!windowState.minimized" class="chat-window__composer">
          <textarea
            v-model="windowState.draftMessage"
            class="input chat-window__textarea"
            rows="2"
            placeholder="Write a message"
            @keydown="handleComposerKeydown($event, windowState)"
          />
          <div class="chat-window__actions">
            <label class="button button--ghost chat-window__upload">
              Photo
              <input class="visually-hidden" type="file" accept="image/*" @change="uploadImage(windowState, $event)" />
            </label>
            <button class="button" type="button" :disabled="windowState.isSending" @click="sendMessage(windowState)">
              Send
            </button>
          </div>
        </div>
      </section>
    </div>

    <div class="app-chat-bar">
      <div class="app-chat-bar__right">
        <div v-if="footerWindows.length" class="app-chat-bar__open-list">
          <button
            v-for="windowState in footerWindows"
            :key="windowState.conversationId"
            class="app-chat-bar__chip"
            :class="{
              'app-chat-bar__chip--active': activeConversationId === windowState.conversationId
            }"
            type="button"
            @click="reopenConversation(windowState)"
          >
            <span class="app-chat-bar__chip-status" :class="{ 'app-chat-bar__chip-status--online': windowState.otherUserOnline }" />
            <span class="app-chat-bar__chip-name">{{ windowState.otherUsername }}</span>
            <span v-if="windowState.unreadCount > 0" class="badge badge--accent">{{ windowState.unreadCount }}</span>
          </button>
        </div>

        <button class="app-chat-bar__launcher" type="button" @click="toggleLauncher">
          <span>Chat</span>
          <span v-if="unreadConversationCount > 0" class="badge badge--accent">{{ unreadConversationCount }}</span>
        </button>
      </div>
    </div>

    <Transition name="chat-sheet">
      <div v-if="imagePreviewUrl" class="chat-image-lightbox" @click="imagePreviewUrl = null">
        <img class="chat-image-lightbox__image" :src="imagePreviewUrl" alt="Chat preview" />
      </div>
    </Transition>
  </div>
</template>
