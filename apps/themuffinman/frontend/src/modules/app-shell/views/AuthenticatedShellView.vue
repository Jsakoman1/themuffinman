<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import {RouterLink, RouterView, useRoute} from "vue-router"
import {appPersonalShortcuts, appPrimaryNavItems, appSecondaryNavItems, getAppSurfaceConfig, type AppPrimaryNavId, type AppSurfaceId} from "../shellDefinitions.ts"
import {buildSurfaceVisionPrompt, buildSurfaceVisionRoute, buildVisionRoute} from "../visionHandoff.ts"
import GlobalVisionEntry from "../components/GlobalVisionEntry.vue"
import AccountMenu from "../components/AccountMenu.vue"
import UniversalCreateMenu from "../components/UniversalCreateMenu.vue"
import GlobalSearchEntry from "../components/GlobalSearchEntry.vue"
import AppActionMenu from "../components/AppActionMenu.vue"
import {userShellApi, type PersonalShortcut} from "../api/userShellApi.ts"

const route = useRoute()

const currentSurfaceId = computed(() => route.meta.surfaceId as AppSurfaceId | undefined)

const activeNavId = computed<AppPrimaryNavId | null>(() => {
  if (!currentSurfaceId.value) {
    return null
  }

  return getAppSurfaceConfig(currentSurfaceId.value).navId
})

const secondaryNavigationActive = computed(() => appSecondaryNavItems.some(item => item.id === activeNavId.value))

const shellTitle = computed(() => {
  if (!currentSurfaceId.value) {
    return "TheMuffinMan"
  }

  return getAppSurfaceConfig(currentSurfaceId.value).title
})

const currentContextLabel = computed(() => {
  if (!currentSurfaceId.value) {
    return "Shell"
  }

  return getAppSurfaceConfig(currentSurfaceId.value).title
})

const shellEyebrow = computed(() => currentSurfaceId.value ? getAppSurfaceConfig(currentSurfaceId.value).eyebrow : "Workspace")

const contextualVisionRoute = computed(() => {
  if (!currentSurfaceId.value) {
    return buildVisionRoute()
  }

  return buildSurfaceVisionRoute(currentSurfaceId.value, route.fullPath, currentContextLabel.value)
})

const visionPlaceholder = computed(() => currentSurfaceId.value ? buildSurfaceVisionPrompt(currentSurfaceId.value) : "Ask Vision for guided help")
const pinned = ref<PersonalShortcut[]>([])
onMounted(async () => { try { pinned.value = await userShellApi.getPersonalShortcuts() } catch { pinned.value = [] } })
</script>

<template>
  <div class="app-shell">
    <aside class="app-shell__rail" aria-label="Primary navigation">
      <div class="app-shell__brand">
        <p class="app-shell__brand-mark" aria-label="TheMuffinMan">TM</p>
        <p class="app-shell__brand-copy">Workspace</p>
      </div>

      <nav class="app-shell__nav">
        <p class="app-shell__nav-heading">Core</p>
        <RouterLink
          v-for="item in appPrimaryNavItems"
          :key="item.id"
          :to="item.to"
          class="app-shell__nav-link"
          :class="{ 'app-shell__nav-link--active': activeNavId === item.id }"
        >
          <span class="app-shell__nav-icon" aria-hidden="true">{{ item.icon }}</span>
          <span class="app-shell__nav-label">{{ item.label }}</span>
        </RouterLink>
        <details class="app-shell__more" :open="secondaryNavigationActive">
          <summary class="app-shell__more-summary">More modules</summary>
          <div class="app-shell__more-items">
            <RouterLink
              v-for="item in appSecondaryNavItems"
              :key="item.id"
              :to="item.to"
              class="app-shell__nav-link app-shell__nav-link--secondary"
              :class="{ 'app-shell__nav-link--active': activeNavId === item.id }"
            >
              <span class="app-shell__nav-icon" aria-hidden="true">{{ item.icon }}</span>
              <span class="app-shell__nav-label">{{ item.label }}</span>
            </RouterLink>
          </div>
        </details>
      </nav>

      <div class="app-shell__rail-footer">
        <p class="app-shell__nav-heading">Personal</p>
        <RouterLink v-for="item in pinned" :key="`pin-${item.targetType}-${item.targetId}`" :to="item.route" class="app-shell__account-link"><span aria-hidden="true">★</span>{{ item.title }}</RouterLink>
        <RouterLink v-for="item in appPersonalShortcuts" :key="item.id" :to="item.to" class="app-shell__account-link"><span aria-hidden="true">{{ item.icon }}</span>{{ item.label }}</RouterLink>
        <p class="app-shell__nav-heading app-shell__nav-heading--account">Account</p>
        <RouterLink to="/profile" class="app-shell__account-link" :class="{ 'app-shell__account-link--active': activeNavId === 'profile' }">Profile</RouterLink>
        <RouterLink to="/profile/settings" class="app-shell__account-link">Settings</RouterLink>
      </div>
    </aside>

    <div class="app-shell__frame">
      <header class="app-shell__header">
        <div class="app-shell__context">
          <p class="app-shell__eyebrow">{{ shellEyebrow }}</p>
          <h1 class="app-shell__title">{{ shellTitle }}</h1>
        </div>

        <div class="app-shell__header-actions">
          <UniversalCreateMenu />
          <GlobalSearchEntry />
          <GlobalVisionEntry :context="currentContextLabel" :placeholder="visionPlaceholder" :contextual-route="contextualVisionRoute" />
          <AppActionMenu label="Open personal shortcuts">
            <RouterLink v-for="item in appPersonalShortcuts" :key="item.id" :to="item.to">{{ item.label }}</RouterLink>
          </AppActionMenu>
          <AccountMenu />
        </div>
      </header>

      <main class="app-shell__content">
        <RouterView />
      </main>
    </div>

    <nav class="app-shell__mobile-nav" aria-label="Mobile navigation">
      <RouterLink
        v-for="item in appPrimaryNavItems"
        :key="item.id"
        :to="item.to"
        class="app-shell__mobile-link"
        :class="{ 'app-shell__mobile-link--active': activeNavId === item.id }"
      >
        <span aria-hidden="true">{{ item.icon }}</span>
        {{ item.label }}
      </RouterLink>
      <details class="app-shell__mobile-more" :open="secondaryNavigationActive">
        <summary class="app-shell__mobile-link">More</summary>
        <div class="app-shell__mobile-more-items">
          <RouterLink
            v-for="item in appSecondaryNavItems"
            :key="item.id"
            :to="item.to"
            class="app-shell__mobile-link"
            :class="{ 'app-shell__mobile-link--active': activeNavId === item.id }"
          >
            <span aria-hidden="true">{{ item.icon }}</span>
            {{ item.label }}
          </RouterLink>
        </div>
      </details>
      <RouterLink :to="contextualVisionRoute" class="app-shell__mobile-vision">Vision</RouterLink>
    </nav>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--workspace-rail-width) minmax(0, 1fr);
  background: var(--bg);
  color: var(--text);
}

