<script setup lang="ts">
import {computed} from "vue"
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"
import VisionFieldRequestBlock from "./VisionFieldRequestBlock.vue"
import VisionResultSummaryBlock from "./VisionResultSummaryBlock.vue"
import VisionReviewSummaryBlock from "./VisionReviewSummaryBlock.vue"
import VisionPromptDock from "./VisionPromptDock.vue"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  displayBlocks: VisionCanvasBlock[]
  lastTranscript: string
  canConfirm: boolean
  promptComposerVisible: boolean
  currentSlotLabel: string
  currentSlotValue: string
  transcriptTargetLabel: string
  transcriptTargetDetail: string
  currentFieldKind: string
  currentPlaceholder: string
  inputText: string
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  textToSpeechEnabled: boolean
  speechRecognitionSupported: boolean
  speechSynthesisSupported: boolean
  speechStatusLabel: string
  canSend: boolean
  voiceState: "idle" | "listening" | "processing" | "speaking"
}>()

const emit = defineEmits<{
  choice: [value: string]
  reviewChange: [target: VisionReviewTarget]
  confirmReview: []
  "update:inputText": [value: string]
  submit: []
  startListening: []
  stopListening: []
  speakSummary: []
  stopSpeaking: []
  reset: []
  cancel: []
  open: []
  close: []
}>()

const choose = (value: string) => {
  emit("choice", value)
}

const hasResponse = computed(() => !!props.response)
</script>

<template>
  <section class="vision-panel">
    <VisionCanvasSection
      v-if="!hasResponse"
      title="Blank canvas"
      body="Type a prompt or speak to begin. The canvas will expand as the task becomes clearer."
      tone="info"
    >
      <div class="vision-panel__idle-prompt">
        <p class="vision-panel__idle-copy">
          The surface is waiting for a task. Start with one sentence, then let the canvas shape itself around what the backend understands.
        </p>
      </div>
    </VisionCanvasSection>

    <template v-else>
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
          :response="response!"
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

    </template>

    <VisionPromptDock
      :visible="promptComposerVisible"
      :current-slot-label="currentSlotLabel"
      :current-slot-value="currentSlotValue"
      :transcript-target-label="transcriptTargetLabel"
      :transcript-target-detail="transcriptTargetDetail"
      :current-field-kind="currentFieldKind"
      :current-placeholder="currentPlaceholder"
      :input-text="inputText"
      :last-transcript="lastTranscript"
      :voice-enabled="voiceEnabled"
      :speech-to-text-enabled="speechToTextEnabled"
      :text-to-speech-enabled="textToSpeechEnabled"
      :speech-recognition-supported="speechRecognitionSupported"
      :speech-synthesis-supported="speechSynthesisSupported"
      :has-response="hasResponse"
      :speech-status-label="speechStatusLabel"
      :can-send="canSend"
      :voice-state="voiceState"
      @update:input-text="emit('update:inputText', $event)"
      @submit="emit('submit')"
      @start-listening="emit('startListening')"
      @stop-listening="emit('stopListening')"
      @speak-summary="emit('speakSummary')"
      @stop-speaking="emit('stopSpeaking')"
      @reset="emit('reset')"
      @cancel="emit('cancel')"
      @open="emit('open')"
      @close="emit('close')"
    />
  </section>
</template>

<style scoped>
.vision-panel {
  width: min(60rem, 100%);
  display: grid;
  gap: 1rem;
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
  text-align: left;
}

.vision-panel__idle-prompt {
  display: grid;
  gap: 0.75rem;
}

.vision-panel__idle-copy {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.6;
}
</style>
