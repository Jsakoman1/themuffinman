<script setup lang="ts">
import type {VisionAttentionState, VisionVoiceState} from "../composables/useVisionConversation.ts"

defineProps<{
  voiceState: VisionVoiceState
  attentionState: VisionAttentionState
  active?: boolean
  intensity?: number
  voiceIntensity?: number
  typingIntensity?: number
}>()
</script>

<template>
  <div
    class="vision-agent"
    :class="[`vision-agent--${voiceState}`, `vision-agent--${attentionState}`, { 'vision-agent--active': active }]"
    :style="{
      '--vision-agent-intensity': intensity ?? 0,
      '--vision-agent-voice-intensity': voiceIntensity ?? 0,
      '--vision-agent-typing-intensity': typingIntensity ?? 0
    }"
  >
    <span class="vision-agent__field"></span>
    <span class="vision-agent__halo"></span>
    <span class="vision-agent__drift"></span>
    <span class="vision-agent__pulse"></span>
    <span class="vision-agent__core"></span>
  </div>
</template>

<style scoped>
.vision-agent {
  position: relative;
  width: min(50rem, 102vw);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  animation:
    orb-float 11s ease-in-out infinite,
    hue-shift 28s ease-in-out infinite;
  --vision-agent-intensity: 0;
  --vision-agent-voice-intensity: 0;
  --vision-agent-typing-intensity: 0;
}

.vision-agent--active {
  transform: scale(calc(1 + (var(--vision-agent-intensity) * 0.015)));
}

.vision-agent__field,
.vision-agent__halo,
.vision-agent__drift,
.vision-agent__pulse,
.vision-agent__core {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.vision-agent__field {
  inset: 8%;
  background:
    radial-gradient(circle at 28% 30%, rgba(255, 223, 151, 0.8), transparent 22%),
    radial-gradient(circle at 72% 28%, rgba(255, 174, 157, 0.62), transparent 20%),
    radial-gradient(circle at 66% 72%, rgba(141, 214, 169, 0.58), transparent 24%),
    radial-gradient(circle at 34% 72%, rgba(137, 206, 255, 0.62), transparent 25%),
    radial-gradient(circle at 50% 50%, rgba(255, 248, 235, 0.5), transparent 44%);
  filter: blur(10px);
  opacity: 0.82;
  animation: field-breathe 16s ease-in-out infinite;
}

.vision-agent--active .vision-agent__field {
  opacity: calc(0.82 + (var(--vision-agent-intensity) * 0.1));
  filter: blur(calc(10px - (var(--vision-agent-intensity) * 2px)));
}

.vision-agent__halo {
  inset: 14%;
  background:
    radial-gradient(circle at 50% 50%, rgba(255, 238, 192, 0.58), rgba(255, 213, 171, 0.38) 34%, transparent 68%);
  filter: blur(10px);
  opacity: 0.78;
  animation: halo-breathe 10s ease-in-out infinite;
}

.vision-agent--active .vision-agent__halo {
  opacity: calc(0.78 + (var(--vision-agent-intensity) * 0.08));
}

.vision-agent__drift {
  inset: 19%;
  background:
    radial-gradient(circle at 30% 34%, rgba(255, 194, 155, 0.6), transparent 18%),
    radial-gradient(circle at 70% 34%, rgba(255, 232, 150, 0.58), transparent 18%),
    radial-gradient(circle at 74% 66%, rgba(135, 205, 255, 0.5), transparent 20%),
    radial-gradient(circle at 28% 68%, rgba(150, 224, 182, 0.54), transparent 18%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.1), transparent 50%);
  filter: blur(5px);
  opacity: 0.98;
  animation: drift-breathe 12s ease-in-out infinite;
}

.vision-agent--active .vision-agent__drift {
  opacity: calc(0.98 + (var(--vision-agent-intensity) * 0.04));
  filter: blur(calc(5px - (var(--vision-agent-intensity) * 0.8px)));
}

.vision-agent__pulse {
  inset: 29%;
  background:
    radial-gradient(circle at 50% 46%, rgba(255, 250, 235, 0.82), transparent 20%),
    radial-gradient(circle at 42% 60%, rgba(255, 177, 141, 0.48), transparent 30%),
    radial-gradient(circle at 60% 58%, rgba(138, 209, 255, 0.4), transparent 30%),
    radial-gradient(circle at 50% 50%, rgba(255, 236, 192, 0.34), transparent 64%);
  filter: blur(3px);
  opacity: 0.96;
  animation: pulse-bloom 7.5s ease-in-out infinite;
}

.vision-agent--active .vision-agent__pulse {
  opacity: calc(0.96 + (var(--vision-agent-intensity) * 0.04));
}