.app-shell__rail {
  display: grid;
  align-content: start;
  gap: 1.25rem;
  position: sticky;
  top: 0;
  height: 100svh;
  padding: 1rem 0.65rem 0.75rem;
  border-right: 1px solid var(--border-subtle);
  background: var(--rail);
}

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
  gap: 0.55rem;
}

.app-shell__brand-mark {
  display: grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  padding: 0;
  border: 1px solid var(--border-strong);
  border-radius: 0.6rem;
  background: var(--surface-strong);
  color: var(--text);
  font-size: 0.92rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.app-shell__brand-copy { margin: 0; color: var(--text-muted); font-size: 0.78rem; font-weight: 700; }

.app-shell__nav {
  display: grid;
  gap: 0.2rem;
}

.app-shell__nav-heading {
  padding: 0.25rem 0.7rem 0.35rem;
  color: var(--text-soft);
  font-size: 0.64rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.app-shell__nav-link,
.app-shell__vision-link {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.7rem;
  border-radius: 0.45rem;
  border: 1px solid transparent;
  color: var(--text-muted);
  transition: border-color 140ms ease, background-color 140ms ease, color 140ms ease;
}

.app-shell__nav-icon { display: inline-grid; place-items: center; width: 1rem; color: var(--text-soft); font-size: 0.9rem; }
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
  border-radius: 999px;
  background: var(--accent);
  content: "";
}

.app-shell__more {
  display: grid;
  gap: 0.2rem;
}

.app-shell__more-summary {
  cursor: pointer;
  padding: 0.5rem 0.7rem;
  border-radius: 0.45rem;
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
  gap: 0.2rem;
}

.app-shell__nav-heading--account { margin-top: 0.8rem; }

.app-shell__account-link {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.7rem;
  border-radius: 0.45rem;
  color: var(--text-muted);
}

.app-shell__account-link:hover,
.app-shell__account-link--active {
  background: var(--surface-hover);
  color: var(--text);
}

.app-shell__mobile-vision {
  background: var(--accent);
  color: #121217;
}

.app-shell__frame {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  min-width: 0;
}

.app-shell__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  min-height: 3.75rem;
  align-items: center;
  padding: 0.65rem 1.25rem;
  border-bottom: 1px solid var(--border-subtle);
  background: color-mix(in srgb, var(--bg-raised) 92%, transparent);
  backdrop-filter: blur(14px);
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
  font-size: 0.94rem;
  line-height: 1;
  letter-spacing: -0.02em;
}

.app-shell__header-actions {
  display: grid;
  grid-auto-flow: column;
  gap: 0.45rem;
  align-items: center;
}

.app-shell__vision-hint {
  margin: 0;
  max-width: 22rem;
  color:var(--text-muted);
  font-size: 0.82rem;
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
  border-radius: 999px;
  border:1px solid var(--border-subtle);
  background:var(--surface);
  padding: 0.68rem 0.95rem;
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
  padding: 1.25rem;
}

.app-shell__mobile-nav {
  display: none;
}

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
  padding: 0.45rem;
  border:1px solid var(--border-subtle);
  border-radius: 1rem;
  background:var(--surface);
  box-shadow: 0 16px 32px rgba(23, 34, 26, 0.14);
}

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: minmax(0, 1fr);
  }

  .app-shell__rail {
    display: none;
  }

  .app-shell__header,
  .app-shell__header-actions {
    align-items: stretch;
  }

  .app-shell__header {
    flex-direction: column;
    padding-bottom: 0.6rem;
  }

  .app-shell__header-actions {
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
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0.4rem;
    padding: 0.75rem 0.8rem calc(0.75rem + env(safe-area-inset-bottom, 0px));
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
    min-height: 2.8rem;
    padding: 0.5rem 0.85rem;
    border-radius: 999px;
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
</style>
