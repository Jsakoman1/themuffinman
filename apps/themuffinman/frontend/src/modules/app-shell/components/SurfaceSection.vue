<script setup lang="ts">
import type {ShellSurfaceSection} from "../shellSurfaceData.ts"
import SurfaceRow from "./SurfaceRow.vue"

defineProps<{section: ShellSurfaceSection}>()
</script>

<template>
  <article class="surface-section">
    <header class="surface-section__header"><div><h2>{{ section.title }}</h2><p>{{ section.description }}</p></div><span>{{ section.rows.length }}</span></header>
    <div v-if="section.rows.length > 0" class="surface-section__rows"><SurfaceRow v-for="row in section.rows" :key="row.id" :row="row" /></div>
    <p v-else class="surface-section__empty">{{ section.emptyState }}</p>
  </article>
</template>

<style scoped>
.surface-section { display: grid; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.surface-section__header { display: flex; justify-content: space-between; align-items: start; gap: var(--space-4); }
.surface-section__header h2, .surface-section__header p, .surface-section__empty { margin: 0; }
.surface-section__header h2 { font-size: var(--text-size-body); letter-spacing: var(--tracking-tight); }
.surface-section__header p { margin-top: var(--space-1); color: var(--text-muted); font-size: var(--text-size-meta); }
.surface-section__header>span { color: var(--text-soft); font-size: var(--text-size-meta); }
.surface-section__rows { display: grid; gap: var(--space-1); }
.surface-section__empty { padding: var(--space-1) 0; color: var(--text-muted); font-size: var(--text-size-body); }
</style>
