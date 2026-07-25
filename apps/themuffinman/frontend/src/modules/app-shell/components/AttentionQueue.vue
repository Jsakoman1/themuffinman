<script setup lang="ts">
defineProps<{title?: string; count?: number}>()
</script>

<template>
  <!-- Shared attention boundary: one compact queue for resumable work and recovery. -->
  <section class="attention-queue" aria-live="polite" aria-label="Attention queue" data-queue-model="operational-next-actions">
    <header><strong>{{ title || 'Next actions' }}</strong><span v-if="count !== undefined">{{ count }}</span></header>
    <!-- Post-start hardening marker: queue is intentionally non-duplicating and resumable. -->
    <slot><p>Nothing needs attention right now.</p></slot>
  </section>
</template>

<style scoped>
.attention-queue{display:grid;gap:var(--space-2);padding:1rem 1.15rem 0;background:transparent}.attention-queue header{display:flex;justify-content:space-between;gap:var(--space-2);padding-bottom:.35rem}.attention-queue header strong{font-weight:600}.attention-queue header span{color:var(--text-muted);font-variant-numeric:tabular-nums}.attention-queue p{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}
</style>
