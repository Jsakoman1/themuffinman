<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute} from "vue-router"
import {useVisionConversation} from "../../vision/composables/useVisionConversation.ts"
import {useVisionForWeb} from "../composables/useVisionForWeb.ts"
import VisionForWebAssistant from "./VisionForWebAssistant.vue"

const props = defineProps<{context: string; source: string; returnTo: string}>()
const open = ref(false)
const composer = ref<HTMLInputElement | null>(null)
const toggleButton = ref<HTMLButtonElement | null>(null)
const route = useRoute()
const {execute, contextFor} = useVisionForWeb()
const {
  isLoading, error, inputText, response, init, processPrompt, canSend, voiceState,
  speechRecognitionSupported, startListening, stopListening
} = useVisionConversation()

const routePrompt = computed(() => typeof route.query.visionPrompt === "string" ? route.query.visionPrompt.trim() : "")
const routeContext = computed(() => typeof route.query.visionContext === "string" ? route.query.visionContext.trim() : "")
const routeSource = computed(() => typeof route.query.visionSource === "string" ? route.query.visionSource.trim() : "")
const routeReturnTo = computed(() => typeof route.query.visionReturnTo === "string" ? route.query.visionReturnTo.trim() : "")
const effectiveWorkspaceHandoff = computed(() => contextFor(
  routeContext.value || props.context,
  routeSource.value || props.source,
  routeReturnTo.value || props.returnTo
))
const conversationHandoff = computed(() => ({
  contextLabel: effectiveWorkspaceHandoff.value.workspaceContext,
  source: effectiveWorkspaceHandoff.value.workspaceSource,
  returnTo: effectiveWorkspaceHandoff.value.workspaceReturnTo
}))
const lastRoutePrompt = ref("")
const routePromptKey = computed(() => `${routePrompt.value}|${routeContext.value}|${routeSource.value}|${routeReturnTo.value}`)
const assistantState = computed(() => {
  if (error.value) return "unavailable"
  if (voiceState.value === "listening") return "listening"
  if (isLoading.value || voiceState.value === "processing") return "thinking"
  if (response.value?.webAction) return "navigating"
  if (response.value?.canvasMode === "blocked") return "recovery"
  if (response.value?.canvasMode === "clarification") return "clarification"
  if (response.value) return "result"
  return "idle"
})
const submit = async () => {
  if (!canSend.value) return
  await processPrompt(inputText.value, "text", "SUBMIT_PROMPT", null, null, null, null, {
    ...conversationHandoff.value
  })
}

const runRoutePrompt = async () => {
  if (route.query.visionAutorun !== "1" || !routePrompt.value || lastRoutePrompt.value === routePromptKey.value) return
  lastRoutePrompt.value = routePromptKey.value
  open.value = true
  inputText.value = routePrompt.value
  await processPrompt(routePrompt.value, "text", "SUBMIT_PROMPT", null, null, null, null, conversationHandoff.value)
}

const toggle = async () => {
  if (open.value) { open.value = false; await toggleButton.value?.focus(); return }
  open.value = true
  if (open.value) {
    await composer.value?.focus()
  }
}
const close = async () => { open.value = false; await toggleButton.value?.focus() }

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape" && open.value) {
    void close()
  }
}

watch(() => response.value?.webAction, async action => {
  if (action && action.canonicalPath !== route.fullPath) {
    await execute(action)
    open.value = false
  }
})

onMounted(async () => { await init(); await runRoutePrompt(); window.addEventListener("keydown", handleKeydown) })
watch(routePrompt, () => { void runRoutePrompt() })
onBeforeUnmount(() => window.removeEventListener("keydown", handleKeydown))
</script>

