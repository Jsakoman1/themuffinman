<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {useRoute} from "vue-router"
import {formatDate} from "../../../services/formatters.ts"
import type {ThingListingResponseDTO} from "../../../contracts/index.ts"
import {thingsApi, type ThingWishlistItem} from "../../things/api/thingsApi.ts"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import DetailSurface from "../components/DetailSurface.vue"
// Full item details keep properties and actions in one reading flow.
import DetailSurfaceHeader from "../components/DetailSurfaceHeader.vue"
import AppStatus from "../components/AppStatus.vue"
import ClientActionList from "../components/ClientActionList.vue"

const route = useRoute()
const userShellApi = {getThingListing: thingsApi.getListing, requestThingBorrow: thingsApi.requestBorrow}
const listing = ref<ThingListingResponseDTO | null>(null)
const message = ref("")
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const wishlistItem = ref<ThingWishlistItem | null>(null)
const sharedCircleIds = ref("")
const requestFormOpen = ref(false)
const listingId = computed(() => Number(route.params.listingId))

const load = async () => {
  isLoading.value = true
  error.value = ""
  try {
    listing.value = await userShellApi.getThingListing(listingId.value)
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
    requestFormOpen.value = false
    feedback.value = "Borrow request sent."
  } catch {
    error.value = "Could not request this thing."
  } finally {
    isSaving.value = false
  }
}
const executeListingAction = (actionId: string) => { if (actionId === "REQUEST_BORROW") requestFormOpen.value = true }
const loadWishlist = async () => { wishlistItem.value = (await thingsApi.getWishlist()).find(item => item.listingId === listingId.value) ?? null; sharedCircleIds.value = wishlistItem.value?.sharedCircleIds.join(", ") ?? "" }
const toggleWishlist = async () => {
  if (!listing.value) return
  if (wishlistItem.value) { await thingsApi.removeWishlist(listingId.value); wishlistItem.value = null; sharedCircleIds.value = ""; feedback.value = "Removed from wishlist."; return }
  const item = {listingId: listingId.value, title: listing.value.title, sharedCircleIds: sharedCircleIds.value.split(",").map(value => Number(value.trim())).filter(Number.isInteger), savedAt: new Date().toISOString()}
  wishlistItem.value = await thingsApi.saveWishlist(item); feedback.value = "Saved to wishlist and shared with the selected circles."
}

watch(listingId, () => { message.value = ""; feedback.value = ""; void load(); void loadWishlist() })
onMounted(() => { void load(); void loadWishlist() })
</script>
<style scoped>
.thing-detail__activity,.borrow-timeline{border-top-color:var(--orientation-line)}

.thing-detail { display:grid; gap:var(--space-3); max-width:none; }
.back { color:var(--text-muted); font-size:var(--text-size-body); }
.back:hover { color:var(--text); }
.detail-card { display:grid; gap:var(--space-3); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.eyebrow { margin:0; color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.owner,.pending { margin:0; color:var(--text-muted); }
.wishlist { display:flex; gap:var(--space-2); align-items:center; flex-wrap:wrap; }
.wishlist input { min-width:16rem; border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; }
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
.borrow-timeline{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.borrow-timeline ol{display:grid;gap:var(--space-1);margin:0;padding-left:1.25rem;color:var(--text-muted);font-size:var(--text-size-meta)}.borrow-timeline__active{color:var(--text);font-weight:var(--text-weight-semibold)}.borrow-timeline__note{margin:0;color:var(--text-soft);font-size:var(--text-size-meta)}
@media(max-width:700px) { .thing-detail__properties { padding:var(--space-2) 0; } }

.status--error .app-button { min-height: auto; margin-left: var(--space-1); padding: 0; border: 0; background: transparent; color: inherit; text-decoration: underline; }
</style>

<template>
  <section class="thing-detail" data-form-model="need-first-request" data-typography-model="shared-content-hierarchy">
    <AppStatus v-if="isLoading" message="Loading thing." busy />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <article v-else-if="listing" class="detail-card" aria-label="Thing narrative and permitted borrowing action">
      <DetailSurfaceHeader eyebrow="Thing listing" :title="listing.title" back-to="/things" back-label="Back to things" aria-label="Thing detail header" />
      <p class="owner" data-surface="need-first-thing-detail">Offered by {{ listing.ownerUsername }} · {{ listing.availabilityLabel || (listing.available ? "Available now" : "Unavailable") }}</p><div class="wishlist" data-wishlist-surface="shareable"><AppButton type="button" tone="secondary" @click="toggleWishlist">{{ wishlistItem ? "Remove from wishlist" : "Save to wishlist" }}</AppButton><input v-model="sharedCircleIds" aria-label="Circle IDs to share wishlist item with" placeholder="Circle IDs to share (optional)"></div>
      <DetailSurface title="Thing detail" utility-label="Thing properties"><p class="description">{{ listing.description || "No description yet." }}</p><section class="thing-detail__activity"><h2>Current availability</h2><p>{{ listing.availabilityLabel }}</p></section><template #utility><div class="thing-detail__properties"><p class="eyebrow">Properties</p><dl><div><dt>Condition</dt><dd>{{ listing.conditionNote || "Not specified" }}</dd></div><div><dt>Listed</dt><dd>{{ formatDate(listing.createdAt) }}</dd></div></dl></div></template></DetailSurface>
      <AppStatus v-if="feedback" :message="feedback" tone="success" />
      <section class="borrow-timeline" aria-label="Borrow request timeline"><p class="eyebrow">Borrow flow</p><ol><li class="borrow-timeline__active">Inspect availability and condition</li><li :class="{'borrow-timeline__active': Boolean(listing.myPendingRequestId)}">Request owner approval</li><li>Agree pickup and return</li><li>Mark returned</li></ol><p class="borrow-timeline__note">The owner and backend remain authoritative for approval, duration, pickup, return, and trust requirements.</p></section>
      <div v-if="listing.myPendingRequestId" class="pending">Your borrow request is pending. Next step: wait for the owner’s decision, then agree pickup and return details.</div>
      <ClientActionList v-else-if="listing.actions.length" :actions="listing.actions" align="start" :busy="isSaving" @execute="executeListingAction" />
      <form v-if="requestFormOpen" class="request-form" @submit.prevent="requestBorrow">
        <h2>Need this thing?</h2>
        <AppFormField label="Pickup and return details" optional hint="Include your preferred duration, pickup timing, and return expectation for the owner."><textarea v-model="message" maxlength="1000" placeholder="When would you pick it up and return it?"></textarea></AppFormField>
        <AppFormFooter><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">Send request</AppButton></template></AppFormFooter>
      </form>
      <p v-else class="pending">This thing is currently unavailable.</p>
    </article>
  </section>
</template>
