<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import SurfaceRow from "../components/SurfaceRow.vue"

const route = useRoute()
const surface = getAppSurfaceConfig("home")
const {model, isLoading, error, reload} = useShellSurfaceData("home", route)
const summaryRows = computed(() => model.value.metrics.map((metric, index) => ({
  id: `home-summary-${index}`,
  title: `${metric.label} · ${metric.value}`,
  description: metric.detail,
  badge: metric.tone === "emphasis" ? "Attention" : undefined,
  to: metric.to,
})))
</script>

<template>
  <section class="home-hub" aria-live="polite">
    <SurfaceHeader :config="surface" />
    <AppLoadingState v-if="isLoading" label="Loading your workspace summary" :rows="4" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <section v-else class="home-hub__main" aria-label="Home summary and next actions">
        <SurfaceRow v-for="row in summaryRows" :key="row.id" :row="row" />
        <AppStatus v-if="summaryRows.length === 0" message="No workspace summary is available yet." />
    </section>
  </section>
</template>

<style scoped>
.home-hub{display:grid;gap:var(--space-3);min-width:0}.home-hub__main{display:grid;gap:0;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.home-hub__main :deep(.surface-row:last-child){border-bottom:0}
</style>
