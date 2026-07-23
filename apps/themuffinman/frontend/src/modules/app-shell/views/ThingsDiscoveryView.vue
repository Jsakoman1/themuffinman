<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import type {ThingBorrowRequestResponseDTO, ThingListingRequestDTO, ThingListingResponseDTO} from "../../../contracts/index.ts"
import {thingsApi} from "../../things/api/thingsApi.ts"
import {RouterLink, useRoute, useRouter} from "vue-router"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import {invokeObjectAction} from "../composables/useObjectActions.ts"
import {useAsyncAction} from "../composables/useAsyncAction.ts"
import {useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import {currentUser} from "../../identity/auth.ts"
import {buildSurfaceVisionRoute} from "../visionHandoff.ts"
import GuidedIntakePanel from "../components/GuidedIntakePanel.vue"

const route = useRoute()
const router = useRouter()
const userShellApi = {getThingPreview: thingsApi.getPreview, getListingsForScope: thingsApi.getListingsForScope, getThingOwnerBorrowRequests: thingsApi.getOwnerBorrowRequests, getMyThingBorrowRequests: thingsApi.getMyBorrowRequests, createThingListing: thingsApi.createListing, requestThingBorrow: thingsApi.requestBorrow, cancelThingBorrow: thingsApi.cancelBorrow, decideThingBorrow: thingsApi.decideBorrow, returnThingBorrow: thingsApi.returnBorrow, updateThingListing: thingsApi.updateListing, archiveThingListing: thingsApi.archiveListing}
const mine = ref(route.query.scope === "mine" || route.path.endsWith("/mine"))
const listings = ref<ThingListingResponseDTO[]>([])
const ownerRequests = ref<ThingBorrowRequestResponseDTO[]>([])
const borrowerRequests = ref<ThingBorrowRequestResponseDTO[]>([])
const form = ref<ThingListingRequestDTO | null>(null)
const guidedThingDraft = ref<Record<string, string> | null>(null)
const acceptGuidedThingDraft = (draft: Record<string, string>) => { guidedThingDraft.value = draft; form.value = {title: draft.title ?? "", description: draft.description ?? "", conditionNote: "", available: true} }
const closeForm = () => { form.value = null; editingId.value = null; guidedThingDraft.value = null }
const editingId = ref<number | null>(null)
const borrowId = ref<number | null>(null)
const borrowMessage = ref("")
const isLoading = ref(true)
const {pending: isSaving, error, execute} = useAsyncAction()
const feedback = ref("")
const title = computed(() => mine.value ? "My things" : "Things to borrow")
const selectedListing = computed(() => listings.value.find(listing => listing.id === viewState.value.selectedId) ?? null)
const visionRoute = computed(() => buildSurfaceVisionRoute("things", route.fullPath, title.value))
const {state: viewState} = useSurfaceViewState("things-discovery", computed(() => currentUser.value?.id), computed(() => route.fullPath))
let loadSequence = 0
const load = async () => {
  const sequence = ++loadSequence
  const requestedMine = mine.value
  isLoading.value = true
  error.value = ""
  try {
    const listingsResult = await userShellApi.getListingsForScope(requestedMine ? "mine" : "discover")
    if (sequence !== loadSequence) return
    listings.value = listingsResult.items
    const requestsResult = requestedMine
      ? await Promise.allSettled([userShellApi.getThingOwnerBorrowRequests()])
      : await Promise.allSettled([userShellApi.getMyThingBorrowRequests()])
    if (sequence !== loadSequence) return
    const requests = requestsResult[0]
    if (requests.status === "fulfilled") {
      if (requestedMine) ownerRequests.value = requests.value
      else borrowerRequests.value = requests.value
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
const cancelBorrow = async (requestId: number) => { if (!await confirmAction("Cancel this borrow request?", "Cancel borrow request")) return; await execute(async () => { await userShellApi.cancelThingBorrow(requestId); feedback.value = "Borrow request cancelled."; await load() }, "Could not cancel this request.") }
const decideBorrow = async (requestId: number, approve: boolean) => { await execute(async () => { await userShellApi.decideThingBorrow(requestId, approve); feedback.value = approve ? "Borrow request approved." : "Borrow request declined."; await load() }, "Could not update this request.") }
const returnBorrow = async (requestId: number) => { await execute(async () => { await userShellApi.returnThingBorrow(requestId); feedback.value = "Thing marked as returned."; await load() }, "Could not mark this thing as returned.") }
const editListing = (listing: ThingListingResponseDTO) => { form.value = {title: listing.title, description: listing.description || "", conditionNote: listing.conditionNote || "", available: listing.available}; editingId.value = listing.id }
const saveListing = async () => { if (!form.value || editingId.value === null) return; await execute(async () => { await userShellApi.updateThingListing(editingId.value!, form.value!); form.value = null; editingId.value = null; feedback.value = "Thing listing updated."; await load() }, "Could not update this listing.") }
const archiveListing = async (listing: ThingListingResponseDTO) => { await invokeObjectAction({id: `archive-${listing.id}`, label: "Archive", tone: "danger", confirmation: {title: "Archive listing", message: `Archive “${listing.title}”?`}, run: async () => { await execute(async () => { await userShellApi.archiveThingListing(listing.id); feedback.value = "Thing listing archived."; await load() }, "Could not archive this listing.") }}) }
watch(() => route.fullPath, () => { mine.value = route.query.scope === "mine" || route.path.endsWith("/mine"); void load() })
watch(() => route.query.create, (value) => { if (value === "1" && !form.value) form.value = {title: "", description: "", conditionNote: "", available: true} }, {immediate: true})
onMounted(() => void load())
</script>

<template>
  <section class="things-surface">
    <header><div><p class="eyebrow">Things</p><h1>{{ title }}</h1></div></header>
    <CollectionToolbar :title="title" :count="listings.length" :busy="isLoading"><template #filters><details class="things-surface__scope"><summary>Scope: {{ mine ? "My things" : "Discover" }}</summary><div><AppButton type="button" :tone="!mine ? 'primary' : 'secondary'" @click="router.replace({query: {...route.query, scope: undefined}})">Discover</AppButton><AppButton type="button" :tone="mine ? 'primary' : 'secondary'" @click="router.replace({query: {...route.query, scope: 'mine'}})">My things</AppButton></div></details></template><template #actions><RouterLink :to="visionRoute">Ask Vision</RouterLink><AppButton type="button" tone="primary" @click="form = {title: '', description: '', conditionNote: '', available: true}">List a thing</AppButton></template></CollectionToolbar>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-if="isLoading" :message="mine ? 'Loading your things.' : 'Loading things to borrow.'" busy /><AppStatus v-else-if="listings.length === 0" :message="mine ? 'You have not listed any things yet.' : 'No things are available to borrow yet.'" />
    <div v-else class="things-workspace"><div class="listing-grid"><SurfaceRow v-for="listing in listings" :key="listing.id" :row="{id: String(listing.id), title: listing.title, description: `${listing.ownerUsername} · ${listing.description || 'No description yet.'}`, meta: listing.conditionNote || 'Condition not specified'}" :selected="viewState.selectedId === listing.id" @click="viewState.selectedId = listing.id"><template #actions><AppButton v-if="listing.allowedActions.includes('EDIT')" type="button" tone="secondary" @click.stop="editListing(listing)">Edit</AppButton><AppButton v-if="listing.allowedActions.includes('ARCHIVE')" type="button" tone="danger" @click.stop="archiveListing(listing)">Archive</AppButton><AppButton v-if="listing.allowedActions.includes('REQUEST_BORROW')" type="button" tone="primary" @click.stop="borrowId = listing.id">Request</AppButton><AppButton v-if="listing.allowedActions.includes('CANCEL_BORROW_REQUEST') && listing.myPendingRequestId" type="button" tone="secondary" @click.stop="cancelBorrow(listing.myPendingRequestId)">Cancel request</AppButton></template></SurfaceRow></div><aside class="things-context" aria-label="Thing context"><template v-if="selectedListing"><p class="eyebrow">Selected thing</p><h2>{{ selectedListing.title }}</h2><p>{{ selectedListing.description || 'No description provided.' }}</p><dl><div><dt>Owner</dt><dd>{{ selectedListing.ownerUsername }}</dd></div><div><dt>Availability</dt><dd>{{ selectedListing.availabilityLabel || (selectedListing.available ? 'Available' : 'Unavailable') }}</dd></div><div v-if="selectedListing.conditionNote"><dt>Condition</dt><dd>{{ selectedListing.conditionNote }}</dd></div></dl><RouterLink class="things-context__link" :to="{path: `/things/${selectedListing.id}`, query: {returnTo: route.fullPath}}">Open full detail</RouterLink></template><template v-else><p class="eyebrow">Thing context</p><h2>Select a thing</h2><p>Choose a listing to inspect its details without leaving this collection.</p></template></aside></div>
    <section v-if="!mine && borrowerRequests.length" class="requests"><h2>My borrow requests</h2><article v-for="request in borrowerRequests" :key="request.id" class="request"><div><strong>{{ request.borrowerUsername }} · {{ request.status }}</strong><small>Listing #{{ request.listingId }}</small></div><div class="request__actions"><AppButton v-if="request.allowedActions.includes('RETURN_BORROWED_THING')" type="button" tone="primary" @click="returnBorrow(request.id)">Mark returned</AppButton><AppButton v-if="request.allowedActions.includes('CANCEL_BORROW_REQUEST')" type="button" tone="secondary" @click="cancelBorrow(request.id)">Cancel</AppButton></div></article></section>
    <section v-if="mine && ownerRequests.length" class="requests"><h2>Borrow requests for my things</h2><article v-for="request in ownerRequests" :key="request.id" class="request"><div><strong>{{ request.borrowerUsername }} · {{ request.status }}</strong><small>Listing #{{ request.listingId }}<template v-if="request.message"> · {{ request.message }}</template></small></div><div class="request__actions"><AppButton v-if="request.allowedActions.includes('APPROVE_BORROW_REQUEST')" type="button" tone="primary" :loading="isSaving" @click="decideBorrow(request.id, true)">Approve</AppButton><AppButton v-if="request.allowedActions.includes('DECLINE_BORROW_REQUEST')" type="button" tone="danger" :loading="isSaving" @click="decideBorrow(request.id, false)">Decline</AppButton></div></article></section>
    <AppDialog :open="form !== null" :title="editingId === null ? 'List a thing' : 'Edit listing'" @close="closeForm"><GuidedIntakePanel v-if="editingId === null && !guidedThingDraft" flow="things.listing.create" title="List a thing" @completed="acceptGuidedThingDraft" @cancel="closeForm" /><form v-else class="form" @submit.prevent="editingId === null ? create() : saveListing()"><AppFormField label="Title" required><input v-model="form!.title" required maxlength="140"></AppFormField><AppFormField label="Description" optional><textarea v-model="form!.description" maxlength="2000"></textarea></AppFormField><AppFormField label="Condition note" optional><input v-model="form!.conditionNote" maxlength="180"></AppFormField><label class="availability"><input v-model="form!.available" type="checkbox"> Available to borrow</label><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="closeForm">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : editingId === null ? "Save listing" : "Update listing" }}</AppButton></template></AppFormFooter></form></AppDialog>
    <AppDialog :open="borrowId !== null" title="Request to borrow" @close="borrowId = null"><form class="form" @submit.prevent="requestBorrow"><AppFormField label="Message" optional hint="Add a note for the owner."><textarea v-model="borrowMessage" maxlength="1000" placeholder="Add a note for the owner."></textarea></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="borrowId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Sending" : "Send request" }}</AppButton></template></AppFormFooter></form></AppDialog>
  </section>
