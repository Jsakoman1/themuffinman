<script setup lang="ts">
defineProps<{
  label: string
  tone?: "neutral" | "danger" | "primary"
  disabled?: boolean
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
  >
    <slot />
  </button>
</template>

<style scoped>
.app-icon-button{display:inline-grid;place-items:center;width:2.25rem;height:2.25rem;padding:0;border:1px solid var(--border-subtle);border-radius:50%;background:var(--surface);color:var(--text);font:inherit;cursor:pointer;transition:background-color 140ms ease,border-color 140ms ease,transform 140ms ease}.app-icon-button:hover{transform:translateY(-1px);background:var(--surface-muted);border-color:var(--border-strong)}.app-icon-button--primary{background:var(--text);color:var(--surface)}.app-icon-button--danger{color:var(--danger)}.app-icon-button:disabled{opacity:.5;cursor:not-allowed;transform:none}
</style>
