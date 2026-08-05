<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import type {QuestResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import {currentUser} from "../../identity/auth.ts"
import {collectionReturnQuery, handleCollectionKeyboard, useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import AppEmptyState from "../components/AppEmptyState.vue"
import AppLoadingState from "../components/AppLoadingState.vue"
import DisplayDensityControl from "../components/DisplayDensityControl.vue"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import {formatCurrency, formatDateTime} from "../../../services/formatters.ts"
import TaskSurface from "../components/TaskSurface.vue"
import FriendlyCollectionHeader from "../components/FriendlyCollectionHeader.vue"

const route = useRoute()
const router = useRouter()
const query = ref(typeof route.query.q === "string" ? route.query.q : "")
const sort = ref(typeof route.query.sort === "string" ? route.query.sort : "recommended")
const scheduledOnly = ref(route.query.scheduled === "1")
const routeContext = computed(() => `${route.path}?q=${encodeURIComponent(query.value.trim())}&sort=${sort.value}&scheduled=${scheduledOnly.value ? "1" : "0"}`)
const {state: viewState} = useSurfaceViewState("work-discovery", computed(() => currentUser.value?.id), routeContext)
const items = ref<QuestResponseDTO[]>([])
const page = ref(0)
const totalItems = ref(0)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
let searchTimer: number | undefined
let requestSequence = 0
let activeRequest: AbortController | null = null

const isMine = computed(() => route.name === "work-quests")
// Keep the viewer scope explicit at the request boundary. The backend owns
// visibility and ownership rules; the page only selects the matching preset.
const workPreset = computed(() => isMine.value ? "MY_VISIBLE" as const : "AVAILABLE" as const)
const title = computed(() => isMine.value ? "My posted SideJobs" : "Find a SideJob")
const emptyTitle = computed(() => isMine.value ? "You have not posted a SideJob yet" : "No SideJobs are available")
const emptyMessage = computed(() => isMine.value ? "Post a SideJob when you need help, then manage requests here." : "Try another search or check back when someone needs help.")
const primaryAction = computed(() => isMine.value
  ? {label: "Find a SideJob", to: "/work/find"}
  : {label: "Post a SideJob", to: "/work/quests/new"})

const locationLabel = (quest: QuestResponseDTO) => quest.presentation.locationLabel || quest.locationLocality || "Anywhere"
const sideJobCard = (quest: QuestResponseDTO) => ({
  id: String(quest.id),
  title: quest.title,
  description: `${formatDateTime(quest.scheduledAt, quest.presentation.timeTypeLabel)} · ${locationLabel(quest)} · ${quest.presentation.commitmentLabel}`,
  badge: quest.presentation.attentionLabel || quest.presentation.statusLabel,
  meta: `${formatCurrency(Number(quest.awardAmount ?? 0))} · ${quest.presentation.nextActionLabel}`,
  to: detailRoute(quest.id)
})

const load = async (reset = true) => {
  const requestId = ++requestSequence
  activeRequest?.abort()
  activeRequest = new AbortController()
  if (reset) {
    isLoading.value = true
    page.value = 0
    items.value = []
  } else {
    isLoadingMore.value = true
  }
  error.value = ""

  try {
    const response = await userShellApi.searchQuests({
      q: query.value,
      preset: workPreset.value,
      scopeKey: isMine.value ? "mine" : "available",
      sort: sort.value === "recommended" ? undefined : sort.value,
      page: page.value,
      size: 12,
      scheduledOnly: scheduledOnly.value,
      signal: activeRequest.signal
    })
    if (requestId !== requestSequence) return
    items.value = reset ? response.items : [...items.value, ...response.items]
    totalItems.value = response.totalItems
    page.value = response.page
    if (viewState.value.selectedId !== null && !items.value.some(item => item.id === viewState.value.selectedId)) viewState.value.selectedId = null
  } catch {
    if (requestId !== requestSequence) return
    error.value = "Could not load SideJobs."
  } finally {
    if (requestId !== requestSequence) return
    isLoading.value = false
    isLoadingMore.value = false
  }
}

const loadMore = async () => {
  if (isLoadingMore.value || items.value.length >= totalItems.value) return
  page.value += 1
  await load(false)
}

const syncCanonicalQuery = () => {
  const nextQuery: Record<string, string> = {}
  if (query.value.trim()) nextQuery.q = query.value.trim()
  if (sort.value !== "recommended") nextQuery.sort = sort.value
  if (scheduledOnly.value) nextQuery.scheduled = "1"
  void router.replace({path: route.path, query: nextQuery})
}

let hydratingScope = false

watch([query, sort, scheduledOnly], () => {
  // Route changes hydrate the new scope in one controlled request below.
  // Do not schedule a second request while that state is being reset.
  if (hydratingScope) return
  syncCanonicalQuery()
  if (searchTimer !== undefined) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => void load(), 250)
})

watch(() => route.name, async (nextRouteName, previousRouteName) => {
  if (nextRouteName === previousRouteName || (nextRouteName !== "work-find" && nextRouteName !== "work-quests")) return

  // Work tabs reuse the same component instance. Reset collection-local state
  // and reload whenever the canonical route changes, otherwise Find help and
  // My posts can display the previous tab's results under the new heading.
  hydratingScope = true
  if (searchTimer !== undefined) window.clearTimeout(searchTimer)
  query.value = typeof route.query.q === "string" ? route.query.q : ""
  sort.value = typeof route.query.sort === "string" ? route.query.sort : "recommended"
  scheduledOnly.value = route.query.scheduled === "1"
  viewState.value.selectedId = null
  await nextTick()
  hydratingScope = false
  await load()
})

const detailRoute = (id: number) => {
  const target = resolveSurfaceDetailRoute("work-quests", id) ?? `/work/quests/${id}`
  return typeof target === "string"
    ? {path: target, query: collectionReturnQuery(route.fullPath)}
    : {...target, query: {...target.query, ...collectionReturnQuery(route.fullPath)}}
}
const handleKeyboard = (event: KeyboardEvent) => handleCollectionKeyboard(event, items.value.map(item => item.id), viewState.value, {
  open: id => void router.push(detailRoute(id)),
  clear: () => { viewState.value.selectedId = null; viewState.value.previewId = null },
})

onMounted(async () => {
  window.addEventListener("keydown", handleKeyboard)
  await load()
  await nextTick()
  if (viewState.value.scrollY > 0) window.scrollTo({top: viewState.value.scrollY, behavior: "auto"})
})
onBeforeUnmount(() => { window.removeEventListener("keydown", handleKeyboard); viewState.value.scrollY = window.scrollY })
</script>

<template>
  <!-- A listing opens its full detail directly; the collection route is retained for Back. -->
  <TaskSurface mode="inspect" label="SideJobs"><section class="work-discovery" data-collection-rhythm="oriented" aria-labelledby="work-discovery-title" :aria-busy="isLoading || isLoadingMore || undefined">
    <FriendlyCollectionHeader eyebrow="SideJobs" :title="title" :description="isMine ? 'SideJobs you posted and can manage.' : 'Browse local requests where you can offer help. Opening one never commits you.'" tone="work" :primary-action="primaryAction" />
    <CollectionToolbar :title="title" :count="totalItems" :busy="isLoading" filter-summary="Refine">
      <template #filters>
      <label class="work-discovery__search">
        <span class="sr-only">Search SideJobs</span>
        <input v-model="query" type="search" placeholder="Search SideJobs" @keyup.enter="load()">
      </label>
      <label class="work-discovery__sort">Sort <select v-model="sort" aria-label="Sort SideJobs"><option value="recommended">Recommended</option><option value="newest">Newest</option><option value="soonest">Soonest</option><option value="highest_reward">Highest payment</option></select></label>
      <details class="work-discovery__options" aria-label="SideJob filters">
        <summary>Filters</summary>
        <div class="work-discovery__options-panel">
          <label class="work-discovery__toggle"><input v-model="scheduledOnly" type="checkbox"><span>Has a fixed time</span></label>
          <DisplayDensityControl v-model="viewState.displayDensity" />
        </div>
      </details>
      </template>
    </CollectionToolbar>

    <AppLoadingState v-if="isLoading" label="Loading SideJobs" :rows="5" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <AppEmptyState v-else-if="items.length === 0" :reason="query.trim() || scheduledOnly ? 'filtered' : isMine ? 'not-created' : 'not-visible'" :title="emptyTitle" :message="emptyMessage" />

    <div v-if="items.length" class="work-discovery__workspace native-group" aria-label="SideJob results" data-selection-model="direct-detail">
    <div class="work-discovery__list">
      <SurfaceRow v-for="quest in items" :key="quest.id" :row="sideJobCard(quest)" :density="viewState.displayDensity" :selected="viewState.selectedId === quest.id" />
    </div>
    </div>

    <AppButton
      v-if="!isLoading && items.length < totalItems"
      type="button"
      tone="secondary"
      :loading="isLoadingMore"
      @click="loadMore"
    >
      {{ isLoadingMore ? "Loading" : "Load more" }}
    </AppButton>
  </section></TaskSurface>
</template>

<style scoped>
.work-discovery {
  display: grid;
  gap: 1rem;
}
.work-discovery__workspace { display: grid; grid-template-columns: minmax(0, 1fr); overflow: hidden; }
.work-discovery__workspace .work-discovery__list { display:grid; gap:var(--space-2); padding:var(--space-2); }
.work-discovery__list :deep(.surface-row){border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface-raised)}
.work-discovery__list :deep(.surface-row:hover){background:var(--surface-hover);transform:translateY(-1px)}
.work-discovery__options { position: relative; }
.work-discovery__options summary { cursor: pointer; color: var(--text-muted); font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.work-discovery__options-panel { position: absolute; z-index: 2; right: 0; display: grid; gap: var(--space-2); min-width: 13rem; margin-top: var(--space-1); padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-raised); box-shadow: var(--shadow-popover); }
.work-discovery__options-panel label { display: grid; gap: var(--space-1); color: var(--text-muted); font-size: var(--text-size-meta); }
.work-discovery__create {
  display: inline-flex;
  align-items: center;
  min-height: var(--control-height-default);
  border: 1px solid var(--control-border);
  border-color: var(--control-border-active);
  border-radius: var(--radius-control);
  background: var(--control-bg);
  padding: var(--space-1) var(--space-3);
  color: var(--control-ink);
  font-size: var(--text-size-meta);
  font-weight: var(--text-weight-semibold);
}

