<script setup lang="ts">
import type {VisionExecutionCandidate} from "../api/visionConversationApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  candidate: VisionExecutionCandidate
}>()
</script>

<template>
  <VisionCanvasSection
    :title="candidate.executionReady ? 'Execution ready' : 'Execution plan'"
    :body="candidate.summary"
    :tone="candidate.executionReady ? 'success' : candidate.reviewReady ? 'info' : 'field'"
  >
    <div class="vision-execution-candidate">
      <div class="vision-execution-candidate__meta">
        <span class="vision-execution-candidate__pill">{{ candidate.capabilityId }}</span>
        <span class="vision-execution-candidate__confidence">{{ Math.round(candidate.confidence * 100) }}% confidence</span>
      </div>
      <p v-if="candidate.planningNote" class="vision-execution-candidate__note">
        {{ candidate.planningNote }}
      </p>
      <p v-if="candidate.blockingReason" class="vision-execution-candidate__blocker">
        {{ candidate.blockingReason }}
      </p>
      <p v-if="candidate.nextRequiredSlot" class="vision-execution-candidate__next">
        Next required field: {{ candidate.nextRequiredSlot }}
      </p>
    </div>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-execution-candidate {
  display: grid;
  gap: 0.5rem;
}

.vision-execution-candidate__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  align-items: center;
}

.vision-execution-candidate__pill {
  display: inline-flex;
  align-items: center;
  padding: 0.34rem 0.64rem;
  border-radius: 999px;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.88);
  color: var(--vision-surface-ink-soft);
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vision-execution-candidate__confidence {
  color: var(--vision-surface-ink-muted);
  font-size: 0.86rem;
}

.vision-execution-candidate__note,
.vision-execution-candidate__blocker,
.vision-execution-candidate__next {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-execution-candidate__blocker {
  color: var(--vision-surface-ink);
}
</style>