</template>

<style scoped>
.things-surface{display:grid;gap:var(--space-3);max-width:none}.things-surface header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.eyebrow,.listing__owner{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}h2{margin:var(--space-1) 0;font-size:var(--text-size-title)}.actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.things-surface button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;cursor:pointer}.things-surface button.active,.things-surface button.primary{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.things-workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,24rem);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base);overflow:hidden}.listing-grid{display:grid;grid-template-columns:1fr;gap:0;padding:var(--space-1)}.things-context{border-left:1px solid var(--border-subtle);padding:var(--space-3);background:var(--surface-raised)}.things-context p{color:var(--text-muted)}.things-context dl,.rides-context dl,.business-context dl{display:grid;gap:var(--space-2);margin:var(--space-3) 0}.things-context dl div,.rides-context dl div,.business-context dl div{display:flex;justify-content:space-between;gap:var(--space-2);border-bottom:1px solid var(--border-subtle);padding-bottom:var(--space-1)}dt{color:var(--text-soft);font-size:var(--text-size-meta)}dd{margin:0;text-align:right}.things-context__link,.rides-context__link,.business-context__link{display:inline-flex;margin-top:var(--space-2);font-weight:var(--text-weight-semibold)}.listing{display:grid;gap:var(--space-2);padding:var(--space-3)}.listing__link{display:block}.listing p{color:var(--text-muted)}.listing small,.pending,.request small{color:var(--text-soft)}.status{padding:var(--space-2) 0;color:var(--text-muted)}.status--error{color:var(--danger)}.status--success{color:var(--success)}.status button{border:0;text-decoration:underline;padding:0}.form{display:grid;gap:var(--space-2);padding:var(--space-3);border-top:1px solid var(--border-subtle)}.form label{display:grid;gap:var(--space-1);font-weight:var(--text-weight-semibold)}.form input,.form textarea{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.form textarea{min-height:5rem}.requests{display:grid;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-3)}.request{display:flex;justify-content:space-between;align-items:center;gap:var(--space-3);padding:var(--space-2) 0;border-bottom:1px solid var(--border-subtle)}.request>div:first-child{display:grid;gap:var(--space-1)}.request__actions{display:flex;gap:var(--space-1)}@media(max-width:860px){.things-workspace{grid-template-columns:1fr}.things-context{border-left:0;border-top:1px solid var(--border-subtle)}}@media(max-width:650px){.things-surface header{align-items:start;flex-direction:column}.request{align-items:start;flex-direction:column}}
</style>
<style scoped>
.things-surface .app-button { min-height:var(--control-height-default); border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.things-surface .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.things-surface .app-button--danger { color:var(--danger); }
</style>
