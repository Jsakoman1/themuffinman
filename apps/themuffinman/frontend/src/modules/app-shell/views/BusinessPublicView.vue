<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
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
import SurfaceRow from "../components/SurfaceRow.vue"
import TaskSurface from "../components/TaskSurface.vue"

const route = useRoute(); const router = useRouter()
const page = ref<BusinessPublicPageDTO | null>(null); const selectedOfferingId = ref<number | null>(null); const schema = ref<BusinessOfferingSchemaDTO | null>(null); const quote = ref<BusinessPublicQuoteDTO | null>(null); const availability = ref<BusinessAvailabilityWindowDTO[]>([]); const demand = ref<Record<string, string>>({}); const selectedOptions = ref<Record<string, string>>({}); const startsAt = ref(""); const selectedSlot = ref(""); const customerNote = ref(""); const quantity = ref(1); const uploads = ref<File[]>([]); const recurring = ref(false); const recurrenceCount = ref(4); const recurrenceNote = computed(() => recurring.value ? `Requested recurring series: ${recurrenceCount.value} occurrences. Each occurrence is revalidated by the business before confirmation.` : ""); const bookingForOther = ref(false); const recipientName = ref(""); const recipientConsent = ref(false); const recipientNote = computed(() => bookingForOther.value ? `Booking for: ${recipientName.value || "recipient not named"}. Recipient consent confirmed: ${recipientConsent.value ? "yes" : "no"}.` : ""); const currentStep = ref(0); const loading = ref(true); const saving = ref(false); const error = ref(""); const bookingError = ref(""); const feedback = ref("")
const steps = ["Service", "Details", "Time", "Review"]
const selectedOffering = computed(() => page.value?.offerings.find(item => item.id === selectedOfferingId.value) ?? null)
const isFavorite = ref(false)
const favoriteBusy = ref(false)
const scrollToServices = () => document.getElementById("business-services")?.scrollIntoView({behavior: "smooth", block: "start"})
const returnTo = computed(() => typeof route.query.returnTo === "string" ? route.query.returnTo : "/business/find")
const load = async () => { loading.value = true; error.value = ""; page.value = null; try { page.value = await userShellApi.getPublicBusinessPage(String(route.params.slug)); isFavorite.value = (await userShellApi.getBusinessFavorites()).some(item => item.businessProfileId === page.value?.businessProfileId) } catch { error.value = "Could not load this business." } finally { loading.value = false } }
const toggleFavorite = async () => { if (!page.value) return; favoriteBusy.value = true; try { if (isFavorite.value) await userShellApi.removeBusinessFavorite(page.value.businessProfileId); else await userShellApi.addBusinessFavorite(page.value.businessProfileId); isFavorite.value = !isFavorite.value } catch { feedback.value = "Could not update favorites. Try again." } finally { favoriteBusy.value = false } }
const selectOffering = async (id: number) => { selectedOfferingId.value = id; currentStep.value = 1; demand.value = {}; selectedOptions.value = {}; quantity.value = 1; uploads.value = []; quote.value = null; availability.value = []; selectedSlot.value = ""; bookingError.value = ""; schema.value = await userShellApi.getPublicServiceSchema(String(route.params.slug), id) }
const resetFlow = () => { selectedOfferingId.value = null; currentStep.value = 0; schema.value = null; quote.value = null; availability.value = []; demand.value = {}; selectedOptions.value = {}; selectedSlot.value = ""; startsAt.value = ""; customerNote.value = ""; quantity.value = 1; uploads.value = []; recurring.value = false; recurrenceCount.value = 4; bookingForOther.value = false; recipientName.value = ""; recipientConsent.value = false }
const selectUploads = (event: Event) => { const selected = Array.from((event.target as HTMLInputElement).files ?? []); const valid = selected.filter(file => file.size <= 10 * 1024 * 1024 && ["image/", "application/pdf", "text/"].some(prefix => file.type.startsWith(prefix))); uploads.value = valid; demand.value = {...demand.value, uploadedFileNames: valid.map(file => file.name).join(", ")}; if (valid.length !== selected.length) feedback.value = "Some files were skipped. Use images, PDF, or text files up to 10 MB each." }
const refreshBookingContext = async () => { if (!selectedOfferingId.value || !startsAt.value) return; const start = new Date(startsAt.value).toISOString(); quote.value = await userShellApi.quotePublicService(String(route.params.slug), {businessOfferingId: selectedOfferingId.value, startsAt: start, quantity: quantity.value, answers: demand.value, selectedOptions: selectedOptions.value}); const from = new Date(new Date(start).getTime() - 86400000).toISOString(); const to = new Date(new Date(start).getTime() + 86400000).toISOString(); availability.value = (await userShellApi.getPublicAvailability(String(route.params.slug), selectedOfferingId.value, from, to)).items }
const goNext = async () => { bookingError.value = ""; if (currentStep.value === 2) await refreshBookingContext(); if (currentStep.value < 3) currentStep.value++ }
const book = async () => { if (!page.value || !selectedOfferingId.value || !(selectedSlot.value || startsAt.value) || (bookingForOther.value && (!recipientName.value.trim() || !recipientConsent.value))) return; saving.value = true; bookingError.value = ""; try { const start = selectedSlot.value || new Date(startsAt.value).toISOString(); const preview = await userShellApi.previewPublicBooking(String(route.params.slug), {businessOfferingId: selectedOfferingId.value, startsAt: start}); const request: BusinessBookingRequestDTO = {businessOfferingId: preview.businessOfferingId, startsAt: preview.startsAt, endsAt: preview.endsAt, customerNote: [customerNote.value, recurrenceNote.value, recipientNote.value].filter(Boolean).join("\n"), idempotencyKey: crypto.randomUUID(), quantity: quote.value?.quantity ?? quantity.value, answers: demand.value, selectedOptions: selectedOptions.value}; await userShellApi.createCustomerBooking(request); feedback.value = recurring.value ? "Recurring booking request sent for business review." : "Booking request sent."; resetFlow() } catch { bookingError.value = "Could not create this booking. Availability may have changed; return to Time and choose another slot." } finally { saving.value = false } }
watch(() => route.params.slug, () => { resetFlow(); feedback.value = ""; bookingError.value = ""; void load() })
onMounted(() => void load())
</script>

