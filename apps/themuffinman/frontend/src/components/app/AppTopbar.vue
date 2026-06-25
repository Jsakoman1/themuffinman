<script setup lang="ts">
import {currentUser} from "../../auth.ts"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import AppBrand from "./AppBrand.vue"
import AppModuleNav from "./AppModuleNav.vue"
import AppNotificationsPanel from "./AppNotificationsPanel.vue"
import {useAppTopbarState} from "./useAppTopbarState.ts"

const {
  topbarRef,
  accountMenuOpen,
  notificationsOpen,
  unreadNewsCount,
  notificationItems,
  isLoadingNotifications,
  notificationsError,
  homeTarget,
  isAuthenticated,
  topbarNavItems,
  toggleAccountMenu,
  toggleNotifications,
  openProfile,
  markAllNotificationsRead,
  openNotificationItem,
  handleLogout
} = useAppTopbarState()

void topbarRef
</script>

<template>
  <header ref="topbarRef" class="dashboard-topbar app-topbar">
    <div class="dashboard-topbar__primary topbar__inner topbar__inner--shell">
      <template v-if="isAuthenticated">
        <div class="app-topbar__layout">
          <div class="app-topbar__brand-slot">
            <AppBrand :to="homeTarget" />
          </div>

          <div class="app-topbar__nav-slot">
            <AppModuleNav :items="topbarNavItems" />
          </div>

          <div class="app-topbar__actions-slot">
            <div class="dashboard-topbar__user-shell dashboard-topbar__user-shell--notifications">
              <button class="dashboard-topbar__utility" type="button" aria-label="Open notifications" @click="toggleNotifications">
                <span class="dashboard-topbar__utility-icon" aria-hidden="true">✦</span>
                <span class="dashboard-topbar__utility-label">Notifications</span>
                <span v-if="unreadNewsCount > 0" class="badge badge--accent dashboard-nav__badge">
                  {{ unreadNewsCount }}
                </span>
              </button>

              <Transition name="sheet-fade">
                <div v-if="notificationsOpen" class="dashboard-notifications-popover">
                  <AppNotificationsPanel
                    :items="notificationItems"
                    :unread-count="unreadNewsCount"
                    :error="notificationsError"
                    :is-loading="isLoadingNotifications"
                    @open-item="openNotificationItem"
                    @mark-all-read="markAllNotificationsRead"
                  />
                </div>
              </Transition>
            </div>

            <div class="dashboard-topbar__user-shell dashboard-topbar__user-shell--account">
              <button
                class="dashboard-topbar__user"
                :class="{ 'dashboard-topbar__user--active': accountMenuOpen }"
                type="button"
                @click="toggleAccountMenu"
              >
                <ProfileAvatar
                  class="dashboard-topbar__avatar"
                  :username="currentUser?.username"
                  :avatar-data-url="currentUser?.profileAvatarDataUrl"
                  :size="34"
                />
                <span class="account-chip">
                  {{ currentUser?.username }}
                </span>
                <span class="dashboard-topbar__chevron" aria-hidden="true">⌄</span>
              </button>

              <Transition name="sheet-fade">
                <div v-if="accountMenuOpen" class="dashboard-account-menu__panel dashboard-account-menu__panel--topbar">
                  <button class="dashboard-account-menu__item" type="button" @click="openProfile">
                    Profile
                  </button>
                  <button class="dashboard-account-menu__item dashboard-account-menu__item--danger" type="button" @click="handleLogout">
                    Logout
                  </button>
                </div>
              </Transition>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <AppBrand :to="homeTarget" />

        <nav class="nav">
          <RouterLink class="nav-link" to="/login">Login</RouterLink>
          <RouterLink class="nav-link" to="/register">Register</RouterLink>
        </nav>
      </template>
    </div>
  </header>
</template>
