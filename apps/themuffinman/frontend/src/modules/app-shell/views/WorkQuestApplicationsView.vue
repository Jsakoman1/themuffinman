<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {QuestApplicationsViewDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import ObjectPreviewPanel from "../components/ObjectPreviewPanel.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const route = useRoute()
const router = useRouter()
const view = ref<QuestApplicationsViewDTO | null>(null)
const isLoading = ref(true)
const isWorking = ref(false)
const error = ref("")
const feedback = ref("")
const showingAll = ref(false)
const replacingApplicationId = ref<number | null>(null)
const replacementApplicationId = ref<number | null>(null)
const selectedApplicationId = ref<number | null>(null)
const questId = () => Number(route.params.questId)
const approvedApplications = computed(() => view.value?.approvedApplications ?? [])
const pendingApplications = computed(() => (view.value?.visibleApplications ?? []).filter(application => application.status === "PENDING"))
const orderedApplications = computed(() => [...approvedApplications.value, ...(view.value?.visibleApplications ?? []).filter(item => item.status !== "APPROVED")])
const previewApplication = computed(() => orderedApplications.value.find(item => item.id === selectedApplicationId.value) ?? null)
const load = async (showAll = showingAll.value) => { isLoading.value = true; error.value = ""; try { view.value = await userShellApi.getQuestApplications(questId(), showAll); showingAll.value = view.value.showingAllApplications } catch { error.value = "Could not load applications." } finally { isLoading.value = false } }
const decide = async (applicationId: number, decision: "approve" | "decline") => { isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.decideQuestApplication(questId(), applicationId, decision); feedback.value = decision === "approve" ? "Application approved." : "Application declined."; await load() } catch { error.value = "Could not update this application." } finally { isWorking.value = false } }
const release = async (applicationId: number) => { if (!await confirmAction("Release this worker and reopen the available slot?", "Release worker")) return; isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.releaseQuestWorker(questId(), applicationId); feedback.value = "Worker assignment released."; await load() } catch { error.value = "Could not release this worker. The quest may have changed, so the application list was refreshed."; await load() } finally { isWorking.value = false } }
const replace = async () => { if (!replacingApplicationId.value || !replacementApplicationId.value) return; isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.replaceQuestWorker(questId(), replacingApplicationId.value, replacementApplicationId.value); feedback.value = "Worker assignment replaced."; replacingApplicationId.value = null; replacementApplicationId.value = null; await load() } catch { error.value = "Could not replace this worker. The quest may have changed, so the application list was refreshed."; replacingApplicationId.value = null; replacementApplicationId.value = null; await load() } finally { isWorking.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="quest-applications">
    <header class="quest-applications__header"><div><p class="quest-applications__eyebrow">Work / Applications</p><h1>Applications</h1></div><RouterLink :to="`/work/quests/${questId()}`">Back to quest</RouterLink></header><CollectionToolbar title="Applicant review" :count="orderedApplications.length" :busy="isLoading" />
    <AppStatus v-if="isLoading" message="Loading applications." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" />
    <template v-else-if="view">
      <AppStatus v-if="feedback" :message="feedback" tone="success" />
      <div class="quest-applications__summary"><span>{{ view.pendingApplicationCount }} pending · {{ view.hiddenApplicationsCount }} hidden</span><AppButton v-if="view.canRevealHiddenApplications" type="button" tone="quiet" @click="load(!view.showingAllApplications)">{{ view.revealLabel }}</AppButton></div>
      <AppStatus v-if="orderedApplications.length === 0" message="No applications to review." /><div v-else class="quest-applications__workspace"><div class="quest-applications__list"><SurfaceRow v-for="application in orderedApplications" :key="application.id" :row="{id: String(application.id), title: application.applicantUsername, description: application.message || 'No message', badge: application.presentation.statusLabel}" :selected="selectedApplicationId === application.id" :previewed="previewApplication?.id === application.id" @click="selectedApplicationId = application.id"><template #actions><AppButton v-if="application.allowedActions.includes('APPROVE')" type="button" tone="primary" :loading="isWorking" @click.stop="decide(application.id, 'approve')">Approve</AppButton><AppButton v-if="application.allowedActions.includes('DECLINE')" type="button" tone="danger" :loading="isWorking" @click.stop="decide(application.id, 'decline')">Decline</AppButton><AppButton v-if="application.allowedActions.includes('RELEASE_WORKER')" type="button" tone="danger" :loading="isWorking" @click.stop="release(application.id)">Release</AppButton><AppButton v-if="application.allowedActions.includes('REPLACE_WORKER') && pendingApplications.length" type="button" tone="secondary" :loading="isWorking" @click.stop="replacingApplicationId = application.id">Replace</AppButton></template></SurfaceRow></div><ObjectPreviewPanel :title="previewApplication?.applicantUsername ?? 'Applicant'" subtitle="Application review" :open="previewApplication !== null" detail-label="Back to quest" @close="selectedApplicationId = null" @open-detail="router.push(`/work/quests/${questId()}`)"><p>{{ previewApplication?.message || 'No application message.' }}</p><p v-if="previewApplication">{{ previewApplication.presentation.statusLabel }}</p></ObjectPreviewPanel></div>
      <AppDialog :open="replacingApplicationId !== null" title="Replace worker" layout="workspace" @close="replacingApplicationId = null; replacementApplicationId = null"><form class="quest-applications__replace" @submit.prevent="replace"><AppFormField label="Replacement applicant" required><select v-model.number="replacementApplicationId" required><option :value="null" disabled>Select pending applicant</option><option v-for="candidate in pendingApplications" :key="candidate.id" :value="candidate.id">{{ candidate.applicantUsername }}</option></select></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="replacingApplicationId = null; replacementApplicationId = null">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isWorking" :disabled="!replacementApplicationId">Confirm replacement</AppButton></template></AppFormFooter></form></AppDialog>
    </template>
  </section>
</template>

<style scoped>
.quest-applications{display:grid;gap:var(--space-3)}.quest-applications__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.quest-applications__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.quest-applications__header a,.quest-applications__summary{color:var(--text-muted);font-size:var(--text-size-meta)}.quest-applications__summary{display:flex;align-items:center;justify-content:space-between;gap:var(--space-2);margin:0}.quest-applications__summary button{min-height:var(--control-height-default);border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);background:var(--control-bg);color:var(--control-ink);font:inherit;font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);cursor:pointer}.quest-applications__summary button:hover{border-color:var(--control-border-active);background:var(--control-bg-hover)}.quest-applications__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,var(--detail-rail-width));overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.quest-applications__list{min-width:0}.quest-applications__list :deep(.surface-row:last-child){border-bottom:0}.quest-applications__decline{color:var(--danger)}.quest-applications__replace{display:grid;gap:var(--space-3)}.quest-applications__replace select{width:100%;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}@media(max-width:980px){.quest-applications__workspace{grid-template-columns:1fr}.quest-applications__summary{align-items:start;flex-direction:column}}
</style>
<style scoped>
.quest-applications .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.quest-applications .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.quest-applications .app-button--danger { color:var(--danger); }
</style>
