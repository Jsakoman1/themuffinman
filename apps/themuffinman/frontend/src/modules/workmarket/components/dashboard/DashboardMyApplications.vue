<script setup lang="ts">
import {computed} from "vue"
import type {QuestApplication} from "../../api/workmarketApi.ts"
import {createDashboardQuestListState} from "../../composables/dashboard/createDashboardQuestListState.ts"
import type {DashboardQuestListFacade} from "../../composables/dashboard/dashboardFacades.ts"
import DashboardSummaryListButton from "../shared/DashboardSummaryListButton.vue"
import DashboardSummaryListSection from "../shared/DashboardSummaryListSection.vue"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"

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

const groupedApplications = computed(() => {
  if (!props.applications) {
    return props.dashboard.dashboardSections?.myApplicationGroups ?? []
  }

  const groups = new Map<string, {key: string; label: string; items: QuestApplication[]}>()

  for (const application of applications.value) {
    const key = application.status
    const existing = groups.get(key)
    if (existing) {
      existing.items.push(application)
      continue
    }

    groups.set(key, {
      key,
      label: application.presentation.statusLabel,
      items: [application]
    })
  }

  const applicationStatusOrder: Record<string, number> = {
    APPROVED: 1,
    PENDING: 2,
    DECLINED: 3,
    WITHDRAWN: 4,
  }

  return Array.from(groups.values())
    .sort((left, right) => (applicationStatusOrder[left.key] ?? 999) - (applicationStatusOrder[right.key] ?? 999))
    .map((group) => ({
      ...group,
      count: group.items.length,
      tone: "accent"
    }))
})
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
    <section
      v-for="group in groupedApplications"
      :key="group.key"
        :class="['dashboard-status-group', `dashboard-status-group--${group.key.toLowerCase()}`]"
    >
      <div class="dashboard-status-group__header">
        <span class="dashboard-status-group__count">{{ group.count }}</span>
        <strong class="dashboard-status-group__title">{{ group.label }}</strong>
      </div>

      <div class="quest-list">
        <DashboardSummaryListButton
          v-for="application in group.items"
          :key="application.id"
          primary-label="When"
          :primary-value="formatQuestTermForDisplay(application.questScheduledAt, application.questEndsAt, application.questTermFixed)"
          :title="application.questTitle"
          description=""
          :status-surface-class="application.presentation.statusSurfaceClass"
          :pulse="dashboard.successPulseTarget === `application-${application.id}`"
          :compact-inline="true"
          @click="dashboard.openApplicationDialog(application.id)"
        />
      </div>
    </section>
  </DashboardSummaryListSection>
</template>
