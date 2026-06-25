<script setup lang="ts">
import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"
import UserProfileDialog from "../components/profile/UserProfileDialog.vue"

const route = useRoute()
const router = useRouter()

const userId = computed(() => {
  const parsed = Number(route.params.id)
  return Number.isFinite(parsed) ? parsed : null
})

const closeProfile = async () => {
  if (window.history.length > 1) {
    await router.back()
    return
  }

  await router.push("/circles")
}

</script>

<template>
  <UserProfileDialog
    :open="true"
    :user-id="userId"
    @close="closeProfile"
  />
</template>
