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
  speechRecognitionSupported: boolean
  hasResponse: boolean
  canSend: boolean
  voiceState: VisionVoiceState
}>()

const emit = defineEmits<{
  "update:inputText": [value: string]
  submit: []
  startListening: []
  stopListening: []
  open: []
  cancel: []
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
  return Math.min(8, Math.max(3, estimatedRows))
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

  </section>
</template>

<style scoped>
.vision-composer {
  width: 100%;
  max-width: 60rem;
  margin: 0 auto;
  display: grid;
  justify-self: center;
  align-self: center;
  gap: 0.9rem;
  padding: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
  position: relative;
  overflow: hidden;
}

.vision-composer--inline,
.vision-composer--active,
.vision-composer--expanded,
.vision-composer--structured {
  background: transparent;
  box-shadow: none;
}

.vision-composer__bar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 1rem;
  padding: 0.92rem;
  border-radius: 2.2rem;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(24, 36, 47, 0.08);
  box-shadow: 0 28px 64px rgba(24, 36, 47, 0.08);
}

.vision-composer__input {
  width: 100%;
  min-height: 5rem;
  max-height: 10rem;
  resize: none;
  border: 0;
  outline: none;
  border-radius: 1.4rem;
  padding: 1rem 0.1rem;
  background: transparent;
  box-shadow: none;
  color: var(--vision-surface-ink);
  line-height: 1.4;
  overflow: hidden;
  font-size: 1.1rem;
}

.vision-composer__icon-button {
  appearance: none;
  border: 0;
  width: 5rem;
  height: 5rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: #ffffff;
  color: var(--vision-surface-ink);
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease, background 180ms ease;
  box-shadow: 0 14px 30px rgba(24, 36, 47, 0.1);
}

.vision-composer__icon-button svg {
  width: 1.45rem;
  height: 1.45rem;
  fill: currentColor;
}

.vision-composer__icon-button--primary {
  background: #18242f;
  color: #ffffff;
  border: 1px solid rgba(24, 36, 47, 0.08);
}

.vision-composer__icon-button:hover,
.vision-composer__icon-button:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(24, 36, 47, 0.12);
}

.vision-composer__icon-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 720px) {
  .vision-composer {
    width: 100%;
    gap: 0.75rem;
  }

  .vision-composer__bar {
    gap: 0.65rem;
    padding: 0.75rem;
    border-radius: 1.6rem;
  }

  .vision-composer__icon-button {
    width: 4.4rem;
    height: 4.4rem;
  }

  .vision-composer__input {
    min-height: 4.25rem;
    font-size: 0.98rem;
    padding-inline: 0.05rem;
  }

  .vision-composer__cloud {
    width: 100%;
  }

  .vision-composer__actions {
    gap: 0.45rem;
  }
}
</style>
