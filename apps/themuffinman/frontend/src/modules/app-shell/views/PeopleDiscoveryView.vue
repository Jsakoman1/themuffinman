<script setup lang="ts">
import {ref} from "vue"
import {RouterLink} from "vue-router"
import type {CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const query = ref("")
const items = ref<CircleSearchResultDTO[]>([])
const isLoading = ref(false)
const error = ref("")
const searched = ref(false)
const isActing = ref(false)
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
<template>
  <section class="people-discovery">
    <header><div><p class="eyebrow">People</p><h1>Find people</h1><p class="intro">Search people through Circles trust and visibility rules.</p></div><RouterLink to="/circles" class="secondary">Manage circles</RouterLink></header>
    <form class="search" @submit.prevent="search"><input v-model="query" type="search" placeholder="Search by username or profile" aria-label="Search people"><button type="submit" :disabled="isLoading">{{ isLoading ? "Searching" : "Search" }}</button></form>
    <p v-if="error" class="status status--error" role="alert">{{ error }} <button type="button" @click="search">Retry</button></p>
    <p v-else-if="isLoading" class="status" role="status">Searching people.</p>
    <p v-else-if="searched && items.length === 0" class="status">No people match this search.</p>
    <div v-else class="results"><article v-for="person in items" :key="person.id" class="result"><div><strong>{{ person.username }}</strong><p>{{ person.profileDescription || "No profile description yet." }}</p><span>{{ person.relationLabel || person.resolutionLabel || "Trust-aware profile" }}</span></div><div class="actions"><RouterLink :to="`/people/${person.id}`" class="secondary">View profile</RouterLink><button v-if="person.primaryAction?.enabled && person.relationLabel !== 'Invite sent'" type="button" class="primary" :disabled="isActing" @click="sendInvite(person.id)">{{ person.primaryAction.label || "Connect" }}</button></div></article></div>
  </section>
</template>
<style scoped>.people-discovery{display:grid;gap:1rem;max-width:58rem}.people-discovery header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.7rem,3vw,2.6rem);letter-spacing:-.075em}.intro{color:var(--text-muted)}.search{display:flex;gap:.5rem;max-width:42rem}.search input{flex:1;border:1px solid var(--border-subtle);border-radius:.7rem;padding:.7rem;font:inherit}.primary,.secondary,.search button{display:inline-flex;align-items:center;justify-content:center;border:1px solid var(--border-subtle);border-radius:999px;padding:.55rem .8rem;font:inherit;font-size:.8rem;font-weight:650}.primary,.search button{background:var(--accent);color:var(--text)}.results{display:grid;gap:.4rem}.result{display:flex;justify-content:space-between;align-items:center;gap:1rem;padding:1rem;border:1px solid var(--border-subtle);border-radius:.9rem;background:var(--surface)}.result p,.result span{display:block;margin:.25rem 0 0;color:var(--text-muted);font-size:.84rem}.actions{display:flex;gap:.4rem;flex-wrap:wrap}.status{color:var(--text-muted)}.status--error{color:var(--danger)}.status button{margin-left:.5rem;border:0;background:none;color:inherit;text-decoration:underline}@media(max-width:620px){.people-discovery header,.result{align-items:start;flex-direction:column}.search{max-width:none;width:100%}}</style>
