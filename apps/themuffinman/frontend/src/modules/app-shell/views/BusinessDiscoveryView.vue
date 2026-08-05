<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
// Discovery cards preserve native route and keyboard interaction semantics.
import {useRoute, useRouter} from "vue-router"
import type {BusinessProfileResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppSearchField from "../components/AppSearchField.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import FriendlyCollectionHeader from "../components/FriendlyCollectionHeader.vue"
import BusinessDiscoveryCard from "../components/BusinessDiscoveryCard.vue"

const route = useRoute()
const router = useRouter()
const rawItems = ref<BusinessProfileResponseDTO[]>([])
const intentFilter = ref<"ALL" | "BOOK_NOW" | "WITH_AREA">("ALL")
const matchesIntent = (business: BusinessProfileResponseDTO) => intentFilter.value === "ALL" || (intentFilter.value === "BOOK_NOW" && business.bookingEnabled) || (intentFilter.value === "WITH_AREA" && Boolean(business.publicAddressLabel))
const items = computed(() => rawItems.value.filter(matchesIntent))
const isFavoritesView = computed(() => route.path === "/business/favorites")
const visibleItems = computed(() => items.value)
const query = ref(typeof route.query.q === "string" ? route.query.q : "")
const isLoading = ref(true)
const error = ref("")
const businessTabs = computed(() => getModuleTabs("services")?.tabs ?? [])
const favoriteIds = ref<Set<number>>(new Set())

const load = async () => {
  isLoading.value = true
  error.value = ""
  try {
    rawItems.value = (await (isFavoritesView.value ? userShellApi.getBusinessFavoriteDirectory(query.value.trim()) : userShellApi.getBusinessDirectory(query.value.trim()))).items
    favoriteIds.value = new Set((await userShellApi.getBusinessFavorites()).map(item => item.businessProfileId))
  } catch {
    error.value = "Could not load businesses."
  } finally {
    isLoading.value = false
  }
}

const submitSearch = async () => {
  const nextQuery = query.value.trim() || undefined
  if (route.query.q !== nextQuery) await router.replace({query: {...route.query, q: nextQuery}})
  await load()
}
const chooseIntent = (value: typeof intentFilter.value) => { intentFilter.value = intentFilter.value === value ? "ALL" : value }
const clearRefinement = () => { query.value = ""; intentFilter.value = "ALL"; void submitSearch() }

watch(() => route.query.q, (value) => {
  const nextQuery = typeof value === "string" ? value : ""
  if (query.value === nextQuery) return
  query.value = nextQuery
  void load()
})

onMounted(() => void load())
</script>

<template>
  <section class="business-discovery" data-collection-rhythm="oriented" data-business-page-model="discover-book">
    <ModuleTabs :tabs="businessTabs" :active-id="isFavoritesView ? 'favorites' : 'find'" />
    <FriendlyCollectionHeader eyebrow="Services" :title="isFavoritesView ? 'Favorite businesses' : 'Find a service'" :description="isFavoritesView ? 'Businesses you saved, ready to revisit.' : 'Search for a business, then open its page to see services, prices, availability and contact details.'" />

    <CollectionToolbar :title="isFavoritesView ? 'Favorite businesses' : 'Public businesses'" :count="visibleItems.length" :busy="isLoading" :filter-summary="visibleItems.length === 1 ? '1 match' : `${visibleItems.length} matches`">
      <template #filters>
        <AppSearchField v-model="query" label="What service or business do you need?" placeholder="e.g. haircut, plumber, yoga" :busy="isLoading" submit-label="Search" @submit="submitSearch" />
        <div v-if="!isFavoritesView" class="business-discovery__quick-filters" aria-label="Quick filters">
          <button type="button" :class="{active: intentFilter === 'BOOK_NOW'}" :aria-pressed="intentFilter === 'BOOK_NOW'" @click="chooseIntent('BOOK_NOW')">Book online</button>
          <button type="button" :class="{active: intentFilter === 'WITH_AREA'}" :aria-pressed="intentFilter === 'WITH_AREA'" @click="chooseIntent('WITH_AREA')">Has an area</button>
          <button v-if="query || intentFilter !== 'ALL'" type="button" class="business-discovery__clear" @click="clearRefinement">Clear</button>
        </div>
      </template>
    </CollectionToolbar>

    <AppStatus v-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-else-if="isLoading" message="Loading businesses." />
    <AppStatus v-else-if="visibleItems.length === 0" :message="isFavoritesView ? 'You have no favorite businesses yet.' : 'No public businesses match this search.'" />
    <div v-else class="business-discovery__workspace">
      <div class="business-discovery__list" aria-label="Service providers"><BusinessDiscoveryCard
        v-for="business in visibleItems"
        :key="business.id"
        :business="business"
        :saved="favoriteIds.has(business.id)"
        :to="{path: `/business/public/${business.slug}`, query: {returnTo: route.fullPath}}"
      /></div>
    </div>
  </section>
</template>

<style scoped>
.business-discovery { display: grid; gap: var(--space-3); max-width: none; }
.business-discovery__header { display: flex; align-items: end; justify-content: space-between; gap: var(--space-3); }
.business-discovery__eyebrow { margin: 0 0 var(--space-1); color: var(--text-soft); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: .08em; text-transform: uppercase; }
h1 { margin: 0; color: var(--text); font-size: var(--text-size-page-title); letter-spacing: var(--tracking-tight); }
.business-discovery__my-business, .business-discovery__search button { border: 1px solid var(--border-subtle); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); color: var(--text-muted); font: inherit; font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.business-discovery__search { display: flex; align-items: center; gap: var(--space-1); }
.business-discovery__search input { min-width: min(24rem, 58vw); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); background: var(--control-bg); color: var(--text); font: inherit; }
.business-discovery__search button { background: var(--accent); color: var(--canvas); }
.business-discovery__search button:disabled { cursor: wait; opacity: .65; }
.business-discovery__workspace { min-width: 0; }
.business-discovery__list { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 22rem), 1fr)); gap: var(--space-3); min-width: 0; }
@media (max-width: 640px) { .business-discovery__header { align-items: start; flex-direction: column; } .business-discovery__search { width: 100%; } .business-discovery__search input { min-width: 0; width: 100%; } }
.business-discovery__quick-filters{display:flex;align-items:center;gap:var(--space-1);flex-wrap:wrap}.business-discovery__quick-filters button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:999px;padding:var(--space-1) var(--space-3);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.business-discovery__quick-filters button.active{border-color:var(--accent);background:var(--accent-muted);color:var(--accent-ink,var(--text))}.business-discovery__quick-filters .business-discovery__clear{border-color:transparent;background:transparent;color:var(--text-muted);text-decoration:underline}
</style>
