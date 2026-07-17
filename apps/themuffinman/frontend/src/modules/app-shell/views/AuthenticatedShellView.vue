<script setup lang="ts">
import {computed} from "vue"
import {RouterLink, RouterView, useRoute} from "vue-router"
import {appPrimaryNavItems, appSecondaryNavItems, getAppSurfaceConfig, type AppPrimaryNavId, type AppSurfaceId} from "../shellDefinitions.ts"
import {buildSurfaceVisionPrompt, buildSurfaceVisionRoute, buildVisionRoute} from "../visionHandoff.ts"
import GlobalVisionEntry from "../components/GlobalVisionEntry.vue"
import AccountMenu from "../components/AccountMenu.vue"
import UniversalCreateMenu from "../components/UniversalCreateMenu.vue"
import GlobalSearchEntry from "../components/GlobalSearchEntry.vue"

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

const contextualVisionRoute = computed(() => {
  if (!currentSurfaceId.value) {
    return buildVisionRoute()
  }

  return buildSurfaceVisionRoute(currentSurfaceId.value, route.fullPath, currentContextLabel.value)
})

const visionPlaceholder = computed(() => currentSurfaceId.value ? buildSurfaceVisionPrompt(currentSurfaceId.value) : "Ask Vision for guided help")
</script>

<template>
  <div class="app-shell">
    <aside class="app-shell__rail" aria-label="Primary navigation">
      <div class="app-shell__brand">
        <p class="app-shell__brand-mark" aria-label="TheMuffinMan">TM</p>
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
              <span class="app-shell__nav-label">{{ item.label }}</span>
            </RouterLink>
          </div>
        </details>
      </nav>

      <div class="app-shell__rail-footer">
        <p class="app-shell__nav-heading">Account</p>
        <RouterLink to="/profile" class="app-shell__account-link" :class="{ 'app-shell__account-link--active': activeNavId === 'profile' }">Profile</RouterLink>
        <RouterLink to="/profile/settings" class="app-shell__account-link">Settings</RouterLink>
      </div>
    </aside>

    <div class="app-shell__frame">
      <header class="app-shell__header">
        <div class="app-shell__context">
          <h1 class="app-shell__title">{{ shellTitle }}</h1>
        </div>

        <div class="app-shell__header-actions">
          <UniversalCreateMenu />
          <GlobalSearchEntry />
          <GlobalVisionEntry :context="currentContextLabel" :placeholder="visionPlaceholder" :contextual-route="contextualVisionRoute" />
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
  grid-template-columns: minmax(16rem, 18.5rem) minmax(0, 1fr);
  background:
    radial-gradient(circle at top left, rgba(214, 228, 218, 0.58), transparent 26%),
    linear-gradient(180deg, #f6f7f2 0%, #eef0e9 100%);
  color: #17221a;
}

.app-shell__rail {
  display: grid;
  align-content: start;
  gap: 1.4rem;
  padding: 1.4rem 1rem 1rem;
  border-right: 1px solid rgba(23, 34, 26, 0.1);
  background: rgba(252, 252, 248, 0.86);
  backdrop-filter: blur(16px);
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
  display: grid;
  gap: 0.35rem;
}

.app-shell__brand-mark {
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: -0.04em;
}

.app-shell__nav {
  display: grid;
  gap: 0.45rem;
}

.app-shell__nav-heading {
  padding: 0 0.85rem;
  color: rgba(23, 34, 26, 0.45);
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.app-shell__nav-link,
.app-shell__vision-link {
  display: grid;
  gap: 0.22rem;
  padding: 0.8rem 0.85rem;
  border-radius: 1rem;
  border: 1px solid transparent;
  transition: border-color 160ms ease, background-color 160ms ease, transform 160ms ease;
}

.app-shell__nav-link:hover,
.app-shell__vision-link:hover,
.app-shell__header-vision:hover,
.app-shell__logout:hover {
  transform: translateY(-1px);
}

.app-shell__nav-link:hover,
.app-shell__vision-link:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(23, 34, 26, 0.1);
}

.app-shell__nav-link--active {
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(23, 34, 26, 0.14);
  box-shadow: 0 18px 36px rgba(23, 34, 26, 0.08);
}

.app-shell__more {
  display: grid;
  gap: 0.45rem;
}

.app-shell__more-summary {
  cursor: pointer;
  padding: 0.8rem 0.85rem;
  border-radius: 1rem;
  color: rgba(23, 34, 26, 0.72);
  font-weight: 600;
  list-style: none;
}

.app-shell__more-summary::-webkit-details-marker {
  display: none;
}

.app-shell__more-summary::after {
  content: "＋";
  float: right;
  color: rgba(23, 34, 26, 0.45);
}

.app-shell__more[open] .app-shell__more-summary::after {
  content: "−";
}

.app-shell__more-summary:hover {
  background: rgba(255, 255, 255, 0.8);
}

.app-shell__more-items {
  display: grid;
  gap: 0.35rem;
  padding-left: 0.55rem;
}

.app-shell__nav-link--secondary {
  padding-block: 0.65rem;
}

.app-shell__nav-label,
.app-shell__vision-label {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.app-shell__rail-footer {
  margin-top: auto;
  display: grid;
  gap: 0.45rem;
}

.app-shell__account-link {
  padding: 0.65rem 0.85rem;
  border-radius: 0.8rem;
  color: rgba(23, 34, 26, 0.72);
}

.app-shell__account-link:hover,
.app-shell__account-link--active {
  background: rgba(255, 255, 255, 0.8);
}

.app-shell__mobile-vision {
  background: rgba(23, 34, 26, 0.96);
  color: #f8f8f4;
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
  padding: 1.25rem 1.35rem 0.85rem;
}

.app-shell__context {
  display: grid;
  gap: 0.28rem;
}

.app-shell__eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.73rem;
  color: rgba(23, 34, 26, 0.45);
}

.app-shell__title {
  font-size: clamp(1.65rem, 2.2vw, 2.2rem);
  line-height: 1;
  letter-spacing: -0.06em;
}

.app-shell__header-actions {
  display: grid;
  gap: 0.7rem;
  justify-items: end;
}

.app-shell__vision-hint {
  margin: 0;
  max-width: 22rem;
  color: rgba(23, 34, 26, 0.58);
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
  border: 1px solid rgba(23, 34, 26, 0.12);
  background: rgba(255, 255, 255, 0.82);
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
  color: rgba(23, 34, 26, 0.76);
}

.app-shell__logout {
  color: inherit;
}

.app-shell__content {
  padding: 0 1.35rem 1.35rem;
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
  border: 1px solid rgba(23, 34, 26, 0.1);
  border-radius: 1rem;
  background: rgba(252, 252, 248, 0.98);
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
    display: flex;
    gap: 0.5rem;
    align-items: center;
    padding: 0.75rem 0.8rem calc(0.75rem + env(safe-area-inset-bottom, 0px));
    border-top: 1px solid rgba(23, 34, 26, 0.1);
    background: rgba(252, 252, 248, 0.94);
    backdrop-filter: blur(16px);
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .app-shell__mobile-link,
  .app-shell__mobile-vision {
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 2.8rem;
    padding: 0.5rem 0.85rem;
    border-radius: 999px;
    border: 1px solid rgba(23, 34, 26, 0.08);
    background: rgba(255, 255, 255, 0.84);
    text-align: center;
    font-size: 0.82rem;
    white-space: nowrap;
    scroll-snap-align: start;
  }

  .app-shell__mobile-link--active {
    background: rgba(23, 34, 26, 0.94);
    color: #f8f8f4;
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
