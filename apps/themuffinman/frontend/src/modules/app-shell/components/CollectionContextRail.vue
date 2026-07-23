<script setup lang="ts">
defineProps<{title: string; subtitle?: string; openLabel?: string}>()
</script>

<template>
  <aside class="collection-context-rail" :aria-label="title">
    <header><p v-if="subtitle">{{ subtitle }}</p><h2>{{ title }}</h2></header>
    <details class="collection-context-rail__content" open>
      <summary>{{ openLabel ?? `Show ${title}` }}</summary>
      <div><slot /></div>
    </details>
  </aside>
</template>

<style scoped>
.collection-context-rail { min-width: 0; width: var(--detail-rail-width); border-right: 1px solid var(--border-subtle); background: var(--rail-canvas); }.collection-context-rail header { display: grid; gap: var(--space-1); padding: var(--space-3); border-bottom: 1px solid var(--border-subtle); }.collection-context-rail header p, .collection-context-rail h2 { margin: 0; }.collection-context-rail header p { color: var(--text-soft); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }.collection-context-rail h2 { color: var(--text); font-size: var(--text-size-title); }.collection-context-rail__content { display: block; }.collection-context-rail__content summary { display: none; }.collection-context-rail__content > div { display: grid; gap: var(--space-2); padding: var(--space-3); }@media (max-width: 980px) { .collection-context-rail { width: auto; border-right: 0; border-bottom: 1px solid var(--border-subtle); }.collection-context-rail header { display: none; }.collection-context-rail__content { display: block; }.collection-context-rail__content summary { display: block; min-height: var(--control-height-default); padding: var(--space-2) var(--space-3); color: var(--text-muted); font-size: var(--text-size-body); cursor: pointer; }.collection-context-rail__content > div { display: grid; gap: var(--space-2); padding: var(--space-3); border-top: 1px solid var(--border-subtle); } }
</style>
