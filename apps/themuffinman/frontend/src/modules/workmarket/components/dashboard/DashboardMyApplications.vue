<script setup lang="ts">
import type {QuestApplication} from "../../api/workmarketApi.ts"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"
import type {DashboardQuestListFacade} from "../../composables/dashboard/dashboardFacades.ts"
import DashboardSummaryListButton from "../shared/DashboardSummaryListButton.vue"
import DashboardSummaryListSection from "../shared/DashboardSummaryListSection.vue"

const props = withDefaults(defineProps<{
  dashboard: DashboardQuestListFacade
  title?: string
  subtitle?: string
  emptyMessage?: string
  applications?: QuestApplication[]
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "My applications",
  subtitle: "",
  emptyMessage: "No applications yet.",
  applications: undefined,
  showHeader: true,
  boxed: true,
})

const {applications} = createDashboardQuestListState(props.dashboard, {applications: props.applications})
</script>

<template>
  <DashboardSummaryListSection
    :title="props.title"
    :subtitle="props.subtitle"
    :empty-message="props.emptyMessage"
    :has-items="applications.length > 0"
    :show-header="props.showHeader"
    :boxed="props.boxed"
  >
    <DashboardSummaryListButton
      v-for="application in applications"
      :key="application.id"
      primary-label="Creator"
      :primary-value="dashboard.questCreatorUsernameForQuest(application.questId)"
      secondary-label="Price"
      :secondary-value="application.proposedPrice"
      secondary-icon="$"
      money-tone="income"
      :title="application.questTitle"
      :description="application.questDescription"
      :status-surface-class="application.presentation.statusSurfaceClass"
      :pulse="dashboard.successPulseTarget === `application-${application.id}`"
      @click="dashboard.openApplicationDialog(application.id)"
    />
  </DashboardSummaryListSection>
</template>
