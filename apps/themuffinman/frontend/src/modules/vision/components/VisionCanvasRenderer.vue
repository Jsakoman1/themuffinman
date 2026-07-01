<script setup lang="ts">
import {computed} from "vue"
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"
import VisionExecutionCandidateBlock from "./VisionExecutionCandidateBlock.vue"
import VisionFieldRequestBlock from "./VisionFieldRequestBlock.vue"
import VisionQuestDiscoveryBlock from "./VisionQuestDiscoveryBlock.vue"
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
  transcriptTargetLabel: string
  currentFieldKind: string
  currentPlaceholder: string
  inputText: string
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  textToSpeechEnabled: boolean
  speechRecognitionSupported: boolean
  speechSynthesisSupported: boolean
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
const executionCandidate = computed(() => props.response?.executionCandidate ?? null)
</script>

<template>
  <section class="vision-panel">
    <VisionPromptDock
      :visible="promptComposerVisible"
      :current-slot-label="currentSlotLabel"
      :transcript-target-label="transcriptTargetLabel"
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

    <template v-if="hasResponse">
      <VisionExecutionCandidateBlock
        v-if="executionCandidate"
        :candidate="executionCandidate"
      />
      <div v-if="displayBlocks.length" class="vision-panel__response-stack">
        <div
          v-for="(block, index) in displayBlocks"
          :key="`${block.type}-${index}`"
          class="vision-panel__response-block"
        >
          <VisionResultSummaryBlock
            v-if="block.type === 'result_summary'"
            :block="block"
          />
          <VisionQuestDiscoveryBlock
            v-else-if="block.type === 'quest_discovery'"
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
            :execution-candidate="executionCandidate"
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
      </div>
    </template>
  </section>
</template>

<style scoped>
.vision-panel {
  width: min(58rem, 100%);
  display: grid;
  justify-items: center;
  gap: 0.9rem;
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
  text-align: left;
}

.vision-panel__response-stack {
  display: grid;
  gap: 0.7rem;
  width: 100%;
}

.vision-panel__response-block {
  display: grid;
  gap: 0.7rem;
}

@media (max-width: 720px) {
  .vision-panel {
    gap: 0.75rem;
  }
}
</style>
