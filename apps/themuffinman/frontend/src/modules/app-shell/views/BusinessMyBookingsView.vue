<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessBookingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
const bookings = ref<BusinessBookingResponseDTO[]>([])
const isLoading = ref(true)
const isActing = ref<number | null>(null)
const error = ref("")
const feedback = ref("")
const rescheduling = ref<number | null>(null)
const rescheduleStart = ref("")
const rescheduleEnd = ref("")
const formatDate = (value: string) => new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value))
const load = async () => { isLoading.value = true; error.value = ""; try { bookings.value = (await userShellApi.getMyBusinessBookings()).items } catch { error.value = "Could not load your bookings." } finally { isLoading.value = false } }
const cancel = async (booking: BusinessBookingResponseDTO) => { if (!await confirmAction("Cancel this booking?", "Cancel booking")) return; isActing.value = booking.id; error.value = ""; try { await userShellApi.cancelMyBusinessBooking(booking.id); feedback.value = "Booking cancelled."; await load() } catch { error.value = "Could not cancel this booking." } finally { isActing.value = null } }
const beginReschedule = (booking: BusinessBookingResponseDTO) => { rescheduling.value = booking.id; rescheduleStart.value = booking.startsAt.slice(0, 16); rescheduleEnd.value = booking.endsAt.slice(0, 16) }
const reschedule = async (booking: BusinessBookingResponseDTO) => { isActing.value = booking.id; error.value = ""; try { await userShellApi.rescheduleMyBusinessBooking(booking.id, new Date(rescheduleStart.value).toISOString(), new Date(rescheduleEnd.value).toISOString()); feedback.value = "Booking rescheduled."; rescheduling.value = null; await load() } catch { error.value = "Could not reschedule this booking. Check the selected time." } finally { isActing.value = null } }
onMounted(() => void load())
</script>

<template><section class="my-bookings"><header><p class="my-bookings__eyebrow">Business / My bookings</p><h1>My bookings</h1></header><p v-if="feedback" class="my-bookings__feedback" role="status">{{ feedback }}</p><div v-if="isLoading" class="my-bookings__status" role="status">Loading.</div><div v-else-if="error" class="my-bookings__status my-bookings__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div><div v-else-if="bookings.length === 0" class="my-bookings__status">No bookings yet.</div><div v-else class="my-bookings__list"><SurfaceRow v-for="booking in bookings" :key="booking.id" :row="{title: `${booking.businessName} · ${booking.businessOfferingTitle}`, description: formatDate(booking.startsAt), meta: booking.statusLabel, badge: booking.statusLabel}"><template #actions><div class="my-bookings__actions"><button v-if="booking.allowedActions.includes('RESCHEDULE')" type="button" :disabled="isActing === booking.id" @click="beginReschedule(booking)">Reschedule</button><button v-if="booking.allowedActions.includes('CANCEL')" type="button" class="my-bookings__danger" :disabled="isActing === booking.id" @click="cancel(booking)">Cancel</button><AppDialog :open="rescheduling === booking.id" title="Reschedule booking" @close="rescheduling = null"><form class="my-bookings__reschedule" @submit.prevent="reschedule(booking)"><label>Start<input v-model="rescheduleStart" type="datetime-local" required></label><label>End<input v-model="rescheduleEnd" type="datetime-local" required></label><button type="submit" :disabled="isActing === booking.id">Save time</button><button type="button" @click="rescheduling = null">Close</button></form></AppDialog></div></template></SurfaceRow></div></section></template>

<style scoped>
.my-bookings{display:grid;gap:1rem}.my-bookings__eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.my-bookings__list{display:grid;gap:.45rem}.my-bookings__row{display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:.85rem 0;border:1px solid var(--border-subtle)}.my-bookings__row>div:first-child{display:grid;gap:.3rem}.my-bookings__row span{color:var(--text-muted);font-size:.84rem}.my-bookings__actions{display:flex;gap:.35rem}.my-bookings__reschedule{display:flex;flex-wrap:wrap;gap:.4rem;align-items:end;margin-top:.5rem}.my-bookings__reschedule label{display:grid;gap:.2rem;font-size:.72rem}.my-bookings__reschedule input{padding:.35rem;border:1px solid var(--border-subtle);border-radius:.4rem;font:inherit}.my-bookings button{border:1px solid var(--border-subtle);border-radius:999px;padding:.45rem .75rem;background:transparent;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer}.my-bookings__danger{color:var(--danger)}.my-bookings__feedback{color:var(--success)}.my-bookings__status{padding:.7rem 0;color:var(--text-muted)}.my-bookings__status--error{color:var(--danger)}.my-bookings__status button{margin-left:.5rem;border:0;color:inherit;text-decoration:underline}@media(max-width:620px){.my-bookings__row{align-items:start;flex-direction:column}.my-bookings__actions{align-self:stretch}}
</style>
