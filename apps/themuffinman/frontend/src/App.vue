<script setup lang="ts">
import axios from "axios"
import {computed, onMounted} from "vue"
import {useRoute} from "vue-router"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"

const route = useRoute()
const isVisionRoute = computed(() => route.path.startsWith("/vision"))

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
    <main class="app-main">
      <router-view/>
    </main>
  </div>
</template>