<template>
  <!-- Post-start hardening marker: preserve route context and keyboard recovery at every handoff. -->
  <section class="vision-web-host" aria-label="Vision assistant" data-context-source="contextual-workspace">
      <button ref="toggleButton" class="vision-web-host__toggle" type="button" aria-controls="vision-web-panel" :aria-expanded="open" @click="toggle">
      <VisionForWebAssistant :state="assistantState" />
      Vision
    </button>
    <div v-if="open" id="vision-web-panel" class="vision-web-host__panel" role="dialog" aria-modal="false" aria-label="Ask Vision" aria-describedby="vision-web-hint">
      <header class="vision-web-host__header">
        <div><strong>Vision</strong><span>{{ props.context }}</span></div>
        <button type="button" class="vision-web-host__close" aria-label="Close Vision" @click="close">×</button>
      </header>
      <p id="vision-web-hint" class="vision-web-host__hint">Ask about this workspace or open another module.</p>
      <p v-if="response" class="vision-web-host__response" :data-vision-state="response.agentState">{{ response.message }}</p>
      <p v-if="error" class="vision-web-host__error" role="alert">{{ error }}</p>
      <form class="vision-web-host__composer" @submit.prevent="submit">
        <input ref="composer" v-model="inputText" type="text" placeholder="Ask Vision…" :disabled="isLoading || voiceState === 'processing'" aria-label="Ask Vision" />
        <button v-if="speechRecognitionSupported" type="button" class="vision-web-host__mic" :disabled="voiceState === 'listening'" @click="voiceState === 'listening' ? stopListening() : startListening()">{{ voiceState === 'listening' ? "Stop" : "Mic" }}</button>
        <button type="submit" :disabled="!canSend || isLoading">{{ isLoading ? "…" : "Ask" }}</button>
      </form>
    </div>
  </section>
</template>

<style scoped>
.vision-web-host { position: relative; }
.vision-web-host__toggle { display: inline-flex; align-items: center; gap: .4rem; min-height: var(--control-height-default); padding: .45rem .7rem; border: 1px solid var(--accent); border-radius: var(--radius-control); background: var(--accent); color: var(--canvas); font: inherit; font-weight: var(--text-weight-semibold); cursor: pointer; }
.vision-web-host__orb { display: inline-grid; place-items: center; width: 1.15rem; height: 1.15rem; border-radius: 50%; background: color-mix(in srgb, var(--canvas) 18%, transparent); }
.vision-web-host__panel { position: absolute; z-index: var(--z-popover); top: calc(100% + .5rem); right: 0; display: grid; gap: .75rem; width: min(25rem, calc(100vw - 2rem)); padding: 1rem; border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }
.vision-web-host__header { display: flex; justify-content: space-between; align-items: start; }
.vision-web-host__header div { display: grid; gap: .15rem; }.vision-web-host__header span, .vision-web-host__hint { color: var(--text-muted); font-size: var(--text-size-meta); }.vision-web-host__hint, .vision-web-host__response { margin: 0; }.vision-web-host__response { padding: .65rem; border-radius: var(--radius-control); background: var(--surface-hover); }.vision-web-host__error { margin: 0; color: var(--danger); }.vision-web-host__close { border: 0; background: transparent; color: var(--text-muted); font-size: 1.25rem; cursor: pointer; }.vision-web-host__composer { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; gap: .5rem; }.vision-web-host__composer input, .vision-web-host__composer button { min-height: var(--control-height-default); padding: .5rem; border: 1px solid var(--control-border); border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink); font: inherit; }.vision-web-host__composer button { background: var(--accent); border-color: var(--accent); color: var(--canvas); cursor: pointer; }.vision-web-host__composer button:disabled { opacity: .55; cursor: not-allowed; }
@media (max-width: 640px) { .vision-web-host__panel { position: fixed; top: 4.5rem; right: .75rem; left: .75rem; width: auto; } .vision-web-host__composer { grid-template-columns: minmax(0, 1fr) auto; } .vision-web-host__composer input { grid-column: 1 / -1; } }
@media (prefers-reduced-motion: reduce) { .vision-web-host__orb { animation: none; } }
</style>
