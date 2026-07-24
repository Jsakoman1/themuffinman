<script setup lang="ts">
withDefaults(defineProps<{label?: string; rows?: number}>(), {label: "Loading", rows: 3})
</script>

<template><section class="app-loading-state" role="status" aria-busy="true" :aria-label="label"><span class="app-loading-state__label">{{ label }}</span><span v-for="index in rows" :key="index" class="app-loading-state__row" aria-hidden="true" /></section></template>

<style scoped>
.app-loading-state { display: grid; gap: var(--space-2); padding: var(--space-3) 0; }.app-loading-state__label { color: var(--text-soft); font-size: var(--text-size-meta); }.app-loading-state__row { display: block; height: var(--dense-row-height-compact); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: linear-gradient(90deg, var(--surface-muted), var(--surface-hover), var(--surface-muted)); background-size: 200% 100%; animation: app-loading-shimmer 1.2s ease-in-out infinite; }@keyframes app-loading-shimmer { to { background-position: -200% 0; } }@media (prefers-reduced-motion: reduce) { .app-loading-state__row { animation: none; background: var(--surface-muted); } }
</style>
