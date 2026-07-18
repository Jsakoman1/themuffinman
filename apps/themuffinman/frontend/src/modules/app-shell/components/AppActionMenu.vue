<script setup lang="ts">
import {ref} from "vue"

withDefaults(defineProps<{label?: string; disabled?: boolean}>(), {label: "More actions", disabled: false})
const open = ref(false)
const syncOpen = (event: Event) => { open.value = (event.currentTarget as HTMLDetailsElement).open }
const preventDisabledToggle = (event: Event) => { if (open.value === false && (event.currentTarget as HTMLDetailsElement).getAttribute("data-disabled") === "true") event.preventDefault() }
const closeAfterAction = () => { open.value = false }
</script>

<template>
  <details :open="open" :data-disabled="disabled" class="app-action-menu" @click.capture="preventDisabledToggle" @contextmenu.prevent="disabled ? undefined : open = true" @toggle="syncOpen" @keydown.esc.prevent="open = false">
    <summary class="app-action-menu__summary" :aria-label="label" :title="label" :aria-expanded="open" :aria-disabled="disabled">⋯</summary>
    <div class="app-action-menu__items" role="menu" @click="closeAfterAction"><slot /></div>
  </details>
</template>

<style scoped>
.app-action-menu { position: relative; }.app-action-menu__summary { display: inline-grid; place-items: center; width: var(--control-height-default); height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: transparent; color: var(--control-ink-muted); cursor: pointer; font-size: 1.25rem; line-height: 1; list-style: none; }.app-action-menu__summary::-webkit-details-marker { display: none; }.app-action-menu__summary:hover, .app-action-menu[open] .app-action-menu__summary { border-color: var(--control-border-active); background: var(--control-bg-hover); color: var(--control-ink); }.app-action-menu__summary:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }.app-action-menu__summary[aria-disabled="true"] { cursor: not-allowed; opacity: .55; }.app-action-menu__items { position: absolute; right: 0; z-index: var(--z-popover); display: grid; gap: var(--space-1); min-width: 11rem; margin-top: var(--space-1); padding: var(--space-1); border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }.app-action-menu__items :deep(a), .app-action-menu__items :deep(button) { display: block; width: 100%; min-height: var(--control-height-default); padding: var(--space-1) var(--space-2); border: 0; border-radius: var(--radius-control); background: transparent; color: var(--text); text-align: left; font: inherit; cursor: pointer; }.app-action-menu__items :deep(a:hover), .app-action-menu__items :deep(button:hover) { background: var(--surface-hover); }
</style>
