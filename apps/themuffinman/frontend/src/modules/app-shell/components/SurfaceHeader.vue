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
.surface-header{display:grid;grid-template-columns:minmax(0,1fr) auto;align-items:end;gap:.75rem 1rem;padding:.35rem .1rem}.surface-header__location{display:flex;align-items:center;grid-column:1/-1;gap:.45rem;min-width:0}.surface-header__location-mark{width:.45rem;height:.45rem;border-radius:50%;background:var(--accent,#17221a)}.surface-header__eyebrow,.surface-header__title,.surface-header__detail{margin:0}.surface-header__eyebrow{color:var(--text-soft);font-size:.76rem;font-weight:650;letter-spacing:.08em;text-transform:uppercase}.surface-header__detail{overflow:hidden;color:var(--text-soft);font-size:.84rem;text-overflow:ellipsis;white-space:nowrap}.surface-header__title{min-width:0;font-size:clamp(1.55rem,2.5vw,2.3rem);letter-spacing:-.075em;line-height:1}.surface-header__actions{display:flex;justify-content:flex-end;gap:.45rem;flex-wrap:wrap}.surface-header__action{display:inline-flex;align-items:center;justify-content:center;min-height:2.25rem;border:1px solid var(--border-subtle);border-radius:999px;padding:.45rem .78rem;font-size:.82rem;font-weight:650;white-space:nowrap}.surface-header__action--primary,.surface-header__action--vision{border-color:#17221a;background:#17221a;color:#f8f8f4}@media(max-width:760px){.surface-header{grid-template-columns:1fr;align-items:start}.surface-header__actions{justify-content:flex-start}}
</style>
