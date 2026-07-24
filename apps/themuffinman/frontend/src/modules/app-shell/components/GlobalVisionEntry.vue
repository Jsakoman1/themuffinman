<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter, type RouteLocationRaw} from "vue-router"

const props = defineProps<{
  context: string
  placeholder: string
  contextualRoute: RouteLocationRaw
}>()

const router = useRouter()
const prompt = ref("")
const isListening = ref(false)
const error = ref("")
type RecognitionEvent = {results: ArrayLike<ArrayLike<{transcript: string}>>}
type Recognition = {lang: string; interimResults: boolean; maxAlternatives: number; onstart: (() => void) | null; onend: (() => void) | null; onerror: (() => void) | null; onresult: ((event: RecognitionEvent) => void) | null; start: () => void}
type RecognitionConstructor = new () => Recognition
const hasSpeechRecognition = computed(() => typeof window !== "undefined" && Boolean((window as Window & {SpeechRecognition?: RecognitionConstructor; webkitSpeechRecognition?: RecognitionConstructor}).SpeechRecognition || (window as Window & {webkitSpeechRecognition?: RecognitionConstructor}).webkitSpeechRecognition))

const openVision = async () => {
  const value = prompt.value.trim()
  await router.push(value ? {path: "/home", query: {visionPrompt: value, visionAutorun: "1", visionContext: props.context, visionReturnTo: window.location.pathname}} : props.contextualRoute)
}

const startListening = () => {
  error.value = ""
  if (!hasSpeechRecognition.value) {
    error.value = "Microphone input is not supported here. You can still use text."
    return
  }

  const SpeechRecognition = (window as Window & {SpeechRecognition?: RecognitionConstructor; webkitSpeechRecognition?: RecognitionConstructor}).SpeechRecognition
    || (window as Window & {webkitSpeechRecognition?: RecognitionConstructor}).webkitSpeechRecognition
  if (!SpeechRecognition) return
  const recognition = new SpeechRecognition()
  recognition.lang = navigator.language || "en-US"
  recognition.interimResults = false
  recognition.maxAlternatives = 1
  recognition.onstart = () => { isListening.value = true }
  recognition.onend = () => { isListening.value = false }
  recognition.onerror = () => { isListening.value = false; error.value = "Microphone input was unavailable. Try text instead." }
  recognition.onresult = (event) => { prompt.value = event.results[0]?.[0]?.transcript ?? "" }
  recognition.start()
}
</script>

<template>
  <details class="global-vision-entry">
    <summary class="global-vision-entry__summary" aria-label="Open Vision">Vision</summary>
    <form class="global-vision-entry__panel" @submit.prevent="openVision">
      <div>
        <strong>Vision</strong>
        <p>Ask by text or voice from anywhere.</p>
      </div>
      <div class="global-vision-entry__composer">
        <input v-model="prompt" type="text" :placeholder="placeholder" aria-label="Ask Vision">
        <button type="button" class="global-vision-entry__mic" :aria-pressed="isListening" :disabled="isListening" @click="startListening">{{ isListening ? "Listening…" : "Mic" }}</button>
        <button type="submit">Open</button>
      </div>
      <p v-if="error" class="global-vision-entry__error" role="alert">{{ error }}</p>
    </form>
  </details>
</template>

<style scoped>
.global-vision-entry { position: relative; }
.global-vision-entry__summary { cursor: pointer; list-style: none; min-height: var(--control-height-default); padding: var(--space-1) var(--space-2); border: 1px solid var(--accent); border-radius: var(--radius-control); background: var(--accent); color: var(--canvas); font-weight: var(--text-weight-semibold); }
.global-vision-entry__summary::-webkit-details-marker { display: none; }
.global-vision-entry__summary:hover, .global-vision-entry[open] .global-vision-entry__summary { background: var(--accent-hover, var(--accent)); }
.global-vision-entry__panel { position: absolute; right: 0; top: calc(100% + var(--space-2)); z-index: var(--z-popover); display: grid; gap: var(--space-3); width: min(28rem, calc(100vw - 2rem)); padding: var(--space-3); border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }
.global-vision-entry__panel p { margin: var(--space-1) 0 0; color: var(--text-muted); font-size: var(--text-size-body); }
.global-vision-entry__composer { display: grid; grid-template-columns: minmax(0,1fr) auto auto; gap: var(--space-2); }
.global-vision-entry__composer input, .global-vision-entry__composer button { min-width: 0; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-2); background: var(--control-bg); color: var(--control-ink); font: inherit; }
.global-vision-entry__composer button { cursor: pointer; background: var(--accent); border-color: var(--accent); color: var(--canvas); font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.global-vision-entry__composer .global-vision-entry__mic { background: var(--surface-hover); color: var(--text); }
.global-vision-entry__error { margin: 0; padding: var(--space-2); border: 1px solid color-mix(in srgb, var(--danger) 40%, var(--border-subtle)); border-radius: var(--radius-control); background: var(--danger-muted); color: var(--danger) !important; }
@media(max-width:640px){.global-vision-entry__panel{position:fixed;right:var(--space-3);top:4.5rem}.global-vision-entry__composer{grid-template-columns:1fr 1fr}.global-vision-entry__composer input{grid-column:1/-1}}
</style>
