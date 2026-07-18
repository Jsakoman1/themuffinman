<script setup lang="ts">
defineProps<{title?: string; count?: number | null; busy?: boolean}>()
</script>

<template>
  <header class="collection-toolbar" :aria-busy="busy || undefined">
    <div class="collection-toolbar__identity"><h2 v-if="title">{{ title }}</h2><span v-if="count !== null && count !== undefined" aria-label="Collection item count">{{ count }}</span></div>
    <div v-if="$slots.filters" class="collection-toolbar__filters"><slot name="filters" /></div>
    <div v-if="$slots.actions" class="collection-toolbar__actions"><slot name="actions" /></div>
  </header>
</template>

<style scoped>
.collection-toolbar { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; align-items: center; gap: var(--space-2); min-height: calc(var(--control-height-default) + var(--space-3)); padding: var(--space-2) var(--space-3); border-bottom: 1px solid var(--border-subtle); }.collection-toolbar__identity { display: flex; align-items: baseline; gap: var(--space-2); min-width: 0; }.collection-toolbar__identity h2 { margin: 0; overflow: hidden; color: var(--text); font-size: var(--text-size-title); text-overflow: ellipsis; white-space: nowrap; }.collection-toolbar__identity span { color: var(--text-soft); font-size: var(--text-size-meta); font-variant-numeric: tabular-nums; }.collection-toolbar__filters, .collection-toolbar__actions { display: flex; align-items: center; gap: var(--space-1); min-width: 0; }.collection-toolbar__actions { justify-content: flex-end; }@media (max-width: 640px) { .collection-toolbar { grid-template-columns: minmax(0, 1fr) auto; }.collection-toolbar__filters { grid-column: 1 / -1; overflow-x: auto; padding-bottom: var(--space-1); }.collection-toolbar__actions { grid-column: 2; grid-row: 1; } }
</style>
