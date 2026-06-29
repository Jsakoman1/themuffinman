<script setup lang="ts">
import {onMounted, reactive, ref} from "vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {ridesApi, type RideOffer, type RideOfferRequest} from "../api/ridesApi.ts"

const visibleOffers = ref<RideOffer[]>([])
const myOffers = ref<RideOffer[]>([])
const loading = ref(true)
const saving = ref(false)
const errorMessage = ref("")
const statusMessage = ref("")

const form = reactive<RideOfferRequest>({
  origin: "",
  destination: "",
  departureAt: "",
  seats: 1,
  note: "",
  active: true,
  visibleCircleIds: [],
})

const formatDeparture = (value: string) => new Intl.DateTimeFormat(undefined, {
  dateStyle: "medium",
  timeStyle: "short",
}).format(new Date(value))

const loadRides = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const [visible, mine] = await Promise.all([
      ridesApi.getVisibleOffers(),
      ridesApi.getMyOffers(),
    ])
    visibleOffers.value = visible.items
    myOffers.value = mine.items
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Ride sharing could not be loaded.")
  } finally {
    loading.value = false
  }
}

const createOffer = async () => {
  saving.value = true
  errorMessage.value = ""
  statusMessage.value = ""
  try {
    await ridesApi.createOffer({
      ...form,
      departureAt: new Date(form.departureAt).toISOString(),
      note: form.note || null,
      active: form.active,
      visibleCircleIds: form.visibleCircleIds ?? [],
    })
    form.origin = ""
    form.destination = ""
    form.departureAt = ""
    form.seats = 1
    form.note = ""
    form.active = true
    statusMessage.value = "Ride offer created."
    await loadRides()
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Ride offer could not be saved.")
  } finally {
    saving.value = false
  }
}

onMounted(loadRides)
</script>

<template>
  <UiAppShellPage page-class="rides-page" eyebrow="Car Sharing" title="Ride offers">
    <UiRequestError
      v-if="errorMessage"
      :message="errorMessage"
      :details="[errorMessage]"
      summary="Ride sharing error details"
      :copied="false"
    />
    <div v-if="statusMessage" class="alert alert--success">{{ statusMessage }}</div>

    <UiWorkspace variant="detail">
      <UiSurfaceSection title="Available rides" subtitle="Voluntary rides visible to you">
        <div v-if="loading" class="empty-state empty-state--compact">Loading rides.</div>
        <div v-else-if="visibleOffers.length === 0" class="empty-state empty-state--compact">No active rides are available.</div>
        <div v-else class="rides-list">
          <article v-for="offer in visibleOffers" :key="offer.id" class="rides-list__item">
            <div>
              <h3>{{ offer.origin }} to {{ offer.destination }}</h3>
              <p class="muted">{{ formatDeparture(offer.departureAt) }} by {{ offer.driverUsername }}</p>
              <p class="muted">{{ offer.seats }} seats</p>
              <p v-if="offer.visibleCircleNames.length" class="muted">Circles: {{ offer.visibleCircleNames.join(", ") }}</p>
              <div v-if="offer.note" class="rides-list__note" v-html="offer.note"></div>
            </div>
          </article>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="My rides" subtitle="Ride offers I created">
        <div v-if="myOffers.length === 0" class="empty-state empty-state--compact">You have not offered a ride yet.</div>
        <div v-else class="rides-list rides-list--compact">
          <article v-for="offer in myOffers" :key="offer.id" class="rides-list__item">
            <strong>{{ offer.origin }} to {{ offer.destination }}</strong>
            <span class="muted">{{ offer.active ? "Active" : "Hidden" }}</span>
          </article>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="Offer a ride" subtitle="Create a voluntary ride offer">
        <form class="rides-form" @submit.prevent="createOffer">
          <label class="field">
            <span>Origin</span>
            <input v-model="form.origin" class="input" required maxlength="140" />
          </label>
          <label class="field">
            <span>Destination</span>
            <input v-model="form.destination" class="input" required maxlength="140" />
          </label>
          <label class="field">
            <span>Departure</span>
            <input v-model="form.departureAt" class="input" required type="datetime-local" />
          </label>
          <label class="field">
            <span>Seats</span>
            <input v-model.number="form.seats" class="input" required type="number" min="1" max="8" />
          </label>
          <label class="field">
            <span>Note</span>
            <textarea v-model="form.note" class="input rides-form__textarea" maxlength="1000"></textarea>
          </label>
          <label class="checkbox-row">
            <input v-model="form.active" type="checkbox" />
            <span>Visible</span>
          </label>
          <button class="button button--primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create ride" }}
          </button>
        </form>
      </UiSurfaceSection>
    </UiWorkspace>
  </UiAppShellPage>
</template>
