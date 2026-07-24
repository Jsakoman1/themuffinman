<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {QuestNewsItemResponseDTO} from "../../../contracts/index.ts"
import {userShellApi, type ActivityItem, type AttentionCenter} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import {formatDateTime} from "../../../services/formatters.ts"

const rawItems = ref<QuestNewsItemResponseDTO[]>([])
const notificationCategory = ref<"ALL" | "NEEDS_ACTION" | "BOOKING" | "WORK" | "CHAT" | "SOCIAL" | "SYSTEM">("ALL")
const snoozedUntil = ref<Record<string, number>>({})
const mutedCategories = ref<string[]>([])
const categoryFor = (item: QuestNewsItemResponseDTO) => {
  if (!item.readAt && (item.destinationType === "APPLICATION" || item.destinationType === "QUEST")) return "NEEDS_ACTION"
  const type = String(item.destinationType || "").toUpperCase()
  if (type.includes("BOOKING")) return "BOOKING"
  if (type.includes("CHAT")) return "CHAT"
  if (type.includes("CIRCLE") || type.includes("SOCIAL")) return "SOCIAL"
  if (item.destinationType === "APPLICATION" || item.destinationType === "QUEST") return "WORK"
  return "SYSTEM"
}
const isSnoozed = (item: QuestNewsItemResponseDTO) => (snoozedUntil.value[String(item.id)] || 0) > Date.now()
const matchesCategory = (item: QuestNewsItemResponseDTO) => !isSnoozed(item) && !mutedCategories.value.includes(categoryFor(item)) && (notificationCategory.value === "ALL" || categoryFor(item) === notificationCategory.value)
const items = computed({get: () => rawItems.value.filter(matchesCategory), set: (value: QuestNewsItemResponseDTO[]) => { rawItems.value = value }})
const categoryCounts = computed(() => Object.fromEntries(["NEEDS_ACTION", "BOOKING", "WORK", "CHAT", "SOCIAL", "SYSTEM"].map(category => [category, rawItems.value.filter(item => categoryFor(item) === category).length])))
const isLoading = ref(true)
const isActing = ref(false)
const error = ref("")
const feedback = ref("")
const attention = ref<AttentionCenter | null>(null)
const selectedNotificationId = ref<number | null>(null)
const selectedNotification = computed(() => items.value.find((item) => item.id === selectedNotificationId.value) ?? null)
const formatDate = (value: string) => formatDateTime(value, "Unknown time")
const load = async () => { isLoading.value = true; error.value = ""; try { const [news, center] = await Promise.all([userShellApi.getMyNews(), userShellApi.getAttentionCenter()]); rawItems.value = news; attention.value = center } catch { error.value = "Could not load notifications." } finally { isLoading.value = false } }
const markAllRead = async () => { const pendingCategoryCount = categoryCounts.value.NEEDS_ACTION || 0; isActing.value = true; error.value = ""; try { await userShellApi.markNewsAsRead(); feedback.value = `All notifications marked as read. ${pendingCategoryCount} needed action before this change.`; await load() } catch { error.value = "Could not update notifications." } finally { isActing.value = false } }
const markRead = async (item: QuestNewsItemResponseDTO) => { selectedNotificationId.value = item.id; if (!item.readAt) { try { await userShellApi.markNewsItemAsRead(item.id); item.readAt = new Date().toISOString() } catch { error.value = "Could not update this notification." } } }
const destination = (item: QuestNewsItemResponseDTO) => {
  if (item.destinationType === "APPLICATION" && item.destinationId) return `/work/applications/${item.destinationId}`
  if (item.destinationType === "QUEST" && item.destinationId) return `/work/quests/${item.destinationId}`
  return null
}
const attentionDestination = (item: ActivityItem) => item.route || null
const snoozeSelected = () => { if (!selectedNotification.value) return; snoozedUntil.value = {...snoozedUntil.value, [selectedNotification.value.id]: Date.now() + 24 * 60 * 60 * 1000}; localStorage.setItem("notificationSnoozedUntil", JSON.stringify(snoozedUntil.value)); feedback.value = "Notification snoozed for 24 hours." }
const muteSelectedCategory = () => { if (!selectedNotification.value) return; const category = categoryFor(selectedNotification.value); if (!mutedCategories.value.includes(category)) mutedCategories.value = [...mutedCategories.value, category]; localStorage.setItem("notificationMutedCategories", JSON.stringify(mutedCategories.value)); feedback.value = `${category} notifications muted on this device.` }
const handleNotificationShortcut = (event: KeyboardEvent) => { if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement || event.target instanceof HTMLSelectElement) return; if (event.key.toLowerCase() === "s") snoozeSelected(); if (event.key.toLowerCase() === "m") muteSelectedCategory(); if (event.key.toLowerCase() === "d" && selectedNotification.value) void markRead(selectedNotification.value) }
onMounted(() => { try { snoozedUntil.value = JSON.parse(localStorage.getItem("notificationSnoozedUntil") || "{}"); mutedCategories.value = JSON.parse(localStorage.getItem("notificationMutedCategories") || "[]") } catch { /* local preferences are recoverable */ } void load(); window.addEventListener("keydown", handleNotificationShortcut) })
onBeforeUnmount(() => window.removeEventListener("keydown", handleNotificationShortcut))
</script>

