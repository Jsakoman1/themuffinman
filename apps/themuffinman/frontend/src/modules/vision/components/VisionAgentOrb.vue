<script setup lang="ts">
import type {VisionAttentionState, VisionVoiceState} from "../composables/useVisionConversation.ts"

defineProps<{
  voiceState: VisionVoiceState
  attentionState: VisionAttentionState
}>()
</script>

<template>
  <div class="vision-agent" :class="[`vision-agent--${voiceState}`, `vision-agent--${attentionState}`]">
    <span class="vision-agent__halo"></span>
    <span class="vision-agent__ring vision-agent__ring--outer"></span>
    <span class="vision-agent__ring vision-agent__ring--middle"></span>
    <span class="vision-agent__ring vision-agent__ring--inner"></span>
    <span class="vision-agent__pulse"></span>
    <span class="vision-agent__core"></span>
  </div>
</template>

<style scoped>
.vision-agent {
  position: relative;
  width: min(25rem, 76vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
}

.vision-agent__halo,
.vision-agent__ring,
.vision-agent__pulse,
.vision-agent__core {
  position: absolute;
  border-radius: 50%;
}

.vision-agent__halo {
  inset: 8%;
  background: radial-gradient(circle, rgba(118, 190, 255, 0.32), rgba(118, 190, 255, 0.06) 48%, transparent 70%);
  filter: blur(10px);
  animation: halo-breathe 9s ease-in-out infinite;
}

.vision-agent__ring {
  inset: 12%;
  border: 1px solid rgba(24, 36, 47, 0.08);
}

.vision-agent__ring--outer {
  animation: rotate-slow 18s linear infinite;
}

.vision-agent__ring--middle {
  inset: 21%;
  border-style: dashed;
  animation: rotate-reverse 14s linear infinite;
}

.vision-agent__ring--inner {
  inset: 31%;
  border-color: rgba(255, 153, 112, 0.24);
  animation: pulse-ring 7s ease-in-out infinite;
}

.vision-agent__pulse {
  inset: 28%;
  background: radial-gradient(circle, rgba(255, 175, 132, 0.18), rgba(125, 195, 255, 0.08) 58%, transparent 76%);
  animation: pulse-core 5.5s ease-in-out infinite;
}

.vision-agent__core {
  inset: 38%;
  background: linear-gradient(145deg, #fff4ed 0%, #e6f5ff 100%);
  box-shadow:
    0 0 0 1px rgba(24, 36, 47, 0.06),
    0 24px 60px rgba(77, 130, 168, 0.2),
    inset 0 0 32px rgba(255, 255, 255, 0.9);
}

.vision-agent--listening .vision-agent__core {
  box-shadow:
    0 0 0 1px rgba(24, 36, 47, 0.06),
    0 28px 72px rgba(255, 160, 122, 0.26),
    inset 0 0 38px rgba(255, 255, 255, 0.95);
}

.vision-agent--processing .vision-agent__ring--outer,
.vision-agent--processing .vision-agent__ring--middle {
  animation-duration: 7s;
}

.vision-agent--review .vision-agent__core {
  background: linear-gradient(145deg, #fff8f0 0%, #f3fbff 100%);
}

.vision-agent--blocked .vision-agent__core {
  background: linear-gradient(145deg, #fff0ef 0%, #fff8f7 100%);
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes rotate-reverse {
  from { transform: rotate(360deg); }
  to { transform: rotate(0deg); }
}

@keyframes halo-breathe {
  0%, 100% { transform: scale(0.96); opacity: 0.72; }
  50% { transform: scale(1.04); opacity: 1; }
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(0.96); opacity: 0.45; }
  50% { transform: scale(1.04); opacity: 0.9; }
}

@keyframes pulse-core {
  0%, 100% { transform: scale(0.95); opacity: 0.72; }
  50% { transform: scale(1.03); opacity: 1; }
}
</style>
