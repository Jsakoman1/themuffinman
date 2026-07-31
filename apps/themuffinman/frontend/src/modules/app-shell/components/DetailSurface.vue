<script setup lang="ts">
defineProps<{title?: string}>()
</script>

<template>
  <section class="detail-surface" data-archetype="detail-single-flow" :aria-label="title">
    <header v-if="$slots.header" class="detail-surface__header"><slot name="header" /></header>
    <main class="detail-surface__main"><slot /></main>
    <section v-if="$slots.utility" class="detail-surface__actions" aria-label="Available actions"><slot name="utility" /></section>
  </section>
</template>

<style scoped>
.detail-surface { width: 100%; min-width: 0; overflow:hidden; border:1px solid var(--orientation-line); border-radius:var(--radius-card); background:var(--surface-base); box-shadow:var(--shadow-card); }
.detail-surface__header { min-width: 0; border-bottom: 1px solid var(--orientation-line); background:var(--orientation-wash); }
.detail-surface__main { min-width: 0; padding: var(--space-5); }
.detail-surface__actions { min-width: 0; padding: 0 var(--space-5) var(--space-5); }
@media (max-width: 980px) {
  .detail-surface__main { padding:var(--space-4); }
  .detail-surface__actions { padding:0 var(--space-4) var(--space-4); }
}
.detail-surface{border-color:var(--border-subtle);border-radius:calc(var(--radius-card) + .2rem);box-shadow:none}.detail-surface__header{border-bottom-color:var(--border-subtle)}
</style>
