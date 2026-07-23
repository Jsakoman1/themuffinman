<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import type {BusinessProfileResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppSearchField from "../components/AppSearchField.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {handleCollectionKeyboard, useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import {currentUser} from "../../identity/auth.ts"

const route = useRoute()
const router = useRouter()
const items = ref<BusinessProfileResponseDTO[]>([])
const query = ref(typeof route.query.q === "string" ? route.query.q : "")
const isLoading = ref(true)
const error = ref("")
const {state: viewState} = useSurfaceViewState("business-discovery", computed(() => currentUser.value?.id), computed(() => route.fullPath))
const selectedBusiness = computed(() => items.value.find(item => item.id === viewState.value.selectedId) ?? null)

const load = async () => {
  isLoading.value = true
  error.value = ""
  try {
    items.value = (await userShellApi.getBusinessDirectory(query.value.trim())).items
    if (!items.value.some(item => item.id === viewState.value.selectedId)) viewState.value.selectedId = null
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

watch(() => route.query.q, (value) => {
  const nextQuery = typeof value === "string" ? value : ""
  if (query.value === nextQuery) return
  query.value = nextQuery
  void load()
})

const handleKeyboard = (event: KeyboardEvent) => handleCollectionKeyboard(event, items.value.map(item => item.id), viewState.value, {
  open: (id) => { viewState.value.selectedId = id },
  clear: () => { viewState.value.selectedId = null; viewState.value.previewId = null }
})
onMounted(() => { window.addEventListener("keydown", handleKeyboard); void load() })
onBeforeUnmount(() => window.removeEventListener("keydown", handleKeyboard))
</script>

<template>
  <section class="business-discovery">
    <header class="business-discovery__header">
      <div><p class="business-discovery__eyebrow">Business / Discover</p><h1>Find a business</h1></div>
    </header>

    <CollectionToolbar title="Public businesses" :count="items.length" :busy="isLoading">
      <template #filters>
        <AppSearchField v-model="query" label="Search businesses" placeholder="Search businesses" :busy="isLoading" @submit="submitSearch" />
      </template>
    </CollectionToolbar>

    <AppStatus v-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-else-if="isLoading" message="Loading businesses." />
    <AppStatus v-else-if="items.length === 0" message="No public businesses match this search." />
    <div v-else class="business-discovery__workspace">
      <div class="business-discovery__list" aria-label="Business results"><SurfaceRow
        v-for="business in items"
        :key="business.id"
        :selected="viewState.selectedId === business.id"
        :row="{id: String(business.id), title: business.businessName, description: business.headline || business.description || 'Public business profile', badge: business.bookingEnabled ? 'Bookings available' : 'Profile only', meta: business.slug}"
        @click="viewState.selectedId = business.id"
      /></div><aside class="business-context" aria-label="Business context"><template v-if="selectedBusiness"><p class="business-discovery__eyebrow">Selected business</p><h2>{{ selectedBusiness.businessName }}</h2><p>{{ selectedBusiness.headline || selectedBusiness.description || 'Public business profile' }}</p><dl><div><dt>Profile</dt><dd>{{ selectedBusiness.slug }}</dd></div><div><dt>Bookings</dt><dd>{{ selectedBusiness.bookingEnabled ? 'Available' : 'Not enabled' }}</dd></div><div v-if="selectedBusiness.publicAddressLabel"><dt>Area</dt><dd>{{ selectedBusiness.publicAddressLabel }}</dd></div></dl><RouterLink class="business-context__link" :to="{path: `/business/public/${selectedBusiness.slug}`, query: {returnTo: route.fullPath}}">Open full detail</RouterLink></template><template v-else><p class="business-discovery__eyebrow">Business context</p><h2>Select a business</h2><p>Choose a result to inspect its profile without leaving this collection.</p></template></aside>
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
.business-discovery__workspace { display: grid; grid-template-columns: minmax(0, 1fr) minmax(18rem, 24rem); overflow: hidden; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.business-discovery__list { min-width: 0; }
.business-discovery__list :deep(.surface-row:last-child) { border-bottom: 0; }
.business-context { border-left: 1px solid var(--border-subtle); padding: var(--space-3); background: var(--surface-raised); }
.business-context p { color: var(--text-muted); }
.business-context dl { display: grid; gap: var(--space-2); margin: var(--space-3) 0; }
.business-context dl div { display: flex; justify-content: space-between; gap: var(--space-2); border-bottom: 1px solid var(--border-subtle); padding-bottom: var(--space-1); }
.business-context dt { color: var(--text-soft); font-size: var(--text-size-meta); }
.business-context dd { margin: 0; text-align: right; }
.business-context__link { display: inline-flex; margin-top: var(--space-2); font-weight: var(--text-weight-semibold); }
@media (max-width: 860px) { .business-discovery__workspace { grid-template-columns: 1fr; } .business-context { border-left: 0; border-top: 1px solid var(--border-subtle); } }
@media (max-width: 640px) { .business-discovery__header { align-items: start; flex-direction: column; } .business-discovery__search { width: 100%; } .business-discovery__search input { min-width: 0; width: 100%; } }
</style>
