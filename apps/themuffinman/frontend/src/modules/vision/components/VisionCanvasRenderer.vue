<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue"
import {RouterLink} from "vue-router"
import {currentUser} from "../../identity/auth.ts"
import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionExecutionCandidate, VisionReviewTarget, VisionRuntimeContext, VisionWorkspaceHandoff} from "../api/visionConversationApi.ts"
import type {VisionVoiceState} from "../composables/useVisionConversation.ts"
import {formatVisionFlowLine} from "../visionPresentation.ts"
import {resolveVisionEntityRoute} from "../../app-shell/visionHandoff.ts"
import VisionTerminalRow from "./VisionTerminalRow.vue"
import VisionVoiceControl from "./VisionVoiceControl.vue"
import VisionTypingText from "./VisionTypingText.vue"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  executionCandidate: VisionExecutionCandidate | null
  runtimeContext: VisionRuntimeContext | null
  workspaceHandoff: VisionWorkspaceHandoff | null
  displayBlocks: VisionCanvasBlock[]
  lastTranscript: string
  isLoading: boolean
  error: string
  inputText: string
  promptComposerVisible: boolean
  currentSlotLabel: string
  currentSlotValue: string
  transcriptTargetLabel: string
  transcriptTargetDetail: string
  currentFieldKind: string
  currentPlaceholder: string
  activeEntityFamilyLabel: string
  activeEntityContextLabel: string
  speechStatusLabel: string
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
  fetchMore: []
  retry: []
  startListening: []
  stopListening: []
  "update:inputText": [value: string]
  submit: []
  open: []
  cancel: []
}>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)

type TerminalRow = {
  key: string
  kind: "choice" | "summary"
  title: string
  meta: string
  sub: string
  actionValue?: string
  routeTo?: import("vue-router").RouteLocationRaw | null
  routeLabel?: string
}

const isNonEmptyText = (value: string | null | undefined): value is string => !!value && value.trim().length > 0

const username = computed(() => currentUser.value?.username ?? "there")
const questionBlock = computed(() => props.displayBlocks.find((block) => block.type === "field_request") ?? null)
const activeFlowLabel = computed(() => props.activeEntityContextLabel.trim() || props.activeEntityFamilyLabel.trim())
const hasMoreResults = computed(() => props.displayBlocks.some((block) => block.questDiscovery?.hasMore || block.searchDiscovery?.hasMore))

const runtimeActionLabel = computed(() => props.runtimeContext?.primaryActionLabel ?? "Ready")
const runtimeDensityLabel = computed(() => props.runtimeContext?.density ?? "inspect")

const headerLine = computed(() => {
  const segments: string[] = []
  if (activeFlowLabel.value) {
    segments.push(activeFlowLabel.value)
  }
  const slotLine = formatVisionFlowLine(props.currentSlotLabel, props.currentSlotValue)
  if (slotLine) {
    segments.push(slotLine)
  }
  return segments.join(" · ")
})

const terminalRows = computed<TerminalRow[]>(() => props.displayBlocks.flatMap<TerminalRow>((block, blockIndex) => {
  if (block.type === "field_request") {
    return []
  }

  if (block.type === "quest_discovery" && block.questDiscovery) {
    return (block.questDiscovery.items ?? []).map((item, itemIndex) => ({
      key: `quest-${blockIndex}-${item.questId}-${item.rank}-${itemIndex}`,
      kind: "choice",
      title: item.title,
      meta: [
        item.rewardLabel,
        item.statusLabel,
        item.locationLabel
      ].filter(isNonEmptyText).join(" · "),
      sub: [
        item.creatorUsername ? `by ${item.creatorUsername}` : "",
        item.scheduledAt ?? ""
      ].filter(isNonEmptyText).join(" · "),
      actionValue: item.title,
      routeTo: resolveVisionEntityRoute("quest", item.questId),
      routeLabel: "Open detail"
    }))
  }

  if (block.type === "search_discovery" && block.searchDiscovery) {
    return (block.searchDiscovery.items ?? []).map((item, itemIndex) => ({
      key: `search-${blockIndex}-${item.entityFamily}-${item.targetId}-${item.capabilityId}-${itemIndex}`,
      kind: "choice",
      title: item.title,
      meta: [
        item.resolutionLabel,
        item.entityFamily,
        item.matchSummary
      ].filter(isNonEmptyText).join(" · "),
      sub: item.summary ?? "",
      actionValue: item.title,
      routeTo: resolveVisionEntityRoute(item.entityFamily, item.targetId),
      routeLabel: "Open route"
    }))
  }

  if ((block.items?.length ?? 0) > 0) {
    return (block.items ?? []).map((item, itemIndex) => ({
      key: `summary-${blockIndex}-${item.slotId}-${itemIndex}`,
      kind: "summary",
      title: item.label,
      meta: item.value ?? "",
      sub: ""
    }))
  }

  const title = block.title?.trim() ?? ""
  const body = block.body?.trim() ?? ""
  if (!title && !body) {
    return []
  }

  return [{
    key: `block-${blockIndex}-${block.type}`,
    kind: "summary",
    title,
    meta: body,
    sub: ""
  }]
}))

