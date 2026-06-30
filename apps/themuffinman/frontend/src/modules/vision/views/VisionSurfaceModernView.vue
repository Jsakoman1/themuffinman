<script setup lang="ts">
import {computed} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import {useVisionSurface} from "../composables/useVisionSurface.ts"

const {
  isLoading,
  error,
  voiceState,
  surfaceMode,
  activeFilter,
  recognizedPrompt,
  visibleJobsLabel,
  voiceEnabled,
  speechToTextEnabled,
  textToSpeechEnabled,
  speechRecognitionSupported,
  speechSynthesisSupported,
  promptComposerVisible,
  agentAttentionLevel,
  speechStatusLabel,
  voiceRuntimeError,
  speechSummary,
  init,
  startListening,
  stopListening,
  speakSummary,
  stopSpeaking,
  processPrompt,
  openComposer,
  closeComposer
} = useVisionSurface()

const surfaceModeLabel = computed(() => {
  if (surfaceMode.value === "compare") {
    return "Compare"
  }

  if (surfaceMode.value === "focus") {
    return "Focus"
  }

  return "Browse"
})

const activeFilterLabel = computed(() => {
  if (activeFilter.value === "today") {
    return "Today"
  }

  if (activeFilter.value === "nearby") {
    return "Nearby"
  }

  return "Best match"
})

const agentCaption = computed(() => {
  if (voiceState.value === "listening") {
    return "Listening for intent."
  }

  if (voiceState.value === "processing") {
    return "Decoding prompt with backend agent planning and OpenAI."
  }

  if (voiceState.value === "speaking") {
    return "Speaking the agent response."
  }

  if (speechSummary.value.trim().length > 0) {
    return speechSummary.value
  }

  return "The agent is waiting for a prompt."
})

const submitPrompt = async () => {
  await processPrompt(recognizedPrompt.value, "text")
}

const clearPrompt = () => {
  recognizedPrompt.value = ""
  speechSummary.value = ""
  closeComposer()
}

useMountedAsync(init)
</script>

<template>
  <section class="vision-surface vision-surface--modern">
    <div class="vision-surface__backdrop vision-surface__backdrop--left" aria-hidden="true"></div>
    <div class="vision-surface__backdrop vision-surface__backdrop--right" aria-hidden="true"></div>
    <div class="vision-surface__mesh" aria-hidden="true"></div>

    <header class="vision-surface__topline">
      <span class="vision-surface__brand">Vision Agent</span>
      <div class="vision-surface__status-chips">
        <span class="vision-surface__chip vision-surface__chip--soft">{{ visibleJobsLabel }} jobs</span>
        <span class="vision-surface__chip vision-surface__chip--soft">{{ surfaceModeLabel }}</span>
        <span class="vision-surface__chip vision-surface__chip--soft">{{ activeFilterLabel }}</span>
      </div>
    </header>

    <main class="vision-surface__stage">
      <div class="vision-agent" :class="[`vision-agent--${voiceState}`, `vision-agent--${agentAttentionLevel}`]">
        <span class="vision-agent__halo"></span>
        <span class="vision-agent__orbit vision-agent__orbit--one"></span>
        <span class="vision-agent__orbit vision-agent__orbit--two"></span>
        <span class="vision-agent__pulse"></span>
        <span class="vision-agent__core"></span>
      </div>

      <div class="vision-surface__caption">
        <p class="vision-surface__eyebrow">Adaptive agent</p>
        <h1>The screen stays calm until intent arrives.</h1>
        <p>{{ agentCaption }}</p>
        <p v-if="voiceRuntimeError" class="vision-surface__inline-error">{{ voiceRuntimeError }}</p>
      </div>
    </main>

    <transition name="vision-composer-fade">
      <section v-if="promptComposerVisible" class="vision-composer">
        <div class="vision-composer__header">
          <div>
            <p class="vision-surface__eyebrow">Prompt field</p>
            <h2>Ask in text or speak first, the backend uses one ingest path.</h2>
          </div>
          <button type="button" class="vision-composer__ghost" @click="closeComposer">Hide</button>
        </div>

        <textarea
          v-model="recognizedPrompt"
          class="vision-composer__input"
          rows="3"
          placeholder="Describe what you want the agent to do"
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
            :disabled="!voiceEnabled || !textToSpeechEnabled || !speechSynthesisSupported"
            @click="speakSummary"
          >
            Speak
          </button>
          <button type="button" class="vision-composer__action vision-composer__action--primary" @click="submitPrompt">
            Send
          </button>
          <button type="button" class="vision-composer__action" @click="clearPrompt">
            Clear
          </button>
          <button type="button" class="vision-composer__action" :disabled="voiceState !== 'listening'" @click="() => stopListening()">
            Stop mic
          </button>
          <button type="button" class="vision-composer__action" :disabled="voiceState !== 'speaking'" @click="stopSpeaking">
            Stop audio
          </button>
        </div>

        <div class="vision-composer__result">
          <span class="vision-composer__result-label">Decoded result</span>
          <p>{{ speechSummary || "The backend response will appear here after processing." }}</p>
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
.vision-surface--modern {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top, rgba(255, 255, 255, 0.18), transparent 32%),
    linear-gradient(180deg, #08111f 0%, #0b1526 48%, #07101b 100%);
  color: #f3f7ff;
}

