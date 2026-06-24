<script setup lang="ts">
import {useRouter} from "vue-router"
import UiSectionHeader from "../../../../components/ui/UiSectionHeader.vue"
import DashboardQuestSummaryRow from "./DashboardQuestSummaryRow.vue"
import type {Quest} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
  title?: string
  subtitle?: string
  emptyMessage?: string
  quests?: Quest[]
  showHeader?: boolean
  boxed?: boolean
}>(), {
  title: "Your work",
  subtitle: "Manage your quests.",
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
  <section class="stack">
    <div :class="{ card: props.boxed }">
      <UiSectionHeader v-if="props.showHeader" :title="props.title" :subtitle="props.subtitle" />

      <div v-if="!quests.length" class="empty-state">
        {{ props.emptyMessage }}
      </div>

      <div v-else class="quest-list mt-4">
        <button
          v-for="quest in quests"
          :key="quest.id"
          type="button"
          class="compact-disclosure compact-disclosure--tight compact-disclosure--launch"
          :class="[quest.presentation.statusSurfaceClass, { 'ui-pulse': dashboard.successPulseTarget === `quest-${quest.id}` }]"
          @click="openQuest(quest)"
        >
          <DashboardQuestSummaryRow
            primary-label="Amount"
            :primary-value="quest.awardAmount"
            primary-icon="$"
            money-tone="expense"
            secondary-label="Term"
            :secondary-value="quest.presentation.termLabel"
            :title="quest.title"
            :description="quest.description"
          />
        </button>
      </div>
    </div>
  </section>
</template>
