<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  clickable?: boolean
  hideTitle?: boolean
}>()

defineEmits<{
  click: []
}>()
</script>

<template>
  <div class="dashboard-section-header dashboard-section-header--centered">
    <button
      v-if="clickable"
      class="dashboard-section-header__button"
      type="button"
      @click="$emit('click')"
    >
      <h2 :class="['dashboard-section-header__title', { 'visually-hidden': hideTitle }]">{{ title }}</h2>
    </button>
    <h2 v-else :class="['dashboard-section-header__title', { 'visually-hidden': hideTitle }]">{{ title }}</h2>

    <p v-if="subtitle" class="dashboard-section-header__subtitle">{{ subtitle }}</p>

    <div v-if="$slots.stats" class="dashboard-section-header__stats">
      <slot name="stats" />
    </div>
  </div>
</template>
