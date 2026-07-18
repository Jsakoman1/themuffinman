<script setup lang="ts">
defineProps<{
  label: string
  tone?: "neutral" | "danger" | "primary"
  disabled?: boolean
  pressed?: boolean
}>()
</script>

<template>
  <button
    type="button"
    class="app-icon-button"
    :class="`app-icon-button--${tone ?? 'neutral'}`"
    :aria-label="label"
    :title="label"
    :disabled="disabled"
    :aria-pressed="pressed"
  >
    <slot />
  </button>
</template>

<style scoped>
.app-icon-button{display:inline-grid;place-items:center;width:2rem;height:2rem;padding:0;border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface);color:var(--text);font:inherit;cursor:pointer;transition:background-color 140ms ease,border-color 140ms ease,transform 140ms ease}.app-icon-button:hover,.app-icon-button[aria-pressed="true"]{transform:translateY(-1px);background:var(--surface-hover);border-color:var(--border-strong)}.app-icon-button:focus-visible{outline:var(--focus-ring);outline-offset:2px}.app-icon-button--primary{background:var(--text);color:var(--surface)}.app-icon-button--danger{color:var(--danger)}.app-icon-button:disabled{opacity:.5;cursor:not-allowed;transform:none}
</style>
