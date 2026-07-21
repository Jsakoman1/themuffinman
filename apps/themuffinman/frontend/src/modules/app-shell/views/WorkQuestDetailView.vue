<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {QuestApplicationRequestDTO, QuestDetailResponseDTO, QuestRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {buildSurfaceVisionRoute} from "../visionHandoff.ts"
import RichTextEditor from "../components/RichTextEditor.vue"
import RichTextPreview from "../components/RichTextPreview.vue"
import AppDialog from "../components/AppDialog.vue"
import DetailSurface from "../components/DetailSurface.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppButton from "../components/AppButton.vue"
import {confirmAction} from "../composables/useActionDialog.ts"

const route = useRoute()
const detail = ref<QuestDetailResponseDTO | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const editing = ref(false)
const form = ref<QuestRequestDTO | null>(null)
const reviewStars = ref(5)
const reviewComment = ref("")
const applicationOpen = ref(false)
const applicationMessage = ref("")
const applicationPrice = ref<number | null>(null)
const utilityOpen = ref(true)
const questId = computed(() => Number(route.params.questId))
const can = (action: string) => detail.value?.quest.allowedActions.includes(action as never) ?? false
const formatCurrency = (value: number | null | undefined) => value == null ? "Not specified" : new Intl.NumberFormat(undefined, {style: "currency", currency: "EUR"}).format(value)

const load = async () => {
  isLoading.value = true; error.value = ""
  try {
    detail.value = await userShellApi.getQuestDetail(questId.value)
    const quest = detail.value.quest
    form.value = {title: quest.title, description: quest.description, awardAmount: quest.awardAmount, assigneeTarget: quest.assigneeTarget, showApprovedApplicants: quest.showApprovedApplicants, scheduledAt: quest.scheduledAt, endsAt: quest.endsAt, termFixed: quest.termFixed, audience: quest.audience, locationVisibility: quest.locationVisibility, locationSource: quest.locationSource, locationLabel: quest.locationLabel, locationCountry: quest.locationCountry, locationLocality: quest.locationLocality, locationPostalCode: quest.locationPostalCode, locationStreet: quest.locationStreet, locationHouseNumber: quest.locationHouseNumber}
  } catch { error.value = "Could not load this quest." } finally { isLoading.value = false }
}
const save = async () => {
  if (!form.value) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.updateQuest(questId.value, form.value); feedback.value = "Quest updated."; editing.value = false; await load() } catch { error.value = "Could not update this quest." } finally { isSaving.value = false }
}
const runAction = async (action: "START" | "COMPLETE" | "DELETE" | "CANCEL" | "PAUSE" | "RESUME") => {
  if (action === "DELETE" && !await confirmAction("Delete this quest?", "Delete quest")) return
  if (action === "CANCEL" && !await confirmAction("Cancel this quest and notify affected applicants or workers?", "Cancel quest")) return
  if (action === "PAUSE" && !await confirmAction("Pause this quest and notify affected applicants or workers?", "Pause quest")) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.executeQuestAction(questId.value, action); feedback.value = action === "DELETE" ? "Quest deleted." : action === "CANCEL" ? "Quest cancelled." : action === "PAUSE" ? "Quest paused." : action === "RESUME" ? "Quest resumed." : "Quest updated."; await load() } catch { error.value = "This action could not be completed." } finally { isSaving.value = false }
}
const decideTerm = async (decision: "confirm" | "reject") => {
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.decideQuestTerm(questId.value, decision); feedback.value = decision === "confirm" ? "Term change confirmed." : "Term change rejected."; await load() } catch { error.value = "Could not resolve the term change." } finally { isSaving.value = false }
}
const changeLifecycle = async (status: "ASSIGNED" | "OPEN") => {
  if (!form.value) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.updateQuest(questId.value, {...form.value, status}); feedback.value = status === "ASSIGNED" ? "Quest assigned." : "Quest reopened."; await load() }
  catch { error.value = status === "ASSIGNED" ? "Could not assign this quest." : "Could not reopen this quest." }
  finally { isSaving.value = false }
}
const openApplication = () => {
  const rules = detail.value?.quest.presentation.applicationDraftRules
  applicationPrice.value = rules?.suggestedApplicationPrice ?? null
  applicationMessage.value = ""
  applicationOpen.value = true
}
const submitApplication = async () => {
  if (!detail.value) return
  const request: QuestApplicationRequestDTO = {message: applicationMessage.value.trim(), proposedPrice: applicationPrice.value}
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.applyForQuest(questId.value, request); feedback.value = "Application sent."; applicationOpen.value = false; await load() }
  catch { error.value = "Could not send this application. Check the message, offer, and quest eligibility." }
  finally { isSaving.value = false }
}
const submitReview = async () => {
  const review = detail.value?.sections.review
  if (!review?.target) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.submitQuestReview(questId.value, {reviewedUserId: review.target.userId, stars: reviewStars.value, comment: reviewComment.value}); feedback.value = "Review submitted."; reviewComment.value = ""; await load() } catch { error.value = "Could not submit this review." } finally { isSaving.value = false }
}
onMounted(() => void load())
</script>

