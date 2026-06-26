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
    || props.dashboard.editQuestDescription !== quest.description
    || props.dashboard.editQuestAwardAmount.trim() !== String(quest.awardAmount ?? "").trim()
    || props.dashboard.editQuestScheduledAt !== formatInstantForInput(quest.scheduledAt)
    || props.dashboard.editQuestEndsAt !== formatInstantForInput(quest.endsAt)
    || props.dashboard.editQuestTermMode !== normalizedTermMode.value
    || props.dashboard.editQuestAudience !== quest.audience
    || props.dashboard.editQuestLocationSource !== (quest.locationSource ?? "PROFILE")
    || props.dashboard.editQuestLocationCountry !== (quest.locationCountry ?? "")
    || props.dashboard.editQuestLocationLocality !== (quest.locationLocality ?? "")
    || props.dashboard.editQuestLocationPostalCode !== (quest.locationPostalCode ?? "")
    || props.dashboard.editQuestLocationStreet !== (quest.locationStreet ?? "")
    || props.dashboard.editQuestLocationHouseNumber !== (quest.locationHouseNumber ?? "")
    || props.dashboard.editQuestLocationVisibility !== quest.locationVisibility
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
    :location-source="dashboard.editQuestLocationSource"
    :location-country="dashboard.editQuestLocationCountry"
    :location-locality="dashboard.editQuestLocationLocality"
    :location-postal-code="dashboard.editQuestLocationPostalCode"
    :location-street="dashboard.editQuestLocationStreet"
    :location-house-number="dashboard.editQuestLocationHouseNumber"
    :location-visibility="dashboard.editQuestLocationVisibility"
    :location-visibility-options="dashboard.questLocationVisibilityOptions"
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
    @update:location-source="dashboard.editQuestLocationSource = $event"
    @update:location-country="dashboard.editQuestLocationCountry = $event"
    @update:location-locality="dashboard.editQuestLocationLocality = $event"
    @update:location-postal-code="dashboard.editQuestLocationPostalCode = $event"
    @update:location-street="dashboard.editQuestLocationStreet = $event"
    @update:location-house-number="dashboard.editQuestLocationHouseNumber = $event"
    @update:location-visibility="dashboard.editQuestLocationVisibility = $event"
    @toggle:circle="dashboard.editQuestSelectedCircleIds = dashboard.editQuestSelectedCircleIds.includes($event)
      ? dashboard.editQuestSelectedCircleIds.filter((id) => id !== $event)
      : [...dashboard.editQuestSelectedCircleIds, $event]"
    @change:images="dashboard.addEditQuestImages(($event.target as HTMLInputElement | null)?.files ?? null); if ($event.target) { ($event.target as HTMLInputElement).value = '' }"
    @remove:image="dashboard.removeEditQuestImage($event)"
    @update:creator-id="dashboard.editQuestCreatorId = $event"
    @update:status="dashboard.editQuestStatus = $event"
    @save="dashboard.saveEditedQuest"
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
