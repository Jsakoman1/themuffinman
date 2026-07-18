<script setup lang="ts">
import AppFormField from "./AppFormField.vue"

const props = withDefaults(defineProps<{
  modelValue: string
  label: string
  placeholder?: string
  submitLabel?: string
  busy?: boolean
}>(), {placeholder: "Search", submitLabel: "Search", busy: false})

const emit = defineEmits<{
  "update:modelValue": [value: string]
  submit: []
}>()
</script>

<template>
  <form class="app-search-field" role="search" @submit.prevent="emit('submit')">
    <AppFormField :label="props.label" :hint="props.busy ? 'Searching…' : undefined">
      <input
        :value="props.modelValue"
        type="search"
        :placeholder="props.placeholder"
        :aria-label="props.label"
        :disabled="props.busy"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      >
    </AppFormField>
    <button type="submit" :disabled="props.busy">{{ props.busy ? "Searching" : props.submitLabel }}</button>
  </form>
</template>

<style scoped>
.app-search-field { display: flex; align-items: end; gap: var(--space-2); }
.app-search-field :deep(.app-form-field) { min-width: min(24rem, 58vw); }
.app-search-field :deep(input) { width: 100%; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); background: var(--control-bg); color: var(--control-ink); font: inherit; }
.app-search-field button { min-height: var(--control-height-default); border: 1px solid var(--accent); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: var(--accent); color: var(--canvas); font: inherit; font-weight: var(--text-weight-semibold); cursor: pointer; }
.app-search-field button:disabled { cursor: wait; opacity: .65; }
@media (max-width: 640px) { .app-search-field { width: 100%; } .app-search-field :deep(.app-form-field) { min-width: 0; flex: 1; } }
</style>
