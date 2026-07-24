<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {CalendarEvent} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import AppButton from "../components/AppButton.vue"
import {formatInTimezone} from "../../../services/formatters.ts"

type CalendarMode = "agenda" | "day" | "week" | "month"
const mode = ref<CalendarMode>("agenda")
const route = useRoute()
const cursor = ref(new Date())
const events = ref<CalendarEvent[]>([])
const availableSources = ref<string[]>([])
const timezone = ref(Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC")
const selectedSources = ref<string[]>([])
const sourceStorageKey = "calendar-visible-sources"
const loading = ref(true)
const error = ref("")
const sourceLabels: Record<string, string> = {BUSINESS: "Business", QUEST: "Work", RIDE: "Rides", THING: "Things", CIRCLE: "Circles", CHAT: "Chat", PERSONAL: "Personal"}
const sourceLabel = (source: string) => sourceLabels[source] ?? source
const range = computed(() => {
  const from = new Date(cursor.value)
  const to = new Date(cursor.value)
  if (mode.value === "day") to.setDate(to.getDate() + 1)
  else if (mode.value === "week") { from.setDate(from.getDate() - from.getDay()); to.setDate(from.getDate() + 7) }
  else if (mode.value === "month") { from.setDate(1); to.setMonth(from.getMonth() + 1); to.setDate(0); to.setDate(to.getDate() + 1) }
  else { from.setDate(from.getDate() - 30); to.setDate(to.getDate() + 90) }
  return {from: from.toISOString(), to: to.toISOString()}
})
const title = computed(() => mode.value === "agenda" ? "Agenda" : cursor.value.toLocaleDateString(undefined, {month: "long", year: "numeric"}))
const visibleEvents = computed(() => events.value.filter(event => selectedSources.value.includes(event.source)))
const groupedEvents = computed(() => visibleEvents.value.reduce<Record<string, CalendarEvent[]>>((groups, event) => { const key = new Date(event.startsAt).toLocaleDateString(); (groups[key] ||= []).push(event); return groups }, {}))
const load = async () => { loading.value = true; error.value = ""; try { const businessId = typeof route.query.businessId === "string" ? Number(route.query.businessId) : undefined; const projection = await userShellApi.getCalendarProjection({...range.value, businessId, sources: selectedSources.value.length ? selectedSources.value : undefined}); events.value = projection.events; availableSources.value = projection.availableSources; timezone.value = projection.timezone || timezone.value; if (!selectedSources.value.length) { const saved = typeof window !== "undefined" ? JSON.parse(window.localStorage.getItem(sourceStorageKey) || "[]") as string[] : []; selectedSources.value = saved.filter(source => projection.availableSources.includes(source)); if (!selectedSources.value.length) selectedSources.value = [...projection.availableSources] } } catch { error.value = "Could not load your unified calendar." } finally { loading.value = false } }
const move = (amount: number) => { const next = new Date(cursor.value); if (mode.value === "day") next.setDate(next.getDate() + amount); else if (mode.value === "week") next.setDate(next.getDate() + amount * 7); else next.setMonth(next.getMonth() + amount); cursor.value = next }
const today = () => { cursor.value = new Date() }
watch([mode, cursor, selectedSources], () => { if (typeof window !== "undefined" && selectedSources.value.length) window.localStorage.setItem(sourceStorageKey, JSON.stringify(selectedSources.value)); void load() }, {deep: true})
onMounted(() => void load())
</script>

<template>
  <section class="calendar-page" aria-label="Unified calendar" data-calendar-model="all-module-events" data-conflict-policy="source-authoritative">
    <header class="calendar-page__header"><div><p class="calendar-page__eyebrow">Calendar</p><h1>{{ title }}</h1><p>One time surface for work, business, rides, things, circles, chat, and personal commitments.</p></div><div class="calendar-page__navigation" aria-label="Calendar navigation"><AppButton type="button" tone="quiet" aria-label="Previous period" @click="move(-1)">‹</AppButton><AppButton type="button" tone="secondary" @click="today">Today</AppButton><AppButton type="button" tone="quiet" aria-label="Next period" @click="move(1)">›</AppButton></div></header>
    <CollectionToolbar title="Schedule" :count="visibleEvents.length" :busy="loading" :filter-summary="`Times shown in ${timezone}`" label="Calendar filters"><template #filters><div class="calendar-page__modes" role="group" aria-label="Calendar view"><button v-for="value in ['agenda','day','week','month']" :key="value" type="button" :aria-pressed="mode === value" :class="{active: mode === value}" @click="mode = value as CalendarMode">{{ value }}</button></div><details class="calendar-page__filters"><summary>Show</summary><label v-for="source in availableSources" :key="source"><input v-model="selectedSources" type="checkbox" :value="source"> {{ sourceLabel(source) }}</label></details></template></CollectionToolbar>
    <AppStatus v-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="loading" message="Loading your unified calendar." busy /><AppStatus v-else-if="visibleEvents.length === 0" message="No events match the selected sources and time range." />
    <div v-else class="calendar-page__agenda"><section v-for="(items, date) in groupedEvents" :key="date" class="calendar-page__day"><h2>{{ date }}</h2><RouterLink v-for="event in items" :key="event.eventKey" class="calendar-page__event" :to="event.navigationPath"><span class="calendar-page__event-time">{{ formatInTimezone(event.startsAt, timezone, "Unknown time") }}</span><span><strong>{{ event.title }}</strong><small>{{ sourceLabel(event.source) }} · {{ event.businessName || event.status }} · {{ timezone }}</small></span></RouterLink></section></div>
  </section>
</template>

<style scoped>
.calendar-page{display:grid;gap:var(--space-4);min-width:0}.calendar-page__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-4)}.calendar-page__header h1,.calendar-page__header p{margin:0}.calendar-page__header h1{font-size:clamp(1.8rem,3vw,2.6rem);letter-spacing:-.04em}.calendar-page__header>div>p:last-child{margin-top:var(--space-2);color:var(--text-muted)}.calendar-page__eyebrow{margin-bottom:var(--space-1)!important;color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);text-transform:uppercase;letter-spacing:.06em}.calendar-page__navigation,.calendar-page__modes{display:flex;align-items:center;gap:var(--space-1)}.calendar-page__modes button{min-height:var(--control-height-default);padding:var(--space-1) var(--space-2);border:1px solid transparent;border-radius:var(--radius-control);background:transparent;color:var(--text-muted);font:inherit;text-transform:capitalize}.calendar-page__modes button.active{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.calendar-page__filters{position:relative;display:grid;gap:var(--space-1);padding:var(--space-1)}.calendar-page__filters summary{cursor:pointer;color:var(--text-muted);font-weight:var(--text-weight-semibold)}.calendar-page__filters label{display:block;padding:var(--space-1);white-space:nowrap}.calendar-page__agenda{display:grid;gap:var(--space-3)}.calendar-page__day{display:grid;gap:var(--space-1);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.calendar-page__day h2{margin:0 0 var(--space-2);font-size:var(--text-size-title)}.calendar-page__event{display:grid;grid-template-columns:5rem 1fr;gap:var(--space-3);padding:var(--space-2);border-radius:var(--radius-control);color:var(--text);text-decoration:none}.calendar-page__event:hover{background:var(--surface-hover)}.calendar-page__event-time{color:var(--text-muted);font-variant-numeric:tabular-nums}.calendar-page__event span:last-child{display:grid;gap:var(--space-1)}.calendar-page__event small{color:var(--text-muted)}@media(max-width:700px){.calendar-page__header{display:grid}.calendar-page__navigation{justify-content:flex-start}.calendar-page__event{grid-template-columns:4rem 1fr}}
</style>
