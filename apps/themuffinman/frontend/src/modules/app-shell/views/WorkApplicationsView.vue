<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {QuestApplicationResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import CollectionContextRail from "../components/CollectionContextRail.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import {formatDateTime} from "../../../services/formatters.ts"

const items = ref<QuestApplicationResponseDTO[]>([])
const page = ref(0)
const totalItems = ref(0)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
const feedback = ref("")
const editingId = ref<number | null>(null)
const editMessage = ref("")
const editPrice = ref<number | null>(null)
const hasMore = computed(() => items.value.length < totalItems.value)
const selectedApplicationId = ref<number | null>(null)
const selectedApplication = computed(() => items.value.find(item => item.id === selectedApplicationId.value) ?? null)
const formatDate = (value: string | null | undefined) => formatDateTime(value, "No date", {month: "short", day: "numeric"})

const load = async (reset = true) => {
  if (reset) { isLoading.value = true; page.value = 0; items.value = [] } else { isLoadingMore.value = true }
  error.value = ""
  try {
    const response = await userShellApi.getMyApplications(page.value, 20)
    items.value = reset ? response.items : [...items.value, ...response.items]
    if (!items.value.some(item => item.id === selectedApplicationId.value)) selectedApplicationId.value = items.value[0]?.id ?? null
    totalItems.value = response.totalItems
    page.value = response.page
  } catch { error.value = "Could not load your applications." }
  finally { isLoading.value = false; isLoadingMore.value = false }
}
const loadMore = async () => { if (!hasMore.value || isLoadingMore.value) return; page.value += 1; await load(false) }
const beginEdit = (application: QuestApplicationResponseDTO) => { editingId.value = application.id; editMessage.value = application.message; editPrice.value = application.proposedPrice }
const saveEdit = async (application: QuestApplicationResponseDTO) => { isLoadingMore.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.updateMyApplication(application.questId, {message: editMessage.value, proposedPrice: editPrice.value}); editingId.value = null; feedback.value = "Application updated."; await load() } catch { error.value = "Could not update this application. Review the fields and try again." } finally { isLoadingMore.value = false } }
const withdraw = async (application: QuestApplicationResponseDTO) => { if (!await confirmAction(`Withdraw your application for “${application.questTitle}”?`, "Withdraw application")) return; isLoadingMore.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.withdrawMyApplication(application.questId); feedback.value = "Application withdrawn."; await load() } catch { error.value = "Could not withdraw this application. The application may have changed; retry to refresh." } finally { isLoadingMore.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="applications-surface">
    <header class="applications-surface__header"><div><p class="applications-surface__eyebrow">Work / My applications</p><h1>My applications</h1></div></header><CollectionToolbar title="My applications" :count="totalItems" :busy="isLoading" />
  <AppStatus v-if="isLoading" message="Loading your applications." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-else-if="!isLoading && !error && items.length === 0" message="You have not submitted any applications yet." />
    <div v-else class="applications-surface__workspace"><div class="applications-surface__list"><SurfaceRow v-for="application in items" :key="application.id" :row="{id: String(application.id), title: application.questTitle, description: `${application.questCreatorUsername} · ${formatDate(application.createdAt)}`, badge: application.presentation.statusLabel, meta: application.proposedPrice == null ? 'No proposed price' : `${application.proposedPrice} €`}" :selected="selectedApplicationId === application.id" @click="selectedApplicationId = application.id"><template #actions><AppButton v-if="application.allowedActions.includes('EDIT')" type="button" tone="secondary" @click.stop="beginEdit(application)">Edit</AppButton><AppButton v-if="application.allowedActions.includes('WITHDRAW')" type="button" tone="danger" :loading="isLoadingMore" @click.stop="withdraw(application)">Withdraw</AppButton></template></SurfaceRow></div><CollectionContextRail title="Application context" subtitle="Submitted by you" open-label="Show application context"><template v-if="selectedApplication"><p class="applications-surface__eyebrow">Selected application</p><h2>{{ selectedApplication.questTitle }}</h2><p>{{ selectedApplication.questDescription || 'No work description provided.' }}</p><dl><div><dt>Owner</dt><dd>{{ selectedApplication.questCreatorUsername }}</dd></div><div><dt>Status</dt><dd>{{ selectedApplication.presentation.statusLabel }}</dd></div><div><dt>Submitted</dt><dd>{{ formatDate(selectedApplication.createdAt) }}</dd></div></dl><RouterLink class="applications-surface__context-link" :to="resolveSurfaceDetailRoute('work-applications', selectedApplication.id) ?? `/work/applications/${selectedApplication.id}`">Open full detail</RouterLink></template><template v-else><p class="applications-surface__eyebrow">Application context</p><h2>Select an application</h2><p>Choose an application to inspect it without leaving this list.</p></template></CollectionContextRail></div>
    <AppDialog :open="editingId !== null" title="Edit application" layout="workspace" @close="editingId = null"><form v-if="editingId !== null" class="applications-surface__edit" @submit.prevent="saveEdit(items.find(item => item.id === editingId)!)"><AppFormField label="Message" required><textarea v-model="editMessage" required maxlength="2000"></textarea></AppFormField><AppFormField label="Proposed price" optional><input v-model.number="editPrice" type="number" min="0" step="0.01"></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="editingId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isLoadingMore">Save</AppButton></template></AppFormFooter></form></AppDialog>
    <AppButton v-if="hasMore" type="button" tone="quiet" :loading="isLoadingMore" @click="loadMore">{{ isLoadingMore ? "Loading" : "Load more" }}</AppButton>
  </section>
</template>

<style scoped>
.applications-surface{display:grid;gap:var(--space-3)}.applications-surface__header{display:flex;align-items:end}.applications-surface__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.applications-surface__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,var(--detail-rail-width));overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.applications-surface__list{min-width:0}.applications-surface__list :deep(.surface-row:last-child){border-bottom:0}.applications-surface__withdraw{color:var(--danger)}.applications-surface__edit{display:grid;gap:var(--space-3)}.applications-surface__edit textarea,.applications-surface__edit input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.applications-surface__edit textarea{min-height:7rem}@media(max-width:980px){.applications-surface__workspace{grid-template-columns:1fr}}
.applications-surface__workspace { box-shadow: none; }
.applications-surface__context { border-left: 1px solid var(--border-subtle); padding: var(--space-3); background: var(--surface-raised); }
.applications-surface__context p { color: var(--text-muted); }
.applications-surface__context h2 { margin: var(--space-1) 0; font-size: var(--text-size-title); }
.applications-surface__context dl { display: grid; gap: var(--space-2); margin: var(--space-3) 0; }
.applications-surface__context dl div { display: flex; justify-content: space-between; gap: var(--space-2); border-bottom: 1px solid var(--border-subtle); padding-bottom: var(--space-1); }
.applications-surface__context dt { color: var(--text-soft); font-size: var(--text-size-meta); }
.applications-surface__context dd { margin: 0; text-align: right; }
.applications-surface__context-link { display: inline-flex; margin-top: var(--space-2); font-weight: var(--text-weight-semibold); }
.applications-surface__withdraw:hover { border-color: var(--danger); background: var(--danger-muted); color: var(--danger); }
.applications-surface__edit :deep(.app-form-footer) { border-top: 1px solid var(--border-subtle); }
@media(max-width:980px){.applications-surface__context{border-left:0;border-top:1px solid var(--border-subtle)}}
</style>
<style scoped>
.applications-surface .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.applications-surface .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.applications-surface .app-button--danger { color:var(--danger); }
</style>
