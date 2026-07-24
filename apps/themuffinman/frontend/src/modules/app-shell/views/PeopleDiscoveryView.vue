<script setup lang="ts">
import {computed, ref} from "vue"
import {RouterLink} from "vue-router"
import type {CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppSearchField from "../components/AppSearchField.vue"
import AppButton from "../components/AppButton.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {useSurfaceViewState} from "../composables/useSurfaceViewState.ts"
import {currentUser} from "../../identity/auth.ts"

const query = ref("")
const items = ref<CircleSearchResultDTO[]>([])
// Search rows stay lightweight; relationship decisions are dispatched from the
// backend-authorized profile action contract rather than inferred from labels.
const isLoading = ref(false)
const error = ref("")
const searched = ref(false)
const isActing = ref(false)
const {state: viewState} = useSurfaceViewState("people-discovery", computed(() => currentUser.value?.id), query)
const selectedPerson = computed(() => items.value.find(item => item.id === viewState.value.selectedId) ?? null)
const search = async () => {
  if (query.value.trim().length < 2) { error.value = "Enter at least two characters to find people."; return }
  isLoading.value = true; error.value = ""; searched.value = true
  try { items.value = (await userShellApi.searchCircleUsers(query.value.trim())).items } catch { error.value = "Could not load people. Try again." } finally { isLoading.value = false }
}
const sendInvite = async (userId: number) => {
  isActing.value = true; error.value = ""
  try { await userShellApi.createCircleRequest({recipientId: userId}); items.value = items.value.map(item => item.id === userId ? {...item, relationLabel: "Invite sent"} : item) }
  catch { error.value = "Could not send this connection invite." }
  finally { isActing.value = false }
}
</script>
<style scoped>
.people-discovery__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,24rem);align-items:start;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}
</style>
<template>
  <section class="people-discovery">
    <header><div><p class="eyebrow">People</p><h1>Find people</h1><p class="intro">Search people through Circles trust and visibility rules.</p></div><RouterLink to="/circles" class="secondary">Manage circles</RouterLink></header>
    <CollectionToolbar title="People" :count="items.length" :busy="isLoading"><template #filters><AppSearchField v-model="query" label="Search people" placeholder="Search by username or profile" :busy="isLoading" @submit="search" /></template></CollectionToolbar>
    <AppStatus v-if="error" :message="error" tone="error" retry @retry="search" /><AppStatus v-else-if="isLoading" message="Searching people." /><AppStatus v-else-if="searched && items.length === 0" message="No people match this search." />
    <div v-else class="people-discovery__workspace"><div class="results"><SurfaceRow v-for="person in items" :key="person.id" :row="{id: String(person.id), title: person.username, description: person.profileDescription || 'No profile description yet.', badge: person.relationLabel || person.resolutionLabel || 'Trust-aware profile', to: `/people/${person.id}`}" :selected="viewState.selectedId === person.id" @click="viewState.selectedId = person.id"><template #actions><AppButton v-if="person.primaryAction?.enabled && person.relationLabel !== 'Invite sent'" type="button" tone="primary" :loading="isActing" @click.stop="sendInvite(person.id)">{{ person.primaryAction.label || "Connect" }}</AppButton></template></SurfaceRow></div><aside class="people-context" aria-label="Person context"><template v-if="selectedPerson"><p class="eyebrow">Selected person</p><h2>{{ selectedPerson.username }}</h2><p>{{ selectedPerson.profileDescription || 'No profile description yet.' }}</p><dl><div v-if="selectedPerson.locationLabel"><dt>Area</dt><dd>{{ selectedPerson.locationLabel }}</dd></div><div><dt>Relationship</dt><dd>{{ selectedPerson.relationLabel || selectedPerson.resolutionLabel }}</dd></div></dl><RouterLink class="people-context__link" :to="`/people/${selectedPerson.id}`">Open full profile</RouterLink></template><template v-else><p class="eyebrow">Person context</p><h2>Select a person</h2><p>Choose a result to inspect its profile without leaving this search.</p></template></aside></div>
  </section>
</template>
<style scoped>.people-discovery{display:grid;gap:var(--space-3);max-width:none}.people-discovery header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.intro{color:var(--text-muted)}.search{display:flex;gap:var(--space-2);max-width:42rem}.search input{flex:1;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.primary,.secondary,.search button{display:inline-flex;align-items:center;justify-content:center;min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.primary,.search button{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.results{display:grid;gap:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden}.result{display:flex;justify-content:space-between;align-items:center;gap:var(--space-3);padding:var(--space-3);border:0;border-bottom:1px solid var(--border-subtle);border-radius:0;background:transparent}.result p,.result span{display:block;margin:var(--space-1) 0 0;color:var(--text-muted);font-size:var(--text-size-body)}.actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.status{color:var(--text-muted)}.status--error{color:var(--danger)}.status button{margin-left:var(--space-1);border:0;background:none;color:inherit;text-decoration:underline}@media(max-width:620px){.people-discovery header,.result{align-items:start;flex-direction:column}.search{max-width:none;width:100%}}</style>
<style scoped>.people-context{border-left:1px solid var(--border-subtle);padding:var(--space-3);background:var(--surface-raised)}.people-context p{color:var(--text-muted)}.people-context h2{margin:var(--space-1) 0;font-size:var(--text-size-title)}.people-context dl{display:grid;gap:var(--space-2);margin:var(--space-3) 0}.people-context dl div{display:flex;justify-content:space-between;gap:var(--space-2);border-bottom:1px solid var(--border-subtle);padding-bottom:var(--space-1)}.people-context dt{color:var(--text-soft);font-size:var(--text-size-meta)}.people-context dd{margin:0;text-align:right}.people-context__link{display:inline-flex;margin-top:var(--space-2);font-weight:var(--text-weight-semibold)}@media(max-width:860px){.people-discovery__workspace{grid-template-columns:1fr}.people-context{border-left:0;border-top:1px solid var(--border-subtle)}}</style>
