<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
  actionLabel: string
  icon?: string
}>()
</script>

<template>
  <button class="ui-launch-card" type="button">
    <div class="ui-launch-card__copy">
      <div class="dashboard-kicker">{{ eyebrow }}</div>
      <h2 class="card__title">{{ title }}</h2>
      <p class="muted mt-2 mb-0">{{ description }}</p>
    </div>
    <div class="ui-launch-card__action">
      <span class="ui-launch-card__icon" aria-hidden="true">{{ icon ?? "+" }}</span>
      <span>{{ actionLabel }}</span>
    </div>
  </button>
</template>
