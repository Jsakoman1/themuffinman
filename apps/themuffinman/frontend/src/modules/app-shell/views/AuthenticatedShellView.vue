<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue"
import {RouterLink, RouterView, useRoute} from "vue-router"
import {appPersonalShortcuts, authenticatedShellContract, getAppSurfaceConfig, type AppPrimaryNavId, type AppSurfaceId} from "../shellDefinitions.ts"
import {buildSurfaceVisionPrompt, buildSurfaceVisionRoute, buildVisionRoute} from "../visionHandoff.ts"
import GlobalVisionEntry from "../components/GlobalVisionEntry.vue"
import AccountMenu from "../components/AccountMenu.vue"
import UniversalCreateMenu from "../components/UniversalCreateMenu.vue"
import GlobalSearchEntry from "../components/GlobalSearchEntry.vue"
import WorkspaceModuleRail from "../components/WorkspaceModuleRail.vue"
import AppButton from "../components/AppButton.vue"
import {userShellApi, type AttentionCenter, type PersonalShortcut} from "../api/userShellApi.ts"
import {useWorkspaceNavigation} from "../composables/useWorkspaceNavigation.ts"
import {useChatRealtime} from "../composables/useChatRealtime.ts"

const route = useRoute()

const currentSurfaceId = computed(() => route.meta.surfaceId as AppSurfaceId | undefined)

const activeNavId = computed<AppPrimaryNavId | null>(() => {
  if (!currentSurfaceId.value) {
    return null
  }

  return getAppSurfaceConfig(currentSurfaceId.value).navId
})

const workspaceNavigation = useWorkspaceNavigation()

const currentContextLabel = computed(() => {
  if (!currentSurfaceId.value) {
    return "Shell"
  }

  return getAppSurfaceConfig(currentSurfaceId.value).title
})

const contextualVisionRoute = computed(() => {
  if (!currentSurfaceId.value) {
    return buildVisionRoute()
  }

  return buildSurfaceVisionRoute(currentSurfaceId.value, route.fullPath, currentContextLabel.value)
})

const visionPlaceholder = computed(() => currentSurfaceId.value ? buildSurfaceVisionPrompt(currentSurfaceId.value) : "Ask Vision for guided help")
const pinned = ref<PersonalShortcut[]>([])
const attention = ref<AttentionCenter | null>(null)
const personalContextError = ref(false)
const railWidthPx = ref(240)
const railResizing = ref(false)
const mobileDrawerOpen = ref(false)
const mobileDrawerTrigger = ref<{focus: () => void} | null>(null)
const mobileDrawer = ref<HTMLElement | null>(null)
const openMobileDrawer = async () => { mobileDrawerOpen.value = true; await nextTick(); mobileDrawer.value?.focus() }
const closeMobileDrawer = async () => { mobileDrawerOpen.value = false; await nextTick(); mobileDrawerTrigger.value?.focus() }
watch(() => route.fullPath, () => { mobileDrawerOpen.value = false })
const clampRailWidth = (width: number) => Math.min(280, Math.max(216, Math.round(width)))
const resizeRail = (event: PointerEvent) => { if (railResizing.value) railWidthPx.value = clampRailWidth(event.clientX) }
const persistRailWidth = async () => { try { railWidthPx.value = (await userShellApi.updateWorkspaceRailPreference(railWidthPx.value)).railWidthPx } catch { /* Keep current-session width; the next reload can restore the last accepted backend value. */ } }
const finishRailResize = async () => { if (!railResizing.value) return; railResizing.value = false; window.removeEventListener("pointermove", resizeRail); window.removeEventListener("pointerup", finishRailResize); try { railWidthPx.value = (await userShellApi.updateWorkspaceRailPreference(railWidthPx.value)).railWidthPx } catch { /* Keep current-session width; next reload restores backend state. */ } }
const beginRailResize = (event: PointerEvent) => { if (window.matchMedia("(max-width: 980px)").matches) return; event.preventDefault(); railResizing.value = true; window.addEventListener("pointermove", resizeRail); window.addEventListener("pointerup", finishRailResize, {once: true}) }
const resizeRailWithKeyboard = async (event: KeyboardEvent) => { if (window.matchMedia("(max-width: 980px)").matches) return; const step = event.shiftKey ? 32 : 16; let next = railWidthPx.value; if (event.key === "ArrowLeft") next -= step; else if (event.key === "ArrowRight") next += step; else if (event.key === "Home") next = 216; else if (event.key === "End") next = 280; else return; event.preventDefault(); railWidthPx.value = clampRailWidth(next); await persistRailWidth() }
const loadPersonalContext = async () => { personalContextError.value = false; const [shortcuts, attentionResult] = await Promise.allSettled([userShellApi.getPersonalShortcuts(), userShellApi.getAttentionCenter()]); pinned.value = shortcuts.status === "fulfilled" ? shortcuts.value : []; attention.value = attentionResult.status === "fulfilled" ? attentionResult.value : null; personalContextError.value = shortcuts.status === "rejected" || attentionResult.status === "rejected" }
const handleRealtimeEvent = (event: import("../../../contracts/index.ts").ChatSocketEventDTO) => {
  if (event.type === "news.updated") { void loadPersonalContext(); void workspaceNavigation.reload() }
}
const shellRealtime = useChatRealtime(handleRealtimeEvent)
onMounted(async () => { const preference = await userShellApi.getWorkspaceRailPreference().catch(() => null); railWidthPx.value = preference?.railWidthPx ?? railWidthPx.value; await loadPersonalContext(); shellRealtime.connect() })
onBeforeUnmount(() => { window.removeEventListener("pointermove", resizeRail); window.removeEventListener("pointerup", finishRailResize) })
</script>

