<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import VisionFlowDebugPanel from "../components/VisionFlowDebugPanel.vue"
import VisionIntentPreviewPanel from "../components/VisionIntentPreviewPanel.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"
import {hasVisionPreviewContent, shouldShowVisionFlowDebugRail} from "../visionPreviewSupport.ts"
import {normalizeWorkspaceVisionHandoff} from "../../app-shell/visionHandoff.ts"
import {useVisionForWeb} from "../../app-shell/composables/useVisionForWeb.ts"

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
  promptComposerVisible,
  speechStatusLabel,
  lastTranscript,
  canSend,
  init,
  processPrompt,
  confirmReview,
  requestReviewChange,
  fetchMoreResults,
  retryLastRequest,
  startListening,
  stopListening,
  openComposer,
  cancelConversation,
} = useVisionConversation()

const route = useRoute()
const router = useRouter()
const {execute: executeWebAction} = useVisionForWeb()
const lastAutoPromptKey = ref("")
const lastExecutedWebAction = ref("")

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

const previewVisible = computed(() => hasVisionPreviewContent(response.value))
const debugRailRequested = computed(() => route.query.debug === "1")
const showPreviewRail = computed(() => debugRailRequested.value && previewVisible.value)
const showFlowDebugRail = computed(() => debugRailRequested.value && shouldShowVisionFlowDebugRail(response.value, lastTranscript.value, voiceState.value))
const showDebugRail = computed(() => showPreviewRail.value || showFlowDebugRail.value)
const handoffContext = computed(() => {
  const context = route.query.context
  return typeof context === "string" ? context.trim() : ""
})
const handoffSource = computed(() => {
  const source = route.query.source
  return typeof source === "string" ? source.trim() : ""
})
const handoffReturnTo = computed(() => {
  const returnTo = route.query.returnTo
  return typeof returnTo === "string" ? returnTo.trim() : ""
})
const workspaceHandoff = computed(() => normalizeWorkspaceVisionHandoff({
  contextLabel: handoffContext.value || null,
  source: handoffSource.value || null,
  returnTo: handoffReturnTo.value || null
}))

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
  const nextQuery: Record<string, string> = {}

  if (handoffContext.value) {
    nextQuery.context = handoffContext.value
  }
  if (handoffSource.value) {
    nextQuery.source = handoffSource.value
  }
  if (handoffReturnTo.value) {
    nextQuery.returnTo = handoffReturnTo.value
  }

  await router.replace(Object.keys(nextQuery).length > 0 ? {path: "/vision", query: nextQuery} : {path: "/vision"})
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
    await processPrompt(routePrompt.value, "text", "SUBMIT_PROMPT", null, null, null, null, workspaceHandoff.value)
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

watch(
  () => response.value?.webAction,
  async (action) => {
    const actionKey = action ? `${action.action}:${action.canonicalPath}` : ""
    if (action && actionKey !== lastExecutedWebAction.value) {
      lastExecutedWebAction.value = actionKey
      await executeWebAction(action)
    }
  }
)
</script>

<template>
  <section class="vision-surface">
    <main
      class="vision-surface__stage"
    >
      <div class="vision-surface__layout" :class="{ 'vision-surface__layout--preview': showDebugRail }">
        <section class="vision-surface__console" :aria-busy="isLoading || voiceState === 'processing'">
          <VisionCanvasRenderer
            :response="response"
            :execution-candidate="response?.executionCandidate ?? null"
            :runtime-context="response?.runtimeContext ?? null"
            :workspace-handoff="response?.workspaceHandoff ?? null"
            :display-blocks="displayBlocks"
            :last-transcript="lastTranscript"
            :is-loading="isLoading"
            :error="error"
            :input-text="inputText"
            :prompt-composer-visible="promptComposerVisible"
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
            @fetch-more="fetchMoreResults"
            @retry="retryLastRequest"
            @start-listening="startListening"
            @stop-listening="stopListening"
            @update:input-text="updateInputText"
            @submit="submitPrompt"
            @open="openComposer"
            @cancel="cancelConversation"
          />
        </section>

        <aside v-if="showDebugRail" class="vision-surface__preview-rail" aria-label="Vision developer diagnostics">
          <VisionFlowDebugPanel
            :response="response"
            :last-transcript="lastTranscript"
            :transcript-target-label="transcriptTargetLabel"
            :transcript-target-detail="transcriptTargetDetail"
            :current-slot-label="currentSlotLabel"
            :current-slot-value="currentSlotValue"
            :current-field-kind="currentFieldKind"
            :voice-state="voiceState"
          />
          <VisionIntentPreviewPanel
            v-if="showPreviewRail"
            :response="response"
            :execution-candidate="response?.executionCandidate ?? null"
            :visible="previewVisible"
          />
        </aside>
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

.vision-surface__layout {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.45fr);
  gap: clamp(0.75rem, 1.6vw, 1.35rem);
  align-items: stretch;
}

.vision-surface__layout--preview {
  grid-template-columns: minmax(0, 1.3fr) minmax(24rem, 0.86fr);
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

.vision-surface__preview-rail {
  min-width: 0;
  min-height: 0;
  display: grid;
  gap: 0.75rem;
  align-content: start;
  justify-items: stretch;
  pointer-events: none;
}

@media (max-width: 980px) {
  .vision-surface__layout,
  .vision-surface__layout--preview {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
