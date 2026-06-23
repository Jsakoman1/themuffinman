<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {formatQuestNewsType, questNewsBadgeClass} from "../../shared/questNews.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const router = useRouter()
const feedMode = ref<"unread" | "all">("unread")

const feedItems = computed(() => {
  const sourceItems = feedMode.value === "all" ? props.dashboard.recentNewsItems : props.dashboard.unreadNewsItems

  return sourceItems.map((item) => ({
    id: item.id,
    title: item.title,
    message: item.message,
    actorUsername: item.actorUsername,
    questTitle: item.questTitle,
    createdAt: item.createdAt,
    typeLabel: formatQuestNewsType(item.type),
    badgeClass: questNewsBadgeClass(item.type),
    questId: item.questId,
    applicationId: item.applicationId,
    isUnread: item.readAt === null
  }))
})

const openQuest = async (item: {id: number; questId: number | null}) => {
  await props.dashboard.markNewsItemAsRead(item.id)
  props.dashboard.closeNotificationsDialog()

  if (item.questId !== null && props.dashboard.questForId(item.questId)) {
    await router.push(`/quests/${item.questId}`)
    return
  }

  await router.push("/quests")
}

const openApplication = async (item: {id: number; applicationId: number | null; questId: number | null}) => {
  if (item.applicationId === null) {
    return
  }

  await props.dashboard.markNewsItemAsRead(item.id)
  props.dashboard.closeNotificationsDialog()
  await router.push({
    path: `/applications/${item.applicationId}`,
    query: item.questId !== null ? {questId: String(item.questId)} : undefined
  })
}

const openItem = (item: {id: number; questId: number | null; applicationId: number | null}) => {
  if (item.applicationId !== null) {
    void openApplication(item)
    return
  }

  void openQuest(item)
}
</script>

<template>
  <section class="dashboard-news-panel dashboard-news-panel--drawer">
    <div class="dashboard-news__header">
      <div class="dashboard-news__header-copy">
        <h2 class="dashboard-news__title">Notifications</h2>
      </div>

      <button
        v-if="props.dashboard.unreadNewsCount > 0"
        class="dashboard-news__clear"
        type="button"
        @click="props.dashboard.markNewsAsRead()"
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

    <div v-if="props.dashboard.newsError" class="empty-state empty-state--error">
      {{ props.dashboard.newsError }}
    </div>

    <div v-else-if="feedItems.length" class="dashboard-news__list">
      <button
        v-for="item in feedItems"
        :key="item.id"
        class="dashboard-news__item"
        :class="{ 'dashboard-news__item--unread': item.isUnread }"
        type="button"
        @click="openItem(item)"
      >
        <div class="dashboard-news__item-dot" :class="item.isUnread ? 'dashboard-news__item-dot--unread' : ''" />

        <div class="dashboard-news__item-main">
          <div class="dashboard-news__item-top">
            <span :class="['badge', item.badgeClass]">{{ item.typeLabel }}</span>
            <span class="dashboard-news__item-time">{{ props.dashboard.formatDateTime(item.createdAt) }}</span>
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
