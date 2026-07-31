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
const profileNeedsIntroduction = computed(() => !currentUser.value?.profileDescription?.trim())
const nextProfileAction = computed(() => profileNeedsIntroduction.value ? "Add a short introduction" : "Review profile and privacy")
const privacyPosture = computed(() => profileNeedsIntroduction.value
  ? "Your contact details stay private. Choose later what your profile shares."
  : "You control who can see your profile, description, and location.")
</script>

<template>
  <section class="profile-page" aria-label="Profile and settings" data-navigation-model="sibling-tabs" data-mental-model="summary-select-edit-save">
    <ModuleTabs :tabs="tabs" :active-id="activeId" />
    <p class="profile-page__mode" data-profile-mode="progressive">{{ activeId === "settings" ? "Choose what you want to manage: your profile, appearance, location and privacy, or notifications." : "The public identity people see." }}</p>
    <section v-if="activeId === 'profile'" class="profile-summary profile-summary--friendly" aria-labelledby="profile-summary-title" data-profile-first-view="identity-privacy-next-action">
      <div class="profile-summary__identity"><div class="profile-summary__avatar">{{ (currentUser?.username?.[0] || "A").toUpperCase() }}</div><div><p class="profile-summary__eyebrow">Your profile</p><h1 id="profile-summary-title">{{ currentUser?.username || "Your profile" }}</h1><p>{{ currentUser?.profileDescription || "A short introduction helps people recognise who they are connecting with." }}</p></div></div>
      <section class="profile-summary__posture" aria-label="Profile privacy summary"><p class="profile-summary__posture-label">Privacy</p><p>{{ privacyPosture }}</p></section>
      <dl class="profile-summary__facts" aria-label="Private account details"><div><dt>Email</dt><dd>{{ currentUser?.email || "Not available" }}</dd></div><div><dt>Member since</dt><dd>{{ currentUser?.createdAt ? new Date(currentUser.createdAt).toLocaleDateString() : "—" }}</dd></div></dl>
      <div class="profile-summary__next"><div><p class="profile-summary__posture-label">Next step</p><p>{{ profileNeedsIntroduction ? "Add a few words about yourself, then decide who can see them." : "Check that your profile still says what you want people to know." }}</p></div><RouterLink class="profile-summary__edit" to="/profile/settings">{{ nextProfileAction }}</RouterLink></div>
    </section>
    <template v-else>
      <nav class="profile-page__settings-intents" aria-label="Profile settings by purpose">
        <RouterLink to="/profile/settings">Profile, appearance, location and privacy</RouterLink>
        <RouterLink to="/profile/settings/notifications">Notifications</RouterLink>
      </nav>
      <ProfileLocationSettingsView :key="activeId" />
    </template>
  </section>
</template>
<style scoped>
.profile-page{container-type:inline-size}

.profile-summary{border-top-color:var(--orientation-line)}.profile-summary__posture,.profile-summary__next{background:var(--orientation-action-bg)}

.profile-page{display:grid;gap:var(--space-4);min-width:0}.profile-page__mode{margin:0;color:var(--text-muted);font-size:var(--text-size-body)}.profile-page__settings-intents{display:flex;flex-wrap:wrap;gap:var(--space-2)}.profile-page__settings-intents a{padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold)}.profile-page__settings-intents a:hover{border-color:var(--control-border-active);background:var(--surface-hover)}.profile-summary{display:grid;gap:var(--space-4);max-width:48rem;padding:var(--space-5);border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-raised)}.profile-summary__identity{display:flex;align-items:center;gap:var(--space-4)}.profile-summary__avatar{display:grid;place-items:center;width:4.5rem;height:4.5rem;border-radius:50%;background:var(--surface-selected);color:var(--text);font-size:1.5rem;font-weight:var(--text-weight-semibold)}.profile-summary__eyebrow,.profile-summary__posture-label{margin:0;color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);text-transform:uppercase}.profile-summary h1{margin:.15rem 0;color:var(--text);font-size:var(--text-size-page-title)}.profile-summary p:last-child{margin:0;color:var(--text-muted)}.profile-summary__posture,.profile-summary__next{padding:var(--space-3);border-radius:var(--radius-control);background:var(--surface-subtle)}.profile-summary__posture p:last-child,.profile-summary__next p:last-child{margin-top:var(--space-1)}.profile-summary__facts{display:grid;gap:var(--space-2);margin:0}.profile-summary__facts div{display:flex;justify-content:space-between;gap:var(--space-3);padding-top:var(--space-2);border-top:1px solid var(--border-subtle)}.profile-summary__facts dt{color:var(--text-soft)}.profile-summary__facts dd{margin:0;color:var(--text);font-weight:var(--text-weight-semibold)}.profile-summary__next{display:flex;align-items:center;justify-content:space-between;gap:var(--space-4)}.profile-summary__edit{display:inline-flex;flex:none;justify-self:start;padding:var(--space-2) var(--space-3);border-radius:var(--radius-control);background:var(--accent);color:var(--accent-contrast);font-weight:var(--text-weight-semibold);text-decoration:none}@media(max-width:600px){.profile-summary{padding:var(--space-4)}.profile-summary__identity,.profile-summary__next,.profile-summary__facts div{align-items:start;flex-direction:column}.profile-summary__facts div{gap:var(--space-1)}}
</style>


