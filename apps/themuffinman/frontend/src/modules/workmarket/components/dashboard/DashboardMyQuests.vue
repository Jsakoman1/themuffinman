<script setup lang="ts">
import {computed} from "vue"
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

const questStatusOrder: Record<string, number> = {
  OPEN: 1,
  IN_PROGRESS: 2,
  ASSIGNED: 3,
  COMPLETED: 4,
  CANCELLED: 5,
}

const groupedQuests = computed(() => {
  const groups = new Map<string, {key: string; label: string; items: Quest[]}>()

  for (const quest of quests.value) {
    const key = quest.status
    const existing = groups.get(key)
    if (existing) {
      existing.items.push(quest)
      continue
    }

    groups.set(key, {
      key,
      label: quest.presentation.statusLabel,
      items: [quest]
    })
  }

  return Array.from(groups.values()).sort((left, right) =>
    (questStatusOrder[left.key] ?? 999) - (questStatusOrder[right.key] ?? 999)
  )
})

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
    <section
      v-for="group in groupedQuests"
      :key="group.key"
      class="dashboard-status-group"
    >
      <div class="dashboard-status-group__header">
        <strong class="dashboard-status-group__title">{{ group.label }}</strong>
        <span class="badge badge--secondary">{{ group.items.length }}</span>
      </div>

      <div class="quest-list">
        <DashboardSummaryListButton
          v-for="quest in group.items"
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
        >
          <template #meta>
            <span :class="quest.presentation.statusBadgeClass">{{ quest.presentation.statusLabel }}</span>
          </template>
        </DashboardSummaryListButton>
      </div>
    </section>
  </DashboardSummaryListSection>
</template>
