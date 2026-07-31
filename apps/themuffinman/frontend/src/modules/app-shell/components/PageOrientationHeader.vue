<script setup lang="ts">
import {computed} from "vue"
import {RouterLink, type RouteLocationRaw} from "vue-router"

export type PageOrientationTone = "work" | "things" | "business" | "services" | "rides" | "people" | "neutral"
export type PageOrientationAction = {label: string; to: RouteLocationRaw; tone?: "primary" | "secondary" | "vision"}

const props = withDefaults(defineProps<{
  eyebrow: string
  title: string
  description?: string
  detailLabel?: string
  tone?: PageOrientationTone
  primaryAction?: PageOrientationAction
  secondaryActions?: PageOrientationAction[]
}>(), {tone: "neutral", secondaryActions: () => []})

const headerId = computed(() => `page-orientation-${props.title.toLowerCase().replace(/[^a-z0-9]+/g, "-")}`)
</script>

<template>
  <header class="page-orientation-header" :class="`page-orientation-header--${props.tone}`" :aria-labelledby="headerId">
    <p v-if="props.detailLabel" class="page-orientation-header__detail">{{ props.detailLabel }}</p>
    <div class="page-orientation-header__copy">
      <p class="page-orientation-header__eyebrow">{{ props.eyebrow }}</p>
      <h1 :id="headerId" class="page-orientation-header__title">{{ props.title }}</h1>
      <p v-if="props.description" class="page-orientation-header__description">{{ props.description }}</p>
    </div>
    <div v-if="props.primaryAction || props.secondaryActions.length || $slots.utility || $slots.actions" class="page-orientation-header__actions" aria-label="Page actions">
      <slot name="utility" />
      <RouterLink v-if="props.primaryAction" :to="props.primaryAction.to" class="page-orientation-header__action page-orientation-header__action--primary">{{ props.primaryAction.label }}</RouterLink>
      <slot name="actions" />
      <details v-if="props.secondaryActions.length" class="page-orientation-header__overflow">
        <summary>More</summary>
        <div class="page-orientation-header__overflow-menu">
          <RouterLink v-for="action in props.secondaryActions" :key="action.label" :to="action.to">{{ action.label }}</RouterLink>
        </div>
      </details>
    </div>
  </header>
</template>

<style scoped>
.page-orientation-header { --orientation-accent: var(--orientation-line); --orientation-action: var(--accent); --orientation-action-ink: var(--canvas); display:grid; grid-template-columns:minmax(0,1fr) auto; align-items:end; gap:var(--space-3) var(--space-5); padding:var(--space-3) 0; }
.page-orientation-header--work { --orientation-accent:var(--launcher-work-ink); --orientation-action:var(--launcher-work-ink); --orientation-action-ink:var(--launcher-work-bg); }
.page-orientation-header--things { --orientation-accent:var(--launcher-share-ink); --orientation-action:var(--launcher-share-ink); --orientation-action-ink:var(--launcher-share-bg); }
.page-orientation-header--business { --orientation-accent:var(--launcher-business-ink); --orientation-action:var(--launcher-business-ink); --orientation-action-ink:var(--launcher-business-bg); }
.page-orientation-header--services { --orientation-accent:var(--launcher-services-ink); --orientation-action:var(--launcher-services-ink); --orientation-action-ink:var(--launcher-services-bg); }
.page-orientation-header--rides { --orientation-accent:var(--launcher-ride-ink); --orientation-action:var(--launcher-ride-ink); --orientation-action-ink:var(--launcher-ride-bg); }
.page-orientation-header--people { --orientation-accent:var(--launcher-people-ink); --orientation-action:var(--launcher-people-ink); --orientation-action-ink:var(--launcher-people-bg); }
.page-orientation-header__detail { grid-column:1/-1; min-width:0; margin:0; overflow:hidden; color:var(--text-soft); font-size:var(--text-size-body); text-overflow:ellipsis; white-space:nowrap; }
.page-orientation-header__copy { min-width:0; padding-left:var(--space-3); border-left:3px solid var(--orientation-accent); }
.page-orientation-header__eyebrow,.page-orientation-header__title,.page-orientation-header__description { margin:0; }
.page-orientation-header__eyebrow { margin-bottom:var(--space-1); color:var(--orientation-accent); font-size:var(--text-size-label); font-weight:var(--text-weight-semibold); letter-spacing:var(--tracking-label); line-height:1.2; text-transform:uppercase; }
.page-orientation-header__title { min-width:0; font-size:var(--text-size-page-title); letter-spacing:var(--tracking-display); line-height:1.08; }
.page-orientation-header__description { max-width:42rem; margin-top:var(--space-1); color:var(--text-muted); font-size:var(--text-size-body); line-height:var(--text-leading-body); }
.page-orientation-header__actions { display:flex; align-items:center; justify-content:flex-end; gap:var(--space-2); flex-wrap:wrap; }
.page-orientation-header__action,.page-orientation-header__overflow summary { display:inline-flex; align-items:center; justify-content:center; min-height:var(--control-height-default); border:1px solid var(--control-border); border-radius:var(--radius-control); padding:var(--space-1) var(--space-3); font-size:var(--text-size-body); font-weight:var(--text-weight-semibold); white-space:nowrap; }
.page-orientation-header__action--primary { border-color:var(--orientation-action); background:var(--orientation-action); color:var(--orientation-action-ink); }
.page-orientation-header__overflow { position:relative; }.page-orientation-header__overflow summary { color:var(--text-muted); cursor:pointer; list-style:none; }.page-orientation-header__overflow summary::-webkit-details-marker { display:none; }
.page-orientation-header__overflow-menu { position:absolute; z-index:var(--z-popover); top:calc(100% + var(--space-1)); right:0; display:grid; min-width:12rem; padding:var(--space-1); border:1px solid var(--border-subtle); border-radius:var(--radius-control); background:var(--surface-raised); box-shadow:var(--shadow-overlay); }.page-orientation-header__overflow-menu a { padding:var(--space-2); border-radius:var(--radius-control); color:var(--text-muted); }.page-orientation-header__overflow-menu a:hover { background:var(--surface-hover); color:var(--text); }
@media(max-width:760px){.page-orientation-header{grid-template-columns:1fr;align-items:start;gap:var(--space-3)}.page-orientation-header__actions{justify-content:flex-start}.page-orientation-header__action--primary{width:100%;min-height:var(--control-height-touch)}}
</style>
