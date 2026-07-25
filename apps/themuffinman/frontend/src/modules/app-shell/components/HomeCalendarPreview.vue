<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink} from "vue-router"
import type {CalendarEvent} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "./AppButton.vue"
import AppStatus from "./AppStatus.vue"
import {formatInTimezone} from "../../../services/formatters.ts"

type CalendarMode = "day" | "week" | "month"
const mode = ref<CalendarMode>("week")
const cursor = ref(new Date())
const events = ref<CalendarEvent[]>([])
const timezone = ref("UTC")
const loading = ref(true)
const error = ref("")
const startOfWeek = (value: Date) => { const start = new Date(value); start.setHours(0, 0, 0, 0); start.setDate(start.getDate() - start.getDay()); return start }
const dates = computed(() => {
  const start = mode.value === "week" ? startOfWeek(cursor.value) : mode.value === "month" ? new Date(cursor.value.getFullYear(), cursor.value.getMonth(), 1) : new Date(cursor.value)
  start.setHours(0, 0, 0, 0)
  const count = mode.value === "week" ? 7 : mode.value === "month" ? new Date(start.getFullYear(), start.getMonth() + 1, 0).getDate() : 1
  return Array.from({length: count}, (_, index) => { const date = new Date(start); date.setDate(start.getDate() + index); return date })
})
const range = computed(() => { const from = dates.value[0] ?? cursor.value; const to = new Date(dates.value.at(-1) ?? cursor.value); to.setDate(to.getDate() + 1); return {from: from.toISOString(), to: to.toISOString()} })
const title = computed(() => mode.value === "day" ? cursor.value.toLocaleDateString(undefined, {weekday: "long", month: "long", day: "numeric"}) : mode.value === "month" ? cursor.value.toLocaleDateString(undefined, {month: "long", year: "numeric"}) : `${dates.value[0]?.toLocaleDateString(undefined, {month: "short", day: "numeric"})} – ${dates.value.at(-1)?.toLocaleDateString(undefined, {month: "short", day: "numeric"})}`)
const dateKey = (date: Date) => `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`
const visibleEvents = computed(() => events.value)
const eventsForDate = (date: Date) => visibleEvents.value.filter((event) => dateKey(new Date(event.startsAt)) === dateKey(date))
const load = async () => { loading.value = true; error.value = ""; try { const projection = await userShellApi.getCalendarProjection({...range.value, view: mode.value}); events.value = projection.events; timezone.value = projection.timezone || timezone.value } catch { error.value = "Could not load your calendar." } finally { loading.value = false } }
const move = (amount: number) => { const next = new Date(cursor.value); if (mode.value === "day") next.setDate(next.getDate() + amount); else if (mode.value === "week") next.setDate(next.getDate() + amount * 7); else next.setMonth(next.getMonth() + amount); cursor.value = next }
const today = () => { cursor.value = new Date() }
watch([mode, cursor], () => void load())
onMounted(() => void load())
</script>

<template>
  <section class="home-calendar" aria-labelledby="home-calendar-title">
    <header class="home-calendar__header"><h2 id="home-calendar-title">Calendar</h2><div class="home-calendar__modes" role="group" aria-label="Calendar view"><button v-for="value in ['day', 'week', 'month']" :key="value" type="button" :class="{active: mode === value}" :aria-pressed="mode === value" @click="mode = value as CalendarMode">{{ value }}</button></div></header>
    <div class="home-calendar__toolbar"><AppButton type="button" tone="quiet" aria-label="Previous period" @click="move(-1)">‹</AppButton><button class="home-calendar__title" type="button" @click="today">{{ title }}</button><AppButton type="button" tone="quiet" aria-label="Next period" @click="move(1)">›</AppButton></div>
    <AppStatus v-if="loading" message="Loading calendar." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <div v-else class="home-calendar__days" :class="`home-calendar__days--${mode}`"><article v-for="date in dates" :key="date.toISOString()" class="home-calendar__day"><header><strong>{{ date.toLocaleDateString(undefined, {weekday: "short"}) }}</strong><span>{{ date.toLocaleDateString(undefined, {day: "numeric", month: "short"}) }}</span></header><RouterLink v-for="event in eventsForDate(date)" :key="event.eventKey" :to="event.navigationPath" class="home-calendar__event"><strong>{{ event.title }}</strong><small>{{ formatInTimezone(event.startsAt, timezone, "Unknown time") }}</small></RouterLink></article><p v-if="visibleEvents.length === 0" class="home-calendar__empty">No events in this period.</p></div>
  </section>
</template>

<style scoped>
.home-calendar{display:grid;gap:var(--space-3);padding-top:var(--space-2);border-top:1px solid var(--border-subtle)}.home-calendar__header,.home-calendar__toolbar{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2)}.home-calendar h2{margin:0;font-size:var(--text-size-title);letter-spacing:var(--tracking-tight)}.home-calendar__modes{display:flex;gap:2px;padding:2px;border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base)}.home-calendar__modes button{border:0;border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:transparent;color:var(--text-muted);font:inherit;font-size:var(--text-size-meta);text-transform:capitalize;cursor:pointer}.home-calendar__modes button.active{background:var(--accent);color:var(--canvas)}.home-calendar__title{border:0;background:transparent;color:var(--text);font:inherit;font-size:var(--text-size-body);font-weight:var(--text-weight-semibold);cursor:pointer}.home-calendar__days{display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:1px;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--border-subtle)}.home-calendar__days--day{grid-template-columns:minmax(0,1fr)}.home-calendar__days--month{grid-template-columns:repeat(7,minmax(0,1fr))}.home-calendar__day{min-height:7rem;padding:var(--space-2);background:var(--surface-base)}.home-calendar__day>header{display:flex;justify-content:space-between;gap:var(--space-1);margin-bottom:var(--space-2);color:var(--text-muted);font-size:var(--text-size-meta)}.home-calendar__event{display:grid;gap:2px;margin-top:var(--space-1);padding:var(--space-1);border-left:2px solid var(--accent);border-radius:var(--radius-control);background:var(--accent-muted);color:var(--text);text-decoration:none;font-size:var(--text-size-meta)}.home-calendar__event small{color:var(--text-muted)}.home-calendar__empty{grid-column:1 / -1;margin:0;padding:var(--space-3);background:var(--surface-base);color:var(--text-muted);font-size:var(--text-size-meta)}@media(max-width:700px){.home-calendar__header{align-items:flex-start;flex-direction:column}.home-calendar__days,.home-calendar__days--month{grid-template-columns:1fr}.home-calendar__days--week{grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>
