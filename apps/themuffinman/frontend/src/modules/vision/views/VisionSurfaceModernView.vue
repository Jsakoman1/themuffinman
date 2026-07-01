<script setup lang="ts">
import {computed} from "vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import VisionAgentOrb from "../components/VisionAgentOrb.vue"
import VisionCanvasRenderer from "../components/VisionCanvasRenderer.vue"
import VisionIntentPreviewPanel from "../components/VisionIntentPreviewPanel.vue"
import {useVisionConversation} from "../composables/useVisionConversation.ts"

const {
  isLoading,
  error,
  inputText,
  response,
  voiceState,
  attentionState,
  voiceEnabled,
  speechToTextEnabled,
  speechRecognitionSupported,
  displayBlocks,
  currentSlotLabel,
  transcriptTargetLabel,
  currentPlaceholder,
  currentFieldKind,
  lastTranscript,
  canSend,
  init,
  processPrompt,
  confirmReview,
  requestReviewChange,
  startListening,
  stopListening,
  openComposer,
  cancelConversation
} = useVisionConversation()

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
))

const surfaceMotionActive = computed(() => (
  isLoading.value
  || voiceState.value === "listening"
  || voiceState.value === "processing"
  || voiceState.value === "speaking"
  || inputText.value.trim().length > 0
))

const typingIntensity = computed(() => Math.min(inputText.value.trim().length / 110, 1))
const voiceIntensity = computed(() => {
  if (voiceState.value === "processing") {
    return 1
  }
  if (voiceState.value === "listening") {
    return 0.82
  }
  if (voiceState.value === "speaking") {
    return 0.72
  }
  return isLoading.value ? 0.5 : 0
})
const orbIntensity = computed(() => Math.max(typingIntensity.value, voiceIntensity.value, isLoading.value ? 0.42 : 0))
const motionIntensity = computed(() => Math.min(Math.max(typingIntensity.value * 0.85, voiceIntensity.value, isLoading.value ? 0.55 : 0), 1))
const motionParticles = computed(() => Array.from({length: 24}, (_, index) => ({
  id: index,
  left: `${(index * 13 + 7) % 100}%`,
  top: `${(index * 19 + 11) % 100}%`,
  size: `${0.42 + ((index % 5) * 0.14)}rem`,
  duration: `${Math.max(5.8, 8.4 - (motionIntensity.value * 1.8) + (index % 7) * 0.55)}s`,
  delay: `${index * -0.7}s`,
  travelX: `${(index % 2 === 0 ? 1 : -1) * (2.1 + (index % 4) * 0.75 + motionIntensity.value * 1.4)}rem`,
  travelY: `${(index % 3 === 0 ? -1 : 1) * (1.9 + (index % 5) * 0.62 + motionIntensity.value * 1.1)}rem`,
  blur: `${9 + (index % 4) * 3.5}px`
})))

useMountedAsync(init)
</script>

<template>
  <section class="vision-surface">
    <main
      class="vision-surface__stage"
      :style="{
        '--vision-surface-ambient-opacity': String(0.78 - (motionIntensity * 0.1)),
        '--vision-surface-particle-opacity': String(0.82 - (motionIntensity * 0.08)),
        '--vision-surface-motion-speed': `${Math.max(0.9, 1.15 - (motionIntensity * 0.18))}`,
        '--vision-surface-motion-scale': `${1 + (motionIntensity * 0.05)}`
      }"
    >
      <div class="vision-surface__ambient" aria-hidden="true">
        <VisionAgentOrb
          :voice-state="voiceState"
          :attention-state="attentionState"
          :active="surfaceMotionActive"
          :intensity="orbIntensity"
          :voice-intensity="voiceIntensity"
          :typing-intensity="typingIntensity"
        />
      </div>

      <div v-if="surfaceMotionActive" class="vision-surface__particles" aria-hidden="true">
        <span
          v-for="particle in motionParticles"
          :key="particle.id"
          class="vision-surface__particle"
          :class="`vision-surface__particle--${particle.id % 8}`"
          :style="{
            left: particle.left,
            top: particle.top,
            width: particle.size,
            height: particle.size,
            animationDuration: particle.duration,
            animationDelay: particle.delay,
            '--vision-particle-travel-x': particle.travelX,
            '--vision-particle-travel-y': particle.travelY,
            '--vision-particle-blur': particle.blur
          }"
        ></span>
      </div>

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
          :transcript-target-label="transcriptTargetLabel"
          :current-field-kind="currentFieldKind"
          :current-placeholder="currentPlaceholder"
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
}

.vision-surface__console {
  display: grid;
  height: 100%;
  position: relative;
  z-index: 2;
}

