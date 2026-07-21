<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {QuestApplicationDetailResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import DetailSurface from "../components/DetailSurface.vue"
import DetailUtilityRail from "../components/DetailUtilityRail.vue"

const route = useRoute()
const detail = ref<QuestApplicationDetailResponseDTO | null>(null)
const isLoading = ref(true)
const error = ref("")
const applicationId = () => Number(route.params.applicationId)
const formatDate = (value: string | null | undefined) => value ? new Intl.DateTimeFormat(undefined, {dateStyle: "medium", timeStyle: "short"}).format(new Date(value)) : "Not scheduled"

const load = async () => {
  isLoading.value = true
  error.value = ""
  try { detail.value = await userShellApi.getApplicationDetail(applicationId()) }
  catch { error.value = "Could not load this application." }
  finally { isLoading.value = false }
}

onMounted(() => void load())
</script>

<template>
  <section class="application-detail"><header class="application-detail__header"><div><p class="application-detail__eyebrow">Work / Application</p><h1>{{ detail?.application.questTitle || "Application" }}</h1></div><RouterLink to="/work/applications">Back to applications</RouterLink></header><AppStatus v-if="isLoading" message="Loading application." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><DetailSurface v-else-if="detail" :title="detail.application.questTitle" utility-label="Quest context"><template #default><div class="application-detail__status-badge">{{ detail.application.presentation.statusLabel }}</div><p class="application-detail__message">{{ detail.application.message || "No application message." }}</p><dl><div><dt>Offer</dt><dd>{{ detail.application.proposedPrice ?? "Not specified" }}{{ detail.application.proposedPrice == null ? "" : " €" }}</dd></div><div><dt>Submitted</dt><dd>{{ formatDate(detail.application.createdAt) }}</dd></div><div><dt>Quest status</dt><dd>{{ detail.application.presentation.questStatusLabel }}</dd></div></dl></template><template #utility><DetailUtilityRail title="Quest context"><p>{{ detail.quest.description }}</p><RouterLink :to="`/work/quests/${detail.quest.id}`">Open quest</RouterLink><p class="application-detail__meta">Posted by {{ detail.quest.creatorUsername }} · {{ formatDate(detail.quest.scheduledAt) }}</p></DetailUtilityRail></template></DetailSurface></section>
</template>

<style scoped>
.application-detail{display:grid;gap:var(--space-3)}.application-detail__header{display:flex;justify-content:space-between;align-items:end;gap:var(--space-3)}.application-detail__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.application-detail__header a{color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.application-detail__status-badge{display:inline-flex;border:1px solid var(--border-subtle);border-radius:var(--radius-control);padding:var(--space-1) var(--space-2);color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.application-detail__message{white-space:pre-wrap;line-height:1.6}.application-detail dl{display:grid;gap:var(--space-2);margin:var(--space-5) 0 0}.application-detail dl div{display:flex;justify-content:space-between;gap:var(--space-3);border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}.application-detail dt,.application-detail__meta{color:var(--text-soft);font-size:var(--text-size-meta)}.application-detail dd{margin:0;color:var(--text);font-weight:var(--text-weight-semibold)}.application-detail :deep(.detail-utility-rail p),.application-detail :deep(.detail-utility-rail a){margin:0;padding:var(--space-3);color:var(--text-muted);line-height:1.5}.application-detail :deep(.detail-utility-rail a){border-top:1px solid var(--border-subtle);color:var(--text);font-weight:var(--text-weight-semibold)}@media(max-width:700px){.application-detail__header{align-items:start;flex-direction:column}}
.application-detail__header a:hover { color: var(--text); }
.application-detail__status-badge { background: var(--surface-selected); color: var(--text); }
</style>
<style scoped>.application-detail__card[aria-label="Application narrative and activity"]{border-top:2px solid var(--accent)}</style>
