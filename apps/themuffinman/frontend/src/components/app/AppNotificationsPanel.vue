<script setup lang="ts">
import {computed, ref} from "vue"
import type {QuestNewsItem} from "../../modules/workmarket/api/workmarketApi.ts"
import {formatInstantForDisplay} from "../../shared/questSchedule.ts"

const props = defineProps<{
  items: QuestNewsItem[]
  unreadCount: number
  error: string
  isLoading: boolean
}>()

const emit = defineEmits<{
  "open-item": [item: QuestNewsItem]
  "mark-all-read": []
}>()

const feedMode = ref<"unread" | "all">("unread")

const unreadItems = computed(() => props.items.filter((item) => item.readAt === null))
const visibleItems = computed(() => feedMode.value === "all" ? props.items : unreadItems.value)
const now = () => new Date()

const isToday = (instant: string) => {
  const date = new Date(instant)
  const today = now()
  return date.getFullYear() === today.getFullYear()
    && date.getMonth() === today.getMonth()
    && date.getDate() === today.getDate()
}

const groupedVisibleItems = computed(() => {
  const todayItems = visibleItems.value.filter((item) => isToday(item.createdAt))
  const earlierItems = visibleItems.value.filter((item) => !isToday(item.createdAt))

  return [
    {key: "today", label: "Today", items: todayItems},
    {key: "earlier", label: "Earlier", items: earlierItems}
  ].filter((group) => group.items.length > 0)
})

const panelSummary = computed(() => {
  if (feedMode.value === "unread") {
    return unreadItems.value.length > 0
      ? `${unreadItems.value.length} unread`
      : "No unread notifications"
  }

  return props.items.length > 0
    ? `${props.items.length} recent notifications`
    : "No recent notifications"
})

const openItem = (item: QuestNewsItem) => {
  emit("open-item", item)
}

const markAllRead = () => {
  emit("mark-all-read")
}

const notificationIcon = (item: QuestNewsItem) => {
  switch (item.type) {
    case "APPLICATION_APPROVED":
      return "✓"
    case "APPLICATION_DECLINED":
    case "QUEST_TERM_REJECTED":
      return "!"
    case "QUEST_COMPLETED":
      return "■"
    case "QUEST_STARTED":
      return "▶"
    case "CIRCLE_REQUEST_ACCEPTED":
      return "◎"
    case "APPLICATION_CREATED":
    case "APPLICATION_UPDATED":
    case "APPLICATION_WITHDRAWN":
      return "↗"
    default:
      return "•"
  }
}
</script>

<template>
  <section class="dashboard-news-panel dashboard-news-panel--drawer">
    <div class="dashboard-news__header">
      <div class="dashboard-news__header-copy">
        <h2 class="dashboard-news__title">Notifications</h2>
        <p class="dashboard-news__subtitle">{{ panelSummary }}</p>
      </div>

      <button
        v-if="unreadCount > 0"
        class="button button--ghost dashboard-news__clear"
        type="button"
        @click="markAllRead"
      >
        Mark all read
      </button>
    </div>

    <div class="ui-pill-tabs">
      <button
        class="ui-pill-tabs__button"
        :class="{ 'ui-pill-tabs__button--active': feedMode === 'unread' }"
        type="button"
        @click="feedMode = 'unread'"
      >
        Unread
        <span class="badge">{{ unreadItems.length }}</span>
      </button>
      <button
        class="ui-pill-tabs__button"
        :class="{ 'ui-pill-tabs__button--active': feedMode === 'all' }"
        type="button"
        @click="feedMode = 'all'"
      >
        Recent
        <span class="badge">{{ items.length }}</span>
      </button>
    </div>

    <div v-if="isLoading" class="empty-state empty-state--soft">
      Loading notifications...
    </div>

    <div v-else-if="error" class="empty-state empty-state--error">
      {{ error }}
    </div>

    <div v-else-if="groupedVisibleItems.length" class="dashboard-news__groups">
      <section
        v-for="group in groupedVisibleItems"
        :key="group.key"
        class="dashboard-news__group"
      >
        <div class="dashboard-news__group-label">{{ group.label }}</div>

        <div class="dashboard-news__list">
          <button
            v-for="item in group.items"
            :key="item.id"
            class="dashboard-news__item"
            :class="{ 'dashboard-news__item--unread': item.readAt === null }"
            type="button"
            @click="openItem(item)"
          >
            <div class="dashboard-news__item-icon" :class="item.readAt === null ? 'dashboard-news__item-icon--unread' : ''">
              {{ notificationIcon(item) }}
            </div>

            <div class="dashboard-news__item-main">
              <div class="dashboard-news__item-top">
                <strong class="dashboard-news__item-title">{{ item.title }}</strong>
                <span class="dashboard-news__item-time">{{ formatInstantForDisplay(item.createdAt) }}</span>
              </div>

              <div class="dashboard-news__item-tags">
                <span :class="['badge', item.badgeClass]">{{ item.typeLabel }}</span>
              </div>
              <p class="dashboard-news__item-message">{{ item.message }}</p>
              <div class="dashboard-news__item-meta">
                <span>{{ item.actorUsername }}</span>
                <span v-if="item.questTitle">{{ item.questTitle }}</span>
              </div>
            </div>
          </button>
        </div>
      </section>
    </div>

    <div v-else class="empty-state empty-state--soft">
      No notifications yet.
    </div>
  </section>
</template>
