<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
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
const orientation = computed(() => model.value.sections[0])
</script>

<template>
  <section class="home-hub" aria-live="polite">
    <SurfaceHeader :config="surface" />
    <CollectionToolbar title="Resume work" :count="summaryRows.length" :busy="isLoading" />
    <AppLoadingState v-if="isLoading" label="Loading your workspace summary" :rows="4" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <div v-else class="home-hub__workspace">
      <section class="home-hub__main" aria-label="Workspace summary">
        <SurfaceRow v-for="row in summaryRows" :key="row.id" :row="row" />
        <AppStatus v-if="summaryRows.length === 0" message="No workspace summary is available yet." />
      </section>
      <aside class="home-hub__rail" aria-label="Workspace context">
        <h2>{{ orientation?.title ?? "Workspace context" }}</h2>
        <p>{{ orientation?.description ?? model.note }}</p>
        <div v-if="orientation?.rows.length" class="home-hub__orientation">
          <SurfaceRow v-for="row in orientation.rows" :key="row.id" :row="row" />
        </div>
        <AppStatus v-else :message="model.note || 'No additional workspace context is available yet.'" />
      </aside>
    </div>
  </section>
</template>

<style scoped>
.home-hub{display:grid;gap:var(--space-3);min-width:0}.home-hub__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,var(--detail-rail-width));overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.home-hub__main{min-width:0}.home-hub__main :deep(.surface-row:last-child){border-bottom:0}.home-hub__rail{min-width:0;border-left:1px solid var(--border-subtle);background:var(--surface-raised)}.home-hub__rail h2{margin:0;padding:var(--space-3);border-bottom:1px solid var(--border-subtle);color:var(--text);font-size:var(--text-size-title)}.home-hub__rail>p{margin:0;padding:var(--space-3);color:var(--text-muted);font-size:var(--text-size-body);line-height:1.5}.home-hub__orientation{border-top:1px solid var(--border-subtle)}@media(max-width:980px){.home-hub__workspace{grid-template-columns:1fr}.home-hub__rail{border-top:1px solid var(--border-subtle);border-left:0}}
</style>
