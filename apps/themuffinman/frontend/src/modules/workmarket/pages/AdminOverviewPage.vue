<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiInfoGrid from "../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../components/ui/UiStatCard.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiAdminTableShell from "../../../components/ui/UiAdminTableShell.vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import {workmarketApi, type DashboardSummary, type LocationDebugStatus} from "../api/workmarketApi.ts"

const summary = ref<DashboardSummary | null>(null)
const locationStatus = ref<LocationDebugStatus | null>(null)
const isLoading = ref(false)
const error = ref("")

const locationCacheHitRate = computed(() => {
  if (!locationStatus.value) {
    return "0%"
  }

  const totalRequests = locationStatus.value.lookupRequests + locationStatus.value.reverseLookupRequests
  const totalHits = locationStatus.value.lookupCacheHits + locationStatus.value.reverseLookupCacheHits
  if (totalRequests <= 0) {
    return "0%"
  }

  return `${Math.round((totalHits / totalRequests) * 100)}%`
})

const formattedDatabaseSize = computed(() => {
  if (!locationStatus.value) {
    return "0 MB"
  }

  const megabytes = locationStatus.value.databaseSizeBytes / (1024 * 1024)
  return `${megabytes.toFixed(megabytes >= 100 ? 0 : 1)} MB`
})

const providerRuntimeCalls = computed(() => {
  if (!locationStatus.value) {
    return 0
  }

  return locationStatus.value.providerLookupCalls + locationStatus.value.providerReverseLookupCalls
})

const topTables = computed(() => {
  if (!locationStatus.value) {
    return []
  }

  return [...locationStatus.value.tableStatuses]
    .sort((left, right) => right.rowCount - left.rowCount)
    .slice(0, 10)
})

const loadOverview = async () => {
  isLoading.value = true
  error.value = ""

  try {
    const [dashboardSummary, adminLocationStatus] = await Promise.all([
      workmarketApi.getDashboardSummary(),
      workmarketApi.getAdminLocationStatus()
    ])
    summary.value = dashboardSummary
    locationStatus.value = adminLocationStatus
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load admin overview.")
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  void loadOverview()
})
</script>

<template>
  <UiDashboardPage admin>
    <AdminShellHeader title="Overview" subtitle="" />

    <div v-if="isLoading" class="empty-state">
      Loading admin overview...
    </div>

    <div v-else-if="error" class="alert alert--error">
      {{ error }}
    </div>

    <div v-else-if="summary && locationStatus" class="admin-overview">
      <UiSurfaceSection
        title="Platform snapshot"
        subtitle="People, jobs, and system usage in one place."
        plain
      >
        <template #actions>
          <div class="admin-overview__actions">
            <button class="button button--secondary" type="button" @click="loadOverview">Refresh</button>
          </div>
        </template>

        <UiInfoGrid :columns="4">
          <UiStatCard class="admin-overview-card admin-overview-card--accent" label="Users" :value="summary.totalUserCount" :hint="`${summary.adminUserCount} admins`" />
          <UiStatCard class="admin-overview-card admin-overview-card--success" label="Visible quests" :value="summary.questCount" :hint="`${summary.openQuestCount} open now`" />
          <UiStatCard class="admin-overview-card admin-overview-card--warning" label="Pending applications" :value="summary.pendingWorkApplicationsCount" :hint="`${summary.waitingConfirmationQuestCount} waiting confirmation`" />
          <UiStatCard class="admin-overview-card admin-overview-card--neutral" label="Unread news" :value="summary.unreadNewsCount" :hint="`${summary.activeWorkCount} active work items`" />
        </UiInfoGrid>
      </UiSurfaceSection>

      <div class="admin-overview__split">
        <UiSurfaceSection
          title="Quest pipeline"
          subtitle="Current flow across open, assigned, and active work."
          plain
        >
          <UiInfoGrid :columns="2">
            <UiStatCard class="admin-overview-card admin-overview-card--accent" label="Open" :value="summary.openQuestCount" />
            <UiStatCard class="admin-overview-card admin-overview-card--warning" label="Assigned" :value="summary.assignedQuestCount" />
            <UiStatCard class="admin-overview-card admin-overview-card--success" label="My active jobs" :value="summary.activeMyQuestsCount" />
            <UiStatCard class="admin-overview-card admin-overview-card--neutral" label="My completed jobs" :value="summary.completedMyQuestsCount" />
          </UiInfoGrid>
        </UiSurfaceSection>

        <UiSurfaceSection
          title="Location and storage"
          subtitle="Provider usage, cache behavior, and database footprint."
          plain
        >
          <UiInfoGrid :columns="2">
            <UiStatCard class="admin-overview-card admin-overview-card--accent" label="Provider calls this month" :value="locationStatus.currentMonthProviderRequests" :hint="`${locationStatus.currentMonthProviderLookupRequests} search, ${locationStatus.currentMonthProviderReverseLookupRequests} reverse`" />
            <UiStatCard class="admin-overview-card admin-overview-card--success" label="Cache hit rate" :value="locationCacheHitRate" :hint="`${locationStatus.lookupCacheHits + locationStatus.reverseLookupCacheHits} hits`" />
            <UiStatCard class="admin-overview-card admin-overview-card--neutral" label="Database size" :value="formattedDatabaseSize" :hint="`${locationStatus.tableStatuses.length} tables`" />
            <UiStatCard class="admin-overview-card admin-overview-card--warning" label="Saved coordinates" :value="locationStatus.usersWithCoordinates + locationStatus.questsWithCoordinates" :hint="`${locationStatus.usersWithCoordinates} users, ${locationStatus.questsWithCoordinates} quests`" />
          </UiInfoGrid>
        </UiSurfaceSection>
      </div>

      <div class="admin-overview__split">
        <UiSurfaceSection
          title="Largest tables"
          subtitle="Top database tables by row count."
          plain
        >
          <UiAdminTableShell compact :has-previous="false" :has-next="false" :show-bottom-pagination="false">
            <table class="admin-table admin-table--compact">
              <thead>
                <tr>
                  <th>Table</th>
                  <th>Rows</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="table in topTables" :key="table.tableName">
                  <td>{{ table.tableName }}</td>
                  <td>{{ table.rowCount }}</td>
                </tr>
              </tbody>
            </table>
          </UiAdminTableShell>

          <div class="admin-overview__runtime-note">
            Runtime provider calls: {{ providerRuntimeCalls }}. Runtime counters reset on backend restart.
          </div>
        </UiSurfaceSection>

        <UiSurfaceSection
          title="Coverage"
          subtitle="How much location data is already attached to profiles and quests."
          plain
        >
          <UiInfoGrid :columns="2">
            <UiStatCard class="admin-overview-card admin-overview-card--neutral" label="Users with coordinates" :value="locationStatus.usersWithCoordinates" :hint="`${locationStatus.usersWithProviderMetadata} with provider metadata`" />
            <UiStatCard class="admin-overview-card admin-overview-card--neutral" label="Quests with coordinates" :value="locationStatus.questsWithCoordinates" :hint="`${locationStatus.questsWithProviderMetadata} with provider metadata`" />
          </UiInfoGrid>
        </UiSurfaceSection>
      </div>
    </div>
  </UiDashboardPage>
</template>
