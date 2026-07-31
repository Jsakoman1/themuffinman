<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import {RouterLink, RouterView, useRoute, useRouter} from "vue-router"
import {authenticatedShellContract, getAppSurfaceConfig, type AppPrimaryNavId, type AppSurfaceId} from "../shellDefinitions.ts"
import ContextualAssistantComposer from "../components/ContextualAssistantComposer.vue"
import AccountMenu from "../components/AccountMenu.vue"
import WorkspaceModuleRail from "../components/WorkspaceModuleRail.vue"
import AppButton from "../components/AppButton.vue"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import AppPurposeIcon, {type AppPurposeIconName} from "../components/AppPurposeIcon.vue"
import NotificationDrawer from "../components/NotificationDrawer.vue"
import {userShellApi, type AttentionCenter, type PersonalShortcut} from "../api/userShellApi.ts"
import {useWorkspaceNavigation} from "../composables/useWorkspaceNavigation.ts"
import {useChatRealtime} from "../composables/useChatRealtime.ts"
import TaskSurface from "../components/TaskSurface.vue"
import {setActiveBusinessProfileId} from "../api/userShellApi.ts"
import {muffinManLogoUrl} from "../../../brand.ts"

const route = useRoute()
const router = useRouter()
const globalContext = ref<string>(typeof window === "undefined" ? "PERSONAL" : window.sessionStorage.getItem("workspaceContext") || "PERSONAL")
const businessProfiles = ref<Awaited<ReturnType<typeof userShellApi.getMyBusinessProfiles>>>([])
const ownerBusinessSurface = computed(() => ["business-profile", "business-bookings", "business-calendar", "business-service-setup"].includes(String(currentSurfaceId.value)))
const loadGlobalContexts = async () => {
  businessProfiles.value = await userShellApi.getMyBusinessProfiles().catch(() => [])
  if (ownerBusinessSurface.value && globalContext.value === "PERSONAL" && businessProfiles.value[0]) {
    globalContext.value = String(businessProfiles.value[0].id)
    window.sessionStorage.setItem("workspaceContext", globalContext.value)
    setActiveBusinessProfileId(businessProfiles.value[0].id)
  }
}

const currentSurfaceId = computed(() => route.meta.surfaceId as AppSurfaceId | undefined)
const mobilePrimary = [
  {id: "home", label: "Home", icon: "home", to: "/home", active: ["home"]},
  {id: "explore", label: "Explore", icon: "explore", to: "/work/find", active: ["work", "business", "services", "things", "rides", "circles"]},
  {id: "calendar", label: "Calendar", icon: "calendar", to: "/calendar", active: ["calendar"]},
  {id: "chat", label: "Chat", icon: "chat", to: "/chat", active: ["chat"]},
  {id: "profile", label: "Profile", icon: "profile", to: "/profile", active: ["profile"]}
] as const
const isMobilePrimaryActive = (item: typeof mobilePrimary[number]) => item.active.includes(String(activeNavId.value) as never) || item.active.includes(String(currentSurfaceId.value) as never)

const activeNavId = computed<AppPrimaryNavId | null>(() => {
  if (!currentSurfaceId.value) {
    return null
  }

  return getAppSurfaceConfig(currentSurfaceId.value).navId
})

const workspaceNavigation = useWorkspaceNavigation()
const backendNavigationReady = computed(() => {
  const response = workspaceNavigation.navigation.value
  return !workspaceNavigation.error.value && !!response && response.modules.length > 0
})

const currentContextLabel = computed(() => {
  if (!currentSurfaceId.value) {
    return "Shell"
  }

  return getAppSurfaceConfig(currentSurfaceId.value).title
})

