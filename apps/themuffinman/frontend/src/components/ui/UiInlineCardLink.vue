<script setup lang="ts">
withDefaults(defineProps<{
  tag?: "button" | "article" | "div"
  interactive?: boolean
}>(), {
  tag: "button",
  interactive: true,
})
</script>

<template>
  <component :is="tag" :class="['ui-inline-card-link', { 'ui-inline-card-link--interactive': interactive }]">
    <slot />
  </component>
</template>
