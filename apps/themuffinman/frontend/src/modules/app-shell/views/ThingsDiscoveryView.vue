<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import type {ThingBorrowRequestResponseDTO, ThingListingRequestDTO, ThingListingResponseDTO} from "../../../contracts/index.ts"
import {thingsApi} from "../../things/api/thingsApi.ts"
import {useRoute, useRouter} from "vue-router"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import {useAsyncAction} from "../composables/useAsyncAction.ts"
import {collectionReturnQuery} from "../composables/useSurfaceViewState.ts"
import TaskSurface from "../components/TaskSurface.vue"
import FriendlyCollectionHeader from "../components/FriendlyCollectionHeader.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import ClientActionList from "../components/ClientActionList.vue"

const route = useRoute()
const router = useRouter()
const userShellApi = {getThingPreview: thingsApi.getPreview, getListingsForScope: thingsApi.getListingsForScope, getThingOwnerBorrowRequests: thingsApi.getOwnerBorrowRequests, getMyThingBorrowRequests: thingsApi.getMyBorrowRequests, createThingListing: thingsApi.createListing, requestThingBorrow: thingsApi.requestBorrow, cancelThingBorrow: thingsApi.cancelBorrow, decideThingBorrow: thingsApi.decideBorrow, returnThingBorrow: thingsApi.returnBorrow, updateThingListing: thingsApi.updateListing, archiveThingListing: thingsApi.archiveListing}
const requestsOnly = computed(() => route.path.endsWith("/requests"))
const activeTabId = computed(() => requestsOnly.value ? "requests" : route.path.endsWith("/mine") ? "mine" : "discover")
const tabs = [
  {id: "discover", label: "Find things", route: "/things", backendScope: "things.discover", emptyState: "Nothing is available to borrow yet."},
  {id: "mine", label: "My things", route: "/things/mine", backendScope: "things.mine", emptyState: "You are not sharing anything yet."},
  {id: "requests", label: "Requests", route: "/things/requests", backendScope: "things.requests", emptyState: "You have no borrow requests yet."}
]
const mine = ref(route.query.scope === "mine" || route.path.endsWith("/mine"))
const needQuery = ref(typeof route.query.q === "string" ? route.query.q : "")
const listings = ref<ThingListingResponseDTO[]>([])
const ownerRequests = ref<ThingBorrowRequestResponseDTO[]>([])
const borrowerRequests = ref<ThingBorrowRequestResponseDTO[]>([])
const form = ref<ThingListingRequestDTO | null>(null)
const closeForm = () => { form.value = null; editingId.value = null }
const editingId = ref<number | null>(null)
const borrowId = ref<number | null>(null)
const borrowMessage = ref("")
const isLoading = ref(true)
const {pending: isSaving, error, execute} = useAsyncAction()
const feedback = ref("")
const title = computed(() => requestsOnly.value ? "My borrow requests" : mine.value ? "My things" : "Things to borrow")
let loadSequence = 0
const load = async () => {
  const sequence = ++loadSequence
  const requestedMine = mine.value
  isLoading.value = true
  error.value = ""
  try {
    const listingsResult = requestsOnly.value ? {items: []} : await userShellApi.getListingsForScope(requestedMine ? "mine" : "discover", needQuery.value.trim())
    if (sequence !== loadSequence) return
    listings.value = listingsResult.items
    const requestsResult = requestsOnly.value || !requestedMine
      ? await Promise.allSettled([userShellApi.getMyThingBorrowRequests()])
      : await Promise.allSettled([userShellApi.getThingOwnerBorrowRequests()])
    if (sequence !== loadSequence) return
    const requests = requestsResult[0]
    if (requests.status === "fulfilled") {
      if (requestsOnly.value || !requestedMine) borrowerRequests.value = requests.value
      else ownerRequests.value = requests.value
    } else {
      error.value = "Things are available, but borrow requests could not be loaded."
    }
  } catch {
    error.value = "Could not load things."
  } finally {
    isLoading.value = false
  }
}
const create = async () => { if (!form.value) return; await execute(async () => { await userShellApi.createThingListing(form.value!); form.value = null; feedback.value = "Thing listed."; await load() }, "Could not create this listing.") }
const requestBorrow = async () => { if (!borrowId.value) return; await execute(async () => { await userShellApi.requestThingBorrow(borrowId.value!, borrowMessage.value); borrowId.value = null; borrowMessage.value = ""; feedback.value = "Borrow request sent."; await load() }, "Could not request this thing.") }
const cancelBorrow = async (requestId: number) => { await execute(async () => { await userShellApi.cancelThingBorrow(requestId); feedback.value = "Borrow request cancelled."; await load() }, "Could not cancel this request.") }
const decideBorrow = async (requestId: number, approve: boolean) => { await execute(async () => { await userShellApi.decideThingBorrow(requestId, approve); feedback.value = approve ? "Borrow request approved. The thing is now unavailable until it is returned." : "Borrow request declined."; await load() }, "Could not update this request.") }
const returnBorrow = async (requestId: number) => { await execute(async () => { await userShellApi.returnThingBorrow(requestId); feedback.value = "Thing marked as returned."; await load() }, "Could not mark this thing as returned.") }
const editListing = (listing: ThingListingResponseDTO) => { form.value = {title: listing.title, description: listing.description || "", conditionNote: listing.conditionNote || "", available: listing.available}; editingId.value = listing.id }
const saveListing = async () => { if (!form.value || editingId.value === null) return; await execute(async () => { await userShellApi.updateThingListing(editingId.value!, form.value!); form.value = null; editingId.value = null; feedback.value = "Thing listing updated."; await load() }, "Could not update this listing.") }
const executeListingAction = (listing: ThingListingResponseDTO, actionId: string) => { if (actionId === "EDIT") return editListing(listing); if (actionId === "ARCHIVE") return void execute(async () => { await userShellApi.archiveThingListing(listing.id); feedback.value = "Thing listing archived."; await load() }, "Could not archive this listing."); if (actionId === "REQUEST_BORROW") borrowId.value = listing.id; if (actionId === "CANCEL_BORROW_REQUEST" && listing.myPendingRequestId) void cancelBorrow(listing.myPendingRequestId) }
const executeRequestAction = (request: ThingBorrowRequestResponseDTO, actionId: string) => { if (actionId === "RETURN_BORROWED_THING") void returnBorrow(request.id); if (actionId === "CANCEL_BORROW_REQUEST") void cancelBorrow(request.id); if (actionId === "APPROVE_BORROW_REQUEST") void decideBorrow(request.id, true); if (actionId === "DECLINE_BORROW_REQUEST") void decideBorrow(request.id, false) }
watch(() => route.fullPath, () => { mine.value = route.query.scope === "mine" || route.path.endsWith("/mine"); void load() })
const searchNeeds = () => { void router.replace({query: {...route.query, q: needQuery.value.trim() || undefined}}) }
watch(() => route.query.create, (value) => { if (value === "1" && !form.value) form.value = {title: "", description: "", conditionNote: "", available: true} }, {immediate: true})
onMounted(() => void load())
</script>