const pinned = ref<PersonalShortcut[]>([])
const attention = ref<AttentionCenter | null>(null)
const notificationDrawerOpen = ref(false)
const personalContextError = ref(false)
const railWidthPx = ref(240)
const railResizing = ref(false)
const clampRailWidth = (width: number) => Math.min(280, Math.max(216, Math.round(width)))
const resizeRail = (event: PointerEvent) => { if (railResizing.value) railWidthPx.value = clampRailWidth(event.clientX) }
const persistRailWidth = async () => { try { railWidthPx.value = (await userShellApi.updateWorkspaceRailPreference(railWidthPx.value)).railWidthPx } catch { /* Keep current-session width; the next reload can restore the last accepted backend value. */ } }
const finishRailResize = async () => { if (!railResizing.value) return; railResizing.value = false; window.removeEventListener("pointermove", resizeRail); window.removeEventListener("pointerup", finishRailResize); try { railWidthPx.value = (await userShellApi.updateWorkspaceRailPreference(railWidthPx.value)).railWidthPx } catch { /* Keep current-session width; next reload restores backend state. */ } }
const beginRailResize = (event: PointerEvent) => { if (window.matchMedia("(max-width: 980px)").matches) return; event.preventDefault(); railResizing.value = true; window.addEventListener("pointermove", resizeRail); window.addEventListener("pointerup", finishRailResize, {once: true}) }
const resizeRailWithKeyboard = async (event: KeyboardEvent) => { if (window.matchMedia("(max-width: 980px)").matches) return; const step = event.shiftKey ? 32 : 16; let next = railWidthPx.value; if (event.key === "ArrowLeft") next -= step; else if (event.key === "ArrowRight") next += step; else if (event.key === "Home") next = 216; else if (event.key === "End") next = 280; else return; event.preventDefault(); railWidthPx.value = clampRailWidth(next); await persistRailWidth() }
const loadPersonalContext = async () => { personalContextError.value = false; const [shortcuts, attentionResult] = await Promise.allSettled([userShellApi.getPersonalShortcuts(), userShellApi.getAttentionCenter()]); pinned.value = shortcuts.status === "fulfilled" ? shortcuts.value : []; attention.value = attentionResult.status === "fulfilled" ? attentionResult.value : null; personalContextError.value = shortcuts.status === "rejected" || attentionResult.status === "rejected" }
const openNotification = async (item: NonNullable<AttentionCenter>["items"][number]) => { if (!item.readAt) { try { await userShellApi.markNewsItemAsRead(item.id); item.readAt = new Date().toISOString() } catch { return } } notificationDrawerOpen.value = false; if (item.destinationType === "APPLICATION" && item.destinationId) await router.push(`/work/applications/${item.destinationId}`); else if (item.destinationType === "QUEST" && item.destinationId) await router.push(`/work/quests/${item.destinationId}`); else await router.push("/notifications") }
const handleRealtimeEvent = (event: import("../../../contracts/index.ts").ChatSocketEventDTO) => {
  if (event.type === "news.updated") { void loadPersonalContext(); void workspaceNavigation.reload() }
}
const shellRealtime = useChatRealtime(handleRealtimeEvent)
onMounted(async () => { const preference = await userShellApi.getWorkspaceRailPreference().catch(() => null); railWidthPx.value = preference?.railWidthPx ?? railWidthPx.value; await Promise.all([loadPersonalContext(), loadGlobalContexts()]); shellRealtime.connect() })
onBeforeUnmount(() => { window.removeEventListener("pointermove", resizeRail); window.removeEventListener("pointerup", finishRailResize) })
</script>