.vision-surface__backdrop {
  position: absolute;
  border-radius: 999px;
  filter: blur(8px);
  opacity: 0.75;
  pointer-events: none;
}

.vision-surface__backdrop--left {
  inset: 12% auto auto -10%;
  width: 22rem;
  height: 22rem;
  background: radial-gradient(circle, rgba(99, 179, 237, 0.32), transparent 68%);
  animation: float-left 14s ease-in-out infinite;
}

.vision-surface__backdrop--right {
  inset: auto -8% 15% auto;
  width: 26rem;
  height: 26rem;
  background: radial-gradient(circle, rgba(255, 154, 120, 0.24), transparent 68%);
  animation: float-right 16s ease-in-out infinite;
}

.vision-surface__mesh {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 88px 88px;
  mask-image: radial-gradient(circle at center, black 28%, transparent 82%);
  opacity: 0.55;
  pointer-events: none;
}

.vision-surface__topline {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.2rem 1.4rem 0;
}

.vision-surface__brand {
  font-size: 0.8rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(220, 232, 255, 0.72);
}

.vision-surface__status-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: flex-end;
}

.vision-surface__chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(172, 196, 255, 0.18);
  border-radius: 999px;
  padding: 0.45rem 0.8rem;
  background: rgba(8, 16, 30, 0.6);
  color: #f3f7ff;
}

.vision-surface__chip--soft {
  font-size: 0.8rem;
  color: rgba(235, 242, 255, 0.9);
}

.vision-surface__stage {
  position: relative;
  z-index: 1;
  min-height: 72vh;
  display: grid;
  place-items: center;
  text-align: center;
  padding: 3rem 1.2rem 12rem;
}

.vision-agent {
  position: relative;
  width: min(28rem, 82vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  margin-bottom: 1.5rem;
}

.vision-agent__halo,
.vision-agent__orbit,
.vision-agent__pulse,
.vision-agent__core {
  position: absolute;
  inset: 0;
  border-radius: 50%;
}

.vision-agent__halo {
  inset: 8%;
  background: radial-gradient(circle, rgba(85, 174, 255, 0.22), rgba(85, 174, 255, 0.04) 46%, transparent 70%);
  filter: blur(8px);
  animation: halo 8s ease-in-out infinite;
}

.vision-agent__orbit {
  inset: 12%;
  border: 1px solid rgba(180, 207, 255, 0.12);
  box-shadow: inset 0 0 42px rgba(111, 176, 255, 0.06);
}

.vision-agent__orbit--one {
  animation: spin 18s linear infinite;
}

.vision-agent__orbit--two {
  inset: 20%;
  animation: spin-reverse 26s linear infinite;
}

.vision-agent__pulse {
  inset: 26%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.9) 0%, rgba(111, 176, 255, 0.72) 22%, rgba(43, 84, 148, 0.1) 58%, transparent 74%);
  box-shadow:
    0 0 48px rgba(95, 168, 255, 0.55),
    inset 0 0 60px rgba(255, 255, 255, 0.26);
  animation: pulse 5s ease-in-out infinite;
}

.vision-agent__core {
  inset: 36%;
  background:
    radial-gradient(circle at 35% 35%, #ffffff 0%, #d8e7ff 24%, #7bc5ff 58%, #37518e 100%);
  box-shadow:
    0 0 30px rgba(106, 184, 255, 0.8),
    0 0 90px rgba(106, 184, 255, 0.22);
  animation: core-breathe 4s ease-in-out infinite;
}

.vision-agent--listening .vision-agent__pulse {
  animation-duration: 2.8s;
  box-shadow:
    0 0 58px rgba(103, 209, 255, 0.72),
    inset 0 0 60px rgba(255, 255, 255, 0.3);
}

.vision-agent--processing .vision-agent__core {
  background:
    radial-gradient(circle at 35% 35%, #fff8f0 0%, #ffd3b6 26%, #ff985f 60%, #8a3f1b 100%);
}

.vision-agent--speaking .vision-agent__pulse {
  box-shadow:
    0 0 72px rgba(255, 152, 111, 0.7),
    inset 0 0 60px rgba(255, 255, 255, 0.32);
}

.vision-agent--ready .vision-agent__halo {
  animation-duration: 10s;
}

.vision-agent--quiet .vision-agent__pulse {
  opacity: 0.85;
}

.vision-surface__caption {
  position: relative;
  max-width: 42rem;
  display: grid;
  gap: 0.6rem;
}

.vision-surface__eyebrow {
  margin: 0;
  font-size: 0.78rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(201, 218, 255, 0.68);
}

.vision-surface__caption h1 {
  margin: 0;
  font-size: clamp(2rem, 4vw, 3.9rem);
  line-height: 0.96;
  letter-spacing: -0.05em;
}

.vision-surface__caption p {
  margin: 0;
  color: rgba(226, 234, 247, 0.78);
}

.vision-surface__inline-error {
  color: #ffb7b7;
}

.vision-composer {
  position: fixed;
  inset: auto 1rem 1rem;
  z-index: 2;
  margin: 0 auto;
  max-width: min(52rem, calc(100vw - 2rem));
  border: 1px solid rgba(173, 196, 255, 0.2);
  border-radius: 1.5rem;
  background: rgba(9, 16, 29, 0.84);
  backdrop-filter: blur(22px);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.36);
  padding: 1rem;
}

.vision-composer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.9rem;
}