<template>
  <TaskSurface mode="inspect" label="Things sharing"><section class="things-surface" data-collection-rhythm="oriented" data-interaction-model="direct-detail" data-mental-model="browse-open-request" :aria-busy="isLoading || isSaving || undefined">
    <ModuleTabs :tabs="tabs" :active-id="activeTabId" />
    <FriendlyCollectionHeader eyebrow="Things" :title="title" :description="requestsOnly ? 'Track requests you sent and see what you need to do next.' : mine ? 'Manage what you share and decide who can borrow it.' : 'Find things shared by people in your circles.'" tone="share" :primary-action="requestsOnly ? undefined : {label: 'List a thing', to: {path: '/things', query: {create: '1'}}}" />
    <CollectionToolbar :title="title" :count="requestsOnly ? borrowerRequests.length : listings.length" :busy="isLoading"><template #filters><form v-if="!requestsOnly" class="things-surface__need-search" aria-label="Search things" @submit.prevent="searchNeeds"><input v-model="needQuery" aria-label="What do you need?" placeholder="What do you need?"><AppButton type="submit">Search</AppButton></form></template></CollectionToolbar>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-if="isLoading" :message="requestsOnly ? 'Loading your borrow requests.' : mine ? 'Loading your things.' : 'Loading things to borrow.'" busy /><AppStatus v-else-if="!requestsOnly && listings.length === 0" :message="mine ? 'You have not listed any things yet.' : 'No things are available to borrow yet.'" /><AppStatus v-else-if="requestsOnly && borrowerRequests.length === 0" message="You have not sent any borrow requests yet." />
    <div v-else-if="!requestsOnly" class="things-workspace"><div class="listing-grid"><SurfaceRow v-for="listing in listings" :key="listing.id" :row="{id: String(listing.id), title: listing.title, description: `${listing.ownerUsername} · ${listing.description || 'No description yet.'}`, meta: listing.conditionNote || 'Condition not specified', to: {path: `/things/${listing.id}`, query: collectionReturnQuery(route.fullPath)}}"><template #actions><ClientActionList :actions="listing.actions" :busy="isSaving" @execute="executeListingAction(listing, $event)" /></template></SurfaceRow></div></div>
    <section v-if="!mine && borrowerRequests.length" class="requests"><h2>Your active borrowing</h2><article v-for="request in borrowerRequests" :key="request.id" class="request"><div><strong>{{ request.status }}</strong><small>{{ request.message || 'Waiting for the owner’s decision.' }}</small></div><ClientActionList :actions="request.actions" :busy="isSaving" @execute="executeRequestAction(request, $event)" /></article></section>
    <section v-if="mine && ownerRequests.length" class="requests"><h2>Needs your decision</h2><p class="requests__hint">Approving makes the thing unavailable until the borrower marks it returned.</p><article v-for="request in ownerRequests" :key="request.id" class="request"><div><strong>{{ request.borrowerUsername }} · {{ request.status }}</strong><small>{{ request.message || 'No message from the borrower.' }}</small></div><ClientActionList :actions="request.actions" :busy="isSaving" @execute="executeRequestAction(request, $event)" /></article></section>
    <AppDialog :open="form !== null" :title="editingId === null ? 'List a thing' : 'Edit listing'" @close="closeForm"><form class="form" @submit.prevent="editingId === null ? create() : saveListing()"><AppFormField label="What are you sharing?" required><input v-model="form!.title" required maxlength="140" placeholder="e.g. Apple Studio Display"></AppFormField><AppFormField label="A short note" optional><textarea v-model="form!.description" maxlength="2000" placeholder="Tell people what they need to know."></textarea></AppFormField><AppFormField label="Condition" optional><input v-model="form!.conditionNote" maxlength="180" placeholder="Optional"></AppFormField><label class="availability"><input v-model="form!.available" type="checkbox"> Available for borrowing or reservation</label><p class="form__help">Availability is checked again by the server when a request is sent.</p><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="closeForm">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : editingId === null ? "Share thing" : "Update listing" }}</AppButton></template></AppFormFooter></form></AppDialog>
    <AppDialog :open="borrowId !== null" title="Request to borrow" @close="borrowId = null"><form class="form" @submit.prevent="requestBorrow"><AppFormField label="Message" optional hint="Add a note for the owner."><textarea v-model="borrowMessage" maxlength="1000" placeholder="Add a note for the owner."></textarea></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="borrowId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Sending" : "Send request" }}</AppButton></template></AppFormFooter></form></AppDialog>
  </section></TaskSurface>
