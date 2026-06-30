<script setup lang="ts">
import {computed} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
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
  currentSlotLabel,
  currentPlaceholder,
  currentMessage,
  attentionState,
  translationWarning,
  speechStatusLabel,
  voiceRuntimeError,
  lastTranscript,
  canSend,
  init,
  processPrompt,
  resetConversation,
  startListening,
  stopListening,
  speakSummary,
  stopSpeaking,
  openComposer,
  closeComposer
} = useVisionConversation()

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

const nextActionLabel = computed(() => {
  if (!response.value) {
    return "Blank"
  }
  if (response.value.nextAction === "SHOW_REVIEW") {
    return "Review"
  }
  if (response.value.nextAction === "BLOCKED") {
    return "Blocked"
  }
  return "Clarify"
})

useMountedAsync(init)
</script>

<template>
  <section class="vision-surface">
    <div class="vision-surface__wash vision-surface__wash--one" aria-hidden="true"></div>
    <div class="vision-surface__wash vision-surface__wash--two" aria-hidden="true"></div>
    <div class="vision-surface__grain" aria-hidden="true"></div>

    <header class="vision-surface__header">
      <span class="vision-surface__brand">Vision Surface</span>
      <div class="vision-surface__chips">
        <span class="vision-surface__chip">{{ nextActionLabel }}</span>
        <span v-if="response?.intent" class="vision-surface__chip">{{ response.intent }}</span>
        <span v-if="currentSlotLabel" class="vision-surface__chip">{{ currentSlotLabel }}</span>
      </div>
    </header>

    <main class="vision-surface__stage">
      <div class="vision-agent" :class="[`vision-agent--${voiceState}`, `vision-agent--${attentionState}`]">
        <span class="vision-agent__halo"></span>
        <span class="vision-agent__ring vision-agent__ring--outer"></span>
        <span class="vision-agent__ring vision-agent__ring--middle"></span>
        <span class="vision-agent__ring vision-agent__ring--inner"></span>
        <span class="vision-agent__pulse"></span>
        <span class="vision-agent__core"></span>
      </div>

      <div class="vision-surface__intro">
        <p class="vision-surface__eyebrow">Adaptive conversation</p>
        <h1>One quiet surface. One next step.</h1>
        <p>{{ agentCaption }}</p>
        <p v-if="translationWarning" class="vision-surface__hint">{{ translationWarning }}</p>
        <p v-if="voiceRuntimeError" class="vision-surface__error">{{ voiceRuntimeError }}</p>
      </div>

      <transition name="vision-panel-fade">
        <section v-if="response" class="vision-panel">
          <div class="vision-panel__section">
            <p class="vision-panel__label">Agent response</p>
            <p class="vision-panel__body">{{ response.message }}</p>
          </div>

          <div v-if="response.slotSummaries.length" class="vision-panel__section">
            <p class="vision-panel__label">Collected so far</p>
            <div class="vision-slot-list">
              <article v-for="slot in response.slotSummaries" :key="slot.slotId" class="vision-slot-card">
                <span class="vision-slot-card__label">{{ slot.label }}</span>
                <p class="vision-slot-card__value">{{ slot.value }}</p>
              </article>
            </div>
          </div>

          <div v-if="response.review" class="vision-panel__section vision-review">
            <p class="vision-panel__label">Review</p>
            <h2>{{ response.review.title }}</h2>
            <p>{{ response.review.description }}</p>
            <dl class="vision-review__grid">
              <div>
                <dt>Reward</dt>
                <dd>{{ response.review.rewardLabel }}</dd>
              </div>
              <div>
                <dt>Visibility</dt>
                <dd>{{ response.review.visibility }}</dd>
              </div>
              <div>
                <dt>Execution</dt>
                <dd>{{ response.executionEnabled ? "Enabled" : "Planning only" }}</dd>
              </div>
            </dl>
          </div>

          <div v-if="lastTranscript" class="vision-panel__section">
            <p class="vision-panel__label">Last transcript</p>
            <p class="vision-panel__body">{{ lastTranscript }}</p>
          </div>
        </section>
      </transition>
    </main>

    <transition name="vision-composer-fade">
      <section v-if="promptComposerVisible" class="vision-composer">
        <div class="vision-composer__header">
          <div>
            <p class="vision-surface__eyebrow">Prompt dock</p>
            <h2>{{ currentSlotLabel || "Speak or type the next turn" }}</h2>
          </div>
          <div class="vision-composer__header-actions">
            <button type="button" class="vision-composer__ghost" @click="resetConversation">New task</button>
            <button type="button" class="vision-composer__ghost" @click="closeComposer">Hide</button>
          </div>
        </div>

        <textarea
          v-model="inputText"
          class="vision-composer__input"
          rows="3"
          :placeholder="currentPlaceholder"
          @focus="openComposer"
        ></textarea>

        <div class="vision-composer__actions">
          <button
            type="button"
            class="vision-composer__action"
            :disabled="!voiceEnabled || !speechToTextEnabled || !speechRecognitionSupported"
            @click="startListening"
          >
            Mic
          </button>
          <button
            type="button"
            class="vision-composer__action"
            :disabled="!response || !voiceEnabled || !textToSpeechEnabled || !speechSynthesisSupported"
            @click="speakSummary"
          >
            Speak
          </button>
          <button
            type="button"
            class="vision-composer__action vision-composer__action--primary"
            :disabled="!canSend"
            @click="submitPrompt"
          >
            Send
          </button>
          <button
            type="button"
            class="vision-composer__action"
            :disabled="voiceState !== 'listening'"
            @click="() => stopListening()"
          >
            Stop mic
          </button>
          <button
            type="button"
            class="vision-composer__action"
            :disabled="voiceState !== 'speaking'"
            @click="stopSpeaking"
          >
            Stop audio
          </button>
        </div>

        <p class="vision-surface__status-text">{{ speechStatusLabel }}</p>
      </section>
    </transition>

    <button
      v-if="!promptComposerVisible"
      type="button"
      class="vision-composer-launcher"
      @click="openComposer"
    >
      Ask agent
    </button>

    <div v-if="isLoading" class="vision-surface__loading">Loading adaptive canvas...</div>
    <div v-else-if="error" class="vision-surface__loading vision-surface__loading--error">{{ error }}</div>
  </section>
