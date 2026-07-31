<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import {RouterLink} from "vue-router"

type FriendlyCardTone = "work" | "share" | "book" | "ride" | "people" | "neutral"

defineProps<{
  eyebrow?: string
  title: string
  description?: string
  tone?: FriendlyCardTone
  icon?: string
  to?: RouteLocationRaw
  actionLabel?: string
}>()
</script>

<template>
  <component :is="to ? RouterLink : 'section'" :to="to" class="friendly-summary-card" :class="[`friendly-summary-card--${tone ?? 'neutral'}`, {'friendly-summary-card--actionable': to}]">
    <span v-if="icon" class="friendly-summary-card__icon" aria-hidden="true">{{ icon }}</span>
    <span class="friendly-summary-card__copy">
      <small v-if="eyebrow">{{ eyebrow }}</small>
      <strong>{{ title }}</strong>
      <span v-if="description">{{ description }}</span>
      <slot />
    </span>
    <span v-if="actionLabel" class="friendly-summary-card__action">{{ actionLabel }} <span aria-hidden="true">→</span></span>
  </component>
</template>

<style scoped>
.friendly-summary-card{display:grid;grid-template-columns:auto minmax(0,1fr) auto;align-items:center;gap:var(--space-3);min-width:0;padding:var(--space-4);border:1px solid color-mix(in srgb,var(--friendly-card-ink) 15%,transparent);border-radius:calc(var(--radius-card) + .2rem);background:var(--friendly-card-bg,var(--surface-raised));color:var(--friendly-card-ink,var(--text));box-shadow:0 1px 0 color-mix(in srgb,var(--friendly-card-ink) 8%,transparent)}.friendly-summary-card--actionable{transition:transform var(--motion-fast) ease,box-shadow var(--motion-fast) ease;color:var(--friendly-card-ink,var(--text));text-decoration:none}.friendly-summary-card--actionable:hover{transform:translateY(-2px);box-shadow:0 .5rem 1.5rem color-mix(in srgb,var(--friendly-card-ink) 14%,transparent)}.friendly-summary-card__icon{display:grid;place-items:center;width:2.75rem;height:2.75rem;border-radius:1rem;background:color-mix(in srgb,var(--friendly-card-ink) 10%,transparent);font-size:1.35rem}.friendly-summary-card__copy{display:grid;gap:var(--space-1);min-width:0}.friendly-summary-card__copy small{font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase;opacity:.7}.friendly-summary-card__copy strong{font-size:1.05rem;line-height:1.18;letter-spacing:var(--tracking-tight)}.friendly-summary-card__copy>span{font-size:var(--text-size-meta);line-height:1.35;opacity:.84}.friendly-summary-card__action{font-size:var(--text-size-meta);font-weight:var(--text-weight-semibold);white-space:nowrap}.friendly-summary-card--work{--friendly-card-bg:var(--launcher-work-bg);--friendly-card-ink:var(--launcher-work-ink)}.friendly-summary-card--share{--friendly-card-bg:var(--launcher-share-bg);--friendly-card-ink:var(--launcher-share-ink)}.friendly-summary-card--book{--friendly-card-bg:var(--launcher-book-bg);--friendly-card-ink:var(--launcher-book-ink)}.friendly-summary-card--ride{--friendly-card-bg:var(--launcher-ride-bg);--friendly-card-ink:var(--launcher-ride-ink)}.friendly-summary-card--people{--friendly-card-bg:var(--launcher-people-bg);--friendly-card-ink:var(--launcher-people-ink)}@media(max-width:560px){.friendly-summary-card{grid-template-columns:auto minmax(0,1fr);align-items:start}.friendly-summary-card__action{grid-column:2;justify-self:start}}
</style>
