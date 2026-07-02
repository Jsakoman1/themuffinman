<script setup lang="ts">
import {computed} from "vue"
import type {VisionCanvasBlock, VisionConversationTurnResponse} from "../api/visionConversationApi.ts"
import type {VisionExecutionCandidate} from "../api/visionConversationApi.ts"
import VisionTypingText from "./VisionTypingText.vue"

const props = defineProps<{
  response: VisionConversationTurnResponse | null
  executionCandidate: VisionExecutionCandidate | null
  visible: boolean
}>()

type IntentField = {
  slotId: string
  label: string
  value: string
}

const previewBlock = computed<VisionCanvasBlock | null>(() => {
  if (!props.response) {
    return null
  }

  const preferredBlock = props.response.blocks.find((block) =>
    block.items.length > 0
    && block.type === "result_summary"
    && block.title !== "Collected so far")

  if (preferredBlock) {
    return preferredBlock
  }

  const fallbackBlock = props.response.blocks.find((block) =>
    block.items.length > 0
    && (block.type === "result_summary" || block.type === "review_summary" || block.type === "success" || block.type === "info"))

  return fallbackBlock ?? null
})

const fieldValues = computed<IntentField[]>(() => {
  if (previewBlock.value?.items.length) {
    return previewBlock.value.items.map((item) => ({
      slotId: item.slotId,
      label: item.label,
      value: item.value ?? ""
    }))
  }

  const review = props.response?.review
  if (review) {
    return [
      {slotId: "quest_title", label: "Title", value: review.title ?? ""},
      {slotId: "quest_description", label: "Description", value: review.description ?? ""},
      {slotId: "reward_amount", label: "Reward", value: review.rewardLabel ?? ""},
      {slotId: "visibility", label: "Visibility", value: review.visibility ?? ""},
      {slotId: "scheduled_at", label: "Schedule", value: review.schedule ?? ""},
      {slotId: "location_label", label: "Location", value: review.location ?? ""}
    ].filter((field) => field.value.trim().length > 0)
  }

  return (props.response?.slotSummaries ?? []).map((item) => ({
    slotId: item.slotId,
    label: item.label,
    value: item.value ?? ""
  }))
})

const filledCount = computed(() => fieldValues.value.filter((field) => field.value.trim().length > 0).length)
const totalCount = computed(() => fieldValues.value.length)

const isComplete = computed(() => {
  const mode = props.response?.canvasMode

  return mode === "review" || mode === "complete" || mode === "results"
})

const visibleFields = computed(() => fieldValues.value.filter((field) => field.value.trim().length > 0 || !isComplete.value))

const previewSummary = computed(() => previewBlock.value?.body?.trim() ?? "")

const previewLabel = computed(() => {
  if (!props.visible) {
    return ""
  }

  if (previewBlock.value?.title?.trim()) {
    return previewBlock.value.title.trim()
  }

  if (props.response?.intent) {
    return props.response.intent.toLowerCase()
  }

  return "vision preview"
})
</script>

<template>
  <section class="vision-intent" :class="{
    'vision-intent--visible': visible,
    'vision-intent--ready': filledCount === totalCount,
    'vision-intent--done': isComplete
  }">
    <div class="vision-intent__orb" aria-hidden="true"></div>

    <article class="vision-intent__ghost">
      <div v-if="previewLabel" class="vision-intent__label">
        {{ previewLabel }}
      </div>
      <p v-if="previewSummary" class="vision-intent__summary">
        {{ previewSummary }}
      </p>
      <div
        v-for="field in visibleFields"
        :key="field.slotId"
        class="vision-intent__line"
        :class="{ 'vision-intent__line--filled': field.value.trim().length > 0 }"
      >
        <span class="vision-intent__key">{{ field.label }}:</span>
        <span v-if="field.value.trim().length > 0" class="vision-intent__value">
          <VisionTypingText
            :text="field.value"
            :active="visible && !isComplete"
            :speed="38"
          />
        </span>
        <span v-else class="vision-intent__placeholder">
          <span class="vision-intent__placeholder-ink"></span>
        </span>
      </div>
    </article>
  </section>
