<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  subtitle?: string
}>(), {
  subtitle: "",
})
</script>

<template>
  <div class="card auth-card">
    <div class="auth-brand">
      <slot name="brand" />
    </div>

    <div class="surface-stack">
      <div class="surface-stack">
        <h1 class="auth-card__title">{{ title }}</h1>
        <p v-if="subtitle" class="auth-card__subtitle">{{ subtitle }}</p>
      </div>

      <slot />
    </div>

    <div v-if="$slots.footer" class="auth-footer">
      <slot name="footer" />
    </div>
  </div>
</template>
