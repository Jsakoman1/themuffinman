<script setup lang="ts">
import {currentUser} from "../../auth.ts"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import AppBrand from "./AppBrand.vue"
import {useAppTopbarState} from "./useAppTopbarState.ts"

const {
  topbarRef,
  accountMenuOpen,
  homeTarget,
  toggleAccountMenu,
  openProfile,
  openSettings,
  handleLogout
} = useAppTopbarState()

void topbarRef
</script>

<template>
  <header ref="topbarRef" class="dashboard-topbar app-topbar">
    <div class="dashboard-topbar__primary topbar__inner topbar__inner--shell">
      <div class="app-topbar__layout">
        <div class="app-topbar__brand-slot">
          <AppBrand :to="homeTarget" />
        </div>

        <div class="app-topbar__actions-slot">
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
                <button class="dashboard-account-menu__item" type="button" @click="openSettings">
                  Settings
                </button>
                <button class="dashboard-account-menu__item dashboard-account-menu__item--danger" type="button" @click="handleLogout">
                  Logout
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>
