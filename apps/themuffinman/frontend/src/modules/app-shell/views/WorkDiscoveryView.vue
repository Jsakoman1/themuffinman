<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {QuestResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import {buildSurfaceVisionRoute} from "../visionHandoff.ts"
import {currentUser} from "../../identity/auth.ts"
import {handleCollectionKeyboard, useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import ObjectPreviewPanel from "../components/ObjectPreviewPanel.vue"
import {isEditableTarget} from "../composables/useObjectActions.ts"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import AppEmptyState from "../components/AppEmptyState.vue"
import AppLoadingState from "../components/AppLoadingState.vue"
import DisplayDensityControl from "../components/DisplayDensityControl.vue"
import AppButton from "../components/AppButton.vue"

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
const scope = computed(() => typeof route.query.scope === "string" ? route.query.scope : (isMine.value ? "owned-active" : "open-visible"))
const isOwnedActive = computed(() => scope.value === "owned-active")
const title = computed(() => isOwnedActive.value ? "My active work" : route.name === "work-find" ? "Find work" : "Work")
const visionRoute = computed(() => buildSurfaceVisionRoute(
  isMine.value ? "work-quests" : "work",
  route.fullPath,
  `${title.value}${query.value.trim() ? ` · ${query.value.trim()}` : ""}`
))

const formatDate = (value: string | null) => {
  if (!value) return "Flexible"
  return new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value))
}

const locationLabel = (quest: QuestResponseDTO) => quest.presentation.locationLabel || quest.locationLocality || "Anywhere"

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
      preset: isOwnedActive.value ? "MY_ACTIVE" : "AVAILABLE",
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

watch([query, sort, scheduledOnly], () => {
  syncCanonicalQuery()
  if (searchTimer !== undefined) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => void load(), 250)
})

