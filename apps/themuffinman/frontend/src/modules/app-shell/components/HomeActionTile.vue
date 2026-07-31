<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import {RouterLink} from "vue-router"
import AppPurposeIcon, {type AppPurposeIconName} from "./AppPurposeIcon.vue"

type HomeActionTone = "work" | "share" | "book" | "ride" | "people"

defineProps<{
  label: string
  description?: string
  icon: AppPurposeIconName
  to: RouteLocationRaw
  tone: HomeActionTone
}>()
</script>

<template>
  <RouterLink :to="to" class="home-action-tile ui-focusable" :class="`home-action-tile--${tone}`" :aria-label="description ? `${label}. ${description}` : label">
    <span class="home-action-tile__icon" aria-hidden="true"><AppPurposeIcon :name="icon" :size="30" /></span>
    <span class="home-action-tile__copy"><strong>{{ label }}</strong><span v-if="description">{{ description }}</span></span>
  </RouterLink>
</template>

<style scoped>
.home-action-tile{display:grid;align-content:space-between;gap:var(--space-3);min-height:10.25rem;padding:var(--space-4);border:0;border-radius:calc(var(--radius-card) + .55rem);background:var(--tile-bg);color:var(--tile-ink);text-decoration:none;box-shadow:0 1px 0 color-mix(in srgb,var(--tile-ink) 10%,transparent);transition:transform var(--motion-fast) ease,box-shadow var(--motion-fast) ease}.home-action-tile:hover{transform:translateY(-2px);box-shadow:0 .5rem 1.5rem color-mix(in srgb,var(--tile-ink) 16%,transparent)}.home-action-tile:active{transform:translateY(0)}.home-action-tile__icon{display:grid;place-items:center;width:3.15rem;height:3.15rem;border-radius:1.05rem;background:color-mix(in srgb,var(--tile-ink) 10%,transparent);line-height:1}.home-action-tile__copy{display:grid;gap:var(--space-1)}.home-action-tile__copy strong{font-size:1.1rem;letter-spacing:var(--tracking-tight);line-height:1.15}.home-action-tile__copy span{font-size:var(--text-size-meta);line-height:1.25;opacity:.82}.home-action-tile--work{--tile-bg:var(--launcher-work-bg);--tile-ink:var(--launcher-work-ink)}.home-action-tile--share{--tile-bg:var(--launcher-share-bg);--tile-ink:var(--launcher-share-ink)}.home-action-tile--book{--tile-bg:var(--launcher-book-bg);--tile-ink:var(--launcher-book-ink)}.home-action-tile--ride{--tile-bg:var(--launcher-ride-bg);--tile-ink:var(--launcher-ride-ink)}.home-action-tile--people{--tile-bg:var(--launcher-people-bg);--tile-ink:var(--launcher-people-ink)}@media(max-width:420px){.home-action-tile{min-height:9rem;padding:var(--space-3)}.home-action-tile__icon{width:2.6rem;height:2.6rem}}
</style>
