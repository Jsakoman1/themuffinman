<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {QuestResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import {buildVisionRoute} from "../visionHandoff.ts"

const route = useRoute()
const router = useRouter()
const query = ref("")
const sort = ref("recommended")
const scheduledOnly = ref(false)
const items = ref<QuestResponseDTO[]>([])
const page = ref(0)
const totalItems = ref(0)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
const primaryActionLabel = ref("Open")
let searchTimer: number | undefined
let requestSequence = 0
let activeRequest: AbortController | null = null

const isMine = computed(() => route.name === "work-quests")
const title = computed(() => isMine.value ? "My quests" : "Work")
const totalLabel = computed(() => totalItems.value > 0 ? `${totalItems.value} results` : "No results")

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
      preset: isMine.value ? "MY_ACTIVE" : undefined,
      sort: sort.value,
      page: page.value,
      size: 12,
      scheduledOnly: scheduledOnly.value,
      signal: activeRequest.signal
    })
    if (requestId !== requestSequence) return
    items.value = reset ? response.items : [...items.value, ...response.items]
    primaryActionLabel.value = response.presentation.primaryActionLabel
    totalItems.value = response.totalItems
    page.value = response.page
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

const openVision = () => {
  void router.push(buildVisionRoute({
    prompt: "find work for me",
    context: "Work",
    source: "work.discovery",
    returnTo: "/work"
  }))
}

watch([query, sort, scheduledOnly], () => {
  if (searchTimer !== undefined) window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => void load(), 250)
})

onMounted(() => void load())
</script>

<template>
  <section class="work-discovery">
    <header class="work-discovery__header">
      <div>
        <p class="work-discovery__eyebrow">{{ isMine ? "Work / Mine" : "Work" }}</p>
        <h1>{{ title }}</h1>
      </div>
      <button type="button" class="work-discovery__vision" @click="openVision">Ask Vision</button>
    </header>

    <div class="work-discovery__controls">
      <label class="work-discovery__search">
        <span class="sr-only">Search work</span>
        <input v-model="query" type="search" placeholder="Search work" @keyup.enter="load()">
      </label>
      <select v-model="sort" aria-label="Sort work">
        <option value="recommended">Recommended</option>
        <option value="newest">Newest</option>
        <option value="soonest">Soonest</option>
        <option value="highest_reward">Highest reward</option>
      </select>
      <label class="work-discovery__toggle">
        <input v-model="scheduledOnly" type="checkbox">
        <span>Scheduled</span>
      </label>
      <span class="work-discovery__count">{{ totalLabel }}</span>
    </div>

    <div v-if="isLoading" class="work-discovery__status" role="status">Loading work.</div>
    <div v-else-if="error" class="work-discovery__status work-discovery__status--error" role="alert">{{ error }}</div>
    <div v-else-if="items.length === 0" class="work-discovery__status">Nothing matches. Ask Vision to broaden the search.</div>

    <div v-else class="work-discovery__list">
      <article v-for="quest in items" :key="quest.id" class="work-discovery__row">
        <div class="work-discovery__row-main">
          <RouterLink :to="resolveSurfaceDetailRoute('work-quests', quest.id) ?? `/vision/quests/${quest.id}`" class="work-discovery__title">
            {{ quest.title }}
          </RouterLink>
          <span class="work-discovery__meta">{{ quest.presentation.statusLabel }}</span>
        </div>
        <div class="work-discovery__row-facts">
          <span>{{ quest.awardAmount }} €</span>
          <span>{{ formatDate(quest.scheduledAt) }}</span>
          <span>{{ locationLabel(quest) }}</span>
        </div>
        <RouterLink :to="resolveSurfaceDetailRoute('work-quests', quest.id) ?? `/vision/quests/${quest.id}`" class="work-discovery__open">
          {{ primaryActionLabel }}
        </RouterLink>
      </article>
    </div>

    <button
      v-if="!isLoading && items.length < totalItems"
      type="button"
      class="work-discovery__load-more"
      :disabled="isLoadingMore"
      @click="loadMore"
    >
      {{ isLoadingMore ? "Loading" : "Load more" }}
    </button>
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

.work-discovery__header {
  justify-content: space-between;
}

.work-discovery__eyebrow {
  margin: 0 0 0.25rem;
  color: rgba(23, 34, 26, 0.54);
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
.work-discovery__open,
.work-discovery__load-more {
  min-height: 2.4rem;
  border: 1px solid rgba(23, 34, 26, 0.12);
  border-radius: 999px;
  padding: 0.5rem 0.85rem;
  font-size: 0.8rem;
  font-weight: 650;
}

.work-discovery__vision {
  border-color: #17221a;
  background: #17221a;
  color: #f8f8f4;
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
  min-height: 2.5rem;
  border: 1px solid rgba(23, 34, 26, 0.12);
  border-radius: 0.8rem;
  background: rgba(255, 255, 255, 0.72);
  padding: 0.55rem 0.75rem;
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
  color: rgba(23, 34, 26, 0.72);
  font-size: 0.8rem;
}

.work-discovery__count {
  margin-left: auto;
  color: rgba(23, 34, 26, 0.52);
  font-size: 0.78rem;
}

.work-discovery__list {
  overflow: hidden;
  border-top: 1px solid rgba(23, 34, 26, 0.1);
}

.work-discovery__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  border-bottom: 1px solid rgba(23, 34, 26, 0.08);
  padding: 0.9rem 0.2rem;
}

.work-discovery__row-main {
  display: grid;
  gap: 0.25rem;
  min-width: 0;
}

.work-discovery__title {
  overflow: hidden;
  color: #17221a;
  font-size: 0.98rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-discovery__meta,
.work-discovery__row-facts {
  color: rgba(23, 34, 26, 0.56);
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
  color: #17221a;
}

.work-discovery__status {
  border-radius: 0.8rem;
  background: rgba(255, 255, 255, 0.66);
  padding: 1rem;
  color: rgba(23, 34, 26, 0.58);
}

.work-discovery__status--error {
  color: #7c2a1d;
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
</style>
