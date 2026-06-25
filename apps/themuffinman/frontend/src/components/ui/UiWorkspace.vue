<script setup lang="ts">
withDefaults(defineProps<{
  tag?: string
  variant?: "detail" | "triad"
}>(), {
  tag: "div",
  variant: "detail",
})
</script>

<template>
  <component :is="tag" :class="['ui-workspace', `ui-workspace--${variant}`]">
    <slot />
  </component>
</template>