<template>
  <TaskSurface mode="choose" label="Public business booking">
    <section class="public-business" data-booking-model="progressive-availability-first" data-business-page-model="hero-services-trust-book" data-mental-model="discover-select-service-choose-time-review" data-trust-cue="public-business-visibility-and-booking-terms" aria-label="Public business booking">
      <AppStatus v-if="loading" message="Loading business." busy />
      <AppStatus v-else-if="error && !page" :message="error" tone="error" retry @retry="load" />
      <template v-else-if="page">
        <button class="public-business__back" type="button" @click="router.push(returnTo)">‹ Back to results</button>
        <header class="public-business__hero">
          <div class="public-business__hero-copy">
            <p class="eyebrow">Business</p>
            <h1>{{ page.businessName }}</h1>
            <p class="public-business__headline">{{ page.headline || 'A trusted local service, ready when you are.' }}</p>
            <div class="public-business__hero-actions">
              <AppButton type="button" tone="primary" :disabled="!page.offerings.length" @click="scrollToServices">Book a service</AppButton>
              <AppButton type="button" tone="quiet" :loading="favoriteBusy" :aria-pressed="isFavorite" @click="toggleFavorite">{{ isFavorite ? "Saved" : "Save business" }}</AppButton>
            </div>
            <p class="public-business__availability"><span class="status-dot" aria-hidden="true"></span>{{ page.bookingEnabled ? 'Booking available' : 'Profile only' }}<span v-if="page.publicAddressLabel"> · {{ page.publicAddressLabel }}</span></p>
          </div>
          <img v-if="page.heroImageUrl" class="public-business__hero-image" :src="page.heroImageUrl" :alt="`${page.businessName} cover`" />
          <div v-else class="public-business__hero-placeholder" aria-hidden="true">{{ page.businessName.slice(0, 1) }}</div>
        </header>
        <section class="public-business__trust" aria-label="Business information summary">
          <div><strong>{{ page.offerings.length }}</strong><span>services</span></div>
          <div><strong>{{ page.timezone }}</strong><span>local time zone</span></div>
          <div v-if="page.publicAddressLabel"><strong>{{ page.publicAddressLabel }}</strong><span>service area</span></div>
        </section>
        <details class="public-business__details">
          <summary>About, gallery and contact</summary>
          <div class="public-business__details-body">
            <p>{{ page.description || 'This business has not added a longer description yet.' }}</p>
            <div v-if="page.galleryImages.length" class="public-business__gallery" aria-label="Business gallery">
              <img v-for="image in page.galleryImages" :key="image.id" :src="image.imageUrl" :alt="image.altText || `${page.businessName} gallery image`" loading="lazy" />
            </div>
            <p v-if="page.contactEmail || page.contactPhone"><strong>Contact:</strong> {{ page.contactEmail || page.contactPhone }}</p>
          </div>
        </details>
        <AppStatus v-if="feedback" :message="feedback" tone="success" />
        <section v-if="!selectedOfferingId" id="business-services" class="offerings" data-primary-surface="service-selection">
          <div>
            <p class="eyebrow">Services</p><h2>What can we help with?</h2>
            <p>Select one service. We will show only the details, price and times relevant to it.</p>
          </div>
          <div v-if="page.offerings.length">
              <SurfaceRow v-for="offering in page.offerings" :key="offering.id" :row="{id: String(offering.id), title: offering.title, description: offering.summary || 'Service details', meta: `${offering.pricingType === 'CUSTOM_QUOTE' ? 'Request a quote' : offering.pricingType === 'FREE' ? 'Free' : `${offering.pricingType === 'FROM' ? 'From ' : ''}${offering.basePriceAmount} ${offering.basePriceCurrency}`} · ${offering.defaultDurationMinutes} min${offering.slotCapacity > 1 ? ` · up to ${offering.slotCapacity}` : ''}`}" data-service-row>
              <template #actions><AppButton type="button" tone="primary" @click="selectOffering(offering.id)">Book</AppButton></template>
            </SurfaceRow>
          </div>
          <AppEmptyState v-else title="No services available" message="This business has not published a bookable service yet." />
        </section>
        <section v-else class="booking-flow native-group">
          <header class="booking-flow__header" :data-booking-step="steps[currentStep].toLowerCase().replaceAll(' ', '-')">
            <div><p class="eyebrow">Booking</p><h2>{{ selectedOffering?.title }}</h2><p>{{ selectedOffering?.summary || 'Configure your request step by step.' }}</p></div>
            <AppButton type="button" tone="quiet" @click="resetFlow">Change service</AppButton>
          </header>
          <BusinessBookingStepper :steps="steps" :current="currentStep" @select="currentStep = $event" />
          <p class="booking-flow__guidance">{{ currentStep === 1 ? 'Answer only the questions needed for this service.' : currentStep === 2 ? 'Choose a slot that is currently available.' : currentStep === 3 ? 'Review the calculated price and terms before sending.' : 'Select a service to begin.' }}</p>
          <form @submit.prevent="currentStep === 3 ? book() : goNext()">
            <section v-if="currentStep === 1" class="booking-panel">
              <h3>Customer details</h3>
              <BusinessDemandForm v-if="schema" v-model="demand" :fields="schema.demandFields" />
              <AppFormField label="Quantity or people" hint="The backend quote recalculates price, capacity, and resource allocation for this quantity."><input v-model.number="quantity" type="number" min="1" max="100" required @change="quote && refreshBookingContext()"></AppFormField>
              <label class="upload-panel">Optional service files<input type="file" multiple accept="image/*,.pdf,.txt" @change="selectUploads"><small>Files are attached to the request context. Maximum 10 MB per file.</small><span v-if="uploads.length">{{ uploads.map(file => file.name).join(', ') }}</span></label>
              <label class="booking-toggle"><input v-model="bookingForOther" type="checkbox"> I am booking for another person</label>
              <div v-if="bookingForOther" class="recipient-panel"><AppFormField label="Recipient name" required><input v-model="recipientName" type="text" maxlength="120" required></AppFormField><label class="booking-toggle"><input v-model="recipientConsent" type="checkbox"> I have the recipient's consent to share this booking context</label><p>The recipient name and confirmation responsibility are recorded in the booking note and remain subject to the business privacy rules.</p></div>
              <div v-if="schema?.options?.length" class="booking-options"><h3>Options</h3><label v-for="option in schema.options" :key="String(option.id ?? option.optionKey)"><input type="checkbox" :checked="selectedOptions[String(option.optionKey ?? option.option_key)] === 'true'" @change="selectedOptions = {...selectedOptions, [String(option.optionKey ?? option.option_key)]: ($event.target as HTMLInputElement).checked ? 'true' : 'false'}">{{ option.label ?? option.optionKey }}</label></div>
            </section>
            <section v-else-if="currentStep === 2" class="booking-panel">
              <h3>Choose a time</h3>
              <AppFormField label="Start date and time" required><input v-model="startsAt" type="datetime-local" required @change="refreshBookingContext"></AppFormField>
              <BusinessAvailabilityPicker v-model="selectedSlot" :items="availability" />
              <label class="booking-toggle"><input v-model="recurring" type="checkbox"> Request a recurring series</label>
              <div v-if="recurring" class="recurrence-panel"><label>Occurrences<select v-model.number="recurrenceCount"><option :value="2">2</option><option :value="4">4</option><option :value="8">8</option></select></label><p>Every occurrence is checked for conflicts by the backend. The business can approve or reject the series; cancellation follows the business terms.</p></div>
            </section>
            <section v-else-if="currentStep === 3" class="booking-panel">
              <h3>Review request</h3><BusinessQuoteSummary :quote="quote" /><p v-if="quote?.pricingState && quote.pricingState !== 'FIXED'" class="quote-guidance">This service requires a business quote or review; no fixed numeric price is shown.</p><p v-if="uploads.length" class="quote-guidance">{{ uploads.length }} file(s) will be included in the request context.</p>
              <AppFormField label="Note" optional><textarea v-model="customerNote" maxlength="2000" placeholder="Anything the business should know?"></textarea></AppFormField>
              <AppStatus v-if="bookingError" :message="bookingError" tone="error" />
            </section>
            <footer class="booking-flow__actions"><AppButton type="button" tone="secondary" @click="currentStep === 1 ? resetFlow() : currentStep--">{{ currentStep === 1 ? 'Cancel' : 'Back' }}</AppButton><AppButton type="submit" tone="primary" :loading="saving">{{ currentStep === 3 ? (saving ? 'Sending' : 'Send request') : 'Continue' }}</AppButton></footer>
          </form>
        </section>
      </template>
    </section>
  </TaskSurface>
