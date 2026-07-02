<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue"
import {currentUser} from "../../identity/auth.ts"
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionReviewTarget} from "../api/visionConversationApi.ts"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"
import VisionVoiceControl from "./VisionVoiceControl.vue"
import VisionTypingText from "./VisionTypingText.vue"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  displayBlocks: VisionCanvasBlock[]
  lastTranscript: string
  isLoading: boolean
  error: string
  inputText: string
  promptComposerVisible: boolean
  currentSlotLabel: string
  transcriptTargetLabel: string
  currentFieldKind: string
  currentPlaceholder: string
  voiceEnabled: boolean
  speechToTextEnabled: boolean
  speechRecognitionSupported: boolean
  voiceState: VisionVoiceState
  canSend: boolean
}>()

const emit = defineEmits<{
  choice: [value: string]
  reviewChange: [target: VisionReviewTarget]
  confirmReview: []
  startListening: []
  stopListening: []
  "update:inputText": [value: string]
  submit: []
  open: []
  cancel: []
}>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)

const username = computed(() => currentUser.value?.username ?? "there")
const questionBlock = computed(() => props.displayBlocks.find((block) => block.type === "field_request") ?? null)
const commandHintVisible = computed(() =>
  !props.response
  && !props.isLoading
  && !props.error
  && !props.inputText.trim()
  && !props.lastTranscript.trim())

const feedLines = computed(() => {
  const lines: Array<
    | {kind: "greeting" | "system" | "error" | "user" | "agent" | "question" | "hint"; text: string}
  > = []

  if (!props.response && !props.isLoading && !props.error) {
    lines.push({
      kind: "greeting",
      text: `Hello, ${username.value}. What do you want to create?`
    })
  }

  if (commandHintVisible.value) {
    lines.push({
      kind: "hint",
      text: "Try: create quest · profile · circles · applications · chat"
    })
  }

  if (props.isLoading) {
    lines.push({
      kind: "system",
      text: "Loading vision surface..."
    })
  }

  if (props.error) {
    lines.push({
      kind: "error",
      text: props.error
    })
  }

  if (props.lastTranscript.trim()) {
    lines.push({
      kind: "user",
      text: props.lastTranscript.trim()
    })
  }

  if (questionBlock.value?.body) {
    lines.push({
      kind: "question",
      text: questionBlock.value.body
    })
  }

  return lines
})

const greetingTypingText = computed(() => `Hello, ${username.value}. What do you want to create?`)
const updateInputText = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  emit("update:inputText", target.value)
}

const submitFromInput = () => {
  if (!props.canSend) {
    return
  }
  emit("submit")
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key !== "Enter" || event.shiftKey || event.altKey || event.metaKey || event.ctrlKey) {
    return
  }
  event.preventDefault()
  submitFromInput()
}

watch(
  () => props.inputText,
  () => {
    if (!textareaRef.value) {
      return
    }
    textareaRef.value.style.height = "auto"
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 260)}px`
  },
  {immediate: true}
)

const choose = (value: string) => {
  emit("choice", value)
}

const focusInput = async () => {
  await nextTick()
  textareaRef.value?.focus()
}

onMounted(() => {
  void focusInput()
})
</script>

<template>
  <section class="vision-console">
    <div class="vision-console__paper">
      <div class="vision-console__lines">
        <p v-for="(line, index) in feedLines" :key="`${line.kind}-${index}`" class="vision-console__line" :class="`vision-console__line--${line.kind}`">
          <span v-if="line.kind === 'greeting'" class="vision-console__text">
            <VisionTypingText
              :text="greetingTypingText"
              :active="true"
              :speed="20"
            />
          </span>
          <span v-else class="vision-console__text">{{ line.text }}</span>
        </p>

        <div class="vision-console__composer">
          <VisionVoiceControl
            :voice-enabled="voiceEnabled"
            :speech-to-text-enabled="speechToTextEnabled"
            :speech-recognition-supported="speechRecognitionSupported"
            :voice-state="voiceState"
            compact
            @start-listening="emit('startListening')"
            @stop-listening="emit('stopListening')"
          />

          <div class="vision-console__input-shell">
            <textarea
              ref="textareaRef"
              :value="inputText"
              class="vision-console__input"
              :placeholder="currentPlaceholder || 'Type here. Enter sends. Shift+Enter makes a new line.'"
              rows="1"
              @focus="emit('open')"
              @keydown="handleKeydown"
              @input="updateInputText"
            ></textarea>
          </div>
        </div>

        <p v-if="props.response?.message" class="vision-console__line vision-console__line--agent">
          <span class="vision-console__text">
            <VisionTypingText
              :text="props.response.message"
              :active="true"
              :speed="18"
            />
          </span>
        </p>

        <div v-if="questionBlock && questionBlock.options.length" class="vision-console__choices">
          <button
            v-for="option in questionBlock.options"
            :key="option.id"
            type="button"
            class="vision-console__choice"
            @click="choose(option.value ?? option.label)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.vision-console {
  width: 100%;
  display: grid;
  height: 100%;
}

.vision-console__paper {
  width: 100%;
  min-height: 100%;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 0.85rem;
  padding: 1rem 0.7rem 0.75rem;
  border-radius: 0;
  border: 0;
  background: transparent;
  box-shadow: none;
}

.vision-console__lines {
  min-height: 0;
  flex: 1;
  display: grid;
  gap: 0.38rem;
  align-content: start;
  overflow: auto;
  padding-right: 0.25rem;
}

.vision-console__line {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  line-height: 1.45;
  font-size: 1rem;
  letter-spacing: -0.01em;
}

.vision-console__line--greeting,
.vision-console__line--agent,
.vision-console__line--user {
  font-size: 1.04rem;
}

.vision-console__line--greeting {
  color: var(--vision-surface-ink);
}

.vision-console__line--system {
  color: var(--vision-surface-ink-muted);
}

.vision-console__line--error {
  color: #b04f43;
}

.vision-console__line--user {
  color: #1d5c49;
}

.vision-console__line--agent {
  color: var(--vision-surface-ink);
}

.vision-console__line--question {
  color: #244a7a;
}

.vision-console__line--status {
  color: var(--vision-surface-ink-soft);
}

.vision-console__line--hint {
  color: rgba(24, 36, 47, 0.46);
}

.vision-console__text {
  white-space: pre-wrap;
}

.vision-console__composer {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.42rem;
  align-items: center;
  padding: 0.05rem 0 0.05rem 0;
}

.vision-console__input-shell {
  min-width: 0;
}

.vision-console__choices {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  padding-left: 0.35rem;
}

.vision-console__choice {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: #244a7a;
  font: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 0.18em;
}

.vision-console__input {
  width: 100%;
  min-height: 2.1rem;
  resize: none;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--vision-surface-ink);
  font-family: "IBM Plex Mono", "SFMono-Regular", "Menlo", monospace;
  font-size: 1.02rem;
  line-height: 1.5;
  padding: 0.05rem 0 0 0;
  margin: 0;
  caret-color: #244a7a;
  text-shadow: 0 0 1px rgba(24, 36, 47, 0.12);
}

.vision-console__input::placeholder {
  color: rgba(24, 36, 47, 0.22);
  transform: translateY(0.3rem);
}

</style>
