<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {useRouter} from "vue-router"
import type {QuestNewsItemResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import NotificationItem from "../components/NotificationItem.vue"

const router = useRouter()
const items = ref<QuestNewsItemResponseDTO[]>([])
const isLoading = ref(true)
const isActing = ref(false)
const error = ref("")
const unreadCount = computed(() => items.value.filter(item => !item.readAt).length)
const load = async () => { isLoading.value = true; error.value = ""; try { items.value = await userShellApi.getMyNews() } catch { error.value = "Could not load updates." } finally { isLoading.value = false } }
const destination = (item: QuestNewsItemResponseDTO) => item.destinationType === "APPLICATION" && item.destinationId ? `/work/applications/${item.destinationId}` : item.destinationType === "QUEST" && item.destinationId ? `/work/quests/${item.destinationId}` : null
const open = async (item: QuestNewsItemResponseDTO) => { if (!item.readAt) { try { await userShellApi.markNewsItemAsRead(item.id); item.readAt = new Date().toISOString() } catch { error.value = "Could not mark this update as read."; return } } const target = destination(item); if (target) await router.push(target) }
const markAllRead = async () => { isActing.value = true; error.value = ""; try { await userShellApi.markNewsAsRead(); items.value = items.value.map(item => ({...item, readAt: item.readAt || new Date().toISOString()})) } catch { error.value = "Could not mark updates as read." } finally { isActing.value = false } }
onMounted(() => { void load() })
</script>

<template>
  <section class="notifications" :aria-busy="isLoading || undefined">
    <CollectionToolbar title="Updates" :count="unreadCount" :busy="isLoading"><template #actions><AppButton tone="secondary" :loading="isActing" @click="markAllRead">Mark all read</AppButton></template></CollectionToolbar>
    <AppStatus v-if="isLoading" message="Loading updates." busy />
    <AppStatus v-else-if="error && !items.length" :message="error" tone="error" retry @retry="load" />
    <AppStatus v-else-if="!items.length" message="You are all caught up." />
    <div v-else class="notifications__list"><section v-for="item in items" :key="item.id"><p v-if="item.createdAt.startsWith(new Date().toISOString().slice(0, 10))" class="notifications__group">Today</p><NotificationItem :item="item" @open="open" /></section></div>
    <AppStatus v-if="error && items.length" :message="error" tone="error" retry @retry="load" />
  </section>
</template>

<style scoped>
.notifications{display:grid;gap:var(--space-3)}.notifications__list{overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.notifications__group{margin:0;padding:var(--space-2) var(--space-3);border-bottom:1px solid var(--border-subtle);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}
</style>
