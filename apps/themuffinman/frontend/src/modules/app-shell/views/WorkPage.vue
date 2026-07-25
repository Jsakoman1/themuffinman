<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import WorkDiscoveryView from "./WorkDiscoveryView.vue"
import WorkApplicationsView from "./WorkApplicationsView.vue"

const route = useRoute()
const isApplications = computed(() => route.name === "work-applications")
const activeTabId = computed(() => isApplications.value ? "applications" : route.name === "work-quests" ? "mine" : "discover")
</script>

<template>
  <section class="work-page" aria-label="Work" data-filter-model="single-filters-action" data-preview-model="shared-adjacent-preview" data-mental-model="browse-inspect-preview" data-scope-source="canonical-route" data-responsive-model="desktop-list-mobile-sheet" data-work-navigation="canonical-find-mine-applications" :data-work-scope="isApplications ? 'applications' : activeTabId">
    <WorkApplicationsView v-if="isApplications" />
    <WorkDiscoveryView v-else />
  </section>
</template>

<style scoped>
.work-page{display:grid;gap:var(--space-4);min-width:0}
</style>
