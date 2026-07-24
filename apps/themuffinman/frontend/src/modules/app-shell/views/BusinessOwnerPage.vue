<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
import ContextSwitcher from "../components/ContextSwitcher.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import AppStatus from "../components/AppStatus.vue"
import BusinessProfileView from "./BusinessProfileView.vue"
import BusinessOfferingsView from "./BusinessOfferingsView.vue"
import BusinessBookingsView from "./BusinessBookingsView.vue"
import BusinessAvailabilityView from "./BusinessAvailabilityView.vue"

const route = useRoute()
const router = useRouter()
const profiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const error = ref("")
const tabs = computed(() => [
  {id: "profile", label: "Overview", route: "/business/profile", backendScope: "business.owner.overview", emptyState: "Create your business profile."},
  {id: "calendar", label: "Calendar", route: "/business/calendar", backendScope: "business.owner.calendar", emptyState: "No availability configured."},
  {id: "bookings", label: "Bookings", route: "/business/bookings", backendScope: "business.owner.bookings", emptyState: "No bookings yet."},
  {id: "services", label: "Services", route: "/business/offerings", backendScope: "business.owner.services", emptyState: "Add your first service."}
])
const activeTab = computed(() => route.path.includes("calendar") ? "calendar" : route.path.includes("bookings") ? "bookings" : route.path.includes("offerings") ? "services" : "profile")
const selectedBusinessId = computed(() => route.query.businessId ? Number(route.query.businessId) : (profiles.value[0]?.id ?? null))
const options = computed(() => profiles.value.map(profile => ({id: profile.id, label: profile.businessName, description: `Business workspace · ${profile.slug}`})))
const view = computed(() => activeTab.value === "calendar" ? BusinessAvailabilityView : activeTab.value === "bookings" ? BusinessBookingsView : activeTab.value === "services" ? BusinessOfferingsView : BusinessProfileView)

const switchBusiness = async (value: string | number | null) => {
  const id = Number(value)
  if (!Number.isFinite(id)) return
  setActiveBusinessProfileId(id)
  await router.replace({query: {...route.query, businessId: String(id)}})
}
onMounted(async () => { try { profiles.value = await userShellApi.getMyBusinessProfiles() } catch { error.value = "Could not load your business workspaces." } })
</script>

<template>
  <section class="business-owner-page" aria-label="Business owner workspace" data-owner-tabs="overview calendar bookings services" data-context-boundary="active-business" data-navigation-model="stable-tabs">
    <header class="business-owner-page__header"><div><p class="business-owner-page__eyebrow">Business / Manage</p><h1>Manage your business</h1><p>Each business has its own private workspace and schedule.</p></div><ContextSwitcher :model-value="selectedBusinessId" :options="options" label="Active business" empty-label="Create a business" @update:model-value="switchBusiness" /></header>
    <ModuleTabs :tabs="tabs" :active-id="activeTab" />
    <AppStatus v-if="error" :message="error" tone="error" />
    <component :is="view" />
  </section>
</template>

<style scoped>
.business-owner-page{display:grid;gap:var(--space-4);min-width:0}.business-owner-page__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-4)}.business-owner-page__header h1,.business-owner-page__header p{margin:0}.business-owner-page__header h1{font-size:clamp(1.6rem,2.5vw,2.35rem);letter-spacing:-.03em}.business-owner-page__header>div>p:last-child{margin-top:var(--space-2);color:var(--text-muted)}.business-owner-page__eyebrow{margin-bottom:var(--space-1)!important;color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-transform:uppercase;letter-spacing:.06em}@media(max-width:700px){.business-owner-page__header{display:grid;gap:var(--space-3)}}
</style>
