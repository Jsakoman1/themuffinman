<script setup lang="ts">
import DashboardEditSheet from "../dashboard/DashboardEditSheet.vue"
import QuestEditFields from "./QuestEditFields.vue"
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
  <form class="form-stack" @submit.prevent="emit('submit')">
    <DashboardEditSheet minimal>
      <QuestEditFields
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
        show-images
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
      />

      <template #actions>
        <button class="button button--action" type="submit" :disabled="isSaving">Save changes</button>
        <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('cancel')">Discard changes</button>
      </template>
    </DashboardEditSheet>
  </form>
</template>
