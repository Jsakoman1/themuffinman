<script setup lang="ts">
withDefaults(defineProps<{open: boolean; title: string; layout?: "standard" | "workspace"}>(), {layout: "standard"})
const emit = defineEmits<{close: []}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="app-dialog__backdrop" role="presentation" @click.self="emit('close')" @keydown.escape="emit('close')">
      <section class="app-dialog" :class="`app-dialog--${layout}`" role="dialog" aria-modal="true" aria-labelledby="app-dialog-title" aria-describedby="app-dialog-body">
        <header class="app-dialog__header"><h2 id="app-dialog-title">{{ title }}</h2><button type="button" class="app-dialog__close" :aria-label="`Close ${title}`" :title="`Close ${title}`" @click="emit('close')">×</button></header>
        <div class="app-dialog__workspace"><div id="app-dialog-body" class="app-dialog__body"><slot /></div><aside v-if="$slots.utility" class="app-dialog__utility" aria-label="Form details and actions"><slot name="utility" /></aside></div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.app-dialog__backdrop{position:fixed;inset:0;z-index:var(--z-dialog);display:grid;place-items:center;padding:var(--space-4);background:rgba(0,0,0,.52);backdrop-filter:blur(5px)}.app-dialog{width:min(100%,38rem);max-height:min(90vh,52rem);overflow:auto;border:1px solid var(--border-strong);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-overlay)}.app-dialog--workspace{width:min(100%,62rem)}.app-dialog__header{display:flex;align-items:center;justify-content:space-between;gap:var(--space-3);padding:var(--space-3) var(--space-4);border-bottom:1px solid var(--border-subtle)}.app-dialog__header h2{margin:0;font-size:var(--text-size-title);letter-spacing:var(--tracking-tight)}.app-dialog__close{width:var(--control-height-default);height:var(--control-height-default);border:1px solid transparent;border-radius:var(--radius-control);background:transparent;color:var(--text-muted);font-size:1.25rem;line-height:1;cursor:pointer}.app-dialog__close:hover,.app-dialog__close:focus-visible{border-color:var(--control-border);background:var(--surface-hover);color:var(--text)}.app-dialog__close:focus-visible{outline:var(--focus-ring);outline-offset:2px}.app-dialog__workspace{display:grid;grid-template-columns:minmax(0,1fr)}.app-dialog:has(.app-dialog__utility) .app-dialog__workspace{grid-template-columns:minmax(0,1fr) var(--detail-rail-width)}.app-dialog__body{min-width:0;padding:var(--space-4)}.app-dialog__utility{min-width:0;border-left:1px solid var(--border-subtle);background:var(--rail-canvas)}@media(max-width:700px){.app-dialog__backdrop{place-items:end stretch;padding:0}.app-dialog{width:100%;max-height:90svh;border-radius:var(--radius-surface) var(--radius-surface) 0 0}.app-dialog:has(.app-dialog__utility) .app-dialog__workspace{grid-template-columns:minmax(0,1fr)}.app-dialog__utility{border-top:1px solid var(--border-subtle);border-left:0}}
</style>
