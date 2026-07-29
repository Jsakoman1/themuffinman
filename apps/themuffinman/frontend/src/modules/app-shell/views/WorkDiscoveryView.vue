<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import type {QuestResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import {currentUser} from "../../identity/auth.ts"
import {handleCollectionKeyboard, useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import {isEditableTarget} from "../composables/useObjectActions.ts"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import AppEmptyState from "../components/AppEmptyState.vue"
import AppLoadingState from "../components/AppLoadingState.vue"
import DisplayDensityControl from "../components/DisplayDensityControl.vue"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import {formatDateTime} from "../../../services/formatters.ts"
import TaskSurface from "../components/TaskSurface.vue"
import ObjectPreviewPanel from "../components/ObjectPreviewPanel.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

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
const title = computed(() => isMine.value ? "My work" : route.name === "work-find" ? "Find work" : "Find work")
const emptyTitle = computed(() => isMine.value ? "You have not offered any work yet" : "No work is available")
const emptyMessage = computed(() => isMine.value ? "Create your first work offer to manage it from this tab." : "Try a different search or check back when new work is posted.")
const selectedQuest = computed(() => items.value.find(item => item.id === viewState.value.selectedId) ?? null)
const surface = computed(() => getAppSurfaceConfig(isMine.value ? "work-quests" : "work"))

const locationLabel = (quest: QuestResponseDTO) => quest.presentation.locationLabel || quest.locationLocality || "Anywhere"
const plainText = (value: string | null | undefined) => (value || "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim()

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
    if (viewState.value.previewId !== null && !items.value.some(item => item.id === viewState.value.previewId)) viewState.value.previewId = null
  } catch {
    if (requestId !== requestSequence) return
    error.value = "Could not load work."
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
  // and reload whenever the canonical route changes, otherwise Find work and
  // My work can display the previous tab's results under the new heading.
  hydratingScope = true
  if (searchTimer !== undefined) window.clearTimeout(searchTimer)
  query.value = typeof route.query.q === "string" ? route.query.q : ""
  sort.value = typeof route.query.sort === "string" ? route.query.sort : "recommended"
  scheduledOnly.value = route.query.scheduled === "1"
  viewState.value.selectedId = null
  viewState.value.previewId = null
  await nextTick()
  hydratingScope = false
  await load()
})

const rememberSelection = (id: number) => {
  viewState.value.selectedId = id
  viewState.value.scrollY = window.scrollY
}
const detailRoute = (id: number) => resolveSurfaceDetailRoute("work-quests", id) ?? `/work/quests/${id}`
const openDetail = () => { if (selectedQuest.value) void router.push(detailRoute(selectedQuest.value.id)) }
const handleRowClick = (event: MouseEvent, id: number) => {
  // Primary row links own navigation. Do not let the selection-state watcher
  // race that navigation by replacing the canonical detail route with a
  // collection query such as `?selected=…`.
  if (event.target instanceof Element && event.target.closest("a,button")) return
  if (!isEditableTarget(event.target)) rememberSelection(id)
}
const handleKeyboard = (event: KeyboardEvent) => handleCollectionKeyboard(event, items.value.map(item => item.id), viewState.value, {
  open: rememberSelection,
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
  <!-- UX simplification: Work keeps browse, inspect, and act states in one surface. -->
  <TaskSurface mode="inspect" label="Work discovery"><section class="work-discovery" aria-labelledby="work-discovery-title" :aria-busy="isLoading || isLoadingMore || undefined">
    <SurfaceHeader :config="surface" :title="title" :description="isMine ? 'Work you created and can manage.' : 'Available work visible to you.'" />
    <nav class="work-discovery__role-guide" aria-label="Choose your work task" data-work-first-view="find-or-offer">
      <div><p class="work-discovery__role-label">{{ isMine ? 'Offering work' : 'Looking for work' }}</p><p>{{ isMine ? 'Review the work you posted, its applicants, and what needs your decision.' : 'Browse work you can apply for. Opening a listing never commits you.' }}</p></div>
      <RouterLink v-if="isMine" to="/work/find">Find available work</RouterLink><RouterLink v-else to="/work/quests/new">Offer work</RouterLink>
    </nav>
    <CollectionToolbar :title="title" :count="totalItems" :busy="isLoading" filter-summary="Refine">
      <template #filters>
      <label class="work-discovery__search">
        <span class="sr-only">Search work</span>
        <input v-model="query" type="search" placeholder="Search work" @keyup.enter="load()">
      </label>
      <label class="work-discovery__sort">Sort <select v-model="sort" aria-label="Sort work"><option value="recommended">Recommended</option><option value="newest">Newest</option><option value="soonest">Soonest</option><option value="highest_reward">Highest reward</option></select></label>
      <details class="work-discovery__options" aria-label="Work filters">
        <summary>Filters</summary>
        <div class="work-discovery__options-panel">
          <label class="work-discovery__toggle"><input v-model="scheduledOnly" type="checkbox"><span>Scheduled only</span></label>
          <DisplayDensityControl v-model="viewState.displayDensity" />
        </div>
      </details>
      </template>
    </CollectionToolbar>

    <AppLoadingState v-if="isLoading" label="Loading work" :rows="5" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <AppEmptyState v-else-if="items.length === 0" :reason="query.trim() || scheduledOnly ? 'filtered' : isMine ? 'not-created' : 'not-visible'" :title="emptyTitle" :message="emptyMessage" />

    <div v-if="items.length" class="work-discovery__workspace native-group" :class="{'work-discovery__workspace--preview': selectedQuest}" aria-label="Work results and selected preview" data-selection-model="persistent-list-selection">
    <div class="work-discovery__list">
      <SurfaceRow v-for="quest in items" :key="quest.id" :row="{id: String(quest.id), title: quest.title, description: `${quest.presentation.statusLabel} · ${locationLabel(quest)}`, meta: `${quest.awardAmount} € · ${formatDateTime(quest.scheduledAt)}`, to: detailRoute(quest.id)}" primary-action="preview" :density="viewState.displayDensity" :selected="viewState.selectedId === quest.id" @click="handleRowClick($event, quest.id)" @preview="rememberSelection(quest.id)" @open="rememberSelection(quest.id)" />
    </div>
    <ObjectPreviewPanel v-if="selectedQuest" :open="true" :title="selectedQuest.title" subtitle="Work preview" @close="viewState.selectedId = null" @open-detail="openDetail">
      <p>{{ plainText(selectedQuest.description) || "No description provided." }}</p>
      <dl><div><dt>Status</dt><dd>{{ selectedQuest.presentation.statusLabel }}</dd></div><div><dt>Location</dt><dd>{{ locationLabel(selectedQuest) }}</dd></div><div><dt>Reward</dt><dd>{{ selectedQuest.awardAmount }} €</dd></div><div><dt>When</dt><dd>{{ formatDateTime(selectedQuest.scheduledAt) }}</dd></div></dl>
    </ObjectPreviewPanel>
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
.work-discovery__role-guide{display:flex;align-items:center;justify-content:space-between;gap:var(--space-3);max-width:52rem;padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.work-discovery__role-guide p{margin:0;color:var(--text-muted)}.work-discovery__role-guide p:last-child{margin-top:var(--space-1)}.work-discovery__role-label{color:var(--text-soft)!important;font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.work-discovery__role-guide a{flex:none;padding:var(--space-2) var(--space-3);border-radius:var(--radius-control);background:var(--accent);color:var(--accent-contrast);font-weight:var(--text-weight-semibold);text-decoration:none}@media(max-width:620px){.work-discovery__role-guide{align-items:start;flex-direction:column}}

.work-discovery__workspace { display: grid; grid-template-columns: minmax(0, 1fr); overflow: hidden; }
.work-discovery__workspace--preview { grid-template-columns: minmax(0, 1fr) minmax(18rem, 24rem); }
.work-discovery__workspace .work-discovery__list { padding: 0.45rem; }
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
