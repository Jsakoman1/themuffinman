<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import VisionFlowDebugPanel from "../components/VisionFlowDebugPanel.vue"
import VisionIntentPreviewPanel from "../components/VisionIntentPreviewPanel.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"
import {hasVisionPreviewContent, shouldShowVisionFlowDebugRail} from "../visionPreviewSupport.ts"

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

const previewVisible = computed(() => hasVisionPreviewContent(response.value))

const showPreviewRail = computed(() => previewVisible.value)
const showFlowDebugRail = computed(() => shouldShowVisionFlowDebugRail(response.value, lastTranscript.value, voiceState.value))
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

const routePrompt = computed(() => {
  const prompt = route.query.prompt
  return typeof prompt === "string" ? prompt.trim() : ""
})

const surfaceNavigator = [
  {label: "Home", to: "/home"},
  {label: "Work", to: "/work"},
  {label: "Chat", to: "/chat"},
  {label: "Calendar", to: "/calendar"},
  {label: "Business", to: "/business"},
  {label: "Profile", to: "/profile"}
]

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
      <div v-if="handoffContext || handoffReturnTo" class="vision-surface__handoff">
        <div class="vision-surface__handoff-copy">
          <p class="vision-surface__handoff-label">Vision Handoff</p>
          <p class="vision-surface__handoff-context">{{ handoffContext || "Shell" }}</p>
          <p v-if="handoffSource" class="vision-surface__handoff-source">Source: {{ handoffSource }}</p>
        </div>

        <div class="vision-surface__handoff-actions">
          <RouterLink v-if="handoffReturnTo" :to="handoffReturnTo" class="vision-surface__handoff-link">
            Return to route
          </RouterLink>
          <RouterLink to="/vision" class="vision-surface__handoff-link vision-surface__handoff-link--vision">
            Vision
          </RouterLink>
        </div>
      </div>

      <div class="vision-surface__navigator" aria-label="Surface navigation">
        <RouterLink
          v-for="item in surfaceNavigator"
          :key="item.to"
          :to="item.to"
          class="vision-surface__navigator-link"
        >
          {{ item.label }}
        </RouterLink>
      </div>

      <div class="vision-surface__layout" :class="{ 'vision-surface__layout--preview': showPreviewRail }">
        <section class="vision-surface__console">
          <VisionCanvasRenderer
            :response="response"
            :runtime-context="response?.runtimeContext ?? null"
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
            @start-listening="startListening"
            @stop-listening="stopListening"
            @update:input-text="updateInputText"
            @submit="submitPrompt"
            @open="openComposer"
            @cancel="cancelConversation"
          />
        </section>

        <aside v-if="showPreviewRail || showFlowDebugRail" class="vision-surface__preview-rail" aria-label="Vision preview rail">
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

.vision-surface__handoff {
  display: flex;
  justify-content: space-between;
  gap: 0.85rem;
  align-items: center;
  margin-bottom: 0.7rem;
  padding: 0.85rem 1rem;
  border-radius: 1.1rem;
  border: 1px solid rgba(23, 34, 26, 0.08);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 30px rgba(23, 34, 26, 0.06);
}

.vision-surface__handoff-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  justify-content: flex-end;
}

.vision-surface__handoff-copy {
  display: grid;
  gap: 0.18rem;
}

.vision-surface__handoff-label,
.vision-surface__handoff-context,
.vision-surface__handoff-source {
  margin: 0;
}

.vision-surface__handoff-label {
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.72rem;
  color: rgba(23, 34, 26, 0.44);
}

.vision-surface__handoff-context {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.vision-surface__handoff-source {
  color: rgba(23, 34, 26, 0.6);
  font-size: 0.92rem;
}

.vision-surface__handoff-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2.5rem;
  padding: 0.55rem 0.85rem;
  border-radius: 999px;
  border: 1px solid rgba(23, 34, 26, 0.12);
  background: rgba(247, 249, 243, 0.92);
}

.vision-surface__handoff-link--vision {
  background: rgba(23, 34, 26, 0.96);
  color: #f8f8f4;
}

.vision-surface__navigator {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-bottom: 0.75rem;
}

.vision-surface__navigator-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2.2rem;
  padding: 0.42rem 0.8rem;
  border-radius: 999px;
  border: 1px solid rgba(23, 34, 26, 0.08);
  background: rgba(255, 255, 255, 0.7);
  color: rgba(23, 34, 26, 0.78);
  font-size: 0.84rem;
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
  .vision-surface__handoff {
    flex-direction: column;
    align-items: flex-start;
  }

  .vision-surface__handoff-actions {
    justify-content: flex-start;
  }

  .vision-surface__layout,
  .vision-surface__layout--preview {
    grid-template-columns: minmax(0, 1fr);
  }

  .vision-surface__navigator {
    gap: 0.4rem;
  }
}
</style>
