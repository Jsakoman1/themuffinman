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
.account-menu{position:relative}.account-menu__summary{display:flex;align-items:center;gap:.5rem;cursor:pointer;list-style:none}.account-menu__summary::-webkit-details-marker{display:none}.account-menu__avatar{width:2rem;height:2rem;border-radius:50%;object-fit:cover;border:1px solid rgba(23,34,26,.14)}.account-menu__avatar--fallback{display:grid;place-items:center;background:rgba(214,228,218,.8);color:#17221a;font-size:.8rem;font-weight:700}.account-menu__username{max-width:12rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.88rem}.account-menu__panel{position:absolute;right:0;top:calc(100% + .55rem);z-index:20;display:grid;gap:.25rem;min-width:13rem;padding:.55rem;border:1px solid rgba(23,34,26,.12);border-radius:1rem;background:#fcfcf8;box-shadow:0 18px 38px rgba(23,34,26,.16)}.account-menu__panel a,.account-menu__panel button{padding:.6rem .7rem;border:0;border-radius:.65rem;background:transparent;color:inherit;text-align:left;font:inherit;font-size:.82rem}.account-menu__panel a:hover,.account-menu__panel button:hover{background:rgba(214,228,218,.55)}.account-menu__identity{display:grid;gap:.15rem;padding:.55rem .7rem .7rem;border-bottom:1px solid rgba(23,34,26,.1)}.account-menu__identity span{overflow:hidden;color:rgba(23,34,26,.56);font-size:.74rem;text-overflow:ellipsis}
</style>
