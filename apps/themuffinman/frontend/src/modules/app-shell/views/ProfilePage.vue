<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import {currentUser} from "../../identity/auth.ts"
import ProfileLocationSettingsView from "./ProfileLocationSettingsView.vue"

const route = useRoute()
const tabs = computed(() => getModuleTabs("profile")?.tabs ?? [])
const activeId = computed(() => route.path.includes("/settings") ? "settings" : "profile")
</script>

<template>
  <section class="profile-page" aria-label="Profile and settings" data-navigation-model="sibling-tabs" data-mental-model="summary-select-edit-save">
    <ModuleTabs :tabs="tabs" :active-id="activeId" />
    <p class="profile-page__mode" data-profile-mode="progressive">{{ activeId === "settings" ? "Settings are grouped by privacy, location, appearance, and account safety." : "The public identity people see." }}</p>
    <section v-if="activeId === 'profile'" class="profile-summary" aria-labelledby="profile-summary-title">
      <div class="profile-summary__identity"><div class="profile-summary__avatar">{{ (currentUser?.username?.[0] || "A").toUpperCase() }}</div><div><p class="profile-summary__eyebrow">Your profile</p><h1 id="profile-summary-title">{{ currentUser?.username || "Your profile" }}</h1><p>{{ currentUser?.profileDescription || "Add a short description so people know who they are connecting with." }}</p></div></div>
      <dl class="profile-summary__facts"><div><dt>Email</dt><dd>{{ currentUser?.email || "Not available" }}</dd></div><div><dt>Member since</dt><dd>{{ currentUser?.createdAt ? new Date(currentUser.createdAt).toLocaleDateString() : "—" }}</dd></div></dl>
      <RouterLink class="profile-summary__edit" to="/profile/settings">Edit profile and privacy</RouterLink>
    </section>
    <ProfileLocationSettingsView v-else :key="activeId" />
  </section>
</template>

<style scoped>.profile-page{display:grid;gap:var(--space-4);min-width:0}.profile-page__mode{margin:0;color:var(--text-muted);font-size:var(--text-size-body)}.profile-summary{display:grid;gap:var(--space-4);max-width:48rem;padding:var(--space-5);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.profile-summary__identity{display:flex;align-items:center;gap:var(--space-4)}.profile-summary__avatar{display:grid;place-items:center;width:4.5rem;height:4.5rem;border-radius:50%;background:var(--surface-selected);color:var(--text);font-size:1.5rem;font-weight:var(--text-weight-semibold)}.profile-summary__eyebrow{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);text-transform:uppercase}.profile-summary h1{margin:.15rem 0;color:var(--text);font-size:var(--text-size-page-title)}.profile-summary p:last-child{margin:0;color:var(--text-muted)}.profile-summary__facts{display:grid;gap:var(--space-2);margin:0}.profile-summary__facts div{display:flex;justify-content:space-between;gap:var(--space-3);padding-top:var(--space-2);border-top:1px solid var(--border-subtle)}.profile-summary__facts dt{color:var(--text-soft)}.profile-summary__facts dd{margin:0;color:var(--text);font-weight:var(--text-weight-semibold)}.profile-summary__edit{justify-self:start;color:var(--accent);font-weight:var(--text-weight-semibold)}@media(max-width:600px){.profile-summary{padding:var(--space-4)}.profile-summary__identity{align-items:start;flex-direction:column}.profile-summary__facts div{align-items:start;flex-direction:column;gap:var(--space-1)}}</style>