<template>
  <div class="app-shell" data-workspace-shell="authenticated" data-responsive-policy="shared-shell" :data-workspace-layout="authenticatedShellContract.layout" :style="{'--workspace-rail-width': `${railWidthPx}px`}">
    <aside class="app-shell__rail" aria-label="Primary navigation">
      <div class="app-shell__brand">
        <p class="app-shell__brand-mark" aria-label="TheMuffinMan">TM</p>
        <p class="app-shell__brand-copy">Workspace</p>
      </div>

      <WorkspaceModuleRail :modules="workspaceNavigation.modules()" :active-module-id="activeNavId" :data-navigation-contract="workspaceNavigation.navigation.value?.contractVersion ?? 'unavailable'" />

      <div class="app-shell__rail-footer">
        <p class="app-shell__nav-heading">Personal</p>
        <RouterLink to="/notifications" class="app-shell__attention-link"><span>Attention</span><strong>{{ attention?.unreadCount ?? '—' }}</strong></RouterLink>
        <p v-if="personalContextError" class="app-shell__personal-recovery">Personal context is unavailable. <AppButton type="button" tone="quiet" @click="loadPersonalContext">Retry</AppButton></p>
        <RouterLink v-for="item in pinned" :key="`pin-${item.targetType}-${item.targetId}`" :to="item.route" class="app-shell__account-link"><span aria-hidden="true">★</span>{{ item.title }}</RouterLink>
        <RouterLink v-for="item in appPersonalShortcuts" :key="item.id" :to="item.to" class="app-shell__account-link"><span aria-hidden="true">{{ item.icon }}</span>{{ item.label }}</RouterLink>
        <p class="app-shell__nav-heading app-shell__nav-heading--account">Account</p>
        <RouterLink to="/profile" class="app-shell__account-link" :class="{ 'app-shell__account-link--active': activeNavId === 'profile' }">Profile</RouterLink>
        <RouterLink to="/profile/settings" class="app-shell__account-link">Settings</RouterLink>
      </div>
    </aside>
      <div class="app-shell__rail-resizer" :class="{'app-shell__rail-resizer--active': railResizing}" role="separator" tabindex="0" aria-orientation="vertical" :aria-valuemin="216" :aria-valuemax="280" :aria-valuenow="railWidthPx" aria-label="Resize workspace navigation" @pointerdown="beginRailResize" @keydown="resizeRailWithKeyboard" />

    <div class="app-shell__frame">
      <header class="app-shell__header" aria-label="Workspace context">
        <div class="app-shell__header-actions">
          <UniversalCreateMenu />
          <GlobalSearchEntry />
          <GlobalVisionEntry :context="currentContextLabel" :placeholder="visionPlaceholder" :contextual-route="contextualVisionRoute" />
          <AccountMenu />
        </div>
      </header>

      <main class="app-shell__content" :data-workspace-surface="currentSurfaceId ?? 'unknown'">
        <RouterView />
      </main>
    </div>

    <nav class="app-shell__mobile-nav" aria-label="Mobile navigation">
      <RouterLink v-for="module in workspaceNavigation.modules().slice(0, 3)" :key="module.id" :to="module.route" class="app-shell__mobile-link" :class="{ 'app-shell__mobile-link--active': activeNavId === module.id }"><span aria-hidden="true">{{ module.iconKey }}</span>{{ module.label }}</RouterLink>
      <AppButton ref="mobileDrawerTrigger" type="button" tone="secondary" class="app-shell__mobile-link" :aria-expanded="mobileDrawerOpen" aria-controls="mobile-workspace-drawer" @click="openMobileDrawer">Menu</AppButton>
      <RouterLink :to="contextualVisionRoute" class="app-shell__mobile-vision">Vision</RouterLink>
    </nav>
    <Teleport to="body">
      <div v-if="mobileDrawerOpen" class="app-shell__drawer-backdrop" @click.self="closeMobileDrawer">
        <aside id="mobile-workspace-drawer" ref="mobileDrawer" class="app-shell__drawer" aria-label="Workspace navigation" tabindex="-1" @keydown.esc.prevent="closeMobileDrawer">
          <header class="app-shell__drawer-header"><span>Workspace</span><AppButton type="button" tone="quiet" aria-label="Close navigation" @click="closeMobileDrawer">×</AppButton></header>
          <WorkspaceModuleRail :modules="workspaceNavigation.modules()" :active-module-id="activeNavId" show-children @click="closeMobileDrawer" />
          <nav class="app-shell__drawer-nav" aria-label="Personal"><p class="app-shell__nav-heading">Personal</p><RouterLink v-for="item in pinned" :key="`drawer-pin-${item.targetType}-${item.targetId}`" :to="item.route" class="app-shell__account-link" @click="closeMobileDrawer">★ {{ item.title }}</RouterLink><RouterLink v-for="item in appPersonalShortcuts" :key="item.id" :to="item.to" class="app-shell__account-link" @click="closeMobileDrawer">{{ item.label }}</RouterLink><RouterLink to="/profile" class="app-shell__account-link" @click="closeMobileDrawer">Profile</RouterLink><RouterLink to="/profile/settings" class="app-shell__account-link" @click="closeMobileDrawer">Settings</RouterLink></nav>
        </aside>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--workspace-rail-width) 6px minmax(0, 1fr);
  background: var(--bg);
  color: var(--text);
}

