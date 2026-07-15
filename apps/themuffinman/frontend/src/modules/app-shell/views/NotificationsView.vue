<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {QuestNewsItemResponseDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"

const items = ref<QuestNewsItemResponseDTO[]>([])
const isLoading = ref(true)
const isActing = ref(false)
const error = ref("")
const feedback = ref("")
const formatDate = (value: string) => new Intl.DateTimeFormat("en-US", {month: "short", day: "numeric", hour: "numeric", minute: "2-digit"}).format(new Date(value))
const load = async () => { isLoading.value = true; error.value = ""; try { items.value = await userShellApi.getMyNews() } catch { error.value = "Could not load notifications." } finally { isLoading.value = false } }
const markAllRead = async () => { isActing.value = true; error.value = ""; try { await userShellApi.markNewsAsRead(); feedback.value = "All notifications marked as read."; await load() } catch { error.value = "Could not update notifications." } finally { isActing.value = false } }
const markRead = async (item: QuestNewsItemResponseDTO) => { if (!item.readAt) { try { await userShellApi.markNewsItemAsRead(item.id); item.readAt = new Date().toISOString() } catch { error.value = "Could not update this notification." } } }
const destination = (item: QuestNewsItemResponseDTO) => item.questId ? `/work/quests/${item.questId}` : null
onMounted(() => void load())
</script>

<template><section class="notifications"><header class="notifications__header"><div><p class="notifications__eyebrow">Updates</p><h1>Notifications</h1></div><button type="button" :disabled="isActing" @click="markAllRead">Mark all read</button></header><p v-if="feedback" class="notifications__feedback" role="status">{{ feedback }}</p><div v-if="isLoading" class="notifications__status" role="status">Loading.</div><div v-else-if="error && items.length === 0" class="notifications__status notifications__status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div><div v-else-if="items.length === 0" class="notifications__status">No notifications.</div><div v-else class="notifications__list"><article v-for="item in items" :key="item.id" class="notifications__row" :class="{'notifications__row--unread': !item.readAt}" @click="markRead(item)"><div><strong>{{ item.title }}</strong><span>{{ item.message }}</span><small>{{ formatDate(item.createdAt) }}<template v-if="item.actorUsername"> · {{ item.actorUsername }}</template></small></div><RouterLink v-if="destination(item)" :to="destination(item)!" @click.stop>Open</RouterLink></article></div><p v-if="error && items.length" class="notifications__status notifications__status--error" role="alert">{{ error }}</p></section></template>

<style scoped>
.notifications{display:grid;gap:1rem}.notifications__header{display:flex;justify-content:space-between;align-items:end;gap:1rem}.notifications__eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.notifications button,.notifications a{border:1px solid rgba(23,34,26,.12);border-radius:999px;padding:.45rem .75rem;background:transparent;color:inherit;font:inherit;font-size:.82rem;font-weight:650;cursor:pointer;text-decoration:none}.notifications__list{display:grid;gap:.45rem}.notifications__row{display:flex;justify-content:space-between;align-items:center;gap:1rem;padding:.85rem;border-bottom:1px solid rgba(23,34,26,.08);border-left:2px solid transparent;cursor:pointer}.notifications__row--unread{border-left-color:#2d6846;background:rgba(45,104,70,.04)}.notifications__row>div{display:grid;gap:.28rem}.notifications__row span,.notifications__row small{color:rgba(23,34,26,.58);font-size:.84rem}.notifications__feedback{margin:0;color:#2d6846}.notifications__status{padding:.7rem 0;color:rgba(23,34,26,.65)}.notifications__status--error{color:#8d2f25}.notifications__status button{margin-left:.5rem;border:0;color:inherit;text-decoration:underline}@media(max-width:620px){.notifications__row{align-items:start;flex-direction:column}}
</style>
