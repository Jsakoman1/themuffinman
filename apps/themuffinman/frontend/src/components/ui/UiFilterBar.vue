<script setup lang="ts">
withDefaults(defineProps<{
  columns?: 1 | 2 | 3 | 5
}>(), {
  columns: 2
})
</script>

<template>
  <div :class="['ui-filter-bar', `ui-filter-bar--${columns}`]">
    <slot />
  </div>
</template>
