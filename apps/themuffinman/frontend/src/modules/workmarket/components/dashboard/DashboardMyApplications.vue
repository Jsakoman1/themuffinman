<script setup lang="ts">
import {computed} from "vue"
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

const applicationStatusOrder: Record<string, number> = {
  PENDING: 1,
  ACCEPTED: 2,
  WITHDRAWN: 3,
  DECLINED: 4,
  CANCELLED: 5,
}

const groupedApplications = computed(() => {
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

  return Array.from(groups.values()).sort((left, right) =>
    (applicationStatusOrder[left.key] ?? 999) - (applicationStatusOrder[right.key] ?? 999)
  )
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
      class="dashboard-status-group"
    >
      <div class="dashboard-status-group__header">
        <strong class="dashboard-status-group__title">{{ group.label }}</strong>
        <span class="badge badge--secondary">{{ group.items.length }}</span>
      </div>

      <div class="quest-list">
        <DashboardSummaryListButton
          v-for="application in group.items"
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
        >
          <template #meta>
            <span :class="application.presentation.statusBadgeClass">{{ application.presentation.statusLabel }}</span>
          </template>
        </DashboardSummaryListButton>
      </div>
    </section>
  </DashboardSummaryListSection>
</template>
