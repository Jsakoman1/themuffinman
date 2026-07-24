<script setup lang="ts">
withDefaults(defineProps<{message: string; tone?: "neutral" | "success" | "warning" | "error" | "stale"; retry?: boolean; busy?: boolean}>(), {tone: "neutral", retry: false, busy: false})
const emit = defineEmits<{retry: []}>()
</script>

<template>
  <p class="app-status" :class="`app-status--${tone}`" :data-state="tone" :data-busy="busy || undefined" :role="tone === 'error' ? 'alert' : 'status'" :aria-busy="busy || undefined" :aria-live="tone === 'error' ? 'assertive' : 'polite'" aria-atomic="true">
    <span class="app-status__indicator" :class="{'app-status__indicator--busy': busy}" aria-hidden="true" />
    <span class="app-status__message">{{ message }}</span>
    <button v-if="retry" type="button" :disabled="busy" :aria-disabled="busy ? 'true' : undefined" :aria-label="busy ? 'Retrying' : 'Retry'" @click="emit('retry')">{{ busy ? 'Retrying…' : 'Retry' }}</button>
  </p>
</template>

<style scoped>
.app-status { display: flex; align-items: center; gap: var(--space-2); margin: 0; padding: var(--space-2) 0; color: var(--text-muted); font-size: var(--text-size-meta); }
.app-status__message { min-width: 0; }
.app-status__indicator { width: 0.45rem; height: 0.45rem; flex: 0 0 auto; border-radius: 50%; background: currentColor; }
.app-status__indicator--busy { animation: app-status-pulse 900ms ease-in-out infinite; }@keyframes app-status-pulse { 50% { opacity: .35; } }@media (prefers-reduced-motion: reduce) { .app-status__indicator--busy { animation: none; } }
.app-status--success { color: var(--success); }.app-status--warning, .app-status--stale { color: var(--warning); }.app-status--error { color: var(--danger); }.app-status--stale .app-status__indicator { box-shadow: 0 0 0 2px var(--danger-muted); }
.app-status button { min-height: var(--control-height-compact); margin-left: var(--space-1); padding: 0 var(--space-2); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: transparent; color: inherit; font: inherit; font-weight: var(--text-weight-semibold); cursor: pointer; }
.app-status button:hover, .app-status button:focus-visible { border-color: var(--control-border-active); background: var(--control-bg-hover); }
.app-status button:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }
</style>
