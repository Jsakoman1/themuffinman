import {onMounted, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import {currentUser, logoutUser} from "../../auth.ts"
import {createTopbarNavState} from "./topbar/createTopbarNavState.ts"
import {useTopbarMenus} from "./topbar/useTopbarMenus.ts"
import {useTopbarNotifications} from "./topbar/useTopbarNotifications.ts"

export const useAppTopbarState = () => {
  const topbarRef = ref<HTMLElement | null>(null)
  const router = useRouter()
  const route = useRoute()
  const {homeTarget, isAuthenticated, topbarNavItems} = createTopbarNavState(route)
  const notifications = useTopbarNotifications(router)
  const menus = useTopbarMenus(route, topbarRef)

  const openProfile = async () => {
    menus.closeMenus()
    notifications.notificationsOpen.value = false
    if (!currentUser.value) {
      return
    }

    await router.push(`/users/${currentUser.value.id}`)
  }

  const openSettings = async () => {
    menus.closeMenus()
    notifications.notificationsOpen.value = false
    if (!currentUser.value) {
      return
    }

    await router.push("/settings")
  }

  const handleLogout = async () => {
    menus.closeMenus()
    notifications.notificationsOpen.value = false
    logoutUser()
    await router.push("/login")
  }

  onMounted(() => {
    void notifications.refreshUnreadNewsCount()
  })

  return {
    topbarRef,
    accountMenuOpen: menus.accountMenuOpen,
    notificationsOpen: notifications.notificationsOpen,
    unreadNewsCount: notifications.unreadNewsCount,
    notificationItems: notifications.notificationItems,
    isLoadingNotifications: notifications.isLoadingNotifications,
    notificationsError: notifications.notificationsError,
    homeTarget,
    isAuthenticated,
    topbarNavItems,
    toggleAccountMenu: () => {
      notifications.notificationsOpen.value = false
      menus.toggleAccountMenu()
    },
    toggleNotifications: async () => {
      menus.accountMenuOpen.value = false
      await notifications.toggleNotifications()
    },
    openProfile,
    openSettings,
    markAllNotificationsRead: notifications.markAllNotificationsRead,
    openNotificationItem: notifications.openNotificationItem,
    handleLogout
  }
}
