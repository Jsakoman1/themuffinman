<script setup lang="ts">
defineProps<{
  title?: string | null
  body?: string | null
  tone?: "default" | "warning" | "info" | "field" | "success"
}>()
</script>

<template>
  <div
    class="vision-panel__section"
    :class="{
      'vision-panel__section--warning': tone === 'warning',
      'vision-panel__section--info': tone === 'info',
      'vision-panel__section--field': tone === 'field',
      'vision-panel__section--success': tone === 'success'
    }"
  >
    <p v-if="title" class="vision-panel__label">{{ title }}</p>
    <p v-if="body" class="vision-panel__body">{{ body }}</p>
    <slot />
  </div>
</template>

<style scoped>
.vision-panel__section {
  display: grid;
  gap: 0.55rem;
}

.vision-panel__section--warning {
  padding: 0.9rem 1rem;
  border-radius: 1.2rem;
  background: rgba(255, 244, 229, 0.9);
}

.vision-panel__section--field {
  padding: 0.9rem 1rem;
  border-radius: 1.2rem;
  background: rgba(244, 249, 255, 0.88);
}

.vision-panel__section--info {
  padding: 0.9rem 1rem;
  border-radius: 1.2rem;
  background: rgba(240, 247, 255, 0.9);
}

.vision-panel__section--success {
  padding: 0.9rem 1rem;
  border-radius: 1.2rem;
  background: rgba(237, 250, 241, 0.95);
}

.vision-panel__label {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: rgba(24, 36, 47, 0.44);
}

.vision-panel__body {
  margin: 0;
  color: rgba(24, 36, 47, 0.74);
}
</style>