.app-shell__rail {
  display: grid;
  align-content: start;
  gap: var(--space-5);
  position: sticky;
  top: 0;
  height: 100svh;
  padding: var(--space-4) var(--space-2) var(--space-3);
  border-right: 1px solid var(--border-subtle);
  background: var(--rail);
}

.app-shell__rail-resizer { grid-column: 2; grid-row: 1; position: sticky; top: 0; height: 100svh; cursor: col-resize; touch-action: none; }.app-shell__rail-resizer::after { display: block; width: 1px; height: 100%; margin-inline: auto; background: transparent; content: ""; }.app-shell__rail-resizer:hover::after, .app-shell__rail-resizer--active::after { background: var(--accent); }

.app-shell__brand,
.app-shell__rail-footer,
.app-shell__header,
.app-shell__content {
  min-width: 0;
}

.app-shell__brand-mark,
.app-shell__title,
.app-shell__nav-label,
.app-shell__vision-label,
.app-shell__account-label,
.app-shell__nav-heading {
  margin: 0;
}

.app-shell__brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.app-shell__brand-mark {
  display: grid;
  place-items: center;
  width: var(--control-height-default);
  height: var(--control-height-default);
  padding: 0;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-control);
  background: var(--surface-strong);
  color: var(--text);
  font-size: 0.92rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.app-shell__brand-copy { margin: 0; color: var(--text-muted); font-size: var(--text-size-meta); font-weight: 700; }

.app-shell__nav {
  display: grid;
  gap: var(--space-1);
}

