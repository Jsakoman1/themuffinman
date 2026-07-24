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
  <button ref="button" class="app-button ui-focusable" :class="`app-button--${tone}`" :type="type" :disabled="disabled || loading" :aria-busy="loading || undefined">
    <span v-if="loading" class="app-button__spinner" aria-hidden="true" />
    <slot />
  </button>
</template>

<style scoped>
.app-button { display: inline-flex; align-items: center; justify-content: center; gap: var(--space-2); min-height: var(--control-height-default); padding: var(--space-1) var(--space-3); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink); font-size: var(--text-size-body); font-weight: var(--text-weight-semibold); cursor: pointer; transition: background-color var(--motion-fast) ease, border-color var(--motion-fast) ease, color var(--motion-fast) ease; }
.app-button:hover:not(:disabled) { border-color: var(--control-border-active); background: var(--control-bg-hover); }.app-button:active:not(:disabled) { background: var(--control-bg-pressed); }.app-button--primary { border-color: var(--accent); background: var(--accent); color: var(--canvas); }.app-button--danger { color: var(--danger); }.app-button--quiet { border-color: transparent; background: transparent; color: var(--control-ink-muted); }.app-button:disabled { cursor: not-allowed; opacity: .55; }.app-button__spinner { width: .75rem; height: .75rem; border: 2px solid currentColor; border-right-color: transparent; border-radius: 50%; animation: app-button-spin 700ms linear infinite; }@keyframes app-button-spin { to { transform: rotate(1turn); } }@media (prefers-reduced-motion: reduce) { .app-button__spinner { animation: none; } }
</style>
