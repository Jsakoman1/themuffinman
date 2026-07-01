import {computed, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import {currentUser, logoutUser} from "../../auth.ts"
import {useTopbarMenus} from "./topbar/useTopbarMenus.ts"

export const useAppTopbarState = () => {
  const topbarRef = ref<HTMLElement | null>(null)
  const router = useRouter()
  const route = useRoute()
  const menus = useTopbarMenus(route, topbarRef)
  const homeTarget = computed(() => "/vision")

  const openProfile = async () => {
    menus.closeMenus()
    if (!currentUser.value) {
      return
    }

    await router.push(`/users/${currentUser.value.id}`)
  }

  const openSettings = async () => {
    menus.closeMenus()
    if (!currentUser.value) {
      return
    }

    await router.push("/settings")
  }

  const handleLogout = async () => {
    menus.closeMenus()
    logoutUser()
    await router.push("/login")
  }

  return {
    topbarRef,
    accountMenuOpen: menus.accountMenuOpen,
    homeTarget,
    toggleAccountMenu: () => {
      menus.toggleAccountMenu()
    },
    openProfile,
    openSettings,
    handleLogout
  }
}
