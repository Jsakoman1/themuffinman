<script setup lang="ts">
import type {VisionCanvasBlock} from "../api/visionConversationApi.ts"
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
    <div v-if="block.options.length" class="vision-field-request">
      <button
        v-for="option in block.options"
        :key="option.id"
        type="button"
        class="vision-field-request__chip"
        @click="emit('choice', option.value ?? option.label)"
      >
        {{ option.label }}
      </button>
    </div>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-field-request {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.vision-field-request__chip {
  appearance: none;
  border: 1px solid rgba(24, 36, 47, 0.08);
  border-radius: 999px;
  padding: 0.68rem 1rem;
  background: rgba(255, 255, 255, 0.9);
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.vision-field-request__chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(24, 36, 47, 0.08);
  border-color: rgba(24, 36, 47, 0.14);
}
</style>
