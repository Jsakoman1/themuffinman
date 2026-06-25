<script setup lang="ts">
defineProps<{
  modelValue: string
  options: Array<{ value: string; label: string }>
  stacked?: boolean
  wide?: boolean
  groupClass?: string
}>()

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void
}>()
</script>

<template>
  <div :class="['ui-chip-group', groupClass, { 'ui-chip-group--stack': stacked }]">
    <button
      v-for="option in options"
      :key="option.value"
      :class="[
        'ui-chip',
        {
          'ui-chip--active': modelValue === option.value,
          'ui-chip--wide': wide,
        },
      ]"
      type="button"
      @click="emit('update:modelValue', option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>
