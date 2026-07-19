import {onBeforeUnmount, onMounted, ref} from "vue"
import {userShellApi} from "../api/userShellApi.ts"
import type {WorkspaceNavigationModule, WorkspaceNavigationResponse} from "../../../contracts/index.ts"

export const useWorkspaceNavigation = () => {
  const navigation = ref<WorkspaceNavigationResponse | null>(null)
  const loading = ref(true)
  const error = ref(false)
  let refreshTimer: number | null = null

  const load = async () => {
    loading.value = true
    error.value = false
    try {
      navigation.value = await userShellApi.getWorkspaceNavigation()
      scheduleRefresh()
    } catch {
      error.value = true
    } finally {
      loading.value = false
    }
  }

  const scheduleRefresh = () => {
    if (refreshTimer !== null) window.clearTimeout(refreshTimer)
    const delay = Math.max(10, navigation.value?.refreshAfterSeconds ?? 30) * 1000
    refreshTimer = window.setTimeout(() => { void load() }, delay)
  }

  const refreshOnVisibility = () => { if (document.visibilityState === "visible") void load() }

  const modules = () => (navigation.value?.modules ?? []).filter((module) => module.visible).sort((a, b) => a.order - b.order)
  const moduleById = (id: string): WorkspaceNavigationModule | undefined => modules().find((module) => module.id === id)

  onMounted(() => { document.addEventListener("visibilitychange", refreshOnVisibility); void load() })
  onBeforeUnmount(() => { document.removeEventListener("visibilitychange", refreshOnVisibility); if (refreshTimer !== null) window.clearTimeout(refreshTimer) })

  return {navigation, modules, moduleById, loading, error, reload: load}
}
