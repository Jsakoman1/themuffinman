<script setup lang="ts">
import {ref} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import VisionAgentOrb from "../components/VisionAgentOrb.vue"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"
import {useVisionSurfaceState} from "../composables/useVisionSurfaceState.ts"

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
  transcriptTargetLabel,
  currentPlaceholder,
  currentFieldKind,
  currentMessage,
  attentionState,
  translationWarning,
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

const {
  surfaceStatusLabel,
  surfaceStatusDetail,
  contextVisible,
  showContextToggle,
  surfaceToneClass,
  recentConversationGroups
} = useVisionSurfaceState({
  voiceState,
  voiceRuntimeError,
  translationWarning,
  response,
  currentMessage,
  currentSlotLabel,
  recentConversations,
  contextPinned
})

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
    <main class="vision-surface__stage">
      <VisionAgentOrb class="vision-surface__orb" :voice-state="voiceState" :attention-state="attentionState" />

      <div v-if="isLoading" class="vision-surface__loading">Loading adaptive canvas...</div>
      <div v-else-if="error" class="vision-surface__loading vision-surface__loading--error">{{ error }}</div>

      <section class="vision-surface__surface-shell">
        <div v-if="showContextToggle" class="vision-surface__shell-utility">
          <button
            type="button"
            class="vision-surface__context-chip"
            :class="{'vision-surface__context-chip--active': contextVisible}"
            @click="contextPinned = !contextPinned"
            :aria-label="contextVisible ? 'Hide context' : 'Show context'"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M7.5 19h10a4.5 4.5 0 0 0 .4-8.98A5.5 5.5 0 0 0 7.7 8.3 3.75 3.75 0 0 0 7.5 19Zm6.5-9h1.5a3 3 0 0 1 .3 6h-8a2.25 2.25 0 0 1-.2-4.49A4 4 0 0 1 14 10Z" />
            </svg>
          </button>
        </div>

        <div class="vision-surface__surface-layout">
          <div class="vision-surface__main-column">
            <transition name="vision-panel-fade">
              <VisionCanvasRenderer
                :response="response"
                :display-blocks="displayBlocks"
                :last-transcript="lastTranscript"
                :can-confirm="canConfirm"
                :prompt-composer-visible="promptComposerVisible"
                :current-slot-label="currentSlotLabel"
                :transcript-target-label="transcriptTargetLabel"
                :current-field-kind="currentFieldKind"
                :current-placeholder="currentPlaceholder"
                :input-text="inputText"
                :voice-enabled="voiceEnabled"
                :speech-to-text-enabled="speechToTextEnabled"
                :text-to-speech-enabled="textToSpeechEnabled"
                :speech-recognition-supported="speechRecognitionSupported"
                :speech-synthesis-supported="speechSynthesisSupported"
                :can-send="canSend"
                :voice-state="voiceState"
                @choice="submitChoice"
                @review-change="requestReviewChange"
                @confirm-review="confirmReview"
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
            </transition>
          </div>

          <transition name="vision-float-fade">
            <aside v-if="contextVisible" class="vision-surface__context-flyout">
              <section class="vision-surface__bubble vision-surface__bubble--state">
                <p class="vision-surface__bubble-title">{{ surfaceStatusLabel }}</p>
                <p class="vision-surface__bubble-copy">{{ surfaceStatusDetail }}</p>
                <div class="vision-surface__bubble-pills">
                  <span v-if="response?.intent">{{ response.intent }}</span>
                  <span v-if="currentSlotLabel">{{ currentSlotLabel }}</span>
                  <span v-if="recentConversations.length">{{ recentConversations.length }}</span>
                </div>
              </section>

              <section v-if="recentConversations.length" class="vision-surface__bubble vision-surface__bubble--recent">
                <div class="vision-surface__recent-scroll">
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
                        <span v-if="conversation.appliedSlotSummaries.length" class="vision-recent-task__applied">
                          <span
                            v-for="slot in conversation.appliedSlotSummaries.slice(0, 2)"
                            :key="slot.slotId"
                            class="vision-recent-task__applied-pill"
                          >
                            {{ slot.label }} · {{ slot.value }}
                          </span>
                        </span>
                      </button>
                    </div>
                  </section>
                </div>
              </section>
            </aside>
          </transition>
        </div>
      </section>
    </main>

  </section>
</template>

