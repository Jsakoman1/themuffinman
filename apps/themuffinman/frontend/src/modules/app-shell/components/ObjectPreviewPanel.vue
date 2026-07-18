<script setup lang="ts">
import {onBeforeUnmount, onMounted} from "vue"

const props = withDefaults(defineProps<{title: string; subtitle?: string; open?: boolean; detailLabel?: string; loading?: boolean}>(), {open: false, detailLabel: "Open full detail", loading: false})
const emit = defineEmits<{close: []; openDetail: []}>()
const closeOnEscape = (event: KeyboardEvent) => {
  if (props.open && event.key === "Escape") emit("close")
}

onMounted(() => window.addEventListener("keydown", closeOnEscape))
onBeforeUnmount(() => window.removeEventListener("keydown", closeOnEscape))
</script>

<template>
  <aside v-if="open" class="object-preview" :aria-label="`${title} preview`" role="complementary">
    <header>
      <div>
        <p v-if="subtitle" class="object-preview__subtitle">{{ subtitle }}</p>
        <h2>{{ title }}</h2>
      </div>
      <button type="button" aria-label="Close preview" title="Close preview (Esc)" @click="$emit('close')">×</button>
    </header>
    <div class="object-preview__body" aria-live="polite" :aria-busy="loading || undefined"><p v-if="loading" class="object-preview__loading">Loading preview…</p><slot v-else /></div>
    <footer>
      <div v-if="$slots.actions" class="object-preview__actions"><slot name="actions" /></div>
      <button type="button" class="object-preview__open-detail" @click="$emit('openDetail')">{{ detailLabel }}</button>
    </footer>
  </aside>
</template>

<style scoped>
.object-preview{display:grid;grid-template-rows:auto minmax(0,1fr) auto;min-width:min(100%,var(--detail-rail-width));border-left:1px solid var(--border-subtle);background:var(--surface-raised)}
header,footer{display:flex;justify-content:space-between;gap:var(--space-2);align-items:center;padding:var(--space-3);border-bottom:1px solid var(--border-subtle)}
h2{margin:0;font-size:var(--text-size-title)}.object-preview__subtitle{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);letter-spacing:var(--tracking-label);text-transform:uppercase}
button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);background:transparent;color:var(--text);padding:var(--space-1) var(--space-2);font:inherit;cursor:pointer}.object-preview__body{min-width:0;padding:var(--space-4);overflow:auto}.object-preview__loading{margin:0;color:var(--text-muted);font-size:var(--text-size-body)}footer{border-top:1px solid var(--border-subtle);border-bottom:0}.object-preview__actions{display:flex;gap:var(--space-1);min-width:0}.object-preview__open-detail{border-color:var(--accent);background:var(--accent);color:var(--canvas)}@media(max-width:980px){.object-preview{position:fixed;inset:auto 0 0 0;z-index:var(--z-drawer);min-width:0;max-height:75svh;border:1px solid var(--border-strong);border-radius:var(--radius-surface) var(--radius-surface) 0 0;box-shadow:var(--shadow-overlay)}}
</style>
