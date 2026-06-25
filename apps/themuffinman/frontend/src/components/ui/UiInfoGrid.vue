<script setup lang="ts">
withDefaults(defineProps<{
  columns?: 1 | 2 | 3 | 4
}>(), {
  columns: 2,
})
</script>

<template>
  <div :class="['ui-info-grid', `ui-info-grid--${columns}`]">
    <slot />
  </div>
</template>