<template><section class="notifications"><header class="notifications__header"><div><p class="notifications__eyebrow">Updates</p><h1>Notifications</h1></div></header><CollectionToolbar title="Updates" :count="items.length" :busy="isLoading"><template #actions><AppButton tone="secondary" :loading="isActing" @click="markAllRead">Mark all read</AppButton></template></CollectionToolbar><AppStatus v-if="feedback" :message="feedback" tone="success" /><section v-if="attention" class="attention-center" aria-labelledby="attention-title"><div><p class="notifications__eyebrow">Attention center</p><h2 id="attention-title">{{ attention.unreadCount }} unread updates</h2><p>Resume a guided task or open the allowed destination from one compact view.</p></div><div v-if="attention.items.length" class="attention-center__items"><SurfaceRow v-for="item in attention.items.slice(0, 4)" :key="`${item.kind}-${item.resumeKey || item.occurredAt}`" :row="{title: item.title, description: item.summary, badge: item.kind}"><template #actions><RouterLink v-if="attentionDestination(item)" :to="attentionDestination(item)!">{{ item.resumable ? 'Continue' : 'Open' }}</RouterLink></template></SurfaceRow></div><AppStatus v-else message="Nothing needs attention right now." /></section><AppStatus v-if="isLoading" message="Loading notifications." busy /><AppStatus v-else-if="error && items.length === 0" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="items.length === 0" message="No notifications." /><div v-else class="notifications__workspace"><div class="notifications__list"><SurfaceRow v-for="item in items" :key="item.id" :row="{id: String(item.id), title: item.title, description: item.message, meta: `${formatDate(item.createdAt)}${item.actorUsername ? ` · ${item.actorUsername}` : ''}`, badge: item.readAt ? undefined : 'Unread'}" :selected="selectedNotificationId === item.id" @click="markRead(item)" /></div><aside v-if="selectedNotification" class="notifications__preview" aria-label="Notification preview"><p class="notifications__eyebrow">Selected update</p><h2>{{ selectedNotification.title }}</h2><p>{{ selectedNotification.message }}</p><dl><div><dt>Received</dt><dd>{{ formatDate(selectedNotification.createdAt) }}</dd></div><div v-if="selectedNotification.actorUsername"><dt>From</dt><dd>{{ selectedNotification.actorUsername }}</dd></div><div><dt>Status</dt><dd>{{ selectedNotification.readAt ? 'Read' : 'Unread' }}</dd></div></dl><RouterLink v-if="destination(selectedNotification)" class="notifications__preview-link" :to="destination(selectedNotification)!">Open full detail</RouterLink><p v-else class="notifications__preview-note">This update has no canonical destination.</p></aside><aside v-else class="notifications__preview notifications__preview--empty" aria-label="Notification preview"><p class="notifications__eyebrow">Preview</p><h2>Select an update</h2><p>Keep the collection context while inspecting a notification.</p></aside></div><AppStatus v-if="error && items.length" :message="error" tone="error" retry @retry="load" /></section></template>

<style scoped>
.notifications { display:grid; gap:var(--space-3); }
.notifications__header { display:flex; justify-content:space-between; align-items:end; gap:var(--space-3); }
.notifications__eyebrow { margin:0 0 var(--space-1); color:var(--text-soft); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); text-transform:uppercase; }
.notifications h1 { margin:0; font-size:var(--text-size-page-title); letter-spacing:var(--tracking-tight); }
.notifications__list { display:grid; gap:0; overflow:hidden; border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.notifications__workspace { display:grid; grid-template-columns:minmax(0,1fr) minmax(16rem,22rem); gap:var(--space-3); align-items:start; }
.notifications__preview { display:grid; gap:var(--space-2); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-raised); color:var(--text-muted); }
.notifications__preview h2,.notifications__preview p { margin:0; }
.notifications__preview h2 { color:var(--text); font-size:var(--text-size-title); }
.notifications__preview dl { display:grid; gap:var(--space-2); margin:var(--space-2) 0; }
.notifications__preview dl div { display:flex; justify-content:space-between; gap:var(--space-2); border-top:1px solid var(--border-subtle); padding-top:var(--space-2); }
.notifications__preview dt { color:var(--text-soft); font-size:var(--text-size-meta); }
.notifications__preview dd { margin:0; color:var(--text); font-size:var(--text-size-meta); font-weight:var(--text-weight-semibold); }
.notifications__preview-link { justify-self:start; min-height:var(--control-height-default); display:inline-flex; align-items:center; padding:var(--space-1) var(--space-3); border:1px solid var(--control-border); border-radius:var(--radius-control); color:var(--control-ink); font-size:var(--text-size-body); font-weight:var(--text-weight-semibold); text-decoration:none; }
.notifications__preview-link:hover { border-color:var(--control-border-active); background:var(--control-bg-hover); }
.notifications__preview-note { color:var(--text-soft); font-size:var(--text-size-meta); }
.notifications__preview--empty { min-height:10rem; align-content:center; }
.attention-center { display:grid; gap:var(--space-3); padding:var(--space-3); border:1px solid var(--border-subtle); border-radius:var(--radius-surface); background:var(--surface-base); }
.attention-center h2,.attention-center p { margin:0; }
.attention-center h2 { font-size:var(--text-size-title); }
.attention-center p { color:var(--text-muted); font-size:var(--text-size-meta); }
.attention-center__items { display:grid; gap:var(--space-1); }
.attention-center__items article { display:flex; justify-content:space-between; gap:var(--space-3); align-items:center; padding:var(--space-2); border:1px solid var(--border-subtle); border-radius:var(--radius-control); background:var(--surface-raised); }
.attention-center__items article>div { display:grid; gap:var(--space-1); }
.attention-center__items small,.attention-center__items span { color:var(--text-muted); font-size:var(--text-size-meta); }
@media(max-width:860px){.notifications__workspace{grid-template-columns:1fr}.notifications__preview{order:2}}
</style>
