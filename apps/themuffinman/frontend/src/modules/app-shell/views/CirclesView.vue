<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import type {CircleGroupResponseDTO, CircleRequestResponseDTO, CircleSearchResultDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppDialog from "../components/AppDialog.vue"
import AppFormField from "../components/AppFormField.vue"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import {confirmAction} from "../composables/useActionDialog.ts"
import GuidedIntakePanel from "../components/GuidedIntakePanel.vue"

// This surface lists circles owned by the viewer; membership actions for other
// circles belong to the relationship request flow, not this owner dashboard.
const groups = ref<CircleGroupResponseDTO[]>([])
const requests = ref<CircleRequestResponseDTO[]>([])
const outgoing = ref<CircleRequestResponseDTO[]>([])
const results = ref<CircleSearchResultDTO[]>([])
const blocked = ref<CircleSearchResultDTO[]>([])
const searchQuery = ref("")
const groupName = ref("")
const isLoading = ref(true)
const isActing = ref(false)
const error = ref("")
const feedback = ref("")
const editingGroupId = ref<number | null>(null)
const editingGroupName = ref("")
const loadWarnings = ref<string[]>([])
const hasUsableData = ref(false)
const createOpen = ref(false)
const guidedCircleDraft = ref<Record<string, string> | null>(null)
const acceptGuidedCircleDraft = (draft: Record<string, string>) => { guidedCircleDraft.value = draft; groupName.value = draft.name ?? "" }
const selectedGroupId = ref<number | null>(null)
const selectedGroup = computed(() => groups.value.find((group) => group.id === selectedGroupId.value) ?? null)

const load = async () => {
  isLoading.value = true; error.value = ""; loadWarnings.value = []
  const results = await Promise.allSettled([
    userShellApi.getCircleGroups(),
    userShellApi.getIncomingCircleRequests(0, 50),
    userShellApi.getOutgoingCircleRequests(0, 50),
    userShellApi.getBlockedCircleUsers(0, 50)
  ])
  const [circleGroups, incoming, outgoingRequests, blockedUsers] = results
  hasUsableData.value = results.some(result => result.status === "fulfilled")
  if (circleGroups.status === "fulfilled") groups.value = circleGroups.value
  else loadWarnings.value.push("circles")
  if (incoming.status === "fulfilled") requests.value = incoming.value.items
  else loadWarnings.value.push("incoming requests")
  if (outgoingRequests.status === "fulfilled") outgoing.value = outgoingRequests.value.items
  else loadWarnings.value.push("outgoing requests")
  if (blockedUsers.status === "fulfilled") blocked.value = blockedUsers.value.items
  else loadWarnings.value.push("blocked people")
  if (loadWarnings.value.length === 4) error.value = "Could not load circles."
  else if (loadWarnings.value.length > 0) error.value = `Some circle data could not be loaded: ${loadWarnings.value.join(", ")}.`
  isLoading.value = false
}
const createGroup = async () => { if (!groupName.value.trim()) return; isActing.value = true; error.value = ""; try { await userShellApi.createCircleGroup({name: groupName.value.trim()}); groupName.value = ""; feedback.value = "Circle created."; await load() } catch { error.value = "Could not create this circle." } finally { isActing.value = false } }
const beginEditGroup = (group: CircleGroupResponseDTO) => { editingGroupId.value = group.id; editingGroupName.value = group.name }
const saveGroup = async () => { if (editingGroupId.value === null || !editingGroupName.value.trim()) return; isActing.value = true; error.value = ""; try { await userShellApi.updateCircleGroup(editingGroupId.value, {name: editingGroupName.value.trim()}); feedback.value = "Circle updated."; editingGroupId.value = null; await load() } catch { error.value = "Could not update this circle." } finally { isActing.value = false } }
const archiveGroup = async (group: CircleGroupResponseDTO) => { if (!await confirmAction(`Remove the circle “${group.name}”?`, "Remove circle")) return; isActing.value = true; error.value = ""; try { await userShellApi.deleteCircleGroup(group.id); feedback.value = "Circle removed."; await load() } catch { error.value = "Could not remove this circle." } finally { isActing.value = false } }
const decide = async (request: CircleRequestResponseDTO, accept: boolean) => { isActing.value = true; error.value = ""; try { if (accept) await userShellApi.acceptCircleRequest(request.id); else await userShellApi.deleteCircleRequest(request.id); feedback.value = accept ? "Request accepted." : "Request declined."; await load() } catch { error.value = "Could not update this request." } finally { isActing.value = false } }
const search = async () => { if (!searchQuery.value.trim()) { results.value = []; return }; try { results.value = (await userShellApi.searchCircleUsers(searchQuery.value.trim())).items } catch { error.value = "Could not search people." } }
const block = async (userId: number) => { if (!await confirmAction("Block this person?", "Block person")) return; isActing.value = true; try { await userShellApi.blockCircleUser(userId); feedback.value = "Person blocked."; results.value = results.value.filter(item => item.id !== userId) } catch { error.value = "Could not block this person." } finally { isActing.value = false } }
const unblock = async (userId: number) => { isActing.value = true; try { await userShellApi.unblockCircleUser(userId); feedback.value = "Person unblocked."; await load() } catch { error.value = "Could not unblock this person." } finally { isActing.value = false } }
const leaveCircle = async (circleId: number) => { if (!await confirmAction("Leave this circle?", "Leave circle")) return; isActing.value = true; error.value = ""; try { await userShellApi.leaveCircle(circleId); feedback.value = "Circle membership removed."; await load() } catch { error.value = "Could not leave this circle. Owners must remove the circle instead." } finally { isActing.value = false } }
const removeMember = async (circleId: number, userId: number, username: string) => { if (!await confirmAction(`Remove ${username} from this circle?`, "Remove circle member")) return; isActing.value = true; error.value = ""; try { await userShellApi.removeCircleMember(circleId, userId); feedback.value = `${username} removed from the circle.`; await load() } catch { error.value = "Could not remove this member." } finally { isActing.value = false } }
onMounted(() => void load())
</script>

<template>
  <section class="circles">
    <header class="circles__header"><div><p class="circles__eyebrow">People / Circles</p><h1>Circles</h1></div></header><CollectionToolbar title="Trust circles" :count="groups.length" :busy="isLoading"><template #actions><AppButton tone="primary" type="button" @click="createOpen = true">New circle</AppButton></template></CollectionToolbar>
    <aside class="circles__privacy" aria-label="Circle privacy explanation"><strong>Circles are a trust boundary.</strong><span>Membership does not automatically reveal your exact address or private activity. Each module still applies its own visibility and consent rules; manage exact location sharing in Profile Settings.</span></aside>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading." /><AppStatus v-else-if="error && !hasUsableData" :message="error" tone="error" retry @retry="load" />
    <template v-else>
      <form class="circles__search" @submit.prevent="search"><AppFormField label="Find people"><input v-model="searchQuery" placeholder="Find people"></AppFormField><AppButton type="submit">Search</AppButton></form>
      <section v-if="results.length" class="circles__section"><h2>People</h2><article v-for="person in results" :key="person.id" class="circles__row"><div><strong>{{ person.username }}</strong><span>{{ person.profileDescription }}</span></div><AppButton tone="danger" type="button" @click="block(person.id)">Block</AppButton></article></section>
      <section v-if="requests.length" class="circles__section"><h2>Incoming requests</h2><article v-for="request in requests" :key="request.id" class="circles__row"><div><strong>{{ request.requesterUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><div class="circles__actions"><AppButton tone="primary" :loading="isActing" @click="decide(request, true)">Accept</AppButton><AppButton tone="quiet" :loading="isActing" @click="decide(request, false)">Decline</AppButton></div></article></section>
      <section v-if="outgoing.length" class="circles__section"><h2>Outgoing requests</h2><article v-for="request in outgoing" :key="request.id" class="circles__row"><div><strong>{{ request.counterpartUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><AppButton tone="quiet" :loading="isActing" @click="decide(request, false)">Cancel</AppButton></article></section>
      <section v-if="blocked.length" class="circles__section"><h2>Blocked</h2><article v-for="person in blocked" :key="person.id" class="circles__row"><div><strong>{{ person.username }}</strong></div><AppButton tone="quiet" :loading="isActing" @click="unblock(person.id)">Unblock</AppButton></article></section>
      <section class="circles__section"><h2>Your circles</h2><p v-if="groups.length === 0" class="circles__status">No circles yet.</p><div v-else class="circles__circles-workspace"><div class="circles__circle-list"><article v-for="group in groups" :key="group.id" class="circles__row" :class="{ 'circles__row--selected': selectedGroupId === group.id }" @click="selectedGroupId = group.id"><form v-if="editingGroupId === group.id" class="circles__inline-edit" @submit.prevent="saveGroup"><input v-model="editingGroupName" required maxlength="120"><AppButton tone="primary" type="submit" :loading="isActing">Save</AppButton><AppButton tone="quiet" type="button" @click="editingGroupId = null">Cancel</AppButton></form><div v-else><strong>{{ group.name }}</strong><span>{{ group.memberCount }} members · {{ group.memberPreviewLabel }}</span><details v-if="group.members?.length" class="circles__members"><summary>View members</summary><span v-for="member in group.members" :key="member.userId"><span>{{ member.username }}</span><AppButton tone="danger" :loading="isActing" @click.stop="removeMember(group.id, member.userId, member.username)">Remove</AppButton></span></details></div><div v-if="editingGroupId !== group.id" class="circles__actions"><AppButton type="button" @click.stop="beginEditGroup(group)">Edit</AppButton><AppButton tone="danger" :loading="isActing" @click.stop="archiveGroup(group)">Remove</AppButton><AppButton tone="quiet" :loading="isActing" @click.stop="leaveCircle(group.id)">Leave</AppButton></div></article></div><aside v-if="selectedGroup" class="circles__preview" aria-label="Circle preview"><p class="circles__eyebrow">Selected circle</p><h2>{{ selectedGroup.name }}</h2><p>{{ selectedGroup.memberCount }} members</p><dl><div><dt>Members</dt><dd>{{ selectedGroup.memberCount }}</dd></div><div><dt>Preview</dt><dd>{{ selectedGroup.memberPreviewLabel }}</dd></div></dl><div v-if="selectedGroup.members?.length" class="circles__preview-members"><strong>Members</strong><span v-for="member in selectedGroup.members" :key="member.userId">{{ member.username }}</span></div><p class="circles__preview-note">Circle membership is a trust boundary; module-specific visibility and consent rules still apply.</p></aside><aside v-else class="circles__preview circles__preview--empty" aria-label="Circle preview"><p class="circles__eyebrow">Preview</p><h2>Select a circle</h2><p>Inspect membership context without leaving the collection.</p></aside></div></section>
      <p v-if="error" class="circles__status circles__status--error" role="alert">{{ error }} <AppButton tone="quiet" type="button" @click="load">Retry</AppButton></p>
    </template>
    <AppDialog :open="createOpen" title="Create a circle" layout="workspace" @close="createOpen = false; guidedCircleDraft = null"><GuidedIntakePanel v-if="!guidedCircleDraft" flow="social.circle.create" title="Create a circle" @completed="acceptGuidedCircleDraft" @cancel="createOpen = false; guidedCircleDraft = null" /><form v-else class="circles__dialog-form" @submit.prevent="createGroup().then(() => { if (!error) { createOpen = false; guidedCircleDraft = null } })"><AppFormField label="Circle name" required><input v-model="groupName" placeholder="New circle name" maxlength="120" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" @click="guidedCircleDraft = null">Back</AppButton></template><template #primary><AppButton tone="primary" type="submit" :loading="isActing">Create circle</AppButton></template></AppFormFooter></form><template #utility><p>A circle is a trust boundary. Module-specific visibility and consent policies still apply after membership changes.</p></template></AppDialog>
  </section>
</template>

<style scoped>
.circles { display:grid; gap:var(--space-3); }
.circles__header { display:flex; justify-content:space-between; align-items:end; }
.circles__eyebrow { margin:0 0 var(--space-1); color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
.circles h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.circles h2 { margin:0; color:var(--text); font-size:var(--text-size-title); }
.circles__privacy { display:grid; gap:var(--space-1); max-width:48rem; padding:var(--space-2) var(--space-3); border-left:3px solid var(--accent); background:var(--surface-base); color:var(--text-muted); }
.circles__privacy span { font-size:var(--text-size-meta); }
.circles__search { display:grid; grid-template-columns:minmax(0,1fr) auto; gap:var(--space-2); max-width:32rem; align-items:end; }
.circles__search input,.circles__inline-edit input,.circles__dialog-form input { width:100%; border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-2); background:var(--control-bg); color:var(--control-ink); font:inherit; }
.circles__search input:focus-visible,.circles__inline-edit input:focus-visible,.circles__dialog-form input:focus-visible { border-color:var(--control-border-active); outline:2px solid var(--focus-ring); outline-offset:2px; }
.circles__section { display:grid; gap:var(--space-1); }
.circles__row { display:flex; justify-content:space-between; align-items:center; gap:var(--space-3); padding:var(--space-2) 0; border:1px solid var(--border-subtle); background:transparent; }
.circles__row>div:first-child { display:grid; gap:var(--space-1); min-width:0; }
.circles__row span { color:var(--text-muted); font-size:var(--text-size-meta); }
.circles__actions { display:flex; align-items:center; justify-content:flex-end; gap:var(--space-1); flex-wrap:wrap; }
.circles__status { padding:var(--space-2) 0; color:var(--text-muted); }
.circles__status--error { color:var(--danger); }
.circles__circles-workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.circles__circle-list { display:grid; gap:0; overflow:hidden; border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.circles__circle-list .circles__row { padding:var(--space-2) var(--space-3); border-bottom:1px solid var(--border-subtle); cursor:pointer; }
.circles__circle-list .circles__row:last-child { border-bottom:0; }
.circles__circle-list .circles__row--selected { background:var(--surface-selected); border-left:2px solid var(--accent); padding-left:calc(var(--space-3) - 2px); }
.circles__inline-edit { display:flex; flex:1; gap:var(--space-1); align-items:center; }
.circles__inline-edit input { min-width:0; }
.circles__preview { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.circles__preview h2,.circles__preview p { margin:0; }
.circles__preview h2 { color:var(--text); }
.circles__preview dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.circles__preview dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.circles__preview dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.circles__preview dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.circles__preview-members { display:grid; gap:var(--space-1); color:var(--text); }
.circles__preview-members span { color:var(--text-muted); font-size:var(--text-size-meta); }
.circles__preview-note { color:var(--text-soft); font-size:var(--text-size-meta); line-height:1.45; }
.circles__preview--empty { min-height:10rem; align-content:center; }
.circles__dialog-form { display:grid; gap:var(--space-3); }
.circles__members { display:grid; gap:var(--space-1); margin-top:var(--space-1); color:var(--text-muted); font-size:var(--text-size-meta); }
.circles__members summary { cursor:pointer; color:var(--text-soft); }
@media(max-width:860px) { .circles__circles-workspace { grid-template-columns:1fr; } .circles__preview { order:2; } }
@media(max-width:620px) { .circles__row { align-items:start; flex-direction:column; } .circles__actions { justify-content:flex-start; } .circles__search { grid-template-columns:1fr; max-width:none; } .circles__inline-edit { align-items:stretch; flex-wrap:wrap; } }
</style>
