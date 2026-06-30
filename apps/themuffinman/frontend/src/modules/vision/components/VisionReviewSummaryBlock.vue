<script setup lang="ts">
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  block: VisionCanvasBlock
  response: VisionConversationTurnResponse
  canConfirm: boolean
}>()

const emit = defineEmits<{
  reviewChange: [target: VisionReviewTarget]
  confirmReview: []
}>()
</script>

<template>
  <VisionCanvasSection
    :title="block.title"
    :body="block.body"
  >
    <template v-if="block.review">
      <div class="vision-review">
        <h2>{{ block.review.title }}</h2>
        <p>{{ block.review.description }}</p>
        <dl class="vision-review__grid">
          <div>
            <dt>Reward</dt>
            <dd>{{ block.review.rewardLabel }}</dd>
          </div>
          <div>
            <dt>Visibility</dt>
            <dd>{{ block.review.visibility }}</dd>
          </div>
          <div>
            <dt>Schedule</dt>
            <dd>{{ block.review.schedule || "Not set" }}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{{ block.review.location || "Not set" }}</dd>
          </div>
          <div>
            <dt>Execution</dt>
            <dd>{{ response.executionEnabled ? "Enabled" : "Planning only" }}</dd>
          </div>
        </dl>
        <div class="vision-choice-list">
          <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'TITLE')">Change title</button>
          <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'DESCRIPTION')">Change description</button>
          <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'REWARD')">Change reward</button>
          <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'SCHEDULE')">Change schedule</button>
          <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'LOCATION')">Change location</button>
        </div>
        <div class="vision-review__actions">
          <button
            type="button"
            class="vision-review__primary"
            :disabled="!canConfirm"
            @click="emit('confirmReview')"
          >
            Confirm and create
          </button>
        </div>
      </div>
    </template>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-review {
  display: grid;
  gap: 0.55rem;
}

.vision-review h2 {
  margin: 0;
  font-size: 1.4rem;
  letter-spacing: -0.03em;
}

.vision-review p {
  margin: 0;
  color: var(--vision-surface-ink-soft);
}

.vision-review__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
  gap: 0.8rem;
  margin: 0;
}

.vision-review__grid dt {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--vision-surface-ink-muted);
}

.vision-review__grid dd {
  margin: 0.35rem 0 0;
  color: var(--vision-surface-ink);
}

.vision-choice-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

.vision-choice-chip,
.vision-review__primary {
  appearance: none;
  border: 1px solid var(--vision-surface-chip-border);
  border-radius: 999px;
  padding: 0.65rem 0.95rem;
  background: var(--vision-surface-chip-bg);
  color: var(--vision-surface-chip-text);
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease;
}

.vision-choice-chip:hover,
.vision-review__primary:hover {
  transform: translateY(-1px);
  box-shadow: var(--vision-surface-chip-shadow);
}

.vision-review__actions {
  display: flex;
  gap: 0.7rem;
  margin-top: 0.2rem;
}

.vision-review__primary {
  border: 0;
  background: var(--vision-surface-accent-gradient);
  color: #10202c;
}

.vision-review__primary:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  transform: none;
  box-shadow: none;
}
</style>
