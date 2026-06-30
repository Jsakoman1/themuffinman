<script setup lang="ts">
import type {VisionCanvasBlock} from "../api/visionApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  block: VisionCanvasBlock
}>()

const emit = defineEmits<{
  choice: [value: string]
}>()
</script>

<template>
  <VisionCanvasSection :title="block.title" :body="block.body" tone="field">
    <div v-if="block.options.length" class="vision-choice-list">
      <button
        v-for="option in block.options"
        :key="option.id"
        type="button"
        class="vision-choice-chip"
        @click="emit('choice', option.value ?? option.label)"
      >
        {{ option.label }}
      </button>
    </div>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-choice-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

.vision-choice-chip {
  appearance: none;
  border: 1px solid rgba(24, 36, 47, 0.08);
  border-radius: 999px;
  padding: 0.65rem 0.95rem;
  background: rgba(255, 255, 255, 0.92);
  color: #18242f;
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.vision-choice-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(24, 36, 47, 0.08);
}
</style>
