<script setup lang="ts">
import UiSurfaceSection from "./UiSurfaceSection.vue"

withDefaults(defineProps<{
  title: string
  emptyMessage: string
  hasItems: boolean
  compact?: boolean
}>(), {
  compact: true,
})
</script>

<template>
  <UiSurfaceSection :title="title" :compact="compact">
    <slot v-if="hasItems" />
    <div v-else class="empty-state">{{ emptyMessage }}</div>
  </UiSurfaceSection>
</template>
