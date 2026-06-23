<script setup lang="ts">
defineProps<{
  label: string
  hasPrevious: boolean
  hasNext: boolean
}>()

defineEmits<{
  previous: []
  next: []
}>()
</script>

<template>
  <div class="dashboard-find-work__pagination">
    <div class="muted">{{ label }}</div>
    <div class="button-row">
      <button class="button button--secondary" type="button" :disabled="!hasPrevious" @click="$emit('previous')">Previous</button>
      <button class="button button--secondary" type="button" :disabled="!hasNext" @click="$emit('next')">Next</button>
    </div>
  </div>
</template>
