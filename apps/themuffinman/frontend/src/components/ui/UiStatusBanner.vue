<script setup lang="ts">
withDefaults(
  defineProps<{
    message: string
    tone?: "success" | "warning" | "error"
  }>(),
  {
    tone: "success",
  },
)
</script>

<template>
  <div v-if="message" :class="['ui-status-banner', `ui-status-banner--${tone}`]" role="status" aria-live="polite">
    <strong>{{ message }}</strong>
  </div>
</template>
