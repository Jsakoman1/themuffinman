<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRouter} from "vue-router"
import type {BusinessOwnerCalendarItemDTO, BusinessOwnerCalendarProjectionDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import {formatDateTime} from "../../../services/formatters.ts"

type Mode = "day" | "week" | "month"
const props = defineProps<{businessId: number}>()
const router = useRouter()
const mode = ref<Mode>("week")
const cursor = ref(new Date())
const projection = ref<BusinessOwnerCalendarProjectionDTO | null>(null)
const selectedId = ref<number | null>(null)
const loading = ref(true)
const error = ref("")
const saving = ref<number | null>(null)

const startOfDay = (date: Date) => { const value = new Date(date); value.setHours(0, 0, 0, 0); return value }
const range = computed(() => { const from = startOfDay(cursor.value); if (mode.value === "week") from.setDate(from.getDate() - from.getDay()); if (mode.value === "month") { from.setDate(1); from.setDate(from.getDate() - from.getDay()) }; const to = new Date(from); to.setDate(to.getDate() + (mode.value === "day" ? 1 : mode.value === "week" ? 7 : 42)); return {from: from.toISOString(), to: to.toISOString()} })
const items = computed(() => projection.value?.days.flatMap(day => day.items) ?? [])
const agendaDays = computed(() => projection.value?.days.filter(day => day.items.length > 0) ?? [])
const selected = computed(() => items.value.find(item => item.bookingId === selectedId.value) ?? null)
const title = computed(() => mode.value === "day" ? cursor.value.toLocaleDateString(undefined, {weekday: "long", month: "long", day: "numeric"}) : mode.value === "week" ? `Week of ${range.value.from.slice(0, 10)}` : cursor.value.toLocaleDateString(undefined, {month: "long", year: "numeric"}))
const load = async () => { loading.value = true; error.value = ""; try { projection.value = await userShellApi.getBusinessOwnerCalendar(props.businessId, range.value); selectedId.value = null } catch { error.value = "Could not load this business calendar." } finally { loading.value = false } }
const move = (amount: number) => { const next = new Date(cursor.value); if (mode.value === "month") next.setMonth(next.getMonth() + amount); else next.setDate(next.getDate() + (mode.value === "day" ? amount : amount * 7)); cursor.value = next }
const action = async (item: BusinessOwnerCalendarItemDTO, value: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show") => { saving.value = item.bookingId; try { await userShellApi.executeBusinessBookingAction(item.bookingId, value); await load() } catch { error.value = "Could not update this booking." } finally { saving.value = null } }
const openBooking = () => { if (selected.value) void router.push({path: `/business/bookings/${selected.value.bookingId}`, query: {businessId: String(props.businessId)}}) }
watch([() => props.businessId, mode, cursor], () => void load())
onMounted(() => void load())
</script>

<template>
  <section class="owner-calendar" aria-label="Business calendar" data-owner-calendar="selected-business">
    <header>
      <div><p>Business calendar</p><h2>{{ title }}</h2><small v-if="projection">{{ projection.totalBookings }} bookings · {{ projection.timezone }}</small></div>
      <div class="owner-calendar__controls"><AppButton type="button" tone="quiet" @click="move(-1)">‹</AppButton><AppButton type="button" tone="secondary" @click="cursor = new Date()">Today</AppButton><AppButton type="button" tone="quiet" @click="move(1)">›</AppButton><div class="owner-calendar__modes"><button v-for="value in ['day','week','month']" :key="value" type="button" :class="{active: mode === value}" @click="mode = value as Mode">{{ value }}</button></div></div>
    </header>
    <AppStatus v-if="loading" message="Loading business calendar." busy />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <div v-else class="owner-calendar__workspace">
      <div class="owner-calendar__days" :class="`owner-calendar__days--${mode}`">
        <template v-if="mode === 'month'"><strong v-for="day in ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']" :key="day" class="owner-calendar__weekday">{{ day }}</strong></template>
        <article v-for="day in projection?.days" :key="day.date" class="owner-calendar__day"><h3>{{ new Date(`${day.date}T00:00:00`).toLocaleDateString(undefined, {weekday: mode === 'month' ? undefined : 'short', day: 'numeric', month: mode === 'month' ? undefined : 'short'}) }}</h3><button v-for="item in day.items" :key="item.bookingId" type="button" class="owner-calendar__event" :class="{selected: selectedId === item.bookingId}" @click="selectedId = item.bookingId"><strong>{{ item.businessOfferingTitle }}</strong><span>{{ formatDateTime(item.startsAt) }} · {{ item.customerUsername }}</span><small>{{ item.statusLabel }}</small></button><p v-if="day.items.length === 0 && mode !== 'month'">No bookings</p></article>
      </div>
      <div class="owner-calendar__agenda" aria-label="Month agenda"><article v-for="day in agendaDays" :key="day.date" class="owner-calendar__agenda-day"><h3>{{ new Date(`${day.date}T00:00:00`).toLocaleDateString(undefined, {weekday: 'long', day: 'numeric', month: 'long'}) }}</h3><button v-for="item in day.items" :key="item.bookingId" type="button" class="owner-calendar__event" :class="{selected: selectedId === item.bookingId}" @click="selectedId = item.bookingId"><strong>{{ item.businessOfferingTitle }}</strong><span>{{ formatDateTime(item.startsAt) }} · {{ item.customerUsername }}</span><small>{{ item.statusLabel }}</small></button></article><p v-if="agendaDays.length === 0" class="owner-calendar__agenda-empty">No bookings this month.</p></div>
      <section v-if="selected" class="owner-calendar__quick-look" data-archetype="calendar-quick-look" aria-label="Booking quick look"><header><div><p>Booking</p><h3>{{ selected.businessOfferingTitle }}</h3></div><AppButton type="button" tone="quiet" aria-label="Close booking quick look" @click="selectedId = null">×</AppButton></header><p>{{ selected.customerUsername }} · {{ formatDateTime(selected.startsAt) }}</p><p>{{ selected.statusLabel }} · Ends {{ formatDateTime(selected.endsAt) }}</p><div class="owner-calendar__quick-look-actions"><AppButton v-if="selected.allowedActions.includes('CONFIRM')" type="button" tone="primary" :loading="saving === selected.bookingId" @click="action(selected, 'confirm')">Confirm</AppButton><AppButton v-if="selected.allowedActions.includes('COMPLETE')" type="button" tone="primary" :loading="saving === selected.bookingId" @click="action(selected, 'complete')">Complete</AppButton><AppButton v-if="selected.allowedActions.includes('REJECT')" type="button" tone="danger" :loading="saving === selected.bookingId" @click="action(selected, 'reject')">Reject</AppButton><AppButton type="button" tone="secondary" @click="openBooking">Open booking</AppButton></div></section>
    </div>
  </section>
</template>

<style scoped>
.owner-calendar{display:grid;gap:var(--space-3)}.owner-calendar>header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.owner-calendar h2,.owner-calendar p{margin:0}.owner-calendar header p,.owner-calendar small,.owner-calendar__day>p{color:var(--text-muted)}.owner-calendar__controls,.owner-calendar__modes,.owner-calendar__quick-look-actions{display:flex;gap:var(--space-1);align-items:center;flex-wrap:wrap}.owner-calendar__modes{padding:2px;border:1px solid var(--border-subtle);border-radius:var(--radius-control)}.owner-calendar__modes button{border:0;border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:transparent;color:var(--text-muted);font:inherit;cursor:pointer;text-transform:capitalize}.owner-calendar__modes button.active{background:var(--accent);color:var(--canvas)}.owner-calendar__workspace{display:grid;grid-template-columns:minmax(0,1fr);overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.owner-calendar__days{display:grid;gap:1px;background:var(--border-subtle)}.owner-calendar__days--day{grid-template-columns:1fr}.owner-calendar__days--week,.owner-calendar__days--month{grid-template-columns:repeat(7,minmax(0,1fr))}.owner-calendar__weekday{padding:var(--space-1) var(--space-2);background:var(--surface-raised);color:var(--text-soft);font-size:var(--text-size-label);text-align:center}.owner-calendar__day{display:grid;align-content:start;gap:var(--space-1);min-height:12rem;padding:var(--space-2);background:var(--surface-base)}.owner-calendar__days--month .owner-calendar__day{min-height:8rem}.owner-calendar__day h3,.owner-calendar__agenda-day h3{margin:0;font-size:var(--text-size-meta)}.owner-calendar__event{display:grid;gap:2px;border:0;border-left:2px solid var(--accent);border-radius:var(--radius-control);padding:var(--space-1);background:var(--accent-muted);color:var(--text);font:inherit;text-align:left;cursor:pointer}.owner-calendar__event.selected{box-shadow:inset 0 0 0 1px var(--accent)}.owner-calendar__event span,.owner-calendar__event small{font-size:var(--text-size-label)}.owner-calendar__agenda{display:none}.owner-calendar__agenda-day{display:grid;gap:var(--space-2);padding:var(--space-3);border-bottom:1px solid var(--border-subtle)}.owner-calendar__agenda-empty{padding:var(--space-3);color:var(--text-muted)}.owner-calendar__quick-look{display:grid;gap:var(--space-2);margin:var(--space-3);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.owner-calendar__quick-look header{display:flex;justify-content:space-between;gap:var(--space-2)}.owner-calendar__quick-look h3{margin:0}@media(max-width:700px){.owner-calendar>header{align-items:start;flex-direction:column}.owner-calendar__days{grid-template-columns:1fr}.owner-calendar__day{min-height:auto}.owner-calendar__weekday{display:none}.owner-calendar__days--month{display:none}.owner-calendar__days--month+.owner-calendar__agenda{display:grid}}
</style>