.app-shell__nav-heading {
  padding: var(--space-1) var(--space-3) var(--space-1);
  color: var(--text-soft);
  font-size: var(--text-size-label);
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.app-shell__nav-link,
.app-shell__vision-link {
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

.app-shell__nav-link:hover,
.app-shell__vision-link:hover,
.app-shell__header-vision:hover,
.app-shell__logout:hover {
  color: var(--text);
}

.app-shell__nav-link:hover,
.app-shell__vision-link:hover {
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

.app-shell__more {
  display: grid;
  gap: var(--space-1);
}

.app-shell__more-summary {
  cursor: pointer;
  min-height: var(--control-height-default);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-control);
  color: var(--text-muted);
  font-weight: 600;
  list-style: none;
}

.app-shell__more-summary::-webkit-details-marker {
  display: none;
}

.app-shell__more-summary::after {
  content: "＋";
  float: right;
  color: var(--text-soft);
}

.app-shell__more[open] .app-shell__more-summary::after {
  content: "−";
}

.app-shell__more-summary:hover {
  background: var(--surface-hover);
}

.app-shell__more-items {
  display: grid;
  gap: 0.15rem;
  padding-left: 0.3rem;
}

.app-shell__nav-link--secondary {
  padding-block: 0.45rem;
}

.app-shell__nav-label,
.app-shell__vision-label {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.app-shell__rail-footer {
  margin-top: auto;
  display: grid;
  gap: var(--space-1);
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

.app-shell__mobile-vision {
  background: var(--accent);
  color: var(--canvas);
}

.app-shell__frame {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-width: 0;
}

.app-shell__header {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: flex-start;
  min-height: var(--workspace-header-height);
  align-items: center;
  padding: var(--space-2) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  background: var(--canvas-raised);
}

.app-shell__context {
  display: grid;
  gap: 0.28rem;
}

.app-shell__eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.73rem;
  color:var(--text-muted);
}

.app-shell__title {
  font-size: var(--text-size-title);
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.app-shell__header-actions {
  display: grid;
  grid-auto-flow: column;
  gap: var(--space-2);
  align-items: center;
}

.app-shell__vision-hint {
  margin: 0;
  max-width: 22rem;
  color:var(--text-muted);
  font-size: var(--text-size-meta);
  line-height: 1.35;
  text-align: right;
}

.app-shell__vision-form {
  display: grid;
  grid-template-columns: minmax(14rem, 22rem) auto;
  gap: 0.55rem;
}

.app-shell__vision-input,
.app-shell__header-vision,
.app-shell__logout {
  border-radius: var(--radius-control);
  border: 1px solid var(--control-border);
  background: var(--control-bg);
  color: var(--control-ink);
  padding: var(--space-2) var(--space-3);
}

.app-shell__vision-input {
  min-width: 0;
}

.app-shell__account {
  display: grid;
  justify-items: end;
  gap: 0.32rem;
}

.app-shell__account-label {
  font-size: 0.9rem;
  color:var(--text-muted);
}

.app-shell__logout {
  color: inherit;
}

.app-shell__content {
  min-height: 0;
  width: 100%;
  padding: var(--space-6) var(--workspace-content-gutter);
}

.app-shell__content > * {
  width: min(100%, var(--workspace-content-max-width));
  margin-inline: auto;
}

@media (min-width: 1800px) {
  .app-shell__content { padding-inline: max(var(--workspace-content-gutter), 4vw); }
}

.app-shell__mobile-nav {
  display: none;
}

.app-shell__drawer-backdrop { display: none; }

.app-shell__mobile-more {
  position: relative;
  flex: 0 0 auto;
}

.app-shell__mobile-more summary {
  list-style: none;
}

.app-shell__mobile-more summary::-webkit-details-marker {
  display: none;
}

.app-shell__mobile-more-items {
  position: absolute;
  right: 0;
  bottom: calc(100% + 0.45rem);
  display: grid;
  gap: 0.4rem;
  min-width: 9rem;
  padding: var(--space-1);
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-surface);
  background: var(--surface-raised);
  box-shadow: var(--shadow-overlay);
}

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: minmax(0, 1fr);
  }

  .app-shell__rail {
    display: none;
  }

  .app-shell__rail-resizer { display: none; }

  .app-shell__header,
  .app-shell__header-actions {
    align-items: stretch;
    min-width: 0;
  }

  .app-shell__header {
    flex-direction: column;
    padding-bottom: 0.6rem;
  }

  .app-shell__header-actions {
    grid-auto-flow: row;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    width: 100%;
    justify-items: stretch;
  }

  .app-shell__vision-form {
    grid-template-columns: minmax(0, 1fr);
  }

  .app-shell__account {
    justify-items: stretch;
  }

  .app-shell__mobile-nav {
    position: sticky;
    bottom: 0;
    z-index: 10;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: var(--space-2);
    padding: var(--space-3) var(--space-3) calc(var(--space-3) + env(safe-area-inset-bottom, 0px));
    border-top: 1px solid var(--border-subtle);
    background:var(--surface);
    backdrop-filter: blur(16px);
    background: color-mix(in srgb, var(--bg-raised) 94%, transparent);
  }

.app-shell__mobile-link,
  .app-shell__mobile-vision {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: var(--control-height-default);
    padding: var(--space-1) var(--space-3);
    border-radius: var(--radius-control);
    min-width: 0;
    border: 1px solid var(--border-subtle);
    background: var(--surface);
    text-align: center;
    font-size: 0.82rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    gap: 0.32rem;
  }

  .app-shell__drawer-backdrop { position: fixed; inset: 0; z-index: var(--z-drawer); display: grid; justify-items: start; background: rgba(0, 0, 0, .58); }
  .app-shell__drawer { width: min(20rem, 88vw); height: 100%; display: grid; align-content: start; gap: var(--space-4); padding: var(--space-3); border-right: 1px solid var(--border-strong); background: var(--rail-canvas); box-shadow: var(--shadow-overlay); outline: none; overflow-y: auto; }
  .app-shell__drawer-header { display: flex; align-items: center; justify-content: space-between; color: var(--text); font-size: var(--text-size-title); font-weight: var(--text-weight-semibold); }.app-shell__drawer-header button { width: var(--control-height-default); height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); background: transparent; color: var(--text-muted); font-size: 1.25rem; cursor: pointer; }.app-shell__drawer-nav { display: grid; gap: var(--space-1); }

  .app-shell__mobile-link--active {
    background: var(--surface-strong);
    color: var(--text);
  }

  .app-shell__content {
    padding-bottom: 5.6rem;
  }
}

