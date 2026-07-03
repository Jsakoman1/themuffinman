<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import VisionIntentPreviewPanel from "../components/VisionIntentPreviewPanel.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"

const {
  isLoading,
  error,
  inputText,
  response,
  voiceState,
  voiceEnabled,
  speechToTextEnabled,
  speechRecognitionSupported,
  displayBlocks,
  activeEntityFamilyLabel,
  activeEntityContextLabel,
  currentSlotLabel,
  currentSlotValue,
  transcriptTargetLabel,
  transcriptTargetDetail,
  currentPlaceholder,
  currentFieldKind,
  speechStatusLabel,
  lastTranscript,
  canSend,
  init,
  processPrompt,
  confirmReview,
  requestReviewChange,
  startListening,
  stopListening,
  openComposer,
  cancelConversation,
} = useVisionConversation()

const route = useRoute()
const router = useRouter()
const lastAutoPromptKey = ref("")

const submitPrompt = async () => {
  await processPrompt(inputText.value, "text")
}

const submitChoice = async (value: string) => {
  inputText.value = value
  await processPrompt(value, "text", "SUBMIT_PROMPT", null, value, value)
}

const updateInputText = (value: string) => {
  inputText.value = value
}

const previewVisible = computed(() => !!response.value && (
  response.value.canvasMode === "review"
  || response.value.canvasMode === "results"
  || response.value.canvasMode === "complete"
  || response.value.canvasMode === "blocked"
  || !!response.value.review
  || !!response.value.executionCandidate
  || !!response.value.questDiscovery
  || !!response.value.memoryTrail
))

const routePrompt = computed(() => {
  const prompt = route.query.prompt
  return typeof prompt === "string" ? prompt.trim() : ""
})

const shouldAutorunRoutePrompt = computed(() => route.query.autorun === "1" && routePrompt.value.length > 0)

const clearRoutePrompt = async () => {
  if (route.path !== "/vision") {
    return
  }
  if (!("prompt" in route.query) && !("autorun" in route.query)) {
    return
  }
  await router.replace({path: "/vision"})
}

const runRoutePromptIfNeeded = async () => {
  if (!shouldAutorunRoutePrompt.value) {
    return
  }

  const autoPromptKey = `${route.fullPath}::${routePrompt.value}`
  if (lastAutoPromptKey.value === autoPromptKey) {
    return
  }

  lastAutoPromptKey.value = autoPromptKey
  inputText.value = routePrompt.value

  try {
    await processPrompt(routePrompt.value, "text")
  } finally {
    await clearRoutePrompt()
  }
}

onMounted(async () => {
  await init()
  await runRoutePromptIfNeeded()
})

watch(
  () => route.fullPath,
  async () => {
    if (isLoading.value) {
      return
    }
    await runRoutePromptIfNeeded()
  }
)
</script>

<template>
  <section class="vision-surface">
    <main
      class="vision-surface__stage"
    >
      <section class="vision-surface__console">
        <VisionCanvasRenderer
          :response="response"
          :display-blocks="displayBlocks"
          :last-transcript="lastTranscript"
          :is-loading="isLoading"
          :error="error"
          :input-text="inputText"
          :prompt-composer-visible="true"
          :current-slot-label="currentSlotLabel"
          :current-slot-value="currentSlotValue"
          :transcript-target-label="transcriptTargetLabel"
          :transcript-target-detail="transcriptTargetDetail"
          :current-field-kind="currentFieldKind"
          :current-placeholder="currentPlaceholder"
          :active-entity-family-label="activeEntityFamilyLabel"
          :active-entity-context-label="activeEntityContextLabel"
          :speech-status-label="speechStatusLabel"
          :voice-enabled="voiceEnabled"
          :speech-to-text-enabled="speechToTextEnabled"
          :speech-recognition-supported="speechRecognitionSupported"
          :voice-state="voiceState"
          :can-send="canSend"
          @choice="submitChoice"
          @review-change="requestReviewChange"
          @confirm-review="confirmReview"
          @start-listening="startListening"
          @stop-listening="stopListening"
          @update:input-text="updateInputText"
          @submit="submitPrompt"
          @open="openComposer"
          @cancel="cancelConversation"
        />
      </section>

      <div class="vision-surface__overlay">
        <VisionIntentPreviewPanel
          :response="response"
          :execution-candidate="response?.executionCandidate ?? null"
          :visible="previewVisible"
        />
      </div>
    </main>
  </section>
</template>

<style scoped>
.vision-surface {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.92), transparent 38%),
    linear-gradient(180deg, #fbfcfa 0%, #f5f7f1 100%);
  color: var(--vision-surface-ink);
}

.vision-surface__stage {
  width: min(100%, 94rem);
  min-height: 100vh;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  align-items: stretch;
  padding: clamp(0.75rem, 1.8vw, 1.5rem);
  position: relative;
}

.vision-surface__console {
  min-width: 0;
  min-height: 0;
  display: grid;
  height: 100%;
  position: relative;
  z-index: 2;
  gap: 0.5rem;
}

.vision-surface__overlay {
  position: absolute;
  top: 50%;
  right: clamp(0.75rem, 2vw, 1.25rem);
  transform: translateY(-42%);
  width: min(31rem, 44vw);
  z-index: 3;
  pointer-events: none;
}

@media (max-width: 980px) {
  .vision-surface__overlay {
    right: 50%;
    transform: translate(50%, -40%);
    width: min(28rem, calc(100vw - 2rem));
  }
}
</style>
