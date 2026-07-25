<script setup lang="ts">
import {ref} from "vue"

const button = ref<HTMLButtonElement | null>(null)
defineExpose({focus: () => button.value?.focus()})

withDefaults(defineProps<{
  tone?: "primary" | "secondary" | "danger" | "quiet"
  loading?: boolean
  disabled?: boolean
  type?: "button" | "submit" | "reset"
}>(), {tone: "secondary", loading: false, disabled: false, type: "button"})
</script>

<template>
  <button ref="button" class="app-button ui-focusable" data-control-grammar="primary-secondary-quiet-danger" :class="`app-button--${tone}`" :type="type" :disabled="disabled || loading" :aria-busy="loading || undefined">
    <span v-if="loading" class="app-button__spinner" aria-hidden="true" />
    <slot />
  </button>
</template>

<style scoped>
.app-button { display: inline-flex; align-items: center; justify-content: center; gap: var(--space-2); min-height: var(--control-height-default); padding: 0 var(--control-padding-inline); border: 0; border-radius: var(--control-radius-interactive); background: var(--surface-selected); color: var(--control-ink); font-size: var(--text-size-body); font-weight: var(--text-weight-medium); cursor: pointer; transition: var(--control-focus-transition); }
.app-button:hover:not(:disabled) { background: var(--surface-hover); }.app-button:active:not(:disabled) { transform: scale(.98); background: var(--surface-selected); }.app-button--primary { background: var(--accent); color: #fff; }.app-button--primary:hover:not(:disabled) { background: color-mix(in srgb, var(--accent) 88%, #000); }.app-button--danger { color: var(--danger); background: transparent; }.app-button--quiet { background: transparent; color: var(--control-ink-muted); }.app-button--quiet:hover:not(:disabled) { background: var(--surface-hover); color: var(--control-ink); }.app-button:disabled { cursor: not-allowed; opacity: .45; }.app-button__spinner { width: .75rem; height: .75rem; border: 2px solid currentColor; border-right-color: transparent; border-radius: 50%; animation: app-button-spin 700ms linear infinite; }@keyframes app-button-spin { to { transform: rotate(1turn); } }@media (prefers-reduced-motion: reduce) { .app-button__spinner { animation: none; } }
</style>
