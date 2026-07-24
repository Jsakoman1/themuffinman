<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import WorkDiscoveryView from "./WorkDiscoveryView.vue"
import WorkApplicationsView from "./WorkApplicationsView.vue"

const route = useRoute()
const workTabs = computed(() => getModuleTabs("work")?.tabs ?? [])
const isApplications = computed(() => route.name === "work-applications")
const activeTabId = computed(() => isApplications.value ? "applications" : route.name === "work-quests" ? "mine" : "discover")
</script>

<template>
  <section class="work-page" aria-label="Work" data-filter-model="single-filters-action" data-preview-model="adjacent">
    <p class="work-page__hint">Choose a work area above. Search and filters apply only to the selected area.</p>
    <ModuleTabs :tabs="workTabs" :active-id="activeTabId" />
    <WorkApplicationsView v-if="isApplications" />
    <WorkDiscoveryView v-else />
  </section>
</template>

<style scoped>
.work-page{display:grid;gap:var(--space-3);min-width:0}.work-page__hint{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}
</style>
