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
      'vision-panel__section--default': !tone || tone === 'default',
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

.vision-panel__section--default {
  padding: 0.9rem 1rem;
  border-radius: 1.35rem;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(24, 36, 47, 0.07);
  box-shadow: 0 18px 44px rgba(24, 36, 47, 0.05);
  backdrop-filter: blur(18px);
}

.vision-panel__section--warning {
  padding: 0.9rem 1rem;
  border-radius: 1.35rem;
  background: var(--vision-surface-section-warning-bg);
  border: 1px solid rgba(191, 92, 36, 0.12);
}

.vision-panel__section--field {
  padding: 0.9rem 1rem;
  border-radius: 1.35rem;
  background: var(--vision-surface-section-field-bg);
  border: 1px solid rgba(24, 36, 47, 0.07);
}

.vision-panel__section--info {
  padding: 0.9rem 1rem;
  border-radius: 1.35rem;
  background: var(--vision-surface-section-info-bg);
  border: 1px solid rgba(24, 36, 47, 0.07);
}

.vision-panel__section--success {
  padding: 0.9rem 1rem;
  border-radius: 1.35rem;
  background: var(--vision-surface-section-success-bg);
  border: 1px solid rgba(24, 36, 47, 0.07);
}

.vision-panel__label {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--vision-surface-ink-muted);
}

.vision-panel__body {
  margin: 0;
  color: var(--vision-surface-ink-soft);
  line-height: 1.55;
}
</style>
