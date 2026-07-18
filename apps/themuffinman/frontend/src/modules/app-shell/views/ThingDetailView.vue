<script setup lang="ts">
import {onMounted, ref} from "vue"
import {useRoute} from "vue-router"
import type {ThingListingResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const route = useRoute()
const listing = ref<ThingListingResponseDTO | null>(null)
const message = ref("")
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const listingId = Number(route.params.listingId)

const load = async () => {
  isLoading.value = true
  error.value = ""
  try {
    listing.value = await userShellApi.getThingListing(listingId)
  } catch {
    error.value = "Could not load this thing."
  } finally {
    isLoading.value = false
  }
}

const requestBorrow = async () => {
  if (!listing.value) return
  isSaving.value = true
  error.value = ""
  try {
    const request = await userShellApi.requestThingBorrow(listing.value.id, message.value)
    listing.value.myPendingRequestId = request.id
    message.value = ""
    feedback.value = "Borrow request sent."
  } catch {
    error.value = "Could not request this thing."
  } finally {
    isSaving.value = false
  }
}

onMounted(() => void load())
</script>

<template>
  <section class="thing-detail">
    <router-link to="/things" class="back">← Back to things</router-link>
    <div v-if="isLoading" class="status">Loading.</div>
    <div v-else-if="error" class="status status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <article v-else-if="listing" class="detail-card" aria-label="Thing narrative and permitted borrowing action">
      <p class="eyebrow">Thing listing</p>
      <h1>{{ listing.title }}</h1>
      <p class="owner">Offered by {{ listing.ownerUsername }}</p>
      <p class="description">{{ listing.description || "No description yet." }}</p>
      <div class="thing-detail__workspace"><main><p class="description">{{ listing.description || "No description yet." }}</p><section class="thing-detail__activity"><h2>Current activity</h2><p>{{ listing.availabilityLabel }}</p></section></main><aside><p class="eyebrow">Properties</p><dl><div><dt>Condition</dt><dd>{{ listing.conditionNote || "Not specified" }}</dd></div><div><dt>Listed</dt><dd>{{ new Date(listing.createdAt).toLocaleDateString() }}</dd></div></dl></aside></div>
      <p v-if="feedback" class="status status--success" role="status">{{ feedback }}</p>
      <div v-if="listing.myPendingRequestId" class="pending">Your borrow request is pending.</div>
      <form v-else-if="listing.allowedActions.includes('REQUEST_BORROW')" class="request-form" @submit.prevent="requestBorrow">
        <h2>Request to borrow</h2>
        <label>Message<textarea v-model="message" maxlength="1000" placeholder="Add a note for the owner."></textarea></label>
        <button type="submit" class="primary" :disabled="isSaving">{{ isSaving ? "Sending" : "Send request" }}</button>
      </form>
      <p v-else class="pending">This thing is currently unavailable.</p>
    </article>
  </section>
</template>

<style scoped>
.thing-detail{display:grid;gap:1rem;max-width:48rem}.back{color:var(--text-muted);font-size:.9rem}.detail-card{display:grid;gap:.8rem;padding:1.4rem;border:1px solid var(--border-subtle);border-radius:1.2rem;background:var(--surface)}.eyebrow{margin:0;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.8rem,4vw,3rem);letter-spacing:-.07em}.owner,.condition,.pending{margin:0;color:var(--text-muted)}.description{margin:0;white-space:pre-wrap}.request-form{display:grid;gap:.7rem;border:1px solid var(--border-subtle);padding-top:1rem}.request-form h2{margin:0;font-size:1.1rem}.request-form label{display:grid;gap:.35rem;font-weight:650}.request-form textarea{min-height:6rem;border:1px solid var(--border-subtle);border-radius:.65rem;padding:.65rem;font:inherit}.primary{justify-self:start;border:0;border-radius:999px;padding:.65rem 1rem;background:var(--accent);color:var(--text);font:inherit;font-weight:650;cursor:pointer}.primary:disabled{opacity:.55;cursor:wait}.status{padding:.7rem 0;color:var(--text-muted)}.status--error{color:var(--danger)}.status--success{color:var(--success)}.status button{border:0;background:none;text-decoration:underline;cursor:pointer}
</style>
<style scoped>.thing-detail__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(14rem,20rem);gap:1rem}.thing-detail__workspace aside{border-left:1px solid var(--border-subtle);padding-left:1rem}.thing-detail__workspace dl{display:grid;gap:.55rem}.thing-detail__workspace dl div{display:flex;justify-content:space-between;gap:.7rem}.thing-detail__workspace dt{color:var(--text-muted);font-size:.8rem}.thing-detail__workspace dd{margin:0;text-align:right}.thing-detail__activity{margin-top:1.25rem;border-top:1px solid var(--border-subtle);padding-top:1rem}.thing-detail__activity h2{margin:0 0 .4rem;font-size:1rem}.thing-detail__activity p{margin:0;color:var(--text-muted)}@media(max-width:700px){.thing-detail__workspace{grid-template-columns:1fr}.thing-detail__workspace aside{border-left:0;border-top:1px solid var(--border-subtle);padding:1rem 0 0}}</style>