<template>
  <div v-if="backendNavigationReady" class="app-shell" data-workspace-shell="authenticated" data-visual-language="apple-semantic-materials" data-responsive-policy="shared-shell" :data-workspace-layout="authenticatedShellContract.layout" :style="{'--workspace-rail-width': `${railWidthPx}px`}">
    <aside class="app-shell__rail" aria-label="Primary navigation">
      <div class="app-shell__workspace-group">
        <p class="app-shell__nav-heading">Workspace</p>
        <WorkspaceModuleRail :modules="workspaceNavigation.modules()" :active-module-id="activeNavId" :data-navigation-contract="workspaceNavigation.navigation.value?.contractVersion ?? 'unavailable'" />
      </div>

      <div class="app-shell__rail-footer">
        <p class="app-shell__nav-heading">Personal</p>
        <AppButton type="button" tone="quiet" class="app-shell__attention-link" @click="notificationDrawerOpen = true"><span>Updates</span><strong>{{ attention?.unreadCount ?? '—' }}</strong></AppButton>
        <p v-if="personalContextError" class="app-shell__personal-recovery">Personal context is unavailable. <AppButton type="button" tone="quiet" @click="loadPersonalContext">Retry</AppButton></p>
        <RouterLink v-for="item in pinned" :key="`pin-${item.targetType}-${item.targetId}`" :to="item.route" class="app-shell__account-link"><span aria-hidden="true">★</span>{{ item.title }}</RouterLink>
        <RouterLink to="/calendar" class="app-shell__account-link"><AppPurposeIcon name="calendar" :size="17" aria-hidden="true" />Calendar</RouterLink>
        <RouterLink to="/chat" class="app-shell__account-link"><AppPurposeIcon name="chat" :size="17" aria-hidden="true" />Chat</RouterLink>
        <div class="app-shell__account-section"><p class="app-shell__nav-heading">Account</p><AccountMenu placement="rail" /></div>
      </div>
    </aside>
      <div class="app-shell__rail-resizer" :class="{'app-shell__rail-resizer--active': railResizing}" role="separator" tabindex="0" aria-orientation="vertical" :aria-valuemin="216" :aria-valuemax="280" :aria-valuenow="railWidthPx" aria-label="Resize workspace navigation" @pointerdown="beginRailResize" @keydown="resizeRailWithKeyboard" />

    <div class="app-shell__frame">
      <!-- Post-start hardening marker: every authenticated route inherits one responsive task surface. -->
      <header class="app-shell__brand-header" :class="{'app-shell__brand-header--home': currentSurfaceId === 'home'}" aria-label="TheMuffinMan workspace header">
        <RouterLink to="/home" class="app-shell__brand-header-link" aria-label="Go to Home">
          <img :src="muffinManLogoUrl" class="app-shell__brand-logo" alt="The Muffin Man — a useful social network">
        </RouterLink>
      </header>
      <main class="app-shell__content" :class="{'app-shell__content--launcher': currentSurfaceId === 'home'}" :data-workspace-surface="currentSurfaceId ?? 'unknown'" data-responsive-task-shell data-vision-surface="persistent-bottom-composer" data-native-frame="sidebar-context-content-canvas" data-system-audit="shared-shell-no-route-chrome" data-chat-policy="two-pane-conversation-surface">
        <TaskSurface :mode="currentSurfaceId === 'home' ? 'orient' : 'workspace'" :label="currentContextLabel">
          <!-- The shell supplies context framing; child surfaces own their domain data and actions. -->
          <RouterView v-slot="{Component}"><component :is="Component" :key="route.fullPath" /></RouterView>
        </TaskSurface>
        <ContextualAssistantComposer :context="currentContextLabel" :source="`shell.surface.${currentSurfaceId ?? 'home'}`" :return-to="route.fullPath" />
      </main>
    </div>

    <nav class="app-shell__mobile-nav" aria-label="Mobile navigation">
      <RouterLink v-for="item in mobilePrimary" :key="item.id" :to="item.to" class="app-shell__mobile-link" :class="{ 'app-shell__mobile-link--active': isMobilePrimaryActive(item) }" :aria-current="isMobilePrimaryActive(item) ? 'page' : undefined"><span aria-hidden="true"><AppPurposeIcon :name="item.icon as AppPurposeIconName" :size="20" /></span><small>{{ item.label }}</small></RouterLink>
    </nav>
    <NotificationDrawer :open="notificationDrawerOpen" :items="attention?.items ?? []" @close="notificationDrawerOpen = false" @open-item="openNotification" />
  </div>
  <section v-else class="backend-shell-state" aria-live="polite" :aria-busy="workspaceNavigation.loading.value || undefined">
    <AppLoadingState v-if="workspaceNavigation.loading.value" label="Loading workspace" :rows="4" />
    <AppStatus v-else message="The workspace navigation is unavailable. Retry when the backend is online." tone="error" retry @retry="workspaceNavigation.reload" />
  </section>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--workspace-rail-width) 1px minmax(0, 1fr);
  background: var(--canvas);
  color: var(--ink);
}

