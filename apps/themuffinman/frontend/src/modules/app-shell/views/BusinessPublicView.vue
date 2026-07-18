<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import type {BusinessPublicPageDTO, BusinessBookingRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import AppEmptyState from "../components/AppEmptyState.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import DetailSurface from "../components/DetailSurface.vue"
import DetailUtilityRail from "../components/DetailUtilityRail.vue"
import SurfaceRow from "../components/SurfaceRow.vue"

const route = useRoute()
const router = useRouter()
const page = ref<BusinessPublicPageDTO | null>(null)
const selectedOfferingId = ref<number | null>(null)
const startsAt = ref("")
const customerNote = ref("")
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const bookingFeedback = ref("")
const bookingError = ref("")
const isFavorite = ref(false)
const isFavoriteActing = ref(false)
const returnTo = computed(() => {
  const value = route.query.returnTo
  if (typeof value !== "string") return "/business/find"
  try {
    const target = new URL(value, "https://workspace.local")
    return target.pathname === "/business/find" ? `${target.pathname}${target.search}` : "/business/find"
  } catch { return "/business/find" }
})
const returnToDiscovery = () => router.push(returnTo.value)
const load = async () => { isLoading.value = true; error.value = ""; try { page.value = await userShellApi.getPublicBusinessPage(String(route.params.slug)); try { const favorites = await userShellApi.getBusinessFavorites(); isFavorite.value = favorites.some(item => item.businessProfileId === page.value?.businessProfileId) } catch { isFavorite.value = false } } catch { error.value = "Could not load this business." } finally { isLoading.value = false } }
const toggleFavorite = async () => { if (!page.value) return; isFavoriteActing.value = true; error.value = ""; try { if (isFavorite.value) await userShellApi.removeBusinessFavorite(page.value.businessProfileId); else await userShellApi.addBusinessFavorite(page.value.businessProfileId); isFavorite.value = !isFavorite.value; feedback.value = isFavorite.value ? "Business saved to favorites." : "Business removed from favorites." } catch { error.value = "Could not update business favorites." } finally { isFavoriteActing.value = false } }
const book = async () => { if (!page.value || !selectedOfferingId.value || !startsAt.value) return; isSaving.value = true; bookingError.value = ""; bookingFeedback.value = ""; const start = new Date(startsAt.value).toISOString(); try { const preview = await userShellApi.previewPublicBooking(String(route.params.slug), {businessOfferingId: selectedOfferingId.value, startsAt: start}); const request: BusinessBookingRequestDTO = {businessOfferingId: preview.businessOfferingId, startsAt: preview.startsAt, endsAt: preview.endsAt, customerNote: customerNote.value, idempotencyKey: crypto.randomUUID()}; await userShellApi.createCustomerBooking(request); bookingFeedback.value = "Booking request sent."; customerNote.value = "" } catch { bookingError.value = "Could not create this booking." } finally { isSaving.value = false } }
onMounted(() => void load())
</script>

<template><section class="public-business"><AppStatus v-if="isLoading" message="Loading business." busy /><AppStatus v-else-if="error && !page" :message="error" tone="error" retry @retry="load" /><template v-else-if="page"><header><AppButton type="button" tone="quiet" @click="returnToDiscovery">Back to results</AppButton><p class="public-business__eyebrow">Business</p><h1>{{ page.businessName }}</h1><p class="public-business__headline">{{ page.headline }}</p></header><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" /><DetailSurface :title="page.businessName" utility-label="Business actions"><template #default><p class="public-business__description">{{ page.description }}</p><template v-if="page.bookingEnabled"><CollectionToolbar title="Offerings" :count="page.offerings.length" /><div v-if="page.offerings.length" class="public-business__offerings"><SurfaceRow v-for="offering in page.offerings" :key="offering.id" :row="{id: String(offering.id), title: offering.title, description: offering.summary || 'No offering summary', meta: `${offering.basePriceAmount} ${offering.basePriceCurrency} · ${offering.defaultDurationMinutes} min`}"><template #actions><AppButton type="button" tone="primary" @click="selectedOfferingId = offering.id; bookingFeedback = ''; bookingError = ''">Book</AppButton></template></SurfaceRow></div><AppEmptyState v-else title="No appointments available yet" message="This business accepts bookings, but the backend has not published an offering that can be requested yet." /></template><AppEmptyState v-else title="Appointments are not available" message="This is a public business profile. Booking is not enabled for this business." /></template><template #utility><DetailUtilityRail title="Business actions"><AppButton type="button" :tone="isFavorite ? 'secondary' : 'primary'" :loading="isFavoriteActing" @click="toggleFavorite">{{ isFavorite ? "Saved" : "Save business" }}</AppButton><p>{{ page.bookingEnabled ? 'Booking availability and appointment validation are prepared by the backend.' : 'This profile does not offer appointment booking.' }}</p></DetailUtilityRail></template></DetailSurface><AppDialog :open="selectedOfferingId !== null" title="Request a booking" layout="workspace" @close="selectedOfferingId = null"><form class="public-business__form" @submit.prevent="book"><AppFormField label="Start" required><input v-model="startsAt" type="datetime-local" required></AppFormField><AppFormField label="Note" optional><textarea v-model="customerNote" maxlength="2000"></textarea></AppFormField><AppStatus v-if="bookingFeedback" :message="bookingFeedback" tone="success" /><AppStatus v-if="bookingError" :message="bookingError" tone="error" /><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="selectedOfferingId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Sending" : "Send request" }}</AppButton></template></AppFormFooter></form></AppDialog></template></section></template>

<style scoped>
.public-business{display:grid;gap:var(--space-3);max-width:none}.public-business__back{justify-self:start;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:transparent;color:var(--text-muted);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.public-business__eyebrow{margin:var(--space-3) 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.public-business__headline,.public-business__description{color:var(--text-muted);line-height:1.6}.public-business__headline{margin:var(--space-1) 0 0}.public-business__description{margin:0 0 var(--space-5)}.public-business__offerings{border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden}.public-business__offerings :deep(.surface-row:last-child){border-bottom:0}.public-business__favorite{width:calc(100% - 2 * var(--space-3));margin:var(--space-3);min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);font:inherit;font-weight:var(--text-weight-semibold);cursor:pointer}.public-business :deep(.detail-utility-rail p){margin:0;padding:var(--space-3);border-top:1px solid var(--border-subtle);color:var(--text-muted);font-size:var(--text-size-meta);line-height:1.5}.public-business__form{display:grid;gap:var(--space-3)}.public-business__form input,.public-business__form textarea{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.public-business__form textarea{min-height:7rem}
</style>
<style scoped>
.public-business .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.public-business .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
</style>
