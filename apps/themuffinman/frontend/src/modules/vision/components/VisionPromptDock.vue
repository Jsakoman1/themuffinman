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

.vision-surface__eyebrow {
  margin: 0 0 0.55rem;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.46);
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
    bottom: 0.75rem;
    width: calc(100vw - 0.9rem);
    border-radius: 1.5rem;
  }
}
</style>