.work-discovery__search {
  flex: 1 1 16rem;
}

.work-discovery__search input,
select {
  width: 100%;
  min-height: var(--control-height-default);
  border: 1px solid var(--control-border);
  border-radius: var(--radius-control);
  background: var(--control-bg);
  color: var(--control-ink);
  padding: var(--space-2) var(--space-3);
  font: inherit;
}

select {
  width: auto;
}

.work-discovery__toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 2.5rem;
  padding: 0 0.45rem;
  color:var(--text-muted);
  font-size: 0.8rem;
}

.work-discovery__list {
  overflow: hidden;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.work-discovery {
  max-width: 80rem;
}

.work-discovery__create {
  min-height: 2.25rem;
  border-color: var(--accent);
  border-radius: var(--radius-control);
  padding: 0.45rem 0.7rem;
  background: var(--accent);
  color: var(--canvas);
  font-weight: var(--text-weight-semibold);
}

.work-discovery__search {
  flex-basis: 17rem;
}

.work-discovery__search input,
.work-discovery select {
  min-height: 2.35rem;
  border-color: var(--border-subtle);
  border-radius: var(--radius-control);
  background: var(--control-bg);
  color: var(--text);
  padding: 0.5rem 0.65rem;
}

.work-discovery__toggle {
  min-height: 2.25rem;
  color: var(--text-muted);
}

.work-discovery__toggle input {
  accent-color: var(--accent);
}

.work-discovery__options-panel {
  background: var(--surface-raised);
}

@media (max-width: 980px) {
  .work-discovery__workspace--preview { grid-template-columns: minmax(0, 1fr); }
}

</style>
