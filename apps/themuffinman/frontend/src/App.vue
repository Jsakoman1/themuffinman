<script setup lang="ts">
import axios from "axios"
import {onMounted} from "vue"
import {authApi} from "./modules/identity/api/authApi.ts"
import {clearSession, saveSession, token} from "./services/sessionService.ts"

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
  <router-view />
</template>
