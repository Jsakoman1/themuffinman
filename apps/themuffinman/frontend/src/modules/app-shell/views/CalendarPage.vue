<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {CalendarEvent, CalendarProjection} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppButton from "../components/AppButton.vue"
import {formatInTimezone} from "../../../services/formatters.ts"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import CalendarToolbar from "../components/CalendarToolbar.vue"
import CalendarSourcePopover from "../components/CalendarSourcePopover.vue"
import {calendarDays} from "../../../services/calendarTime.ts"
import {useCalendarLayout} from "../composables/useCalendarLayout.ts"
import CalendarTimeGrid from "../components/CalendarTimeGrid.vue"
import CalendarMobileAgenda from "../components/CalendarMobileAgenda.vue"
import CalendarEventPopover from "../components/CalendarEventPopover.vue"

type CalendarMode = "day" | "week" | "month"
const mode = ref<CalendarMode>("week")
const route = useRoute()
const surface = getAppSurfaceConfig("calendar")
const cursor = ref(new Date())
const events = ref<CalendarEvent[]>([])
type CalendarSource = CalendarProjection["sources"][number]
const availableSources = ref<CalendarSource[]>([])
const timezone = ref(Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC")
const selectedSources = ref<string[]>([])
const selectedMobileDay = ref("")
const selectedEvent = ref<CalendarEvent | null>(null)
const calendarScopeKey = computed(() => `calendar:${route.query.businessId || "all"}:${mode.value}:${range.value.from}:${range.value.to}`)
const sourceStorageKey = "calendar-visible-sources"
const loading = ref(true)
const error = ref("")
const sourceLabel = (source: string) => availableSources.value.find(item => item.key === source)?.label ?? source
const range = computed(() => {
  const from = new Date(cursor.value)
  const to = new Date(cursor.value)
  if (mode.value === "day") to.setDate(to.getDate() + 1)
  else if (mode.value === "week") { from.setDate(from.getDate() - from.getDay()); to.setDate(from.getDate() + 7) }
  else if (mode.value === "month") { from.setDate(1); to.setMonth(from.getMonth() + 1); to.setDate(0); to.setDate(to.getDate() + 1) }
  else { from.setDate(from.getDate() - 30); to.setDate(to.getDate() + 90) }
  return {from: from.toISOString(), to: to.toISOString()}
})
const title = computed(() => {
  if (mode.value === "day") return cursor.value.toLocaleDateString(undefined, {weekday: "long", month: "long", day: "numeric", year: "numeric"})
  if (mode.value === "week") {
    const start = new Date(cursor.value)
    start.setDate(start.getDate() - start.getDay())
    const end = new Date(start)
    end.setDate(end.getDate() + 6)
    return `${start.toLocaleDateString(undefined, {month: "short", day: "numeric"})} – ${end.toLocaleDateString(undefined, {month: "short", day: "numeric", year: "numeric"})}`
  }
  return cursor.value.toLocaleDateString(undefined, {month: "long", year: "numeric"})
})
const visibleEvents = computed(() => events.value.filter(event => selectedSources.value.includes(event.source)))
const upcomingEvents = computed(() => visibleEvents.value
  .filter(event => new Date(event.endsAt).getTime() >= Date.now())
  .sort((left, right) => new Date(left.startsAt).getTime() - new Date(right.startsAt).getTime())
  .slice(0, 3))
const gridDates = computed(() => calendarDays(cursor.value, mode.value, timezone.value))
const {eventsForDay} = useCalendarLayout(() => timezone.value)
const eventsForDate = (date: (typeof gridDates.value)[number]) => eventsForDay(visibleEvents.value, date)
const setMobileDay = (key: string) => { selectedMobileDay.value = key }
const load = async () => { loading.value = true; error.value = ""; try { const businessId = typeof route.query.businessId === "string" ? Number(route.query.businessId) : undefined; const projection = await userShellApi.getCalendarProjection({...range.value, businessId, view: mode.value, sources: selectedSources.value.length ? selectedSources.value : undefined}); events.value = projection.events; availableSources.value = projection.sources; timezone.value = projection.timezone || timezone.value; if (!selectedSources.value.length) { let saved: string[] = []; if (typeof window !== "undefined") { try { const parsed: unknown = JSON.parse(window.localStorage.getItem(sourceStorageKey) || "[]"); saved = Array.isArray(parsed) && parsed.every(value => typeof value === "string") ? parsed : [] } catch { saved = [] } } selectedSources.value = saved.filter(source => projection.sources.some(item => item.key === source)); if (!selectedSources.value.length) selectedSources.value = projection.sources.map(source => source.key) } } catch { error.value = "Could not load your unified calendar." } finally { loading.value = false } }
const move = (amount: number) => { const next = new Date(cursor.value); if (mode.value === "day") next.setDate(next.getDate() + amount); else if (mode.value === "week") next.setDate(next.getDate() + amount * 7); else next.setMonth(next.getMonth() + amount); cursor.value = next }
const today = () => { cursor.value = new Date() }
watch([mode, cursor, selectedSources], () => { if (typeof window !== "undefined" && selectedSources.value.length) window.localStorage.setItem(sourceStorageKey, JSON.stringify(selectedSources.value)); void load() }, {deep: true})
watch(gridDates, days => { if (!days.some(day => day.key === selectedMobileDay.value)) selectedMobileDay.value = days[0]?.key ?? "" }, {immediate: true})
watch(() => route.query.businessId, () => { events.value = []; void load() })
onMounted(() => void load())
</script>

<template>
  <section class="calendar-page native-group" aria-label="Unified calendar" data-calendar-model="all-module-events" data-mental-model="today-range-source-preview" data-conflict-policy="source-authoritative" data-trust-cue="source-and-permission-aware" data-preview-model="shared-adjacent-preview" data-read-model="bounded-range-locale-aware" data-responsive-model="desktop-grid-mobile-agenda">
    <SurfaceHeader :config="surface" title="Calendar" description="Everything scheduled across your workspace." />
    <CalendarToolbar :range-title="title" :mode="mode" :timezone="timezone" :busy="loading" @previous="move(-1)" @today="today" @next="move(1)" @update:mode="mode = $event"><CalendarSourcePopover v-model="selectedSources" :sources="availableSources" /></CalendarToolbar>
    <section v-if="!loading && !error" class="calendar-page__next" aria-label="What is next"><header><div><p>Up next</p><h2>{{ upcomingEvents.length ? 'Your next scheduled items' : 'Nothing else is scheduled' }}</h2></div><AppButton type="button" tone="quiet" @click="today">Show today</AppButton></header><div v-if="upcomingEvents.length" class="calendar-page__next-list"><RouterLink v-for="event in upcomingEvents" :key="event.eventKey" :to="event.navigationPath"><strong>{{ event.title }}</strong><span>{{ formatInTimezone(event.startsAt, timezone, 'Unknown time') }} · {{ sourceLabel(event.source) }}</span></RouterLink></div><p v-else>Try another time range or add plans in the relevant workspace.</p></section>
    <AppStatus v-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="loading" :message="`Loading ${calendarScopeKey}.`" busy />
    <div v-else-if="mode === 'month'" class="calendar-page__grid" :data-calendar-view="mode" role="grid" :aria-label="`${mode} calendar`"><section v-for="date in gridDates" :key="date.key" class="calendar-page__grid-day" role="gridcell"><h2>{{ date.weekday }} {{ date.label }}</h2><button v-for="event in eventsForDate(date)" :key="event.eventKey" type="button" class="calendar-page__grid-event" @click="selectedEvent = event"><strong>{{ event.title }}</strong><small>{{ formatInTimezone(event.startsAt, timezone, "Unknown time") }} · {{ sourceLabel(event.source) }}</small></button></section></div>
    <CalendarTimeGrid v-else :mode="mode" :days="gridDates" :events="visibleEvents" :timezone="timezone" :source-label="sourceLabel" :data-calendar-view="mode" @select="selectedEvent = $event" />
    <CalendarMobileAgenda v-if="!loading && !error && mode !== 'day'" :mode="mode" :days="gridDates" :selected-day="selectedMobileDay" :events="visibleEvents" :timezone="timezone" :source-label="sourceLabel" @update:selected-day="setMobileDay" @select="selectedEvent = $event" />
    <CalendarEventPopover :event="selectedEvent" :timezone="timezone" :source-label="sourceLabel" @close="selectedEvent = null" />
    <p v-if="!error && !loading && visibleEvents.length === 0" class="calendar-page__empty">No events match the selected sources and time range.</p>
  </section>
</template>

<style scoped>
.calendar-page{border-top-color:var(--orientation-line)}
.calendar-page{display:grid;gap:var(--space-4);min-width:0}.calendar-page__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-4)}.calendar-page__header h1,.calendar-page__header p{margin:0}.calendar-page__header h1{font-size:clamp(1.8rem,3vw,2.6rem);letter-spacing:-.04em}.calendar-page__header>div>p:last-child{margin-top:var(--space-2);color:var(--text-muted)}.calendar-page__eyebrow{margin-bottom:var(--space-1)!important;color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-transform:uppercase;letter-spacing:.06em}.calendar-page__navigation,.calendar-page__modes{display:flex;align-items:center;gap:var(--space-1)}.calendar-page__modes{padding:var(--space-1);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--control-bg)}.calendar-page__modes button{min-height:var(--control-height-compact);padding:0 var(--space-2);border:0;border-radius:calc(var(--radius-control) - 2px);background:transparent;color:var(--text-muted);font:inherit;text-transform:capitalize;cursor:pointer;transition:background-color .16s ease,color .16s ease}.calendar-page__modes button:hover,.calendar-page__modes button:focus-visible{background:var(--surface-hover);color:var(--text)}.calendar-page__modes button.active{background:var(--accent);color:var(--canvas)}.calendar-page__filters{position:relative;display:grid;gap:var(--space-1);padding:var(--space-1)}.calendar-page__filters summary{cursor:pointer;color:var(--text-muted);font-weight:var(--text-weight-semibold)}.calendar-page__filters label{display:block;padding:var(--space-1);white-space:nowrap}.calendar-page__agenda{display:grid;gap:var(--space-3)}.calendar-page__day{display:grid;gap:var(--space-1);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.calendar-page__day h2{margin:0 0 var(--space-2);font-size:var(--text-size-title)}.calendar-page__event{display:grid;grid-template-columns:5rem 1fr;gap:var(--space-3);padding:var(--space-2);border-radius:var(--radius-control);color:var(--text);text-decoration:none}.calendar-page__event:hover{background:var(--surface-hover)}.calendar-page__event-time{color:var(--text-muted);font-variant-numeric:tabular-nums}.calendar-page__event span:last-child{display:grid;gap:var(--space-1)}.calendar-page__event small{color:var(--text-muted)}.calendar-page__grid{display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:1px;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--border-subtle)}.calendar-page__grid-day{display:grid;align-content:start;gap:var(--space-2);min-height:var(--calendar-grid-min-height);padding:var(--space-2);background:var(--surface-base)}.calendar-page__grid-day h2{margin:0;color:var(--text-muted);font-size:var(--text-size-meta)}.calendar-page__grid-event{display:grid;gap:.15rem;padding:var(--space-1);border-radius:var(--radius-control);background:var(--surface-selected);color:var(--text);text-decoration:none;font-size:var(--text-size-meta)}.calendar-page__grid-event:hover{background:var(--surface-hover)}.calendar-page__grid-event small,.calendar-page__grid-empty{color:var(--text-muted);font-size:var(--text-size-label)}.calendar-page__grid-empty{margin:0}@media(max-width:700px){.calendar-page__header{display:grid}.calendar-page__navigation{justify-content:flex-start}.calendar-page__event{grid-template-columns:4rem 1fr}.calendar-page__modes{width:max-content;max-width:100%;overflow:auto}.calendar-page__grid{grid-template-columns:1fr;gap:var(--space-2);border:0;background:transparent}.calendar-page__grid-day{min-height:5rem;border:1px solid var(--border-subtle);border-radius:var(--radius-surface)}}
.calendar-page__next{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.calendar-page__next header{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2)}.calendar-page__next header p{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.calendar-page__next h2,.calendar-page__next>p{margin:0}.calendar-page__next h2{font-size:var(--text-size-title)}.calendar-page__next>p{color:var(--text-muted)}.calendar-page__next-list{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-2)}.calendar-page__next-list a{display:grid;gap:var(--space-1);padding:var(--space-2);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);text-decoration:none}.calendar-page__next-list a:hover{background:var(--surface-hover)}.calendar-page__next-list span{color:var(--text-muted);font-size:var(--text-size-meta)}@media(max-width:700px){.calendar-page__next header{align-items:start;flex-direction:column}.calendar-page__next-list{grid-template-columns:1fr}}
.calendar-page__time-grid{display:grid;grid-template-columns:repeat(var(--calendar-days,1),minmax(0,1fr));overflow:auto;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--border-subtle)}.calendar-page__time-day{min-width:18rem;background:var(--surface-base)}.calendar-page__time-day>header{display:grid;gap:.15rem;padding:var(--space-2);border-bottom:1px solid var(--border-subtle);text-align:center}.calendar-page__time-day>header strong{color:var(--text-muted);font-size:var(--text-size-label);text-transform:uppercase}.calendar-page__time-day>header span{color:var(--text);font-weight:var(--text-weight-semibold)}.calendar-page__time-column{position:relative;height:1440px}.calendar-page__hour{display:grid;grid-template-columns:4.5rem 1fr;height:60px}.calendar-page__hour span{padding:0 var(--space-2);transform:translateY(-.5em);color:var(--text-soft);font-size:var(--text-size-label);font-variant-numeric:tabular-nums;text-align:right}.calendar-page__hour i{display:block;border-top:1px solid var(--border-subtle)}.calendar-page__time-event{position:absolute;left:4.75rem;right:.35rem;display:grid;align-content:start;gap:.15rem;overflow:hidden;padding:var(--space-1) var(--space-2);border-left:3px solid var(--accent);border-radius:var(--radius-control);background:var(--accent-muted);color:var(--text);text-decoration:none;font-size:var(--text-size-meta)}.calendar-page__time-event small{color:var(--text-muted)}@media(max-width:700px){.calendar-page__time-grid{grid-template-columns:repeat(var(--calendar-days,1),minmax(16rem,1fr))}.calendar-page__time-day{min-width:16rem}}
.calendar-page__mobile-agenda{display:none}.calendar-page__mobile-agenda header{display:grid;gap:var(--space-1)}.calendar-page__mobile-agenda header p{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.calendar-page__mobile-agenda h2,.calendar-page__mobile-agenda>p{margin:0}.calendar-page__mobile-agenda a{display:grid;grid-template-columns:6.5rem minmax(0,1fr);gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface-raised);color:var(--text);text-decoration:none}.calendar-page__mobile-agenda time{color:var(--text-muted);font-size:var(--text-size-meta)}.calendar-page__mobile-agenda a span{display:grid;gap:var(--space-1)}.calendar-page__mobile-agenda small{color:var(--text-muted);font-size:var(--text-size-meta)}@media(max-width:700px){.calendar-page__mobile-agenda{display:grid;gap:var(--space-2)}.calendar-page__grid,.calendar-page__time-grid{display:none}}
 .calendar-page__empty{margin:0;padding:var(--space-3);border:1px dashed var(--border-subtle);border-radius:var(--radius-surface);color:var(--text-muted);text-align:center}
.calendar-page{container-type:inline-size}
</style>
