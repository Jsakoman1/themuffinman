<script setup lang="ts">
import QuestComposerForm from "./QuestComposerForm.vue"
import type {CircleGroup, Quest, QuestAudienceOption, QuestLocationVisibilityOption} from "../../api/workmarketApi.ts"

defineProps<{
  title: string
  description: string
  awardAmount: string
  scheduledAt: string
  endsAt: string
  termMode: "flexible" | "start-only" | "start-end"
  audience: "EVERYONE" | "CIRCLES"
  selectedCircleIds: number[]
  locationSource: NonNullable<Quest["locationSource"]>
  locationCountry: string
  locationLocality: string
  locationPostalCode: string
  locationStreet: string
  locationHouseNumber: string
  locationVisibility: NonNullable<Quest["locationVisibility"]>
  images: string[]
  circleGroups: CircleGroup[]
  questAudienceOptions: QuestAudienceOption[]
  questLocationVisibilityOptions: QuestLocationVisibilityOption[]
  isSaving: boolean
  hasChanges?: boolean
}>()

const emit = defineEmits<{
  "update:title": [value: string]
  "update:description": [value: string]
  "update:awardAmount": [value: string]
  "update:scheduledAt": [value: string]
  "update:endsAt": [value: string]
  "update:termMode": [value: "flexible" | "start-only" | "start-end"]
  "update:audience": [value: "EVERYONE" | "CIRCLES"]
  "toggle:circle": [circleId: number]
  "update:locationSource": [value: NonNullable<Quest["locationSource"]>]
  "update:locationCountry": [value: string]
  "update:locationLocality": [value: string]
  "update:locationPostalCode": [value: string]
  "update:locationStreet": [value: string]
  "update:locationHouseNumber": [value: string]
  "update:locationVisibility": [value: NonNullable<Quest["locationVisibility"]>]
  "change:images": [value: Event]
  "remove:image": [index: number]
  "save": []
  "cancel": []
}>()
</script>

<template>
  <QuestComposerForm
    form-id="quest-detail-edit-form"
    :title="title"
    :description="description"
    :award-amount="awardAmount"
    :term-mode="termMode"
    :scheduled-at="scheduledAt"
    :ends-at="endsAt"
    :audience="audience"
    :audience-options="questAudienceOptions"
    :circles="circleGroups"
    :selected-circle-ids="selectedCircleIds"
    :location-source="locationSource"
    :location-country="locationCountry"
    :location-locality="locationLocality"
    :location-postal-code="locationPostalCode"
    :location-street="locationStreet"
    :location-house-number="locationHouseNumber"
    :location-visibility="locationVisibility"
    :location-visibility-options="questLocationVisibilityOptions"
    :images="images"
    inline-editable
    :submit-visible="hasChanges ?? true"
    submit-label="Save changes"
    :submit-disabled="isSaving"
    show-cancel
    cancel-label="Discard changes"
    @update:title="emit('update:title', $event)"
    @update:description="emit('update:description', $event)"
    @update:award-amount="emit('update:awardAmount', $event)"
    @update:term-mode="emit('update:termMode', $event)"
    @update:scheduled-at="emit('update:scheduledAt', $event)"
    @update:ends-at="emit('update:endsAt', $event)"
    @update:audience="emit('update:audience', $event)"
    @toggle:circle="emit('toggle:circle', $event)"
    @update:location-source="emit('update:locationSource', $event)"
    @update:location-country="emit('update:locationCountry', $event)"
    @update:location-locality="emit('update:locationLocality', $event)"
    @update:location-postal-code="emit('update:locationPostalCode', $event)"
    @update:location-street="emit('update:locationStreet', $event)"
    @update:location-house-number="emit('update:locationHouseNumber', $event)"
    @update:location-visibility="emit('update:locationVisibility', $event)"
    @change:images="emit('change:images', $event)"
    @remove:image="emit('remove:image', $event)"
    @save="emit('save')"
    @cancel="emit('cancel')"
  >
    <template #main-after>
      <slot name="main-after" />
    </template>

    <template #side-after>
      <slot name="side-after" />
    </template>
  </QuestComposerForm>
</template>
