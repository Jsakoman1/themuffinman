<script setup lang="ts">
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionApi.ts"
import type {VisionExecutionCandidate} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  block: VisionCanvasBlock
  response: VisionConversationTurnResponse
  canConfirm: boolean
  executionCandidate: VisionExecutionCandidate | null
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
        <div class="vision-review__header">
          <div class="vision-review__summary">
            <p class="vision-review__eyebrow">Review ready</p>
            <h2>{{ block.review.title }}</h2>
            <p class="vision-review__lede">{{ block.review.description }}</p>
            <p class="vision-review__note">Edit any field above, then confirm when the summary feels right.</p>
          </div>
          <div class="vision-review__cta">
            <span class="vision-review__pill">
              {{ response.executionEnabled ? "Execution enabled" : "Planning only" }}
            </span>
            <span v-if="executionCandidate" class="vision-review__pill vision-review__pill--soft">
              {{ executionCandidate.executionReady ? "Ready to execute" : executionCandidate.summary }}
            </span>
            <button
              type="button"
              class="vision-review__confirm"
              :disabled="!canConfirm"
              @click="emit('confirmReview')"
            >
              Confirm and create
            </button>
          </div>
        </div>
        <div class="vision-review__body">
          <div class="vision-review__applied" v-if="response.appliedSlotSummaries.length">
            <p class="vision-review__subheading">Applied this turn</p>
            <div class="vision-review__applied-list">
              <article
                v-for="slot in response.appliedSlotSummaries"
                :key="slot.slotId"
                class="vision-review__applied-chip"
              >
                <span class="vision-review__applied-label">{{ slot.label }}</span>
                <span class="vision-review__applied-value">{{ slot.value }}</span>
              </article>
            </div>
          </div>
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
          </dl>
          <div class="vision-choice-list">
            <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'TITLE')">Title</button>
            <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'DESCRIPTION')">Description</button>
            <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'REWARD')">Reward</button>
            <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'SCHEDULE')">Schedule</button>
            <button type="button" class="vision-choice-chip" @click="emit('reviewChange', 'LOCATION')">Location</button>
          </div>
        </div>
      </div>
    </template>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-review {
  display: grid;
  gap: 0.8rem;
}

.vision-review__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
}

.vision-review__summary {
  display: grid;
  gap: 0.35rem;
}

.vision-review__eyebrow {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-review h2 {
  margin: 0;
  font-size: 1.08rem;
  letter-spacing: -0.03em;
}

.vision-review__lede {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-review__note {
  margin: 0;
  color: var(--vision-surface-ink-muted);
  font-size: 0.9rem;
  line-height: 1.45;
}

.vision-review__cta {
  display: grid;
  gap: 0.55rem;
  justify-items: end;
}

.vision-review__pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 0.42rem 0.78rem;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.82);
  color: var(--vision-surface-ink-soft);
  font-size: 0.76rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vision-review__pill--soft {
  opacity: 0.85;
}

.vision-review__body {
  display: grid;
  gap: 0.75rem;
}

.vision-review__applied {
  display: grid;
  gap: 0.45rem;
}

.vision-review__subheading {
  margin: 0;
  font-size: 0.7rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-review__applied-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-review__applied-chip {
  display: grid;
  gap: 0.2rem;
  min-width: 9.5rem;
  padding: 0.65rem 0.78rem;
  border-radius: 1rem;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.78);
}

.vision-review__applied-label {
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-review__applied-value {
  color: var(--vision-surface-ink);
}

.vision-review__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
  gap: 0.55rem 0.75rem;
  margin: 0;
}

.vision-review__grid dt {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: var(--vision-surface-ink-muted);
}

.vision-review__grid dd {
  margin: 0.35rem 0 0;
  color: var(--vision-surface-ink);
}

.vision-choice-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-choice-chip,
.vision-review__confirm {
  appearance: none;
  border: 1px solid var(--vision-surface-chip-border);
  border-radius: 999px;
  padding: 0.58rem 0.9rem;
  background: var(--vision-surface-chip-bg);
  color: var(--vision-surface-chip-text);
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease;
}

.vision-choice-chip:hover,
.vision-review__confirm:hover {
  transform: translateY(-1px);
  box-shadow: var(--vision-surface-chip-shadow);
}

.vision-review__confirm {
  border-color: rgba(24, 36, 47, 0.1);
  background: rgba(255, 255, 255, 0.88);
  color: var(--vision-surface-ink);
  padding-inline: 1rem;
}

.vision-review__confirm:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  transform: none;
  box-shadow: none;
}

@media (max-width: 42rem) {
  .vision-review__header {
    flex-direction: column;
  }

  .vision-review__cta {
    justify-items: start;
  }
}
</style>
