<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import {RouterLink} from "vue-router"
import AppPurposeIcon, {type AppPurposeIconName} from "./AppPurposeIcon.vue"

type HomeFollowUpTone = "attention" | "schedule"

defineProps<{
  eyebrow: string
  title: string
  description: string
  actionLabel: string
  icon: AppPurposeIconName
  to: RouteLocationRaw
  tone: HomeFollowUpTone
}>()
</script>

<template>
  <RouterLink :to="to" class="home-follow-up-card ui-focusable" :class="`home-follow-up-card--${tone}`">
    <span class="home-follow-up-card__icon" aria-hidden="true"><AppPurposeIcon :name="icon" :size="23" /></span>
    <span class="home-follow-up-card__copy">
      <small>{{ eyebrow }}</small>
      <strong>{{ title }}</strong>
      <span>{{ description }}</span>
    </span>
    <span class="home-follow-up-card__action"><span>{{ actionLabel }}</span><span aria-hidden="true">→</span></span>
  </RouterLink>
</template>

<style scoped>
.home-follow-up-card{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:var(--space-3);min-height:7.25rem;padding:var(--space-4);border:1px solid color-mix(in srgb,var(--follow-up-ink) 16%,transparent);border-radius:calc(var(--radius-card) + .45rem);background:var(--follow-up-bg);color:var(--follow-up-ink);text-decoration:none;box-shadow:0 1px 0 color-mix(in srgb,var(--follow-up-ink) 10%,transparent);transition:transform var(--motion-fast) ease,box-shadow var(--motion-fast) ease}.home-follow-up-card:hover{transform:translateY(-2px);box-shadow:0 .5rem 1.5rem color-mix(in srgb,var(--follow-up-ink) 14%,transparent)}.home-follow-up-card:active{transform:translateY(0)}.home-follow-up-card__icon{display:grid;place-items:center;width:2.9rem;height:2.9rem;border-radius:1rem;background:color-mix(in srgb,var(--follow-up-ink) 11%,transparent)}.home-follow-up-card__copy{display:grid;gap:var(--space-1);min-width:0}.home-follow-up-card__copy small{font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase;opacity:.72}.home-follow-up-card__copy strong{font-size:1.08rem;line-height:1.15;letter-spacing:var(--tracking-tight)}.home-follow-up-card__copy>span{font-size:var(--text-size-meta);line-height:1.3;opacity:.84}.home-follow-up-card__action{display:flex;align-items:center;gap:var(--space-1);font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);white-space:nowrap}.home-follow-up-card--attention{--follow-up-bg:var(--launcher-people-bg);--follow-up-ink:var(--launcher-people-ink)}.home-follow-up-card--schedule{--follow-up-bg:var(--launcher-share-bg);--follow-up-ink:var(--launcher-share-ink)}@media(max-width:480px){.home-follow-up-card{grid-template-columns:auto minmax(0,1fr);align-items:start;min-height:0}.home-follow-up-card__action{grid-column:2;justify-self:start}.home-follow-up-card__icon{width:2.6rem;height:2.6rem}}
</style>
