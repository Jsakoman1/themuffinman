<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"

const props = defineProps<{
  visible: boolean
  currentSlotLabel: string
  transcriptTargetLabel: string
  currentFieldKind: string
  currentPlaceholder: string
  inputText: string
  lastTranscript: string
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  textToSpeechEnabled: boolean
  speechRecognitionSupported: boolean
  speechSynthesisSupported: boolean
  hasResponse: boolean
  canSend: boolean
  voiceState: VisionVoiceState
}>()

const emit = defineEmits<{
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

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const updateInputText = (event: Event) => {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  emit("update:inputText", target.value)
}

const focusComposerField = async () => {
  if (!props.visible) {
    return
  }

  await nextTick()
  textareaRef.value?.focus()
}

watch(
  () => [props.visible, props.currentSlotLabel, props.currentFieldKind],
  () => {
    void focusComposerField()
  },
  {immediate: true}
)

onMounted(() => {
  void focusComposerField()
})

const textareaRows = computed(() => {
  const lineCount = Math.max(1, props.inputText.split("\n").length)
  const estimatedRows = Math.ceil(Math.max(props.inputText.length, 1) / 56) + lineCount - 1
  return Math.min(12, Math.max(4, estimatedRows))
})

const composerToneClass = computed(() => {
  if (props.currentFieldKind === "date" || props.currentFieldKind === "time") {
    return "vision-composer--structured"
  }

  if (props.inputText.trim().length > 140 || props.inputText.includes("\n")) {
    return "vision-composer--expanded"
  }

  if (props.inputText.trim().length > 0) {
    return "vision-composer--active"
  }

  return "vision-composer--compact"
})

const transcriptSignalLabel = computed(() => {
  if (props.voiceState === "processing") {
    return "Mapping"
  }
  if (props.voiceState === "listening") {
    return "Heard"
  }
  if (props.voiceState === "speaking") {
    return "Saying"
  }
  return "Heard"
})

const showStopMicAction = computed(() => props.voiceState === "listening")
</script>

<template>
  <section
    v-if="visible"
    class="vision-composer"
    :class="[composerToneClass, { 'vision-composer--idle': !hasResponse, 'vision-composer--inline': hasResponse }]"
  >
    <div class="vision-composer__bar">
      <button
        type="button"
        class="vision-composer__icon-button"
        :disabled="!voiceEnabled || !speechToTextEnabled || !speechRecognitionSupported"
        :aria-label="showStopMicAction ? 'Stop recording' : 'Start recording'"
        @click="showStopMicAction ? emit('stopListening') : emit('startListening')"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M12 14.5c1.93 0 3.5-1.57 3.5-3.5V6.5C15.5 4.57 13.93 3 12 3S8.5 4.57 8.5 6.5V11c0 1.93 1.57 3.5 3.5 3.5Zm6-3.5a1 1 0 0 0-2 0 4 4 0 0 1-8 0 1 1 0 0 0-2 0 6 6 0 0 0 5 5.91V19H9a1 1 0 0 0 0 2h6a1 1 0 0 0 0-2h-2v-2.09A6 6 0 0 0 18 11Z" />
        </svg>
      </button>

      <textarea
        ref="textareaRef"
        :value="inputText"
        class="vision-composer__input"
        :rows="textareaRows"
        :placeholder="currentPlaceholder || 'Type or speak'"
        @focus="emit('open')"
        @input="updateInputText"
      ></textarea>

      <button
        type="button"
        class="vision-composer__icon-button vision-composer__icon-button--primary"
        :disabled="!canSend"
        aria-label="Send"
        @click="emit('submit')"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M3.4 20.6 21 12 3.4 3.4 3 10l10 2-10 2z" />
        </svg>
      </button>
    </div>
    <div v-if="lastTranscript || currentSlotLabel" class="vision-composer__notes">
      <span v-if="lastTranscript" class="vision-composer__note">{{ transcriptSignalLabel }} · {{ transcriptTargetLabel }}</span>
      <span v-else class="vision-composer__note">{{ currentSlotLabel }}</span>
    </div>
  </section>
</template>

<style scoped>
.vision-composer {
  width: 100%;
  max-width: 48rem;
  margin: 0 auto;
  display: grid;
  justify-self: center;
  align-self: center;
  gap: 0.35rem;
  border-radius: 1.6rem;
  padding: 0.75rem 0.8rem;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(24, 36, 47, 0.06);
  box-shadow: 0 16px 38px rgba(24, 36, 47, 0.08);
  backdrop-filter: none;
  position: relative;
  overflow: hidden;
}

.vision-composer--active {
  box-shadow: 0 24px 58px rgba(24, 36, 47, 0.09);
}

.vision-composer--expanded {
  box-shadow: 0 30px 72px rgba(24, 36, 47, 0.11);
}

.vision-composer--structured {
  box-shadow: 0 24px 58px rgba(24, 36, 47, 0.09);
}

.vision-composer > * {
  position: relative;
  z-index: 1;
}

.vision-composer--inline {
  margin-top: 0;
  background: rgba(255, 255, 255, 0.99);
  box-shadow: 0 14px 34px rgba(24, 36, 47, 0.07);
}

.vision-composer__bar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 0.78rem;
  padding: 0;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid rgba(24, 36, 47, 0.05);
  box-shadow: 0 10px 22px rgba(24, 36, 47, 0.05);
}

.vision-composer__input {
  width: 100%;
  min-height: 3.4rem;
  max-height: 8rem;
  resize: none;
  border: 0;
  outline: none;
  border-radius: 999px;
  padding: 0.88rem 1.05rem;
  background: transparent;
  border: 0;
  box-shadow: none;
  color: var(--vision-surface-ink);
  line-height: 1.4;
  overflow: hidden;
  font-size: 1.02rem;
}

.vision-composer__icon-button {
  appearance: none;
  border: 0;
  width: 3.9rem;
  height: 3.9rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.82);
  color: var(--vision-surface-ink);
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease, background 180ms ease;
  box-shadow: 0 12px 26px rgba(24, 36, 47, 0.08);
}

.vision-composer__icon-button svg {
  width: 1.3rem;
  height: 1.3rem;
  fill: currentColor;
}

.vision-composer__icon-button--primary {
  background: #ffffff;
  color: #2f6fff;
  border: 1px solid rgba(47, 111, 255, 0.12);
}

.vision-composer__icon-button:hover,
.vision-composer__icon-button:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(24, 36, 47, 0.08);
}

.vision-composer__icon-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.vision-composer__notes {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  justify-content: center;
}

.vision-composer__note {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 0.28rem 0.55rem;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(24, 36, 47, 0.05);
  color: var(--vision-surface-ink-soft);
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

@media (max-width: 720px) {
  .vision-composer {
    width: 100%;
  padding: 0.68rem 0.72rem;
}

  .vision-composer__bar {
    grid-template-columns: auto minmax(0, 1fr) auto;
    gap: 0.55rem;
  }

  .vision-composer__input {
    font-size: 0.98rem;
  }

  .vision-composer__icon-button {
    width: 3.45rem;
    height: 3.45rem;
  }
}
</style>
