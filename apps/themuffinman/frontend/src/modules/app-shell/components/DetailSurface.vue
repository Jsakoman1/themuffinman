<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  utilityLabel?: string
}>(), {
  utilityLabel: "Details and actions",
})
</script>

<template>
  <section class="detail-surface" :aria-label="title">
    <header v-if="$slots.header" class="detail-surface__header"><slot name="header" /></header>
    <div class="detail-surface__workspace">
      <main class="detail-surface__main"><slot /></main>
      <aside v-if="$slots.utility" class="detail-surface__utility" :aria-label="utilityLabel"><slot name="utility" /></aside>
    </div>
  </section>
</template>

<style scoped>
.detail-surface { min-width: 0; }
.detail-surface__header { min-width: 0; border-bottom: 1px solid var(--border-subtle); }
.detail-surface__workspace { display: grid; grid-template-columns: minmax(0, 1fr) var(--detail-rail-width); min-height: 0; }
.detail-surface__main { min-width: 0; padding: var(--space-5); }
.detail-surface__utility { min-width: 0; border-left: 1px solid var(--border-subtle); background: var(--rail-canvas); }
@media (max-width: 980px) {
  .detail-surface__workspace { grid-template-columns: minmax(0, 1fr); }
  .detail-surface__utility { border-top: 1px solid var(--border-subtle); border-left: 0; }
}
</style>
