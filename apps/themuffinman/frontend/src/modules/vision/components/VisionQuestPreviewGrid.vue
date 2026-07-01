<script setup lang="ts">
import {computed} from "vue"
import type {VisionConversationTurnResponse} from "../api/visionConversationApi.ts"
import type {VisionExecutionCandidate} from "../api/visionConversationApi.ts"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  executionCandidate: VisionExecutionCandidate | null
  canConfirm: boolean
  visible: boolean
  debugText: string
}>()

const emit = defineEmits<{
  reviewChange: [target: "TITLE" | "DESCRIPTION" | "REWARD" | "VISIBILITY" | "SCHEDULE" | "LOCATION"]
  confirmReview: []
}>()

type PreviewField = {
  slotId: string
  label: string
  value: string
  missingLabel: string
}

const fieldOrder = computed<PreviewField[]>(() => {
  const summaries = new Map(props.response?.slotSummaries.map((slot) => [slot.slotId, slot.value]) ?? [])
  const review = props.response?.review

  return [
    {slotId: "quest_title", label: "Title", value: review?.title ?? summaries.get("quest_title") ?? "", missingLabel: "waiting"},
    {slotId: "quest_description", label: "Description", value: review?.description ?? summaries.get("quest_description") ?? "", missingLabel: "waiting"},
    {slotId: "reward_amount", label: "Reward", value: review?.rewardLabel ?? summaries.get("reward_amount") ?? "", missingLabel: "waiting"},
    {slotId: "visibility", label: "Visibility", value: review?.visibility ?? summaries.get("visibility") ?? "", missingLabel: "waiting"},
    {slotId: "scheduled_date", label: "Date", value: summaries.get("scheduled_date") ?? "", missingLabel: "waiting"},
    {slotId: "scheduled_time", label: "Time", value: summaries.get("scheduled_time") ?? "", missingLabel: "waiting"},
    {slotId: "location_label", label: "Location", value: review?.location ?? summaries.get("location_label") ?? "", missingLabel: "waiting"}
  ]
})

const filledCount = computed(() => fieldOrder.value.filter((field) => field.value.trim().length > 0).length)
const totalCount = computed(() => fieldOrder.value.length)
const missingCount = computed(() => totalCount.value - filledCount.value)
const showReviewActions = computed(() => props.response?.canvasMode === "review")
</script>

<template>
  <section class="vision-preview">
    <div v-if="visible" class="vision-preview__heading">
      <p class="vision-preview__eyebrow">Preview</p>
      <p class="vision-preview__summary">
        {{ filledCount }}/{{ totalCount }} filled, {{ missingCount }} missing
      </p>
    </div>

    <div class="vision-preview__lines">
      <template v-if="visible">
        <p v-if="!response" class="vision-preview__empty">
          Preview is waiting for the first command.
        </p>
        <p
          v-for="field in fieldOrder"
          v-else
          :key="field.slotId"
          class="vision-preview__line"
          :class="{
            'vision-preview__line--filled': field.value.trim().length > 0,
            'vision-preview__line--empty': field.value.trim().length === 0,
            'vision-preview__line--focus': response?.requestedSlot === field.slotId
          }"
        >
          <span class="vision-preview__label">{{ field.label }}:</span>
          <span v-if="field.value.trim().length > 0" class="vision-preview__value">{{ field.value }}</span>
          <span v-else class="vision-preview__missing">{{ field.missingLabel }}</span>
        </p>
      </template>
    </div>

    <p v-if="visible && executionCandidate" class="vision-preview__candidate">
      {{ executionCandidate.executionReady ? "Ready to create." : executionCandidate.summary }}
    </p>

    <p v-if="debugText" class="vision-preview__debug">
      {{ debugText }}
    </p>

    <div v-if="showReviewActions" class="vision-preview__actions">
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'TITLE')">Edit title</button>
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'DESCRIPTION')">Edit description</button>
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'REWARD')">Edit reward</button>
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'VISIBILITY')">Edit visibility</button>
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'SCHEDULE')">Edit schedule</button>
      <button type="button" class="vision-preview__action" @click="emit('reviewChange', 'LOCATION')">Edit location</button>
      <button type="button" class="vision-preview__confirm" :disabled="!canConfirm" @click="emit('confirmReview')">
        Confirm
      </button>
    </div>
  </section>
</template>

<style scoped>
.vision-preview {
  width: 100%;
  display: grid;
  gap: 0.5rem;
  color: var(--vision-surface-ink);
  height: 100%;
  align-content: start;
}

.vision-preview__heading {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 0.45rem;
  align-items: baseline;
}

.vision-preview__eyebrow {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-preview__summary {
  margin: 0;
  font-size: 0.78rem;
  color: var(--vision-surface-ink-muted);
  letter-spacing: 0.04em;
}

.vision-preview__lines {
  display: grid;
  gap: 0.26rem;
}

.vision-preview__empty {
  margin: 0 0 0.3rem;
  color: var(--vision-surface-ink-muted);
}

.vision-preview__line,
.vision-preview__candidate {
  margin: 0;
  line-height: 1.42;
  font-size: 0.94rem;
}

.vision-preview__line {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-preview__label {
  color: var(--vision-surface-ink-soft);
  font-variant-numeric: tabular-nums;
}

.vision-preview__value {
  color: var(--vision-surface-ink);
}

.vision-preview__missing {
  color: rgba(24, 36, 47, 0.35);
  text-transform: lowercase;
}

.vision-preview__line--filled .vision-preview__value {
  font-weight: 600;
}

.vision-preview__line--empty .vision-preview__label {
  color: rgba(24, 36, 47, 0.5);
}

.vision-preview__line--focus {
  color: #244a7a;
}

.vision-preview__candidate {
  color: var(--vision-surface-ink-soft);
  padding-top: 0.2rem;
}

.vision-preview__debug {
  margin: 0;
  color: rgba(24, 36, 47, 0.45);
  font-size: 0.72rem;
  line-height: 1.4;
  padding-top: 0.15rem;
  border-top: 1px dashed rgba(24, 36, 47, 0.1);
}

.vision-preview__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  padding-top: 0.15rem;
}

.vision-preview__action,
.vision-preview__confirm {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 0.18em;
}

.vision-preview__confirm {
  font-weight: 600;
}

.vision-preview__action:hover,
.vision-preview__confirm:hover {
  color: #244a7a;
}

.vision-preview__confirm:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  text-decoration: none;
}
</style>