.vision-agent__core {
  inset: 36%;
  background:
    radial-gradient(circle at 46% 40%, rgba(255, 255, 255, 0.86), transparent 20%),
    radial-gradient(circle at 55% 58%, rgba(255, 240, 208, 0.66), rgba(255, 240, 208, 0.24) 26%, transparent 58%),
    radial-gradient(circle at 46% 60%, rgba(255, 190, 155, 0.44), transparent 62%),
    radial-gradient(circle at 60% 42%, rgba(141, 214, 255, 0.34), transparent 42%),
    radial-gradient(circle at 50% 50%, rgba(255, 248, 239, 0.32), transparent 72%);
  filter: blur(0);
  opacity: 0.94;
  animation: core-breathe 6.5s ease-in-out infinite;
}

.vision-agent--active .vision-agent__core {
  opacity: calc(0.94 + (var(--vision-agent-intensity) * 0.02));
}

.vision-agent--listening .vision-agent__field {
  background:
    radial-gradient(circle at 28% 30%, rgba(255, 229, 159, 0.3), transparent 22%),
    radial-gradient(circle at 70% 30%, rgba(255, 170, 154, 0.26), transparent 20%),
    radial-gradient(circle at 66% 72%, rgba(144, 224, 171, 0.2), transparent 24%),
    radial-gradient(circle at 34% 72%, rgba(137, 206, 255, 0.22), transparent 25%),
    radial-gradient(circle at 50% 50%, rgba(255, 249, 238, 0.2), transparent 44%);
}

.vision-agent--processing .vision-agent__drift {
  background:
    radial-gradient(circle at 30% 34%, rgba(255, 198, 150, 0.2), transparent 18%),
    radial-gradient(circle at 70% 34%, rgba(255, 236, 164, 0.24), transparent 18%),
    radial-gradient(circle at 74% 66%, rgba(144, 217, 255, 0.24), transparent 20%),
    radial-gradient(circle at 28% 68%, rgba(157, 232, 186, 0.24), transparent 18%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.08), transparent 50%);
}

.vision-agent--review .vision-agent__pulse {
  background:
    radial-gradient(circle at 50% 46%, rgba(255, 251, 238, 0.52), transparent 20%),
    radial-gradient(circle at 42% 60%, rgba(255, 218, 166, 0.26), transparent 30%),
    radial-gradient(circle at 60% 58%, rgba(155, 232, 255, 0.18), transparent 30%),
    radial-gradient(circle at 50% 50%, rgba(255, 239, 201, 0.14), transparent 64%);
}

.vision-agent--blocked .vision-agent__core {
  background:
    radial-gradient(circle at 46% 40%, rgba(255, 255, 255, 0.38), transparent 20%),
    radial-gradient(circle at 55% 58%, rgba(255, 225, 215, 0.32), rgba(255, 225, 215, 0.08) 26%, transparent 58%),
    radial-gradient(circle at 46% 60%, rgba(255, 185, 168, 0.2), transparent 62%),
    radial-gradient(circle at 60% 42%, rgba(247, 214, 255, 0.12), transparent 42%),
    radial-gradient(circle at 50% 50%, rgba(255, 248, 239, 0.12), transparent 72%);
}

@keyframes orb-float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.01); }
}

@keyframes hue-shift {
  0% {
    filter: hue-rotate(0deg) saturate(0.98) brightness(1);
  }
  25% {
    filter: hue-rotate(8deg) saturate(1.02) brightness(1.01);
  }
  50% {
    filter: hue-rotate(-6deg) saturate(1.04) brightness(1.02);
  }
  75% {
    filter: hue-rotate(10deg) saturate(1.01) brightness(1);
  }
  100% {
    filter: hue-rotate(0deg) saturate(0.98) brightness(1);
  }
}

@keyframes field-breathe {
  0%, 100% { transform: scale(0.98); opacity: 0.3; }
  50% { transform: scale(1.03); opacity: 0.46; }
}

@keyframes halo-breathe {
  0%, 100% { transform: scale(0.97); opacity: 0.32; }
  50% { transform: scale(1.05); opacity: 0.48; }
}

@keyframes drift-breathe {
  0%, 100% { transform: scale(0.98) rotate(0deg); opacity: 0.62; }
  50% { transform: scale(1.04) rotate(14deg); opacity: 0.82; }
}

@keyframes pulse-bloom {
  0%, 100% { transform: scale(0.96); opacity: 0.46; }
  50% { transform: scale(1.05); opacity: 0.68; }
}

@keyframes core-breathe {
  0%, 100% { transform: scale(0.97); opacity: 0.38; }
  50% { transform: scale(1.04); opacity: 0.56; }
}
</style>
