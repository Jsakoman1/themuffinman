<script setup lang="ts">
import {onBeforeUnmount, onMounted} from "vue"

const props = withDefaults(defineProps<{title: string; subtitle?: string; open?: boolean}>(), {open: false})
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
    <div class="object-preview__body" aria-live="polite"><slot /></div>
    <footer v-if="$slots.actions">
      <slot name="actions" />
      <button type="button" @click="$emit('openDetail')">Open detail</button>
    </footer>
  </aside>
</template>

<style scoped>
.object-preview{display:grid;grid-template-rows:auto minmax(0,1fr) auto;min-width:min(100%,20rem);border-left:1px solid var(--border-subtle);background:var(--surface)}
header,footer{display:flex;justify-content:space-between;gap:var(--space-2);align-items:center;padding:var(--space-3);border-bottom:1px solid var(--border-subtle)}
h2{margin:0;font-size:.95rem}.object-preview__subtitle{margin:0 0 .2rem;color:var(--text-muted);font-size:.75rem}
button{border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-strong);color:var(--text);padding:.4rem .6rem}.object-preview__body{min-width:0;padding:var(--space-4);overflow:auto}footer{border-top:1px solid var(--border-subtle);border-bottom:0}
</style>
