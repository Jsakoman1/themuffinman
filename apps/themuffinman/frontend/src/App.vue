<script setup lang="ts">
import axios from "axios"
import {onBeforeUnmount, onMounted} from "vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"
import AppActionDialog from "./modules/app-shell/components/AppActionDialog.vue"
import {userShellApi, type AppearancePreference} from "./modules/app-shell/api/userShellApi.ts"

const applyAppearanceTheme = (theme: AppearancePreference["theme"]) => {
  document.documentElement.dataset.theme = theme.toLowerCase()
  document.documentElement.style.colorScheme = theme === "SYSTEM" ? "light dark" : theme.toLowerCase()
}
const handleAppearanceChanged = (event: Event) => applyAppearanceTheme((event as CustomEvent<AppearancePreference["theme"]>).detail)

onMounted(() => {
  if (!token.value) {
    return
  }

  void (async () => {
    const appearance = await userShellApi.getAppearancePreference().catch(() => ({theme: "SYSTEM" as const}))
    applyAppearanceTheme(appearance.theme)
    try {
      const response = await authApi.me()
      saveSession({
        ...response,
        token: token.value
      })
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        clearSession()
        window.location.assign("/login")
      }
    }
  })()
})

onMounted(() => {
  if (!token.value) return
  window.addEventListener("app:appearance-changed", handleAppearanceChanged)
})
onBeforeUnmount(() => window.removeEventListener("app:appearance-changed", handleAppearanceChanged))

const editableTarget = (target: EventTarget | null) => target instanceof HTMLElement && Boolean(target.closest("input,textarea,select,[contenteditable='true']"))
const handleGlobalShortcut = (event: KeyboardEvent) => {
  if (editableTarget(event.target) || event.defaultPrevented) return
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") { event.preventDefault(); window.dispatchEvent(new Event("app:open-command")) }
  if (!event.metaKey && !event.ctrlKey && !event.altKey && event.key.toLowerCase() === "c") { event.preventDefault(); window.dispatchEvent(new Event("app:open-create")) }
}
onMounted(() => window.addEventListener("keydown", handleGlobalShortcut))
onBeforeUnmount(() => window.removeEventListener("keydown", handleGlobalShortcut))
</script>

<template>
  <router-view />
  <AppActionDialog />
</template>