</template>

<style scoped>
.vision-surface {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 208, 173, 0.35), transparent 32%),
    radial-gradient(circle at 80% 18%, rgba(157, 214, 255, 0.28), transparent 28%),
    linear-gradient(180deg, #fffdf8 0%, #fbfbf8 45%, #f4f6f8 100%);
  color: #18242f;
}

.vision-surface__wash,
.vision-surface__grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.vision-surface__wash--one {
  background: radial-gradient(circle at 22% 60%, rgba(255, 173, 141, 0.18), transparent 30%);
}

.vision-surface__wash--two {
  background: radial-gradient(circle at 78% 34%, rgba(116, 197, 255, 0.18), transparent 26%);
}

.vision-surface__grain {
  opacity: 0.4;
  background-image: linear-gradient(rgba(24, 36, 47, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(24, 36, 47, 0.025) 1px, transparent 1px);
  background-size: 84px 84px;
  mask-image: radial-gradient(circle at center, black 35%, transparent 90%);
}

.vision-surface__header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.4rem 1.4rem 0;
}

.vision-surface__brand {
  font-size: 0.82rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.58);
}

.vision-surface__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
  justify-content: flex-end;
}

.vision-surface__chip {
  border: 1px solid rgba(24, 36, 47, 0.08);
  border-radius: 999px;
  padding: 0.42rem 0.8rem;
  background: rgba(255, 255, 255, 0.78);
  font-size: 0.78rem;
  color: rgba(24, 36, 47, 0.78);
  box-shadow: 0 12px 32px rgba(24, 36, 47, 0.06);
}

.vision-surface__stage {
  position: relative;
  z-index: 1;
  min-height: 78vh;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 1.6rem;
  padding: 2rem 1.2rem 14rem;
  text-align: center;
}