<style scoped>
.vision-surface {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #ffffff;
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
  --vision-surface-base-top: #ffffff;
  --vision-surface-base-mid: #ffffff;
  --vision-surface-base-bottom: #ffffff;
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

.vision-surface--results {
  --vision-surface-accent-gradient: linear-gradient(135deg, #82d4c4 0%, #8db4ff 100%);
  --vision-surface-accent-gradient-soft: linear-gradient(135deg, rgba(130, 212, 196, 0.84) 0%, rgba(141, 180, 255, 0.84) 100%);
  --vision-surface-accent-wash: rgba(130, 212, 196, 0.2);
  --vision-surface-orb-core-start: #f4fcfa;
  --vision-surface-orb-core-end: #eef6ff;
  --vision-surface-orb-core-shadow: rgba(111, 173, 166, 0.2);
  --vision-surface-base-top: #f7fcfb;
  --vision-surface-base-mid: #fafcfb;
  --vision-surface-base-bottom: #f2f7f8;
  --vision-surface-wash-one: rgba(130, 212, 196, 0.18);
  --vision-surface-wash-two: rgba(141, 180, 255, 0.14);
  --vision-surface-wash-three: rgba(244, 252, 250, 0.16);
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
  display: none;
}

.vision-surface__shell-utility {
  position: absolute;
  top: 0.8rem;
  right: 0.8rem;
  z-index: 2;
  display: flex;
  justify-content: flex-end;
}

.vision-surface__context-chip {
  appearance: none;
  border: 1px solid rgba(24, 36, 47, 0.06);
  border-radius: 999px;
  width: 2.8rem;
  height: 2.8rem;
  padding: 0;
  background: rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 24px rgba(24, 36, 47, 0.05);
  color: var(--vision-surface-ink);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.vision-surface__context-chip svg {
  width: 1.15rem;
  height: 1.15rem;
  fill: currentColor;
}

.vision-surface__context-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(24, 36, 47, 0.07);
  background: rgba(255, 255, 255, 0.68);
}

.vision-surface__context-chip--active {
  background: rgba(255, 255, 255, 0.74);
}

.vision-surface__stage {
  position: relative;
  z-index: 1;
  width: min(70rem, calc(100vw - 1.6rem));
  min-height: 100vh;
  display: grid;
  place-items: center;
  justify-items: center;
  gap: 0.35rem;
  padding: 0;
  text-align: center;
}

.vision-surface__orb {
  position: absolute;
  inset: 50% auto auto 50%;
  transform: translate(-50%, -50%) scale(1.08);
  width: min(48rem, 96vw);
  opacity: 0.28;
  filter: blur(0.3px) saturate(1.18);
  pointer-events: none;
  z-index: 0;
}

.vision-surface__surface-shell {
  width: 100%;
  display: grid;
  gap: 0.25rem;
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
  backdrop-filter: none;
  position: relative;
  overflow: visible;
}

.vision-surface__surface-layout {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  justify-items: center;
  gap: 0.2rem;
  padding-top: 0;
}

.vision-surface__main-column {
  min-width: 0;
  display: grid;
  gap: 0.3rem;
  width: min(100%, 48rem);
  justify-self: center;
}

.vision-surface__context-flyout {
  position: absolute;
  top: 3.8rem;
  right: 0.8rem;
  width: min(20rem, calc(100vw - 1.6rem));
  display: grid;
  gap: 0.45rem;
  align-content: start;
  max-height: min(40vh, 28rem);
  overflow: auto;
  padding: 0.1rem;
  border-radius: 1.2rem;
  background: rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(24, 36, 47, 0.035);
  box-shadow: 0 14px 32px rgba(24, 36, 47, 0.04);
  backdrop-filter: blur(14px);
}

.vision-surface__bubble {
  display: grid;
  gap: 0.28rem;
  padding: 0.48rem 0.56rem;
  border-radius: 0.95rem;
  background: rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(24, 36, 47, 0.04);
  box-shadow: none;
}

.vision-surface__bubble-title {
  margin: 0;
  color: var(--vision-surface-ink);
  font-size: 0.92rem;
  font-weight: 600;
}

.vision-surface__bubble-copy {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.45;
  font-size: 0.86rem;
}

.vision-surface__bubble-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.vision-surface__bubble-pills span {
  border-radius: 999px;
  padding: 0.24rem 0.5rem;
  background: rgba(255, 255, 255, 0.68);
  font-size: 0.68rem;
  color: var(--vision-surface-ink-soft);
}

.vision-surface__intro {
  max-width: 38rem;
  justify-self: center;
}

.vision-surface__intro-copy {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.65;
  font-size: 1rem;
  max-width: 42rem;
}

.vision-surface__recent-scroll {
  display: grid;
  gap: 0.35rem;
  overflow: auto;
  max-height: min(38vh, 22rem);
  padding-right: 0.1rem;
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
  gap: 0.55rem;
  width: 100%;
  margin-top: 0.55rem;
}

.vision-recent-group--drawer {
  margin-top: 1rem;
}

.vision-recent-group {
  display: grid;
  gap: 0.3rem;
  margin-top: 0;
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
  gap: 0.18rem;
  justify-items: flex-start;
  width: 100%;
  border: 1px solid var(--vision-surface-chip-border);
  border-radius: 1rem;
  padding: 0.62rem 0.68rem;
  background: rgba(255, 255, 255, 0.7);
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
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--vision-surface-ink);
}

.vision-recent-task__applied {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-top: 0.05rem;
}

.vision-recent-task__applied-pill {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  border-radius: 999px;
  padding: 0.28rem 0.55rem;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(24, 36, 47, 0.08);
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  color: var(--vision-surface-ink-soft);
}

.vision-surface__loading {
  position: relative;
  z-index: 1;
  width: fit-content;
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

  .vision-surface__context-flyout {
    position: static;
    width: 100%;
    max-height: none;
    overflow: visible;
    box-shadow: none;
  }

  .vision-surface__orb {
    width: min(38rem, 94vw);
    transform: translate(-50%, -50%) scale(1);
  }
}
</style>
