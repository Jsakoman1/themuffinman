<script setup lang="ts">
import type {Quest} from "../../api/workmarketApi.ts"
import type {DashboardApplicationApplyFacade} from "../../composables/dashboard/dashboardFacades.ts"
import QuestApplyForm from "../shared/QuestApplyForm.vue"

defineProps<{
  dashboard: DashboardApplicationApplyFacade
  quest: Quest
  canSubmit: boolean
}>()
</script>

<template>
  <QuestApplyForm
    :message="dashboard.applicationMessages[quest.id] ?? ''"
    :price="dashboard.proposedPrices[quest.id] ?? ''"
    :price-placeholder="String(quest.awardAmount ?? '')"
    quickfill-label="Use suggested"
    :can-submit="canSubmit"
    @update:message="dashboard.applicationMessages[quest.id] = $event"
    @update:price="dashboard.proposedPrices[quest.id] = $event"
    @quickfill="dashboard.proposedPrices[quest.id] = String(quest.awardAmount ?? '')"
    @submit="dashboard.applyForQuest(quest.id)"
  />
</template>
