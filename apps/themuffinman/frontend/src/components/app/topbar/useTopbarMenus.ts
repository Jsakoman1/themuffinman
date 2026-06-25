import {onBeforeUnmount, onMounted, ref, watch} from "vue"
import type {RouteLocationNormalizedLoaded} from "vue-router"

export const useTopbarMenus = (
  route: RouteLocationNormalizedLoaded,
  topbarRef: {value: HTMLElement | null}
) => {
  const accountMenuOpen = ref(false)

  const closeMenus = () => {
    accountMenuOpen.value = false
  }

  const toggleAccountMenu = () => {
    accountMenuOpen.value = !accountMenuOpen.value
  }

  const handleDocumentClick = (event: MouseEvent) => {
    const target = event.target as Node | null
    if (topbarRef.value && target && !topbarRef.value.contains(target)) {
      closeMenus()
    }
  }

  watch(() => route.fullPath, closeMenus)

  onMounted(() => {
    document.addEventListener("click", handleDocumentClick)
  })

  onBeforeUnmount(() => {
    document.removeEventListener("click", handleDocumentClick)
  })

  return {
    accountMenuOpen,
    closeMenus,
    toggleAccountMenu
  }
}
