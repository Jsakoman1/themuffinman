<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {QuestApplicationResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"
import AppDialog from "../components/AppDialog.vue"

const items = ref<QuestApplicationResponseDTO[]>([])
const page = ref(0)
const totalItems = ref(0)
const isLoading = ref(true)
const isLoadingMore = ref(false)
const error = ref("")
const editingId = ref<number | null>(null)
const editMessage = ref("")
const editPrice = ref<number | null>(null)
const hasMore = computed(() => items.value.length < totalItems.value)
const formatDate = (value: string | null | undefined) => value ? new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric"}).format(new Date(value)) : "No date"

const load = async (reset = true) => {
  if (reset) { isLoading.value = true; page.value = 0; items.value = [] } else { isLoadingMore.value = true }
  error.value = ""
  try {
    const response = await userShellApi.getMyApplications(page.value, 20)
    items.value = reset ? response.items : [...items.value, ...response.items]
    totalItems.value = response.totalItems
    page.value = response.page
  } catch { error.value = "Could not load applications." }
  finally { isLoading.value = false; isLoadingMore.value = false }
}
const loadMore = async () => { if (!hasMore.value || isLoadingMore.value) return; page.value += 1; await load(false) }
const beginEdit = (application: QuestApplicationResponseDTO) => { editingId.value = application.id; editMessage.value = application.message; editPrice.value = application.proposedPrice }
const saveEdit = async (application: QuestApplicationResponseDTO) => { isLoadingMore.value = true; error.value = ""; try { await userShellApi.updateMyApplication(application.questId, {message: editMessage.value, proposedPrice: editPrice.value}); editingId.value = null; await load() } catch { error.value = "Could not update this application." } finally { isLoadingMore.value = false } }
const withdraw = async (application: QuestApplicationResponseDTO) => { if (!window.confirm(`Withdraw your application for “${application.questTitle}”?`)) return; isLoadingMore.value = true; error.value = ""; try { await userShellApi.withdrawMyApplication(application.questId); await load() } catch { error.value = "Could not withdraw this application." } finally { isLoadingMore.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="applications-surface">
    <header class="applications-surface__header"><div><p class="applications-surface__eyebrow">Work / Applications</p><h1>Applications</h1></div><span class="applications-surface__count">{{ totalItems }} total</span></header>
    <div v-if="isLoading" class="applications-surface__status" role="status">Loading.</div>
    <div v-else-if="error" class="applications-surface__status applications-surface__status--error" role="alert">{{ error }} <button type="button" @click="load()">Retry</button></div>
    <div v-else-if="items.length === 0" class="applications-surface__status">No applications yet.</div>
    <div v-else class="applications-surface__list">
      <article v-for="application in items" :key="application.id" class="applications-surface__row">
        <div v-if="editingId !== application.id"><RouterLink :to="resolveSurfaceDetailRoute('work-applications', application.id) ?? `/vision/applications/${application.id}`" class="applications-surface__title">{{ application.questTitle }}</RouterLink><span class="applications-surface__meta">{{ application.presentation.statusLabel }} · {{ application.questCreatorUsername }} · {{ formatDate(application.createdAt) }}</span></div>
        <AppDialog :open="editingId === application.id" title="Edit application" @close="editingId = null"><form class="applications-surface__edit" @submit.prevent="saveEdit(application)"><label>Message<textarea v-model="editMessage" required maxlength="2000"></textarea></label><label>Proposed price<input v-model.number="editPrice" type="number" min="0" step="0.01"></label><div><button type="submit">Save</button><button type="button" @click="editingId = null">Cancel</button></div></form></AppDialog>
        <div class="applications-surface__row-actions"><RouterLink :to="resolveSurfaceDetailRoute('work-applications', application.id) ?? `/vision/applications/${application.id}`" class="applications-surface__open">Open</RouterLink><button v-if="application.allowedActions.includes('EDIT')" type="button" class="applications-surface__open" @click="beginEdit(application)">Edit</button><button v-if="application.allowedActions.includes('WITHDRAW')" type="button" class="applications-surface__open applications-surface__withdraw" @click="withdraw(application)">Withdraw</button></div>
      </article>
    </div>
    <button v-if="hasMore" type="button" class="applications-surface__more" :disabled="isLoadingMore" @click="loadMore">{{ isLoadingMore ? "Loading" : "Load more" }}</button>
  </section>
</template>

<style scoped>
.applications-surface{display:grid;gap:1rem}.applications-surface__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.applications-surface__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.applications-surface__count,.applications-surface__meta{color:rgba(23,34,26,.58);font-size:.84rem}.applications-surface__list{display:grid;gap:.45rem}.applications-surface__row{display:flex;justify-content:space-between;gap:1rem;align-items:center;padding:.85rem 0;border-bottom:1px solid rgba(23,34,26,.08)}.applications-surface__row>div:first-child{display:grid;gap:.28rem;min-width:0}.applications-surface__title{font-weight:650;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.applications-surface__meta{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.applications-surface__row-actions{display:flex;gap:.35rem;align-items:center}.applications-surface__open,.applications-surface__more{display:inline-flex;align-items:center;justify-content:center;min-height:2.25rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .8rem;font-size:.82rem;font-weight:650;white-space:nowrap;background:transparent;cursor:pointer}.applications-surface__more{justify-self:start;background:#17221a;color:#f8f8f4}.applications-surface__withdraw{color:#8d2f25}.applications-surface__edit{display:grid;gap:.4rem}.applications-surface__edit textarea,.applications-surface__edit input{border:1px solid rgba(23,34,26,.14);border-radius:.6rem;padding:.55rem;font:inherit}.applications-surface__edit textarea{min-width:18rem;min-height:5rem}.applications-surface__edit button{margin-right:.35rem;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.4rem .65rem;background:transparent;cursor:pointer}.applications-surface__status{padding:1rem 0;color:rgba(23,34,26,.65)}.applications-surface__status--error{color:#8d2f25}.applications-surface__status button{margin-left:.6rem;border:0;background:none;color:inherit;text-decoration:underline;cursor:pointer}@media(max-width:620px){.applications-surface__row{align-items:start;flex-direction:column}.applications-surface__meta{white-space:normal}.applications-surface__row-actions{flex-wrap:wrap}}
</style>
