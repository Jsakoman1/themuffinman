<script setup lang="ts">
import type {Quest} from "../../api/workmarketApi.ts"
import type {DashboardApplicationApplyFacade} from "../../composables/dashboard/dashboardFacades.ts"
import DashboardEditSheet from "./DashboardEditSheet.vue"
import ApplicationEditFields from "../shared/ApplicationEditFields.vue"

defineProps<{
  dashboard: DashboardApplicationApplyFacade
  quest: Quest
  canSubmit: boolean
}>()
</script>

<template>
  <form class="stack calendar-application-form" autocomplete="off" @submit.prevent="dashboard.applyForQuest(quest.id)">
    <DashboardEditSheet minimal>
      <ApplicationEditFields
        :message="dashboard.applicationMessages[quest.id] ?? ''"
        :price="dashboard.proposedPrices[quest.id] ?? ''"
        :price-placeholder="String(quest.awardAmount ?? '')"
        quickfill-label="Use suggested"
        @update:message="dashboard.applicationMessages[quest.id] = $event"
        @update:price="dashboard.proposedPrices[quest.id] = $event"
        @quickfill="dashboard.proposedPrices[quest.id] = String(quest.awardAmount ?? '')"
      />

      <template #actions>
        <button class="button button--action" type="submit" :disabled="!canSubmit">Apply</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
