<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import {userShellApi, type ActivityItem} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import AppButton from "../components/AppButton.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import FriendlyCollectionHeader from "../components/FriendlyCollectionHeader.vue"
import FriendlySummaryCard from "../components/FriendlySummaryCard.vue"

const items = ref<ActivityItem[]>([])
const loading = ref(true)
const error = ref("")
const dismissingKey = ref<string | null>(null)
const load = async () => { loading.value = true; error.value = ""; try { items.value = await userShellApi.getActivity() } catch { error.value = "Could not load activity." } finally { loading.value = false } }
const dismiss = async (item: ActivityItem) => { if (!item.resumeKey) return; dismissingKey.value = item.resumeKey; try { await userShellApi.dismissActivityResume(item.resumeKey); items.value = items.value.filter(current => current.resumeKey !== item.resumeKey) } catch { error.value = "Could not dismiss this suggestion." } finally { dismissingKey.value = null } }
onMounted(() => void load())
</script>
<style scoped>
.activity__workspace{border-top-color:var(--orientation-line)}

.activity{display:grid;gap:var(--space-3);max-width:none}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}header p:last-child,.activity__selection-hint{color:var(--text-muted)}.activity__workspace{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.list{min-width:0;overflow:hidden}.list :deep(.surface-row:last-child){border-bottom:0}.list :deep(.surface-row){border-radius:var(--radius-card)}.activity__workspace{box-shadow:var(--shadow-card)}
</style>
<template><section class="activity"><FriendlyCollectionHeader eyebrow="Your space" title="Activity" description="Pick up what needs you, then review the rest when you have time." /><FriendlySummaryCard v-if="items[0]" eyebrow="Start here" :title="items[0].title" :description="items[0].summary" :action-label="items[0].primaryActionLabel || (items[0].resumable ? 'Resume' : 'Open')" :to="items[0].route || '/activity'" tone="people" /><CollectionToolbar title="Recent activity" :count="items.length" :busy="loading" /><AppStatus v-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-if="loading" message="Loading activity." busy /><AppStatus v-else-if="!items.length" message="No recent activity." /><div v-else class="activity__workspace"><div class="list"><SurfaceRow v-for="item in items" :key="`${item.kind}-${item.resumeKey || item.occurredAt}`" :row="{id: `${item.kind}-${item.resumeKey || item.occurredAt}`, title: item.title, description: item.summary, badge: item.kind, meta: item.resumable ? 'Resume available' : undefined}"><template #actions><RouterLink v-if="item.route" :to="item.route">{{ item.primaryActionLabel || (item.resumable ? 'Resume' : 'Open') }}</RouterLink><AppButton v-if="item.resumable" type="button" tone="quiet" :aria-label="`Remove ${item.title} from activity`" :loading="dismissingKey === item.resumeKey" @click="dismiss(item)">Remove</AppButton></template></SurfaceRow></div></div></section></template>

