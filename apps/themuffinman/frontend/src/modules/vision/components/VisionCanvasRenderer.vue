<script setup lang="ts">
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"
import VisionFieldRequestBlock from "./VisionFieldRequestBlock.vue"
import VisionResultSummaryBlock from "./VisionResultSummaryBlock.vue"
import VisionReviewSummaryBlock from "./VisionReviewSummaryBlock.vue"

defineProps<{
  response: VisionConversationTurnResponse
  displayBlocks: VisionCanvasBlock[]
  lastTranscript: string
  canConfirm: boolean
}>()

const emit = defineEmits<{
  choice: [value: string]
  reviewChange: [target: VisionReviewTarget]
  confirmReview: []
}>()

const choose = (value: string) => {
  emit("choice", value)
}
</script>

<template>
  <section class="vision-panel">
    <div
      v-for="(block, index) in displayBlocks"
      :key="`${block.type}-${index}`"
    >
      <VisionResultSummaryBlock
        v-if="block.type === 'result_summary'"
        :block="block"
      />
      <VisionFieldRequestBlock
        v-else-if="block.type === 'field_request'"
        :block="block"
        @choice="choose"
      />
      <VisionReviewSummaryBlock
        v-else-if="block.type === 'review_summary'"
        :block="block"
        :response="response"
        :can-confirm="canConfirm"
        @review-change="emit('reviewChange', $event)"
        @confirm-review="emit('confirmReview')"
      />
      <VisionCanvasSection
        v-else
        :title="block.title"
        :body="block.body"
        :tone="block.type === 'warning'
          ? 'warning'
          : block.type === 'info'
            ? 'info'
            : block.type === 'success'
              ? 'success'
              : 'default'"
      />
    </div>

    <VisionCanvasSection v-if="lastTranscript" title="Last transcript" :body="lastTranscript" />
  </section>
</template>

<style scoped>
.vision-panel {
  width: min(60rem, 100%);
  display: grid;
  gap: 1rem;
  padding: 1.2rem;
  border-radius: 2rem;
  background: var(--vision-surface-panel-bg);
  border: 1px solid var(--vision-surface-border-soft);
  box-shadow: var(--vision-surface-panel-shadow-soft);
  backdrop-filter: blur(20px);
  text-align: left;
}
</style>
