<script setup lang="ts">
import {computed, ref} from "vue"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"

const props = defineProps<{
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  speechRecognitionSupported: boolean
  voiceState: VisionVoiceState
  compact?: boolean
}>()

const emit = defineEmits<{
  startListening: []
  stopListening: []
}>()

const pressActive = ref(false)

const canRecord = computed(() => props.voiceEnabled && props.speechToTextEnabled && props.speechRecognitionSupported)
const isListening = computed(() => props.voiceState === "listening")
const isProcessing = computed(() => props.voiceState === "processing")
const isSpeaking = computed(() => props.voiceState === "speaking")

const handlePointerDown = () => {
  if (!canRecord.value) {
    return
  }
  pressActive.value = true
  emit("startListening")
}

const handlePointerUp = () => {
  if (!pressActive.value) {
    return
  }
  pressActive.value = false
  emit("stopListening")
}

const handlePointerCancel = () => {
  if (!pressActive.value) {
    return
  }
  pressActive.value = false
  emit("stopListening")
}

const handlePointerLeave = () => {
  if (!pressActive.value) {
    return
  }
  pressActive.value = false
  emit("stopListening")
}
</script>

<template>
  <section class="vision-voice" :class="{
    'vision-voice--recording': isListening,
    'vision-voice--processing': isProcessing,
    'vision-voice--speaking': isSpeaking,
    'vision-voice--compact': compact
  }">
    <button
      type="button"
      class="vision-voice__button"
      :class="{
        'vision-voice__button--active': isListening,
        'vision-voice__button--processing': isProcessing,
        'vision-voice__button--compact': compact
      }"
      :disabled="!canRecord || isProcessing"
      :aria-label="isListening ? 'Release to stop recording' : 'Hold to record'"
      @pointerdown.prevent="handlePointerDown"
      @pointerup="handlePointerUp"
      @pointercancel="handlePointerCancel"
      @pointerleave="handlePointerLeave"
    >
      <span v-if="isProcessing" class="vision-voice__hourglass" aria-hidden="true"></span>
      <svg v-else viewBox="0 0 24 24" aria-hidden="true">
        <path d="M12 14.5c1.93 0 3.5-1.57 3.5-3.5V6.5C15.5 4.57 13.93 3 12 3S8.5 4.57 8.5 6.5V11c0 1.93 1.57 3.5 3.5 3.5Zm6-3.5a1 1 0 0 0-2 0 4 4 0 0 1-8 0 1 1 0 0 0-2 0 6 6 0 0 0 5 5.91V19H9a1 1 0 0 0 0 2h6a1 1 0 0 0 0-2h-2v-2.09A6 6 0 0 0 18 11Z" />
      </svg>
    </button>

    <p v-if="!compact" class="vision-voice__caption">
      <span v-if="isListening">Recording. Release to send.</span>
      <span v-else-if="isProcessing">Processing...</span>
      <span v-else-if="isSpeaking">Speaking response...</span>
      <span v-else-if="canRecord">Hold to talk.</span>
      <span v-else>Voice unavailable.</span>
    </p>
  </section>
</template>

<style scoped>
.vision-voice {
  display: grid;
  justify-items: center;
  gap: 0.55rem;
  width: 100%;
  min-height: 0;
}

.vision-voice--compact {
  gap: 0;
  width: auto;
  justify-items: center;
  align-self: center;
  transform: translateY(0.08rem);
}

.vision-voice__button {
  appearance: none;
  border: 0;
  width: min(9rem, 100%);
  aspect-ratio: 1;
  border-radius: 1.35rem;
  display: grid;
  place-items: center;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(241, 244, 238, 0.98));
  box-shadow:
    0 24px 54px rgba(24, 36, 47, 0.1),
    inset 0 0 0 1px rgba(24, 36, 47, 0.08);
  color: var(--vision-surface-ink);
  cursor: pointer;
  touch-action: none;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease;
}

.vision-voice__button svg {
  width: 2.15rem;
  height: 2.15rem;
  fill: currentColor;
}

.vision-voice__button--compact {
  width: auto;
  min-width: auto;
  height: auto;
  aspect-ratio: auto;
  border-radius: 0;
  padding: 0;
  background: transparent;
  box-shadow: none;
  color: rgba(24, 36, 47, 0.62);
  line-height: 1;
}

.vision-voice__button--compact svg {
  width: 0.95rem;
  height: 0.95rem;
  display: block;
}

.vision-voice__button--compact:hover,
.vision-voice__button--compact:focus-visible {
  transform: none;
  box-shadow: none;
  color: rgba(24, 36, 47, 0.92);
}

.vision-voice__button:hover,
.vision-voice__button:focus-visible {
  transform: translateY(-1px) scale(1.01);
  box-shadow:
    0 34px 88px rgba(24, 36, 47, 0.14),
    inset 0 0 0 1px rgba(24, 36, 47, 0.09);
}

.vision-voice__button--active {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 244, 237, 0.92));
  box-shadow:
    0 26px 58px rgba(255, 156, 112, 0.16),
    inset 0 0 0 1px rgba(255, 156, 112, 0.16);
}

.vision-voice__button--processing {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(243, 246, 249, 0.95));
}

.vision-voice__hourglass {
  position: relative;
  width: 2.3rem;
  height: 2.3rem;
  display: inline-block;
  border: 0.18rem solid currentColor;
  border-top-left-radius: 0.9rem;
  border-top-right-radius: 0.9rem;
  border-bottom-left-radius: 0.9rem;
  border-bottom-right-radius: 0.9rem;
  clip-path: polygon(0 0, 100% 0, 68% 50%, 100% 100%, 0 100%, 32% 50%);
  animation: visionHourglassSpin 1.1s linear infinite;
}

.vision-voice__caption {
  margin: 0;
  color: var(--vision-surface-ink-muted);
  font-size: 0.7rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

@keyframes visionHourglassSpin {
  from {
    transform: rotate(0deg) scale(1);
  }

  to {
    transform: rotate(360deg) scale(1);
  }
}
</style>
