<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import type {CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppSearchField from "../components/AppSearchField.vue"
import AppButton from "../components/AppButton.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import ObjectPreviewPanel from "../components/ObjectPreviewPanel.vue"
import {useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import {currentUser} from "../../identity/auth.ts"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

const query = ref("")
const router = useRouter()
const items = ref<CircleSearchResultDTO[]>([])
// Search rows stay lightweight; relationship decisions are dispatched from the
// backend-authorized profile action contract rather than inferred from labels.
const isLoading = ref(false)
const error = ref("")
const searched = ref(false)
const isActing = ref(false)
const {state: viewState} = useSurfaceViewState("people-discovery", computed(() => currentUser.value?.id), query)
const selectedPerson = computed(() => items.value.find(item => item.id === viewState.value.selectedId) ?? null)
const surface = getAppSurfaceConfig("people")
const search = async () => {
  if (query.value.trim().length < 2) { error.value = "Enter at least two characters to find people."; return }
  isLoading.value = true; error.value = ""; searched.value = true
  try { items.value = (await userShellApi.searchCircleUsers(query.value.trim())).items } catch { error.value = "Could not load people. Try again." } finally { isLoading.value = false }
}
const sendInvite = async (userId: number) => {
  isActing.value = true; error.value = ""
  try { await userShellApi.createCircleRequest({recipientId: userId}); await search() }
  catch { error.value = "Could not send this connection invite." }
  finally { isActing.value = false }
}
const openProfile = () => {
  if (selectedPerson.value) void router.push(`/people/${selectedPerson.value.id}`)
}
</script>
<template>
  <section class="people-discovery" data-people-context="relationship-visibility-actions">
    <SurfaceHeader :config="surface" title="Find people" description="Search people through Circles trust and visibility rules." />
    <CollectionToolbar title="People" :count="items.length" :busy="isLoading"><template #filters><AppSearchField v-model="query" label="Search people" placeholder="Search by username or profile" :busy="isLoading" @submit="search" /></template></CollectionToolbar>
    <AppStatus v-if="error" :message="error" tone="error" retry @retry="search" /><AppStatus v-else-if="isLoading" message="Searching people." /><AppStatus v-else-if="searched && items.length === 0" message="No people match this search." />
    <div v-else class="people-discovery__workspace" :class="{'people-discovery__workspace--preview': selectedPerson}"><div class="results"><SurfaceRow v-for="person in items" :key="person.id" :row="{id: String(person.id), title: person.username, description: person.profileDescription || 'No profile description yet.', badge: person.relationLabel || person.resolutionLabel || 'Trust-aware profile', to: `/people/${person.id}`}" primary-action="preview" :selected="viewState.selectedId === person.id" @preview="viewState.selectedId = person.id"><template #actions><AppButton v-if="person.primaryAction?.enabled" type="button" tone="primary" :loading="isActing" @click.stop="sendInvite(person.id)">{{ person.primaryAction.label || "Connect" }}</AppButton></template></SurfaceRow></div><ObjectPreviewPanel v-if="selectedPerson" :open="true" :title="selectedPerson.username" subtitle="Selected person" detail-label="Open full profile" @close="viewState.selectedId = null" @open-detail="openProfile"><p>{{ selectedPerson.profileDescription || 'No profile description yet.' }}</p><dl><div v-if="selectedPerson.locationLabel"><dt>Area</dt><dd>{{ selectedPerson.locationLabel }}</dd></div><div><dt>Relationship</dt><dd>{{ selectedPerson.relationLabel || selectedPerson.resolutionLabel }}</dd></div></dl></ObjectPreviewPanel></div>
  </section>
</template>
<style scoped>
.people-discovery{display:grid;gap:var(--space-3);max-width:none}.people-discovery header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.people-discovery h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.intro{color:var(--text-muted)}.people-discovery__workspace{display:grid;grid-template-columns:minmax(0,1fr);align-items:start;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.people-discovery__workspace--preview{grid-template-columns:minmax(0,1fr) minmax(18rem,24rem)}.results{display:grid;gap:0}@media(max-width:980px){.people-discovery header{align-items:start;flex-direction:column}.people-discovery__workspace--preview{grid-template-columns:1fr}}
</style>
