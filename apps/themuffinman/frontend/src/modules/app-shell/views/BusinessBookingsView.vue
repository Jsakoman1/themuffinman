<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
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

const bookings = ref<BusinessBookingResponseDTO[]>([])
// Owner booking controls are rendered only from each booking's allowedActions.
const isLoading = ref(true)
const isActing = ref<number | null>(null)
const error = ref("")
const feedback = ref("")
const rescheduling = ref<number | null>(null)
const rescheduleStart = ref("")
const rescheduleEnd = ref("")
const selectedBookingId = ref<number | null>(null)
const selectedBooking = computed(() => bookings.value.find((booking) => booking.id === selectedBookingId.value) ?? null)

const formatDate = (value: string) => new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value))

const load = async () => {
  isLoading.value = true
  error.value = ""
  try { bookings.value = (await userShellApi.getBusinessOwnerBookings()).items }
  catch { error.value = "Could not load bookings." }
  finally { isLoading.value = false }
}

const execute = async (booking: BusinessBookingResponseDTO, action: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show") => {
  if ((action === "cancel" || action === "reject") && !await confirmAction(`${action === "cancel" ? "Cancel" : "Reject"} this booking?`, `${action === "cancel" ? "Cancel" : "Reject"} booking`)) return
  isActing.value = booking.id
  error.value = ""
  feedback.value = ""
  try { await userShellApi.executeBusinessBookingAction(booking.id, action); feedback.value = "Booking updated."; await load() }
  catch { error.value = "Could not update this booking." }
  finally { isActing.value = null }
}
const beginReschedule = (booking: BusinessBookingResponseDTO) => { rescheduling.value = booking.id; rescheduleStart.value = booking.startsAt.slice(0, 16); rescheduleEnd.value = booking.endsAt.slice(0, 16) }
const reschedule = async (booking: BusinessBookingResponseDTO) => { isActing.value = booking.id; error.value = ""; try { await userShellApi.rescheduleBusinessBookingAsOwner(booking.id, new Date(rescheduleStart.value).toISOString(), new Date(rescheduleEnd.value).toISOString()); feedback.value = "Booking rescheduled."; rescheduling.value = null; await load() } catch { error.value = "Could not reschedule this booking. Check the selected time." } finally { isActing.value = null } }

onMounted(() => void load())
</script>

<template>
  <section class="bookings-surface">
    <header class="bookings-surface__header"><div><p class="bookings-surface__eyebrow">Business / Bookings</p><h1>Bookings</h1></div></header>
    <CollectionToolbar title="Owner bookings" :count="bookings.length" :busy="isLoading" />
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading." /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="bookings.length === 0" message="No bookings yet." />
    <div v-else class="bookings-surface__workspace">
      <div class="bookings-surface__list">
      <SurfaceRow v-for="booking in bookings" :key="booking.id" :row="{id: `booking-${booking.id}`, title: booking.businessOfferingTitle, description: `${booking.customerUsername} · ${formatDate(booking.startsAt)}`, meta: booking.blockingReason || booking.statusLabel, badge: booking.statusLabel}" :selected="selectedBookingId === booking.id" @click="selectedBookingId = booking.id">
        <template #actions><div class="bookings-surface__actions" aria-label="Booking actions">
          <AppButton v-if="booking.allowedActions.includes('CONFIRM')" type="button" tone="primary" :loading="isActing === booking.id" @click="execute(booking, 'confirm')">Confirm</AppButton>
          <AppButton v-if="booking.allowedActions.includes('REJECT')" type="button" tone="danger" :loading="isActing === booking.id" @click="execute(booking, 'reject')">Reject</AppButton>
          <AppButton v-if="booking.allowedActions.includes('COMPLETE')" type="button" tone="primary" :loading="isActing === booking.id" @click="execute(booking, 'complete')">Complete</AppButton>
          <AppButton v-if="booking.allowedActions.includes('MARK_NO_SHOW')" type="button" tone="danger" :loading="isActing === booking.id" @click="execute(booking, 'mark-no-show')">No-show</AppButton>
          <AppButton v-if="booking.allowedActions.includes('RESCHEDULE')" type="button" tone="secondary" :disabled="isActing === booking.id" @click="beginReschedule(booking)">Reschedule</AppButton>
          <AppButton v-if="booking.allowedActions.includes('CANCEL') || booking.allowedActions.includes('CANCEL_AS_OWNER')" type="button" tone="danger" :loading="isActing === booking.id" @click="execute(booking, 'cancel')">Cancel</AppButton>
          <AppDialog :open="rescheduling === booking.id" title="Reschedule booking" layout="workspace" @close="rescheduling = null"><form class="bookings-surface__reschedule" @submit.prevent="reschedule(booking)"><AppFormField label="Start" required><input v-model="rescheduleStart" type="datetime-local" required></AppFormField><AppFormField label="End" required><input v-model="rescheduleEnd" type="datetime-local" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="rescheduling = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isActing === booking.id">Save time</AppButton></template></AppFormFooter></form><template #utility><p>The server checks the requested period against availability and existing booking rules before accepting the reschedule.</p></template></AppDialog></div></template>
      </SurfaceRow>
      </div>
      <aside v-if="selectedBooking" class="bookings-surface__preview" aria-label="Booking preview">
        <p class="bookings-surface__eyebrow">Selected booking</p>
        <h2>{{ selectedBooking.businessOfferingTitle }}</h2>
        <p>{{ selectedBooking.customerUsername }} · {{ formatDate(selectedBooking.startsAt) }}</p>
        <dl>
          <div><dt>Status</dt><dd>{{ selectedBooking.statusLabel }}</dd></div>
          <div><dt>Duration</dt><dd>{{ selectedBooking.durationSnapshotMinutes }} min</dd></div>
          <div><dt>Timezone</dt><dd>{{ selectedBooking.timezone }}</dd></div>
          <div><dt>Allowed actions</dt><dd>{{ selectedBooking.allowedActions.length }}</dd></div>
        </dl>
        <p v-if="selectedBooking.blockingReason" class="bookings-surface__preview-note">{{ selectedBooking.blockingReason }}</p>
        <p v-if="selectedBooking.customerNote" class="bookings-surface__preview-note"><strong>Customer note:</strong> {{ selectedBooking.customerNote }}</p>
        <p class="bookings-surface__preview-note">Actions remain controlled by the server-provided allowed action list.</p>
      </aside>
      <aside v-else class="bookings-surface__preview bookings-surface__preview--empty" aria-label="Booking preview"><p class="bookings-surface__eyebrow">Preview</p><h2>Select a booking</h2><p>Inspect booking context without leaving the owner collection.</p></aside>
    </div>
  </section>
