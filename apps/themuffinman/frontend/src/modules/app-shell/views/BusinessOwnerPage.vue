<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
import ContextSwitcher from "../components/ContextSwitcher.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import AppStatus from "../components/AppStatus.vue"
import BusinessProfileView from "./BusinessProfileView.vue"
import BusinessOverviewView from "./BusinessOverviewView.vue"
import BusinessOfferingsView from "./BusinessOfferingsView.vue"
import BusinessBookingsView from "./BusinessBookingsView.vue"
import BusinessAvailabilityView from "./BusinessAvailabilityView.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

const route = useRoute()
const router = useRouter()
const surface = getAppSurfaceConfig("business")
const profiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const error = ref("")
const tabs = computed(() => [
  {id: "profile", label: "Overview", route: "/business/profile", backendScope: "business.owner.overview", emptyState: "Create your business profile."},
  {id: "calendar", label: "Calendar", route: "/business/calendar", backendScope: "business.owner.calendar", emptyState: "No availability configured."},
  {id: "bookings", label: "Bookings", route: "/business/bookings", backendScope: "business.owner.bookings", emptyState: "No bookings yet."},
  {id: "services", label: "Services", route: "/business/offerings", backendScope: "business.owner.services", emptyState: "Add your first service."},
  {id: "settings", label: "Settings", route: "/business/settings", backendScope: "business.owner.settings", emptyState: "Configure your business."}
])
const activeTab = computed(() => route.path.includes("settings") ? "settings" : route.path.includes("calendar") ? "calendar" : route.path.includes("bookings") ? "bookings" : route.path.includes("offerings") ? "services" : "profile")
const selectedBusinessId = computed(() => route.query.businessId ? Number(route.query.businessId) : (profiles.value[0]?.id ?? null))
const options = computed(() => profiles.value.map(profile => ({id: profile.id, label: profile.businessName, description: `Business workspace · ${profile.slug}`})))
const view = computed(() => activeTab.value === "calendar" ? BusinessAvailabilityView : activeTab.value === "bookings" ? BusinessBookingsView : activeTab.value === "services" ? BusinessOfferingsView : activeTab.value === "settings" ? BusinessProfileView : BusinessOverviewView)

const switchBusiness = async (value: string | number | null) => {
  const id = Number(value)
  if (!Number.isFinite(id)) return
  setActiveBusinessProfileId(id)
  await router.replace({query: {...route.query, businessId: String(id)}})
}
watch(selectedBusinessId, (id) => { if (id !== null) setActiveBusinessProfileId(id) })
onMounted(async () => { try { profiles.value = await userShellApi.getMyBusinessProfiles(); if (selectedBusinessId.value !== null) setActiveBusinessProfileId(selectedBusinessId.value) } catch { error.value = "Could not load your business workspaces." } })
</script>

<template>
  <section class="business-owner-page" aria-label="Business owner workspace" data-owner-tabs="overview calendar bookings services" data-context-boundary="active-business" data-mental-model="business-overview-tabs-detail" data-navigation-model="stable-tabs" :data-business-id="selectedBusinessId ?? undefined">
    <SurfaceHeader :config="surface" title="Manage your business" description="Each business has its own private workspace and schedule."><template #utility><ContextSwitcher :model-value="selectedBusinessId" :options="options" label="Active business" empty-label="Create a business" @update:model-value="switchBusiness" /></template></SurfaceHeader>
    <ModuleTabs :tabs="tabs" :active-id="activeTab" />
    <AppStatus v-if="error" :message="error" tone="error" />
    <component :is="view" :key="`${activeTab}:${selectedBusinessId ?? 'none'}`" />
  </section>
</template>

<style scoped>
.business-owner-page{display:grid;gap:var(--space-4);min-width:0}
</style>
