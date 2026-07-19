<script setup lang="ts">
import {computed, ref} from "vue"
import {RouterLink} from "vue-router"
import type {AppSurfaceConfig} from "../shellDefinitions.ts"
import type {ShellSurfaceSection, ShellSurfaceMetric} from "../shellSurfaceData.ts"
import SurfaceRow from "./SurfaceRow.vue"
import SurfaceHeader from "./SurfaceHeader.vue"
import SurfaceMetricGrid from "./SurfaceMetricGrid.vue"
import SurfaceSection from "./SurfaceSection.vue"
import AppLoadingState from "./AppLoadingState.vue"
import AppEmptyState from "./AppEmptyState.vue"

const props = defineProps<{
  config: AppSurfaceConfig
  metrics: ShellSurfaceMetric[]
  sections: ShellSurfaceSection[]
  loading: boolean
  error: string
  detailLabel?: string
  note?: string
  onRetry?: () => void
}>()

const surfaceClass = computed(() => `surface-content--${props.config.archetype}`)
type CalendarMode = "month" | "week" | "day"
const calendarMode = ref<CalendarMode>("month")
const calendarCursor = ref(new Date())
const calendarRows = computed(() => props.sections.flatMap(section => section.rows).filter(row => row.startAt))
const calendarEventsForDate = (date: Date) => calendarRows.value.filter(row => {
  if (!row.startAt) return false
  const eventDate = new Date(row.startAt)
  return eventDate.getFullYear() === date.getFullYear() && eventDate.getMonth() === date.getMonth() && eventDate.getDate() === date.getDate()
})
const startOfWeek = (date: Date) => {
  const result = new Date(date)
  result.setHours(0, 0, 0, 0)
  result.setDate(result.getDate() - result.getDay())
  return result
}
const calendarDays = computed(() => {
  const first = new Date(calendarCursor.value.getFullYear(), calendarCursor.value.getMonth(), 1)
  const start = new Date(first)
  start.setDate(1 - first.getDay())
  return Array.from({length: 42}, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return {date, events: calendarEventsForDate(date), inMonth: date.getMonth() === calendarCursor.value.getMonth()}
  })
})
const calendarWeekDays = computed(() => Array.from({length: 7}, (_, index) => {
  const date = startOfWeek(calendarCursor.value)
  date.setDate(date.getDate() + index)
  return {date, events: calendarEventsForDate(date)}
}))
const calendarTitle = computed(() => new Intl.DateTimeFormat("en-US", {month: "long", year: "numeric"}).format(calendarCursor.value))
const calendarTimezone = new Intl.DateTimeFormat().resolvedOptions().timeZone || "local time"
const moveCalendar = (amount: number) => {
  const next = new Date(calendarCursor.value)
  if (calendarMode.value === "month") next.setMonth(next.getMonth() + amount)
  else if (calendarMode.value === "week") next.setDate(next.getDate() + amount * 7)
  else next.setDate(next.getDate() + amount)
  calendarCursor.value = next
}
const formatEventTime = (value: string | null | undefined) => value ? new Intl.DateTimeFormat("en-US", {hour: "numeric", minute: "2-digit"}).format(new Date(value)) : ""
const formatDay = (date: Date) => new Intl.DateTimeFormat("en-US", {weekday: "short", month: "short", day: "numeric"}).format(date)
</script>

