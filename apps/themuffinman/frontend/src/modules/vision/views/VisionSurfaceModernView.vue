<script setup lang="ts">
import {computed, ref} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import VisionAgentOrb from "../components/VisionAgentOrb.vue"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import VisionPromptDock from "../components/VisionPromptDock.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"

const {
  isLoading,
  error,
  inputText,
  response,
  voiceState,
  voiceEnabled,
  speechToTextEnabled,
  textToSpeechEnabled,
  speechRecognitionSupported,
  speechSynthesisSupported,
  promptComposerVisible,
  displayBlocks,
  currentSlotLabel,
  currentPlaceholder,
  currentFieldKind,
  currentMessage,
  attentionState,
  translationWarning,
  speechStatusLabel,
  voiceRuntimeError,
  lastTranscript,
  canSend,
  canConfirm,
  recentConversations,
  init,
  processPrompt,
  confirmReview,
  requestReviewChange,
  resetConversation,
  cancelConversation,
  resumeConversation,
  startListening,
  stopListening,
  speakSummary,
  stopSpeaking,
  openComposer,
  closeComposer
} = useVisionConversation()

const contextPinned = ref(false)

const agentCaption = computed(() => {
  if (voiceState.value === "listening") {
    return "Listening for the next turn."
  }
  if (voiceState.value === "processing") {
    return "Updating the persisted backend conversation."
  }
  if (voiceState.value === "speaking") {
    return "Speaking the current backend response."
  }
  return currentMessage.value
})

const submitPrompt = async () => {
  await processPrompt(inputText.value, "text")
}

const submitChoice = async (value: string) => {
  inputText.value = value
  await processPrompt(value, "text")
}

const updateInputText = (value: string) => {
  inputText.value = value
}

const nextActionLabel = computed(() => {
  if (!response.value) {
    return "Blank"
  }
  if (response.value.nextAction === "SHOW_REVIEW") {
    return "Review"
  }
  if (response.value.nextAction === "COMPLETE") {
    return "Complete"
  }
  if (response.value.nextAction === "BLOCKED") {
    return "Blocked"
  }
  return "Clarify"
})

const surfaceStatusLabel = computed(() => {
  if (voiceState.value === "listening") {
    return "Listening"
  }
  if (voiceState.value === "processing") {
    return "Processing"
  }
  if (voiceState.value === "speaking") {
    return "Speaking"
  }
  return nextActionLabel.value
})

const surfaceStatusDetail = computed(() => {
  if (voiceRuntimeError.value) {
    return voiceRuntimeError.value
  }
  if (translationWarning.value) {
    return translationWarning.value
  }
  return agentCaption.value
})

const showIntroDetail = computed(() => !!response.value
  || voiceState.value !== "idle"
  || !!translationWarning.value
  || !!voiceRuntimeError.value)

const showIntroHeading = computed(() => !response.value && voiceState.value === "idle")

const autoContextVisible = computed(() => {
  if (!response.value) {
    return false
  }
  return response.value.canvasMode === "review"
    || response.value.canvasMode === "blocked"
    || response.value.canvasMode === "complete"
    || !!translationWarning.value
    || !!voiceRuntimeError.value
})

const contextVisible = computed(() => contextPinned.value || autoContextVisible.value)

const showSurfaceControls = computed(() => !!response.value
  || recentConversations.value.length > 0
  || voiceState.value !== "idle"
  || contextPinned.value
  || autoContextVisible.value)

const surfaceToneClass = computed(() => {
  const canvasMode = response.value?.canvasMode
  if (canvasMode === "review") {
    return "vision-surface--review"
  }
  if (canvasMode === "blocked") {
    return "vision-surface--blocked"
  }
  if (canvasMode === "complete") {
    return "vision-surface--complete"
  }
  if (voiceState.value === "processing") {
    return "vision-surface--processing"
  }
  if (voiceState.value === "listening") {
    return "vision-surface--listening"
  }
  if (voiceState.value === "speaking") {
    return "vision-surface--speaking"
  }
  return "vision-surface--calm"
})

