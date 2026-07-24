<script setup lang="ts">
import AppTooltip from "./AppTooltip.vue"

defineProps<{
  label: string
  tone?: "neutral" | "danger" | "primary"
  disabled?: boolean
  pressed?: boolean
  loading?: boolean
}>()
</script>

<template>
  <AppTooltip :label="label">
    <button
      type="button"
      class="app-icon-button ui-focusable"
      :class="`app-icon-button--${tone ?? 'neutral'}`"
      :aria-label="label"
      :disabled="disabled || loading"
      :aria-pressed="pressed"
      :aria-busy="loading || undefined"
    >
      <span v-if="loading" class="app-icon-button__spinner" aria-hidden="true" />
      <slot v-else />
    </button>
  </AppTooltip>
</template>

<style scoped>
.app-icon-button { display: inline-grid; place-items: center; width: var(--control-height-default); height: var(--control-height-default); padding: 0; border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: transparent; color: var(--text-muted); font: inherit; cursor: pointer; transition: background-color 140ms ease, border-color 140ms ease, color 140ms ease; }
.app-icon-button:hover, .app-icon-button[aria-pressed="true"] { background: var(--surface-hover); border-color: var(--border-strong); color: var(--text); }
.app-icon-button:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }
.app-icon-button--primary { border-color: var(--accent); background: var(--accent); color: var(--canvas); }.app-icon-button--danger { color: var(--danger); }.app-icon-button--danger:hover { border-color: var(--danger); }
.app-icon-button:disabled { opacity: .5; cursor: not-allowed; }
.app-icon-button__spinner { width: .75rem; height: .75rem; border: 2px solid currentColor; border-right-color: transparent; border-radius: 50%; animation: app-icon-button-spin 700ms linear infinite; }
@keyframes app-icon-button-spin { to { transform: rotate(1turn); } }
@media (prefers-reduced-motion: reduce) { .app-icon-button__spinner { animation: none; } }
</style>