<template>
  <section class="surface-content" :class="surfaceClass" :data-surface-id="config.id" aria-live="polite">
    <SurfaceHeader :config="config" :detail-label="detailLabel" />

    <SurfaceMetricGrid :metrics="metrics" />

    <p v-if="note" class="surface-content__note">{{ note }}</p>

    <AppLoadingState v-if="loading" label="Loading workspace" :rows="4" />
    <div v-else-if="error" class="surface-content__status-card surface-content__status-card--error" role="alert">
      <span>{{ error }}</span><button v-if="onRetry" type="button" @click="onRetry">Retry</button>
    </div>

    <div v-else-if="!loading && !error && config.archetype === 'chat'" class="surface-content__inbox-layout">
      <aside class="surface-content__inbox-index" aria-label="Conversation list">
        <h2 class="surface-content__card-title">{{ sections[sections.length > 1 ? 1 : 0]?.title ?? "Inbox" }}</h2>
        <div class="surface-content__rows">
          <SurfaceRow v-for="row in sections[sections.length > 1 ? 1 : 0]?.rows ?? []" :key="row.id" :row="row" />
        </div>
      </aside>
      <section class="surface-content__inbox-focus" aria-label="Selected conversation">
        <h2 class="surface-content__card-title">{{ sections[0]?.title ?? "Conversation" }}</h2>
        <div class="surface-content__rows">
          <SurfaceRow v-for="row in sections[0]?.rows ?? []" :key="row.id" :row="row" />
        </div>
        <p v-if="sections.length === 0" class="surface-content__empty-state">Select a conversation.</p>
      </section>
    </div>

    <div v-else-if="!loading && !error && config.archetype === 'calendar'" class="surface-content__calendar" aria-label="Calendar workspace">
      <div class="surface-content__calendar-toolbar">
        <div><strong>{{ calendarTitle }}</strong><span>{{ calendarRows.length }} scheduled items · {{ calendarTimezone }}</span></div>
        <div class="surface-content__calendar-actions" aria-label="Calendar controls"><button type="button" @click="moveCalendar(-1)">Previous</button><button type="button" @click="calendarCursor = new Date()">Today</button><button type="button" @click="moveCalendar(1)">Next</button><button v-for="mode in ['month','week','day']" :key="mode" type="button" :aria-pressed="calendarMode === mode" :class="{'surface-content__calendar-mode--active': calendarMode === mode}" @click="calendarMode = mode as CalendarMode">{{ mode }}</button></div>
      </div>
      <div v-if="calendarMode === 'month'" class="surface-content__month-grid"><span v-for="day in ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']" :key="day" class="surface-content__weekday">{{ day }}</span><div v-for="day in calendarDays" :key="day.date.toISOString()" class="surface-content__month-day" :class="{'surface-content__month-day--outside': !day.inMonth}"><strong>{{ day.date.getDate() }}</strong><RouterLink v-for="event in day.events.slice(0, 3)" :key="event.id" :to="event.to ?? '/calendar'" class="surface-content__calendar-event"><small>{{ formatEventTime(event.startAt) }}</small>{{ event.title }}</RouterLink><span v-if="day.events.length > 3" class="surface-content__more-events">+{{ day.events.length - 3 }} more</span></div></div>
      <div v-else-if="calendarMode === 'week'" class="surface-content__week-grid"><article v-for="day in calendarWeekDays" :key="day.date.toISOString()"><h2>{{ formatDay(day.date) }}</h2><RouterLink v-for="event in day.events" :key="event.id" :to="event.to ?? '/calendar'" class="surface-content__calendar-event"><small>{{ formatEventTime(event.startAt) }}</small>{{ event.title }}<span>{{ event.badge || event.eventType }}</span></RouterLink><p v-if="day.events.length === 0">No events</p></article></div>
      <div v-else class="surface-content__day-view"><h2>{{ formatDay(calendarCursor) }}</h2><RouterLink v-for="event in calendarEventsForDate(calendarCursor)" :key="event.id" :to="event.to ?? '/calendar'" class="surface-content__calendar-event"><small>{{ formatEventTime(event.startAt) }}–{{ formatEventTime(event.endAt) }}</small><strong>{{ event.title }}</strong><span>{{ event.description }}</span></RouterLink><p v-if="calendarEventsForDate(calendarCursor).length === 0">No events scheduled for this day.</p></div>
      <p v-if="calendarRows.length === 0" class="surface-content__empty-state">No scheduled work or business events are available yet.</p>
    </div>

    <div v-else-if="!loading && !error && config.archetype === 'business'" class="surface-content__operations" aria-label="Business operations">
      <section v-for="section in sections" :key="section.title" class="surface-content__operations-group">
        <div class="surface-content__operations-heading">
          <h2 class="surface-content__card-title">{{ section.title }}</h2>
          <span>{{ section.rows.length }}</span>
        </div>
        <div class="surface-content__rows">
          <SurfaceRow v-for="row in section.rows" :key="row.id" :row="row" />
        </div>
      </section>
    </div>

    <div v-else-if="!loading && !error" class="surface-content__collection" aria-label="Workspace collection">
      <SurfaceSection v-for="section in sections" :key="section.title" :section="section" />
    </div>

    <AppEmptyState v-if="!loading && !error && sections.length === 0" title="Nothing here yet." />
  </section>
