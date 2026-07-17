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
.surface-section{display:grid;gap:.75rem;padding:1rem;border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:rgba(255,255,255,.58)}.surface-section__header{display:flex;justify-content:space-between;align-items:start;gap:1rem}.surface-section__header h2,.surface-section__header p,.surface-section__empty{margin:0}.surface-section__header h2{font-size:1rem;letter-spacing:-.03em}.surface-section__header p{margin-top:.2rem;color:var(--text-muted);font-size:.82rem}.surface-section__header>span{color:var(--text-soft);font-size:.8rem}.surface-section__rows{display:grid;gap:.4rem}.surface-section__empty{padding:.5rem 0;color:var(--text-muted);font-size:.85rem}
</style>
