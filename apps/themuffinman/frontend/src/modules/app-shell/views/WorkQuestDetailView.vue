<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {QuestDetailResponseDTO, QuestRequestDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {buildSurfaceVisionRoute} from "../visionHandoff.ts"

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
const questId = computed(() => Number(route.params.questId))
const can = (action: string) => detail.value?.quest.allowedActions.includes(action as never) ?? false

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
const runAction = async (action: "START" | "COMPLETE" | "DELETE") => {
  if (action === "DELETE" && !window.confirm("Delete this quest?")) return
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.executeQuestAction(questId.value, action); feedback.value = action === "DELETE" ? "Quest deleted." : "Quest updated."; await load() } catch { error.value = "This action could not be completed." } finally { isSaving.value = false }
}
const decideTerm = async (decision: "confirm" | "reject") => {
  isSaving.value = true; error.value = ""; feedback.value = ""
  try { await userShellApi.decideQuestTerm(questId.value, decision); feedback.value = decision === "confirm" ? "Term change confirmed." : "Term change rejected."; await load() } catch { error.value = "Could not resolve the term change." } finally { isSaving.value = false }
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
    <header class="quest-detail__header"><div><p class="quest-detail__eyebrow">Work / Quest</p><h1>{{ detail?.quest.title || "Quest" }}</h1></div><RouterLink :to="buildSurfaceVisionRoute('work-quests', `/work/quests/${questId}`, 'Work quest')" class="quest-detail__vision">Ask Vision</RouterLink></header>
    <div v-if="isLoading" class="quest-detail__status" role="status">Loading.</div>
    <div v-else-if="error" class="quest-detail__status quest-detail__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <template v-else-if="detail">
      <p v-if="feedback" class="quest-detail__feedback" role="status">{{ feedback }}</p>
      <div v-if="!editing" class="quest-detail__layout">
        <main class="quest-detail__main"><p class="quest-detail__description">{{ detail.quest.description }}</p><div class="quest-detail__facts"><span>{{ detail.quest.awardAmount }} €</span><span>{{ detail.quest.presentation.statusLabel }}</span><span>{{ detail.quest.presentation.timeTypeLabel }}</span><span>{{ detail.quest.presentation.locationLabel || "Anywhere" }}</span></div></main>
        <aside class="quest-detail__actions" aria-label="Quest actions"><button v-if="can('EDIT')" type="button" @click="editing = true">Edit</button><button v-if="can('START')" type="button" @click="runAction('START')">Start</button><button v-if="can('COMPLETE')" type="button" @click="runAction('COMPLETE')">Complete</button><button v-if="detail.sections.termChange.actionable && can('CONFIRM_TERM_CHANGE')" type="button" @click="decideTerm('confirm')">{{ detail.sections.termChange.confirmLabel }}</button><button v-if="detail.sections.termChange.actionable && can('REJECT_TERM_CHANGE')" type="button" class="quest-detail__danger" @click="decideTerm('reject')">{{ detail.sections.termChange.rejectLabel }}</button><button v-if="can('DELETE')" type="button" class="quest-detail__danger" @click="runAction('DELETE')">Delete</button><RouterLink v-if="can('VIEW_APPLICATIONS')" :to="`/work/quests/${questId}/applications`">Applications</RouterLink><RouterLink v-if="can('APPLY')" :to="buildSurfaceVisionRoute('work-quests', `/work/quests/${questId}`, 'Apply to quest')">Apply with Vision</RouterLink></aside>
      </div>
      <form v-else class="quest-detail__edit" @submit.prevent="save"><label>Title<input v-model="form!.title" required maxlength="255"></label><label>Description<textarea v-model="form!.description" required maxlength="2000"></textarea></label><label>Award<input v-model.number="form!.awardAmount" type="number" min="0" step="0.01" required></label><div class="quest-detail__form-actions"><button type="button" @click="editing = false">Cancel</button><button type="submit" :disabled="isSaving">{{ isSaving ? "Saving" : "Save" }}</button></div></form>
      <section v-if="detail.sections.review.visible" class="quest-detail__review"><h2>{{ detail.sections.review.introTitle }}</h2><p>{{ detail.sections.review.introSubtitle || detail.sections.review.emptyStateMessage }}</p><div v-if="detail.sections.review.submittedReview"><strong>{{ detail.sections.review.submittedReview.stars }}/5</strong><span>{{ detail.sections.review.submittedReview.comment }}</span></div><form v-else-if="detail.sections.review.canSubmit && detail.sections.review.target" @submit.prevent="submitReview"><label>Rating<select v-model.number="reviewStars"><option v-for="star in 5" :key="star" :value="star">{{ star }}/5</option></select></label><label>{{ detail.sections.review.placeholder }}<textarea v-model="reviewComment" maxlength="2000"></textarea></label><button type="submit" :disabled="isSaving">{{ isSaving ? "Saving" : detail.sections.review.submitLabel }}</button></form></section>
    </template>
  </section>
</template>

<style scoped>
.quest-detail{display:grid;gap:1rem}.quest-detail__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.quest-detail__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.quest-detail__vision,.quest-detail__actions button,.quest-detail__actions a,.quest-detail__form-actions button{display:inline-flex;align-items:center;justify-content:center;min-height:2.3rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .8rem;font-size:.82rem;font-weight:650;background:transparent;cursor:pointer}.quest-detail__vision,.quest-detail__actions a:last-child,.quest-detail__form-actions button[type=submit]{background:#17221a;color:#f8f8f4}.quest-detail__layout{display:grid;grid-template-columns:minmax(0,1fr) minmax(12rem,18rem);gap:1rem}.quest-detail__main,.quest-detail__actions,.quest-detail__edit{border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62);padding:1rem}.quest-detail__description{margin:0;white-space:pre-wrap;line-height:1.55}.quest-detail__facts{display:flex;flex-wrap:wrap;gap:.45rem;margin-top:1.2rem}.quest-detail__facts span{border:1px solid rgba(23,34,26,.1);border-radius:999px;padding:.42rem .65rem;color:rgba(23,34,26,.68);font-size:.82rem}.quest-detail__actions{display:grid;align-content:start;gap:.45rem}.quest-detail__actions a{text-align:center}.quest-detail__danger{color:#8d2f25!important}.quest-detail__edit{display:grid;gap:.8rem;max-width:44rem}.quest-detail__edit label{display:grid;gap:.3rem;font-size:.82rem;font-weight:650}.quest-detail__edit input,.quest-detail__edit textarea{width:100%;border:1px solid rgba(23,34,26,.14);border-radius:.7rem;padding:.7rem;font:inherit}.quest-detail__edit textarea{min-height:10rem;resize:vertical}.quest-detail__form-actions{display:flex;gap:.45rem;justify-content:flex-end}.quest-detail__status,.quest-detail__feedback{padding:1rem 0;color:rgba(23,34,26,.65)}.quest-detail__status--error{color:#8d2f25}.quest-detail__status button{margin-left:.6rem;border:0;background:none;text-decoration:underline;cursor:pointer}.quest-detail__feedback{color:#28663b}@media(max-width:700px){.quest-detail__layout{grid-template-columns:1fr}.quest-detail__header{align-items:start;flex-direction:column}}
</style>
