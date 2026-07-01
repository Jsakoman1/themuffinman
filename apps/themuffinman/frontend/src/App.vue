<script setup lang="ts">
import axios from "axios"
import {computed, onMounted} from "vue"
import {useRoute} from "vue-router"
import AppTopbar from "./components/app/AppTopbar.vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"

const route = useRoute()
const isAdminRoute = computed(() => route.path.startsWith("/admin"))
const isAuthRoute = computed(() => route.path.startsWith("/login") || route.path.startsWith("/register"))
const isVisionRoute = computed(() => route.path.startsWith("/vision"))
const showLegacyChrome = computed(() => !isAdminRoute.value && !isVisionRoute.value && !isAuthRoute.value)

onMounted(() => {
  if (!token.value) {
    return
  }

  void (async () => {
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
</script>

<template>
  <div :class="['app-shell', { 'app-shell--vision': isVisionRoute }]">
    <AppTopbar v-if="showLegacyChrome" />

    <main class="app-main">
      <router-view/>
    </main>
  </div>
</template>