</template>

<style scoped>
.surface-content {
  display: grid;
  gap: var(--surface-content-gap);
  min-width: 0;
}

.surface-content__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 0.75rem 1rem;
  padding: 0.35rem 0.1rem;
}

.surface-content__location {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
  grid-column: 1 / -1;
}

.surface-content__location-mark {
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 50%;
  background: var(--accent);
}

.surface-content__eyebrow,
.surface-content__title,
.surface-content__detail-label,
.surface-content__metric-label,
.surface-content__metric-value,
.surface-content__card-title,
.surface-content__row-title,
.surface-content__row-meta,
.surface-content__empty-state,
.surface-content__empty-surface {
  margin: 0;
}

.surface-content__eyebrow {
  color:var(--text-muted);
  font-size: 0.76rem;
  font-weight: 650;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.surface-content__detail-label {
  color:var(--text-muted);
  font-size: 0.84rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.surface-content__title {
  min-width: 0;
  font-size: clamp(1.55rem, 2.5vw, 2.3rem);
  letter-spacing: -0.075em;
  line-height: 1;
}

.surface-content__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.45rem;
}

.surface-content__action,
.surface-content__row-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2.25rem;
  border: 1px solid var(--control-border);
  border-radius: var(--radius-control);
  padding: 0.45rem 0.78rem;
  background: var(--control-bg);
  color: var(--control-ink);
  font-size: 0.82rem;
  font-weight: 650;
  white-space: nowrap;
}

.surface-content__action--primary,
.surface-content__action--vision,
.surface-content__row-link--vision {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--canvas);
}

.surface-content__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.surface-content__metric {
  display: inline-flex;
  align-items: baseline;
  gap: 0.45rem;
  min-height: 2.45rem;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-control);
  background: var(--surface-base);
  padding: 0.45rem 0.75rem;
}

.surface-content__metric[href] {
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease, box-shadow 160ms ease;
}

.surface-content__metric[href]:hover,
.surface-content__metric[href]:focus-visible {
  border-color: var(--border-strong);
  box-shadow: none;
  transform: none;
}

.surface-content__metric-detail {
  display: none;
}

.surface-content__note {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.surface-content__metric--emphasis {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--canvas);
}

.surface-content__metric-label {
  color:var(--text-muted);
  font-size: 0.76rem;
}

.surface-content__metric--emphasis .surface-content__metric-label {
  color: color-mix(in srgb, var(--canvas) 70%, transparent);
}

.surface-content__metric-value {
  font-size: 1rem;
  letter-spacing: -0.04em;
}

.surface-content__status-card {
  border-radius: var(--radius-surface);
  background:var(--surface);
  padding: 0.7rem 0.85rem;
  color:var(--text-muted);
  font-size: 0.86rem;
}

.surface-content__status-card--error {
  background:var(--danger-muted);
  color: var(--danger);
}

.surface-content__status-card--error button {
  margin-left: 0.6rem;
  border: 0;
  background: none;
  color: inherit;
  font: inherit;
  text-decoration: underline;
  cursor: pointer;
}

.surface-content__collection {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: var(--space-2);
  border-top: 1px solid var(--border-subtle);
  border-bottom: 1px solid var(--border-subtle);
}

.surface-content__inbox-layout {
  display: grid;
  grid-template-columns: minmax(14rem, 0.8fr) minmax(0, 1.6fr);
  gap: 0.7rem;
}

.surface-content__inbox-index,
.surface-content__inbox-focus,
.surface-content__timeline-group,
.surface-content__operations-group {
  min-width: 0;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-surface);
  background: var(--surface-base);
  padding: 0.85rem;
}

.surface-content__inbox-row,
.surface-content__focus-row {
  display: grid;
  gap: 0.2rem;
  border:1px solid var(--border-subtle);
  padding: 0.7rem 0;
}

.surface-content__inbox-row:first-child,
.surface-content__focus-row:first-child {
  border-top: none;
}

.surface-content__timeline,
.surface-content__operations {
  display: grid;
  gap: 0.7rem;
}

