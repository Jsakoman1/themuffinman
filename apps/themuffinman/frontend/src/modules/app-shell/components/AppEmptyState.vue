<script setup lang="ts">
import {computed} from "vue"
const props = withDefaults(defineProps<{title: string; message?: string; reason?: "not-created" | "not-visible" | "filtered" | "failed" | "changed" | "forbidden"}>(), {reason: "not-created"})
const reasonLabel = computed(() => ({"not-created": "Nothing exists yet", "not-visible": "Nothing is visible to you", filtered: "No results match this view", failed: "Could not load this view", changed: "This view changed while you were editing", forbidden: "You do not have permission"}[props.reason]))
</script>

<template><section class="app-empty-state" :data-empty-reason="reason"><p class="app-empty-state__reason">{{ reasonLabel }}</p><h2>{{ title }}</h2><p v-if="message">{{ message }}</p><div v-if="$slots.actions" class="app-empty-state__actions"><slot name="actions" /></div></section></template>

<style scoped>
.app-empty-state { display: grid; justify-items: start; gap: var(--space-2); padding: var(--space-5) var(--space-3); border: 1px dashed var(--border-subtle); border-radius: var(--radius-surface); color: var(--text-muted); }.app-empty-state h2, .app-empty-state p { margin: 0; }.app-empty-state h2 { color: var(--text); font-size: var(--text-size-title); }.app-empty-state p { max-width: 36rem; font-size: var(--text-size-body); line-height: 1.5; }.app-empty-state__reason { color: var(--text-soft); font-size: var(--text-size-label) !important; font-weight: var(--text-weight-semibold); text-transform: uppercase; letter-spacing: var(--tracking-label); }.app-empty-state__actions { display: flex; gap: var(--space-2); margin-top: var(--space-1); }
</style>
