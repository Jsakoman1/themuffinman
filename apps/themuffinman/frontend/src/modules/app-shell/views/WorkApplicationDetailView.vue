<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {QuestApplicationDetailResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const route = useRoute()
const detail = ref<QuestApplicationDetailResponseDTO | null>(null)
const isLoading = ref(true)
const error = ref("")
const applicationId = () => Number(route.params.applicationId)
const formatDate = (value: string | null | undefined) => value ? new Intl.DateTimeFormat("en-US", {dateStyle: "medium", timeStyle: "short"}).format(new Date(value)) : "Not scheduled"

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
  <section class="application-detail">
    <header class="application-detail__header"><div><p class="application-detail__eyebrow">Work / Application</p><h1>{{ detail?.application.questTitle || "Application" }}</h1></div><RouterLink to="/work/applications">Back to applications</RouterLink></header>
    <div v-if="isLoading" class="application-detail__status" role="status">Loading.</div>
    <div v-else-if="error" class="application-detail__status application-detail__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div>
    <template v-else-if="detail">
      <div class="application-detail__grid">
        <main class="application-detail__card" aria-label="Application narrative and activity"><div class="application-detail__status-badge">{{ detail.application.presentation.statusLabel }}</div><p class="application-detail__message">{{ detail.application.message || "No application message." }}</p><dl><div><dt>Offer</dt><dd>{{ detail.application.proposedPrice ?? "Not specified" }}{{ detail.application.proposedPrice == null ? "" : " €" }}</dd></div><div><dt>Submitted</dt><dd>{{ formatDate(detail.application.createdAt) }}</dd></div><div><dt>Quest status</dt><dd>{{ detail.application.presentation.questStatusLabel }}</dd></div></dl></main>
        <aside class="application-detail__card application-detail__context" aria-label="Quest context and canonical route"><h2>Quest context</h2><p>{{ detail.quest.description }}</p><RouterLink :to="`/work/quests/${detail.quest.id}`">Open quest</RouterLink><p class="application-detail__meta">Posted by {{ detail.quest.creatorUsername }} · {{ formatDate(detail.quest.scheduledAt) }}</p></aside>
      </div>
    </template>
  </section>
</template>

<style scoped>
.application-detail{display:grid;gap:1rem}.application-detail__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.application-detail__eyebrow{margin:0 0 .3rem;color:var(--text-muted);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.application-detail__header a,.application-detail__context a{font-size:.84rem}.application-detail__grid{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(14rem,.75fr);gap:1rem}.application-detail__card{border:1px solid var(--border-subtle);border-radius:1rem;background:var(--surface);padding:1rem}.application-detail__status-badge{display:inline-flex;border-radius:999px;background:#e8efe7;padding:.4rem .7rem;font-size:.8rem;font-weight:650}.application-detail__message{white-space:pre-wrap;line-height:1.55}.application-detail__card dl{display:grid;gap:.7rem;margin:1.4rem 0 0}.application-detail__card dl div{display:flex;justify-content:space-between;gap:1rem;border:1px solid var(--border-subtle);padding-top:.7rem}.application-detail__card dt,.application-detail__meta{color:var(--text-muted);font-size:.82rem}.application-detail__card dd{margin:0;font-weight:650}.application-detail__context{display:grid;align-content:start;gap:.7rem}.application-detail__context h2{margin:0;font-size:1rem}.application-detail__context p{margin:0;line-height:1.5}.application-detail__status{padding:1rem 0;color:var(--text-muted)}.application-detail__status--error{color:var(--danger)}.application-detail__status button{margin-left:.6rem;border:0;background:none;text-decoration:underline;cursor:pointer}@media(max-width:700px){.application-detail__header{align-items:start;flex-direction:column}.application-detail__grid{grid-template-columns:1fr}}
</style>
<style scoped>.application-detail__card[aria-label="Application narrative and activity"]{border-top:2px solid var(--accent)}</style>
