<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import {currentUser, isAdmin, logoutUser} from "../../auth.ts"
import type {QuestNewsItem} from "../../modules/workmarket/api/workmarketApi.ts"
import {productModules} from "../../modules/moduleRegistry.ts"
import {workmarketApi} from "../../modules/workmarket/api/workmarketApi.ts"
import {routeForNavigationTarget} from "../../modules/workmarket/shared/navigationTargets.ts"
import AppNotificationsPanel from "./AppNotificationsPanel.vue"

const topbarRef = ref<HTMLElement | null>(null)
const accountMenuOpen = ref(false)
const notificationsOpen = ref(false)
const unreadNewsCount = ref(0)
const notificationItems = ref<QuestNewsItem[]>([])
const isLoadingNotifications = ref(false)
const notificationsError = ref("")
const router = useRouter()
const route = useRoute()

const homeTarget = computed(() => {
  if (!currentUser.value) {
    return "/login"
  }

  return isAdmin() ? "/admin/work" : "/work"
})

const isAuthenticated = computed(() => currentUser.value !== null)

const isModuleActive = (path: string) => {
  if (path === "/work") {
    return route.path.startsWith("/work")
      || route.path.startsWith("/quests")
      || route.path.startsWith("/applications")
      || route.path.startsWith("/users")
      || route.path.startsWith("/admin")
  }

  return route.path.startsWith(path)
}

const closeMenus = () => {
  accountMenuOpen.value = false
  notificationsOpen.value = false
}

const refreshUnreadNewsCount = async () => {
  if (!currentUser.value) {
    unreadNewsCount.value = 0
    return
  }

  try {
    const summary = await workmarketApi.getDashboardSummary()
    unreadNewsCount.value = summary.unreadNewsCount
  } catch {
    unreadNewsCount.value = 0
  }
}

const loadNotifications = async () => {
  if (!currentUser.value) {
    notificationItems.value = []
    notificationsError.value = ""
    return
  }

  isLoadingNotifications.value = true
  notificationsError.value = ""

  try {
    notificationItems.value = await workmarketApi.getMyNews()
  } catch {
    notificationsError.value = "Could not load notifications."
    notificationItems.value = []
  } finally {
    isLoadingNotifications.value = false
  }
}

const toggleAccountMenu = () => {
  notificationsOpen.value = false
  accountMenuOpen.value = !accountMenuOpen.value
}

const toggleNotifications = async () => {
  accountMenuOpen.value = false

  if (notificationsOpen.value) {
    notificationsOpen.value = false
    return
  }

  notificationsOpen.value = true
  await loadNotifications()
  await refreshUnreadNewsCount()
}

const openProfile = async () => {
  closeMenus()
  if (!currentUser.value) {
    return
  }

  await router.push(`/users/${currentUser.value.id}`)
}

const markAllNotificationsRead = async () => {
  await workmarketApi.markMyNewsAsRead()
  unreadNewsCount.value = 0
  notificationItems.value = notificationItems.value.map((item) => ({
    ...item,
    readAt: item.readAt ?? new Date().toISOString()
  }))
}

const openNotificationItem = async (item: QuestNewsItem) => {
  await workmarketApi.markMyNewsItemAsRead(item.id)
  unreadNewsCount.value = Math.max(0, unreadNewsCount.value - (item.readAt === null ? 1 : 0))
  notificationItems.value = notificationItems.value.map((candidate) => candidate.id === item.id
    ? {...candidate, readAt: candidate.readAt ?? new Date().toISOString()}
    : candidate
  )
  notificationsOpen.value = false
  await router.push(routeForNavigationTarget(item.navigation))
}

const handleLogout = async () => {
  closeMenus()
  logoutUser()
  await router.push("/login")
}

const handleDocumentClick = (event: MouseEvent) => {
  const target = event.target as Node | null
  if (topbarRef.value && target && !topbarRef.value.contains(target)) {
    closeMenus()
  }
}

watch(() => route.fullPath, () => {
  closeMenus()
})

watch(() => currentUser.value?.id, async (userId) => {
  closeMenus()
  notificationItems.value = []
  notificationsError.value = ""

  if (!userId) {
    unreadNewsCount.value = 0
    return
  }

  await refreshUnreadNewsCount()
})

onMounted(() => {
  document.addEventListener("click", handleDocumentClick)
  void refreshUnreadNewsCount()
})

onBeforeUnmount(() => {
  document.removeEventListener("click", handleDocumentClick)
})
</script>

<template>
  <header ref="topbarRef" class="dashboard-topbar app-topbar">
    <div class="dashboard-topbar__primary topbar__inner topbar__inner--shell">
      <RouterLink class="brand brand--logo brand--interactive" :to="homeTarget">
        <div class="brand__mark" aria-hidden="true">
          <span></span>
          <span></span>
        </div>
        <div class="brand__stack">
          <div class="brand__eyebrow">One App</div>
          <div class="brand__title">TheMuffinMan</div>
        </div>
      </RouterLink>

      <template v-if="isAuthenticated">
        <nav class="module-switcher" aria-label="Product modules">
          <RouterLink
            v-for="module in productModules"
            :key="module.key"
            :to="module.path"
            class="module-pill"
            :class="{
              'module-pill--planned': module.state === 'planned',
              'module-pill--active': isModuleActive(module.path)
            }"
          >
            <span>{{ module.shortTitle }}</span>
            <small>{{ module.state === "live" ? "Live" : "Planned" }}</small>
          </RouterLink>
        </nav>

        <div class="dashboard-topbar__user-shell">
          <RouterLink class="nav-link nav-link--utility" to="/circles">Circles</RouterLink>

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

          <button
            class="dashboard-topbar__user"
            :class="{ 'dashboard-topbar__user--active': accountMenuOpen }"
            type="button"
            @click="toggleAccountMenu"
          >
            <span class="account-chip">
              {{ currentUser?.username }}
              <small>{{ currentUser?.role }}</small>
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
      </template>

      <nav v-else class="nav">
        <RouterLink class="nav-link" to="/login">Login</RouterLink>
        <RouterLink class="nav-link" to="/register">Register</RouterLink>
      </nav>
    </div>
  </header>
</template>
