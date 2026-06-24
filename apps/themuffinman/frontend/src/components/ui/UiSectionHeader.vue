<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  leading?: string
}>()
</script>

<template>
  <div class="card__header">
    <div class="card__header-main">
      <span v-if="leading" class="card__header-leading">{{ leading }}</span>
      <h2 class="card__title">{{ title }}</h2>
      <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
    </div>

    <div v-if="$slots.actions" class="card__header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>
