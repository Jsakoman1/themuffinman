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

const query = ref("")
const items = ref<CircleSearchResultDTO[]>([])
const isLoading = ref(false)
const error = ref("")
const searched = ref(false)
const isActing = ref(false)
const selectedPersonId = ref<number | null>(null)
const selectedPerson = computed(() => items.value.find((item) => item.id === selectedPersonId.value) ?? null)
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
.people-discovery__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(16rem,22rem);gap:var(--space-3);align-items:start}.people-discovery__preview{display:grid;gap:var(--space-2);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised);color:var(--text-muted)}.people-discovery__preview h2,.people-discovery__preview p{margin:0}.people-discovery__preview h2{color:var(--text);font-size:var(--text-size-title)}.people-discovery__preview-note{color:var(--text-soft);font-size:var(--text-size-meta)}.people-discovery__preview--empty{min-height:10rem;align-content:center}@media(max-width:860px){.people-discovery__workspace{grid-template-columns:1fr}.people-discovery__preview{order:2}}
</style>
<template>
  <section class="people-discovery">
    <header><div><p class="eyebrow">People</p><h1>Find people</h1><p class="intro">Search people through Circles trust and visibility rules.</p></div><RouterLink to="/circles" class="secondary">Manage circles</RouterLink></header>
    <CollectionToolbar title="People" :count="items.length" :busy="isLoading"><template #filters><AppSearchField v-model="query" label="Search people" placeholder="Search by username or profile" :busy="isLoading" @submit="search" /></template></CollectionToolbar>
    <AppStatus v-if="error" :message="error" tone="error" retry @retry="search" /><AppStatus v-else-if="isLoading" message="Searching people." /><AppStatus v-else-if="searched && items.length === 0" message="No people match this search." />
    <div v-else class="people-discovery__workspace"><div class="results"><SurfaceRow v-for="person in items" :key="person.id" :row="{id: String(person.id), title: person.username, description: person.profileDescription || 'No profile description yet.', badge: person.relationLabel || person.resolutionLabel || 'Trust-aware profile', to: `/people/${person.id}`}" :selected="selectedPersonId === person.id"><template #actions><AppButton type="button" tone="secondary" @click.stop="selectedPersonId = person.id">Preview</AppButton><AppButton v-if="person.primaryAction?.enabled && person.relationLabel !== 'Invite sent'" type="button" tone="primary" :loading="isActing" @click.stop="sendInvite(person.id)">{{ person.primaryAction.label || "Connect" }}</AppButton></template></SurfaceRow></div><aside v-if="selectedPerson" class="people-discovery__preview" aria-label="Person preview"><p class="eyebrow">Selected person</p><h2>{{ selectedPerson.username }}</h2><p>{{ selectedPerson.profileDescription || "No profile description yet." }}</p><p class="people-discovery__preview-note">{{ selectedPerson.relationLabel || selectedPerson.resolutionLabel || "Trust-aware profile" }}</p><RouterLink class="secondary" :to="`/people/${selectedPerson.id}`">Open full profile</RouterLink></aside><aside v-else class="people-discovery__preview people-discovery__preview--empty" aria-label="Person preview"><p class="eyebrow">Preview</p><h2>Select a person</h2><p>Inspect trust-aware context without leaving search results.</p></aside></div>
  </section>
</template>
<style scoped>.people-discovery{display:grid;gap:var(--space-3);max-width:none}.people-discovery header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.intro{color:var(--text-muted)}.search{display:flex;gap:var(--space-2);max-width:42rem}.search input{flex:1;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit}.primary,.secondary,.search button{display:inline-flex;align-items:center;justify-content:center;min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.primary,.search button{border-color:var(--accent);background:var(--accent);color:var(--canvas)}.results{display:grid;gap:0;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);overflow:hidden}.result{display:flex;justify-content:space-between;align-items:center;gap:var(--space-3);padding:var(--space-3);border:0;border-bottom:1px solid var(--border-subtle);border-radius:0;background:transparent}.result p,.result span{display:block;margin:var(--space-1) 0 0;color:var(--text-muted);font-size:var(--text-size-body)}.actions{display:flex;gap:var(--space-1);flex-wrap:wrap}.status{color:var(--text-muted)}.status--error{color:var(--danger)}.status button{margin-left:var(--space-1);border:0;background:none;color:inherit;text-decoration:underline}@media(max-width:620px){.people-discovery header,.result{align-items:start;flex-direction:column}.search{max-width:none;width:100%}}</style>
