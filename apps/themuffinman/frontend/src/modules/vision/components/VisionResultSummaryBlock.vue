<script setup lang="ts">
import type {VisionCanvasBlock} from "../api/visionApi.ts"
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
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 0.8rem;
}

.vision-slot-card {
  border-radius: 1.2rem;
  padding: 0.95rem 1rem;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(24, 36, 47, 0.05);
}

.vision-slot-card__label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: rgba(24, 36, 47, 0.46);
}

.vision-slot-card__value {
  margin: 0;
  color: #18242f;
}
</style>
