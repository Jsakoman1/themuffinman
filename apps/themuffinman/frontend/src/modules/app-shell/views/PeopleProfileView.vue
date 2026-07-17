<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRoute} from "vue-router"
import type {UserProfileViewDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {confirmAction, showActionNotice} from "../composables/useActionDialog.ts"
const route = useRoute(); const profile = ref<UserProfileViewDTO | null>(null); const isLoading = ref(true); const isActing = ref(false); const error = ref("")
const load = async () => { isLoading.value = true; error.value = ""; try { profile.value = await userShellApi.getCurrentProfileView(Number(route.params.userId)) } catch { error.value = "Could not load this profile." } finally { isLoading.value = false } }
const sendInvite = async () => {
  if (!profile.value || !profile.value.primaryAction?.enabled) return
  isActing.value = true; error.value = ""
  try { await userShellApi.createCircleRequest({recipientId: profile.value.profile.id}); await load() } catch { error.value = "Could not send this connection invite." } finally { isActing.value = false }
}
const toggleBlock = async () => {
  if (!profile.value || !profile.value.blockActionEnabled) return
  if (!profile.value.relation.blockedByCurrentUser && !await confirmAction(`Block ${profile.value.profile.username}?`, "Block person")) return
  isActing.value = true; error.value = ""
  try {
    if (profile.value.relation.blockedByCurrentUser) await userShellApi.unblockCircleUser(profile.value.profile.id)
    else await userShellApi.blockCircleUser(profile.value.profile.id)
    await load()
  } catch { error.value = "Could not update this person's trust boundary." } finally { isActing.value = false }
}
const reportProfile = async () => {
  if (!profile.value) return
  const reason = window.prompt("Why should this profile be reviewed?")?.trim()
  if (!reason) return
  isActing.value = true; error.value = ""
  try { await userShellApi.createSafetyReport({targetUserId: profile.value.profile.id, targetFamily: "user", targetId: profile.value.profile.id, reason}); error.value = ""; await showActionNotice("Report submitted privately for review.") }
  catch { error.value = "Could not submit this report." } finally { isActing.value = false }
}
onMounted(() => void load())
</script>
<template><section class="profile-view"><RouterLink to="/people" class="back">Back to people</RouterLink><p v-if="isLoading" class="status" role="status">Loading profile.</p><p v-else-if="error" class="status status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></p><article v-else-if="profile" class="card"><p class="eyebrow">People / Profile</p><h1>{{ profile.profile.username }}</h1><p>{{ profile.profile.profileDescription || "No profile description yet." }}</p><p class="muted">{{ profile.relation?.relationLabel || profile.resolutionLabel || "Visibility is controlled by trust and consent." }}</p><div v-if="profile.employerRating || profile.workerRating" class="ratings"><span v-if="profile.employerRating">Employer rating available</span><span v-if="profile.workerRating">Worker rating available</span></div><div class="actions"><RouterLink to="/chat" class="primary">Open Chat</RouterLink><button v-if="profile.primaryAction?.enabled" type="button" class="secondary" :disabled="isActing" @click="sendInvite">{{ profile.primaryAction.label }}</button><button v-if="profile.showBlockAction && profile.blockActionEnabled" type="button" class="danger" :disabled="isActing" @click="toggleBlock">{{ profile.relation?.blockedByCurrentUser ? "Unblock" : profile.blockActionLabel }}</button><button v-if="!profile.ownProfile" type="button" class="danger" :disabled="isActing" @click="reportProfile">Report</button></div></article></section></template>
<style scoped>.profile-view{display:grid;gap:1rem;max-width:48rem}.back{color:rgba(23,34,26,.65)}.card{display:grid;gap:.75rem;padding:1.2rem;border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62)}.eyebrow{margin:0;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.7rem,3vw,2.6rem)}.muted,.status{color:rgba(23,34,26,.62)}.status--error{color:#8d2f25}.status button{margin-left:.5rem;border:0;background:none;color:inherit;text-decoration:underline}.ratings,.actions{display:flex;gap:.5rem;flex-wrap:wrap}.ratings span{padding:.35rem .55rem;border-radius:999px;background:rgba(214,228,218,.7);font-size:.8rem}.primary,.secondary,.danger{justify-self:start;border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.55rem .8rem;font:inherit;font-size:.8rem;font-weight:650}.primary{background:#17221a;color:#f8f8f4}.secondary{background:rgba(214,228,218,.7);color:#17221a}.danger{color:#8d2f25;background:transparent}</style>