<template>
  <section class="quest-detail">
    <header class="quest-detail__header"><div><p class="quest-detail__eyebrow">Work / Quest</p><h1>{{ detail?.quest.title || "Quest" }}</h1></div><div class="quest-detail__header-actions"><AppButton type="button" tone="secondary" :aria-expanded="utilityOpen" aria-controls="quest-detail-actions" @click="utilityOpen = !utilityOpen">{{ utilityOpen ? "Hide actions" : "Show actions" }}</AppButton><RouterLink :to="buildSurfaceVisionRoute('work-quests', `/work/quests/${questId}`, 'Work quest')" class="quest-detail__vision">Ask Vision</RouterLink></div></header>
    <div v-if="isLoading" class="quest-detail__status" role="status">Loading.</div>
    <div v-else-if="error" class="quest-detail__status quest-detail__status--error" role="alert">{{ error }} <AppButton type="button" tone="secondary" @click="load">Retry</AppButton></div>
    <template v-else-if="detail">
      <p v-if="feedback" class="quest-detail__feedback" role="status">{{ feedback }}</p>
      <DetailSurface v-if="!editing" title="Quest detail" utility-label="Quest actions" class="quest-detail__layout">
        <RichTextPreview :content="detail.quest.description" /><section class="quest-detail__activity"><h2>Current facts</h2><dl><div v-for="item in detail.activityRail" :key="item.label"><dt>{{ item.label }}</dt><dd>{{ item.label.toLowerCase().includes('award') ? formatCurrency(Number(item.value)) : item.value }}</dd></div></dl></section>
        <template #utility><aside v-if="utilityOpen" id="quest-detail-actions" class="quest-detail__actions" aria-label="Quest actions"><AppButton v-if="can('EDIT')" type="button" tone="secondary" @click="editing = true">Edit</AppButton><AppButton v-if="can('ASSIGN')" type="button" tone="primary" :loading="isSaving" @click="changeLifecycle('ASSIGNED')">Assign approved worker</AppButton><AppButton v-if="can('REOPEN')" type="button" tone="secondary" :loading="isSaving" @click="changeLifecycle('OPEN')">Reopen</AppButton><AppButton v-if="can('PAUSE')" type="button" tone="secondary" :loading="isSaving" @click="runAction('PAUSE')">Pause quest</AppButton><AppButton v-if="can('RESUME')" type="button" tone="primary" :loading="isSaving" @click="runAction('RESUME')">Resume quest</AppButton><AppButton v-if="can('START')" type="button" tone="primary" :loading="isSaving" @click="runAction('START')">Start</AppButton><AppButton v-if="can('COMPLETE')" type="button" tone="primary" :loading="isSaving" @click="runAction('COMPLETE')">Complete</AppButton><AppButton v-if="detail.sections.termChange.actionable && can('CONFIRM_TERM_CHANGE')" type="button" tone="primary" :loading="isSaving" @click="decideTerm('confirm')">{{ detail.sections.termChange.confirmLabel }}</AppButton><AppButton v-if="detail.sections.termChange.actionable && can('REJECT_TERM_CHANGE')" type="button" tone="danger" :loading="isSaving" @click="decideTerm('reject')">{{ detail.sections.termChange.rejectLabel }}</AppButton><AppButton v-if="can('CANCEL')" type="button" tone="danger" :loading="isSaving" @click="runAction('CANCEL')">Cancel quest</AppButton><AppButton v-if="can('DELETE')" type="button" tone="danger" :loading="isSaving" @click="runAction('DELETE')">Delete</AppButton><RouterLink v-if="can('VIEW_APPLICATIONS')" :to="`/work/quests/${questId}/applications`">Applications</RouterLink><AppButton v-if="can('APPLY')" type="button" tone="primary" @click="openApplication">Apply on web</AppButton><RouterLink v-if="can('APPLY')" :to="buildSurfaceVisionRoute('work-quests', `/work/quests/${questId}`, 'Apply to quest')">Apply with Vision</RouterLink></aside></template>
      </DetailSurface>
      <AppDialog :open="applicationOpen" title="Apply to this quest" @close="applicationOpen = false"><form class="quest-detail__application" @submit.prevent="submitApplication"><AppFormField label="Message" hint="Tell the owner why you are a good fit." required><textarea v-model="applicationMessage" required maxlength="2000" placeholder="Tell the owner why you are a good fit."></textarea></AppFormField><AppFormField v-if="detail.quest.awardAmount > 0" label="Offer" hint="Include your proposed price." required><input v-model.number="applicationPrice" type="number" min="0.01" step="0.01" required></AppFormField><p class="quest-detail__application-help">{{ detail.quest.awardAmount > 0 ? "The server validates the proposed price against quest rules." : "This quest does not require a price." }}</p><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="applicationOpen = false">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Sending" : "Send application" }}</AppButton></template></AppFormFooter></form></AppDialog>
      <form v-if="editing && !applicationOpen" class="quest-detail__edit" @submit.prevent="save"><AppFormField label="Title" required><input v-model="form!.title" required maxlength="255"></AppFormField><AppFormField label="Description" hint="Rich text is saved through the quest update contract."><RichTextEditor v-model="form!.description" label="Quest description" /></AppFormField><AppFormField label="Award" hint="The server applies currency and workflow validation." required><input v-model.number="form!.awardAmount" type="number" min="0" step="0.01" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="editing = false">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : "Save" }}</AppButton></template></AppFormFooter></form>
      <section v-if="detail.sections.review.visible" class="quest-detail__review"><h2>{{ detail.sections.review.introTitle }}</h2><p>{{ detail.sections.review.introSubtitle || detail.sections.review.emptyStateMessage }}</p><div v-if="detail.sections.review.submittedReview"><strong>{{ detail.sections.review.submittedReview.stars }}/5</strong><span>{{ detail.sections.review.submittedReview.comment }}</span></div><form v-else-if="detail.sections.review.canSubmit && detail.sections.review.target" @submit.prevent="submitReview"><AppFormField label="Rating" required><select v-model.number="reviewStars" required><option v-for="star in 5" :key="star" :value="star">{{ star }}/5</option></select></AppFormField><AppFormField :label="detail.sections.review.placeholder" optional><textarea v-model="reviewComment" maxlength="2000"></textarea></AppFormField><AppFormFooter><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : detail.sections.review.submitLabel }}</AppButton></template></AppFormFooter></form></section>
    </template>
  </section>
