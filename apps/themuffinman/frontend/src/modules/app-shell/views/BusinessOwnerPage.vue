<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import {setActiveBusinessProfileId, userShellApi} from "../api/userShellApi.ts"
import ContextSwitcher from "../components/ContextSwitcher.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import AppStatus from "../components/AppStatus.vue"
import BusinessProfileView from "./BusinessProfileView.vue"
import BusinessOwnerOverviewView from "./BusinessOwnerOverviewView.vue"
import BusinessOfferingsView from "./BusinessOfferingsView.vue"
import BusinessBookingsView from "./BusinessBookingsView.vue"
import BusinessOwnerCalendarView from "./BusinessOwnerCalendarView.vue"
import type {BusinessOwnerDashboardDTO} from "../../../contracts/index.ts"

const route = useRoute()
const router = useRouter()
const profiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const error = ref("")
const dashboard = ref<BusinessOwnerDashboardDTO | null>(null)
const businessTabs = computed(() => [
  {id: "overview", label: "My businesses", route: "/business", backendScope: "business.owner", emptyState: "Create your first business."},
  ...profiles.value.map((profile) => ({id: `business-${profile.id}`, label: profile.businessName, route: `/business/profile?businessId=${profile.id}`, backendScope: "business.owner.workspace", emptyState: "Open this business workspace."}))
])
const activeBusinessTab = computed(() => selectedBusinessId.value ? `business-${selectedBusinessId.value}` : "overview")
const tabs = computed(() => [
  {id: "profile", label: "Overview", route: `/business/profile?businessId=${selectedBusinessId.value ?? ''}`, backendScope: "business.owner.overview", emptyState: "Create your business profile."},
  {id: "calendar", label: "Calendar", route: `/business/calendar?businessId=${selectedBusinessId.value ?? ''}`, backendScope: "business.owner.calendar", emptyState: "No availability configured."},
  {id: "bookings", label: "Bookings", badge: dashboard.value?.pendingConfirmationCount || undefined, route: `/business/bookings?businessId=${selectedBusinessId.value ?? ''}`, backendScope: "business.owner.bookings", emptyState: "No bookings yet."},
  {id: "services", label: "Services", route: `/business/offerings?businessId=${selectedBusinessId.value ?? ''}`, backendScope: "business.owner.services", emptyState: "Add your first service."},
  {id: "settings", label: "Settings", route: `/business/settings?businessId=${selectedBusinessId.value ?? ''}`, backendScope: "business.owner.settings", emptyState: "Configure your business."}
])
const activeTab = computed(() => route.path.includes("settings") ? "settings" : route.path.includes("calendar") ? "calendar" : route.path.includes("bookings") ? "bookings" : route.path.includes("offerings") || route.path.includes("service-setup") ? "services" : "profile")
const selectedBusinessId = computed(() => route.query.businessId ? Number(route.query.businessId) : (profiles.value[0]?.id ?? null))
const options = computed(() => profiles.value.map(profile => ({id: profile.id, label: profile.businessName, description: `Business workspace · ${profile.slug}`})))
const view = computed(() => activeTab.value === "calendar" ? BusinessOwnerCalendarView : activeTab.value === "bookings" ? BusinessBookingsView : activeTab.value === "services" ? BusinessOfferingsView : activeTab.value === "settings" ? BusinessProfileView : BusinessOwnerOverviewView)

const switchBusiness = async (value: string | number | null) => {
  const id = Number(value)
  if (!Number.isFinite(id)) return
  setActiveBusinessProfileId(id)
  await router.replace({query: {...route.query, businessId: String(id)}})
}
const loadDashboard = async () => { if (selectedBusinessId.value === null) return; try { dashboard.value = await userShellApi.getBusinessDashboard(selectedBusinessId.value) } catch { dashboard.value = null } }
watch(selectedBusinessId, (id) => { if (id !== null) { setActiveBusinessProfileId(id); void loadDashboard() } })
onMounted(async () => { try { profiles.value = await userShellApi.getMyBusinessProfiles(); if (selectedBusinessId.value !== null) { setActiveBusinessProfileId(selectedBusinessId.value); await loadDashboard() } } catch { error.value = "Could not load your business workspaces." } })
</script>

<template>
  <section class="business-owner-page" aria-label="Business owner workspace" data-owner-tabs="overview calendar bookings services" data-context-boundary="active-business" data-mental-model="business-overview-tabs-detail" data-navigation-model="stable-tabs" :data-business-id="selectedBusinessId ?? undefined">
    <ModuleTabs :tabs="businessTabs" :active-id="activeBusinessTab" />
    <div class="business-owner-page__context">
      <ContextSwitcher :model-value="selectedBusinessId" :options="options" label="Active business" empty-label="Create a business" @update:model-value="switchBusiness" />
    </div>
    <ModuleTabs :tabs="tabs" :active-id="activeTab" />
    <AppStatus v-if="error" :message="error" tone="error" />
    <component :is="view" :key="`${activeTab}:${selectedBusinessId ?? 'none'}`" v-bind="selectedBusinessId ? {businessId: selectedBusinessId} : {}" />
  </section>
</template>

<style scoped>
.business-owner-page{display:grid;gap:var(--space-4);min-width:0}.business-owner-page__context{display:flex;justify-content:flex-end}@media(max-width:700px){.business-owner-page__context{justify-content:stretch}}
</style>
