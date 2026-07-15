<script setup lang="ts">
import {onMounted, ref} from "vue"
import {useRoute} from "vue-router"
import type {BusinessPublicPageDTO, BusinessBookingRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const route = useRoute()
const page = ref<BusinessPublicPageDTO | null>(null)
const selectedOfferingId = ref<number | null>(null)
const startsAt = ref("")
const customerNote = ref("")
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const load = async () => { isLoading.value = true; error.value = ""; try { page.value = await userShellApi.getPublicBusinessPage(String(route.params.slug)) } catch { error.value = "Could not load this business." } finally { isLoading.value = false } }
const book = async () => { if (!page.value || !selectedOfferingId.value || !startsAt.value) return; const offering = page.value.offerings.find(item => item.id === selectedOfferingId.value); if (!offering) return; isSaving.value = true; error.value = ""; feedback.value = ""; const start = new Date(startsAt.value); const end = new Date(start.getTime() + offering.defaultDurationMinutes * 60000); const request: BusinessBookingRequestDTO = {businessOfferingId: offering.id, startsAt: start.toISOString(), endsAt: end.toISOString(), customerNote: customerNote.value, idempotencyKey: crypto.randomUUID()}; try { await userShellApi.createCustomerBooking(request); feedback.value = "Booking request sent."; customerNote.value = "" } catch { error.value = "Could not create this booking." } finally { isSaving.value = false } }
onMounted(() => void load())
</script>

<template><section class="public-business"><div v-if="isLoading" class="public-business__status" role="status">Loading.</div><div v-else-if="error && !page" class="public-business__status public-business__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div><template v-else-if="page"><header><p class="public-business__eyebrow">Business</p><h1>{{ page.businessName }}</h1><p class="public-business__headline">{{ page.headline }}</p><p>{{ page.description }}</p></header><div class="public-business__offerings"><article v-for="offering in page.offerings" :key="offering.id" class="public-business__offering"><strong>{{ offering.title }}</strong><span>{{ offering.summary }} · {{ offering.basePriceAmount }} {{ offering.basePriceCurrency }} · {{ offering.defaultDurationMinutes }} min</span><button type="button" @click="selectedOfferingId = offering.id">Book</button></article></div><form v-if="selectedOfferingId" class="public-business__form" @submit.prevent="book"><h2>Request a booking</h2><label>Start<input v-model="startsAt" type="datetime-local" required></label><label>Note<textarea v-model="customerNote" maxlength="2000"></textarea></label><p v-if="feedback" class="public-business__feedback" role="status">{{ feedback }}</p><p v-if="error" class="public-business__status public-business__status--error" role="alert">{{ error }}</p><button type="submit" :disabled="isSaving">{{ isSaving ? "Sending" : "Send request" }}</button></form></template></section></template>

<style scoped>
.public-business{display:grid;gap:1.2rem;max-width:52rem}.public-business__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.7rem,3vw,2.6rem);letter-spacing:-.075em}.public-business__headline{color:rgba(23,34,26,.65)}.public-business__offerings{display:grid;gap:.45rem}.public-business__offering{display:flex;align-items:center;gap:1rem;padding:.85rem 0;border-bottom:1px solid rgba(23,34,26,.08)}.public-business__offering strong{margin-right:auto}.public-business__offering span{color:rgba(23,34,26,.58);font-size:.84rem}.public-business button{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .75rem;background:#17221a;color:#f8f8f4;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer}.public-business__form{display:grid;gap:.75rem;border-top:1px solid rgba(23,34,26,.1);padding-top:1rem;max-width:32rem}.public-business__form label{display:grid;gap:.35rem;font-size:.83rem;font-weight:650}.public-business__form input,.public-business__form textarea{border:1px solid rgba(23,34,26,.14);border-radius:.65rem;padding:.65rem;font:inherit}.public-business__form textarea{min-height:5rem}.public-business__feedback{color:#2d6846}.public-business__status{padding:.7rem 0;color:rgba(23,34,26,.65)}.public-business__status--error{color:#8d2f25}.public-business__status button{margin-left:.5rem;background:none;color:inherit;text-decoration:underline}@media(max-width:620px){.public-business__offering{align-items:start;flex-wrap:wrap}.public-business__offering span{width:100%}}
</style>
