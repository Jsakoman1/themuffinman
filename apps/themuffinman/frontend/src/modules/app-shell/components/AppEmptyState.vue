<script setup lang="ts">
defineProps<{title: string; message?: string}>()
</script>

<template><section class="app-empty-state"><h2>{{ title }}</h2><p v-if="message">{{ message }}</p><div v-if="$slots.actions" class="app-empty-state__actions"><slot name="actions" /></div></section></template>

<style scoped>
.app-empty-state { display: grid; justify-items: start; gap: var(--space-2); padding: var(--space-5) var(--space-3); border: 1px dashed var(--border-subtle); border-radius: var(--radius-surface); color: var(--text-muted); }.app-empty-state h2, .app-empty-state p { margin: 0; }.app-empty-state h2 { color: var(--text); font-size: var(--text-size-title); }.app-empty-state p { max-width: 36rem; font-size: var(--text-size-body); line-height: 1.5; }.app-empty-state__actions { display: flex; gap: var(--space-2); margin-top: var(--space-1); }
</style>