.vision-agent {
  position: relative;
  width: min(25rem, 76vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
}

.vision-agent__halo,
.vision-agent__ring,
.vision-agent__pulse,
.vision-agent__core {
  position: absolute;
  border-radius: 50%;
}

.vision-agent__halo {
  inset: 8%;
  background: radial-gradient(circle, rgba(118, 190, 255, 0.32), rgba(118, 190, 255, 0.06) 48%, transparent 70%);
  filter: blur(10px);
  animation: halo-breathe 9s ease-in-out infinite;
}

.vision-agent__ring {
  inset: 12%;
  border: 1px solid rgba(24, 36, 47, 0.08);
}

.vision-agent__ring--outer {
  animation: rotate-slow 18s linear infinite;
}

.vision-agent__ring--middle {
  inset: 21%;
  border-style: dashed;
  animation: rotate-reverse 14s linear infinite;
}

.vision-agent__ring--inner {
  inset: 31%;
  border-color: rgba(255, 153, 112, 0.24);
  animation: pulse-ring 7s ease-in-out infinite;
}

.vision-agent__pulse {
  inset: 28%;
  background: radial-gradient(circle, rgba(255, 175, 132, 0.18), rgba(125, 195, 255, 0.08) 58%, transparent 76%);
  animation: pulse-core 5.5s ease-in-out infinite;
}

.vision-agent__core {
  inset: 38%;
  background: linear-gradient(145deg, #fff4ed 0%, #e6f5ff 100%);
  box-shadow:
    0 0 0 1px rgba(24, 36, 47, 0.06),
    0 24px 60px rgba(77, 130, 168, 0.2),
    inset 0 0 32px rgba(255, 255, 255, 0.9);
}

.vision-agent--listening .vision-agent__core {
  box-shadow:
    0 0 0 1px rgba(24, 36, 47, 0.06),
    0 28px 72px rgba(255, 160, 122, 0.26),
    inset 0 0 38px rgba(255, 255, 255, 0.95);
}

.vision-agent--processing .vision-agent__ring--outer,
.vision-agent--processing .vision-agent__ring--middle {
  animation-duration: 7s;
}

.vision-agent--review .vision-agent__core {
  background: linear-gradient(145deg, #fff8f0 0%, #f3fbff 100%);
}

.vision-agent--blocked .vision-agent__core {
  background: linear-gradient(145deg, #fff0ef 0%, #fff8f7 100%);
}

.vision-surface__intro {
  max-width: 42rem;
}

.vision-surface__eyebrow {
  margin: 0 0 0.55rem;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.46);
}

.vision-surface__intro h1 {
  margin: 0 0 0.8rem;
  font-size: clamp(2.2rem, 6vw, 4.8rem);
  line-height: 0.95;
  letter-spacing: -0.05em;
  font-weight: 650;
}

.vision-surface__intro p {
  margin: 0;
  font-size: 1rem;
  line-height: 1.6;
  color: rgba(24, 36, 47, 0.72);
}

.vision-surface__hint {
  margin-top: 0.8rem !important;
  color: #945d2d !important;
}

.vision-surface__error {
  margin-top: 0.8rem !important;
  color: #b24747 !important;
}

.vision-panel {
  width: min(60rem, 100%);
  display: grid;
  gap: 1rem;
  padding: 1.2rem;
  border-radius: 2rem;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(24, 36, 47, 0.06);
  box-shadow: 0 32px 80px rgba(24, 36, 47, 0.08);
  backdrop-filter: blur(20px);
  text-align: left;
}

.vision-panel__section {
  display: grid;
  gap: 0.55rem;
}

.vision-panel__label {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.44);
}

.vision-panel__body {
  margin: 0;
  color: rgba(24, 36, 47, 0.74);
}

.vision-slot-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 0.8rem;
}

.vision-slot-card {
  border-radius: 1.2rem;
  padding: 0.95rem 1rem;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(24, 36, 47, 0.05);
}

.vision-slot-card__label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: rgba(24, 36, 47, 0.46);
}

.vision-slot-card__value {
  margin: 0;
  color: #18242f;
}

.vision-review h2 {
  margin: 0;
  font-size: 1.4rem;
  letter-spacing: -0.03em;
}

.vision-review p {
  margin: 0;
  color: rgba(24, 36, 47, 0.74);
}

.vision-review__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
  gap: 0.8rem;
  margin: 0;
}

