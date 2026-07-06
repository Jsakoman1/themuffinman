<script setup lang="ts">
import VisionTypingText from "./VisionTypingText.vue"

const props = withDefaults(defineProps<{
  label: string
  value: string
  active?: boolean
  animate?: boolean
  clickable?: boolean
  tone?: "default" | "muted" | "strong"
}>(), {
  active: false,
  animate: false,
  clickable: false,
  tone: "default"
})

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <button
    v-if="clickable"
    type="button"
    class="vision-row"
    :class="[
      `vision-row--${tone}`,
      { 'vision-row--active': active }
    ]"
    @click="emit('click')"
  >
    <span class="vision-row__label">{{ label }}:</span>
    <span class="vision-row__value">
      <VisionTypingText v-if="animate" :text="value" :active="active" :speed="28" />
      <template v-else>{{ value }}</template>
    </span>
  </button>

  <div
    v-else
    class="vision-row"
    :class="[
      `vision-row--${tone}`,
      { 'vision-row--active': active }
    ]"
  >
    <span class="vision-row__label">{{ label }}:</span>
    <span class="vision-row__value">
      <VisionTypingText v-if="animate" :text="value" :active="active" :speed="28" />
      <template v-else>{{ value }}</template>
    </span>
  </div>
</template>

<style scoped>
.vision-row {
  appearance: none;
  border: 0;
  background: transparent;
  width: 100%;
  padding: 0.14rem 0;
  margin: 0;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 0.35rem;
  align-items: baseline;
  text-align: left;
  color: inherit;
  font: inherit;
}

.vision-row + .vision-row {
  border-top: 1px solid rgba(24, 36, 47, 0.04);
}

.vision-row--muted {
  color: rgba(24, 36, 47, 0.72);
}

.vision-row--strong {
  color: var(--vision-surface-ink);
}

.vision-row__label {
  color: rgba(24, 36, 47, 0.48);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 0.62rem;
  white-space: nowrap;
}

.vision-row--muted .vision-row__label {
  color: rgba(24, 36, 47, 0.42);
}

.vision-row__value {
  color: var(--vision-surface-ink);
  font-size: 0.9rem;
  line-height: 1.35;
  white-space: pre-wrap;
  font-family: "SFMono-Regular", "SF Mono", "Menlo", "Monaco", "Consolas", monospace;
}

.vision-row--active .vision-row__value {
  text-shadow: 0 0 18px rgba(127, 203, 255, 0.14);
}
</style>
