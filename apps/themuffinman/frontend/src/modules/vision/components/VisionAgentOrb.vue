<script setup lang="ts">
import type {VisionAttentionState, VisionVoiceState} from "../composables/useVisionConversation.ts"

defineProps<{
  voiceState: VisionVoiceState
  attentionState: VisionAttentionState
}>()
</script>

<template>
  <div class="vision-agent" :class="[`vision-agent--${voiceState}`, `vision-agent--${attentionState}`]">
    <span class="vision-agent__field"></span>
    <span class="vision-agent__halo"></span>
    <span class="vision-agent__drift"></span>
    <span class="vision-agent__ring vision-agent__ring--outer"></span>
    <span class="vision-agent__ring vision-agent__ring--middle"></span>
    <span class="vision-agent__ring vision-agent__ring--inner"></span>
    <span class="vision-agent__pulse"></span>
    <span class="vision-agent__core"></span>
    <span class="vision-agent__spark vision-agent__spark--one"></span>
    <span class="vision-agent__spark vision-agent__spark--two"></span>
    <span class="vision-agent__spark vision-agent__spark--three"></span>
  </div>
</template>

<style scoped>
.vision-agent {
  position: relative;
  width: min(46rem, 96vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  animation: orb-float 9s ease-in-out infinite;
}

.vision-agent__field,
.vision-agent__halo,
.vision-agent__drift,
.vision-agent__ring,
.vision-agent__pulse,
.vision-agent__core,
.vision-agent__spark {
  position: absolute;
  border-radius: 50%;
}

.vision-agent__field {
  inset: 0;
  background:
    radial-gradient(circle at 50% 50%, rgba(255, 245, 236, 0.72), transparent 34%),
    conic-gradient(
      from 0deg,
      rgba(255, 172, 134, 0.72),
      rgba(120, 197, 255, 0.62),
      rgba(255, 216, 175, 0.42),
      var(--vision-surface-orb-field-start)
    );
  filter: blur(22px);
  opacity: 0.98;
  animation: field-drift 18s linear infinite;
}

.vision-agent__halo {
  inset: 8%;
  background: radial-gradient(circle, rgba(118, 190, 255, 0.44), rgba(118, 190, 255, 0.14) 50%, transparent 72%);
  filter: blur(14px);
  animation: halo-breathe 8.5s ease-in-out infinite;
}

.vision-agent__drift {
  inset: 17%;
  background:
    radial-gradient(circle at 30% 30%, rgba(255, 189, 147, 0.32), transparent 20%),
    radial-gradient(circle at 68% 72%, rgba(135, 205, 255, 0.32), transparent 24%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.24), transparent 46%);
  mix-blend-mode: screen;
  filter: blur(8px);
  animation: drift-breathe 10s ease-in-out infinite;
}

.vision-agent__ring {
  inset: 12%;
  border: 1px solid var(--vision-surface-border);
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
  border-color: var(--vision-surface-accent-wash);
  animation: pulse-ring 7s ease-in-out infinite;
}

.vision-agent__pulse {
  inset: 28%;
  background: radial-gradient(circle, rgba(255, 174, 137, 0.48), rgba(125, 195, 255, 0.22) 58%, transparent 76%);
  animation: pulse-core 5.5s ease-in-out infinite;
}

.vision-agent__core {
  inset: 38%;
  background: linear-gradient(145deg, rgba(255, 230, 215, 0.98) 0%, rgba(209, 233, 255, 0.98) 100%);
  box-shadow:
    0 0 0 1px var(--vision-surface-border-soft),
    0 32px 80px rgba(89, 145, 189, 0.3),
    inset 0 0 36px rgba(255, 255, 255, 0.88);
  animation: core-breathe 6.5s ease-in-out infinite;
}

.vision-agent--listening .vision-agent__core {
  box-shadow:
    0 0 0 1px var(--vision-surface-border-soft),
    0 28px 72px var(--vision-surface-orb-core-shadow-listening),
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

.vision-agent__spark {
  width: 0.6rem;
  height: 0.6rem;
  background: rgba(255, 252, 250, 0.96);
  box-shadow: 0 0 24px rgba(123, 192, 255, 0.6);
  opacity: 0.8;
}

.vision-agent__spark--one {
  top: 20%;
  left: 22%;
  animation: spark-drift-one 10s ease-in-out infinite;
}

.vision-agent__spark--two {
  right: 23%;
  top: 24%;
  animation: spark-drift-two 12s ease-in-out infinite;
}

.vision-agent__spark--three {
  bottom: 22%;
  right: 32%;
  animation: spark-drift-three 11s ease-in-out infinite;
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

@keyframes orb-float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.01); }
}

@keyframes field-drift {
  0% { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(180deg) scale(1.03); }
  100% { transform: rotate(360deg) scale(1); }
}

@keyframes drift-breathe {
  0%, 100% { transform: scale(0.97) rotate(0deg); opacity: 0.62; }
  50% { transform: scale(1.05) rotate(18deg); opacity: 0.95; }
}

@keyframes core-breathe {
  0%, 100% { transform: scale(0.98); }
  50% { transform: scale(1.03); }
}

@keyframes spark-drift-one {
  0%, 100% { transform: translate(0, 0) scale(0.92); opacity: 0.62; }
  50% { transform: translate(12px, -8px) scale(1.08); opacity: 1; }
}

@keyframes spark-drift-two {
  0%, 100% { transform: translate(0, 0) scale(0.9); opacity: 0.55; }
  50% { transform: translate(-10px, 10px) scale(1.12); opacity: 0.98; }
}

@keyframes spark-drift-three {
  0%, 100% { transform: translate(0, 0) scale(0.94); opacity: 0.58; }
  50% { transform: translate(-14px, -6px) scale(1.1); opacity: 1; }
}
</style>