@media (max-width: 640px) {
  .app-shell__vision-hint {
    text-align: left;
    max-width: none;
  }
}

/* The shell is the shared workspace frame: controls never introduce a pill/card dialect. */
.app-shell__account-trigger,.app-shell__nav-link,.app-shell__mobile-link,.app-shell__mobile-vision{border-radius:var(--radius-control);background:var(--control-bg);color:var(--control-ink)}
.app-shell__vision-entry{color:var(--canvas)}
</style>
<style scoped>
.app-shell__personal-recovery .app-button { min-height: auto; margin-left: var(--space-1); padding: 0; border: 0; background: transparent; color: inherit; text-decoration: underline; }
.app-shell__mobile-nav .app-shell__mobile-link.app-button { width: 100%; }
.app-shell__drawer-header .app-button { width: var(--control-height-default); min-height: var(--control-height-default); padding: 0; font-size: 1.25rem; }
</style>
<style scoped>
/* Keep shell controls aligned with the shared app-like contract. */
.app-shell__vision-input,
.app-shell__header-vision,
.app-shell__logout { border-radius: var(--radius-control); background: var(--control-bg); color: var(--control-ink); }
.app-shell__mobile-more-items { box-shadow: var(--shadow-overlay); background: var(--surface-raised); }
.app-shell { background: var(--canvas); }
.app-shell__rail { background: var(--rail-canvas); }
.app-shell__frame { background: var(--surface-base); }
.app-shell__header { background: color-mix(in srgb, var(--surface-base) 94%, transparent); border-bottom: 1px solid var(--border-subtle); }
.app-shell__mobile-vision { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
.app-shell__rail-resizer:focus-visible { outline: var(--focus-ring); outline-offset: -2px; }
</style>