const recentConversationGroups = computed(() => {
  const groups = [
    {
      key: "active",
      title: "In progress",
      items: recentConversations.value.filter((conversation) => conversation.groupKey === "active")
    },
    {
      key: "review_ready",
      title: "Ready for review",
      items: recentConversations.value.filter((conversation) => conversation.groupKey === "review_ready")
    },
    {
      key: "blocked",
      title: "Blocked",
      items: recentConversations.value.filter((conversation) => conversation.groupKey === "blocked")
    },
    {
      key: "completed",
      title: "Completed",
      items: recentConversations.value.filter((conversation) => conversation.groupKey === "completed")
    }
  ]
  return groups.filter((group) => group.items.length > 0)
})

const openRecentConversation = async (conversationId: number, resumable: boolean) => {
  if (!resumable) {
    return
  }
  await resumeConversation(conversationId)
}

useMountedAsync(init)
</script>

<template>
  <section class="vision-surface" :class="surfaceToneClass">
    <div class="vision-surface__wash vision-surface__wash--one" aria-hidden="true"></div>
    <div class="vision-surface__wash vision-surface__wash--two" aria-hidden="true"></div>
    <div class="vision-surface__grain" aria-hidden="true"></div>

    <div v-if="showSurfaceControls" class="vision-surface__controls">
      <button type="button" class="vision-surface__control vision-surface__control--context" @click="contextPinned = !contextPinned">
        <span class="vision-surface__control-label">Context</span>
        <span class="vision-surface__control-value">
          {{ surfaceStatusLabel }}<span v-if="recentConversations.length"> · {{ recentConversations.length }} tasks</span>
        </span>
      </button>
    </div>

    <transition name="vision-float-fade">
      <aside v-if="contextVisible" class="vision-floating-card vision-floating-card--context">
        <p class="vision-floating-card__eyebrow">Current state</p>
        <p class="vision-floating-card__title">{{ surfaceStatusLabel }}</p>
        <p class="vision-floating-card__body">{{ surfaceStatusDetail }}</p>
        <div class="vision-floating-card__meta">
          <span v-if="response?.intent">{{ response.intent }}</span>
          <span v-if="currentSlotLabel">{{ currentSlotLabel }}</span>
        </div>
        <section v-if="recentConversations.length" class="vision-recent-group vision-recent-group--drawer">
          <p class="vision-floating-card__eyebrow">Recent tasks</p>
          <section
            v-for="group in recentConversationGroups"
            :key="group.key"
            class="vision-recent-group"
          >
            <p class="vision-recent-group__title">{{ group.title }}</p>
            <div class="vision-choice-list vision-choice-list--recent">
              <button
                v-for="conversation in group.items"
                :key="conversation.conversationId"
                type="button"
                class="vision-choice-chip vision-choice-chip--recent"
                :class="{
                  'vision-choice-chip--completed': conversation.completed,
                  'vision-choice-chip--active': conversation.resumable,
                  'vision-choice-chip--stale': conversation.stale
                }"
                :disabled="!conversation.resumable"
                @click="openRecentConversation(conversation.conversationId, conversation.resumable)"
              >
                <span class="vision-recent-task__stage">
                  {{ conversation.stageLabel }}<span v-if="conversation.stale"> · Stale</span>
                </span>
                <span class="vision-recent-task__title">{{ conversation.title }}</span>
                <span class="vision-recent-task__progress">{{ conversation.progressLabel }}</span>
              </button>
            </div>
          </section>
        </section>
      </aside>
    </transition>

    <main class="vision-surface__stage">
      <VisionAgentOrb :voice-state="voiceState" :attention-state="attentionState" />

      <div class="vision-surface__intro">
        <p v-if="showIntroHeading" class="vision-surface__eyebrow">Blank canvas</p>
        <h1 v-if="showIntroHeading">One surface that waits until it matters.</h1>
        <p v-if="showIntroDetail">{{ agentCaption }}</p>
        <p v-if="translationWarning && !contextVisible" class="vision-surface__hint">{{ translationWarning }}</p>
        <p v-if="voiceRuntimeError && !contextVisible" class="vision-surface__error">{{ voiceRuntimeError }}</p>
      </div>

      <transition name="vision-panel-fade">
        <VisionCanvasRenderer
          v-if="response"
          :response="response"
          :display-blocks="displayBlocks"
          :last-transcript="lastTranscript"
          :can-confirm="canConfirm"
          @choice="submitChoice"
          @review-change="requestReviewChange"
          @confirm-review="confirmReview"
        />
      </transition>
    </main>

    <VisionPromptDock
      :visible="promptComposerVisible"
      :current-slot-label="currentSlotLabel"
      :current-field-kind="currentFieldKind"
      :current-placeholder="currentPlaceholder"
      :input-text="inputText"
      :voice-enabled="voiceEnabled"
      :speech-to-text-enabled="speechToTextEnabled"
      :text-to-speech-enabled="textToSpeechEnabled"
      :speech-recognition-supported="speechRecognitionSupported"
      :speech-synthesis-supported="speechSynthesisSupported"
      :has-response="!!response"
      :speech-status-label="speechStatusLabel"
      :can-send="canSend"
      :voice-state="voiceState"
      @update:input-text="updateInputText"
      @submit="submitPrompt"
      @start-listening="startListening"
      @stop-listening="stopListening"
      @speak-summary="speakSummary"
      @stop-speaking="stopSpeaking"
      @reset="resetConversation"
      @cancel="cancelConversation"
      @open="openComposer"
      @close="closeComposer"
    />

    <div v-if="isLoading" class="vision-surface__loading">Loading adaptive canvas...</div>
    <div v-else-if="error" class="vision-surface__loading vision-surface__loading--error">{{ error }}</div>
  </section>
