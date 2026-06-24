<script setup lang="ts">
import axios from "axios"
import {computed, onMounted} from "vue"
import AppTopbar from "./components/app/AppTopbar.vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"

const currentYear = computed(() => new Date().getFullYear())

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
      if (axios.isAxiosError(error) && (error.response?.status === 401 || error.response?.status === 403)) {
        clearSession()
        window.location.assign("/login")
      }
    }
  })()
})
</script>

<template>
  <div class="app-shell">
    <AppTopbar />

    <main class="app-main">
      <router-view/>
    </main>

    <footer class="site-footer">
      <span class="site-footer__flag" aria-hidden="true">🇨🇭</span>
      <span>© {{ currentYear }} Sakoman. Made in Switzerland.</span>
    </footer>
  </div>
</template>
