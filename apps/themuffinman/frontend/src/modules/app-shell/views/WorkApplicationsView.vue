<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
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
import {confirmAction} from "../composables/useActionDialog.ts"
import {formatDateTime} from "../../../services/formatters.ts"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

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
const formatDate = (value: string | null | undefined) => formatDateTime(value, "No date", {month: "short", day: "numeric"})
const surface = getAppSurfaceConfig("work-applications")

const load = async (reset = true) => {
  if (reset) { isLoading.value = true; page.value = 0; items.value = [] } else { isLoadingMore.value = true }
  error.value = ""
  try {
    const response = await userShellApi.getMyApplications(page.value, 20)
    items.value = reset ? response.items : [...items.value, ...response.items]
    totalItems.value = response.totalItems
    page.value = response.page
  } catch { error.value = "Could not load your help requests." }
  finally { isLoading.value = false; isLoadingMore.value = false }
}
const loadMore = async () => { if (!hasMore.value || isLoadingMore.value) return; page.value += 1; await load(false) }
const beginEdit = (application: QuestApplicationResponseDTO) => { editingId.value = application.id; editMessage.value = application.message; editPrice.value = application.proposedPrice }
const saveEdit = async (application: QuestApplicationResponseDTO) => { isLoadingMore.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.updateMyApplication(application.questId, {message: editMessage.value, proposedPrice: editPrice.value}); editingId.value = null; feedback.value = "Help request updated."; await load() } catch (cause) { error.value = userShellApi.actionFailureMessage("Could not update this help request. Review the fields and try again.", cause) } finally { isLoadingMore.value = false } }
const withdraw = async (application: QuestApplicationResponseDTO) => { if (!await confirmAction(`Withdraw your request to help with “${application.questTitle}”?`, "Withdraw request")) return; isLoadingMore.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.withdrawMyApplication(application.questId); feedback.value = "Help request withdrawn."; await load() } catch { error.value = "Could not withdraw this request. It may have changed; refresh and try again." } finally { isLoadingMore.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="applications-surface">
    <SurfaceHeader :config="surface" description="Track the requests you sent to help with SideJobs and see what happens next." />
    <CollectionToolbar title="My help requests" :count="totalItems" :busy="isLoading" /><p class="applications-surface__contract-note">You can change or withdraw a request only while the SideJob still allows it.</p>
  <AppStatus v-if="isLoading" message="Loading your help requests." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-else-if="!isLoading && !error && items.length === 0" message="You have not offered to help with a SideJob yet." />
    <div v-else class="applications-surface__workspace"><div class="applications-surface__list"><SurfaceRow v-for="application in items" :key="application.id" :row="{id: String(application.id), title: application.questTitle, description: `${application.questCreatorUsername} · Sent ${formatDate(application.createdAt)}`, badge: application.presentation.statusLabel, meta: application.proposedPrice == null ? 'No price proposal' : `Your offer: ${application.proposedPrice} €`, to: resolveSurfaceDetailRoute('work-applications', application.id) ?? `/work/applications/${application.id}`}"><template #actions><AppButton v-if="application.allowedActions.includes('EDIT')" type="button" tone="secondary" @click.stop="beginEdit(application)">Edit request</AppButton><AppButton v-if="application.allowedActions.includes('WITHDRAW')" type="button" tone="danger" :loading="isLoadingMore" @click.stop="withdraw(application)">Withdraw request</AppButton></template></SurfaceRow></div></div>
    <AppDialog :open="editingId !== null" title="Edit your help request" layout="workspace" @close="editingId = null"><form v-if="editingId !== null" class="applications-surface__edit" @submit.prevent="saveEdit(items.find(item => item.id === editingId)!)"><AppFormField label="Message" required><textarea v-model="editMessage" required maxlength="2000"></textarea></AppFormField><AppFormField label="Your price proposal" optional><input v-model.number="editPrice" type="number" min="0" step="0.01"></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="editingId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isLoadingMore">Save request</AppButton></template></AppFormFooter></form></AppDialog>
    <AppButton v-if="hasMore" type="button" tone="quiet" :loading="isLoadingMore" @click="loadMore">{{ isLoadingMore ? "Loading" : "Load more" }}</AppButton>
  </section>
</template>

<style scoped>
.applications-surface{display:grid;gap:var(--space-3)}.applications-surface__workspace{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.applications-surface__list{min-width:0}.applications-surface__list :deep(.surface-row:last-child){border-bottom:0}.applications-surface__withdraw{color:var(--danger)}.applications-surface__edit{display:grid;gap:var(--space-3)}.applications-surface__edit textarea,.applications-surface__edit input{width:100%;box-sizing:border-box;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}.applications-surface__edit textarea{min-height:7rem}
.applications-surface__workspace { box-shadow: none; }
.applications-surface__withdraw:hover { border-color: var(--danger); background: var(--danger-muted); color: var(--danger); }
.applications-surface__edit :deep(.app-form-footer) { border-top: 1px solid var(--border-subtle); }
.applications-surface .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.applications-surface .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.applications-surface .app-button--danger { color:var(--danger); }
</style>
