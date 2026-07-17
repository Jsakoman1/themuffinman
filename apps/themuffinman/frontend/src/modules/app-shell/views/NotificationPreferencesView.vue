<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import type {NotificationPreferenceItemDTO, NotificationPreferenceUpdateDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
const items = ref<NotificationPreferenceItemDTO[]>([])
const isLoading = ref(true)
const isSaving = ref(false)
const error = ref("")
const feedback = ref("")
const categories = computed(() => [...new Set(items.value.map(item => item.category))])
const labels: Record<string, string> = {CHAT: "Chat", WORK: "Work", BOOKING: "Bookings", CIRCLE: "Circles", LOCATION: "Location", SYSTEM: "System"}
const load = async () => { isLoading.value = true; error.value = ""; try { items.value = (await userShellApi.getNotificationPreferences()).items } catch { error.value = "Could not load notification preferences." } finally { isLoading.value = false } }
const save = async () => { isSaving.value = true; error.value = ""; feedback.value = ""; try { const updates: NotificationPreferenceUpdateDTO[] = items.value.map(item => ({category: item.category, level: item.level, enabled: item.enabled})); items.value = (await userShellApi.updateNotificationPreferences(updates)).items; feedback.value = "Notification preferences saved." } catch { error.value = "Could not save notification preferences. Required system notices remain enabled." } finally { isSaving.value = false } }
onMounted(() => void load())
</script>
<template><section class="notification-preferences"><header><p class="eyebrow">Profile / Settings</p><h1>Notifications</h1><p class="intro">Choose where product updates arrive. System notices stay enabled for account and safety reasons.</p></header><div v-if="isLoading" class="status" role="status">Loading.</div><div v-else-if="error" class="status status--error" role="alert">{{ error }} <button type="button" @click="load">Retry</button></div><form v-else @submit.prevent="save"><fieldset v-for="category in categories" :key="category"><legend>{{ labels[category] || category }}</legend><label v-for="item in items.filter(value => value.category === category)" :key="`${item.category}-${item.level}`" class="option"><input v-model="item.enabled" type="checkbox" :disabled="item.required || isSaving"><span>{{ item.level.replace('_', ' ') }}<small v-if="item.required">Required</small></span></label></fieldset><p v-if="feedback" class="feedback" role="status">{{ feedback }}</p><button class="save" type="submit" :disabled="isSaving">{{ isSaving ? "Saving" : "Save preferences" }}</button></form></section></template>
<style scoped>
.notification-preferences{display:grid;gap:1rem;max-width:44rem}.eyebrow{margin:0 0 .3rem;color:rgba(23,34,26,.55);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}h1{margin:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em}.intro{max-width:38rem;color:rgba(23,34,26,.65)}form{display:grid;gap:1rem;padding:1rem;border:1px solid rgba(23,34,26,.08);border-radius:1rem;background:rgba(255,255,255,.62)}fieldset{display:grid;gap:.55rem;border:0;padding:0;margin:0}legend{font-weight:700;margin-bottom:.15rem}.option{display:flex;align-items:center;gap:.6rem;font-weight:550}.option input{width:1rem;height:1rem}.option span{display:flex;gap:.5rem;align-items:center}.option small{color:rgba(23,34,26,.55);font-size:.72rem}.save,.status button{justify-self:start;border:1px solid rgba(23,34,26,.14);border-radius:999px;padding:.6rem .85rem;background:#17221a;color:#f8f8f4;font:inherit;font-weight:650;cursor:pointer}.save:disabled{opacity:.55}.feedback{margin:0;color:#2d6846}.status{padding:.7rem 0;color:rgba(23,34,26,.65)}.status--error{color:#8d2f25}.status button{margin-left:.5rem;background:transparent;color:inherit;text-decoration:underline}
</style>
