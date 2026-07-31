<script setup lang="ts">
import {RouterLink} from "vue-router"
import type {CalendarEvent} from "../../../contracts/index.ts"
import {formatInTimezone} from "../../../services/formatters.ts"

const props = defineProps<{event: CalendarEvent | null; timezone: string; sourceLabel: (source: string) => string}>()
const emit = defineEmits<{close: []}>()
</script>

<template>
  <div v-if="props.event" class="calendar-event-popover" data-archetype="calendar-quick-look" role="presentation" @click.self="emit('close')"><section role="dialog" aria-modal="true" :aria-label="props.event.title"><button type="button" class="calendar-event-popover__close" aria-label="Close event details" @click="emit('close')">×</button><p>{{ sourceLabel(props.event.source) }}</p><h2>{{ props.event.title }}</h2><span>{{ props.event.allDay ? 'All day' : `${formatInTimezone(props.event.startsAt, timezone, 'Unknown time')} – ${formatInTimezone(props.event.endsAt, timezone, 'Unknown time')}` }}</span><small v-if="props.event.businessName">{{ props.event.businessName }}</small><RouterLink :to="props.event.navigationPath" @click="emit('close')">Open details</RouterLink></section></div>
</template>

<style scoped>
.calendar-event-popover{position:fixed;z-index:var(--z-popover);inset:0;display:grid;place-items:center;padding:var(--space-4);background:color-mix(in srgb,var(--canvas) 30%,transparent)}.calendar-event-popover section{position:relative;display:grid;gap:var(--space-2);width:min(24rem,100%);padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-popover)}.calendar-event-popover p{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.calendar-event-popover h2{margin:0;font-size:var(--text-size-title)}.calendar-event-popover span,.calendar-event-popover small{color:var(--text-muted)}.calendar-event-popover a{justify-self:start;margin-top:var(--space-2);color:var(--accent);font-weight:var(--text-weight-semibold);text-decoration:none}.calendar-event-popover__close{position:absolute;top:var(--space-2);right:var(--space-2);border:0;background:transparent;color:var(--text-muted);font:inherit;font-size:1.5rem;cursor:pointer}@media(max-width:700px){.calendar-event-popover{align-items:end;padding:0}.calendar-event-popover section{width:100%;border-bottom:0;border-radius:var(--radius-surface) var(--radius-surface) 0 0;padding-bottom:max(var(--space-5),env(safe-area-inset-bottom))}}
</style>
