<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import type {BusinessBookingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import {formatDateTime} from "../../../services/formatters.ts"
import {RouterLink, useRoute, useRouter} from "vue-router"
import ReviewPane from "../components/ReviewPane.vue"
import ClientActionList from "../components/ClientActionList.vue"
const route = useRoute()
const router = useRouter()
const props = defineProps<{businessId: number}>()

const bookings = ref<BusinessBookingResponseDTO[]>([])
const isLoading = ref(true)
const isActing = ref<number | null>(null)
const error = ref("")
const feedback = ref("")
const rescheduling = ref<number | null>(null)
const rescheduleStart = ref("")
const rescheduleEnd = ref("")
const selectedBookingId = ref<number | null>(typeof route.query.review === "string" ? Number(route.query.review) : null)
const selectedBooking = computed(() => bookings.value.find((booking) => booking.id === selectedBookingId.value) ?? null)
const now = () => Date.now()
const bookingGroups = computed(() => {
  const pending = bookings.value.filter(booking => booking.status === "PENDING_CONFIRMATION")
  const upcoming = bookings.value.filter(booking => booking.status !== "PENDING_CONFIRMATION" && new Date(booking.startsAt).getTime() >= now())
  const history = bookings.value.filter(booking => booking.status !== "PENDING_CONFIRMATION" && new Date(booking.startsAt).getTime() < now())
  return [{id: "pending", title: "Needs attention", items: pending}, {id: "upcoming", title: "Upcoming", items: upcoming}, {id: "history", title: "Past and cancelled", items: history}].filter(group => group.items.length)
})

const formatDate = (value: string) => formatDateTime(value, "Unknown time")
const actionOutcome = (booking: BusinessBookingResponseDTO) => booking.actions.find(action => action.enabled)?.outcome ?? "This booking has no action available right now."

const load = async () => {
  isLoading.value = true
  error.value = ""
  try { bookings.value = (await userShellApi.getBusinessOwnerBookings(props.businessId)).items }
  catch { error.value = "Could not load bookings." }
  finally { isLoading.value = false }
}

const execute = async (booking: BusinessBookingResponseDTO, action: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show") => {
  if ((action === "cancel" || action === "reject") && !await confirmAction(`${action === "cancel" ? "Cancel" : "Reject"} this booking? The customer will see that it is no longer going ahead.`, `${action === "cancel" ? "Cancel" : "Reject"} booking`)) return
  isActing.value = booking.id
  error.value = ""
  feedback.value = ""
  try { await userShellApi.executeBusinessBookingAction(booking.id, action); feedback.value = "Booking updated."; await load() }
  catch { error.value = "Could not update this booking." }
  finally { isActing.value = null }
}
const beginReschedule = (booking: BusinessBookingResponseDTO) => { rescheduling.value = booking.id; rescheduleStart.value = booking.startsAt.slice(0, 16); rescheduleEnd.value = booking.endsAt.slice(0, 16) }
const executeClientAction = (booking: BusinessBookingResponseDTO, actionId: string) => {
  if (actionId === "RESCHEDULE") return beginReschedule(booking)
  const command = ({CONFIRM: "confirm", REJECT: "reject", CANCEL: "cancel", CANCEL_AS_OWNER: "cancel", COMPLETE: "complete", MARK_NO_SHOW: "mark-no-show"} as const)[actionId as "CONFIRM" | "REJECT" | "CANCEL" | "CANCEL_AS_OWNER" | "COMPLETE" | "MARK_NO_SHOW"]
  if (command) void execute(booking, command)
}
const reschedule = async (booking: BusinessBookingResponseDTO) => { isActing.value = booking.id; error.value = ""; try { await userShellApi.rescheduleBusinessBookingAsOwner(booking.id, new Date(rescheduleStart.value).toISOString(), new Date(rescheduleEnd.value).toISOString()); feedback.value = "Booking rescheduled."; rescheduling.value = null; await load() } catch { error.value = "Could not reschedule this booking. Check the selected time." } finally { isActing.value = null } }
const selectBooking = (bookingId: number | null) => { selectedBookingId.value = bookingId; void router.replace({query: {...route.query, review: bookingId ? String(bookingId) : undefined}}) }

watch(() => props.businessId, () => void load())
onMounted(() => void load())
</script>

<template>
  <section class="bookings-surface" data-owner-tab="bookings" data-calendar-scope="active-business" aria-label="Business bookings">
    <CollectionToolbar title="Owner bookings" :count="bookings.length" :busy="isLoading"><template #actions><RouterLink :to="{path: '/business/calendar', query: {businessId: String(props.businessId)}}">Open calendar</RouterLink></template></CollectionToolbar>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading bookings." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="bookings.length === 0" message="No bookings yet." />
    <div v-else class="bookings-surface__workspace">
      <div class="bookings-surface__list">
      <template v-for="group in bookingGroups" :key="group.id"><p class="bookings-surface__group-title">{{ group.title }} · {{ group.items.length }}</p><SurfaceRow v-for="booking in group.items" :key="booking.id" :row="{id: `booking-${booking.id}`, title: booking.businessOfferingTitle, description: `${booking.customerUsername} · ${formatDate(booking.startsAt)}`, meta: booking.blockingReason || booking.statusLabel, badge: booking.statusLabel}" :selected="selectedBookingId === booking.id" @click="selectBooking(booking.id)">
        <template #actions><div class="bookings-surface__actions" aria-label="Booking actions">
          <ClientActionList :actions="booking.actions" :busy="isActing === booking.id" @execute="executeClientAction(booking, $event)" />
          <AppDialog :open="rescheduling === booking.id" title="Reschedule booking" layout="workspace" @close="rescheduling = null"><form class="bookings-surface__reschedule" @submit.prevent="reschedule(booking)"><AppFormField label="Start" required><input v-model="rescheduleStart" type="datetime-local" required></AppFormField><AppFormField label="End" required><input v-model="rescheduleEnd" type="datetime-local" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="rescheduling = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isActing === booking.id">Save changes</AppButton></template></AppFormFooter></form><template #utility><p>The server checks the requested period against availability and existing booking rules before accepting the reschedule.</p></template></AppDialog></div></template>
      </SurfaceRow></template>
      </div>
      <ReviewPane :title="selectedBooking?.businessOfferingTitle ?? 'Booking'" eyebrow="Booking review" :open="selectedBooking !== null" @close="selectBooking(null)">
        <p v-if="selectedBooking">
          {{ selectedBooking.customerUsername }} · {{ formatDate(selectedBooking.startsAt) }}
        </p>
        <dl v-if="selectedBooking" class="bookings-surface__review-details">
          <div><dt>Status</dt><dd>{{ selectedBooking.statusLabel }}</dd></div>
          <div><dt>Duration</dt><dd>{{ selectedBooking.durationSnapshotMinutes }} min</dd></div>
          <div><dt>Timezone</dt><dd>{{ selectedBooking.timezone }}</dd></div>
        </dl>
        <p v-if="selectedBooking?.blockingReason">{{ selectedBooking.blockingReason }}</p>
        <p v-if="selectedBooking?.customerNote"><strong>Customer note:</strong> {{ selectedBooking.customerNote }}</p>
        <p v-if="selectedBooking"><strong>What happens next:</strong> {{ actionOutcome(selectedBooking) }}</p>
      </ReviewPane>
    </div>
  </section>
</template>

<style scoped>
.bookings-surface{display:grid;gap:var(--space-3);max-width:none}.bookings-surface__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.bookings-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.bookings-surface__count,.bookings-surface__details span,.bookings-surface__details small{color:var(--text-muted);font-size:var(--text-size-meta)}.bookings-surface__feedback{margin:0;color:var(--success)}.bookings-surface__list{display:grid;gap:0;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.bookings-surface__row{display:flex;justify-content:space-between;gap:var(--space-3);align-items:center;padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);background:var(--surface-base)}.bookings-surface__details{display:grid;gap:var(--space-1);min-width:0}.bookings-surface__details span,.bookings-surface__details small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.bookings-surface__reschedule{display:flex;flex-wrap:wrap;gap:var(--space-2);align-items:end;margin-top:var(--space-1);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.bookings-surface__reschedule label{display:grid;gap:var(--space-1);font-size:var(--text-size-meta)}.bookings-surface__reschedule input{min-height:var(--control-height-default);padding:var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink);font:inherit}.bookings-surface__actions{display:flex;gap:var(--space-1);flex-wrap:wrap;justify-content:end}.bookings-surface__actions button,.bookings-surface__status button{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.bookings-surface__actions button:hover{border-color:var(--control-border-active);background:var(--control-bg-hover)}.bookings-surface__actions button:disabled{opacity:.5;cursor:wait}.bookings-surface__danger{color:var(--danger)}.bookings-surface__status{padding:var(--space-3) 0;color:var(--text-muted)}.bookings-surface__status--error{color:var(--danger)}.bookings-surface__status button{margin-left:var(--space-2);color:inherit;text-decoration:underline;border:0;background:transparent}@media(max-width:620px){.bookings-surface__row{align-items:start;flex-direction:column}.bookings-surface__details span,.bookings-surface__details small{white-space:normal}.bookings-surface__actions{justify-content:start}}
/* Workspace visual remediation keeps booking authority unchanged. */
.bookings-surface{gap:var(--space-3);max-width:none}.bookings-surface button,.bookings-surface input,.bookings-surface select{border-radius:var(--radius-control)}
.bookings-surface__workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.bookings-surface__list { gap: 0; overflow: hidden; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.bookings-surface__group-title{margin:0;padding:var(--space-2) var(--space-3);border-bottom:1px solid var(--border-subtle);background:var(--surface-raised);color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}
.bookings-surface__review-details{display:grid;gap:var(--space-2);margin:0}.bookings-surface__review-details div{display:flex;justify-content:space-between;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.bookings-surface__review-details dt{color:var(--text-soft);font-size:var(--text-size-meta)}.bookings-surface__review-details dd{margin:0;color:var(--text);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}
.bookings-surface__actions button { background: var(--control-bg); color: var(--control-ink); }
.bookings-surface__actions button:hover { border-color: var(--border-strong); background: var(--surface-hover); }
.bookings-surface__actions .bookings-surface__danger:hover { border-color: var(--danger); background: var(--danger-muted); color: var(--danger); }
.bookings-surface__reschedule input { border-color: var(--control-border); background: var(--control-bg); color: var(--control-ink); }
.bookings-surface .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.bookings-surface .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.bookings-surface .app-button--danger { color:var(--danger); }
.bookings-surface__row { padding: var(--space-2) var(--space-3); background: var(--surface-base); }
.bookings-surface__reschedule { padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.bookings-surface__reschedule input { min-height: var(--control-height-default); padding: var(--space-2); }
@media(max-width:860px){.bookings-surface__workspace{grid-template-columns:1fr}}
</style>
