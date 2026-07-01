<script setup lang="ts">
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionConversationApi.ts"
import type {VisionExecutionCandidate} from "../api/visionConversationApi.ts"
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
        <div class="vision-review__hero">
          <div class="vision-review__summary">
            <p class="vision-review__eyebrow">Review stage</p>
            <h2>{{ block.review.title }}</h2>
            <p class="vision-review__lede">{{ block.review.description }}</p>
          </div>

          <div class="vision-review__status-stack">
            <span class="vision-review__pill">
              {{ response.executionEnabled ? "Execution on" : "Planning only" }}
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
              Confirm
            </button>
          </div>
        </div>
        <div class="vision-review__snapshot">
          <article class="vision-review__snapshot-card">
            <span class="vision-review__snapshot-label">Reward</span>
            <strong>{{ block.review.rewardLabel }}</strong>
          </article>
          <article class="vision-review__snapshot-card">
            <span class="vision-review__snapshot-label">Visibility</span>
            <strong>{{ block.review.visibility }}</strong>
          </article>
          <article class="vision-review__snapshot-card">
            <span class="vision-review__snapshot-label">Schedule</span>
            <strong>{{ block.review.schedule || "Not set" }}</strong>
          </article>
          <article class="vision-review__snapshot-card">
            <span class="vision-review__snapshot-label">Location</span>
            <strong>{{ block.review.location || "Not set" }}</strong>
          </article>
        </div>

        <div class="vision-review__footer">
          <div v-if="response.appliedSlotSummaries.length" class="vision-review__applied">
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

          <div class="vision-review__edit-rail">
            <button type="button" class="vision-review__edit-chip" @click="emit('reviewChange', 'TITLE')">Title</button>
            <button type="button" class="vision-review__edit-chip" @click="emit('reviewChange', 'DESCRIPTION')">Description</button>
            <button type="button" class="vision-review__edit-chip" @click="emit('reviewChange', 'REWARD')">Reward</button>
            <button type="button" class="vision-review__edit-chip" @click="emit('reviewChange', 'SCHEDULE')">Schedule</button>
            <button type="button" class="vision-review__edit-chip" @click="emit('reviewChange', 'LOCATION')">Location</button>
          </div>
        </div>
      </div>
    </template>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-review {
  display: grid;
  gap: 0.85rem;
}

.vision-review__hero {
  padding: 1rem 1rem 0.95rem;
  border-radius: 1.35rem;
  background: linear-gradient(135deg, rgba(255, 157, 115, 0.12), rgba(127, 203, 255, 0.12));
  border: 1px solid rgba(24, 36, 47, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
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
  font-size: 1.12rem;
  letter-spacing: -0.03em;
}

.vision-review__lede {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-review__status-stack {
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

.vision-review__snapshot {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.65rem;
}

.vision-review__snapshot-card {
  display: grid;
  gap: 0.25rem;
  padding: 0.82rem 0.9rem;
  border-radius: 1.1rem;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(24, 36, 47, 0.07);
  box-shadow: 0 12px 24px rgba(24, 36, 47, 0.04);
}

.vision-review__snapshot-label {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--vision-surface-ink-muted);
}

.vision-review__snapshot-card strong {
  font-size: 0.96rem;
  color: var(--vision-surface-ink);
  font-weight: 600;
}

.vision-review__footer {
  display: grid;
  gap: 0.7rem;
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
  min-width: 9rem;
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

.vision-review__edit-rail {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-review__edit-chip,
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

.vision-review__edit-chip:hover,
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

@media (max-width: 720px) {
  .vision-review__hero {
    grid-template-columns: 1fr;
  }

  .vision-review__status-stack {
    justify-items: start;
  }

  .vision-review__snapshot {
    grid-template-columns: 1fr;
  }
}
</style>
