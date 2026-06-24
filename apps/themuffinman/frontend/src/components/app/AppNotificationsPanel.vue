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
      </div>

      <button
        v-if="unreadCount > 0"
        class="dashboard-news__clear"
        type="button"
        @click="markAllRead"
      >
        Mark all read
      </button>
    </div>

    <div class="dashboard-news__tabs">
      <button
        class="dashboard-news__tab"
        :class="{ 'dashboard-news__tab--active': feedMode === 'unread' }"
        type="button"
        @click="feedMode = 'unread'"
      >
        Unread
      </button>
      <button
        class="dashboard-news__tab"
        :class="{ 'dashboard-news__tab--active': feedMode === 'all' }"
        type="button"
        @click="feedMode = 'all'"
      >
        Recent
      </button>
    </div>

    <div v-if="isLoading" class="empty-state empty-state--soft">
      Loading notifications...
    </div>

    <div v-else-if="error" class="empty-state empty-state--error">
      {{ error }}
    </div>

    <div v-else-if="visibleItems.length" class="dashboard-news__list">
      <button
        v-for="item in visibleItems"
        :key="item.id"
        class="dashboard-news__item"
        :class="{ 'dashboard-news__item--unread': item.readAt === null }"
        type="button"
        @click="openItem(item)"
      >
        <div class="dashboard-news__item-dot" :class="item.readAt === null ? 'dashboard-news__item-dot--unread' : ''" />

        <div class="dashboard-news__item-main">
          <div class="dashboard-news__item-top">
            <span :class="['badge', item.badgeClass]">{{ item.typeLabel }}</span>
            <span class="dashboard-news__item-time">{{ formatInstantForDisplay(item.createdAt) }}</span>
          </div>

          <strong class="dashboard-news__item-title">{{ item.title }}</strong>
          <p class="dashboard-news__item-message">{{ item.message }}</p>
          <div class="dashboard-news__item-meta">
            <span>{{ item.actorUsername }}</span>
            <span v-if="item.questTitle">{{ item.questTitle }}</span>
          </div>
        </div>
      </button>
    </div>

    <div v-else class="empty-state empty-state--soft">
      No notifications yet.
    </div>
  </section>
</template>
