<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
// Public business content retains a single readable column on narrow viewports.
import {useRoute, useRouter} from "vue-router"
import type {BusinessBookingRequestDTO, BusinessPublicPageDTO, BusinessOfferingSchemaDTO, BusinessPublicQuoteDTO, BusinessAvailabilityWindowDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppStatus from "../components/AppStatus.vue"
import AppEmptyState from "../components/AppEmptyState.vue"
import BusinessAvailabilityPicker from "../components/BusinessAvailabilityPicker.vue"
import BusinessBookingStepper from "../components/BusinessBookingStepper.vue"
import BusinessDemandForm from "../components/BusinessDemandForm.vue"
import BusinessQuoteSummary from "../components/BusinessQuoteSummary.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import TaskSurface from "../components/TaskSurface.vue"
import BusinessPublicProfileHeader from "../components/BusinessPublicProfileHeader.vue"
import BusinessPublicSectionTabs from "../components/BusinessPublicSectionTabs.vue"
import BusinessServiceCard from "../components/BusinessServiceCard.vue"
import BusinessAvailabilityCalendar from "../components/BusinessPublicAvailabilityCalendar.vue"
import BusinessBookingSelectionSummary from "../components/BusinessBookingSelectionSummary.vue"
import BusinessRatingSummary from "../components/BusinessRatingSummary.vue"
import BusinessReviewList from "../components/BusinessReviewList.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"

const route = useRoute(); const router = useRouter()
const page = ref<BusinessPublicPageDTO | null>(null); const selectedOfferingId = ref<number | null>(null); const schema = ref<BusinessOfferingSchemaDTO | null>(null); const quote = ref<BusinessPublicQuoteDTO | null>(null); const availability = ref<BusinessAvailabilityWindowDTO[]>([]); const demand = ref<Record<string, string>>({}); const selectedOptions = ref<Record<string, string>>({}); const bookingDate = ref(""); const selectedSlot = ref(""); const customerNote = ref(""); const quantity = ref(1); const currentStep = ref(0); const loading = ref(true); const saving = ref(false); const error = ref(""); const bookingError = ref(""); const feedback = ref(""); const completedBooking = ref<import("../../../contracts/index.ts").BusinessBookingResponseDTO | null>(null)
const steps = ["Service", "Details", "Time", "Review"]
const selectedOffering = computed(() => page.value?.offerings.find(item => item.id === selectedOfferingId.value) ?? null)
const availabilityCalendar = ref<import("../../../contracts/index.ts").BusinessPublicAvailabilityCalendarDTO | null>(null)
const calendarView = ref<"MONTH" | "WEEK" | "DAY">("MONTH")
const calendarAnchorDate = ref("")
const reviews = ref<import("../../../contracts/index.ts").BusinessReviewResponseDTO[]>([])
const reviewsLoading = ref(false)
const isFavorite = ref(false)
const favoriteBusy = ref(false)
const serviceTabs = getModuleTabs("services")?.tabs ?? []
const returnTo = computed(() => typeof route.query.returnTo === "string" ? route.query.returnTo : "/business/find")
const activeSection = computed<"overview" | "services" | "reviews">(() => route.query.section === "services" || route.query.section === "reviews" ? route.query.section : "overview")
const selectSection = (section: "overview" | "services" | "reviews") => router.replace({query: {...route.query, section}})
const load = async () => { loading.value = true; error.value = ""; page.value = null; try { page.value = await userShellApi.getPublicBusinessPage(String(route.params.slug)); isFavorite.value = (await userShellApi.getBusinessFavorites()).some(item => item.businessProfileId === page.value?.businessProfileId) } catch { error.value = "Could not load this business." } finally { loading.value = false } }
const loadReviews = async () => { reviewsLoading.value = true; try { reviews.value = (await userShellApi.getPublicBusinessReviews(String(route.params.slug))).items } finally { reviewsLoading.value = false } }
const toggleFavorite = async () => { if (!page.value) return; favoriteBusy.value = true; try { if (isFavorite.value) await userShellApi.removeBusinessFavorite(page.value.businessProfileId); else await userShellApi.addBusinessFavorite(page.value.businessProfileId); isFavorite.value = !isFavorite.value } catch { feedback.value = "Could not update favorites. Try again." } finally { favoriteBusy.value = false } }
const businessToday = () => page.value ? new Intl.DateTimeFormat("en-CA", {timeZone: page.value.timezone}).format(new Date()) : ""
const loadCalendar = async () => { if (!selectedOfferingId.value || !calendarAnchorDate.value) return; const days = calendarView.value === "DAY" ? 1 : calendarView.value === "WEEK" ? 7 : 31; availabilityCalendar.value = await userShellApi.getPublicAvailabilityCalendar(String(route.params.slug), selectedOfferingId.value, calendarAnchorDate.value, days, calendarView.value) }
const moveCalendar = async (amount: number) => { const cursor = new Date(`${calendarAnchorDate.value}T12:00:00`); cursor.setDate(cursor.getDate() + amount * (calendarView.value === "DAY" ? 1 : calendarView.value === "WEEK" ? 7 : 31)); calendarAnchorDate.value = cursor.toISOString().slice(0, 10); await loadCalendar() }
const selectCalendarDate = async (date: string) => { bookingDate.value = date; calendarAnchorDate.value = date; await Promise.all([loadAvailabilityForDate(), loadCalendar()]) }
const selectOffering = async (id: number) => { selectedOfferingId.value = id; currentStep.value = 1; demand.value = {}; selectedOptions.value = {}; quantity.value = 1; quote.value = null; availability.value = []; selectedSlot.value = ""; bookingDate.value = businessToday(); calendarAnchorDate.value = bookingDate.value; bookingError.value = ""; schema.value = await userShellApi.getPublicServiceSchema(String(route.params.slug), id); await loadCalendar() }
const resetFlow = () => { selectedOfferingId.value = null; currentStep.value = 0; schema.value = null; quote.value = null; availability.value = []; demand.value = {}; selectedOptions.value = {}; selectedSlot.value = ""; bookingDate.value = ""; calendarAnchorDate.value = ""; customerNote.value = ""; quantity.value = 1 }
const loadAvailabilityForDate = async () => { if (!selectedOfferingId.value || !bookingDate.value) return; selectedSlot.value = ""; quote.value = null; bookingError.value = ""; availability.value = (await userShellApi.getPublicAvailabilityForBusinessDate(String(route.params.slug), selectedOfferingId.value, bookingDate.value)).items }
const refreshQuote = async () => { if (!selectedOfferingId.value || !selectedSlot.value) return; quote.value = await userShellApi.quotePublicService(String(route.params.slug), {businessOfferingId: selectedOfferingId.value, startsAt: selectedSlot.value, quantity: quantity.value, answers: demand.value, selectedOptions: selectedOptions.value}) }
const goNext = async () => { bookingError.value = ""; if (currentStep.value === 1) { await loadAvailabilityForDate(); currentStep.value = 2; return } if (currentStep.value === 2) { if (!selectedSlot.value) { bookingError.value = "Choose one available time to continue."; return } await refreshQuote(); currentStep.value = 3; return } if (currentStep.value < 3) currentStep.value++ }
const book = async () => { if (!page.value || !selectedOfferingId.value || !selectedSlot.value) return; saving.value = true; bookingError.value = ""; try { const preview = await userShellApi.previewPublicBooking(String(route.params.slug), {businessOfferingId: selectedOfferingId.value, startsAt: selectedSlot.value}); const request: BusinessBookingRequestDTO = {businessOfferingId: preview.businessOfferingId, startsAt: preview.startsAt, endsAt: preview.endsAt, customerNote: customerNote.value, idempotencyKey: crypto.randomUUID(), quantity: quote.value?.quantity ?? quantity.value, answers: demand.value, selectedOptions: selectedOptions.value}; completedBooking.value = await userShellApi.createCustomerBooking(request); feedback.value = "Booking request sent."; resetFlow() } catch { currentStep.value = 2; selectedSlot.value = ""; availability.value = []; bookingError.value = "That time is no longer available. Your details were kept; choose another time."; await loadAvailabilityForDate() } finally { saving.value = false } }
watch(() => route.params.slug, () => { resetFlow(); feedback.value = ""; bookingError.value = ""; void load() })
onMounted(() => void load())
</script>

<template>
  <TaskSurface mode="choose" label="Service booking">
    <section class="public-business" data-booking-model="progressive-availability-first" data-business-page-model="service-first-booking" data-mental-model="discover-select-service-choose-time-review" aria-label="Service booking">
      <AppStatus v-if="loading" message="Loading business." busy />
      <AppStatus v-else-if="error && !page" :message="error" tone="error" retry @retry="load" />
      <template v-else-if="page">
        <ModuleTabs :tabs="serviceTabs" active-id="find" />
        <button class="public-business__back" type="button" @click="router.push(returnTo)">‹ Back to services</button>
        <BusinessPublicProfileHeader :page="page" :saved="isFavorite" :saving="favoriteBusy" @toggle-save="toggleFavorite" />
        <BusinessPublicSectionTabs :active="activeSection" @select="selectSection" />
        <section v-if="completedBooking" class="booking-completion" aria-live="polite"><p class="eyebrow">Booking requested</p><h2>{{ completedBooking.statusLabel }}</h2><p>Your request for {{ completedBooking.businessOfferingTitle }} is saved. The business will contact you if it needs to confirm anything.</p><dl><div><dt>When</dt><dd>{{ new Date(completedBooking.startsAt).toLocaleString(undefined, {timeZone: completedBooking.timezone}) }}</dd></div><div><dt>Price</dt><dd>{{ completedBooking.priceSnapshotAmount == null ? "Confirmed after review" : `${completedBooking.priceSnapshotAmount} ${completedBooking.priceSnapshotCurrency}` }}</dd></div><div><dt>Time zone</dt><dd>{{ completedBooking.timezone }}</dd></div></dl><p class="booking-completion__note">You can review the booking, its confirmation status, and any cancellation option in My bookings.</p><div class="booking-completion__actions"><AppButton tone="primary" @click="router.push('/business/my-bookings')">View my bookings</AppButton><AppButton tone="secondary" @click="completedBooking = null">Book another service</AppButton></div></section>
        <template v-else><AppStatus v-if="feedback" :message="feedback" tone="success" />
        <section v-if="activeSection === 'overview'" class="public-business__overview"><div v-if="page.description" class="public-business__description" v-html="page.description" /><div v-if="page.galleryImages.length" class="public-business__gallery" aria-label="Business gallery"><img v-for="image in page.galleryImages" :key="image.id" :src="image.imageUrl" :alt="image.altText || `${page.businessName} gallery image`" loading="lazy" /></div><AppButton v-if="page.offerings.length" tone="primary" @click="selectSection('services')">View services and availability</AppButton></section>
        <section v-else-if="activeSection === 'reviews'" class="public-business__reviews"><h2>Reviews</h2><BusinessRatingSummary :average-stars="page.ratingSummary.averageStars" :review-count="page.ratingSummary.reviewCount" /><AppButton type="button" tone="secondary" @click="loadReviews">Show reviews</AppButton><BusinessReviewList :reviews="reviews" :loading="reviewsLoading" /></section>
        <section v-if="activeSection === 'services' && !selectedOfferingId" id="business-services" class="offerings" data-primary-surface="service-selection">
          <div>
            <p class="eyebrow">Services</p><h2>What can we help with?</h2>
            <p>Select one service. We will show only the details, price and times relevant to it.</p>
          </div>
          <div v-if="page.offerings.length">
              <BusinessServiceCard v-for="offering in page.offerings" :key="offering.id" :offering="offering" data-service-row @book="selectOffering" />
          </div>
          <AppEmptyState v-else title="No services available" message="This business has not published a bookable service yet." />
          <details v-if="page.description || page.galleryImages.length || page.contactEmail || page.contactPhone" class="public-business__details">
            <summary>More about {{ page.businessName }}</summary>
            <div class="public-business__details-body">
              <p v-if="page.description">{{ page.description }}</p>
              <div v-if="page.galleryImages.length" class="public-business__gallery" aria-label="Business gallery">
                <img v-for="image in page.galleryImages" :key="image.id" :src="image.imageUrl" :alt="image.altText || `${page.businessName} gallery image`" loading="lazy" />
              </div>
              <p v-if="page.contactEmail || page.contactPhone"><strong>Contact:</strong> {{ page.contactEmail || page.contactPhone }}</p>
            </div>
          </details>
        </section>
        <section v-else class="booking-flow native-group">
          <header class="booking-flow__header" :data-booking-step="steps[currentStep].toLowerCase().replaceAll(' ', '-')">
            <div><p class="eyebrow">Booking</p><h2>{{ selectedOffering?.title }}</h2><p>{{ selectedOffering?.summary || 'Configure your request step by step.' }}</p></div>
            <AppButton type="button" tone="quiet" @click="resetFlow">Change service</AppButton>
          </header>
          <BusinessBookingStepper :steps="steps" :current="currentStep" @select="currentStep = $event" />
          <BusinessBookingSelectionSummary :service-title="selectedOffering?.title" :starts-at="selectedSlot" :timezone="page.timezone" />
          <p class="booking-flow__guidance">{{ currentStep === 1 ? 'Answer only the questions needed for this service.' : currentStep === 2 ? `Choose a date and an available local time (${page.timezone}).` : currentStep === 3 ? 'Review the price and confirmation terms before sending.' : 'Select a service to begin.' }}</p>
          <form @submit.prevent="currentStep === 3 ? book() : goNext()">
            <section v-if="currentStep === 1" class="booking-panel">
              <h3>Customer details</h3>
              <BusinessDemandForm v-if="schema" v-model="demand" :fields="schema.demandFields" />
              <AppFormField label="Quantity or people" hint="Use this only when the service is for more than one person or item."><input v-model.number="quantity" type="number" min="1" max="100" required @change="quote && refreshQuote()"></AppFormField>
              <div v-if="schema?.options?.length" class="booking-options"><h3>Options</h3><label v-for="option in schema.options" :key="String(option.id ?? option.optionKey)"><input type="checkbox" :checked="selectedOptions[String(option.optionKey ?? option.option_key)] === 'true'" @change="selectedOptions = {...selectedOptions, [String(option.optionKey ?? option.option_key)]: ($event.target as HTMLInputElement).checked ? 'true' : 'false'}">{{ option.label ?? option.optionKey }}</label></div>
            </section>
            <section v-else-if="currentStep === 2" class="booking-panel">
              <h3>Choose a date and time</h3>
              <BusinessAvailabilityCalendar :calendar="availabilityCalendar" :selected-date="bookingDate" :view="calendarView" @change-view="async view => { calendarView = view; await loadCalendar() }" @previous="moveCalendar(-1)" @next="moveCalendar(1)" @select="selectCalendarDate" />
              <AppFormField label="Date" required :hint="`Times are shown in ${page.timezone}.`"><input v-model="bookingDate" type="date" required @change="selectCalendarDate(bookingDate)"></AppFormField>
              <BusinessAvailabilityPicker :model-value="selectedSlot" :items="availability" @update:model-value="async value => { selectedSlot = value; await refreshQuote() }" />
              <AppStatus v-if="bookingError" :message="bookingError" tone="error" />
            </section>
            <section v-else-if="currentStep === 3" class="booking-panel">
              <h3>Review request</h3><BusinessQuoteSummary :quote="quote" /><p v-if="quote?.pricingState && quote.pricingState !== 'FIXED'" class="quote-guidance">This service requires a business quote or review; no fixed numeric price is shown.</p>
              <AppFormField label="Note" optional><textarea v-model="customerNote" maxlength="2000" placeholder="Anything the business should know?"></textarea></AppFormField>
              <AppStatus v-if="bookingError" :message="bookingError" tone="error" />
            </section>
            <footer class="booking-flow__actions"><AppButton type="button" tone="secondary" @click="currentStep === 1 ? resetFlow() : currentStep--">{{ currentStep === 1 ? 'Cancel' : 'Back' }}</AppButton><AppButton type="submit" tone="primary" :loading="saving">{{ currentStep === 3 ? (saving ? 'Sending' : 'Send request') : 'Continue' }}</AppButton></footer>
          </form>
        </section>
      </template>
      </template>
    </section>
  </TaskSurface>
</template>

<style scoped>
.public-business { display: grid; gap: var(--space-4); max-width: 70rem; }
h1, h2, h3, p { margin: 0; }
.eyebrow { color: var(--text-soft); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }
.public-business h1 { font-size: var(--text-size-page-title); letter-spacing: var(--tracking-tight); }
.public-business__back { justify-self: start; border: 0; background: transparent; color: var(--text-muted); cursor: pointer; font: inherit; padding: 0; }
.public-business__overview, .public-business__reviews { display: grid; gap: var(--space-3); max-width: 52rem; }
.public-business__description { color: var(--text-muted); line-height: 1.6; }
.public-business__muted, .offerings > div:first-child p, .booking-flow__header p, .booking-flow__guidance { color: var(--text-muted); }
.offerings, .booking-flow { display: grid; gap: var(--space-3); }
.offerings > div:first-child, .booking-flow__header { display: flex; align-items: end; justify-content: space-between; gap: var(--space-3); }
.offerings > div:nth-child(2) { overflow: hidden; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.public-business__details { border-top: 1px solid var(--border-subtle); padding-top: var(--space-2); }
.public-business__details summary { color: var(--text-muted); cursor: pointer; font-weight: var(--text-weight-semibold); }
.public-business__details-body { display: grid; gap: var(--space-2); padding-top: var(--space-2); color: var(--text-muted); }
.public-business__gallery { display: grid; grid-template-columns: repeat(auto-fit, minmax(8rem, 1fr)); gap: var(--space-2); }
.public-business__gallery img { width: 100%; aspect-ratio: 4 / 3; border-radius: var(--radius-control); background: var(--surface-sunken); object-fit: cover; }
.booking-flow { padding: var(--space-4); border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.booking-flow__guidance { font-size: var(--text-size-meta); }
.booking-panel { display: grid; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-raised); }
.booking-panel textarea, .booking-panel input { box-sizing: border-box; width: 100%; border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-2); background: var(--control-bg); color: var(--control-ink); font: inherit; }
.booking-options { display: grid; gap: var(--space-2); }
.booking-options label { display: flex; align-items: center; gap: var(--space-2); }
.booking-flow__actions, .booking-completion__actions { display: flex; justify-content: space-between; gap: var(--space-2); flex-wrap: wrap; padding-top: var(--space-2); }
.booking-completion { display: grid; gap: var(--space-3); max-width: 42rem; padding: var(--space-4); border: 1px solid color-mix(in srgb, var(--success) 50%, var(--border-subtle)); border-radius: var(--radius-surface); background: var(--surface-base); }
.booking-completion > p:not(.eyebrow) { color: var(--text-muted); line-height: 1.5; }
.booking-completion dl { display: grid; gap: var(--space-2); margin: 0; }
.booking-completion dl div { display: flex; justify-content: space-between; gap: var(--space-3); padding-top: var(--space-2); border-top: 1px solid var(--border-subtle); }
.booking-completion dt { color: var(--text-muted); }
.booking-completion dd { margin: 0; font-weight: var(--text-weight-semibold); }
.booking-completion__note { font-size: var(--text-size-meta); }
@media (max-width: 640px) { .offerings > div:first-child, .booking-flow__header { align-items: start; flex-direction: column; } .booking-flow { padding: var(--space-3); } }
</style>
