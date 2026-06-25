<script setup lang="ts">
import QuestComposerForm from "../shared/QuestComposerForm.vue"
import type {DashboardQuestCreateFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {useDashboardPostWorkState} from "../../composables/dashboard/useDashboardPostWorkState.ts"

const props = withDefaults(defineProps<{
  dashboard: DashboardQuestCreateFacade
  boxed?: boolean
}>(), {
  boxed: true,
})

const {
  handleQuestImagesChange,
  toggleQuestAudience,
  toggleQuestCircle
} = useDashboardPostWorkState(props.dashboard)
</script>

<template>
  <QuestComposerForm
    form-id="create-job-form"
    :title="dashboard.questTitle"
    :description="dashboard.questDescription"
    :award-amount="dashboard.questAwardAmount"
    :term-mode="dashboard.questTermMode"
    :scheduled-at="dashboard.questScheduledAt"
    :ends-at="dashboard.questEndsAt"
    :audience="dashboard.questAudience"
    :audience-options="dashboard.questAudienceOptions"
    :circles="dashboard.circles"
    :selected-circle-ids="dashboard.questSelectedCircleIds"
    :images="dashboard.questImages"
    submit-label="Create job"
    :show-creator="dashboard.adminModeEnabled"
    :creator-id="dashboard.questCreatorId"
    :creator-options="dashboard.appUsers"
    @update:title="dashboard.questTitle = $event"
    @update:description="dashboard.questDescription = $event"
    @update:award-amount="dashboard.questAwardAmount = $event"
    @update:term-mode="dashboard.setQuestTermMode($event)"
    @update:scheduled-at="dashboard.questScheduledAt = $event"
    @update:ends-at="dashboard.questEndsAt = $event"
    @update:audience="toggleQuestAudience($event)"
    @toggle:circle="toggleQuestCircle($event)"
    @change:images="handleQuestImagesChange"
    @remove:image="dashboard.removeQuestImage($event)"
    @update:creator-id="dashboard.questCreatorId = $event"
    @submit="dashboard.createQuest"
  />
</template>