.vision-composer__header h2 {
  margin: 0.2rem 0 0;
  font-size: 1.05rem;
  line-height: 1.25;
}

.vision-composer__ghost {
  border: 1px solid rgba(173, 196, 255, 0.2);
  border-radius: 999px;
  padding: 0.5rem 0.8rem;
  background: transparent;
  color: #f3f7ff;
}

.vision-composer__input {
  width: 100%;
  min-height: 6.5rem;
  border: 1px solid rgba(173, 196, 255, 0.16);
  border-radius: 1.1rem;
  background: rgba(5, 10, 18, 0.8);
  color: #f3f7ff;
  padding: 0.9rem 1rem;
  resize: none;
  outline: none;
  font: inherit;
}

.vision-composer__input:focus {
  border-color: rgba(117, 184, 255, 0.8);
  box-shadow: 0 0 0 3px rgba(117, 184, 255, 0.12);
}

.vision-composer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-top: 0.9rem;
}

.vision-composer__action {
  border: 1px solid rgba(173, 196, 255, 0.18);
  border-radius: 999px;
  padding: 0.6rem 0.95rem;
  background: rgba(255, 255, 255, 0.04);
  color: #f3f7ff;
}

.vision-composer__action--primary {
  background: linear-gradient(135deg, #84c8ff 0%, #6f96ff 100%);
  color: #07101b;
  font-weight: 600;
}

.vision-composer__result {
  margin-top: 1rem;
  border: 1px solid rgba(173, 196, 255, 0.12);
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.03);
  padding: 0.85rem 1rem;
}

.vision-composer__result-label {
  display: block;
  margin-bottom: 0.3rem;
  font-size: 0.76rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(201, 218, 255, 0.7);
}

.vision-composer__result p {
  margin: 0;
  color: rgba(236, 243, 255, 0.88);
}

.vision-surface__status-text {
  margin: 0.8rem 0 0;
  font-size: 0.9rem;
  color: rgba(201, 218, 255, 0.76);
}

.vision-composer-launcher {
  position: fixed;
  left: 50%;
  bottom: 1rem;
  transform: translateX(-50%);
  z-index: 2;
  border: 1px solid rgba(173, 196, 255, 0.22);
  border-radius: 999px;
  padding: 0.8rem 1.1rem;
  background: rgba(9, 16, 29, 0.86);
  color: #f3f7ff;
  backdrop-filter: blur(20px);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.34);
}

.vision-surface__loading {
  position: fixed;
  top: 1rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  border-radius: 999px;
  padding: 0.65rem 1rem;
  background: rgba(9, 16, 29, 0.86);
  color: rgba(243, 247, 255, 0.92);
  border: 1px solid rgba(173, 196, 255, 0.18);
}

.vision-surface__loading--error {
  color: #ffb7b7;
}

.vision-composer-fade-enter-active,
.vision-composer-fade-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.vision-composer-fade-enter-from,
.vision-composer-fade-leave-to {
  opacity: 0;
  transform: translateY(18px);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes spin-reverse {
  from { transform: rotate(0deg); }
  to { transform: rotate(-360deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(0.97); }
  50% { transform: scale(1.04); }
}

@keyframes core-breathe {
  0%, 100% { transform: scale(0.98); }
  50% { transform: scale(1.08); }
}

@keyframes halo {
  0%, 100% { transform: scale(1); opacity: 0.72; }
  50% { transform: scale(1.05); opacity: 0.92; }
}

@keyframes float-left {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(2rem, 2rem, 0); }
}

@keyframes float-right {
  0%, 100% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(-2rem, -1.5rem, 0); }
}

@media (max-width: 820px) {
  .vision-surface__topline {
    flex-direction: column;
    align-items: flex-start;
  }

  .vision-surface__stage {
    min-height: 64vh;
    padding-bottom: 14rem;
  }

  .vision-composer {
    inset-inline: 0.75rem;
    max-width: none;
  }
}
</style>
