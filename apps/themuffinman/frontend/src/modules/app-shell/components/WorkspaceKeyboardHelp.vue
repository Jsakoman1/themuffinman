<script setup lang="ts">
import {ref} from "vue"

withDefaults(defineProps<{shortcuts?: Array<{keys: string; label: string}>}>(), {shortcuts: () => [
  {keys: "⌘/Ctrl K", label: "Open command center"}, {keys: "↑/↓ or J/K", label: "Move collection selection"}, {keys: "Enter / P", label: "Open or preview selected item"}, {keys: "Esc", label: "Close or clear selection"}, {keys: "?", label: "Show keyboard help"}
]})
const open = ref(false)
</script>

<template>
  <div class="workspace-keyboard-help">
    <button type="button" class="workspace-keyboard-help__trigger" aria-label="Show keyboard shortcuts" @click="open = true">?</button>
    <Teleport to="body"><div v-if="open" class="workspace-keyboard-help__backdrop" @click.self="open = false"><section class="workspace-keyboard-help__panel" role="dialog" aria-modal="true" aria-labelledby="workspace-keyboard-help-title" @keydown.esc="open = false"><header><h2 id="workspace-keyboard-help-title">Keyboard shortcuts</h2><button type="button" aria-label="Close keyboard shortcuts" @click="open = false">×</button></header><dl><template v-for="shortcut in shortcuts" :key="shortcut.keys"><dt><kbd>{{ shortcut.keys }}</kbd></dt><dd>{{ shortcut.label }}</dd></template></dl></section></div></Teleport>
  </div>
</template>

<style scoped>
.workspace-keyboard-help__trigger { display: inline-grid; place-items: center; width: var(--control-height-default); height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink-muted); font: inherit; font-weight: var(--text-weight-semibold); cursor: pointer; }.workspace-keyboard-help__trigger:hover, .workspace-keyboard-help__trigger:focus-visible { border-color: var(--control-border-active); background: var(--control-bg-hover); color: var(--control-ink); }.workspace-keyboard-help__trigger:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }.workspace-keyboard-help__backdrop { position: fixed; inset: 0; z-index: var(--z-dialog); display: grid; place-items: center; padding: var(--space-4); background: rgba(0, 0, 0, .64); }.workspace-keyboard-help__panel { width: min(100%, 28rem); border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }.workspace-keyboard-help__panel header { display: flex; align-items: center; justify-content: space-between; padding: var(--space-3) var(--space-4); border-bottom: 1px solid var(--border-subtle); }.workspace-keyboard-help__panel h2 { margin: 0; font-size: var(--text-size-title); }.workspace-keyboard-help__panel header button { width: var(--control-height-default); height: var(--control-height-default); border: 1px solid transparent; border-radius: var(--radius-control); background: transparent; color: var(--text-muted); font-size: 1.25rem; cursor: pointer; }.workspace-keyboard-help__panel header button:hover, .workspace-keyboard-help__panel header button:focus-visible { border-color: var(--control-border); background: var(--surface-hover); color: var(--text); }.workspace-keyboard-help__panel header button:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }.workspace-keyboard-help__panel dl { display: grid; grid-template-columns: auto 1fr; gap: var(--space-2) var(--space-3); margin: 0; padding: var(--space-4); font-size: var(--text-size-body); }.workspace-keyboard-help__panel dd { margin: 0; color: var(--text-muted); }.workspace-keyboard-help__panel kbd { padding: 0 var(--space-1); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); color: var(--text); font-family: var(--font); font-size: var(--text-size-meta); }
</style>
