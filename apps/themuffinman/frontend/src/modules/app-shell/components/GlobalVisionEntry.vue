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
  await router.push(value ? {path: "/vision", query: {prompt: value, autorun: "1", context: props.context, returnTo: window.location.pathname}} : props.contextualRoute)
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
.global-vision-entry{position:relative}.global-vision-entry__summary{cursor:pointer;list-style:none;padding:.7rem .85rem;border:1px solid var(--border-subtle);border-radius:999px;background:rgba(23,34,26,.96);color:var(--text);font-weight:650}.global-vision-entry__summary::-webkit-details-marker{display:none}.global-vision-entry__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;display:grid;gap:.75rem;width:min(28rem,calc(100vw - 2rem));padding:1rem;border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface);box-shadow:0 18px 38px rgba(23,34,26,.16)}.global-vision-entry__panel p{margin:.25rem 0 0;color:var(--text-muted);font-size:.82rem}.global-vision-entry__composer{display:grid;grid-template-columns:minmax(0,1fr) auto auto;gap:.4rem}.global-vision-entry__composer input,.global-vision-entry__composer button{min-width:0;border:1px solid var(--border-subtle);border-radius:.7rem;padding:.6rem .7rem;font:inherit}.global-vision-entry__composer button{cursor:pointer;background:var(--accent);color:var(--text);font-size:.8rem;font-weight:650}.global-vision-entry__composer .global-vision-entry__mic{background:var(--surface-muted);color:var(--text)}.global-vision-entry__error{color:var(--danger)!important}@media(max-width:640px){.global-vision-entry__panel{position:fixed;right:1rem;top:4.5rem}.global-vision-entry__composer{grid-template-columns:1fr 1fr}.global-vision-entry__composer input{grid-column:1/-1}}
</style>
