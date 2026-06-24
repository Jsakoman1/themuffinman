<script setup lang="ts">
import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"
import AppPageHeader from "../../../../components/app/AppPageHeader.vue"

const props = defineProps<{
  title: string
  subtitle: string
}>()

const route = useRoute()
const router = useRouter()

const isQuestsActive = computed(() => route.path.startsWith("/admin/work") || route.path.startsWith("/admin/quests"))
const isUsersActive = computed(() => route.path.startsWith("/admin/users"))
const isApplicationsActive = computed(() => route.path.startsWith("/admin/applications"))
const isCirclesActive = computed(() => route.path.startsWith("/admin/circles"))

const goToQuests = () => {
  void router.push("/admin/work")
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
  <AppPageHeader class="admin-shell-header" eyebrow="Admin area" :title="title" :subtitle="subtitle">
    <template #actions>
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
          Work
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
      </div>
    </template>
  </AppPageHeader>
</template>
