<script setup lang="ts">
import {computed} from "vue"

type CalendarSource = {key: string; label: string; color: string}
const props = defineProps<{sources: CalendarSource[]; modelValue: string[]}>()
const emit = defineEmits<{"update:modelValue": [string[]]}>()
const summary = computed(() => props.modelValue.length === props.sources.length ? "All calendars" : `${props.modelValue.length} calendars`)
const toggle = (key: string) => emit("update:modelValue", props.modelValue.includes(key) ? props.modelValue.filter(source => source !== key) : [...props.modelValue, key])
const showAll = () => emit("update:modelValue", props.sources.map(source => source.key))
</script>

<template>
  <details class="calendar-source-popover"><summary>Calendars <span>{{ summary }}</span></summary><div class="calendar-source-popover__panel"><header><strong>Show in calendar</strong><button type="button" @click="showAll">Show all</button></header><button v-for="source in sources" :key="source.key" type="button" class="calendar-source-popover__source" :aria-pressed="modelValue.includes(source.key)" @click="toggle(source.key)"><i :style="{backgroundColor: source.color}" aria-hidden="true" /><span>{{ source.label }}</span><b v-if="modelValue.includes(source.key)">✓</b></button></div></details>
</template>

<style scoped>
.calendar-source-popover{position:relative}.calendar-source-popover summary{display:flex;align-items:center;gap:var(--space-1);min-height:var(--control-height-default);padding:0 var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink);font-weight:var(--text-weight-semibold);cursor:pointer;list-style:none}.calendar-source-popover summary::-webkit-details-marker{display:none}.calendar-source-popover summary span{color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-regular)}.calendar-source-popover__panel{position:absolute;right:0;z-index:var(--z-popover);display:grid;gap:var(--space-1);min-width:15rem;margin-top:var(--space-1);padding:var(--space-2);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-popover)}.calendar-source-popover__panel header{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2);padding:var(--space-1)}.calendar-source-popover__panel header button{border:0;background:transparent;color:var(--accent);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.calendar-source-popover__source{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:var(--space-2);border:0;border-radius:var(--radius-control);padding:var(--space-2);background:transparent;color:var(--text);font:inherit;text-align:left;cursor:pointer}.calendar-source-popover__source:hover,.calendar-source-popover__source:focus-visible{background:var(--surface-hover)}.calendar-source-popover__source i{width:.65rem;height:.65rem;border-radius:50%}.calendar-source-popover__source b{color:var(--accent)}@media(max-width:700px){.calendar-source-popover__panel{position:fixed;right:var(--space-3);left:var(--space-3);bottom:max(var(--space-3),env(safe-area-inset-bottom));min-width:0}}
</style>
