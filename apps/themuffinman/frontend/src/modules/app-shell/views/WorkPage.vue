<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import WorkDiscoveryView from "./WorkDiscoveryView.vue"
import WorkApplicationsView from "./WorkApplicationsView.vue"
import ModuleTabs from "../components/ModuleTabs.vue"

const route = useRoute()
const isApplications = computed(() => route.name === "work-applications")
const activeTabId = computed(() => isApplications.value ? "applications" : route.name === "work-quests" ? "mine" : "discover")
const tabs = [
  {id: "discover", label: "Find help", route: "/work/find", backendScope: "work.discover", emptyState: "No SideJobs are available yet."},
  {id: "mine", label: "My posts", route: "/work/quests", backendScope: "work.mine", emptyState: "You have not posted a SideJob yet."},
  {id: "applications", label: "My help offers", route: "/work/applications", backendScope: "work.applications", emptyState: "You have no SideJob activity yet."}
]
</script>

<template>
  <section class="work-page" aria-label="SideJobs" data-collection-rhythm="oriented" data-filter-model="single-filters-action" data-preview-model="shared-adjacent-preview" data-mental-model="browse-inspect-decide" data-scope-source="canonical-route" data-responsive-model="desktop-list-mobile-sheet" data-work-navigation="canonical-find-mine-activity" data-work-first-view="intent-before-role" :data-work-scope="isApplications ? 'activity' : activeTabId">
    <ModuleTabs :tabs="tabs" :active-id="activeTabId" />
    <WorkApplicationsView v-if="isApplications" />
    <WorkDiscoveryView v-else />
  </section>
</template>

<style scoped>
.work-page{display:grid;align-content:start;gap:var(--space-4);min-width:0}
</style>