.vision-surface__ambient {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  z-index: 0;
  pointer-events: none;
  transform: translateY(-2%);
  opacity: 1;
  filter: saturate(1.2) brightness(1.05);
}

.vision-surface__ambient :deep(.vision-agent) {
  width: min(92rem, 132vw);
  opacity: var(--vision-surface-ambient-opacity, 0.78);
  filter: blur(0) saturate(1.22) brightness(1.06);
  animation-duration: calc(11s * var(--vision-surface-motion-speed, 1));
}

.vision-surface__particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 1;
  mix-blend-mode: multiply;
}

.vision-surface__particle {
  position: absolute;
  border-radius: 999px;
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(255, 221, 150, 0.86), transparent 62%);
  box-shadow:
    0 0 22px rgba(127, 203, 255, 0.32),
    0 0 34px rgba(255, 174, 137, 0.18);
  opacity: var(--vision-surface-particle-opacity, 0.74);
  filter: blur(var(--vision-particle-blur, 12px));
  animation-timing-function: ease-in-out;
  animation-fill-mode: both;
}

.vision-surface__particle--0 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(255, 226, 140, 0.88), transparent 62%);
}

.vision-surface__particle--1 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(255, 178, 162, 0.88), transparent 62%);
}

.vision-surface__particle--2 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(147, 223, 180, 0.88), transparent 62%);
}

.vision-surface__particle--3 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(142, 209, 255, 0.88), transparent 62%);
}

.vision-surface__particle--4 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(255, 232, 188, 0.88), transparent 62%);
}

.vision-surface__particle--5 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(255, 190, 170, 0.88), transparent 62%);
}

.vision-surface__particle--6 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(167, 236, 193, 0.88), transparent 62%);
}

.vision-surface__particle--7 {
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.98), transparent 34%),
    radial-gradient(circle at 50% 50%, rgba(177, 214, 255, 0.9), transparent 62%);
}

@keyframes particle-drift-0 {
  0%, 100% { transform: translate(0, 0) scale(0.75); opacity: 0; }
  20% { opacity: 0.62; }
  50% { transform: translate(var(--vision-particle-travel-x), var(--vision-particle-travel-y)) scale(1.28); opacity: 0.98; }
  80% { opacity: 0.18; }
}

@keyframes particle-drift-1 {
  0%, 100% { transform: translate(0, 0) scale(0.7); opacity: 0; }
  18% { opacity: 0.55; }
  55% { transform: translate(calc(var(--vision-particle-travel-x) * -1), calc(var(--vision-particle-travel-y) * 1.1)) scale(1.22); opacity: 0.92; }
  82% { opacity: 0.12; }
}

@keyframes particle-drift-2 {
  0%, 100% { transform: translate(0, 0) scale(0.72); opacity: 0; }
  25% { opacity: 0.6; }
  50% { transform: translate(calc(var(--vision-particle-travel-x) * 1.2), calc(var(--vision-particle-travel-y) * 0.8)) scale(1.25); opacity: 0.88; }
  75% { opacity: 0.14; }
}

@keyframes particle-drift-3 {
  0%, 100% { transform: translate(0, 0) scale(0.68); opacity: 0; }
  18% { opacity: 0.5; }
  60% { transform: translate(calc(var(--vision-particle-travel-x) * -1), calc(var(--vision-particle-travel-y) * -1)) scale(1.26); opacity: 0.94; }
  84% { opacity: 0.12; }
}

@keyframes particle-drift-4 {
  0%, 100% { transform: translate(0, 0) scale(0.75); opacity: 0; }
  22% { opacity: 0.58; }
  52% { transform: translate(calc(var(--vision-particle-travel-x) * 0.8), calc(var(--vision-particle-travel-y) * -0.8)) scale(1.28); opacity: 0.9; }
  78% { opacity: 0.08; }
}

@keyframes particle-drift-5 {
  0%, 100% { transform: translate(0, 0) scale(0.7); opacity: 0; }
  25% { opacity: 0.54; }
  58% { transform: translate(calc(var(--vision-particle-travel-x) * -0.9), calc(var(--vision-particle-travel-y) * 0.9)) scale(1.24); opacity: 0.92; }
  86% { opacity: 0.1; }
}

@keyframes particle-drift-6 {
  0%, 100% { transform: translate(0, 0) scale(0.74); opacity: 0; }
  20% { opacity: 0.62; }
  54% { transform: translate(calc(var(--vision-particle-travel-x) * 1.1), calc(var(--vision-particle-travel-y) * 1.1)) scale(1.22); opacity: 0.96; }
  82% { opacity: 0.14; }
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
