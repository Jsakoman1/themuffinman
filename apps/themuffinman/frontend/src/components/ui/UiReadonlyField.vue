<script setup lang="ts">
defineProps<{
  label: string
  value?: string | null
}>()
</script>

<template>
  <div class="field field--readonly">
    <span class="label">{{ label }}</span>
    <strong>{{ value || "—" }}</strong>
  </div>
</template>
