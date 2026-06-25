<script setup lang="ts">
withDefaults(defineProps<{
  tag?: string
  variant?: "hero"
}>(), {
  tag: "div",
  variant: "hero",
})
</script>

<template>
  <component :is="tag" :class="['ui-split-layout', `ui-split-layout--${variant}`]">
    <slot />
  </component>
</template>
