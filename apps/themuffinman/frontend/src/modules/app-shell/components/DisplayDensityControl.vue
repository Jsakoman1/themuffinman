<script setup lang="ts">
import type {DisplayDensity} from "../api/userShellApi.ts"

const props = withDefaults(defineProps<{modelValue: DisplayDensity; disabled?: boolean}>(), {disabled: false})
const emit = defineEmits<{"update:modelValue": [value: DisplayDensity]}>()
const options: Array<{value: DisplayDensity; label: string}> = [{value: "compact", label: "Compact"}, {value: "default", label: "Default"}, {value: "comfortable", label: "Comfortable"}]
</script>

<template>
  <div class="display-density-control" role="group" aria-label="Display density">
    <button v-for="option in options" :key="option.value" type="button" :disabled="disabled" :aria-pressed="props.modelValue === option.value" @click="emit('update:modelValue', option.value)">{{ option.label }}</button>
  </div>
</template>

<style scoped>
.display-density-control { display: inline-flex; align-items: center; border: 1px solid var(--control-border); border-radius: var(--radius-control); overflow: hidden; }.display-density-control button { min-height: var(--control-height-compact); border: 0; border-right: 1px solid var(--control-border); background: transparent; color: var(--text-soft); padding: 0 var(--space-2); font: inherit; font-size: var(--text-size-meta); cursor: pointer; }.display-density-control button:last-child { border-right: 0; }.display-density-control button[aria-pressed="true"] { background: var(--surface-selected); color: var(--text); }.display-density-control button:disabled { cursor: not-allowed; opacity: .55; }
</style>