.backend-shell-state { min-height: 100vh; display: grid; place-items: center; padding: var(--space-5); background: var(--canvas); color: var(--text); }
.backend-shell-state :deep(.app-status), .backend-shell-state :deep(.app-loading-state) { width: min(100%, 34rem); }

.app-shell__rail {
  display: grid;
  align-content: start;
  gap: var(--space-6);
  position: sticky;
  top: 0;
  height: 100svh;
  overflow-y: auto;
  padding: 1.1rem .75rem var(--space-3);
  border-right: 0;
  background: var(--rail);
}

.app-shell__workspace-group { display: grid; gap: var(--space-1); }

.app-shell__rail-resizer { grid-column: 2; grid-row: 1; position: sticky; top: 0; height: 100svh; cursor: col-resize; touch-action: none; }.app-shell__rail-resizer::after { display: block; width: 1px; height: 100%; margin-inline: auto; background: transparent; content: ""; }.app-shell__rail-resizer:hover::after, .app-shell__rail-resizer--active::after { background: var(--accent); }

.app-shell__rail-footer,
.app-shell__content {
  min-width: 0;
}

.app-shell__nav-label,
.app-shell__nav-heading {
  margin: 0;
}

.app-shell__nav {
  display: grid;
  gap: var(--space-1);
}

.app-shell__nav-heading {
  padding: var(--space-1) var(--space-3) var(--space-1);
  color: var(--text-soft);
  font-size: .75rem;
  font-weight: 600;
  letter-spacing: 0;
  text-transform: none;
}

.app-shell__nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-height: var(--control-height-default);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-control);
  border: 1px solid transparent;
  color: var(--text-muted);
  transition: border-color 140ms ease, background-color 140ms ease, color 140ms ease;
}

.app-shell__nav-icon { display: inline-grid; place-items: center; width: 1.15rem; color: var(--text-soft); font-size: var(--text-size-body); }
.app-shell__nav-link--active .app-shell__nav-icon { color: var(--accent); }

.app-shell__nav-link:hover {
  color: var(--text);
}

.app-shell__nav-link:hover {
  background: var(--surface-hover);
  border-color: var(--border-subtle);
}

.app-shell__nav-link--active {
  position: relative;
  background: var(--surface-strong);
  border-color: var(--border-subtle);
  color: var(--text);
}

.app-shell__nav-link--active::before {
  position: absolute;
  left: -0.2rem;
  width: 0.18rem;
  height: 1rem;
  border-radius: var(--radius-control);
  background: var(--accent);
  content: "";
}

.app-shell__nav-link--secondary {
  padding-block: 0.45rem;
}

.app-shell__nav-label {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.app-shell__rail-footer {
  margin-top: auto;
  display: grid;
  gap: .15rem;
}

.app-shell__attention-link { display: flex; align-items: center; justify-content: space-between; min-height: var(--control-height-default); padding: var(--space-1) var(--space-3); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); color: var(--text-muted); font-size: var(--text-size-body); }.app-shell__attention-link strong { color: var(--text); font-variant-numeric: tabular-nums; }.app-shell__personal-recovery { margin: 0; padding: 0 var(--space-3); color: var(--warning); font-size: var(--text-size-meta); }.app-shell__personal-recovery button { border: 0; background: transparent; color: inherit; font: inherit; text-decoration: underline; cursor: pointer; }

.app-shell__nav-heading--account { margin-top: 0.8rem; }

.app-shell__account-link {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  min-height: var(--control-height-default);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-control);
  color: var(--text-muted);
}

.app-shell__account-link:hover,
.app-shell__account-link--active {
  background: var(--surface-hover);
  color: var(--text);
}

.app-shell__frame {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-width: 0;
}

