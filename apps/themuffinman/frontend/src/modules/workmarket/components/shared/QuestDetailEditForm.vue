<script setup lang="ts">
import QuestComposerForm from "./QuestComposerForm.vue"
import type {CircleGroup, QuestAudienceOption} from "../../api/workmarketApi.ts"

defineProps<{
  title: string
  description: string
  awardAmount: string
  scheduledAt: string
  endsAt: string
  termMode: "flexible" | "start-only" | "start-end"
  audience: "EVERYONE" | "CIRCLES"
  selectedCircleIds: number[]
  images: string[]
  circleGroups: CircleGroup[]
  questAudienceOptions: QuestAudienceOption[]
  isSaving: boolean
  hasChanges?: boolean
}>()

const emit = defineEmits<{
  (event: "update:title", value: string): void
  (event: "update:description", value: string): void
  (event: "update:awardAmount", value: string): void
  (event: "update:scheduledAt", value: string): void
  (event: "update:endsAt", value: string): void
  (event: "update:termMode", value: "flexible" | "start-only" | "start-end"): void
  (event: "update:audience", value: "EVERYONE" | "CIRCLES"): void
  (event: "toggle:circle", circleId: number): void
  (event: "change:images", value: Event): void
  (event: "remove:image", index: number): void
  (event: "submit"): void
  (event: "cancel"): void
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
    @change:images="emit('change:images', $event)"
    @remove:image="emit('remove:image', $event)"
    @submit="emit('submit')"
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
