<script setup lang="ts">
import {useRouter} from "vue-router"
import type {Quest} from "../../api/workmarketApi.ts"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"
import type {DashboardQuestListFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import DashboardSummaryListButton from "../shared/DashboardSummaryListButton.vue"
import DashboardSummaryListSection from "../shared/DashboardSummaryListSection.vue"

const props = withDefaults(defineProps<{
  dashboard: DashboardQuestListFacade
  title?: string
  subtitle?: string
  emptyMessage?: string
  quests?: Quest[]
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "Your work",
  subtitle: "",
  emptyMessage: "No quests here yet.",
  quests: undefined,
  showHeader: true,
  boxed: true,
})

const router = useRouter()
const {quests} = createDashboardQuestListState(props.dashboard, {quests: props.quests})

const openQuest = async (quest: Quest) => {
  await router.push(routeForNavigationTarget(quest.questNavigation))
}
</script>

<template>
  <DashboardSummaryListSection
    :title="props.title"
    :subtitle="props.subtitle"
    :empty-message="props.emptyMessage"
    :has-items="quests.length > 0"
    :show-header="props.showHeader"
    :boxed="props.boxed"
  >
    <DashboardSummaryListButton
      v-for="quest in quests"
      :key="quest.id"
      primary-label="Amount"
      :primary-value="quest.awardAmount"
      primary-icon="$"
      money-tone="expense"
      secondary-label="Term"
      :secondary-value="quest.presentation.termLabel"
      :title="quest.title"
      :description="quest.description"
      :status-surface-class="quest.presentation.statusSurfaceClass"
      :pulse="dashboard.successPulseTarget === `quest-${quest.id}`"
      @click="openQuest(quest)"
    />
  </DashboardSummaryListSection>
</template>
