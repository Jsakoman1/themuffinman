<script setup lang="ts">
import {computed} from "vue"
import {useRouter} from "vue-router"
import type {Quest} from "../../api/workmarketApi.ts"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"
import type {DashboardQuestListFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"
import {formatQuestScheduleForDisplay} from "../../../../shared/questSchedule.ts"
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

const groupedQuests = computed(() => {
  if (!props.quests) {
    return props.dashboard.dashboardSections?.myQuestGroups ?? []
  }

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

  const questStatusOrder: Record<string, number> = {
    OPEN: 1,
    WAITING_CONFIRMATION: 2,
    ASSIGNED: 3,
    IN_PROGRESS: 4,
    COMPLETED: 5,
    CANCELLED: 6,
  }

  return Array.from(groups.values())
    .sort((left, right) => (questStatusOrder[left.key] ?? 999) - (questStatusOrder[right.key] ?? 999))
    .map((group) => ({
      ...group,
      count: group.items.length,
      tone: "accent"
    }))
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
        :class="['dashboard-status-group', `dashboard-status-group--${group.key.toLowerCase()}`]"
    >
      <div class="dashboard-status-group__header">
        <span class="dashboard-status-group__count">{{ group.count }}</span>
        <strong class="dashboard-status-group__title">{{ group.label }}</strong>
      </div>

      <div class="quest-list">
        <DashboardSummaryListButton
          v-for="quest in group.items"
          :key="quest.id"
          primary-label="When"
          :primary-value="formatQuestScheduleForDisplay(quest.scheduledAt, quest.endsAt)"
          :title="quest.title"
          description=""
          :status-surface-class="quest.presentation.statusSurfaceClass"
          :pulse="dashboard.successPulseTarget === `quest-${quest.id}`"
          :compact-inline="true"
          @click="openQuest(quest)"
        />
      </div>
    </section>
  </DashboardSummaryListSection>
</template>
