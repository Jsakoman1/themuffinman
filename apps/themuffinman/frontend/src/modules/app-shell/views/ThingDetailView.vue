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
    <article v-else-if="listing" class="detail-card">
      <p class="eyebrow">Thing listing</p>
      <h1>{{ listing.title }}</h1>
      <p class="owner">Offered by {{ listing.ownerUsername }}</p>
      <p class="description">{{ listing.description || "No description yet." }}</p>
      <p class="condition">{{ listing.conditionNote || "Condition not specified" }}</p>
      <p v-if="feedback" class="status status--success" role="status">{{ feedback }}</p>
      <div v-if="listing.myPendingRequestId" class="pending">Your borrow request is pending.</div>
      <form v-else-if="listing.available" class="request-form" @submit.prevent="requestBorrow">
        <h2>Request to borrow</h2>
        <label>Message<textarea v-model="message" maxlength="1000" placeholder="Add a note for the owner."></textarea></label>
        <button type="submit" class="primary" :disabled="isSaving">{{ isSaving ? "Sending" : "Send request" }}</button>
      </form>
      <p v-else class="pending">This thing is currently unavailable.</p>
    </article>
  </section>
</template>

<style scoped>
.thing-detail{display:grid;gap:1rem;max-width:48rem}.back{color:rgba(23,34,26,.62);font-size:.9rem}.detail-card{display:grid;gap:.8rem;padding:1.4rem;border:1px solid rgba(23,34,26,.1);border-radius:1.2rem;background:rgba(255,255,255,.68)}.eyebrow{margin:0;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.8rem,4vw,3rem);letter-spacing:-.07em}.owner,.condition,.pending{margin:0;color:rgba(23,34,26,.58)}.description{margin:0;white-space:pre-wrap}.request-form{display:grid;gap:.7rem;border-top:1px solid rgba(23,34,26,.1);padding-top:1rem}.request-form h2{margin:0;font-size:1.1rem}.request-form label{display:grid;gap:.35rem;font-weight:650}.request-form textarea{min-height:6rem;border:1px solid rgba(23,34,26,.14);border-radius:.65rem;padding:.65rem;font:inherit}.primary{justify-self:start;border:0;border-radius:999px;padding:.65rem 1rem;background:#17221a;color:#f8f8f4;font:inherit;font-weight:650;cursor:pointer}.primary:disabled{opacity:.55;cursor:wait}.status{padding:.7rem 0;color:rgba(23,34,26,.65)}.status--error{color:#8d2f25}.status--success{color:#2d6846}.status button{border:0;background:none;text-decoration:underline;cursor:pointer}
</style>
