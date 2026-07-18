<script setup lang="ts">
import {useActionDialog} from "../composables/useActionDialog.ts"

const dialog = useActionDialog()
</script>

<template>
  <Teleport to="body">
    <div v-if="dialog.state.value.open" class="action-dialog__backdrop" role="presentation" @click.self="dialog.close(false)" @keydown.escape="dialog.close(false)">
      <section class="action-dialog" role="dialog" aria-modal="true" :aria-labelledby="'action-dialog-title'" aria-describedby="action-dialog-message">
        <div class="action-dialog__signal" aria-hidden="true"></div>
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
.action-dialog__backdrop{position:fixed;inset:0;z-index:var(--z-dialog,40);display:grid;place-items:center;padding:1rem;background:rgba(5,12,20,.62);backdrop-filter:blur(10px)}
.action-dialog{position:relative;width:min(100%,32rem);overflow:hidden;border:1px solid var(--border-strong);border-radius:var(--radius-card);background:var(--surface-strong);color:var(--text);box-shadow:0 28px 90px rgba(0,0,0,.34)}
.action-dialog__signal{height:3px;background:linear-gradient(90deg,var(--accent-cyan),var(--accent-amber),transparent)}
.action-dialog__header,.action-dialog__message,.action-dialog__actions{padding-inline:1.25rem}.action-dialog__header{padding-top:1.2rem}.action-dialog__eyebrow{margin:0 0 .45rem;color:var(--text-soft);font-family:var(--font-mono);font-size:.68rem;letter-spacing:.16em}.action-dialog h2{margin:0;font-size:1.35rem;letter-spacing:-.04em}.action-dialog__message{margin:1rem 0;color:var(--text-muted);line-height:1.6}.action-dialog__actions{display:flex;justify-content:flex-end;gap:.5rem;padding-top:.35rem;padding-bottom:1.1rem}.action-dialog__button{min-height:2.5rem;border:1px solid transparent;border-radius:var(--radius-control);padding:.6rem .9rem;background:var(--accent-cyan);color:#061018;font-weight:700}.action-dialog__button--quiet{border-color:var(--border-subtle);background:transparent;color:var(--text-muted)}.action-dialog__button--danger{background:var(--accent-red);color:#fff}.action-dialog__button--vision{background:var(--accent-amber);color:#1c1304}
</style>
