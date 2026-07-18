<script setup lang="ts">
import {RouterLink} from "vue-router"
import type {AppSurfaceConfig} from "../shellDefinitions.ts"

defineProps<{config: AppSurfaceConfig; detailLabel?: string}>()
</script>

<template>
  <header class="surface-header" :aria-labelledby="`surface-title-${config.id}`">
    <div class="surface-header__location"><span class="surface-header__location-mark" aria-hidden="true"></span><p class="surface-header__eyebrow">{{ config.eyebrow }}</p><span v-if="detailLabel" class="surface-header__detail">{{ detailLabel }}</span></div>
    <h1 :id="`surface-title-${config.id}`" class="surface-header__title">{{ config.title }}</h1>
    <div v-if="config.actions.length > 0" class="surface-header__actions" aria-label="Surface actions">
      <RouterLink v-for="action in config.actions" :key="action.label" :to="action.to" class="surface-header__action" :class="`surface-header__action--${action.tone ?? 'secondary'}`">{{ action.label }}</RouterLink>
    </div>
  </header>
</template>

<style scoped>
.surface-header { display: grid; grid-template-columns: minmax(0,1fr) auto; align-items: end; gap: var(--space-2) var(--space-4); padding: var(--space-1) 0; }
.surface-header__location { display: flex; align-items: center; grid-column: 1/-1; gap: var(--space-2); min-width: 0; }
.surface-header__location-mark { width: .45rem; height: .45rem; border-radius: 50%; background: var(--accent); }
.surface-header__eyebrow, .surface-header__title, .surface-header__detail { margin: 0; }
.surface-header__eyebrow { color: var(--text-soft); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }
.surface-header__detail { overflow: hidden; color: var(--text-soft); font-size: var(--text-size-body); text-overflow: ellipsis; white-space: nowrap; }
.surface-header__title { min-width: 0; font-size: clamp(1.55rem,2.5vw,2.3rem); letter-spacing: var(--tracking-display); line-height: 1; }
.surface-header__actions { display: flex; justify-content: flex-end; gap: var(--space-2); flex-wrap: wrap; }
.surface-header__action { display: inline-flex; align-items: center; justify-content: center; min-height: var(--control-height-default); border: 1px solid var(--control-border); border-radius: var(--radius-control); padding: var(--space-1) var(--space-3); background: var(--control-bg); color: var(--control-ink); font-size: var(--text-size-body); font-weight: var(--text-weight-semibold); white-space: nowrap; }
.surface-header__action--primary, .surface-header__action--vision { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
@media(max-width:760px){.surface-header{grid-template-columns:1fr;align-items:start}.surface-header__actions{justify-content:flex-start}}
</style>
