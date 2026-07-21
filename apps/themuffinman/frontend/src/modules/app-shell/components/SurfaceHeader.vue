<script setup lang="ts">
import {computed} from "vue"
import {RouterLink} from "vue-router"
import type {AppSurfaceConfig} from "../shellDefinitions.ts"

const props = defineProps<{config: AppSurfaceConfig; detailLabel?: string}>()
const primaryAction = computed(() => props.config.actions.find(action => action.tone === "primary") ?? props.config.actions[0])
const secondaryActions = computed(() => props.config.actions.filter(action => action !== primaryAction.value))
</script>

<template>
  <header class="surface-header" :data-action-count="config.actions.length" :aria-labelledby="`surface-title-${config.id}`">
    <div v-if="detailLabel" class="surface-header__location"><span class="surface-header__detail">{{ detailLabel }}</span></div>
    <h1 :id="`surface-title-${config.id}`" class="surface-header__title">{{ config.title }}</h1>
    <div v-if="primaryAction" class="surface-header__actions" aria-label="Primary and secondary surface actions">
      <RouterLink :to="primaryAction.to" class="surface-header__action" :class="`surface-header__action--${primaryAction.tone ?? 'secondary'}`">{{ primaryAction.label }}</RouterLink>
      <details v-if="secondaryActions.length" class="surface-header__overflow">
        <summary>More</summary>
        <div class="surface-header__overflow-menu">
          <RouterLink v-for="action in secondaryActions" :key="action.label" :to="action.to">{{ action.label }}</RouterLink>
        </div>
      </details>
    </div>
  </header>
</template>

<style scoped>
.surface-header { display: grid; grid-template-columns: minmax(0,1fr) auto; align-items: end; gap: var(--space-3) var(--space-5); padding: var(--space-2) 0; }
.surface-header__location { display: flex; align-items: center; grid-column: 1/-1; min-width: 0; }
.surface-header__title, .surface-header__detail { margin: 0; }
.surface-header__detail { overflow: hidden; color: var(--text-soft); font-size: var(--text-size-body); text-overflow: ellipsis; white-space: nowrap; }
.surface-header__title { min-width: 0; font-size: var(--text-size-page-title); letter-spacing: var(--tracking-display); line-height: 1.08; }
.surface-header__actions { display: flex; justify-content: flex-end; gap: var(--space-2); flex-wrap: wrap; }
.surface-header__overflow { position: relative; }
.surface-header__overflow summary { display: inline-flex; align-items: center; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); color: var(--text-muted); cursor: pointer; list-style: none; }
.surface-header__overflow summary::-webkit-details-marker { display: none; }
.surface-header__overflow-menu { position: absolute; z-index: var(--z-popover); top: calc(100% + var(--space-1)); right: 0; display: grid; min-width: 12rem; padding: var(--space-1); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-raised); box-shadow: var(--shadow-overlay); }
.surface-header__overflow-menu a { padding: var(--space-2); border-radius: var(--radius-control); color: var(--text-muted); }
.surface-header__overflow-menu a:hover { background: var(--surface-hover); color: var(--text); }
.surface-header__action { display: inline-flex; align-items: center; justify-content: center; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: var(--control-bg); color: var(--control-ink); font-size: var(--text-size-body); font-weight: var(--text-weight-semibold); white-space: nowrap; }
.surface-header__action--primary, .surface-header__action--vision { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
@media(max-width:760px){.surface-header{grid-template-columns:1fr;align-items:start}.surface-header__actions{justify-content:flex-start}}
</style>
