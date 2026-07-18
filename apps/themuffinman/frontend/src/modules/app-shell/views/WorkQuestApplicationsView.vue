<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {QuestApplicationsViewDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {confirmAction} from "../composables/useActionDialog.ts"

const route = useRoute()
const view = ref<QuestApplicationsViewDTO | null>(null)
const isLoading = ref(true)
const isWorking = ref(false)
const error = ref("")
const feedback = ref("")
const replacingApplicationId = ref<number | null>(null)
const replacementApplicationId = ref<number | null>(null)
const questId = () => Number(route.params.questId)
const approvedApplications = computed(() => view.value?.approvedApplications ?? [])
const pendingApplications = computed(() => (view.value?.visibleApplications ?? []).filter(application => application.status === "PENDING"))
const load = async () => { isLoading.value = true; error.value = ""; try { view.value = await userShellApi.getQuestApplications(questId()) } catch { error.value = "Could not load applications." } finally { isLoading.value = false } }
const decide = async (applicationId: number, decision: "approve" | "decline") => { isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.decideQuestApplication(questId(), applicationId, decision); feedback.value = decision === "approve" ? "Application approved." : "Application declined."; await load() } catch { error.value = "Could not update this application." } finally { isWorking.value = false } }
const release = async (applicationId: number) => { if (!await confirmAction("Release this worker and reopen the available slot?", "Release worker")) return; isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.releaseQuestWorker(questId(), applicationId); feedback.value = "Worker assignment released."; await load() } catch { error.value = "Could not release this worker." } finally { isWorking.value = false } }
const replace = async () => { if (!replacingApplicationId.value || !replacementApplicationId.value) return; isWorking.value = true; error.value = ""; feedback.value = ""; try { await userShellApi.replaceQuestWorker(questId(), replacingApplicationId.value, replacementApplicationId.value); feedback.value = "Worker assignment replaced."; replacingApplicationId.value = null; replacementApplicationId.value = null; await load() } catch { error.value = "Could not replace this worker." } finally { isWorking.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="quest-applications">
    <header class="quest-applications__header"><div><p class="quest-applications__eyebrow">Work / Applications</p><h1>Applications</h1></div><RouterLink :to="`/work/quests/${questId()}`">Back to quest</RouterLink></header>
    <div v-if="isLoading" class="quest-applications__status" role="status">Loading.</div>
    <div v-else-if="error" class="quest-applications__status quest-applications__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <template v-else-if="view">
      <p v-if="feedback" class="quest-applications__feedback" role="status">{{ feedback }}</p>
      <p class="quest-applications__summary">{{ view.pendingApplicationCount }} pending · {{ view.hiddenApplicationsCount }} hidden</p>
      <div v-if="view.visibleApplications.length === 0 && view.approvedApplications.length === 0" class="quest-applications__status">No applications to review.</div>
      <div v-else class="quest-applications__list">
        <article v-for="application in [...approvedApplications, ...view.visibleApplications.filter(item => item.status !== 'APPROVED')]" :key="application.id" class="quest-applications__row" :aria-label="`${application.applicantUsername}, ${application.status}`">
          <div><strong>{{ application.applicantUsername }}</strong><span>{{ application.presentation.statusLabel }} · {{ application.message || "No message" }}</span></div>
          <div class="quest-applications__actions"><button v-if="application.allowedActions.includes('APPROVE')" type="button" :disabled="isWorking" @click="decide(application.id, 'approve')">Approve</button><button v-if="application.allowedActions.includes('DECLINE')" type="button" class="quest-applications__decline" :disabled="isWorking" @click="decide(application.id, 'decline')">Decline</button><template v-if="application.status === 'APPROVED'"><button type="button" class="quest-applications__decline" :disabled="isWorking" @click="release(application.id)">Release</button><button v-if="pendingApplications.length" type="button" :disabled="isWorking" @click="replacingApplicationId = application.id">Replace</button></template></div>
          <form v-if="replacingApplicationId === application.id" class="quest-applications__replace" @submit.prevent="replace"><label>Replacement applicant<select v-model.number="replacementApplicationId" required><option :value="null" disabled>Select pending applicant</option><option v-for="candidate in pendingApplications" :key="candidate.id" :value="candidate.id">{{ candidate.applicantUsername }}</option></select></label><button type="submit" :disabled="isWorking || !replacementApplicationId">Confirm replacement</button><button type="button" @click="replacingApplicationId = null">Cancel</button></form>
        </article>
      </div>
    </template>
  </section>
</template>

<style scoped>
.quest-applications{display:grid;gap:1rem}.quest-applications__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.quest-applications__eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.quest-applications__header a{font-size:.84rem}.quest-applications__summary,.quest-applications__status,.quest-applications__feedback{margin:0;padding:.6rem 0;color:var(--text-muted)}.quest-applications__feedback{color:var(--success)}.quest-applications__status--error{color:var(--danger)}.quest-applications__status button{margin-left:.6rem;border:0;background:none;text-decoration:underline;cursor:pointer}.quest-applications__list{display:grid;gap:.45rem}.quest-applications__row{display:flex;justify-content:space-between;gap:1rem;align-items:center;padding:.9rem 0;border:1px solid var(--border-subtle)}.quest-applications__row>div:first-child{display:grid;gap:.25rem;min-width:0}.quest-applications__row span{color:var(--text-muted);font-size:.84rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.quest-applications__actions{display:flex;gap:.4rem}.quest-applications__actions button{border:1px solid var(--border-subtle);border-radius:999px;background:var(--accent);color:var(--text);padding:.45rem .7rem;font-size:.78rem;font-weight:650;cursor:pointer}.quest-applications__actions .quest-applications__decline{background:transparent;color:var(--danger)}@media(max-width:700px){.quest-applications__row{align-items:start;flex-direction:column}.quest-applications__row span{white-space:normal}}
</style>