</template>

<style scoped>
.things-surface{display:grid;gap:var(--space-3);max-width:none}.eyebrow,.listing__owner{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h2{margin:var(--space-1) 0;font-size:var(--text-size-title)}.actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.things-surface button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;cursor:pointer}.things-surface button.active,.things-surface button.primary{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.things-workspace{display:grid;grid-template-columns:minmax(0,1fr);border:0;border-radius:0;background:transparent;overflow:visible}.listing-grid{display:grid;grid-template-columns:1fr;gap:var(--space-2);padding:0}.listing-grid :deep(.surface-row){border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface-raised)}.listing-grid :deep(.surface-row:hover){background:var(--surface-hover);transform:translateY(-1px)}.listing{display:grid;gap:var(--space-2);padding:var(--space-3)}.listing__link{display:block}.listing p{color:var(--text-muted)}.listing small,.pending,.request small{color:var(--text-soft)}.status{padding:var(--space-2) 0;color:var(--text-muted)}.status--error{color:var(--danger)}.status--success{color:var(--success)}.status button{border:0;text-decoration:underline;padding:0}.form{display:grid;gap:var(--space-2);padding:var(--space-3);border-top:1px solid var(--border-subtle)}.form label{display:grid;gap:var(--space-1);font-weight:var(--text-weight-semibold)}.form input,.form textarea{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.form textarea{min-height:5rem}.requests{display:grid;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-3)}.request{display:flex;justify-content:space-between;align-items:center;gap:var(--space-3);padding:var(--space-2) 0;border-bottom:1px solid var(--border-subtle)}.request>div:first-child{display:grid;gap:var(--space-1)}.request__actions{display:flex;gap:var(--space-1)}@media(max-width:650px){.request{align-items:start;flex-direction:column}}

.things-surface .app-button{min-height:var(--control-height-default);border-radius:var(--radius-control);padding:var(--space-1) var(--space-3);background:var(--control-bg);color:var(--control-ink)}.things-surface .app-button--primary{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.things-surface .app-button--danger{color:var(--danger)}.things-context{min-width:0;overflow-wrap:anywhere}.things-surface__need-search{display:flex;gap:var(--space-1);align-items:center}.things-surface__need-search input{min-width:12rem;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}
</style>
