<script setup lang="ts">
defineProps<{title: string; eyebrow: string; open: boolean}>()
const emit = defineEmits<{close: []}>()
</script>

<template>
  <aside v-if="open" class="review-pane" aria-label="Selected item review">
    <header class="review-pane__header"><div><p>{{ eyebrow }}</p><h2>{{ title }}</h2></div><button type="button" aria-label="Close review" @click="emit('close')">×</button></header>
    <div class="review-pane__body"><slot /></div>
    <footer v-if="$slots.actions" class="review-pane__actions"><slot name="actions" /></footer>
  </aside>
</template>

<style scoped>
.review-pane{display:grid;grid-template-rows:auto minmax(0,1fr) auto;min-width:0;border-left:1px solid var(--border-subtle);background:var(--surface-raised)}.review-pane__header,.review-pane__actions{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2);padding:var(--space-3);border-bottom:1px solid var(--border-subtle)}.review-pane__header p{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.review-pane__header h2{margin:0;font-size:var(--text-size-title)}.review-pane__header button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);background:transparent;color:var(--text);font:inherit;cursor:pointer}.review-pane__body{display:grid;align-content:start;gap:var(--space-2);padding:var(--space-3);overflow:auto;color:var(--text-muted)}.review-pane__body :deep(p){margin:0}.review-pane__actions{border-top:1px solid var(--border-subtle);border-bottom:0;flex-wrap:wrap}@media(max-width:980px){.review-pane{position:fixed;inset:0;z-index:var(--z-drawer);border:0}.review-pane__header{padding-top:max(var(--space-3),env(safe-area-inset-top))}.review-pane__actions{padding-bottom:max(var(--space-3),env(safe-area-inset-bottom))}}
</style>