.vision-review__grid dt {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: rgba(24, 36, 47, 0.46);
}

.vision-review__grid dd {
  margin: 0.35rem 0 0;
  color: #18242f;
}

.vision-composer {
  position: fixed;
  left: 50%;
  bottom: 1.2rem;
  z-index: 2;
  width: min(56rem, calc(100vw - 1.4rem));
  transform: translateX(-50%);
  border-radius: 2rem;
  padding: 1.1rem;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(24, 36, 47, 0.08);
  box-shadow: 0 28px 80px rgba(24, 36, 47, 0.12);
  backdrop-filter: blur(24px);
}

.vision-composer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.9rem;
}

.vision-composer__header h2 {
  margin: 0;
  font-size: 1.05rem;
  letter-spacing: -0.02em;
}

.vision-composer__header-actions {
  display: flex;
  gap: 0.55rem;
}

.vision-composer__ghost,
.vision-composer__action,
.vision-composer-launcher {
  appearance: none;
  border: 0;
  border-radius: 999px;
  padding: 0.72rem 1rem;
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease;
}

.vision-composer__ghost,
.vision-composer__action {
  background: rgba(24, 36, 47, 0.06);
  color: #18242f;
}

.vision-composer__action--primary {
  background: linear-gradient(135deg, #ff9d73 0%, #7fcbff 100%);
  color: #10202c;
}

.vision-composer__ghost:hover,
.vision-composer__action:hover,
.vision-composer-launcher:hover {
  transform: translateY(-1px);
}

.vision-composer__action:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  transform: none;
}

.vision-composer__input {
  width: 100%;
  min-height: 7.5rem;
  resize: vertical;
  border: 0;
  outline: none;
  border-radius: 1.4rem;
  padding: 1rem 1.1rem;
  background: rgba(248, 250, 252, 0.92);
  color: #18242f;
  box-shadow: inset 0 0 0 1px rgba(24, 36, 47, 0.06);
}

.vision-composer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  margin-top: 0.9rem;
}

.vision-surface__status-text {
  margin: 0.9rem 0 0;
  font-size: 0.92rem;
  color: rgba(24, 36, 47, 0.58);
}

.vision-composer-launcher {
  position: fixed;
  left: 50%;
  bottom: 1.25rem;
  z-index: 2;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  color: #18242f;
  box-shadow: 0 22px 64px rgba(24, 36, 47, 0.1);
}

.vision-surface__loading {
  position: fixed;
  left: 50%;
  bottom: 7.8rem;
  transform: translateX(-50%);
  z-index: 2;
  padding: 0.65rem 1rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: rgba(24, 36, 47, 0.72);
  box-shadow: 0 18px 50px rgba(24, 36, 47, 0.08);
}

.vision-surface__loading--error {
  color: #a34040;
}

.vision-panel-fade-enter-active,
.vision-panel-fade-leave-active,
.vision-composer-fade-enter-active,
.vision-composer-fade-leave-active {
  transition: opacity 220ms ease, transform 220ms ease;
}

.vision-panel-fade-enter-from,
.vision-panel-fade-leave-to,
.vision-composer-fade-enter-from,
.vision-composer-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes rotate-reverse {
  from { transform: rotate(360deg); }
  to { transform: rotate(0deg); }
}

@keyframes halo-breathe {
  0%, 100% { transform: scale(0.96); opacity: 0.72; }
  50% { transform: scale(1.04); opacity: 1; }
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(0.96); opacity: 0.45; }
  50% { transform: scale(1.04); opacity: 0.9; }
}

@keyframes pulse-core {
  0%, 100% { transform: scale(0.95); opacity: 0.72; }
  50% { transform: scale(1.03); opacity: 1; }
}

@media (max-width: 720px) {
  .vision-surface__header,
  .vision-composer__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .vision-surface__chips,
  .vision-composer__header-actions,
  .vision-composer__actions {
    width: 100%;
  }

  .vision-composer {
    bottom: 0.75rem;
    width: calc(100vw - 0.9rem);
    border-radius: 1.5rem;
  }

  .vision-surface__stage {
    padding-bottom: 17rem;
  }
}
</style>
