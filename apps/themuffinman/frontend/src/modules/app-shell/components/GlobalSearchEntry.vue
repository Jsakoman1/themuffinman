<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {VisionSearchDiscoveryDTO, VisionSearchDiscoveryItemDTO} from "../../../contracts/index.ts"
import {userShellApi, type WorkspaceCommandCatalog} from "../api/userShellApi.ts"

const router = useRouter()
const query = ref("")
const result = ref<VisionSearchDiscoveryDTO | null>(null)
const error = ref("")
const loading = ref(false)
const saved = ref(false)
const openPanel = ref(false)
const catalog = ref<WorkspaceCommandCatalog | null>(null)
const commands = computed(() => catalog.value ? [...catalog.value.personal, ...catalog.value.navigation, ...catalog.value.create, ...catalog.value.vision] : [])
const focusSearch = async () => { openPanel.value = true; try { catalog.value = await userShellApi.getWorkspaceCommandCatalog() } catch { error.value = "Could not load commands." }; requestAnimationFrame(() => document.querySelector<HTMLInputElement>(".global-search-entry input")?.focus()) }
const togglePanel = () => { if (openPanel.value) { openPanel.value = false; return }; void focusSearch() }
onMounted(() => window.addEventListener("app:open-command", focusSearch))
const closeOnEscape = (event: KeyboardEvent) => { if (event.key === "Escape") openPanel.value = false }
onMounted(() => window.addEventListener("keydown", closeOnEscape))
onBeforeUnmount(() => { window.removeEventListener("app:open-command", focusSearch); window.removeEventListener("keydown", closeOnEscape) })
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
const openCommand = async (route: string) => { openPanel.value = false; await router.push(route) }
</script>

<template>
  <details :open="openPanel" class="global-search-entry" @toggle="openPanel = ($event.currentTarget as HTMLDetailsElement).open">
    <summary aria-label="Open command center" title="Open command center (Ctrl or Command K)" @click.prevent="togglePanel">Search</summary>
      <div class="global-search-entry__panel">
      <p class="global-search-entry__scope">Command center · permitted routes and records only</p>
      <div v-if="commands.length" class="commands"><button v-for="command in commands" :key="command.id" type="button" @click="openCommand(command.route)"><span>{{ command.group }}</span><strong>{{ command.label }}</strong><small>{{ command.description }}</small></button></div>
      <div class="global-search-entry__destinations"><RouterLink to="/work/find">Find work</RouterLink><RouterLink to="/people">Find people</RouterLink><RouterLink to="/business/find">Find a business</RouterLink></div>
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
.global-search-entry{position:relative}.global-search-entry>summary{cursor:pointer;list-style:none;padding:.48rem .65rem;border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface);color:var(--text-muted);font-size:.8rem;font-weight:650}.global-search-entry>summary:hover{border-color:var(--border-strong);color:var(--text)}.global-search-entry>summary::-webkit-details-marker{display:none}.global-search-entry__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;width:min(34rem,calc(100vw - 2rem));display:grid;gap:.65rem;padding:1rem;border:1px solid var(--border-strong);border-radius:var(--radius-card);background:var(--surface-strong);box-shadow:var(--shadow-card)}.global-search-entry__scope{margin:0;color:var(--text-soft);font-size:.72rem}.global-search-entry__destinations{display:flex;gap:.4rem;flex-wrap:wrap}.global-search-entry__destinations a{border:1px solid var(--border-subtle);border-radius:999px;padding:.35rem .55rem;color:var(--text-muted);font-size:.74rem}.global-search-entry__panel form{display:grid;grid-template-columns:1fr auto;gap:.4rem}.global-search-entry input,.global-search-entry form button{border:1px solid var(--border-subtle);border-radius:var(--radius-control);padding:.58rem .7rem;background:var(--bg-raised);color:var(--text);font:inherit}.global-search-entry form button{background:var(--accent);border-color:var(--accent);color:#121217;cursor:pointer}.results{display:grid;gap:.2rem;max-height:24rem;overflow:auto}.results button{display:grid;gap:.15rem;text-align:left;border:0;border-bottom:1px solid var(--border-subtle);background:transparent;color:var(--text);padding:.55rem;cursor:pointer}.results button:hover{background:var(--surface-hover)}.results span,.results small,.summary,.empty{color:var(--text-muted);font-size:.78rem}.error{color:var(--danger);font-size:.82rem}@media(max-width:640px){.global-search-entry__panel{position:fixed;right:1rem;top:4.5rem}}
</style>
