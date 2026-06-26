<script setup lang="ts">
import axios from "axios"
import {computed, onMounted} from "vue"
import {useRoute} from "vue-router"
import AppChatTray from "./components/app/AppChatTray.vue"
import AppTopbar from "./components/app/AppTopbar.vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, currentUser, saveSession, token} from "./services/sessionService.ts"

const route = useRoute()
const currentYear = computed(() => new Date().getFullYear())
const isAdminRoute = computed(() => route.path.startsWith("/admin"))

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
  <div :class="['app-shell', { 'app-shell--with-chat': !!currentUser }]">
    <AppTopbar v-if="!isAdminRoute" />

    <main class="app-main">
      <router-view/>
    </main>

    <AppChatTray v-if="!isAdminRoute" />

    <footer v-if="!isAdminRoute" class="site-footer">
      <span class="site-footer__flag" aria-hidden="true">🇨🇭</span>
      <span>© {{ currentYear }} Sakoman. Made in Switzerland.</span>
    </footer>
  </div>
</template>
