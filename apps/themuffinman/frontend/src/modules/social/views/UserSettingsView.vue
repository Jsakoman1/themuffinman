<script setup lang="ts">
import {currentUser} from "../../../auth.ts"
import {useRouter} from "vue-router"
import UserSettingsDialog from "../components/profile/UserSettingsDialog.vue"

const router = useRouter()

const closeSettings = async () => {
  if (window.history.length > 1) {
    await router.back()
    return
  }

  if (currentUser.value?.id) {
    await router.push(`/users/${currentUser.value.id}`)
    return
  }

  await router.push("/vision")
}
</script>

<template>
  <UserSettingsDialog
    :open="true"
    @close="closeSettings"
  />
</template>