</template>

<style scoped>
.vision-intent {
  position: relative;
  width: 100%;
  min-height: 0;
  display: grid;
  place-items: center end;
  padding: 0;
  pointer-events: none;
}

.vision-intent--visible {
  opacity: 1;
}

.vision-intent--done {
  opacity: 0.14;
}

.vision-intent__orb {
  position: absolute;
  inset: 0 0 auto auto;
  margin: auto;
  width: min(28rem, 92%);
  aspect-ratio: 1;
  border-radius: 50%;
  background:
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.9), transparent 28%),
    radial-gradient(circle at 48% 46%, rgba(255, 172, 134, 0.18), transparent 40%),
    radial-gradient(circle at 56% 60%, rgba(127, 203, 255, 0.16), transparent 44%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.18), transparent 64%);
  filter: blur(18px);
  opacity: 0.72;
  transform: translate(9%, -3%);
  animation: visionIntentOrbFloat 11s ease-in-out infinite;
}

.vision-intent__ghost {
  position: relative;
  z-index: 1;
  width: min(31rem, 44vw);
  display: grid;
  gap: 0.58rem;
  padding: 1rem 1.05rem;
  border-radius: 1.6rem;
  border: 1px solid rgba(24, 36, 47, 0.05);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.34), rgba(249, 251, 246, 0.18));
  backdrop-filter: blur(14px);
  box-shadow:
    0 26px 56px rgba(24, 36, 47, 0.06),
    inset 0 0 0 1px rgba(255, 255, 255, 0.38);
  opacity: 0;
  transform: translateY(0.85rem) scale(0.985);
  transition: opacity 220ms ease, transform 220ms ease;
}

.vision-intent--visible .vision-intent__ghost {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.vision-intent--done .vision-intent__ghost {
  opacity: 0;
  transform: translateY(0.35rem) scale(0.98);
}

.vision-intent__line {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.55rem;
  align-items: center;
  min-height: 2.55rem;
  padding: 0.15rem 0;
}

.vision-intent__line + .vision-intent__line {
  border-top: 1px solid rgba(24, 36, 47, 0.04);
}

.vision-intent__label {
  color: rgba(24, 36, 47, 0.42);
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  padding-bottom: 0.12rem;
}

.vision-intent__key {
  color: rgba(24, 36, 47, 0.48);
  font-size: 0.78rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  white-space: nowrap;
}

.vision-intent__summary {
  margin: 0;
  color: rgba(24, 36, 47, 0.62);
  font-size: 0.88rem;
  line-height: 1.5;
}

.vision-intent__line--filled .vision-intent__value {
  text-shadow: 0 0 18px rgba(127, 203, 255, 0.14);
}

.vision-intent__value {
  color: var(--vision-surface-ink);
  font-size: 0.98rem;
  line-height: 1.35;
  font-weight: 500;
  letter-spacing: -0.01em;
  white-space: pre-wrap;
  font-family: "Bradley Hand", "Segoe Print", "Comic Sans MS", "Snell Roundhand", cursive;
  transform: rotate(-0.35deg);
}

.vision-intent__placeholder {
  display: block;
  min-height: 1rem;
  opacity: 0.18;
}

.vision-intent__placeholder-ink {
  display: inline-block;
  width: 7rem;
  height: 0.82rem;
  border-bottom: 1px solid rgba(24, 36, 47, 0.18);
  transform: rotate(-0.6deg);
}

.vision-intent--ready .vision-intent__ghost {
  box-shadow:
    0 30px 72px rgba(24, 36, 47, 0.08),
    inset 0 0 0 1px rgba(255, 255, 255, 0.46);
}

@keyframes visionIntentOrbFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
  }

  50% {
    transform: translateY(-8px) scale(1.02);
  }
}
</style>
