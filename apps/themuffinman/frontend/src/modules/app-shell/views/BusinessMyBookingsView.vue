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
import {formatDateTime} from "../../../services/formatters.ts"
const bookings = ref<BusinessBookingResponseDTO[]>([])
// Customer booking rows preserve the backend-provided timezone and lifecycle snapshot.
// Customer actions stay separate from the owner booking queue.
const isLoading = ref(true)
const isActing = ref<number | null>(null)
const error = ref("")
const feedback = ref("")
const rescheduling = ref<number | null>(null)
const rescheduleStart = ref("")
const rescheduleEnd = ref("")
const reschedulingBooking = computed(() => bookings.value.find(booking => booking.id === rescheduling.value) ?? null)
const selectedBookingId = ref<number | null>(null)
const selectedBooking = computed(() => bookings.value.find(booking => booking.id === selectedBookingId.value) ?? null)
const formatDate = (value: string) => formatDateTime(value, "Unknown time")
const load = async () => { isLoading.value = true; error.value = ""; try { bookings.value = (await userShellApi.getMyBusinessBookings()).items } catch { error.value = "Could not load your bookings." } finally { isLoading.value = false } }
const cancel = async (booking: BusinessBookingResponseDTO) => { if (!await confirmAction("Cancel this booking?", "Cancel booking")) return; isActing.value = booking.id; error.value = ""; try { await userShellApi.cancelMyBusinessBooking(booking.id); feedback.value = "Booking cancelled."; await load() } catch { error.value = "Could not cancel this booking." } finally { isActing.value = null } }
const beginReschedule = (booking: BusinessBookingResponseDTO) => { rescheduling.value = booking.id; rescheduleStart.value = booking.startsAt.slice(0, 16); rescheduleEnd.value = booking.endsAt.slice(0, 16) }
const reschedule = async (booking: BusinessBookingResponseDTO) => { isActing.value = booking.id; error.value = ""; try { await userShellApi.rescheduleMyBusinessBooking(booking.id, new Date(rescheduleStart.value).toISOString(), new Date(rescheduleEnd.value).toISOString()); feedback.value = "Booking rescheduled."; rescheduling.value = null; await load() } catch { error.value = "Could not reschedule this booking. Check the selected time." } finally { isActing.value = null } }
onMounted(() => void load())
</script>

<template><section class="my-bookings"><header><p class="my-bookings__eyebrow">Business / My bookings</p><h1>My bookings</h1></header><CollectionToolbar title="Customer bookings" :count="bookings.length" :busy="isLoading" /><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading your bookings." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="bookings.length === 0" message="No bookings yet." /><div v-else class="my-bookings__workspace"><div class="my-bookings__list"><SurfaceRow v-for="booking in bookings" :key="booking.id" :row="{id: String(booking.id), title: `${booking.businessName} · ${booking.businessOfferingTitle}`, description: formatDate(booking.startsAt), meta: booking.statusLabel, badge: booking.statusLabel}" :selected="selectedBookingId === booking.id" @click="selectedBookingId = booking.id"><template #actions><AppButton v-if="booking.allowedActions.includes('RESCHEDULE')" type="button" tone="secondary" :loading="isActing === booking.id" @click="beginReschedule(booking)">Reschedule</AppButton><AppButton v-if="booking.allowedActions.includes('CANCEL')" type="button" tone="danger" :loading="isActing === booking.id" @click="cancel(booking)">Cancel</AppButton></template></SurfaceRow></div><aside v-if="selectedBooking" class="my-bookings__preview" aria-label="Booking context"><p class="my-bookings__eyebrow">Booking context</p><h2>{{ selectedBooking.businessName }}</h2><p>{{ selectedBooking.businessOfferingTitle }} · {{ formatDate(selectedBooking.startsAt) }}</p><dl><div><dt>Status</dt><dd>{{ selectedBooking.statusLabel }}</dd></div><div><dt>Duration</dt><dd>{{ selectedBooking.durationSnapshotMinutes }} min</dd></div><div><dt>Timezone</dt><dd>{{ selectedBooking.timezone }}</dd></div></dl><p v-if="selectedBooking.blockingReason" class="my-bookings__preview-note">{{ selectedBooking.blockingReason }}</p><p v-if="selectedBooking.customerNote" class="my-bookings__preview-note"><strong>Your note:</strong> {{ selectedBooking.customerNote }}</p><p class="my-bookings__preview-note">Available actions follow the booking policy.</p></aside><aside v-else class="my-bookings__preview my-bookings__preview--empty" aria-label="Booking context"><p class="my-bookings__eyebrow">Booking context</p><h2>Select a booking</h2><p>Details appear here when you select a booking.</p></aside></div><AppDialog :open="reschedulingBooking !== null" title="Reschedule booking" layout="workspace" @close="rescheduling = null"><form v-if="reschedulingBooking" class="my-bookings__reschedule" @submit.prevent="reschedule(reschedulingBooking)"><AppFormField label="Start" required><input v-model="rescheduleStart" type="datetime-local" required></AppFormField><AppFormField label="End" required><input v-model="rescheduleEnd" type="datetime-local" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="rescheduling = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isActing === reschedulingBooking.id">Save time</AppButton></template></AppFormFooter></form></AppDialog></section></template>

<style scoped>
.my-bookings{display:grid;gap:var(--space-3)}.my-bookings__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.my-bookings__list{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.my-bookings__list :deep(.surface-row:last-child){border-bottom:0}.my-bookings__danger{color:var(--danger)}.my-bookings__reschedule{display:grid;gap:var(--space-3)}.my-bookings__reschedule input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}
.my-bookings__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(16rem,22rem);gap:var(--space-3);align-items:start}.my-bookings__preview{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised);color:var(--text-muted)}.my-bookings__preview h2,.my-bookings__preview p{margin:0}.my-bookings__preview h2{color:var(--text);font-size:var(--text-size-title)}.my-bookings__preview dl{display:grid;gap:var(--space-2);margin:var(--space-2) 0}.my-bookings__preview dl div{display:flex;justify-content:space-between;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.my-bookings__preview dt{color:var(--text-soft);font-size:var(--text-size-meta)}.my-bookings__preview dd{margin:0;color:var(--text);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.my-bookings__preview-note{color:var(--text-soft);font-size:var(--text-size-meta);line-height:1.45}.my-bookings__preview--empty{min-height:10rem;align-content:center}@media(max-width:860px){.my-bookings__workspace{grid-template-columns:1fr}.my-bookings__preview{order:2}}
</style>
<style scoped>
.my-bookings .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.my-bookings .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.my-bookings .app-button--danger { color:var(--danger); }
</style>
