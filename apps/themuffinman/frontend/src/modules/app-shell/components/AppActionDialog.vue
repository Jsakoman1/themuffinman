<script setup lang="ts">
import {useActionDialog} from "../composables/useActionDialog.ts"

const dialog = useActionDialog()
</script>

<template>
  <Teleport to="body">
    <div v-if="dialog.state.value.open" class="action-dialog__backdrop" role="presentation" @click.self="dialog.close(false)" @keydown.escape="dialog.close(false)">
      <section class="action-dialog" role="dialog" aria-modal="true" :aria-labelledby="'action-dialog-title'" aria-describedby="action-dialog-message">
        <header class="action-dialog__header">
          <p class="action-dialog__eyebrow">{{ dialog.state.value.mode === 'notice' ? 'SYSTEM NOTICE' : 'ACTION GATE' }}</p>
          <h2 id="action-dialog-title">{{ dialog.state.value.title }}</h2>
        </header>
        <p id="action-dialog-message" class="action-dialog__message">{{ dialog.state.value.message }}</p>
        <footer class="action-dialog__actions" aria-label="Confirmation actions" data-surface="shared-action-gate">
          <button v-if="dialog.state.value.mode === 'confirm'" type="button" class="action-dialog__button action-dialog__button--quiet" @click="dialog.close(false)">{{ dialog.state.value.cancelLabel }}</button>
          <button type="button" class="action-dialog__button" :class="`action-dialog__button--${dialog.state.value.tone}`" @click="dialog.close(true)">{{ dialog.state.value.confirmLabel }}</button>
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.action-dialog__backdrop { position: fixed; inset: 0; z-index: var(--z-dialog); display: grid; place-items: center; padding: var(--space-4); background: rgba(0, 0, 0, .64); backdrop-filter: blur(6px); }.action-dialog { position: relative; width: min(100%, 32rem); border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); color: var(--text); box-shadow: var(--shadow-overlay); }.action-dialog__header, .action-dialog__message, .action-dialog__actions { padding-inline: var(--space-5); }.action-dialog__header { padding-top: var(--space-5); }.action-dialog__eyebrow { margin: 0 0 var(--space-2); color: var(--text-soft); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); }.action-dialog h2 { margin: 0; font-size: var(--text-size-title); letter-spacing: var(--tracking-tight); }.action-dialog__message { margin: var(--space-3) 0; color: var(--text-muted); line-height: 1.5; }.action-dialog__actions { display: flex; justify-content: flex-end; gap: var(--space-2); padding-top: var(--space-1); padding-bottom: var(--space-4); }.action-dialog__button { min-height: var(--control-height-default); border: 1px solid var(--accent); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: var(--accent); color: var(--canvas); font-weight: var(--text-weight-semibold); cursor: pointer; }.action-dialog__button:hover, .action-dialog__button:focus-visible { filter: brightness(1.08); }.action-dialog__button:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }.action-dialog__button--quiet { border-color: var(--control-border); background: transparent; color: var(--text-muted); }.action-dialog__button--danger { border-color: var(--danger); background: var(--danger); color: var(--canvas); }.action-dialog__button--vision { border-color: var(--accent-amber); background: var(--accent-amber); color: var(--canvas); }
</style>
