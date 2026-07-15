<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {BusinessBookingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const bookings = ref<BusinessBookingResponseDTO[]>([])
const isLoading = ref(true)
const isActing = ref<number | null>(null)
const error = ref("")
const feedback = ref("")

const formatDate = (value: string) => new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value))

const load = async () => {
  isLoading.value = true
  error.value = ""
  try { bookings.value = (await userShellApi.getBusinessOwnerBookings()).items }
  catch { error.value = "Could not load bookings." }
  finally { isLoading.value = false }
}

const execute = async (booking: BusinessBookingResponseDTO, action: "confirm" | "reject" | "cancel" | "complete" | "mark-no-show") => {
  if ((action === "cancel" || action === "reject") && !window.confirm(`${action === "cancel" ? "Cancel" : "Reject"} this booking?`)) return
  isActing.value = booking.id
  error.value = ""
  feedback.value = ""
  try { await userShellApi.executeBusinessBookingAction(booking.id, action); feedback.value = "Booking updated."; await load() }
  catch { error.value = "Could not update this booking." }
  finally { isActing.value = null }
}

onMounted(() => void load())
</script>

<template>
  <section class="bookings-surface">
    <header class="bookings-surface__header"><div><p class="bookings-surface__eyebrow">Business / Bookings</p><h1>Bookings</h1></div><span class="bookings-surface__count">{{ bookings.length }}</span></header>
    <p v-if="feedback" class="bookings-surface__feedback" role="status">{{ feedback }}</p>
    <div v-if="isLoading" class="bookings-surface__status" role="status">Loading.</div>
    <div v-else-if="error" class="bookings-surface__status bookings-surface__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <div v-else-if="bookings.length === 0" class="bookings-surface__status">No bookings yet.</div>
    <div v-else class="bookings-surface__list">
      <article v-for="booking in bookings" :key="booking.id" class="bookings-surface__row">
        <div class="bookings-surface__details"><strong>{{ booking.businessOfferingTitle }}</strong><span>{{ booking.customerUsername }} · {{ formatDate(booking.startsAt) }}</span><small>{{ booking.statusLabel }}<template v-if="booking.blockingReason"> · {{ booking.blockingReason }}</template></small></div>
        <div class="bookings-surface__actions">
          <button v-if="booking.allowedActions.includes('CONFIRM')" type="button" :disabled="isActing === booking.id" @click="execute(booking, 'confirm')">Confirm</button>
          <button v-if="booking.allowedActions.includes('REJECT')" type="button" :disabled="isActing === booking.id" @click="execute(booking, 'reject')">Reject</button>
          <button v-if="booking.allowedActions.includes('COMPLETE')" type="button" :disabled="isActing === booking.id" @click="execute(booking, 'complete')">Complete</button>
          <button v-if="booking.allowedActions.includes('MARK_NO_SHOW')" type="button" :disabled="isActing === booking.id" @click="execute(booking, 'mark-no-show')">No-show</button>
          <button v-if="booking.allowedActions.includes('CANCEL') || booking.allowedActions.includes('CANCEL_AS_OWNER')" type="button" class="bookings-surface__danger" :disabled="isActing === booking.id" @click="execute(booking, 'cancel')">Cancel</button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.bookings-surface{display:grid;gap:1rem}.bookings-surface__header{display:flex;justify-content:space-between;align-items:end}.bookings-surface__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.bookings-surface__count,.bookings-surface__details span,.bookings-surface__details small{color:rgba(23,34,26,.58);font-size:.84rem}.bookings-surface__feedback{margin:0;color:#2d6846}.bookings-surface__list{display:grid;gap:.45rem}.bookings-surface__row{display:flex;justify-content:space-between;gap:1rem;align-items:center;padding:.85rem 0;border-bottom:1px solid rgba(23,34,26,.08)}.bookings-surface__details{display:grid;gap:.28rem;min-width:0}.bookings-surface__details span,.bookings-surface__details small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.bookings-surface__actions{display:flex;gap:.35rem;flex-wrap:wrap;justify-content:end}.bookings-surface__actions button,.bookings-surface__status button{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .75rem;background:transparent;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer}.bookings-surface__actions button:disabled{opacity:.5;cursor:wait}.bookings-surface__danger{color:#8d2f25}.bookings-surface__status{padding:1rem 0;color:rgba(23,34,26,.65)}.bookings-surface__status--error{color:#8d2f25}.bookings-surface__status button{margin-left:.6rem;color:inherit;text-decoration:underline;border:0}@media(max-width:620px){.bookings-surface__row{align-items:start;flex-direction:column}.bookings-surface__details span,.bookings-surface__details small{white-space:normal}.bookings-surface__actions{justify-content:start}}
</style>