</template>

<style scoped>
.vision-surface {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  --vision-surface-ink: #18242f;
  --vision-surface-ink-soft: rgba(24, 36, 47, 0.72);
  --vision-surface-ink-muted: rgba(24, 36, 47, 0.46);
  --vision-surface-border: rgba(24, 36, 47, 0.08);
  --vision-surface-border-soft: rgba(24, 36, 47, 0.05);
  --vision-surface-card: rgba(255, 255, 255, 0.82);
  --vision-surface-card-strong: rgba(255, 255, 255, 0.92);
  --vision-surface-card-soft: rgba(248, 250, 252, 0.9);
  --vision-surface-card-muted: rgba(255, 255, 255, 0.7);
  --vision-surface-shadow-soft: 0 18px 40px rgba(24, 36, 47, 0.06);
  --vision-surface-shadow: 0 30px 80px rgba(24, 36, 47, 0.1);
  --vision-surface-shadow-strong: 0 32px 80px rgba(24, 36, 47, 0.08);
  --vision-surface-shadow-float: 0 18px 50px rgba(24, 36, 47, 0.08);
  --vision-surface-accent-gradient: linear-gradient(135deg, #ff9d73 0%, #7fcbff 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(255, 157, 115, 0.86) 0%, rgba(127, 203, 255, 0.86) 100%);
  --vision-surface-accent-start: rgba(255, 157, 115, 1);
  --vision-surface-accent-end: rgba(127, 203, 255, 1);
  --vision-surface-accent-wash: rgba(255, 157, 115, 0.18);
  --vision-surface-panel-bg: rgba(255, 255, 255, 0.82);
  --vision-surface-panel-border: rgba(24, 36, 47, 0.08);
  --vision-surface-panel-shadow: 0 30px 80px rgba(24, 36, 47, 0.1);
  --vision-surface-panel-shadow-soft: 0 18px 40px rgba(24, 36, 47, 0.06);
  --vision-surface-chip-bg: rgba(255, 255, 255, 0.92);
  --vision-surface-chip-bg-soft: rgba(248, 250, 252, 0.9);
  --vision-surface-chip-text: #18242f;
  --vision-surface-chip-border: rgba(24, 36, 47, 0.08);
  --vision-surface-chip-shadow: 0 10px 24px rgba(24, 36, 47, 0.08);
  --vision-surface-input-bg: rgba(248, 250, 252, 0.92);
  --vision-surface-input-border: rgba(24, 36, 47, 0.06);
  --vision-surface-input-shadow: inset 0 0 0 1px rgba(24, 36, 47, 0.06);
  --vision-surface-section-warning-bg: rgba(255, 244, 229, 0.9);
  --vision-surface-section-info-bg: rgba(240, 247, 255, 0.9);
  --vision-surface-section-field-bg: rgba(244, 249, 255, 0.88);
  --vision-surface-section-success-bg: rgba(237, 250, 241, 0.95);
  --vision-surface-orb-core-start: #fff4ed;
  --vision-surface-orb-core-end: #e6f5ff;
  --vision-surface-orb-core-shadow: rgba(77, 130, 168, 0.2);
  --vision-surface-orb-core-shadow-listening: rgba(255, 160, 122, 0.26);
  --vision-surface-orb-field-start: rgba(255, 162, 121, 0.15);
  --vision-surface-orb-field-mid: rgba(120, 195, 255, 0.12);
  --vision-surface-orb-field-end: rgba(255, 216, 175, 0.08);
  --vision-surface-base-top: #fffdf8;
  --vision-surface-base-mid: #fbfbf8;
  --vision-surface-base-bottom: #f4f6f8;
  --vision-surface-wash-one: rgba(255, 173, 141, 0.18);
  --vision-surface-wash-two: rgba(116, 197, 255, 0.18);
  --vision-surface-wash-three: rgba(255, 255, 255, 0.08);
  background:
    radial-gradient(circle at top, var(--vision-surface-wash-one), transparent 32%),
    radial-gradient(circle at 80% 18%, var(--vision-surface-wash-two), transparent 28%),
    radial-gradient(circle at 48% 84%, var(--vision-surface-wash-three), transparent 24%),
    linear-gradient(180deg, var(--vision-surface-base-top) 0%, var(--vision-surface-base-mid) 45%, var(--vision-surface-base-bottom) 100%);
  color: var(--vision-surface-ink);
}

.vision-surface--review {
  --vision-surface-accent-gradient: linear-gradient(135deg, #ff9f73 0%, #8fb7ff 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(255, 159, 115, 0.84) 0%, rgba(143, 183, 255, 0.84) 100%);
  --vision-surface-accent-wash: rgba(255, 167, 120, 0.2);
  --vision-surface-orb-core-start: #fff7f0;
  --vision-surface-orb-core-end: #eef7ff;
  --vision-surface-orb-core-shadow: rgba(109, 155, 200, 0.2);
  --vision-surface-base-top: #fffaf6;
  --vision-surface-base-mid: #fcfbf9;
  --vision-surface-base-bottom: #f6f2f0;
  --vision-surface-wash-one: rgba(255, 167, 120, 0.2);
  --vision-surface-wash-two: rgba(143, 201, 255, 0.16);
  --vision-surface-wash-three: rgba(255, 245, 232, 0.16);
}

.vision-surface--blocked {
  --vision-surface-accent-gradient: linear-gradient(135deg, #ff8f7e 0%, #ffc6ad 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(255, 143, 126, 0.86) 0%, rgba(255, 198, 173, 0.86) 100%);
  --vision-surface-accent-wash: rgba(255, 149, 131, 0.22);
  --vision-surface-orb-core-start: #fff2f0;
  --vision-surface-orb-core-end: #fff8f7;
  --vision-surface-orb-core-shadow: rgba(178, 105, 105, 0.22);
  --vision-surface-base-top: #fff9f8;
  --vision-surface-base-mid: #fbf8f8;
  --vision-surface-base-bottom: #f6f0ef;
  --vision-surface-wash-one: rgba(255, 149, 131, 0.22);
  --vision-surface-wash-two: rgba(255, 194, 173, 0.14);
  --vision-surface-wash-three: rgba(255, 255, 255, 0.12);
}

.vision-surface--complete {
  --vision-surface-accent-gradient: linear-gradient(135deg, #9edab0 0%, #7abfff 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(158, 218, 176, 0.84) 0%, rgba(122, 191, 255, 0.84) 100%);
  --vision-surface-accent-wash: rgba(160, 220, 176, 0.2);
  --vision-surface-orb-core-start: #f3fbf4;
  --vision-surface-orb-core-end: #ecf7ff;
  --vision-surface-orb-core-shadow: rgba(98, 160, 136, 0.2);
  --vision-surface-base-top: #f9fbf7;
  --vision-surface-base-mid: #fafcf8;
  --vision-surface-base-bottom: #f2f6f1;
  --vision-surface-wash-one: rgba(160, 220, 176, 0.2);
  --vision-surface-wash-two: rgba(120, 189, 255, 0.12);
  --vision-surface-wash-three: rgba(246, 255, 248, 0.18);
}

.vision-surface--processing,
.vision-surface--speaking {
  --vision-surface-accent-gradient: linear-gradient(135deg, #79baff 0%, #ffae89 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(121, 186, 255, 0.84) 0%, rgba(255, 174, 137, 0.84) 100%);
  --vision-surface-accent-wash: rgba(121, 186, 255, 0.22);
  --vision-surface-orb-core-start: #f5fbff;
  --vision-surface-orb-core-end: #edf4f9;
  --vision-surface-orb-core-shadow: rgba(77, 130, 168, 0.22);
  --vision-surface-base-top: #f8fbff;
  --vision-surface-base-mid: #f7f9fc;
  --vision-surface-base-bottom: #eef4f9;
  --vision-surface-wash-one: rgba(121, 186, 255, 0.22);
  --vision-surface-wash-two: rgba(255, 174, 137, 0.16);
  --vision-surface-wash-three: rgba(245, 251, 255, 0.16);
}

.vision-surface--listening {
  --vision-surface-accent-gradient: linear-gradient(135deg, #ffaa84 0%, #78c5ff 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(255, 170, 132, 0.84) 0%, rgba(120, 197, 255, 0.84) 100%);
  --vision-surface-accent-wash: rgba(255, 170, 132, 0.22);
  --vision-surface-orb-core-start: #fff6f2;
  --vision-surface-orb-core-end: #eef8ff;
  --vision-surface-orb-core-shadow: rgba(255, 160, 122, 0.26);
  --vision-surface-base-top: #fffaf8;
  --vision-surface-base-mid: #fcfbf9;
  --vision-surface-base-bottom: #f7f1ee;
  --vision-surface-wash-one: rgba(255, 170, 132, 0.22);
  --vision-surface-wash-two: rgba(120, 197, 255, 0.15);
  --vision-surface-wash-three: rgba(255, 252, 249, 0.14);
}

.vision-surface__wash,
.vision-surface__grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.vision-surface__wash--one {
  background: radial-gradient(circle at 22% 60%, var(--vision-surface-wash-one), transparent 30%);
}

.vision-surface__wash--two {
  background: radial-gradient(circle at 78% 34%, var(--vision-surface-wash-two), transparent 26%);
}

.vision-surface__grain {
  opacity: 0.4;
  background-image: linear-gradient(rgba(24, 36, 47, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(24, 36, 47, 0.025) 1px, transparent 1px);
  background-size: 84px 84px;
  mask-image: radial-gradient(circle at center, black 35%, transparent 90%);
}

.vision-surface__controls {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.25rem 1.25rem 0;
}

.vision-surface__control {
  appearance: none;
  border: 1px solid var(--vision-surface-panel-border);
  border-radius: 1.4rem;
  padding: 0.7rem 0.9rem;
  background: var(--vision-surface-card-muted);
  backdrop-filter: blur(18px);
  box-shadow: var(--vision-surface-shadow-soft);
  color: var(--vision-surface-ink);
  text-align: left;
  cursor: pointer;
  display: grid;
  gap: 0.2rem;
  min-width: 8rem;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.vision-surface__control:hover {
  transform: translateY(-1px);
  box-shadow: var(--vision-surface-shadow-float);
  background: rgba(255, 255, 255, 0.76);
}

.vision-surface__control-label {
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-surface__control-value {
  font-size: 0.92rem;
  color: var(--vision-surface-ink-soft);
}

.vision-floating-card {
  position: absolute;
  z-index: 2;
  border: 1px solid var(--vision-surface-panel-border);
  border-radius: 1.8rem;
  background: var(--vision-surface-panel-bg);
  backdrop-filter: blur(24px);
  box-shadow: var(--vision-surface-panel-shadow);
}

.vision-floating-card--context {
  top: 5.4rem;
  left: 1.25rem;
  width: min(24rem, calc(100vw - 2.5rem));
  padding: 1rem 1.1rem;
}

.vision-floating-card__eyebrow {
  margin: 0 0 0.35rem;
  font-size: 0.68rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-floating-card__title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--vision-surface-ink);
}

.vision-floating-card__body {
  margin: 0.45rem 0 0;
  line-height: 1.5;
  color: var(--vision-surface-ink-soft);
}

.vision-floating-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.vision-floating-card__meta span {
  border-radius: 999px;
  padding: 0.38rem 0.72rem;
  background: var(--vision-surface-card-soft);
  font-size: 0.76rem;
  color: var(--vision-surface-ink-soft);
}

.vision-surface__stage {
  position: relative;
  z-index: 1;
  min-height: 82vh;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 1.2rem;
  padding: 2rem 1.2rem 14rem;
  text-align: center;
}

.vision-surface__intro {
  max-width: 38rem;
}

.vision-surface__eyebrow {
  margin: 0 0 0.55rem;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-surface__intro h1 {
  margin: 0 0 0.8rem;
  font-size: clamp(2.5rem, 7vw, 5.8rem);
  line-height: 0.92;
  letter-spacing: -0.05em;
  font-weight: 620;
}

.vision-surface__intro p {
  margin: 0;
  font-size: 1.02rem;
  line-height: 1.7;
  color: var(--vision-surface-ink-soft);
}

.vision-surface__hint {
  margin-top: 0.8rem !important;
  color: #945d2d !important;
}

.vision-surface__error {
  margin-top: 0.8rem !important;
  color: #b24747 !important;
}

.vision-choice-list--recent {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(14rem, 1fr));
  gap: 0.7rem;
  width: 100%;
  margin-top: 1rem;
}

.vision-recent-group--drawer {
  margin-top: 1rem;
}

.vision-recent-group {
  display: grid;
  gap: 0.4rem;
  margin-top: 1rem;
}

.vision-recent-group__title {
  margin: 0;
  font-size: 0.72rem !important;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted) !important;
}

.vision-choice-chip--recent {
  appearance: none;
  display: grid;
  gap: 0.4rem;
  justify-items: flex-start;
  width: 100%;
  border: 1px solid var(--vision-surface-chip-border);
  border-radius: 1.3rem;
  padding: 0.95rem 1rem;
  background: var(--vision-surface-chip-bg);
  color: var(--vision-surface-chip-text);
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.vision-choice-chip--recent:hover {
  transform: translateY(-1px);
  box-shadow: var(--vision-surface-chip-shadow);
}

.vision-choice-chip--active {
  background: var(--vision-surface-card-strong);
}

.vision-choice-chip--completed {
  background: var(--vision-surface-chip-bg-soft);
}

.vision-choice-chip--stale {
  border-color: rgba(202, 144, 73, 0.28);
}

.vision-choice-chip--recent:disabled {
  cursor: default;
  opacity: 0.8;
  transform: none;
  box-shadow: none;
}

.vision-recent-task__stage {
  font-size: 0.68rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-recent-task__title {
  font-size: 0.98rem;
  font-weight: 600;
  color: var(--vision-surface-ink);
}

.vision-recent-task__progress {
  font-size: 0.84rem;
  line-height: 1.45;
  color: rgba(24, 36, 47, 0.66);
}

.vision-surface__loading {
  position: fixed;
  left: 50%;
  bottom: 7.8rem;
  transform: translateX(-50%);
  z-index: 2;
  padding: 0.65rem 1rem;
  border-radius: 999px;
  background: var(--vision-surface-card-strong);
  color: var(--vision-surface-ink-soft);
  box-shadow: var(--vision-surface-shadow-float);
}

.vision-surface__loading--error {
  color: #a34040;
}

.vision-float-fade-enter-active,
.vision-float-fade-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.vision-float-fade-enter-from,
.vision-float-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.vision-panel-fade-enter-active,
.vision-panel-fade-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.vision-panel-fade-enter-from,
.vision-panel-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 720px) {
  .vision-surface__controls {
    gap: 0.75rem;
  }

  .vision-surface__control {
    min-width: 0;
    flex: 1 1 0;
  }

  .vision-floating-card--context {
    left: 1rem;
    right: 1rem;
    width: auto;
  }

  .vision-surface__stage {
    padding-bottom: 17rem;
  }
}
</style>
