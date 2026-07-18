<script setup lang="ts">
import {RouterLink} from "vue-router"
import {currentUser, logoutUser} from "../../identity/auth.ts"

const handleLogout = () => {
  logoutUser()
  window.location.assign("/login")
}
</script>

<template>
  <details class="account-menu">
    <summary class="account-menu__summary" aria-label="Open account menu">
      <img v-if="currentUser?.profileAvatarDataUrl" :src="currentUser.profileAvatarDataUrl" alt="" class="account-menu__avatar">
      <span v-else class="account-menu__avatar account-menu__avatar--fallback">{{ (currentUser?.username?.[0] ?? "A").toUpperCase() }}</span>
      <span class="account-menu__username">{{ currentUser?.username ?? "Account" }}</span>
    </summary>
    <div class="account-menu__panel">
      <div class="account-menu__identity"><strong>{{ currentUser?.username ?? "Account" }}</strong><span>{{ currentUser?.email ?? "" }}</span></div>
      <RouterLink to="/profile">My profile</RouterLink>
          <RouterLink to="/profile/settings">Profile settings</RouterLink>
          <RouterLink to="/onboarding">Setup guide</RouterLink>
          <RouterLink to="/activity">Activity</RouterLink>
      <RouterLink to="/profile/settings/notifications">Notification settings</RouterLink>
      <button type="button" @click="handleLogout">Log out</button>
    </div>
  </details>
</template>

<style scoped>
.account-menu{position:relative}.account-menu__summary{display:flex;align-items:center;gap:.45rem;cursor:pointer;list-style:none;color:var(--text-muted)}.account-menu__summary::-webkit-details-marker{display:none}.account-menu__avatar{width:1.85rem;height:1.85rem;border-radius:50%;object-fit:cover;border:1px solid var(--border-strong)}.account-menu__avatar--fallback{display:grid;place-items:center;background:var(--surface-strong);color:var(--text);font-size:.78rem;font-weight:700}.account-menu__username{max-width:10rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.82rem}.account-menu__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;display:grid;gap:.25rem;min-width:13rem;padding:.55rem;border:1px solid var(--border-strong);border-radius:var(--radius-card);background:var(--surface-strong);box-shadow:var(--shadow-card)}.account-menu__panel a,.account-menu__panel button{padding:.6rem .7rem;border:0;border-radius:var(--radius-control);background:transparent;color:var(--text);text-align:left;font:inherit;font-size:.82rem}.account-menu__panel a:hover,.account-menu__panel button:hover{background:var(--surface-hover)}.account-menu__identity{display:grid;gap:.15rem;padding:.55rem .7rem .7rem;border-bottom:1px solid var(--border-subtle)}.account-menu__identity span{overflow:hidden;color:var(--text-muted);font-size:.74rem;text-overflow:ellipsis}
</style>
