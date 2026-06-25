<script setup lang="ts">
withDefaults(defineProps<{
  label: string
  tag?: "label" | "div"
  fieldClass?: string
}>(), {
  tag: "label",
  fieldClass: "",
})
</script>

<template>
  <component :is="tag" :class="['field', fieldClass]">
    <div v-if="$slots.headerAction" class="field__header">
      <span class="label">{{ label }}</span>
      <slot name="headerAction" />
    </div>
    <span v-else class="label">{{ label }}</span>
    <slot />
  </component>
</template>
