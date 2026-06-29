<script setup lang="ts">
import {onMounted, reactive, ref} from "vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {thingsApi, type ThingListing, type ThingListingRequest} from "../api/thingsApi.ts"

const listings = ref<ThingListing[]>([])
const myListings = ref<ThingListing[]>([])
const loading = ref(true)
const saving = ref(false)
const requestingId = ref<number | null>(null)
const errorMessage = ref("")
const statusMessage = ref("")

const form = reactive<ThingListingRequest>({
  title: "",
  description: "",
  conditionNote: "",
  available: true,
})

const loadThings = async () => {
  loading.value = true
  errorMessage.value = ""
  try {
    const [available, mine] = await Promise.all([
      thingsApi.getAvailableListings(),
      thingsApi.getMyListings(),
    ])
    listings.value = available.items
    myListings.value = mine.items
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Thing Sharing could not be loaded.")
  } finally {
    loading.value = false
  }
}

const createListing = async () => {
  saving.value = true
  errorMessage.value = ""
  statusMessage.value = ""
  try {
    await thingsApi.createListing({
      ...form,
      description: form.description || null,
      conditionNote: form.conditionNote || null,
      available: form.available,
    })
    form.title = ""
    form.description = ""
    form.conditionNote = ""
    form.available = true
    statusMessage.value = "Thing listing created."
    await loadThings()
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Thing listing could not be saved.")
  } finally {
    saving.value = false
  }
}

const requestBorrow = async (listing: ThingListing) => {
  requestingId.value = listing.id
  errorMessage.value = ""
  statusMessage.value = ""
  try {
    await thingsApi.requestBorrow(listing.id, {message: `I would like to borrow ${listing.title}.`})
    statusMessage.value = "Borrow request sent."
    await loadThings()
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, "Borrow request could not be sent.")
  } finally {
    requestingId.value = null
  }
}

onMounted(loadThings)
</script>

<template>
  <UiAppShellPage page-class="things-page" eyebrow="Thing Sharing" title="Lend and borrow">
    <UiRequestError
      v-if="errorMessage"
      :message="errorMessage"
      :details="[errorMessage]"
      summary="Thing Sharing error details"
      :copied="false"
    />
    <div v-if="statusMessage" class="alert alert--success">{{ statusMessage }}</div>

    <UiWorkspace variant="detail">
      <UiSurfaceSection title="Available things" subtitle="Items people are willing to lend">
        <div v-if="loading" class="empty-state empty-state--compact">Loading things.</div>
        <div v-else-if="listings.length === 0" class="empty-state empty-state--compact">No things are available yet.</div>
        <div v-else class="things-list">
          <article v-for="listing in listings" :key="listing.id" class="things-list__item">
            <div>
              <h3>{{ listing.title }}</h3>
              <p class="muted">{{ listing.ownerUsername }}</p>
              <p v-if="listing.conditionNote" class="muted">{{ listing.conditionNote }}</p>
              <div v-if="listing.description" class="things-list__description" v-html="listing.description"></div>
            </div>
            <button
              class="button button--secondary"
              type="button"
              :disabled="!!listing.myPendingRequestId || requestingId === listing.id"
              @click="requestBorrow(listing)"
            >
              {{ listing.myPendingRequestId ? "Requested" : requestingId === listing.id ? "Sending..." : "Request" }}
            </button>
          </article>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="My listings" subtitle="Things I can lend">
        <div v-if="myListings.length === 0" class="empty-state empty-state--compact">You have not listed anything yet.</div>
        <div v-else class="things-list things-list--compact">
          <article v-for="listing in myListings" :key="listing.id" class="things-list__item">
            <strong>{{ listing.title }}</strong>
            <span class="muted">{{ listing.available ? "Available" : "Hidden" }}</span>
          </article>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="Add a thing" subtitle="Create a lending listing">
        <form class="things-form" @submit.prevent="createListing">
          <label class="field">
            <span>Title</span>
            <input v-model="form.title" class="input" required maxlength="140" />
          </label>
          <label class="field">
            <span>Condition</span>
            <input v-model="form.conditionNote" class="input" maxlength="180" />
          </label>
          <label class="field">
            <span>Description</span>
            <textarea v-model="form.description" class="input things-form__textarea" maxlength="2000"></textarea>
          </label>
          <label class="checkbox-row">
            <input v-model="form.available" type="checkbox" />
            <span>Available to request</span>
          </label>
          <button class="button button--primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create listing" }}
          </button>
        </form>
      </UiSurfaceSection>
    </UiWorkspace>
  </UiAppShellPage>
</template>
