import {ref, watch} from "vue"
import type {Router} from "vue-router"
import {currentUser} from "../../../auth.ts"
import type {QuestNewsItem} from "../../../modules/workmarket/api/workmarketApi.ts"
import {workmarketApi} from "../../../modules/workmarket/api/workmarketApi.ts"
import {routeForNavigationTarget} from "../../../modules/workmarket/shared/navigationTargets.ts"

export const useTopbarNotifications = (router: Router) => {
  const notificationsOpen = ref(false)
  const unreadNewsCount = ref(0)
  const notificationItems = ref<QuestNewsItem[]>([])
  const isLoadingNotifications = ref(false)
  const notificationsError = ref("")

  const resetNotificationsState = () => {
    notificationItems.value = []
    notificationsError.value = ""
  }

  const refreshUnreadNewsCount = async () => {
    if (!currentUser.value) {
      unreadNewsCount.value = 0
      return
    }

    try {
      const summary = await workmarketApi.getDashboardSummary()
      unreadNewsCount.value = summary.unreadNewsCount
    } catch {
      unreadNewsCount.value = 0
    }
  }

  const loadNotifications = async () => {
    if (!currentUser.value) {
      resetNotificationsState()
      return
    }

    isLoadingNotifications.value = true
    notificationsError.value = ""

    try {
      notificationItems.value = await workmarketApi.getMyNews()
    } catch {
      notificationsError.value = "Could not load notifications."
      notificationItems.value = []
    } finally {
      isLoadingNotifications.value = false
    }
  }

  const toggleNotifications = async () => {
    if (notificationsOpen.value) {
      notificationsOpen.value = false
      return
    }

    notificationsOpen.value = true
    await loadNotifications()
    await refreshUnreadNewsCount()
  }

  const markAllNotificationsRead = async () => {
    await workmarketApi.markMyNewsAsRead()
    unreadNewsCount.value = 0
    notificationItems.value = notificationItems.value.map((item) => ({
      ...item,
      readAt: item.readAt ?? new Date().toISOString()
    }))
  }

  const openNotificationItem = async (item: QuestNewsItem) => {
    await workmarketApi.markMyNewsItemAsRead(item.id)
    unreadNewsCount.value = Math.max(0, unreadNewsCount.value - (item.readAt === null ? 1 : 0))
    notificationItems.value = notificationItems.value.map((candidate) => candidate.id === item.id
      ? {...candidate, readAt: candidate.readAt ?? new Date().toISOString()}
      : candidate
    )
    notificationsOpen.value = false
    await router.push(routeForNavigationTarget(item.navigation))
  }

  watch(() => currentUser.value?.id, async (userId) => {
    resetNotificationsState()

    if (!userId) {
      unreadNewsCount.value = 0
      return
    }

    await refreshUnreadNewsCount()
  })

  return {
    notificationsOpen,
    unreadNewsCount,
    notificationItems,
    isLoadingNotifications,
    notificationsError,
    refreshUnreadNewsCount,
    toggleNotifications,
    markAllNotificationsRead,
    openNotificationItem,
    resetNotificationsState
  }
}
