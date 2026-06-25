<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  leading?: string
}>()
</script>

<template>
  <div class="surface-section__header">
    <div class="surface-section__heading">
      <div v-if="leading" class="surface-section__eyebrow">{{ leading }}</div>
      <h2 class="surface-section__title">{{ title }}</h2>
      <p v-if="subtitle" class="surface-section__subtitle">{{ subtitle }}</p>
    </div>

    <div v-if="$slots.actions" class="surface-section__actions">
      <slot name="actions" />
    </div>
  </div>
</template>
