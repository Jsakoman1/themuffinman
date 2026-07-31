<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue"
import type {CalendarEvent} from "../../../contracts/index.ts"
import type {CalendarDay} from "../../../services/calendarTime.ts"
import {calendarDayKey, calendarMinutes} from "../../../services/calendarTime.ts"
import {useCalendarLayout} from "../composables/useCalendarLayout.ts"

const props = defineProps<{mode: "day" | "week"; days: CalendarDay[]; events: CalendarEvent[]; timezone: string; sourceLabel: (source: string) => string}>()
const emit = defineEmits<{select: [CalendarEvent]}>()
const hours = Array.from({length: 24}, (_, hour) => hour)
const scroller = ref<HTMLElement | null>(null)
const {eventsForDay, eventStyle, layoutTimedEvents} = useCalendarLayout(() => props.timezone)
const todayKey = computed(() => calendarDayKey(new Date(), props.timezone))
const currentMinutes = computed(() => calendarMinutes(new Date(), props.timezone))
const allDayFor = (day: CalendarDay) => eventsForDay(props.events.filter(event => event.allDay), day)
const timedFor = (day: CalendarDay) => layoutTimedEvents(eventsForDay(props.events, day))
const timedStyle = (event: CalendarEvent, column: number, columns: number) => ({...eventStyle(event), "--calendar-event-column": `${column}`, "--calendar-event-columns": `${columns}`})
const scrollToUsefulHour = async () => { await nextTick(); if (scroller.value) scroller.value.scrollTop = Math.max(0, Math.min(8 * 60 - 42, currentMinutes.value - 180)) }
onMounted(scrollToUsefulHour)
watch(() => [props.days.map(day => day.key).join(","), props.timezone], scrollToUsefulHour)
</script>

<template>
  <div ref="scroller" class="calendar-time-grid" :class="`calendar-time-grid--${mode}`" :style="{gridTemplateColumns: `repeat(${days.length}, minmax(18rem, 1fr))`}" role="grid" :aria-label="`${days.length === 1 ? 'Day' : 'Week'} calendar`">
    <section v-for="day in days" :key="day.key" class="calendar-time-grid__day" role="gridcell"><header><strong>{{ day.weekday }}</strong><span>{{ day.label }}</span></header><div class="calendar-time-grid__all-day"><small>All day</small><button v-for="event in allDayFor(day)" :key="event.eventKey" type="button" @click="emit('select', event)">{{ event.title }}</button></div><div class="calendar-time-grid__column"><div v-for="hour in hours" :key="hour" class="calendar-time-grid__hour"><span>{{ `${String(hour).padStart(2, '0')}:00` }}</span><i aria-hidden="true" /></div><i v-if="day.key === todayKey" class="calendar-time-grid__now" :style="{top: `${currentMinutes}px`}" aria-label="Current time" /><button v-for="placement in timedFor(day)" :key="placement.event.eventKey" type="button" class="calendar-time-grid__event" :style="timedStyle(placement.event, placement.column, placement.columns)" @click="emit('select', placement.event)"><strong>{{ placement.event.title }}</strong><small>{{ sourceLabel(placement.event.source) }}</small></button></div></section>
  </div>
</template>

<style scoped>
.calendar-time-grid{display:grid;overflow:auto;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--border-subtle);max-height:clamp(34rem,70vh,54rem)}.calendar-time-grid__day{min-width:18rem;background:var(--surface-base)}.calendar-time-grid__day>header{display:grid;gap:.15rem;padding:var(--space-2);border-bottom:1px solid var(--border-subtle);text-align:center}.calendar-time-grid__day>header strong{color:var(--text-muted);font-size:var(--text-size-label);text-transform:uppercase}.calendar-time-grid__day>header span{color:var(--text);font-weight:var(--text-weight-semibold)}.calendar-time-grid__all-day{display:grid;grid-template-columns:4.5rem minmax(0,1fr);gap:var(--space-1);min-height:2.5rem;padding:var(--space-1) var(--space-2);border-bottom:1px solid var(--border-subtle);background:var(--surface-sunken)}.calendar-time-grid__all-day small{color:var(--text-soft);font-size:var(--text-size-label)}.calendar-time-grid__all-day button{overflow:hidden;border:0;border-left:3px solid var(--accent);border-radius:var(--radius-control);padding:0 var(--space-1);background:var(--accent-muted);color:var(--text);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);line-height:1.7;text-align:left;text-overflow:ellipsis;white-space:nowrap;cursor:pointer}.calendar-time-grid__column{position:relative;height:1440px}.calendar-time-grid__hour{display:grid;grid-template-columns:4.5rem 1fr;height:60px}.calendar-time-grid__hour span{padding:0 var(--space-2);transform:translateY(-.5em);color:var(--text-soft);font-size:var(--text-size-label);font-variant-numeric:tabular-nums;text-align:right}.calendar-time-grid__hour i{display:block;border-top:1px solid var(--border-subtle)}.calendar-time-grid__now{position:absolute;z-index:2;right:0;left:4.5rem;height:2px;background:var(--danger);pointer-events:none}.calendar-time-grid__now::before{position:absolute;top:-3px;left:-4px;width:8px;height:8px;border-radius:50%;background:var(--danger);content:""}.calendar-time-grid__event{position:absolute;z-index:1;left:calc(4.75rem + ((100% - 5.1rem) * var(--calendar-event-column) / var(--calendar-event-columns)));width:calc((100% - 5.1rem) / var(--calendar-event-columns) - .35rem);display:grid;align-content:start;gap:.15rem;overflow:hidden;box-sizing:border-box;padding:var(--space-1) var(--space-2);border:0;border-left:3px solid var(--accent);border-radius:var(--radius-control);background:var(--accent-muted);color:var(--text);font:inherit;font-size:var(--text-size-meta);text-align:left;cursor:pointer}.calendar-time-grid__event:hover{background:var(--surface-hover)}.calendar-time-grid__event small{color:var(--text-muted)}@media(max-width:700px){.calendar-time-grid--week{display:none}.calendar-time-grid{grid-template-columns:1fr!important;max-height:none;overflow:visible;border:0}.calendar-time-grid__day{min-width:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface)}.calendar-time-grid__day:not(:first-child){display:none}}
</style>
