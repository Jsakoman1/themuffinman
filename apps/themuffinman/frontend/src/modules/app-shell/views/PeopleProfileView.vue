<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import type {UserProfileViewDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppDialog from "../components/AppDialog.vue"
import AppButton from "../components/AppButton.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import DetailSurface from "../components/DetailSurface.vue"
import DetailUtilityRail from "../components/DetailUtilityRail.vue"
import DetailSurfaceHeader from "../components/DetailSurfaceHeader.vue"
import {confirmAction, showActionNotice} from "../composables/useActionDialog.ts"
const route = useRoute(); const router = useRouter(); const profile = ref<UserProfileViewDTO | null>(null); const isLoading = ref(true); const isActing = ref(false); const error = ref(""); const reportReason = ref(""); const reportOpen = ref(false)
// Vision/profile handoffs must preserve this target context; profile actions are
// always reloaded after mutation so stale relationship
// labels cannot remain actionable in the utility rail.
const load = async () => { isLoading.value = true; error.value = ""; try { profile.value = await userShellApi.getCurrentProfileView(Number(route.params.userId)) } catch { error.value = "Could not load this profile." } finally { isLoading.value = false } }
const sendInvite = async () => {
  if (!profile.value || !profile.value.primaryAction?.enabled) return
  isActing.value = true; error.value = ""
  try { await userShellApi.createCircleRequest({recipientId: profile.value.profile.id}); await load() } catch { error.value = "Could not send this connection invite." } finally { isActing.value = false }
}
const runPrimaryAction = async () => {
  const action = profile.value?.primaryAction
  if (!action?.enabled) return
  if (action.type === "SEND_INVITE") return sendInvite()
  if (action.type === "UNBLOCK") return toggleBlock()
  if (action.type === "OPEN_CIRCLES") return void router.push("/circles")
  if (action.type === "EDIT_PROFILE") return void router.push("/profile/settings")
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
  const reason = reportReason.value.trim()
  if (!reason) return
  isActing.value = true; error.value = ""
  try { await userShellApi.createSafetyReport({targetUserId: profile.value.profile.id, targetFamily: "user", targetId: profile.value.profile.id, reason}); error.value = ""; reportReason.value = ""; reportOpen.value = false; await showActionNotice("Report submitted privately for review.") }
  catch { error.value = "Could not submit this report." } finally { isActing.value = false }
}
onMounted(() => void load())
</script>
<template><section class="profile-view"><AppStatus v-if="isLoading" message="Loading profile." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><DetailSurface v-else-if="profile" :title="profile.profile.username" utility-label="Profile actions"><template #header><DetailSurfaceHeader eyebrow="People / Profile" :title="profile.profile.username" back-to="/people" back-label="Back to people" /></template><template #default><p class="profile-view__description">{{ profile.profile.profileDescription || "No profile description yet." }}</p><section class="profile-view__trust" aria-label="Trust and visibility context"><p class="profile-view__trust-label">Trust context</p><strong>{{ profile.relation?.relationLabel || profile.resolutionLabel || "No shared relationship" }}</strong><span>{{ profile.relation?.blockedByCurrentUser ? "You blocked this person." : profile.relation?.relationStatus === "BLOCKED" ? "This person has restricted access." : "What you can see is controlled by trust and consent." }}</span></section><nav class="profile-view__related" aria-label="Related spaces"><p class="profile-view__trust-label">Related spaces</p><RouterLink to="/chat">Chat</RouterLink><RouterLink to="/circles">Circles and mutual trust</RouterLink><RouterLink to="/work/find">Work context</RouterLink><RouterLink to="/business/find">Business context</RouterLink></nav><dl v-if="profile.employerRating || profile.workerRating"><div v-if="profile.employerRating"><dt>Employer rating</dt><dd>Available</dd></div><div v-if="profile.workerRating"><dt>Worker rating</dt><dd>Available</dd></div></dl></template><template #utility><DetailUtilityRail title="Profile actions"><RouterLink to="/chat" class="profile-view__action">Open Chat</RouterLink><AppButton v-if="profile.primaryAction?.enabled" tone="primary" :loading="isActing" @click="runPrimaryAction">{{ profile.primaryAction.label }}</AppButton><AppButton v-if="profile.showBlockAction && profile.blockActionEnabled" tone="danger" :loading="isActing" @click="toggleBlock">{{ profile.relation?.blockedByCurrentUser ? "Unblock" : profile.blockActionLabel }}</AppButton><AppButton v-if="!profile.ownProfile" tone="danger" :loading="isActing" @click="reportOpen = true">Report</AppButton></DetailUtilityRail></template></DetailSurface><AppDialog :open="reportOpen" title="Report profile" layout="workspace" @close="reportOpen = false"><form class="profile-view__report" @submit.prevent="reportProfile"><AppFormField label="Reason" required><textarea v-model="reportReason" required maxlength="1000" placeholder="Describe the issue for private review."></textarea></AppFormField><AppFormFooter><template #secondary><AppButton type="button" tone="secondary" @click="reportOpen = false">Cancel</AppButton></template><template #primary><AppButton type="submit" tone="primary" :loading="isActing">Submit report</AppButton></template></AppFormFooter></form></AppDialog></section></template>
<style scoped>.profile-view{display:grid;gap:var(--space-3)}.back{color:var(--text-muted);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.profile-view__header{padding:var(--space-4) var(--space-5)}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.profile-view__description{white-space:pre-wrap;line-height:1.6}.muted{color:var(--text-muted)}dl{display:grid;gap:var(--space-2);margin:var(--space-5) 0 0}dl div{display:flex;justify-content:space-between;border-top:1px solid var(--border-subtle);padding-top:var(--space-2)}dt{color:var(--text-soft);font-size:var(--text-size-meta)}dd{margin:0;color:var(--text);font-weight:var(--text-weight-semibold)}.profile-view__action{display:flex;align-items:center;min-height:var(--control-height-default);margin:var(--space-3);padding:var(--space-1) var(--space-2);border:1px solid var(--control-border);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);font:inherit;font-size:var(--text-size-body);font-weight:var(--text-weight-semibold);cursor:pointer}.profile-view__danger{color:var(--danger)}.profile-view__report{display:grid;gap:var(--space-3)}.profile-view__report textarea{width:100%;box-sizing:border-box;min-height:8rem;border:1px solid var(--control-border);border-radius:var(--radius-control);padding:var(--space-2);background:var(--control-bg);color:var(--text);font:inherit}</style>
<style scoped>
.profile-view .app-button { width:calc(100% - (var(--space-3) * 2)); margin:var(--space-3); border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); background:var(--control-bg); color:var(--control-ink); }
.profile-view .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
.profile-view .app-button--danger { color:var(--danger); }
.profile-view__trust{display:grid;gap:var(--space-1);margin-top:var(--space-3);padding:var(--space-3);border-left:3px solid var(--accent);background:var(--surface-base);color:var(--text-muted)}.profile-view__trust-label{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.profile-view__trust strong{color:var(--text)}.profile-view__trust span{font-size:var(--text-size-meta)}
.profile-view__related{display:grid;gap:var(--space-1);margin-top:var(--space-3);padding:var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.profile-view__related a{color:var(--control-ink);font-weight:var(--text-weight-semibold)}
</style>
