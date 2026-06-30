<script setup lang="ts">
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"

const props = defineProps<{
  visible: boolean
  currentSlotLabel: string
  currentFieldKind: string
  currentPlaceholder: string
  inputText: string
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  textToSpeechEnabled: boolean
  speechRecognitionSupported: boolean
  speechSynthesisSupported: boolean
  hasResponse: boolean
  speechStatusLabel: string
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

const updateInputText = (event: Event) => {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  emit("update:inputText", target.value)
}
</script>

<template>
  <transition name="vision-composer-fade">
    <section v-if="visible" class="vision-composer">
      <div class="vision-composer__header">
        <div>
          <p class="vision-surface__eyebrow">Prompt dock</p>
          <h2>{{ currentSlotLabel || "Speak or type the next turn" }}</h2>
        </div>
        <div class="vision-composer__header-actions">
          <button type="button" class="vision-composer__ghost" @click="emit('reset')">New task</button>
          <button type="button" class="vision-composer__ghost" @click="emit('cancel')">Cancel</button>
          <button type="button" class="vision-composer__ghost" @click="emit('close')">Hide</button>
        </div>
      </div>

      <textarea
        v-if="currentFieldKind !== 'date_time'"
        :value="inputText"
        class="vision-composer__input"
        rows="3"
        :placeholder="currentPlaceholder"
        @focus="emit('open')"
        @input="updateInputText"
      ></textarea>

      <input
        v-else
        :value="inputText"
        type="datetime-local"
        class="vision-composer__input vision-composer__input--datetime"
        @focus="emit('open')"
        @input="updateInputText"
      />

      <div class="vision-composer__actions">
        <button
          type="button"
          class="vision-composer__action"
          :disabled="!voiceEnabled || !speechToTextEnabled || !speechRecognitionSupported"
          @click="emit('startListening')"
        >
          Mic
        </button>
        <button
          type="button"
          class="vision-composer__action"
          :disabled="!hasResponse || !voiceEnabled || !textToSpeechEnabled || !speechSynthesisSupported"
          @click="emit('speakSummary')"
        >
          Speak
        </button>
        <button
          type="button"
          class="vision-composer__action vision-composer__action--primary"
          :disabled="!canSend"
          @click="emit('submit')"
        >
          Send
        </button>
        <button
          type="button"
          class="vision-composer__action"
          :disabled="voiceState !== 'listening'"
          @click="emit('stopListening')"
        >
          Stop mic
        </button>
        <button
          type="button"
          class="vision-composer__action"
          :disabled="voiceState !== 'speaking'"
          @click="emit('stopSpeaking')"
        >
          Stop audio
        </button>
      </div>

      <p class="vision-surface__status-text">{{ speechStatusLabel }}</p>
    </section>
  </transition>

  <button
    v-if="!props.visible"
    type="button"
    class="vision-composer-launcher"
    @click="emit('open')"
  >
    Ask agent
  </button>
</template>

<style scoped>
.vision-composer {
  position: fixed;
  left: 50%;
  bottom: calc(6.75rem + env(safe-area-inset-bottom));
  z-index: 2;
  width: min(56rem, calc(100vw - 1.4rem));
  transform: translateX(-50%);
  border-radius: 2rem;
  padding: 1.1rem;
  background: var(--vision-surface-panel-bg);
  border: 1px solid var(--vision-surface-panel-border);
  box-shadow: var(--vision-surface-panel-shadow);
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
  color: var(--vision-surface-ink);
}

.vision-surface__eyebrow {
  margin: 0 0 0.55rem;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
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
  background: var(--vision-surface-chip-bg-soft);
  color: var(--vision-surface-ink);
}

.vision-composer__action--primary {
  background: var(--vision-surface-accent-gradient);
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
  background: var(--vision-surface-input-bg);
  color: var(--vision-surface-ink);
  box-shadow: var(--vision-surface-input-shadow);
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
  color: var(--vision-surface-ink-soft);
}

.vision-composer-launcher {
  position: fixed;
  left: 50%;
  bottom: calc(6.75rem + env(safe-area-inset-bottom));
  z-index: 4;
  transform: translateX(-50%);
  background: var(--vision-surface-accent-gradient);
  color: #10202c;
  border: 1px solid var(--vision-surface-panel-border);
  box-shadow: 0 24px 60px rgba(24, 36, 47, 0.16);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.vision-composer-fade-enter-active,
.vision-composer-fade-leave-active {
  transition: opacity 220ms ease, transform 220ms ease;
}

.vision-composer-fade-enter-from,
.vision-composer-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 720px) {
  .vision-composer__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .vision-composer__header-actions,
  .vision-composer__actions {
    width: 100%;
  }

  .vision-composer {
    bottom: calc(6.5rem + env(safe-area-inset-bottom));
    width: calc(100vw - 0.9rem);
    border-radius: 1.5rem;
  }
}
</style>
