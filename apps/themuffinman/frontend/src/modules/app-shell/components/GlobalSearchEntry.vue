<script setup lang="ts">
import {ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {VisionSearchDiscoveryDTO, VisionSearchDiscoveryItemDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const router = useRouter()
const query = ref("")
const result = ref<VisionSearchDiscoveryDTO | null>(null)
const error = ref("")
const loading = ref(false)
const saved = ref(false)
const search = async () => {
  if (!query.value.trim()) return
  loading.value = true; error.value = ""
  try { result.value = await userShellApi.searchUniversal(query.value.trim()) }
  catch { error.value = "Could not search right now." }
  finally { loading.value = false }
}
const saveIntent = async () => { if (!query.value.trim()) return; try { await userShellApi.createSavedSearchIntent({query: query.value.trim(), notifyEnabled: true}); saved.value = true } catch { error.value = "Could not save this search." } }
const destination = (item: VisionSearchDiscoveryItemDTO) => {
  if (!item.targetId) return null
  if (item.entityFamily === "quest") return `/work/quests/${item.targetId}`
  if (item.entityFamily === "thing") return `/things/${item.targetId}`
  if (item.entityFamily === "circle") return `/circles/${item.targetId}`
  if (item.entityFamily === "user") return `/people/${item.targetId}`
  if (item.entityFamily === "business") return `/business/${item.resolutionLabel || item.targetId}`
  return null
}
const open = async (item: VisionSearchDiscoveryItemDTO) => { const to = destination(item); if (to) await router.push(to) }
</script>

<template>
  <details class="global-search-entry">
    <summary aria-label="Search across TheMuffinMan">Search</summary>
      <div class="global-search-entry__panel">
      <RouterLink class="saved-link" to="/search/saved">Manage saved searches</RouterLink>
      <form @submit.prevent="search"><input v-model="query" type="search" placeholder="Work, people, business, things…" aria-label="Search across modules"><button type="submit" :disabled="loading">{{ loading ? "Searching" : "Search" }}</button></form>
      <p v-if="error" class="error" role="alert">{{ error }}</p>
      <div v-else-if="result" class="summary-row"><p class="summary">{{ result.summary }}</p><button type="button" @click="saveIntent">{{ saved ? "Saved" : "Save search" }}</button></div>
      <div v-if="result?.items?.length" class="results"><button v-for="item in result.items" :key="`${item.entityFamily}-${item.targetId}`" type="button" @click="open(item)"><span>{{ item.entityFamily }}</span><strong>{{ item.title }}</strong><small>{{ item.matchSummary || item.summary }}</small></button></div>
      <p v-else-if="result" class="empty">No permitted matches.</p>
    </div>
  </details>
</template>

<style scoped>
.global-search-entry{position:relative}.global-search-entry>summary{cursor:pointer;list-style:none;padding:.7rem .85rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;background:rgba(255,255,255,.65);font-weight:650}.global-search-entry>summary::-webkit-details-marker{display:none}.global-search-entry__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;width:min(34rem,calc(100vw - 2rem));display:grid;gap:.65rem;padding:1rem;border:1px solid rgba(23,34,26,.12);border-radius:1rem;background:#fcfcf8;box-shadow:0 18px 38px rgba(23,34,26,.16)}.global-search-entry__panel form{display:grid;grid-template-columns:1fr auto;gap:.4rem}.global-search-entry input,.global-search-entry form button{border:1px solid rgba(23,34,26,.14);border-radius:.7rem;padding:.6rem .7rem;font:inherit}.global-search-entry form button{background:#17221a;color:#f8f8f4;cursor:pointer}.results{display:grid;gap:.35rem;max-height:24rem;overflow:auto}.results button{display:grid;gap:.15rem;text-align:left;border:0;border-bottom:1px solid rgba(23,34,26,.08);background:transparent;padding:.55rem;cursor:pointer}.results span,.results small,.summary,.empty{color:rgba(23,34,26,.58);font-size:.78rem}.error{color:#8d2f25;font-size:.82rem}@media(max-width:640px){.global-search-entry__panel{position:fixed;right:1rem;top:4.5rem}}
</style>
