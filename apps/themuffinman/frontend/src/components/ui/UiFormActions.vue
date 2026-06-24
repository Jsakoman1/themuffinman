<script setup lang="ts">
withDefaults(defineProps<{
  align?: "start" | "end"
}>(), {
  align: "start"
})
</script>

<template>
  <div :class="['button-row', { 'button-row--end': align === 'end' }]">
    <slot />
  </div>
</template>
