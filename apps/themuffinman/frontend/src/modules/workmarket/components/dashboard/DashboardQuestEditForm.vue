<script setup lang="ts">
import {computed} from "vue"
import type {DashboardQuestEditFacade} from "../../composables/dashboard/dashboardFacades.ts"
import QuestComposerForm from "../shared/QuestComposerForm.vue"
import {formatInstantForInput} from "../../../../shared/questSchedule.ts"

const props = defineProps<{ dashboard: DashboardQuestEditFacade }>()
defineEmits<{ discard: [] }>()

const normalizedTermMode = computed(() => (
  props.dashboard.selectedQuestDialog?.termFixed
    ? (props.dashboard.selectedQuestDialog.endsAt ? "start-end" : "start-only")
    : "flexible"
))

const hasChanges = computed(() => {
  const quest = props.dashboard.selectedQuestDialog
  if (!quest) {
    return false
  }

  return props.dashboard.editQuestTitle.trim() !== quest.title.trim()
    || props.dashboard.editQuestDescription.trim() !== quest.description.trim()
    || props.dashboard.editQuestAwardAmount.trim() !== String(quest.awardAmount ?? "").trim()
    || props.dashboard.editQuestScheduledAt !== formatInstantForInput(quest.scheduledAt)
    || props.dashboard.editQuestEndsAt !== formatInstantForInput(quest.endsAt)
    || props.dashboard.editQuestTermMode !== normalizedTermMode.value
    || props.dashboard.editQuestAudience !== quest.audience
    || props.dashboard.editQuestCreatorId !== String(quest.creatorId)
    || props.dashboard.editQuestStatus !== quest.status
    || props.dashboard.editQuestSelectedCircleIds.length !== quest.visibleToCircles.length
    || props.dashboard.editQuestSelectedCircleIds.some((id) => !quest.visibleToCircles.some((circle) => circle.id === id))
    || props.dashboard.editQuestImages.length !== quest.images.length
    || props.dashboard.editQuestImages.some((image, index) => image !== quest.images[index])
})
</script>

<template>
  <QuestComposerForm
    form-id="edit-dashboard-quest-form"
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
    inline-editable
    :submit-visible="hasChanges"
    submit-label="Save changes"
    show-cancel
    cancel-label="Discard changes"
    :show-creator="dashboard.adminModeEnabled"
    :creator-id="dashboard.editQuestCreatorId"
    :creator-options="dashboard.appUsers"
    show-status
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
    @submit="dashboard.saveEditedQuest"
    @cancel="$emit('discard')"
  >
    <template #main-after>
      <slot name="main-after" />
    </template>

    <template #side-after>
      <slot name="side-after" />
    </template>
  </QuestComposerForm>
</template>