.surface-content__timeline-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 0.65rem;
  border:1px solid var(--border-subtle);
  padding: 0.75rem 0;
}

.surface-content__timeline-row:first-child {
  border-top: none;
}

.surface-content__timeline-dot {
  width: 0.48rem;
  height: 0.48rem;
  border-radius: 50%;
  background: var(--success);
}

.surface-content__operations-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.surface-content__operations-heading > span {
  color:var(--text-muted);
  font-size: 0.76rem;
}

.surface-content--chat .surface-content__sections,
.surface-content--calendar .surface-content__sections,
.surface-content--business .surface-content__sections {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.surface-content--work .surface-content__sections {
  grid-template-columns: minmax(0, 1fr);
}

.surface-content__card {
  min-width: 0;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-surface);
  background: var(--surface-base);
  padding: 0.85rem;
}

.surface-content__card-title {
  font-size: 0.83rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.surface-content__rows {
  display: grid;
  gap: 0;
  margin-top: 0.45rem;
}

.surface-content__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 0.55rem;
  min-width: 0;
  border:1px solid var(--border-subtle);
  padding: 0.72rem 0;
}

.surface-content__row:first-child {
  border-top: none;
}

.surface-content__row-main {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
}

.surface-content__row-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.9rem;
  font-weight: 650;
  letter-spacing: -0.025em;
}

.surface-content__row-meta {
  color:var(--text-muted);
  font-size: 0.76rem;
  white-space: nowrap;
}

.surface-content__badge {
  flex: 0 0 auto;
  border-radius: var(--radius-control);
  background: var(--surface-muted);
  padding: 0.18rem 0.45rem;
  color:var(--text-muted);
  font-size: 0.68rem;
  white-space: nowrap;
}

.surface-content__row-actions {
  display: flex;
  gap: 0.35rem;
}

.surface-content__row-link {
  min-height: 1.9rem;
  padding: 0.3rem 0.58rem;
  font-size: 0.72rem;
}

.surface-content__empty-state,
.surface-content__empty-surface {
  color:var(--text-muted);
  font-size: 0.84rem;
}

.surface-content__calendar {
  display: grid;
  gap: 0.8rem;
}

.surface-content__calendar-toolbar,
.surface-content__calendar-actions {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  flex-wrap: wrap;
}

.surface-content__calendar-toolbar {
  justify-content: space-between;
}

.surface-content__calendar-toolbar > div:first-child {
  display: grid;
  gap: 0.2rem;
}

.surface-content__calendar-toolbar span,
.surface-content__calendar-toolbar small {
  color:var(--text-muted);
  font-size: 0.78rem;
}

.surface-content__calendar-actions button {
  border: 1px solid var(--control-border);
  border-radius: var(--radius-control);
  background: var(--control-bg);
  color: var(--control-ink);
  padding: 0.42rem 0.65rem;
  font: inherit;
  font-size: 0.72rem;
  cursor: pointer;
}

.surface-content__calendar-actions .surface-content__calendar-mode--active {
  background: var(--accent);
  color: var(--canvas);
}

.surface-content__month-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  overflow: hidden;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-surface);
  background: var(--surface-base);
}

.surface-content__weekday {
  padding: 0.55rem;
  color:var(--text-muted);
  font-size: 0.7rem;
  font-weight: 650;
  text-align: right;
}

.surface-content__month-day {
  display: grid;
  align-content: start;
  gap: 0.25rem;
  min-height: 7rem;
  border: 1px solid var(--border-subtle);
  padding: 0.45rem;
}

.surface-content__month-day:nth-child(7n) {
  border-right: 0;
}

.surface-content__month-day--outside {
  background: var(--bg-raised);
  color: var(--text-soft);
}

.surface-content__calendar-event {
  display: grid;
  gap: 0.12rem;
  overflow: hidden;
  border-radius: var(--radius-control);
  background: var(--accent-muted);
  padding: 0.3rem 0.4rem;
  color: var(--text);
  font-size: 0.7rem;
  line-height: 1.2;
  text-decoration: none;
}

.surface-content__calendar-event small,
.surface-content__calendar-event span {
  color:var(--text-muted);
  font-size: 0.65rem;
}

