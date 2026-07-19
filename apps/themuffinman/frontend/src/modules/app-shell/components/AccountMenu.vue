<script setup lang="ts">
import {RouterLink} from "vue-router"
import {currentUser, logoutUser} from "../../identity/auth.ts"
import {authApi} from "../../identity/api/authApi.ts"

const handleLogout = async () => {
  try {
    await authApi.logout()
  } finally {
    logoutUser()
    window.location.assign("/login")
  }
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
.account-menu { position: relative; }
.account-menu__summary { display: flex; align-items: center; gap: var(--space-2); min-height: var(--control-height-default); padding: var(--space-1) var(--space-2); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink-muted); cursor: pointer; list-style: none; }
.account-menu__summary:hover, .account-menu[open] .account-menu__summary { border-color: var(--control-border-active); background: var(--control-bg-hover); color: var(--control-ink); }
.account-menu__summary:focus-visible { outline: var(--focus-ring); outline-offset: 2px; }
.account-menu__summary::-webkit-details-marker { display: none; }
.account-menu__avatar { width: 1.85rem; height: 1.85rem; border-radius: 50%; object-fit: cover; border: 1px solid var(--border-strong); }
.account-menu__avatar--fallback { display: grid; place-items: center; background: var(--surface-strong); color: var(--text); font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.account-menu__username { max-width: 10rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: var(--text-size-meta); }
.account-menu__panel { position: absolute; right: 0; top: calc(100% + var(--space-2)); z-index: var(--z-popover); display: grid; gap: var(--space-1); min-width: 13rem; padding: var(--space-1); border: 1px solid var(--border-strong); border-radius: var(--radius-surface); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }
.account-menu__panel a, .account-menu__panel button { min-height: var(--control-height-default); padding: var(--space-1) var(--space-2); border: 0; border-radius: var(--radius-control); background: transparent; color: var(--text); text-align: left; font: inherit; font-size: var(--text-size-meta); cursor: pointer; }
.account-menu__panel a:hover, .account-menu__panel a:focus-visible, .account-menu__panel button:hover, .account-menu__panel button:focus-visible { background: var(--surface-hover); }
.account-menu__panel a:focus-visible, .account-menu__panel button:focus-visible { outline: var(--focus-ring); outline-offset: -2px; }
.account-menu__identity { display: grid; gap: var(--space-1); padding: var(--space-2); border-bottom: 1px solid var(--border-subtle); }
.account-menu__identity span { overflow: hidden; color: var(--text-muted); font-size: var(--text-size-meta); text-overflow: ellipsis; }
</style>
