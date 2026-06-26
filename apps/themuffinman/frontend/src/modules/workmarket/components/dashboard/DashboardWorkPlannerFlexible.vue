<script setup lang="ts">
import UiEventPill from "../../../../components/ui/UiEventPill.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {createDashboardWorkPlannerState} from "../../composables/dashboard/createDashboardWorkPlannerState.ts"
import type {DashboardWorkPlannerFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardWorkPlannerFacade
}>()

const {
  flexibleItems,
  openItem
} = createDashboardWorkPlannerState(props.dashboard)
</script>

<template>
  <UiSurfaceSection v-if="flexibleItems.length" class="dashboard-calendar-panel" plain title="Flexible">
    <div class="dashboard-calendar__flexible-list">
      <button
        v-for="item in flexibleItems"
        :key="item.id"
        class="button-reset"
        type="button"
        @click="openItem(item)"
      >
        <UiEventPill
          :time="item.timeLabel"
          :title="item.title"
          :tone="item.tone"
          month
        />
      </button>
    </div>
  </UiSurfaceSection>
</template>
