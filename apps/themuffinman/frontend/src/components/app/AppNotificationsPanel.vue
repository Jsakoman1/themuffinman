<script setup lang="ts">
import {computed} from "vue"
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
  "accept-circle-request": [item: QuestNewsItem]
  "decline-circle-request": [item: QuestNewsItem]
}>()

const unreadItems = computed(() => props.items.filter((item) => item.readAt === null))
const now = () => new Date()

const isToday = (instant: string) => {
  const date = new Date(instant)
  const today = now()
  return date.getFullYear() === today.getFullYear()
    && date.getMonth() === today.getMonth()
    && date.getDate() === today.getDate()
}

const groupedVisibleItems = computed(() => {
  const todayItems = unreadItems.value.filter((item) => isToday(item.createdAt))
  const earlierItems = unreadItems.value.filter((item) => !isToday(item.createdAt))

  return [
    {key: "today", label: "Today", items: todayItems},
    {key: "earlier", label: "Earlier", items: earlierItems}
  ].filter((group) => group.items.length > 0)
})

const panelSummary = computed(() => unreadItems.value.length > 0
  ? `${unreadItems.value.length} unread`
  : "No unread notifications"
)

const openItem = (item: QuestNewsItem) => {
  emit("open-item", item)
}

const markAllRead = () => {
  emit("mark-all-read")
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

    <div class="dashboard-news__summary-badge">
      <span class="badge badge--accent">{{ unreadItems.length }}</span>
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
          <article
            v-for="item in group.items"
            :key="item.id"
            class="dashboard-news__item"
            :class="{ 'dashboard-news__item--unread': item.readAt === null }"
          >
            <button
              class="dashboard-news__item-hit"
              type="button"
              @click="openItem(item)"
            >
            <div class="dashboard-news__item-icon" :class="item.readAt === null ? 'dashboard-news__item-icon--unread' : ''">
              {{ item.iconGlyph }}
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

            <div v-if="item.canAcceptCircleRequest || item.canDeclineCircleRequest" class="dashboard-news__item-actions">
              <button v-if="item.canAcceptCircleRequest" class="button" type="button" @click="emit('accept-circle-request', item)">Accept</button>
              <button v-if="item.canDeclineCircleRequest" class="button button--ghost" type="button" @click="emit('decline-circle-request', item)">Decline</button>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div v-else class="empty-state empty-state--soft">
      No unread notifications.
    </div>
  </section>
</template>