</template>

<style scoped>.public-business{display:grid;gap:var(--space-4);max-width:70rem}.public-business__header{display:grid;gap:var(--space-1)}.eyebrow{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1,h2,h3,p{margin:0}.public-business h1{font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.public-business__header p:last-child,.offerings>div:first-child p,.booking-flow__header p{color:var(--text-muted)}.public-business__details{margin-top:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.public-business__details summary{cursor:pointer;color:var(--text-muted);font-weight:var(--text-weight-semibold)}.public-business__details-body{display:grid;gap:var(--space-2);padding-top:var(--space-2);color:var(--text-muted)}.offerings,.booking-flow{display:grid;gap:var(--space-3)}.offerings>div:first-child,.booking-flow__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.offerings>div:nth-child(2){overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.booking-flow{padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.booking-flow__guidance{color:var(--text-muted);font-size:var(--text-size-meta)}.booking-panel{display:grid;gap:var(--space-3);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-raised)}.booking-panel textarea,.booking-panel input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.booking-options{display:grid;gap:var(--space-2)}.booking-options label{display:flex;gap:var(--space-2);align-items:center}.booking-flow__actions{display:flex;justify-content:space-between;gap:var(--space-2);padding-top:var(--space-2)}@media(max-width:640px){.offerings>div:first-child,.booking-flow__header{align-items:start;flex-direction:column}.booking-flow{padding:var(--space-3)}}
</style>
<style scoped>
.public-business__back{justify-self:start;border:0;background:transparent;color:var(--text-muted);font:inherit;cursor:pointer;padding:0}.public-business__hero{display:grid;grid-template-columns:minmax(0,1fr) minmax(12rem,24rem);gap:var(--space-5);align-items:center;padding:clamp(1.25rem,4vw,3rem);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:linear-gradient(135deg,var(--surface-base),var(--surface-raised))}.public-business__hero-copy{display:grid;gap:var(--space-2)}.public-business__headline{max-width:42rem;color:var(--text-muted);font-size:clamp(1.1rem,2vw,1.35rem);line-height:1.45}.public-business__hero-actions{display:flex;align-items:center;gap:var(--space-2);flex-wrap:wrap;margin-top:var(--space-2)}.public-business__availability{display:flex;align-items:center;gap:var(--space-1);color:var(--text-muted);font-size:var(--text-size-meta)}.status-dot{width:.5rem;height:.5rem;border-radius:50%;background:var(--success)}.public-business__hero-image,.public-business__hero-placeholder{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:var(--radius-control);background:var(--surface-sunken)}.public-business__hero-placeholder{display:grid;place-items:center;color:var(--text-soft);font-size:clamp(3rem,8vw,6rem);font-weight:var(--text-weight-semibold)}.public-business__trust{display:flex;gap:var(--space-5);flex-wrap:wrap;padding:0 var(--space-1);color:var(--text-muted)}.public-business__trust div{display:grid;gap:.15rem}.public-business__trust strong{color:var(--text);font-size:var(--text-size-title)}.public-business__trust span{font-size:var(--text-size-meta)}.public-business__gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(8rem,1fr));gap:var(--space-2)}.public-business__gallery img{width:100%;aspect-ratio:4/3;object-fit:cover;border-radius:var(--radius-control);background:var(--surface-sunken)}#business-services{scroll-margin-top:var(--workspace-header-height)}@media(max-width:700px){.public-business__hero{grid-template-columns:1fr;gap:var(--space-3);padding:var(--space-4)}.public-business__hero-image,.public-business__hero-placeholder{order:-1;max-height:14rem}.public-business__trust{gap:var(--space-3);justify-content:space-between}}
</style>
