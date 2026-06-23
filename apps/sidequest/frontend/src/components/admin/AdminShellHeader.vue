<script setup lang="ts">
import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"

const props = defineProps<{
  title: string
  subtitle: string
  onLogout: () => void
}>()

const route = useRoute()
const router = useRouter()

const isQuestsActive = computed(() => route.path.startsWith("/admin/quests"))
const isUsersActive = computed(() => route.path.startsWith("/admin/users"))
const isApplicationsActive = computed(() => route.path.startsWith("/admin/applications"))
const isCirclesActive = computed(() => route.path.startsWith("/admin/circles"))

const goToQuests = () => {
  void router.push("/admin/quests")
}

const goToUsers = () => {
  void router.push("/admin/users")
}

const goToApplications = () => {
  void router.push("/admin/applications")
}

const goToCircles = () => {
  void router.push("/admin/circles")
}
</script>

<template>
  <header class="admin-shell-header">
    <div class="admin-shell-header__copy">
      <p class="admin-shell-header__eyebrow">Admin area</p>
      <h1 class="page-title">{{ title }}</h1>
      <p class="page-subtitle">{{ subtitle }}</p>
    </div>

    <div class="admin-shell-header__actions">
      <div class="admin-shell-header__tabs" role="tablist" aria-label="Admin sections">
        <button
          class="button button--secondary admin-shell-header__tab"
          :class="{ 'button--active': isQuestsActive }"
          type="button"
          role="tab"
          :aria-selected="isQuestsActive"
          @click="goToQuests"
        >
          Quests
        </button>
        <button
          class="button button--secondary admin-shell-header__tab"
          :class="{ 'button--active': isUsersActive }"
          type="button"
          role="tab"
          :aria-selected="isUsersActive"
          @click="goToUsers"
        >
          Users
        </button>
        <button
          class="button button--secondary admin-shell-header__tab"
          :class="{ 'button--active': isApplicationsActive }"
          type="button"
          role="tab"
          :aria-selected="isApplicationsActive"
          @click="goToApplications"
        >
          Applications
        </button>
        <button
          class="button button--secondary admin-shell-header__tab"
          :class="{ 'button--active': isCirclesActive }"
          type="button"
          role="tab"
          :aria-selected="isCirclesActive"
          @click="goToCircles"
        >
          Circles
        </button>
      </div>

      <button class="button button--secondary" type="button" @click="props.onLogout">Logout</button>
    </div>
  </header>
</template>
