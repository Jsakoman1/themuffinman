<script setup lang="ts">
import DashboardOverviewColumn from "./DashboardOverviewColumn.vue"
import DashboardWorkPlanner from "./DashboardWorkPlanner.vue"
import {createDashboardOverviewState} from "../../composables/dashboard/createDashboardOverviewState.ts"
import type {DashboardOverviewFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardOverviewFacade
}>()
const {postedBuckets, workBuckets, openQuest} = createDashboardOverviewState(props.dashboard)
</script>

<template>
  <section class="dashboard-overview">
    <div class="dashboard-overview__layout">
      <DashboardOverviewColumn
        eyebrow="You posted"
        title="Managed work"
        action-label="New quest"
        action-tone="success"
        :buckets="postedBuckets"
        @action="props.dashboard.openCreateJobDialog()"
        @open="openQuest"
      />
      <DashboardWorkPlanner :dashboard="dashboard" />
      <DashboardOverviewColumn
        eyebrow="You accepted"
        title="Doing work"
        action-label="Find work"
        action-tone="accent"
        :buckets="workBuckets"
        @action="props.dashboard.openFindWorkDialog()"
        @open="openQuest"
      />
    </div>
  </section>
</template>
