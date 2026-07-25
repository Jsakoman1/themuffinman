<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
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
import TaskSurface from "../components/TaskSurface.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"

// This surface lists circles owned by the viewer; membership actions for other
// circles belong to the relationship request flow, not this owner dashboard.
const groups = ref<CircleGroupResponseDTO[]>([])
const route = useRoute()
const router = useRouter()
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
const tabs = getModuleTabs("circles")
const surface = getAppSurfaceConfig("circles")
const activeTab = computed(() => {
  if (route.path.endsWith("/requests")) return "requests"
  if (route.path.endsWith("/circles")) return "groups"
  if (route.path.endsWith("/find")) return "people"
  const tab = typeof route.query.tab === "string" ? route.query.tab : "groups"
  return tabs?.tabs.some(item => item.id === tab) ? tab : "groups"
})

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
const createGroup = async () => { if (!groupName.value.trim()) return; isActing.value = true; error.value = ""; try { await userShellApi.createCircleGroup({name: groupName.value.trim()}); groupName.value = ""; feedback.value = "Circle created."; await load() } catch (cause) { error.value = userShellApi.actionFailureMessage("Could not create this circle.", cause) } finally { isActing.value = false } }
const beginEditGroup = (group: CircleGroupResponseDTO) => { editingGroupId.value = group.id; editingGroupName.value = group.name }
const saveGroup = async () => { if (editingGroupId.value === null || !editingGroupName.value.trim()) return; isActing.value = true; error.value = ""; try { await userShellApi.updateCircleGroup(editingGroupId.value, {name: editingGroupName.value.trim()}); feedback.value = "Circle updated."; editingGroupId.value = null; await load() } catch (cause) { error.value = userShellApi.actionFailureMessage("Could not update this circle.", cause) } finally { isActing.value = false } }
const archiveGroup = async (group: CircleGroupResponseDTO) => { if (!await confirmAction(`Remove the circle “${group.name}”?`, "Remove circle")) return; isActing.value = true; error.value = ""; try { await userShellApi.deleteCircleGroup(group.id); feedback.value = "Circle removed."; await load() } catch { error.value = "Could not remove this circle." } finally { isActing.value = false } }
const decide = async (request: CircleRequestResponseDTO, accept: boolean) => { isActing.value = true; error.value = ""; try { if (accept) await userShellApi.acceptCircleRequest(request.id); else await userShellApi.deleteCircleRequest(request.id); feedback.value = accept ? "Request accepted." : "Request declined."; await load() } catch { error.value = "Could not update this request." } finally { isActing.value = false } }
const search = async () => { if (!searchQuery.value.trim()) { results.value = []; return }; try { results.value = (await userShellApi.searchCircleUsers(searchQuery.value.trim())).items } catch { error.value = "Could not search people." } }
const addToSelectedGroup = async (person: CircleSearchResultDTO) => {
  if (!selectedGroup.value) return
  isActing.value = true; error.value = ""
  try { await userShellApi.addCircleMember(selectedGroup.value.id, person.id); feedback.value = `${person.username} added to ${selectedGroup.value.name}.`; await load() }
  catch { error.value = "Could not add this person to the selected circle. The server may require an accepted connection first." }
  finally { isActing.value = false }
}
const connect = async (person: CircleSearchResultDTO) => {
  isActing.value = true; error.value = ""
  try { await userShellApi.createCircleRequest({recipientId: person.id}); feedback.value = `Connection request sent to ${person.username}.`; await search() }
  catch { error.value = "Could not send the connection request." }
  finally { isActing.value = false }
}
const createClubChat = async (group: CircleGroupResponseDTO) => {
  const participantUserIds = (group.members ?? []).map(member => member.userId)
  if (!participantUserIds.length) { error.value = "Add at least one member before creating a club chat."; return }
  isActing.value = true; error.value = ""
  try {
    const conversation = await userShellApi.createChatGroup({title: group.name, participantUserIds})
    feedback.value = `${group.name} club chat created.`
    await router.push(`/chat/${conversation.conversationId}`)
  } catch { error.value = "Could not create the club chat. The server will keep membership and permissions authoritative." }
  finally { isActing.value = false }
}
const block = async (userId: number) => { if (!await confirmAction("Block this person?", "Block person")) return; isActing.value = true; try { await userShellApi.blockCircleUser(userId); feedback.value = "Person blocked."; results.value = results.value.filter(item => item.id !== userId) } catch { error.value = "Could not block this person." } finally { isActing.value = false } }
const leaveCircle = async (circleId: number) => { if (!await confirmAction("Leave this circle?", "Leave circle")) return; isActing.value = true; error.value = ""; try { await userShellApi.leaveCircle(circleId); feedback.value = "Circle membership removed."; await load() } catch { error.value = "Could not leave this circle. Owners must remove the circle instead." } finally { isActing.value = false } }
const removeMember = async (circleId: number, userId: number, username: string) => { if (!await confirmAction(`Remove ${username} from this circle?`, "Remove circle member")) return; isActing.value = true; error.value = ""; try { await userShellApi.removeCircleMember(circleId, userId); feedback.value = `${username} removed from the circle.`; await load() } catch { error.value = "Could not remove this member." } finally { isActing.value = false } }
onMounted(() => void load())
</script>