const rememberSelection = (id: number) => {
  viewState.value.selectedId = id
  viewState.value.scrollY = window.scrollY
}
const detailRoute = (id: number) => resolveSurfaceDetailRoute("work-quests", id) ?? `/vision/quests/${id}`
const previewQuest = computed(() => items.value.find(item => item.id === viewState.value.previewId) ?? null)
const previewSummary = ref("")
const openPreview = async (id: number) => {
  rememberSelection(id)
  viewState.value.previewId = id
  try { previewSummary.value = (await userShellApi.getQuestPreview(id)).summary || "No description provided." } catch { previewSummary.value = "This work item is no longer available." }
}
const handleRowClick = (event: MouseEvent, id: number) => {
  // Primary row links own navigation. Do not let the selection-state watcher
  // race that navigation by replacing the canonical detail route with a
  // collection query such as `?selected=…`.
  if (event.target instanceof Element && event.target.closest("a,button")) return
  if (!isEditableTarget(event.target)) rememberSelection(id)
}
const handleKeyboard = (event: KeyboardEvent) => handleCollectionKeyboard(event, items.value.map(item => item.id), viewState.value, {
  open: (id) => { void router.push(detailRoute(id)) },
  preview: (id) => { void openPreview(id) },
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
  <section class="work-discovery" aria-labelledby="work-discovery-title">
    <header class="work-discovery__header">
      <div>
        <p class="work-discovery__eyebrow">{{ isMine ? "Work / Mine" : "Work" }}</p>
        <h1 id="work-discovery-title">{{ title }}</h1>
      </div>
    </header>

    <CollectionToolbar :title="title" :count="totalItems" :busy="isLoading">
      <template #filters>
      <label class="work-discovery__search">
        <span class="sr-only">Search work</span>
        <input v-model="query" type="search" placeholder="Search work" @keyup.enter="load()">
      </label>
      <details class="work-discovery__options">
        <summary>View options</summary>
        <div class="work-discovery__options-panel">
          <label>Sort <select v-model="sort" aria-label="Sort work"><option value="recommended">Recommended</option><option value="newest">Newest</option><option value="soonest">Soonest</option><option value="highest_reward">Highest reward</option></select></label>
          <label class="work-discovery__toggle"><input v-model="scheduledOnly" type="checkbox"><span>Scheduled only</span></label>
          <DisplayDensityControl v-model="viewState.displayDensity" />
        </div>
      </details>
      </template>
      <template #actions>
        <RouterLink :to="visionRoute" class="work-discovery__vision">Ask Vision</RouterLink>
        <RouterLink to="/work/offer" class="work-discovery__create">Offer work</RouterLink>
      </template>
    </CollectionToolbar>

    <AppLoadingState v-if="isLoading" label="Loading work" :rows="5" />
    <div v-else-if="error" class="work-discovery__status work-discovery__status--error" role="alert"><strong>Work could not be loaded.</strong><span>{{ error }}</span><AppButton type="button" tone="secondary" @click="load()">Try again</AppButton></div>
    <AppEmptyState v-else-if="items.length === 0" title="No matching work" message="Try another search or adjust the filters." />

    <p v-if="items.length" class="work-discovery__scope">{{ isOwnedActive ? "Owned work that still needs attention." : "Available work visible to you." }}</p>
    <div v-if="items.length" class="work-discovery__workspace">
    <div class="work-discovery__list">
      <SurfaceRow v-for="quest in items" :key="quest.id" :row="{id: String(quest.id), title: quest.title, description: `${quest.presentation.statusLabel} · ${locationLabel(quest)}`, meta: `${quest.awardAmount} € · ${formatDate(quest.scheduledAt)}`, to: detailRoute(quest.id)}" :density="viewState.displayDensity" :selected="viewState.selectedId === quest.id" :previewed="previewQuest?.id === quest.id" @click="handleRowClick($event, quest.id)" @preview="openPreview(quest.id)">
        <template #actions><AppButton type="button" tone="quiet" @click.stop="openPreview(quest.id)">Preview</AppButton></template>
      </SurfaceRow>
    </div>
    <ObjectPreviewPanel :title="previewQuest?.title ?? 'Work'" subtitle="Work preview" :open="previewQuest !== null" @close="viewState.previewId = null" @open-detail="previewQuest && router.push(detailRoute(previewQuest.id))">
      <p v-if="previewQuest">{{ previewSummary || previewQuest.description || 'No description provided.' }}</p>
      <p v-if="previewQuest" class="work-discovery__preview-meta">{{ previewQuest.presentation.statusLabel }} · {{ previewQuest.creatorUsername }}</p>
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
  </section>
</template>

<style scoped>
.work-discovery {
  display: grid;
  gap: 1rem;
}

.work-discovery__header,
.work-discovery__controls,
.work-discovery__row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.work-discovery__workspace { display: grid; grid-template-columns: minmax(0, 1fr) minmax(18rem, 24rem); border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); overflow: hidden; }
.work-discovery__workspace .work-discovery__list { padding: 0.45rem; }
.work-discovery__preview-meta { color: var(--text-muted); font-size: 0.84rem; }
.work-discovery__options { position: relative; }
.work-discovery__options summary { cursor: pointer; color: var(--text-muted); font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.work-discovery__options-panel { position: absolute; z-index: 2; right: 0; display: grid; gap: var(--space-2); min-width: 13rem; margin-top: var(--space-1); padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-raised); box-shadow: var(--shadow-popover); }
.work-discovery__options-panel label { display: grid; gap: var(--space-1); color: var(--text-muted); font-size: var(--text-size-meta); }
@media (max-width: 860px) { .work-discovery__workspace { grid-template-columns: 1fr; } .work-discovery__workspace :deep(.object-preview) { border-left: 0; border-top: 1px solid var(--border-subtle); } }

.work-discovery__header {
  justify-content: space-between;
}

.work-discovery__header-actions {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.work-discovery__eyebrow {
  margin: 0 0 0.25rem;
  color:var(--text-muted);
  font-size: 0.76rem;
  font-weight: 650;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(1.55rem, 2.5vw, 2.3rem);
  letter-spacing: -0.075em;
}

.work-discovery__vision,
.work-discovery__create,
.work-discovery__open,
.work-discovery__load-more {
  display: inline-flex;
  align-items: center;
  min-height: var(--control-height-default);
  border: 1px solid var(--control-border);
  border-radius: var(--radius-control);
  padding: var(--space-1) var(--space-3);
  color: var(--control-ink);
  font-size: var(--text-size-meta);
  font-weight: var(--text-weight-semibold);
}

.work-discovery__vision {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--canvas);
}

.work-discovery__create {
  border-color: var(--control-border-active);
  background: var(--control-bg);
  color: var(--control-ink);
}

.work-discovery__controls {
  flex-wrap: wrap;
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

.work-discovery__scope{margin:0;color:var(--text-soft);font-size:.78rem}

.work-discovery__count {
  margin-left: auto;
  color:var(--text-muted);
  font-size: 0.78rem;
}

.work-discovery__list {
  overflow: hidden;
  border:1px solid var(--border-subtle);
}

.work-discovery__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  border:1px solid var(--border-subtle);
  padding: 0.9rem 0.2rem;
}

.work-discovery__row-main {
  display: grid;
  gap: 0.25rem;
  min-width: 0;
}

.work-discovery__title {
  overflow: hidden;
  color:var(--text);
  font-size: 0.98rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-discovery__meta,
.work-discovery__row-facts {
  color:var(--text-muted);
  font-size: 0.76rem;
}

.work-discovery__row-facts {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.7rem;
}

.work-discovery__open {
  display: inline-flex;
  align-items: center;
  color:var(--text);
}

.work-discovery__status {
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-surface);
  background: var(--surface-base);
  padding: var(--space-3);
  color:var(--text-muted);
}

.work-discovery__status--error {
  color: var(--danger);
}

.work-discovery__load-more {
  justify-self: center;
  background: transparent;
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

@media (max-width: 700px) {
  .work-discovery__header {
    align-items: flex-start;
  }

  .work-discovery__row {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .work-discovery__row-facts {
    grid-column: 1;
    justify-content: flex-start;
  }

  .work-discovery__open {
    grid-column: 2;
    grid-row: 1 / span 2;
  }
}

/* Desktop product surface overrides: preserve the existing data and interactions. */
.work-discovery{gap:1.15rem;max-width:80rem}.work-discovery__header,.work-discovery__controls{gap:.7rem}.work-discovery__header{padding-bottom:.15rem}.work-discovery__eyebrow{color:var(--text-soft);font-size:.7rem;font-weight:700;letter-spacing:.1em}h1{color:var(--text);font-size:clamp(1.45rem,2.4vw,2rem);letter-spacing:-.055em;line-height:1.05}.work-discovery__create,.work-discovery__load-more{min-height:2.25rem;border-color:var(--accent);border-radius:var(--radius-control);padding:.45rem .7rem;background:var(--accent);color:var(--canvas);font-weight:700}.work-discovery__controls{padding:.65rem;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface)}.work-discovery__search{flex-basis:17rem}.work-discovery__search input,select{min-height:2.35rem;border-color:var(--border-subtle);border-radius:var(--radius-control);background:var(--control-bg);color:var(--text);padding:.5rem .65rem}.work-discovery__toggle{min-height:2.25rem;color:var(--text-muted)}.work-discovery__toggle input{accent-color:var(--accent)}.work-discovery__count{color:var(--text-soft);white-space:nowrap}.work-discovery__list{border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface)}.work-discovery__row{grid-template-columns:minmax(0,1fr) minmax(20rem,auto);gap:1rem;border:0;border-bottom:1px solid var(--border-subtle);border-radius:0;padding:.9rem 1rem;background:transparent}.work-discovery__row:last-child{border-bottom:0}.work-discovery__row:hover{background:var(--surface-hover)}.work-discovery__title{color:var(--text);font-size:.94rem;font-weight:680;letter-spacing:-.02em}.work-discovery__meta{width:max-content;max-width:100%;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-control);padding:.14rem .42rem;color:var(--text-muted);font-size:.7rem;text-overflow:ellipsis;white-space:nowrap}.work-discovery__row-facts{gap:.4rem;color:var(--text-muted)}.work-discovery__fact{border-left:1px solid var(--border-subtle);padding-left:.5rem;white-space:nowrap}.work-discovery__fact:first-child{border-left:0}.work-discovery__fact--reward{color:var(--text);font-weight:700}.work-discovery__status{display:grid;gap:.25rem;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface);color:var(--text-muted)}.work-discovery__status strong{color:var(--text);font-size:.9rem}.work-discovery__status--error{border-color:rgba(226,109,109,.5)}.work-discovery__status--error strong{color:var(--danger)}.work-discovery__status button{justify-self:start;margin-top:.35rem;border:0;border-radius:var(--radius-control);padding:.42rem .6rem;background:var(--surface-hover);color:var(--text);font:inherit;font-size:.78rem;font-weight:650}.work-discovery__load-more{justify-self:center;background:var(--surface-strong);color:var(--text);border-color:var(--border-strong)}

@media (max-width: 700px){.work-discovery__row{grid-template-columns:minmax(0,1fr)}.work-discovery__row-facts{grid-column:auto;justify-content:flex-start}.work-discovery__open{grid-column:auto;grid-row:auto}}

/* Shared workspace control contract; route content remains a dense collection. */
.work-discovery__vision,.work-discovery__create,.work-discovery__open,.work-discovery__load-more{border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink)}
.work-discovery__vision,.work-discovery__create{border-color:var(--accent);background:var(--accent);color:var(--canvas)}
.work-discovery__search input,select{border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink)}
.work-discovery__meta{border-radius:var(--radius-control);background:var(--surface-muted)}
.work-discovery__status{border-radius:var(--radius-surface);background:var(--surface-base)}
.work-discovery__status--error{color:var(--danger)}
</style>
<style scoped>
.work-discovery .app-button { min-height:2.25rem; border-radius:var(--radius-control); padding:.45rem .7rem; background:var(--control-bg); color:var(--control-ink); }
.work-discovery .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
</style>
