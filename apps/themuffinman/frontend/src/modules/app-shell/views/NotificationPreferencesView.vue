<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import type {NotificationPreferenceItemDTO, NotificationPreferenceUpdateDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppFormFooter from "../components/AppFormFooter.vue"
import AppButton from "../components/AppButton.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
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
<template><section class="notification-preferences"><header><p class="eyebrow">Profile / Settings</p><h1>Notifications</h1><p class="intro">Choose where product updates arrive. System notices stay enabled for account and safety reasons.</p></header><CollectionToolbar title="Notification delivery" :count="items.length" :busy="isLoading" /><AppStatus v-if="isLoading" message="Loading notification preferences." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><form v-else @submit.prevent="save"><fieldset v-for="category in categories" :key="category"><legend>{{ labels[category] || category }}</legend><label v-for="item in items.filter(value => value.category === category)" :key="`${item.category}-${item.level}`" class="option"><input v-model="item.enabled" type="checkbox" :disabled="item.required || isSaving"><span>{{ item.level.replace('_', ' ') }}<small v-if="item.required">Required by the backend</small></span></label></fieldset><AppStatus v-if="feedback" :message="feedback" tone="success" /><AppStatus v-if="error" :message="error" tone="error" /><AppFormFooter sticky><template #secondary><span>Required system notices cannot be disabled.</span></template><template #primary><AppButton type="submit" tone="primary" :loading="isSaving">{{ isSaving ? "Saving" : "Save preferences" }}</AppButton></template></AppFormFooter></form></section></template>
<style scoped>
.notification-preferences{display:grid;gap:var(--space-3);max-width:60rem}.eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}h1{margin:0;color:var(--text);font-size:var(--text-size-page-title);letter-spacing:var(--tracking-tight)}.intro{max-width:38rem;color:var(--text-muted)}form{display:grid;gap:var(--space-4);padding:var(--space-4);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}fieldset{display:grid;gap:var(--space-2);border:0;padding:0;margin:0}legend{color:var(--text);font-size:var(--text-size-title);font-weight:var(--text-weight-semibold)}.option{display:flex;align-items:center;gap:var(--space-2);min-height:var(--control-height-default);padding:var(--space-1) var(--space-2);border:1px solid var(--border-subtle);border-radius:var(--radius-control);color:var(--text);font-weight:var(--text-weight-medium)}.option input{width:1rem;height:1rem}.option span{display:flex;gap:var(--space-2);align-items:center}.option small,.app-form-footer__secondary{color:var(--text-soft);font-size:var(--text-size-meta)}
</style>
<style scoped>
.notification-preferences .app-button { border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); }
.notification-preferences .app-button--primary { border-color:var(--accent); background:var(--accent); color:var(--canvas); }
</style>