.app-shell__brand-header { position:sticky; top:0; z-index:var(--z-popover); display:flex; align-items:center; gap:var(--space-3); min-height:5.75rem; padding:var(--space-2) var(--workspace-content-gutter); border-bottom:1px solid var(--line-subtle); background:linear-gradient(100deg,var(--header-sand-start),var(--header-sand-middle) 55%,var(--header-sand-start)); box-shadow:0 1px 0 rgba(98,70,47,.08); }
.app-shell__brand-header-link { display:inline-flex; align-items:center; min-width:0; }
.app-shell__brand-logo { display:block; width:clamp(13rem,21vw,17rem); height:5rem; object-fit:contain; object-position:center; }
.app-shell__brand-header--home { min-height:9.5rem; justify-content:center; }
.app-shell__brand-header--home .app-shell__brand-logo { width:clamp(23rem,40vw,34rem); height:8rem; }

.app-shell__content {
  --persistent-vision-dock-clearance: calc(5.5rem + env(safe-area-inset-bottom, 0px));
  min-height: 0;
  width: 100%;
  padding: var(--space-3) var(--workspace-content-gutter) var(--persistent-vision-dock-clearance);
  scroll-padding-bottom: var(--persistent-vision-dock-clearance);
}

.app-shell__content > * {
  width: 100%;
  margin-inline: 0;
}

@media (min-width: 1800px) {
  .app-shell__content { padding-inline: max(var(--workspace-content-gutter), 4vw); }
}
.app-shell__content--launcher { display: grid; align-content: start; justify-items: center; }

.app-shell__mobile-nav {
  display: none;
}

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: minmax(0, 1fr);
  }

  .app-shell__rail {
    display: none;
  }

  .app-shell__rail-resizer { display: none; }

  .app-shell__mobile-nav {
    position: sticky;
    bottom: 0;
    z-index: calc(var(--z-popover) + 1);
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: var(--space-1);
    padding: var(--space-2) var(--space-3) calc(var(--space-2) + env(safe-area-inset-bottom, 0px));
    border-top: 1px solid var(--border-subtle);
    background:var(--surface);
    backdrop-filter: blur(16px);
    background: color-mix(in srgb, var(--bg-raised) 94%, transparent);
  }

  .app-shell__mobile-link {
    display: grid;
    align-items: center;
    justify-content: center;
    min-height: 3.35rem;
    padding: var(--space-1);
    border: 0;
    border-radius: var(--radius-control);
    min-width: 0;
    border: 1px solid var(--border-subtle);
    background: var(--surface);
    text-align: center;
    font-size: 0.72rem;
    overflow: hidden;
    text-overflow: ellipsis;
    gap: 0;
  }

  .app-shell__mobile-link > span[aria-hidden="true"] { display:grid; place-items:center; color:currentColor; line-height:1; }
  .app-shell__mobile-link small { font: inherit; }
  .app-shell__mobile-link--active {
    background: var(--surface-strong);
    color: var(--text);
  }

  .app-shell__content {
    --persistent-vision-dock-clearance: calc(10.25rem + env(safe-area-inset-bottom, 0px));
  }

  .app-shell__brand-header { min-height:4.5rem; padding-inline:var(--space-3); }
  .app-shell__brand-logo { width:12.5rem; height:4rem; }
  .app-shell__brand-header--home { min-height:7.5rem; }
  .app-shell__brand-header--home .app-shell__brand-logo { width:min(23rem,88vw); height:6rem; }
}

/* The shell is the shared workspace frame: controls never introduce a pill/card dialect. */
.app-shell__nav-link,.app-shell__mobile-link{border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink)}

/* One quiet macOS-like toolbar: navigation context stays here; business selection belongs to the sidebar. */
.app-shell__rail { background:var(--rail-canvas); }
.app-shell__attention-link { border-color:transparent; }
.app-shell__attention-link:hover { background:var(--surface-hover); }

.app-shell__personal-recovery .app-button { min-height: auto; margin-left: var(--space-1); padding: 0; border: 0; background: transparent; color: inherit; text-decoration: underline; }
.app-shell__mobile-nav .app-shell__mobile-link.app-button { width: 100%; }
.app-shell { background: var(--canvas); }
.app-shell__rail { background: var(--rail-canvas); }
.app-shell__frame { background: var(--canvas); }
.app-shell__rail-resizer:focus-visible { outline: var(--focus-ring); outline-offset: -2px; }
</style>
