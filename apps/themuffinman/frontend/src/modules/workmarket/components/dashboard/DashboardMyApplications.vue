<script setup lang="ts">
import DashboardQuestSummaryRow from "./DashboardQuestSummaryRow.vue"
import type {QuestApplication} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import UiSectionHeader from "../../../../components/ui/UiSectionHeader.vue"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
  title?: string
  subtitle?: string
  emptyMessage?: string
  applications?: QuestApplication[]
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "My applications",
  subtitle: "Jobs you have already applied to.",
  emptyMessage: "No applications yet.",
  applications: undefined,
  showHeader: true,
  boxed: true,
})

const {applications} = createDashboardQuestListState(props.dashboard, {applications: props.applications})
</script>

<template>
  <section class="stack">
    <div :class="{ card: props.boxed }">
      <UiSectionHeader v-if="props.showHeader" :title="props.title" :subtitle="props.subtitle" />

      <div v-if="!applications.length" class="empty-state">
        {{ props.emptyMessage }}
      </div>

      <div v-else class="quest-list mt-4">
        <button
          v-for="application in applications"
          :key="application.id"
          type="button"
          class="compact-disclosure compact-disclosure--tight compact-disclosure--launch"
          :class="[application.presentation.statusSurfaceClass, { 'ui-pulse': dashboard.successPulseTarget === `application-${application.id}` }]"
          @click="dashboard.openApplicationDialog(application.id)"
        >
          <DashboardQuestSummaryRow
            primary-label="Creator"
            :primary-value="dashboard.questCreatorUsernameForQuest(application.questId)"
            secondary-label="Price"
            :secondary-value="application.proposedPrice"
            secondary-icon="$"
            money-tone="income"
            :title="application.questTitle"
            :description="application.questDescription"
          />
        </button>
      </div>
    </div>
  </section>
</template>
