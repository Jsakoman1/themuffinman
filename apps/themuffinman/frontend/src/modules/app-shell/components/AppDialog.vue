<script setup lang="ts">
defineProps<{open: boolean; title: string}>()
const emit = defineEmits<{close: []}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="app-dialog__backdrop" role="presentation" @click.self="emit('close')">
      <section class="app-dialog" role="dialog" aria-modal="true" :aria-label="title">
        <header class="app-dialog__header"><h2>{{ title }}</h2><button type="button" class="app-dialog__close" :aria-label="`Close ${title}`" :title="`Close ${title}`" @click="emit('close')">×</button></header>
        <div class="app-dialog__body"><slot /></div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.app-dialog__backdrop{position:fixed;inset:0;z-index:20;display:grid;place-items:center;padding:1rem;background:rgba(23,34,26,.3);backdrop-filter:blur(5px)}.app-dialog{width:min(100%,38rem);max-height:min(90vh,52rem);overflow:auto;border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface);box-shadow:0 24px 80px rgba(23,34,26,.2)}.app-dialog__header{display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:1rem 1.2rem;border-bottom:1px solid var(--border-subtle)}.app-dialog__header h2{margin:0;font-size:1.15rem;letter-spacing:-.04em}.app-dialog__close{width:2rem;height:2rem;border:0;border-radius:50%;background:transparent;font-size:1.5rem;line-height:1;cursor:pointer}.app-dialog__close:hover{background:var(--surface-muted)}.app-dialog__body{padding:1.2rem}
</style>
