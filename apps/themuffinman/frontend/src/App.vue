<script setup lang="ts">
import axios from "axios"
import {onBeforeUnmount, onMounted} from "vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"
import AppActionDialog from "./modules/app-shell/components/AppActionDialog.vue"
import {userShellApi, type AppearancePreference} from "./modules/app-shell/api/userShellApi.ts"
import {applyAppearanceTheme, readCachedAppearanceTheme} from "./services/appearanceTheme.ts"

// Apply the last known choice before the authenticated preference request returns.
applyAppearanceTheme(readCachedAppearanceTheme())

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
  window.addEventListener("app:appearance-changed", handleAppearanceChanged)
})
onBeforeUnmount(() => window.removeEventListener("app:appearance-changed", handleAppearanceChanged))

</script>

<template>
  <router-view />
  <AppActionDialog />
</template>
