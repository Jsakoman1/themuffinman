<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {CircleGroupResponseDTO, CircleRequestResponseDTO, CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const groups = ref<CircleGroupResponseDTO[]>([])
const requests = ref<CircleRequestResponseDTO[]>([])
const outgoing = ref<CircleRequestResponseDTO[]>([])
const results = ref<CircleSearchResultDTO[]>([])
const blocked = ref<CircleSearchResultDTO[]>([])
const searchQuery = ref("")
const groupName = ref("")
const isLoading = ref(true)
const isActing = ref(false)
const error = ref("")
const feedback = ref("")

const load = async () => {
  isLoading.value = true; error.value = ""
  try {
    const [circleGroups, incoming, outgoingRequests, blockedUsers] = await Promise.all([userShellApi.getCircleGroups(), userShellApi.getIncomingCircleRequests(0, 50), userShellApi.getOutgoingCircleRequests(0, 50), userShellApi.getBlockedCircleUsers(0, 50)])
    groups.value = circleGroups; requests.value = incoming.items; outgoing.value = outgoingRequests.items; blocked.value = blockedUsers.items
  } catch { error.value = "Could not load circles." }
  finally { isLoading.value = false }
}
const createGroup = async () => { if (!groupName.value.trim()) return; isActing.value = true; error.value = ""; try { await userShellApi.createCircleGroup({name: groupName.value.trim()}); groupName.value = ""; feedback.value = "Circle created."; await load() } catch { error.value = "Could not create this circle." } finally { isActing.value = false } }
const decide = async (request: CircleRequestResponseDTO, accept: boolean) => { isActing.value = true; error.value = ""; try { if (accept) await userShellApi.acceptCircleRequest(request.id); else await userShellApi.deleteCircleRequest(request.id); feedback.value = accept ? "Request accepted." : "Request declined."; await load() } catch { error.value = "Could not update this request." } finally { isActing.value = false } }
const search = async () => { if (!searchQuery.value.trim()) { results.value = []; return }; try { results.value = (await userShellApi.searchCircleUsers(searchQuery.value.trim())).items } catch { error.value = "Could not search people." } }
const block = async (userId: number) => { if (!window.confirm("Block this person?")) return; isActing.value = true; try { await userShellApi.blockCircleUser(userId); feedback.value = "Person blocked."; results.value = results.value.filter(item => item.id !== userId) } catch { error.value = "Could not block this person." } finally { isActing.value = false } }
const unblock = async (userId: number) => { isActing.value = true; try { await userShellApi.unblockCircleUser(userId); feedback.value = "Person unblocked."; await load() } catch { error.value = "Could not unblock this person." } finally { isActing.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="circles">
    <header class="circles__header"><div><p class="circles__eyebrow">People / Circles</p><h1>Circles</h1></div><span>{{ groups.length }} groups</span></header>
    <aside class="circles__privacy" aria-label="Circle privacy explanation"><strong>Circles are a trust boundary.</strong><span>Membership does not automatically reveal your exact address or private activity. Each module still applies its own visibility and consent rules; manage exact location sharing in Profile Settings.</span></aside>
    <p v-if="feedback" class="circles__feedback" role="status">{{ feedback }}</p>
    <div v-if="isLoading" class="circles__status" role="status">Loading.</div>
    <div v-else-if="error && groups.length === 0 && requests.length === 0" class="circles__status circles__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <template v-else>
      <form class="circles__create" @submit.prevent="createGroup"><input v-model="groupName" placeholder="New circle name" maxlength="120"><button type="submit" :disabled="isActing">Create</button></form>
      <form class="circles__search" @submit.prevent="search"><input v-model="searchQuery" placeholder="Find people"><button type="submit">Search</button></form>
      <section v-if="results.length" class="circles__section"><h2>People</h2><article v-for="person in results" :key="person.id" class="circles__row"><div><strong>{{ person.username }}</strong><span>{{ person.profileDescription }}</span></div><button type="button" @click="block(person.id)">Block</button></article></section>
      <section v-if="requests.length" class="circles__section"><h2>Incoming requests</h2><article v-for="request in requests" :key="request.id" class="circles__row"><div><strong>{{ request.requesterUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><div><button type="button" :disabled="isActing" @click="decide(request, true)">Accept</button><button type="button" :disabled="isActing" @click="decide(request, false)">Decline</button></div></article></section>
      <section v-if="outgoing.length" class="circles__section"><h2>Outgoing requests</h2><article v-for="request in outgoing" :key="request.id" class="circles__row"><div><strong>{{ request.counterpartUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><button type="button" :disabled="isActing" @click="decide(request, false)">Cancel</button></article></section>
      <section v-if="blocked.length" class="circles__section"><h2>Blocked</h2><article v-for="person in blocked" :key="person.id" class="circles__row"><div><strong>{{ person.username }}</strong></div><button type="button" :disabled="isActing" @click="unblock(person.id)">Unblock</button></article></section>
      <section class="circles__section"><h2>Your circles</h2><p v-if="groups.length === 0" class="circles__status">No circles yet.</p><article v-for="group in groups" :key="group.id" class="circles__row"><div><strong>{{ group.name }}</strong><span>{{ group.memberCount }} members · {{ group.memberPreviewLabel }}</span></div></article></section>
      <p v-if="error" class="circles__status circles__status--error" role="alert">{{ error }}</p>
    </template>
  </section>
</template>

<style scoped>
.circles{display:grid;gap:1rem}.circles__header{display:flex;justify-content:space-between;align-items:end}.circles__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}h2{margin:0;font-size:1rem}.circles__header>span,.circles__row span{color:rgba(23,34,26,.58);font-size:.84rem}.circles__privacy{display:grid;gap:.25rem;max-width:48rem;padding:.8rem 1rem;border-left:3px solid #7c9c82;background:rgba(214,228,218,.48);color:rgba(23,34,26,.72)}.circles__privacy span{font-size:.88rem}.circles__create{display:flex;gap:.45rem;max-width:30rem}.circles__create input,.circles__search input{flex:1;border:1px solid rgba(23,34,26,.14);border-radius:999px;padding:.65rem .8rem;font:inherit}.circles__search{display:flex;gap:.45rem;max-width:30rem}.circles button{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .75rem;background:transparent;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer}.circles__create button{background:#17221a;color:#f8f8f4}.circles__section{display:grid;gap:.45rem}.circles__row{display:flex;justify-content:space-between;align-items:center;gap:1rem;padding:.8rem 0;border-bottom:1px solid rgba(23,34,26,.08)}.circles__row>div:first-child{display:grid;gap:.25rem}.circles__feedback{color:#2d6846}.circles__status{padding:.7rem 0;color:rgba(23,34,26,.65)}.circles__status--error{color:#8d2f25}.circles__status button{margin-left:.5rem;border:0;color:inherit;text-decoration:underline}@media(max-width:620px){.circles__row{align-items:start;flex-direction:column}.circles__create,.circles__search{max-width:none}}
</style>