const feedLines = computed(() => {
  const lines: Array<
    | {kind: "greeting" | "system" | "error" | "user" | "agent" | "question"; text: string}
  > = []

  if (!props.response && !props.isLoading && !props.error) {
    lines.push({
      kind: "greeting",
      text: `Hello, ${username.value}.`
    })
  }

  if (headerLine.value) {
    lines.push({
      kind: "system",
      text: headerLine.value
    })
  }

  if (props.isLoading) {
    lines.push({
      kind: "system",
      text: "Loading..."
    })
  }

  if (props.error) {
    lines.push({
      kind: "error",
      text: props.error
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

const greetingTypingText = computed(() => `Hello, ${username.value}.`)
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
  <section class="vision-console" aria-live="polite" :aria-busy="props.isLoading || props.voiceState === 'processing'">
    <div class="vision-console__paper">
      <header v-if="props.runtimeContext" class="vision-console__runtime">
        <div class="vision-console__runtime-state">
          <span class="vision-console__runtime-dot" aria-hidden="true"></span>
          <span>{{ runtimeActionLabel }}</span>
          <span class="vision-console__runtime-density">{{ runtimeDensityLabel }}</span>
        </div>
        <p v-if="props.runtimeContext.consentRequired" class="vision-console__runtime-note">
          Confirm before continuing.
        </p>
        <button v-if="props.error" type="button" class="vision-console__retry" @click="emit('retry')">Try again</button>
      </header>

      <div v-if="props.workspaceHandoff" class="vision-console__handoff">
        <span>{{ props.workspaceHandoff.explanation }}</span>
        <RouterLink v-if="props.workspaceHandoff.returnTo" :to="props.workspaceHandoff.returnTo" class="vision-console__handoff-link">
          Return
        </RouterLink>
      </div>

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
            :speech-status-label="speechStatusLabel"
            compact
            @start-listening="emit('startListening')"
            @stop-listening="emit('stopListening')"
          />

          <div class="vision-console__input-shell">
            <textarea
              ref="textareaRef"
              :value="inputText"
              class="vision-console__input"
              :placeholder="currentPlaceholder || 'Type here.'"
              :aria-label="currentSlotLabel || 'Vision prompt'"
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

        <div
          v-if="props.executionCandidate?.reviewReady && props.executionCandidate.confirmationRequired"
          class="vision-console__review-actions"
          aria-label="Vision review actions"
        >
          <button type="button" class="vision-console__review-confirm" @click="emit('confirmReview')">Confirm and create</button>
          <button type="button" class="vision-console__review-cancel" @click="emit('cancel')">Cancel</button>
        </div>

        <div v-for="row in terminalRows" :key="row.key" class="vision-console__row-group">
          <VisionTerminalRow
            class="vision-console__row"
            :label="row.kind === 'choice' ? 'Option' : row.title"
            :value="row.kind === 'choice' ? [row.meta, row.sub].filter(Boolean).join(' · ') : row.meta"
            :clickable="row.kind === 'choice'"
            :tone="row.kind === 'choice' ? 'strong' : 'muted'"
            @click="choose(row.actionValue ?? row.title)"
          />
          <div v-if="row.routeTo" class="vision-console__row-actions">
            <RouterLink :to="row.routeTo" class="vision-console__route-link">
              {{ row.routeLabel ?? "Open route" }}
            </RouterLink>
          </div>
        </div>

        <div v-if="questionBlock && questionBlock.options.length" class="vision-console__choices">
          <VisionTerminalRow
            v-for="option in questionBlock.options"
            :key="option.id"
            class="vision-console__row"
            label="Choice"
            :value="option.label"
            clickable
            tone="muted"
            @click="choose(option.value ?? option.label)"
          />
        </div>

        <button v-if="hasMoreResults" type="button" class="vision-console__more" @click="emit('fetchMore')">Show more</button>
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
  grid-template-rows: auto auto minmax(0, 1fr) auto;
  gap: 0.7rem;
  padding: 1rem 0.7rem 0.75rem;
  border-radius: 0;
  border: 0;
  background: transparent;
  box-shadow: none;
}

.vision-console__runtime {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 0.7rem;
  align-items: flex-start;
  padding: 0.25rem 0 0.15rem;
}

.vision-console__runtime-state {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  color: rgba(24, 36, 47, 0.74);
  font-size: 0.8rem;
  font-weight: 650;
}

.vision-console__runtime-dot {
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 50%;
  background: #1d5c49;
}

.vision-console__runtime-density {
  border-radius: 999px;
  background: rgba(24, 36, 47, 0.07);
  padding: 0.2rem 0.45rem;
  color: rgba(24, 36, 47, 0.5);
  font-size: 0.68rem;
  font-weight: 500;
  text-transform: uppercase;
}

.vision-console__runtime-copy {
  display: grid;
  gap: 0.4rem;
  min-width: 0;
}

.vision-console__runtime-label {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.45);
}

.vision-console__runtime-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.vision-console__runtime-pill {
  display: inline-flex;
  align-items: center;
  min-height: 1.75rem;
  padding: 0.18rem 0.55rem;
  border-radius: 999px;
  font-size: 0.76rem;
  letter-spacing: 0.02em;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(255, 255, 255, 0.76);
  color: rgba(24, 36, 47, 0.75);
}

.vision-console__runtime-pill--strong {
  background: rgba(35, 58, 92, 0.08);
  color: #244a7a;
}

.vision-console__runtime-pill--accent {
  background: rgba(36, 74, 122, 0.1);
  color: #1d5c49;
}

.vision-console__runtime-guidance {
  display: grid;
  gap: 0.24rem;
  justify-items: end;
  text-align: right;
  min-width: min(100%, 15rem);
}

.vision-console__runtime-note {
  margin: 0;
  font-size: 0.78rem;
  line-height: 1.35;
  color: rgba(24, 36, 47, 0.62);
}

.vision-console__hint-rail {
  display: flex;
  flex-wrap: wrap;
  gap: 0.38rem;
  align-items: center;
  padding: 0.05rem 0 0.15rem;
}

.vision-console__hint-label {
  font-size: 0.72rem;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.42);
}

.vision-console__hint-pill {
  display: inline-flex;
  align-items: center;
  min-height: 1.6rem;
  padding: 0.18rem 0.48rem;
  border-radius: 999px;
  border: 1px solid rgba(24, 36, 47, 0.08);
  background: rgba(247, 249, 243, 0.86);
  color: rgba(24, 36, 47, 0.72);
  font-size: 0.74rem;
}

.vision-console__row-group {
  display: grid;
  gap: 0.3rem;
}

.vision-console__row-actions {
  padding-left: 0.1rem;
}

.vision-console__route-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2rem;
  padding: 0.3rem 0.65rem;
  border-radius: 999px;
  border: 1px solid rgba(24, 36, 47, 0.1);
  background: rgba(255, 255, 255, 0.7);
  color: rgba(24, 36, 47, 0.78);
  font-size: 0.78rem;
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

.vision-console__text {
  white-space: pre-wrap;
}

.vision-console__block {
  display: grid;
  gap: 0.35rem;
  padding: 0.45rem 0;
  border-top: 1px solid rgba(24, 36, 47, 0.06);
}

.vision-console__composer {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.42rem;
  align-items: center;
  padding: 0.05rem 0 0.05rem 0;
}

.vision-console__voice-meta {
  display: grid;
  gap: 0.45rem;
  padding: 0.2rem 0 0.1rem;
}

.vision-console__voice-status {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  align-items: center;
  color: var(--vision-surface-ink-muted);
  font-size: 0.73rem;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.vision-console__input-shell {
  min-width: 0;
}

.vision-console__choices {
  display: grid;
  gap: 0.1rem;
}

.vision-console__retry {
  justify-self: start;
  border: 1px solid rgba(24, 36, 47, 0.16);
  border-radius: 999px;
  padding: 0.42rem 0.72rem;
  background: transparent;
  color: rgba(24, 36, 47, 0.76);
  font: inherit;
  font-size: 0.8rem;
  font-weight: 650;
  cursor: pointer;
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

@media (max-width: 980px) {
  .vision-console__runtime {
    flex-direction: column;
  }

  .vision-console__runtime-guidance {
    justify-items: start;
    text-align: left;
    min-width: 0;
  }
}

</style>
