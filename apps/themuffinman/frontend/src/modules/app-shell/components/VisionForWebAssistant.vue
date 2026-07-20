<script setup lang="ts">
import {computed} from "vue"

const props = withDefaults(defineProps<{
  state?: "idle" | "attention" | "listening" | "thinking" | "navigating" | "result" | "clarification" | "unavailable" | "recovery"
  label?: string
}>(), {state: "idle", label: "Vision assistant"})

const stateLabel = computed(() => ({
  idle: "Ready",
  attention: "Needs your attention",
  listening: "Listening",
  thinking: "Thinking",
  navigating: "Opening",
  result: "Completed",
  clarification: "Needs clarification",
  unavailable: "Temporarily unavailable",
  recovery: "Ready to retry"
}[props.state]))
</script>

<template>
  <span class="vision-assistant" :data-state="state" role="img" :aria-label="`${label}: ${stateLabel}`">
    <span class="vision-assistant__spark" aria-hidden="true">✦</span>
    <span class="vision-assistant__pulse" aria-hidden="true" />
  </span>
</template>

<style scoped>
.vision-assistant { position: relative; display: inline-grid; place-items: center; width: 1.15rem; height: 1.15rem; flex: 0 0 auto; }
.vision-assistant__spark { position: relative; z-index: 1; display: grid; place-items: center; width: 100%; height: 100%; border-radius: 50%; background: color-mix(in srgb, var(--canvas) 18%, transparent); font-size: .75rem; }
.vision-assistant__pulse { position: absolute; inset: 0; border: 1px solid color-mix(in srgb, var(--canvas) 60%, transparent); border-radius: 50%; opacity: .5; animation: vision-assistant-breathe 2.2s ease-in-out infinite; }
.vision-assistant[data-state="listening"] .vision-assistant__pulse, .vision-assistant[data-state="thinking"] .vision-assistant__pulse { animation-duration: .9s; }
.vision-assistant[data-state="unavailable"] .vision-assistant__spark, .vision-assistant[data-state="recovery"] .vision-assistant__spark { opacity: .7; }
@keyframes vision-assistant-breathe { 0%, 100% { transform: scale(.82); opacity: .25; } 50% { transform: scale(1.18); opacity: .7; } }
@media (prefers-reduced-motion: reduce) { .vision-assistant__pulse { animation: none; opacity: .45; } }
</style>
