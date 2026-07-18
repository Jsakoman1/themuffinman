<script setup lang="ts">
withDefaults(defineProps<{sticky?: boolean}>(), {sticky: false})
</script>

<template>
  <footer class="app-form-footer" :class="{'app-form-footer--sticky': sticky}"><div class="app-form-footer__secondary"><slot name="secondary" /></div><div class="app-form-footer__primary"><slot name="primary" /></div></footer>
</template>

<style scoped>
.app-form-footer { display: flex; justify-content: space-between; align-items: center; gap: var(--space-2); flex-wrap: wrap; margin-top: var(--space-3); padding-top: var(--space-3); border-top: 1px solid var(--border-subtle); }.app-form-footer--sticky { position:sticky;bottom:0;margin-right:calc(-1 * var(--space-4));margin-bottom:calc(-1 * var(--space-4));margin-left:calc(-1 * var(--space-4));padding:var(--space-3) var(--space-4);background:var(--surface-raised); }.app-form-footer__secondary,.app-form-footer__primary{display:flex;align-items:center;gap:var(--space-2);flex-wrap:wrap}.app-form-footer :deep(a), .app-form-footer :deep(button) { display: inline-flex; align-items: center; justify-content: center; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: transparent; color: var(--text-muted); font: inherit; font-size: var(--text-size-body); font-weight: var(--text-weight-semibold); cursor: pointer; }.app-form-footer :deep(button[type="submit"]) { border-color: var(--accent); background: var(--accent); color: var(--canvas); }.app-form-footer :deep(button:disabled) { cursor: not-allowed; opacity: .55; }
</style>
