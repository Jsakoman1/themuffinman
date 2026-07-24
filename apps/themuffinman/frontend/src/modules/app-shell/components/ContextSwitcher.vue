<script setup lang="ts">
export type ContextSwitcherOption = {id: string | number; label: string; description?: string}
withDefaults(defineProps<{modelValue: string | number | null; options: ContextSwitcherOption[]; label?: string; emptyLabel?: string}>(), {label: "Context", emptyLabel: "Choose context"})
const emit = defineEmits<{"update:modelValue": [string | number | null]}>()
</script>
<template><label class="context-switcher"><span class="context-switcher__label">{{ label }}</span><select :value="modelValue ?? ''" :aria-label="label" @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value || null)"><option v-if="!options.length" value="" disabled>{{ emptyLabel }}</option><option v-for="option in options" :key="String(option.id)" :value="String(option.id)" :title="option.description">{{ option.label }}</option></select></label></template>
<!-- Context selection changes only the read projection; permissions and actions remain server-owned. -->
<style scoped>.context-switcher{display:inline-grid;gap:.15rem;min-width:0}.context-switcher__label{color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.context-switcher select{min-height:var(--control-height-default);max-width:18rem;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}</style>
