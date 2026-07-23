<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink, useRouter} from "vue-router"
import type {VisionSearchComparison, VisionSearchDiscoveryDTO, VisionSearchDiscoveryItemDTO} from "../../../contracts/index.ts"
import {userShellApi, type WorkspaceCommandCatalog} from "../api/userShellApi.ts"

const router = useRouter()
const query = ref("")
const result = ref<VisionSearchDiscoveryDTO | null>(null)
const comparison = ref<VisionSearchComparison | null>(null)
const selectedKeys = ref<string[]>([])
const error = ref("")
const loading = ref(false)
const searchPage = ref(0)
const selectedFamily = ref<string | undefined>(undefined)
const saved = ref(false)
const openPanel = ref(false)
const catalog = ref<WorkspaceCommandCatalog | null>(null)
const commandGroups = computed(() => catalog.value ? ([
  ["Personal", catalog.value.personal], ["Navigate", catalog.value.navigation], ["Create", catalog.value.create], ["Vision", catalog.value.vision]
] as const).filter(([, items]) => items.length) : [])
const focusSearch = async () => { openPanel.value = true; try { catalog.value = await userShellApi.getWorkspaceCommandCatalog() } catch { error.value = "Could not load commands." }; requestAnimationFrame(() => document.querySelector<HTMLInputElement>(".global-search-entry input")?.focus()) }
const togglePanel = () => { if (openPanel.value) { openPanel.value = false; return }; void focusSearch() }
onMounted(() => window.addEventListener("app:open-command", focusSearch))
const closeOnEscape = (event: KeyboardEvent) => { if (event.key === "Escape") openPanel.value = false }
const commandShortcut = (event: KeyboardEvent) => { const target = event.target; const editing = target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement || (target instanceof HTMLElement && target.isContentEditable); if (!editing && (event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") { event.preventDefault(); void focusSearch() } }
onMounted(() => window.addEventListener("keydown", closeOnEscape))
onMounted(() => window.addEventListener("keydown", commandShortcut))
onBeforeUnmount(() => { window.removeEventListener("app:open-command", focusSearch); window.removeEventListener("keydown", closeOnEscape); window.removeEventListener("keydown", commandShortcut) })
const search = async (page = 0) => {
  if (!query.value.trim()) return
  loading.value = true; error.value = ""; comparison.value = null; if (page === 0) selectedKeys.value = []
  try { result.value = await userShellApi.searchUniversal(query.value.trim(), page, selectedFamily.value); searchPage.value = page }
  catch { error.value = "Could not search right now." }
  finally { loading.value = false }
}
const selectionKey = (item: VisionSearchDiscoveryItemDTO) => `${item.entityFamily}:${item.targetId}`
const toggleSelection = (item: VisionSearchDiscoveryItemDTO) => {
  const key = selectionKey(item)
  selectedKeys.value = selectedKeys.value.includes(key)
    ? selectedKeys.value.filter(value => value !== key)
    : selectedKeys.value.length < 3 ? [...selectedKeys.value, key] : selectedKeys.value
}
const compare = async () => {
  if (!query.value.trim() || selectedKeys.value.length < 1) return
  loading.value = true; error.value = ""
  try { comparison.value = await userShellApi.compareUniversal(query.value.trim(), selectedKeys.value) }
  catch { error.value = "Could not compare these permitted results." }
  finally { loading.value = false }
}
const saveIntent = async () => { if (!query.value.trim()) return; try { await userShellApi.createSavedSearchIntent({query: query.value.trim(), notifyEnabled: true}); saved.value = true } catch { error.value = "Could not save this search." } }
const destination = (item: VisionSearchDiscoveryItemDTO) => {
  if (item.detailRoute) return item.detailRoute
  if (!item.targetId) return null
  if (item.entityFamily === "quest") return `/work/quests/${item.targetId}`
  if (item.entityFamily === "thing") return `/things/${item.targetId}`
  if (item.entityFamily === "circle") return "/circles"
  if (item.entityFamily === "user") return `/people/${item.targetId}`
  if (item.entityFamily === "business") return `/business/public/${item.resolutionLabel || item.targetId}`
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
      <div v-if="commandGroups.length" class="commands"><section v-for="[group, commands] in commandGroups" :key="group"><p>{{ group }}</p><button v-for="command in commands" :key="command.id" type="button" @click="openCommand(command.route)"><strong>{{ command.label }}</strong><small>{{ command.description }}</small></button></section></div>
      <RouterLink class="saved-link" to="/search/saved">Manage saved searches</RouterLink>
      <form @submit.prevent="search()"><input v-model="query" type="search" placeholder="Work, people, business, things…" aria-label="Search across modules"><select v-model="selectedFamily" aria-label="Filter search family"><option :value="undefined">All modules</option><option v-for="family in (result?.availableEntityFamilies || [])" :key="family" :value="family">{{ family }}</option></select><button type="submit" :disabled="loading">{{ loading ? "Searching" : "Search" }}</button></form>
      <div v-if="error" class="error-state" role="alert"><p class="error">{{ error }}</p><button type="button" @click="search(searchPage)">Retry</button></div>
      <div v-else-if="result" class="summary-row"><p class="summary">{{ result.summary }}</p><button type="button" @click="saveIntent">{{ saved ? "Saved" : "Save search" }}</button></div>
      <div v-if="result?.items?.length" class="results"><div v-for="item in result.items" :key="`${item.entityFamily}-${item.targetId}`" class="result-row"><input type="checkbox" :checked="selectedKeys.includes(selectionKey(item))" :aria-label="`Select ${item.title} for comparison`" @change="toggleSelection(item)"><button type="button" @click="open(item)"><span>{{ item.entityFamily }}</span><strong>{{ item.title }}</strong><small>{{ item.matchSummary || item.summary }}</small></button></div><button type="button" class="compare-action" :disabled="selectedKeys.length === 0 || loading" @click="compare">Compare selected ({{ selectedKeys.length }}/3)</button></div>
      <p v-else-if="result" class="empty">{{ result.recoveryCode === "ENTER_QUERY" ? "Enter a search to discover permitted records." : result.recoveryCode === "REFINE_QUERY" ? "No permitted matches. Try a more specific query." : "No permitted matches for this filter." }}</p>
      <button v-if="result?.hasMore && !loading" type="button" class="next-page" @click="search(searchPage + 1)">Show more results</button>
      <section v-if="comparison" class="comparison" aria-label="Search comparison"><p class="summary">{{ comparison.items.length }} permitted results compared.</p><p v-if="comparison.fallbackMessage" class="fallback" role="status">{{ comparison.fallbackMessage }}</p><div v-for="item in comparison.items" :key="`${item.entityFamily}-${item.targetId}`" class="comparison-item"><strong>{{ item.title }}</strong><small>{{ item.entityFamily }} · <RouterLink :to="item.sourceRoute">Open source</RouterLink></small><dl><template v-for="field in comparison.comparableFields" :key="field"><dt>{{ field.replaceAll("_", " ") }}</dt><dd>{{ item.fields[field] || "Not available" }}</dd></template></dl></div></section>
    </div>
  </details>
</template>

<style scoped>
.global-search-entry{position:relative}.global-search-entry>summary{cursor:pointer;list-style:none;padding:var(--space-1) var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:transparent;color:var(--text-muted);font-size:var(--text-size-body);font-weight:var(--text-weight-semibold)}.global-search-entry>summary:hover{border-color:var(--control-border-active);color:var(--text)}.global-search-entry>summary::-webkit-details-marker{display:none}.global-search-entry__panel{position:fixed;inset-inline-start:max(var(--space-3),env(safe-area-inset-left));inset-inline-end:max(var(--space-3),env(safe-area-inset-right));top:calc(var(--workspace-header-height) + var(--space-2));z-index:var(--z-popover);width:min(34rem,calc(100vw - (2 * var(--space-3))));max-height:calc(100dvh - var(--workspace-header-height) - (2 * var(--space-3)));overflow:auto;display:grid;gap:var(--space-3);padding:var(--space-3);border:1px solid var(--border-strong);border-radius:var(--radius-surface);background:var(--surface-raised);box-shadow:var(--shadow-overlay);box-sizing:border-box}.global-search-entry__scope,.commands section>p{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.commands{display:grid;gap:var(--space-2)}.commands section{display:grid;gap:var(--space-1)}.commands button{display:grid;gap:var(--space-1);min-height:var(--control-height-default);padding:var(--space-1) var(--space-2);border:0;border-radius:var(--radius-control);background:transparent;color:var(--text);text-align:left;cursor:pointer}.commands button:hover,.commands button:focus-visible{background:var(--surface-hover)}.commands small,.results span,.results small,.summary,.empty{color:var(--text-muted);font-size:var(--text-size-meta)}.global-search-entry__panel form{display:grid;grid-template-columns:1fr auto;gap:var(--space-2)}.global-search-entry input,.global-search-entry form button{border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.global-search-entry form button{background:var(--accent);border-color:var(--accent);color:var(--canvas);cursor:pointer}.results{display:grid;gap:var(--space-1);max-height:24rem;overflow:auto}.result-row{display:grid;grid-template-columns:auto minmax(0,1fr);align-items:start;gap:var(--space-1)}.result-row>input{margin-top:var(--space-2)}.result-row button{width:100%}.compare-action{width:100%;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);cursor:pointer}.compare-action:disabled{opacity:.55;cursor:not-allowed}.comparison{display:grid;gap:var(--space-2);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.comparison-item{display:grid;gap:var(--space-1);border:1px solid var(--border-subtle);padding:var(--space-2)}.comparison-item small,.comparison-item dt,.comparison-item dd{color:var(--text-muted);font-size:var(--text-size-meta)}.comparison-item dl{display:grid;grid-template-columns:auto 1fr;gap:var(--space-1) var(--space-2);margin:0}.comparison-item dd{margin:0;color:var(--text)}.error{color:var(--danger);font-size:var(--text-size-body)}
</style>