.surface-content__more-events {
  color:var(--text-muted);
  font-size: 0.68rem;
}

.surface-content__week-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 0.45rem;
}

.surface-content__week-grid article,
.surface-content__day-view {
  display: grid;
  align-content: start;
  gap: 0.45rem;
  min-height: 12rem;
  border:1px solid var(--border-subtle);
  border-radius: var(--radius-surface);
  background:var(--surface);
  padding: 0.65rem;
}

.surface-content__week-grid h2,
.surface-content__day-view h2 {
  margin: 0;
  font-size: 0.8rem;
}

.surface-content__week-grid p,
.surface-content__day-view p {
  color:var(--text-muted);
  font-size: 0.75rem;
}

@media (max-width: 760px) {
  .surface-content__header {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .surface-content__actions {
    justify-content: flex-start;
  }

  .surface-content__sections,
  .surface-content--chat .surface-content__sections,
  .surface-content--calendar .surface-content__sections,
  .surface-content--business .surface-content__sections {
    grid-template-columns: minmax(0, 1fr);
  }

  .surface-content__inbox-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .surface-content__inbox-focus {
    order: -1;
  }

  .surface-content__timeline-row {
    grid-template-columns: auto minmax(0, 1fr) auto;
  }

  .surface-content__timeline-row .surface-content__row-meta {
    grid-column: 2;
  }

  .surface-content__timeline-row .surface-content__row-link {
    grid-column: 3;
    grid-row: 1 / span 2;
  }

  .surface-content__row {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .surface-content__row-meta {
    grid-column: 1;
  }

  .surface-content__row-actions {
    grid-column: 2;
    grid-row: 1 / span 2;
  }

  .surface-content__month-day {
    min-height: 5rem;
  }

  .surface-content__week-grid {
    grid-template-columns: 1fr;
  }

  .surface-content__calendar-toolbar {
    align-items: start;
    flex-direction: column;
  }

  .surface-content__calendar-actions {
    width: 100%;
    overflow-x: auto;
    justify-content: flex-start;
    padding-bottom: .2rem;
  }

  .surface-content__calendar-event {
    min-height: 2.5rem;
  }
}
</style>
<style scoped>
/* Shared graphite surface contract: collection primitives stay dense and token-owned. */
.surface-content__action,
.surface-content__row-link,
.surface-content__metric,
.surface-content__filter,
.surface-content__chip,
.surface-content__toolbar button {
  border-radius: var(--radius-control);
  background: var(--control-bg);
  color: var(--control-ink);
}
.surface-content__action--primary,
.surface-content__action--vision,
.surface-content__row-link--vision,
.surface-content__metric--emphasis {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--canvas);
}
.surface-content__metric,
.surface-content__status-card,
.surface-content__inbox-index,
.surface-content__inbox-focus,
.surface-content__timeline-group,
.surface-content__operations-group,
.surface-content__week-grid article,
.surface-content__day-view,
.surface-content__calendar-event {
  border-radius: var(--radius-surface);
  background: var(--surface-base);
  box-shadow: none;
}
.surface-content__metric { min-height: var(--control-height-default); padding: var(--space-1) var(--space-2); }
.surface-content__inbox-index,
.surface-content__inbox-focus,
.surface-content__timeline-group,
.surface-content__operations-group { padding: var(--space-3); }
.surface-content__status-card { padding: var(--space-2) var(--space-3); }
.surface-content__metric--emphasis .surface-content__metric-label { color: color-mix(in srgb, var(--canvas) 70%, transparent); }
.surface-content__metric[href]:hover,
.surface-content__metric[href]:focus-visible { box-shadow: none; transform: none; border-color: var(--border-strong); }
.surface-content__badge { border-radius: var(--radius-control); background: var(--surface-muted); color: var(--text-muted); }
.surface-content__calendar-actions button { border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink); }
.surface-content__calendar-actions .surface-content__calendar-mode--active { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
.surface-content__month-grid { border-radius: var(--radius-surface); background: var(--surface-base); }
.surface-content__month-day { background: var(--surface-base); }
.surface-content__month-day--outside { background: var(--surface-muted); color: var(--text-soft); }
.surface-content__calendar-event { border-radius: var(--radius-control); background: var(--accent-muted); }
</style>
