<script setup lang="ts">
import AppButton from "./AppButton.vue"

defineProps<{rangeTitle: string; mode: "day" | "week" | "month"; timezone: string; busy: boolean}>()
const emit = defineEmits<{previous: []; today: []; next: []; "update:mode": ["day" | "week" | "month"]}>()
</script>

<template>
  <section class="calendar-toolbar" aria-label="Calendar controls">
    <div class="calendar-toolbar__navigation">
      <AppButton type="button" tone="quiet" aria-label="Previous period" :disabled="busy" @click="emit('previous')">‹</AppButton>
      <AppButton type="button" tone="secondary" :disabled="busy" @click="emit('today')">Today</AppButton>
      <AppButton type="button" tone="quiet" aria-label="Next period" :disabled="busy" @click="emit('next')">›</AppButton>
    </div>
    <div class="calendar-toolbar__range"><strong>{{ rangeTitle }}</strong><small>{{ timezone }}</small></div>
    <div class="calendar-toolbar__modes" role="group" aria-label="Calendar view">
      <button v-for="value in ['day', 'week', 'month'] as const" :key="value" type="button" :aria-pressed="mode === value" :class="{active: mode === value}" @click="emit('update:mode', value)">{{ value }}</button>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.calendar-toolbar{display:flex;align-items:center;gap:var(--space-2);flex-wrap:wrap;padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.calendar-toolbar__navigation,.calendar-toolbar__modes{display:flex;align-items:center;gap:var(--space-1)}.calendar-toolbar__range{display:grid;gap:.1rem;min-width:min(16rem,100%);margin-right:auto}.calendar-toolbar__range strong{font-size:var(--text-size-title)}.calendar-toolbar__range small{color:var(--text-muted);font-size:var(--text-size-meta)}.calendar-toolbar__modes{padding:var(--space-1);border-radius:var(--radius-control);background:var(--surface-sunken)}.calendar-toolbar__modes button{min-height:var(--control-height-compact);border:0;border-radius:calc(var(--radius-control) - 2px);padding:0 var(--space-2);background:transparent;color:var(--text-muted);font:inherit;text-transform:capitalize;cursor:pointer}.calendar-toolbar__modes button:hover,.calendar-toolbar__modes button:focus-visible{background:var(--surface-hover);color:var(--text)}.calendar-toolbar__modes button.active{background:var(--accent);color:var(--accent-contrast)}@media(max-width:700px){.calendar-toolbar{align-items:flex-start}.calendar-toolbar__range{order:3;flex-basis:100%;min-width:0}.calendar-toolbar__modes{margin-left:auto}}
</style>
