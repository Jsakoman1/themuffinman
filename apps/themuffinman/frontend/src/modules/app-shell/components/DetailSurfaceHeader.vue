<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"

withDefaults(defineProps<{
  eyebrow: string
  title: string
  backTo?: RouteLocationRaw
  backLabel?: string
}>(), {backLabel: "Back"})
</script>

<template>
  <header class="detail-surface-header" aria-label="Detail header">
    <div class="detail-surface-header__identity"><p>{{ eyebrow }}</p><h1>{{ title }}</h1></div>
    <div v-if="backTo || $slots.actions" class="detail-surface-header__actions"><RouterLink v-if="backTo" :to="backTo" class="detail-surface-header__back">{{ backLabel }}</RouterLink><slot name="actions" /></div>
  </header>
</template>

<style scoped>
.detail-surface-header{display:flex;align-items:end;justify-content:space-between;gap:var(--space-3);min-width:0;padding:var(--space-4) var(--space-5)}.detail-surface-header__identity{min-width:0}.detail-surface-header p{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.detail-surface-header h1{margin:0;overflow-wrap:anywhere;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.detail-surface-header__actions{display:flex;align-items:center;justify-content:end;gap:var(--space-1);flex-wrap:wrap}.detail-surface-header__back{display:inline-flex;align-items:center;min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-decoration:none}.detail-surface-header__back:hover{border-color:var(--border-strong);background:var(--surface-hover);color:var(--text)}@media(max-width:700px){.detail-surface-header{align-items:start;flex-direction:column;padding:var(--space-3)}.detail-surface-header__actions{justify-content:start}}
</style>
