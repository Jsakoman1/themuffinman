<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"

const props = defineProps<{
  visible: boolean
  currentSlotLabel: string
  currentSlotValue: string
  transcriptTargetLabel: string
  transcriptTargetDetail: string
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

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const datetimeRef = ref<HTMLInputElement | null>(null)

const updateInputText = (event: Event) => {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement
  emit("update:inputText", target.value)
}

const focusComposerField = async () => {
  if (!props.visible) {
    return
  }

  await nextTick()
  const target = props.currentFieldKind === "date_time" ? datetimeRef.value : textareaRef.value
  target?.focus()
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

const normalizedSlotLabel = computed(() => props.currentSlotLabel.toLowerCase())
const transcriptText = computed(() => props.lastTranscript.trim())
const transcriptLower = computed(() => transcriptText.value.toLowerCase())
const currentSlotValue = computed(() => props.currentSlotValue.trim())

const composerToneClass = computed(() => {
  if (props.currentFieldKind === "date_time") {
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

const surfaceBadgeLabel = computed(() => {
  if (props.voiceState === "listening") {
    return "Listening"
  }
  if (props.voiceState === "processing") {
    return "Processing"
  }
  if (props.voiceState === "speaking") {
    return "Speaking"
  }
  if (props.hasResponse) {
    return "Active"
  }
  return "Ready"
})

const suggestionChips = computed(() => {
  if (props.voiceState !== "idle") {
    return []
  }

  const chips: Array<{label: string; value: string}> = []
  const slot = normalizedSlotLabel.value
  const hasTypedInput = props.inputText.trim().length > 0
  const hasTranscript = transcriptText.value.length > 0
  const allowCorrectionChips = hasTranscript || !hasTypedInput
  const hasExistingSlotValue = currentSlotValue.value.length > 0

  if (hasTranscript && (!hasTypedInput || slot.includes("reward") || slot.includes("location") || slot.includes("schedule") || props.currentFieldKind === "date_time")) {
    chips.push({label: "Edit transcript", value: transcriptText.value})
  }

  if (hasExistingSlotValue) {
    chips.push({label: "Keep current", value: currentSlotValue.value})
    if (hasTranscript) {
      chips.push({label: "Replace with transcript", value: transcriptText.value})
    }
  }

  if (props.currentFieldKind === "date_time") {
    chips.push(
      {label: "Tomorrow at 2 PM", value: "tomorrow at 2 PM"},
      {label: "This Friday 18:00", value: "this Friday at 18:00"},
      {label: "By agreement", value: "by agreement"}
    )
  } else if (slot.includes("reward")) {
    const rewardMatchesFree = transcriptLower.value.includes("free") || transcriptLower.value.includes("no pay") || transcriptLower.value.includes("unpaid")
    const rewardMatchesAmount = /\b\d+([.,]\d+)?\b/.test(transcriptLower.value)
    if (rewardMatchesFree) {
      chips.push({label: "Mark free", value: "free"})
      chips.push({label: "Use 20 euros", value: "20 euros"})
      chips.push({label: "Use 35 euros", value: "35 euros"})
    } else if (rewardMatchesAmount) {
      chips.push({label: "Use typed reward", value: transcriptText.value})
      chips.push({label: "Make it free", value: "free"})
      chips.push({label: "Round to 20 euros", value: "20 euros"})
    } else {
      chips.push({label: "Free", value: "free"})
      chips.push({label: "20 euros", value: "20 euros"})
      chips.push({label: "35 euros", value: "35 euros"})
    }
  } else if (slot.includes("location")) {
    const transcriptHintsHide = transcriptLower.value.includes("hide") || transcriptLower.value.includes("private")
    const transcriptHintsProfile = transcriptLower.value.includes("profile") || transcriptLower.value.includes("current location")
    if (transcriptHintsHide) {
      chips.push({label: "Hide location", value: "hide location"})
      chips.push({label: "Use profile location", value: "use profile location"})
    } else if (transcriptHintsProfile) {
      chips.push({label: "Use profile location", value: "use profile location"})
      chips.push({label: "Hide location", value: "hide location"})
    } else {
      chips.push({label: "Use profile location", value: "use profile location"})
      chips.push({label: "Hide location", value: "hide location"})
      chips.push({label: "Ban Jelacic Square", value: "Ban Jelacic Square"})
    }
  } else if (slot.includes("schedule")) {
    const transcriptHintsAgreement = transcriptLower.value.includes("agreement") || transcriptLower.value.includes("flexible")
    const transcriptHintsFixed = transcriptLower.value.includes("tomorrow") || transcriptLower.value.includes("next") || transcriptLower.value.includes("friday")
    if (transcriptHintsFixed) {
      chips.push({label: "Use typed time", value: transcriptText.value})
      chips.push({label: "By agreement", value: "by agreement"})
      chips.push({label: "Pick another time", value: "tomorrow at 2 PM"})
    } else if (transcriptHintsAgreement) {
      chips.push({label: "By agreement", value: "by agreement"})
      chips.push({label: "Fixed time", value: "fixed time"})
      chips.push({label: "Tomorrow at 2 PM", value: "tomorrow at 2 PM"})
    } else {
      chips.push({label: "Fixed time", value: "fixed time"})
      chips.push({label: "By agreement", value: "by agreement"})
      chips.push({label: "Tomorrow at 2 PM", value: "tomorrow at 2 PM"})
    }
  }

  if (!chips.length && !hasTypedInput && allowCorrectionChips) {
    if (!props.hasResponse) {
      chips.push(
        {label: "Create a quest", value: "Create a quest"},
        {label: "Help me write it", value: "Help me create a quest"},
        {label: "Use voice", value: "I want to speak this"}
      )
    } else {
      chips.push(
        {label: "Change title", value: "change title"},
        {label: "Change reward", value: "change reward"},
        {label: "Change location", value: "change location"}
      )
    }
  }

  return chips.slice(0, 5)
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

const showReadAction = computed(() => props.hasResponse)
const showStopMicAction = computed(() => props.voiceState === "listening")
const showStopAudioAction = computed(() => props.voiceState === "speaking")
const showCancelAction = computed(() => props.hasResponse || props.inputText.trim().length > 0)

const applySuggestion = (value: string) => {
  emit("update:inputText", value)
  emit("open")
}
</script>

<template>
    <section
      v-if="visible"
      class="vision-composer"
      :class="[composerToneClass, { 'vision-composer--idle': !hasResponse, 'vision-composer--inline': hasResponse }]"
    >
      <div class="vision-composer__header">
        <div>
          <p class="vision-surface__eyebrow">Prompt surface</p>
          <h2>{{ currentSlotLabel || "Tell the surface what to do" }}</h2>
          <p class="vision-composer__lede">
            {{ currentFieldKind === 'date_time' ? 'Structured date and time input.' : 'One field at a time, shaped by the current state.' }}
          </p>
        </div>
        <div class="vision-composer__header-actions">
          <span class="vision-composer__badge">{{ surfaceBadgeLabel }}</span>
          <button v-if="showCancelAction" type="button" class="vision-composer__ghost" @click="emit('cancel')">Cancel</button>
          <button v-if="props.hasResponse" type="button" class="vision-composer__ghost" @click="emit('reset')">New task</button>
        </div>
      </div>

      <div v-if="lastTranscript" class="vision-composer__transcript">
        <div class="vision-composer__transcript-topline">
          <p class="vision-composer__transcript-label">{{ transcriptSignalLabel }}</p>
          <span class="vision-composer__transcript-slot">Mapped to {{ transcriptTargetLabel }}</span>
        </div>
        <p class="vision-composer__transcript-text">{{ lastTranscript }}</p>
        <p class="vision-composer__transcript-detail">{{ transcriptTargetDetail }}</p>
        <p v-if="currentSlotValue" class="vision-composer__transcript-current">Current slot value: {{ currentSlotValue }}</p>
      </div>

      <div class="vision-composer__field-shell">
        <textarea
          v-if="currentFieldKind !== 'date_time'"
          ref="textareaRef"
          :value="inputText"
          class="vision-composer__input"
          :rows="textareaRows"
          :placeholder="currentPlaceholder"
          @focus="emit('open')"
          @input="updateInputText"
        ></textarea>

        <input
          v-else
          ref="datetimeRef"
          :value="inputText"
          type="datetime-local"
          class="vision-composer__input vision-composer__input--datetime"
          @focus="emit('open')"
          @input="updateInputText"
        />
      </div>

      <div v-if="suggestionChips.length" class="vision-composer__suggestions">
        <button
          v-for="chip in suggestionChips"
          :key="chip.value"
          type="button"
          class="vision-composer__chip"
          @click="applySuggestion(chip.value)"
        >
          {{ chip.label }}
        </button>
      </div>

      <div class="vision-composer__actions" :class="{ 'vision-composer__actions--minimal': !showReadAction && !showStopMicAction && !showStopAudioAction }">
        <button
          type="button"
          class="vision-composer__action"
          :disabled="!voiceEnabled || !speechToTextEnabled || !speechRecognitionSupported"
          @click="emit('startListening')"
        >
          Listen
        </button>
        <button
          v-if="showReadAction"
          type="button"
          class="vision-composer__action"
          :disabled="!hasResponse || !voiceEnabled || !textToSpeechEnabled || !speechSynthesisSupported"
          @click="emit('speakSummary')"
        >
          Read
        </button>
        <button
          type="button"
          class="vision-composer__action vision-composer__action--primary"
          :disabled="!canSend"
          @click="emit('submit')"
        >
          Go
        </button>
        <button
          v-if="showStopMicAction"
          type="button"
          class="vision-composer__action"
          :disabled="voiceState !== 'listening'"
          @click="emit('stopListening')"
        >
          Stop mic
        </button>
        <button
          v-if="showStopAudioAction"
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
</template>

<style scoped>
.vision-composer {
  width: 100%;
  margin-top: 0.25rem;
  border-radius: 1.85rem;
  padding: 1.05rem;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 250, 252, 0.88) 100%);
  border: 1px solid rgba(24, 36, 47, 0.08);
  box-shadow: 0 22px 52px rgba(24, 36, 47, 0.08);
  backdrop-filter: blur(26px);
  position: relative;
  overflow: hidden;
}

.vision-composer--compact {
  transform: translateY(0);
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

.vision-composer::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 0%, rgba(255, 173, 141, 0.16), transparent 34%),
    radial-gradient(circle at 88% 8%, rgba(116, 197, 255, 0.14), transparent 28%);
  pointer-events: none;
}

.vision-composer > * {
  position: relative;
  z-index: 1;
}

.vision-composer--idle {
  padding-top: 1rem;
}

.vision-composer--inline {
  margin-top: 0;
  border-radius: 1.4rem;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.78) 0%, rgba(248, 250, 252, 0.72) 100%);
  box-shadow: var(--vision-surface-shadow-soft);
}

.vision-composer--inline .vision-composer__header {
  margin-bottom: 0.8rem;
}

.vision-composer--inline .vision-composer__field-shell {
  background: rgba(255, 255, 255, 0.38);
}

.vision-composer__lede {
  margin: 0.3rem 0 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
  font-size: 0.95rem;
}

.vision-composer__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.vision-composer__header h2 {
  margin: 0;
  font-size: 1.08rem;
  letter-spacing: -0.03em;
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
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
}

.vision-composer__ghost,
.vision-composer__action {
  appearance: none;
  border: 0;
  border-radius: 999px;
  padding: 0.7rem 0.98rem;
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, opacity 180ms ease, background 180ms ease;
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

.vision-composer__badge {
  display: inline-flex;
  align-items: center;
  align-self: center;
  border-radius: 999px;
  padding: 0.42rem 0.78rem;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(24, 36, 47, 0.08);
  color: var(--vision-surface-ink-soft);
  font-size: 0.76rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vision-composer__ghost:hover,
.vision-composer__action:hover,
.vision-composer__ghost:focus-visible,
.vision-composer__action:focus-visible {
  transform: translateY(-1px);
}

.vision-composer__action:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  transform: none;
}

.vision-composer__field-shell {
  border-radius: 1.6rem;
  padding: 0.4rem;
  background: rgba(255, 255, 255, 0.42);
  border: 1px solid rgba(24, 36, 47, 0.06);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: padding 180ms ease, background 180ms ease, box-shadow 180ms ease;
}

.vision-composer__transcript {
  display: grid;
  gap: 0.35rem;
  padding: 0.75rem 0.9rem;
  border-radius: 1.15rem;
  background: rgba(240, 247, 255, 0.82);
  border: 1px solid rgba(24, 36, 47, 0.06);
}

.vision-composer__transcript-topline {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.vision-composer__transcript-label {
  margin: 0;
  font-size: 0.68rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-composer__transcript-slot {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 0.28rem 0.62rem;
  border: 1px solid rgba(24, 36, 47, 0.06);
  background: rgba(255, 255, 255, 0.82);
  color: var(--vision-surface-ink-soft);
  font-size: 0.68rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.vision-composer__transcript-text {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.5;
}

.vision-composer__transcript-detail {
  margin: 0;
  color: var(--vision-surface-ink-muted);
  font-size: 0.88rem;
  line-height: 1.45;
}

.vision-composer__transcript-current {
  margin: 0;
  color: var(--vision-surface-ink);
  font-size: 0.9rem;
  line-height: 1.45;
}

.vision-composer__input {
  width: 100%;
  min-height: 7rem;
  resize: none;
  border: 0;
  outline: none;
  border-radius: 1.05rem;
  padding: 0.95rem 1rem;
  background: linear-gradient(180deg, rgba(250, 252, 255, 0.96) 0%, rgba(244, 248, 252, 0.96) 100%);
  color: var(--vision-surface-ink);
  box-shadow:
    inset 0 0 0 1px rgba(24, 36, 47, 0.06),
    0 8px 24px rgba(24, 36, 47, 0.03);
  line-height: 1.6;
  overflow: hidden;
  transition: min-height 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.vision-composer--compact .vision-composer__input {
  min-height: 6.4rem;
}

.vision-composer--active .vision-composer__field-shell {
  padding: 0.46rem;
  background: rgba(255, 255, 255, 0.5);
}

.vision-composer--expanded .vision-composer__field-shell {
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.56);
}

.vision-composer--structured .vision-composer__field-shell {
  padding: 0.52rem;
  background: rgba(255, 255, 255, 0.54);
}

.vision-composer--expanded .vision-composer__input {
  min-height: 10rem;
}

.vision-composer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  margin-top: 0.95rem;
}

.vision-composer__actions--minimal {
  gap: 0.5rem;
}

.vision-composer__suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
  margin-top: 0.85rem;
}

.vision-composer__chip {
  appearance: none;
  border: 1px solid rgba(24, 36, 47, 0.08);
  border-radius: 999px;
  padding: 0.52rem 0.82rem;
  background: rgba(255, 255, 255, 0.78);
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.vision-composer__chip:hover,
.vision-composer__chip:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.94);
}

.vision-surface__status-text {
  margin: 0.85rem 0 0;
  font-size: 0.9rem;
  color: var(--vision-surface-ink-soft);
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
    width: 100%;
    border-radius: 1.45rem;
    padding: 1rem;
  }
}
</style>
