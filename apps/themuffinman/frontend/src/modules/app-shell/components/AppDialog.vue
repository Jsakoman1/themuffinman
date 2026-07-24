<script setup lang="ts">
import {nextTick, onBeforeUnmount, ref, watch} from "vue"

const props = withDefaults(defineProps<{open: boolean; title: string; layout?: "standard" | "workspace"}>(), {layout: "standard"})
const emit = defineEmits<{close: []}>()
const dialog = ref<HTMLElement | null>(null)
let previouslyFocused: HTMLElement | null = null
const titleId = `app-dialog-title-${Math.random().toString(36).slice(2)}`
const bodyId = `app-dialog-body-${Math.random().toString(36).slice(2)}`
const focusableSelector = "button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex='-1'])"

const focusInitial = async () => {
  await nextTick()
  const first = dialog.value?.querySelector<HTMLElement>(focusableSelector)
  first?.focus()
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape") { event.preventDefault(); emit("close"); return }
  if (event.key !== "Tab" || !dialog.value) return
  const focusable = [...dialog.value.querySelectorAll<HTMLElement>(focusableSelector)]
  if (focusable.length === 0) { event.preventDefault(); return }
  const first = focusable[0]
  const last = focusable[focusable.length - 1]
  if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus() }
  else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus() }
}

watch(() => props.open, async open => {
  if (open) { previouslyFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null; await focusInitial() }
  else { await nextTick(); previouslyFocused?.focus(); previouslyFocused = null }
})
onBeforeUnmount(() => { previouslyFocused?.focus() })
</script>

<template>
  <Teleport to="body">
    <div v-if="props.open" class="app-dialog__backdrop" role="presentation" @click.self="emit('close')">
      <section ref="dialog" class="app-dialog" :class="`app-dialog--${props.layout}`" role="dialog" aria-modal="true" :aria-labelledby="titleId" :aria-describedby="bodyId" @keydown="handleKeydown">
        <header class="app-dialog__header"><h2 :id="titleId">{{ props.title }}</h2><button type="button" class="app-dialog__close" :aria-label="`Close ${props.title}`" :title="`Close ${props.title}`" @click="emit('close')">×</button></header>
        <div class="app-dialog__workspace"><div :id="bodyId" class="app-dialog__body"><slot /></div><aside v-if="$slots.utility" class="app-dialog__utility" aria-label="Form details and actions"><slot name="utility" /></aside></div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.app-dialog__backdrop{position:fixed;inset:0;z-index:var(--z-dialog);display:grid;place-items:center;padding:var(--space-4);background:rgba(0,0,0,.52);backdrop-filter:blur(5px)}.app-dialog{width:min(100%,38rem);max-height:min(90vh,52rem);overflow:auto;border:1px solid var(--border-strong);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-overlay)}.app-dialog--workspace{width:min(100%,62rem)}.app-dialog__header{display:flex;align-items:center;justify-content:space-between;gap:var(--space-3);padding:var(--space-3) var(--space-4);border-bottom:1px solid var(--border-subtle)}.app-dialog__header h2{margin:0;font-size:var(--text-size-title);letter-spacing:var(--tracking-tight)}.app-dialog__close{width:var(--control-height-default);height:var(--control-height-default);border:1px solid transparent;border-radius:var(--radius-control);background:transparent;color:var(--text-muted);font-size:1.25rem;line-height:1;cursor:pointer}.app-dialog__close:hover,.app-dialog__close:focus-visible{border-color:var(--control-border);background:var(--surface-hover);color:var(--text)}.app-dialog__close:focus-visible{outline:var(--focus-ring);outline-offset:2px}.app-dialog__workspace{display:grid;grid-template-columns:minmax(0,1fr)}.app-dialog:has(.app-dialog__utility) .app-dialog__workspace{grid-template-columns:minmax(0,1fr) var(--detail-rail-width)}.app-dialog__body{min-width:0;padding:var(--space-4)}.app-dialog__utility{min-width:0;border-left:1px solid var(--border-subtle);background:var(--rail-canvas)}@media(max-width:700px){.app-dialog__backdrop{place-items:end stretch;padding:0}.app-dialog{width:100%;max-height:90svh;border-radius:var(--radius-surface) var(--radius-surface) 0 0}.app-dialog:has(.app-dialog__utility) .app-dialog__workspace{grid-template-columns:minmax(0,1fr)}.app-dialog__utility{border-top:1px solid var(--border-subtle);border-left:0}}
</style>