</template>

<style scoped>
.bookings-surface{display:grid;gap:var(--space-3);max-width:none}.bookings-surface__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.bookings-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.bookings-surface__count,.bookings-surface__details span,.bookings-surface__details small{color:var(--text-muted);font-size:var(--text-size-meta)}.bookings-surface__feedback{margin:0;color:var(--success)}.bookings-surface__list{display:grid;gap:0;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.bookings-surface__row{display:flex;justify-content:space-between;gap:var(--space-3);align-items:center;padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);background:var(--surface-base)}.bookings-surface__details{display:grid;gap:var(--space-1);min-width:0}.bookings-surface__details span,.bookings-surface__details small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.bookings-surface__reschedule{display:flex;flex-wrap:wrap;gap:var(--space-2);align-items:end;margin-top:var(--space-1);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.bookings-surface__reschedule label{display:grid;gap:var(--space-1);font-size:var(--text-size-meta)}.bookings-surface__reschedule input{min-height:var(--control-height-default);padding:var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink);font:inherit}.bookings-surface__actions{display:flex;gap:var(--space-1);flex-wrap:wrap;justify-content:end}.bookings-surface__actions button,.bookings-surface__status button{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.bookings-surface__actions button:hover{border-color:var(--control-border-active);background:var(--control-bg-hover)}.bookings-surface__actions button:disabled{opacity:.5;cursor:wait}.bookings-surface__danger{color:var(--danger)}.bookings-surface__status{padding:var(--space-3) 0;color:var(--text-muted)}.bookings-surface__status--error{color:var(--danger)}.bookings-surface__status button{margin-left:var(--space-2);color:inherit;text-decoration:underline;border:0;background:transparent}@media(max-width:620px){.bookings-surface__row{align-items:start;flex-direction:column}.bookings-surface__details span,.bookings-surface__details small{white-space:normal}.bookings-surface__actions{justify-content:start}}
/* Workspace visual remediation keeps booking authority unchanged. */
.bookings-surface{gap:var(--space-3);max-width:none}.bookings-surface button,.bookings-surface input,.bookings-surface select{border-radius:var(--radius-control)}
</style>
<style scoped>
.bookings-surface__list { gap: 0; overflow: hidden; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.bookings-surface__workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.bookings-surface__preview { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.bookings-surface__preview h2,.bookings-surface__preview p { margin:0; }
.bookings-surface__preview h2 { color:var(--text); font-size:var(--text-size-title); }
.bookings-surface__preview dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.bookings-surface__preview dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.bookings-surface__preview dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.bookings-surface__preview dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.bookings-surface__preview-note { color:var(--text-soft); font-size:var(--text-size-meta); line-height:1.45; }
.bookings-surface__preview--empty { min-height:10rem; align-content:center; }
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
@media(max-width:860px){.bookings-surface__workspace{grid-template-columns:1fr}.bookings-surface__preview{order:2}}
</style>
