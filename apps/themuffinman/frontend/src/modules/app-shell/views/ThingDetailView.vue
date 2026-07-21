<script setup lang="ts">
import {onMounted, ref} from "vue"
import {useRoute} from "vue-router"
import type {ThingListingResponseDTO} from "../../../contracts/index.ts"
import {thingsApi} from "../../things/api/thingsApi.ts"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import DetailSurface from "../components/DetailSurface.vue"

const route = useRoute()
const userShellApi = {getThingListing: thingsApi.getListing, requestThingBorrow: thingsApi.requestBorrow}
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
    <div v-else-if="error" class="status status--error" role="alert">{{ error }} <AppButton type="button" tone="quiet" @click="load">Retry</AppButton></div>
    <article v-else-if="listing" class="detail-card" aria-label="Thing narrative and permitted borrowing action">
      <p class="eyebrow">Thing listing</p>
      <h1>{{ listing.title }}</h1>
      <p class="owner">Offered by {{ listing.ownerUsername }}</p>
      <DetailSurface title="Thing detail" utility-label="Thing properties"><p class="description">{{ listing.description || "No description yet." }}</p><section class="thing-detail__activity"><h2>Current availability</h2><p>{{ listing.availabilityLabel }}</p></section><template #utility><div class="thing-detail__properties"><p class="eyebrow">Properties</p><dl><div><dt>Condition</dt><dd>{{ listing.conditionNote || "Not specified" }}</dd></div><div><dt>Listed</dt><dd>{{ new Date(listing.createdAt).toLocaleDateString(undefined, {dateStyle: "medium"}) }}</dd></div></dl></div></template></DetailSurface>
      <p v-if="feedback" class="status status--success" role="status">{{ feedback }}</p>
      <div v-if="listing.myPendingRequestId" class="pending">Your borrow request is pending.</div>
      <form v-else-if="listing.allowedActions.includes('REQUEST_BORROW')" class="request-form" @submit.prevent="requestBorrow">
        <h2>Request to borrow</h2>
        <AppFormField label="Message" optional hint="Add context for the owner."><textarea v-model="message" maxlength="1000" placeholder="Add a note for the owner."></textarea></AppFormField>
        <AppFormFooter><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">Send request</AppButton></template></AppFormFooter>
      </form>
      <p v-else class="pending">This thing is currently unavailable.</p>
    </article>
  </section>
</template>

<style scoped>
.thing-detail { display:grid; gap:var(--space-3); max-width:none; }
.back { color:var(--text-muted); font-size:var(--text-size-body); }
.back:hover { color:var(--text); }
.detail-card { display:grid; gap:var(--space-3); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.eyebrow { margin:0; color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.owner,.pending { margin:0; color:var(--text-muted); }
.description { margin:0; color:var(--text); white-space:pre-wrap; }
.request-form { display:grid; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-3); }
.request-form h2 { margin:0; color:var(--text); font-size:var(--text-size-title); }
.request-form textarea { min-height:6rem; border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; resize:vertical; }
.request-form textarea:focus-visible { border-color:var(--control-border-active); outline:2px solid var(--focus-ring); outline-offset:2px; }
.status { padding:var(--space-2) 0; color:var(--text-muted); }
.status--error { color:var(--danger); }
.status--success { color:var(--success); }
.status button { border:0; background:transparent; color:inherit; text-decoration:underline; cursor:pointer; }
.thing-detail__activity { margin-top:var(--space-3); border-top:1px solid var(--border-subtle); padding-top:var(--space-3); }
.thing-detail__activity h2 { margin:0 0 var(--space-1); color:var(--text); font-size:var(--text-size-title); }
.thing-detail__activity p { margin:0; color:var(--text-muted); }
.thing-detail__properties { display:grid; gap:var(--space-2); padding:var(--space-3); }
.thing-detail__properties dl { display:grid; gap:var(--space-2); margin:0; }
.thing-detail__properties dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.thing-detail__properties dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.thing-detail__properties dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); text-align:right; }
@media(max-width:700px) { .thing-detail__properties { padding:var(--space-2) 0; } }
</style>
<style scoped>
.status--error .app-button { min-height: auto; margin-left: var(--space-1); padding: 0; border: 0; background: transparent; color: inherit; text-decoration: underline; }
</style>
