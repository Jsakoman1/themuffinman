<script setup lang="ts">
import type {VisionCanvasBlock} from "../api/visionConversationApi.ts"
import VisionCanvasSection from "./VisionCanvasSection.vue"

defineProps<{
  block: VisionCanvasBlock
}>()
</script>

<template>
  <VisionCanvasSection :title="block.title" :body="block.body">
    <div v-if="block.items.length" class="vision-slot-list">
      <article v-for="slot in block.items" :key="slot.slotId" class="vision-slot-card">
        <span class="vision-slot-card__label">{{ slot.label }}</span>
        <p class="vision-slot-card__value">{{ slot.value }}</p>
      </article>
    </div>
  </VisionCanvasSection>
</template>

<style scoped>
.vision-slot-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(11rem, 1fr));
  gap: 0.65rem;
}

.vision-slot-card {
  border-radius: 1.25rem;
  padding: 0.95rem 1rem;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(24, 36, 47, 0.07);
  box-shadow: 0 12px 24px rgba(24, 36, 47, 0.04);
}

.vision-slot-card__label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--vision-surface-ink-muted);
}

.vision-slot-card__value {
  margin: 0;
  color: var(--vision-surface-ink);
}
</style>