</template>

<style scoped>
.quest-detail { display: grid; gap: var(--space-3); }
.quest-detail__header { display: flex; align-items: end; justify-content: space-between; gap: var(--space-3); }
.quest-detail__header-actions { display: flex; align-items: center; gap: var(--space-2); }
.quest-detail__eyebrow { margin: 0 0 var(--space-1); color: var(--text-muted); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }
.quest-detail h1 { margin: 0; color: var(--text); font-size: clamp(1.55rem, 2.5vw, 2.3rem); letter-spacing: var(--tracking-tight); }
.quest-detail__utility-toggle, .quest-detail__vision, .quest-detail__actions button, .quest-detail__actions a, .quest-detail__form-actions button { display: inline-flex; align-items: center; justify-content: center; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); background: var(--control-bg); color: var(--control-ink); font: inherit; font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); cursor: pointer; }
.quest-detail__vision, .quest-detail__actions a:last-child, .quest-detail__form-actions button[type=submit] { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
.quest-detail__layout { gap: 0; overflow: hidden; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); }
.quest-detail__layout :deep(.detail-surface__workspace) { grid-template-columns: minmax(0, 1fr) var(--detail-rail-width); }
.quest-detail__layout :deep(.detail-surface__main) { padding: var(--space-4); }
.quest-detail__layout :deep(.detail-surface__utility) { background: var(--rail-canvas); }
.quest-detail__actions { display: grid; align-content: start; gap: var(--space-2); border: 0; border-radius: 0; background: transparent; }
.quest-detail__actions a { text-align: center; text-decoration: none; }
.quest-detail__actions button:hover, .quest-detail__actions a:hover, .quest-detail__utility-toggle:hover { border-color: var(--control-border-active); background: var(--surface-hover); }
.quest-detail__actions .quest-detail__danger { border-color: color-mix(in srgb, var(--danger) 40%, var(--border-subtle)); color: var(--danger); }
.quest-detail__actions .quest-detail__danger:hover { background: var(--danger-muted); }
.quest-detail__activity { margin-top: var(--space-4); border-top: 1px solid var(--border-subtle); padding-top: var(--space-3); }
.quest-detail__activity h2, .quest-detail__review h2 { margin: 0 0 var(--space-2); color: var(--text); font-size: var(--text-size-title); }
.quest-detail__activity dl { display: grid; gap: var(--space-2); margin: 0; }
.quest-detail__activity dl div { display: flex; justify-content: space-between; gap: var(--space-3); }
.quest-detail__activity dt { color: var(--text-muted); font-size: var(--text-size-meta); }
.quest-detail__activity dd { margin: 0; color: var(--text); font-size: var(--text-size-meta); text-align: right; }
.quest-detail__facts { display: flex; flex-wrap: wrap; gap: var(--space-2); margin-top: var(--space-4); }
.quest-detail__facts span { border: 1px solid var(--border-subtle); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); background: var(--surface-muted); color: var(--text-muted); font-size: var(--text-size-meta); }
.quest-detail__edit, .quest-detail__application, .quest-detail__review { display: grid; gap: var(--space-3); max-width: 44rem; border: 1px solid var(--border-subtle); border-radius: var(--radius-surface); background: var(--surface-base); padding: var(--space-3); }
.quest-detail__edit input, .quest-detail__edit textarea, .quest-detail__application input, .quest-detail__application textarea, .quest-detail__review select, .quest-detail__review textarea { width: 100%; box-sizing: border-box; border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-2); background: var(--control-bg); color: var(--control-ink); font: inherit; }
.quest-detail__edit textarea, .quest-detail__application textarea { min-height: 8rem; resize: vertical; }
.quest-detail__application-help, .quest-detail__review p { margin: 0; color: var(--text-muted); font-size: var(--text-size-meta); }
.quest-detail__status, .quest-detail__feedback { padding: var(--space-3) 0; color: var(--text-muted); }
.quest-detail__status--error { color: var(--danger); }
.quest-detail__status button { margin-left: var(--space-2); border: 0; background: none; color: inherit; text-decoration: underline; cursor: pointer; }
.quest-detail__feedback { color: var(--success); }
@media (max-width: 700px) { .quest-detail__header { align-items: start; flex-direction: column; }.quest-detail__header-actions { width: 100%; justify-content: space-between; }.quest-detail__layout :deep(.detail-surface__workspace) { grid-template-columns: 1fr; }.quest-detail__layout :deep(.detail-surface__utility) { border-top: 1px solid var(--border-subtle); border-left: 0; }.quest-detail__actions { border-top: 1px solid var(--border-subtle); }
}
</style>
<style scoped>
.quest-detail .app-button { width:100%; min-height:var(--control-height-default); border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.quest-detail__header .app-button { width:auto; }
.quest-detail .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.quest-detail .app-button--danger { color:var(--danger); }
</style>
