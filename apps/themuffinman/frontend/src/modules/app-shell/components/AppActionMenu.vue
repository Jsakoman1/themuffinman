<script setup lang="ts">
import {ref} from "vue"

withDefaults(defineProps<{label?: string; disabled?: boolean}>(), {label: "More actions", disabled: false})
const open = ref(false)
const syncOpen = (event: Event) => { open.value = (event.currentTarget as HTMLDetailsElement).open }
</script>

<template>
  <details :open="open" class="app-action-menu" @toggle="syncOpen" @keydown.esc.prevent="open = false">
    <summary class="app-action-menu__summary" :aria-label="label" :title="label" :aria-expanded="open" :aria-disabled="disabled">⋯</summary>
    <div class="app-action-menu__items"><slot /></div>
  </details>
</template>

<style scoped>
.app-action-menu{position:relative}.app-action-menu__summary{display:inline-grid;place-items:center;width:2.25rem;height:2.25rem;border:1px solid var(--border-subtle);border-radius:50%;background:var(--surface);color:var(--text-muted);cursor:pointer;font-size:1.25rem;line-height:1;list-style:none}.app-action-menu__summary::-webkit-details-marker{display:none}.app-action-menu__summary:hover,.app-action-menu[open] .app-action-menu__summary{border-color:var(--border-strong);background:var(--surface-hover);color:var(--text)}.app-action-menu__summary[aria-disabled="true"]{pointer-events:none;opacity:.55}.app-action-menu__items{position:absolute;right:0;z-index:var(--z-popover);display:grid;gap:.25rem;min-width:10rem;margin-top:.35rem;padding:.35rem;border:1px solid var(--border-strong);border-radius:var(--radius-card);background:var(--surface-strong);box-shadow:var(--shadow-card)}.app-action-menu__items :deep(a),.app-action-menu__items :deep(button){display:block;width:100%;padding:.55rem .65rem;border:0;border-radius:var(--radius-control);background:transparent;color:var(--text);text-align:left;font:inherit;cursor:pointer}.app-action-menu__items :deep(a:hover),.app-action-menu__items :deep(button:hover){background:var(--surface-hover)}
</style>
