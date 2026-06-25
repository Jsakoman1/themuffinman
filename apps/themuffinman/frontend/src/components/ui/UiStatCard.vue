<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  hint?: string
}>()
</script>

<template>
  <article class="ui-stat-card">
    <span class="ui-stat-card__label">{{ label }}</span>
    <strong class="ui-stat-card__value">{{ value }}</strong>
    <span v-if="hint" class="ui-stat-card__hint">{{ hint }}</span>
  </article>
</template>
