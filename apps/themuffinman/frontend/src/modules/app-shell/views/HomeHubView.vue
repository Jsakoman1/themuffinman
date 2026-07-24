<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRoute} from "vue-router"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import TaskSurface from "../components/TaskSurface.vue"

const route = useRoute()
const surface = getAppSurfaceConfig("home")
const {model, isLoading, error, reload} = useShellSurfaceData("home", route)
const dismissedRows = ref<string[]>([])
const recentContexts = ref<string[]>([])
const summaryRows = computed(() => model.value.metrics.map((metric, index) => ({
  id: `home-summary-${index}`,
  title: `${metric.label} · ${metric.value}`,
  description: metric.detail,
  badge: metric.tone === "emphasis" ? "Attention" : undefined,
  to: metric.to,
})))
const visibleRows = computed(() => summaryRows.value.filter(row => !dismissedRows.value.includes(row.id)))
const dismissRow = (id: string) => { dismissedRows.value = [...dismissedRows.value, id]; localStorage.setItem("homeDismissedRows", JSON.stringify(dismissedRows.value)) }
onMounted(() => { try { dismissedRows.value = JSON.parse(localStorage.getItem("homeDismissedRows") || "[]"); recentContexts.value = JSON.parse(localStorage.getItem("homeRecentContexts") || "[]") } catch { /* local home state can reset safely */ } })
const todayRows = computed(() => visibleRows.value.filter(row => /confirm|review|overdue|today|attention/i.test(`${row.title} ${row.description}`)))
const nextRows = computed(() => visibleRows.value.filter(row => !todayRows.value.includes(row)).slice(0, 5))
const informationRows = computed(() => visibleRows.value.filter(row => !todayRows.value.includes(row) && !nextRows.value.includes(row)))
</script>

<template>
  <!-- UX simplification: Home exposes one next-action surface before secondary detail. -->
  <TaskSurface mode="choose" label="Home next actions"><section class="home-hub" aria-live="polite" :aria-busy="isLoading || undefined">
    <SurfaceHeader :config="surface" />
    <AppLoadingState v-if="isLoading" label="Loading your workspace summary" :rows="4" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <section v-else class="home-hub__main" aria-label="Home summary and next actions" data-surface="personal-next-actions">
        <div class="home-hub__section-heading"><p class="home-hub__label">Today</p><p class="home-hub__hint" role="note">Confirmations, reviews, overdue items, and actions that need attention now.</p></div>
        <SurfaceRow v-for="row in todayRows" :key="row.id" :row="row"><template #actions><button type="button" class="home-hub__dismiss" @click.stop="dismissRow(row.id)">Dismiss</button></template></SurfaceRow>
        <AppStatus v-if="todayRows.length === 0" message="Nothing needs action today." />
        <div class="home-hub__section-heading home-hub__section-heading--next"><p class="home-hub__label">Next</p><p class="home-hub__hint" role="note">Upcoming appointments and the next useful step.</p></div>
        <SurfaceRow v-for="row in nextRows" :key="row.id" :row="row"><template #actions><button type="button" class="home-hub__dismiss" @click.stop="dismissRow(row.id)">Dismiss</button></template></SurfaceRow>
        <div v-if="informationRows.length" class="home-hub__section-heading home-hub__section-heading--information"><p class="home-hub__label">Information</p><p class="home-hub__hint" role="note">Useful summaries that do not require action.</p></div>
        <SurfaceRow v-for="row in informationRows" :key="row.id" :row="row" />
        <section class="home-hub__recent" aria-label="Recent contexts"><p class="home-hub__label">Recent contexts</p><p v-if="recentContexts.length === 0" class="home-hub__hint">Your recently used business, chat, and work contexts will appear here.</p><ul v-else><li v-for="context in recentContexts" :key="context">{{ context }}</li></ul></section>
        <AppStatus v-if="summaryRows.length === 0" message="No workspace summary is available yet." />
    </section>
  </section></TaskSurface>
</template>

<style scoped>
.home-hub{display:grid;gap:var(--space-3);min-width:0}.home-hub__main{display:grid;gap:0;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.home-hub__main :deep(.surface-row:last-child){border-bottom:0}.home-hub__section-heading{display:grid;gap:var(--space-1);padding:var(--space-3) var(--space-4);border-bottom:1px solid var(--border-subtle)}.home-hub__label{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.home-hub__hint{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}
</style>
<style scoped>.home-hub__section-heading--next,.home-hub__section-heading--information{border-top:1px solid var(--border-subtle)}.home-hub__dismiss{min-height:var(--control-height-compact);padding:var(--space-1) var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink);font:inherit;cursor:pointer}.home-hub__recent{display:grid;gap:var(--space-1);padding:var(--space-3) var(--space-4);border-top:1px solid var(--border-subtle)}.home-hub__recent p{margin:0}.home-hub__recent ul{margin:0;padding-left:1.2rem;color:var(--text-muted)}</style>