<template>
  <!-- UX simplification: trust, inspect, and membership actions stay in one focused turn. -->
  <TaskSurface mode="workspace" label="Trust circles"><section class="circles" data-mental-model="people-groups-permissions" :aria-busy="isLoading || isActing || undefined">
    <SurfaceHeader :config="surface" title="Circles" description="Keep people, groups, and connection requests in one calm place." />
    <ModuleTabs v-if="tabs" :tabs="tabs.tabs" :active-id="activeTab" data-surface="circles-tabs" />
    <CollectionToolbar title="Trust circles" :count="groups.length" :busy="isLoading" />
    <details class="circles__privacy" aria-label="Circle privacy"><summary>How circles work</summary><span>Circles control shared visibility and consent. Exact location and private profile fields stay in Profile Settings.</span></details>
    <AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="isLoading" message="Loading circles." busy /><AppStatus v-else-if="error && !hasUsableData" :message="error" tone="error" retry @retry="load" />
    <template v-else>
      <section v-if="activeTab === 'people'" class="circles__section" data-surface="people-organizer"><h2>Find people</h2><form class="circles__search" @submit.prevent="search"><AppFormField label="Search by name"><input v-model="searchQuery" placeholder="Search by name"></AppFormField><AppButton type="submit">Search</AppButton></form><p class="circles__hint">Select a circle first to add an accepted connection to it.</p><article v-for="person in results" :key="person.id" class="circles__row"><div><strong>{{ person.username }}</strong><span>{{ person.relationLabel || person.profileDescription }}</span></div><div class="circles__actions"><AppButton v-if="person.primaryAction?.enabled" tone="primary" type="button" :loading="isActing" @click="connect(person)">Connect</AppButton><AppButton v-if="selectedGroup" type="button" :loading="isActing" @click="addToSelectedGroup(person)">Add to {{ selectedGroup.name }}</AppButton><AppButton tone="quiet" type="button" @click="block(person.id)">Block</AppButton></div></article><p v-if="searchQuery && !results.length" class="circles__status">No people found. Try a different name.</p></section>
      <section v-if="activeTab === 'requests'" class="circles__section" data-surface="circle-requests"><h2>Connection requests</h2><p v-if="!requests.length && !outgoing.length" class="circles__status">No requests need your attention.</p><section v-if="requests.length"><h3>Incoming</h3><article v-for="request in requests" :key="request.id" class="circles__row"><div><strong>{{ request.requesterUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><div class="circles__actions"><AppButton tone="primary" :loading="isActing" @click="decide(request, true)">Accept</AppButton><AppButton tone="quiet" :loading="isActing" @click="decide(request, false)">Decline</AppButton></div></article></section><section v-if="outgoing.length"><h3>Sent</h3><article v-for="request in outgoing" :key="request.id" class="circles__row"><div><strong>{{ request.counterpartUsername }}</strong><span>{{ request.requestSummaryLabel }}</span></div><AppButton tone="quiet" :loading="isActing" @click="decide(request, false)">Cancel</AppButton></article></section></section>
      <section v-if="activeTab === 'groups'" class="circles__section" data-surface="circle-groups"><h2>Your circles</h2><p v-if="groups.length === 0" class="circles__status">No circles yet. Create one for family, friends, or a shared activity.</p><div v-else class="circles__circles-workspace"><div class="circles__circle-list"><article v-for="group in groups" :key="group.id" class="circles__row" :class="{ 'circles__row--selected': selectedGroupId === group.id }" @click="selectedGroupId = group.id"><form v-if="editingGroupId === group.id" class="circles__inline-edit" @submit.prevent="saveGroup"><input v-model="editingGroupName" required maxlength="120"><AppButton tone="primary" type="submit" :loading="isActing">Save</AppButton><AppButton tone="quiet" type="button" @click="editingGroupId = null">Cancel</AppButton></form><div v-else><strong>{{ group.name }}</strong><span>{{ group.memberCount }} members · {{ group.memberPreviewLabel }}</span></div><div v-if="editingGroupId !== group.id" class="circles__actions"><AppButton type="button" @click.stop="beginEditGroup(group)">Edit</AppButton><AppButton tone="danger" :loading="isActing" @click.stop="archiveGroup(group)">Remove</AppButton><AppButton tone="quiet" :loading="isActing" @click.stop="leaveCircle(group.id)">Leave</AppButton></div></article></div><aside v-if="selectedGroup" class="circles__context" aria-label="Selected circle context" data-visibility-boundary="circle"><p class="circles__eyebrow">Selected circle</p><h2>{{ selectedGroup.name }}</h2><p>{{ selectedGroup.memberCount }} members</p><dl><div><dt>Visibility</dt><dd>Circle members</dd></div><div><dt>Privileges</dt><dd>Shared activity and chat context</dd></div></dl><div v-if="selectedGroup.members?.length" class="circles__context-members"><strong>Members</strong><span v-for="member in selectedGroup.members" :key="member.userId">{{ member.username }} <AppButton tone="danger" :loading="isActing" @click="removeMember(selectedGroup.id, member.userId, member.username)">Remove</AppButton></span></div><div class="circles__club-actions"><AppButton tone="primary" :loading="isActing" @click="createClubChat(selectedGroup)">Create club chat</AppButton><RouterLink to="/calendar" class="circles__calendar-link">Plan a club event in Calendar</RouterLink></div><p class="circles__context-note">Exact location and private profile fields are never implied by membership. The server decides every module action.</p></aside><aside v-else class="circles__context circles__context--empty" aria-label="Circle context"><p class="circles__eyebrow">Circle context</p><h2>Select a circle</h2><p>Select a circle to inspect members and manage membership.</p></aside></div></section>
      <p v-if="error" class="circles__status circles__status--error" role="alert">{{ error }} <AppButton tone="quiet" type="button" @click="load">Retry</AppButton></p>
    </template>
    <AppDialog :open="createOpen" title="Create a circle" layout="workspace" @close="createOpen = false; guidedCircleDraft = null"><GuidedIntakePanel v-if="!guidedCircleDraft" flow="social.circle.create" title="Create a circle" @completed="acceptGuidedCircleDraft" @cancel="createOpen = false; guidedCircleDraft = null" /><form v-else class="circles__dialog-form" @submit.prevent="createGroup().then(() => { if (!error) { createOpen = false; guidedCircleDraft = null } })"><AppFormField label="Circle name" required><input v-model="groupName" placeholder="New circle name" maxlength="120" required></AppFormField><AppFormFooter><template #secondary><AppButton type="button" @click="guidedCircleDraft = null">Back</AppButton></template><template #primary><AppButton tone="primary" type="submit" :loading="isActing">Create circle</AppButton></template></AppFormFooter></form><template #utility><p>A circle is a trust boundary. Module-specific visibility and consent policies still apply after membership changes.</p></template></AppDialog>
  </section></TaskSurface>
</template>

<style scoped>
.circles { display:grid; gap:var(--space-3); }
.circles__header { display:flex; justify-content:space-between; align-items:end; }
.circles__intro,.circles__hint { margin:var(--space-1) 0 0; color:var(--text-muted); }
.circles__eyebrow { margin:0 0 var(--space-1); color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
.circles h1 { margin:0; color:var(--text); font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.circles h2 { margin:0; color:var(--text); font-size:var(--text-size-title); }
.circles h3 { margin:var(--space-2) 0 0; color:var(--text); font-size:var(--text-size-body); }
.circles__privacy { display:grid; gap:var(--space-1); max-width:48rem; padding:var(--space-2) var(--space-3); border-left:3px solid var(--accent); background:var(--surface-base); color:var(--text-muted); }
.circles__privacy summary { cursor:pointer; color:var(--text); font-weight:var(--text-weight-semibold); }
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
.circles__context { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.circles__context h2,.circles__context p { margin:0; }
.circles__context h2 { color:var(--text); }
.circles__context dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.circles__context dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.circles__context dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.circles__context dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.circles__context-members { display:grid; gap:var(--space-1); color:var(--text); }
.circles__context-members span { color:var(--text-muted); font-size:var(--text-size-meta); }
.circles__context-note { color:var(--text-soft); font-size:var(--text-size-meta); line-height:1.45; }
.circles__context--empty { min-height:10rem; align-content:center; }
.circles__dialog-form { display:grid; gap:var(--space-3); }
.circles__members { display:grid; gap:var(--space-1); margin-top:var(--space-1); color:var(--text-muted); font-size:var(--text-size-meta); }
.circles__members summary { cursor:pointer; color:var(--text-soft); }
@media(max-width:860px) { .circles__circles-workspace { grid-template-columns:1fr; } .circles__context { order:2; } }
@media(max-width:620px) { .circles__row { align-items:start; flex-direction:column; } .circles__actions { justify-content:flex-start; } .circles__search { grid-template-columns:1fr; max-width:none; } .circles__inline-edit { align-items:stretch; flex-wrap:wrap; } }
.circles__circle-list .circles__row:focus-within { outline:var(--focus-ring); outline-offset:-2px; }
</style>
