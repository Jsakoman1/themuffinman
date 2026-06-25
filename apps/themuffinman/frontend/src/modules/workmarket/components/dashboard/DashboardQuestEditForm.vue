<script setup lang="ts">
import type {DashboardQuestEditFacade} from "../../composables/dashboard/dashboardFacades.ts"
import DashboardEditSheet from "./DashboardEditSheet.vue"
import QuestEditFields from "../shared/QuestEditFields.vue"

defineProps<{ dashboard: DashboardQuestEditFacade }>()
defineEmits<{ discard: [] }>()
</script>

<template>
  <form class="form-stack" @submit.prevent="dashboard.saveEditedQuest">
    <DashboardEditSheet minimal>
      <QuestEditFields
        :title="dashboard.editQuestTitle"
        :description="dashboard.editQuestDescription"
        :award-amount="dashboard.editQuestAwardAmount"
        :term-mode="dashboard.editQuestTermMode"
        :scheduled-at="dashboard.editQuestScheduledAt"
        :ends-at="dashboard.editQuestEndsAt"
        :audience="dashboard.editQuestAudience"
        :audience-options="dashboard.questAudienceOptions"
        :circles="dashboard.circles"
        :selected-circle-ids="dashboard.editQuestSelectedCircleIds"
        :images="dashboard.editQuestImages"
        show-images
        :show-admin-fields="dashboard.adminModeEnabled"
        :creator-id="dashboard.editQuestCreatorId"
        :creator-options="dashboard.appUsers"
        :status="dashboard.editQuestStatus"
        :status-options="dashboard.questStatusOptions"
        @update:title="dashboard.editQuestTitle = $event"
        @update:description="dashboard.editQuestDescription = $event"
        @update:award-amount="dashboard.editQuestAwardAmount = $event"
        @update:term-mode="dashboard.setEditQuestTermMode($event)"
        @update:scheduled-at="dashboard.editQuestScheduledAt = $event"
        @update:ends-at="dashboard.editQuestEndsAt = $event"
        @update:audience="dashboard.editQuestAudience = $event"
        @toggle:circle="dashboard.editQuestSelectedCircleIds = dashboard.editQuestSelectedCircleIds.includes($event)
          ? dashboard.editQuestSelectedCircleIds.filter((id) => id !== $event)
          : [...dashboard.editQuestSelectedCircleIds, $event]"
        @change:images="dashboard.addEditQuestImages(($event.target as HTMLInputElement | null)?.files ?? null); if ($event.target) { ($event.target as HTMLInputElement).value = '' }"
        @remove:image="dashboard.removeEditQuestImage($event)"
        @update:creator-id="dashboard.editQuestCreatorId = $event"
        @update:status="dashboard.editQuestStatus = $event"
      />

      <template #actions>
        <button class="button button--action" type="submit">Save changes</button>
        <button class="button button--ghost" type="button" @click="$emit('discard')">Discard changes</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
