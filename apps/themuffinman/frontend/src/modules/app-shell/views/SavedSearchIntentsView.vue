<script setup lang="ts">
import {onMounted, ref} from "vue"
import {userShellApi, type SavedSearchIntent} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppButton from "../components/AppButton.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const items = ref<SavedSearchIntent[]>([])
const loading = ref(true); const error = ref(""); const feedback = ref(""); const actingId = ref<number | null>(null)
const load = async () => { loading.value = true; error.value = ""; try { items.value = await userShellApi.getSavedSearchIntents() } catch { error.value = "Could not load saved searches." } finally { loading.value = false } }
const toggle = async (item: SavedSearchIntent) => { actingId.value = item.id; try { const updated = await userShellApi.updateSavedSearchIntent(item.id, {query: item.query, entityFamily: item.entityFamily, paused: !item.paused, notifyEnabled: item.notifyEnabled, expiresAt: item.expiresAt}); items.value = items.value.map(current => current.id === updated.id ? updated : current); feedback.value = updated.paused ? "Search paused." : "Search resumed." } catch { error.value = "Could not update this search." } finally { actingId.value = null } }
const remove = async (item: SavedSearchIntent) => { if (!await confirmAction("Delete this saved search?", "Delete saved search")) return; actingId.value = item.id; try { await userShellApi.deleteSavedSearchIntent(item.id); items.value = items.value.filter(current => current.id !== item.id); feedback.value = "Saved search deleted." } catch { error.value = "Could not delete this search." } finally { actingId.value = null } }
onMounted(() => void load())
</script>

<template><section class="saved-searches"><header><p class="eyebrow">Search</p><h1>Saved searches</h1><p>Resume recurring discovery without exposing private source data.</p></header><CollectionToolbar title="Saved intents" :count="items.length" :busy="loading" /><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-if="loading" message="Loading saved searches." busy /><AppStatus v-else-if="!items.length" message="No saved searches yet." /><div v-else class="list"><SurfaceRow v-for="item in items" :key="item.id" :row="{id: String(item.id), title: item.query, description: item.entityFamily || 'All modules', badge: item.paused ? 'Paused' : 'Active'}"><template #actions><AppButton type="button" tone="secondary" :loading="actingId === item.id" @click="toggle(item)">{{ item.paused ? "Resume" : "Pause" }}</AppButton><AppButton type="button" tone="danger" :loading="actingId === item.id" @click="remove(item)">Delete</AppButton></template></SurfaceRow></div></section></template>

<style scoped>.saved-searches{display:grid;gap:var(--space-3);max-width:none}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.saved-searches header p:last-child{color:var(--text-muted)}.list{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.list :deep(.surface-row:last-child){border-bottom:0}.saved-searches__danger{color:var(--danger)}</style>
