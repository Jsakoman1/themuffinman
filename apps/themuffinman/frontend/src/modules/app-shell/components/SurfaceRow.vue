<script setup lang="ts">
import type {ShellSurfaceRow} from "../shellSurfaceData.ts"
import type {DisplayDensity} from "../api/userShellApi.ts"
import {RouterLink} from "vue-router"

withDefaults(defineProps<{row: Omit<ShellSurfaceRow, "id"> & {id?: string}; selected?: boolean; previewed?: boolean; primaryAction?: "navigate" | "preview"; density?: DisplayDensity}>(), {primaryAction: "navigate", density: "default"})
const emit = defineEmits<{preview: []; open: []}>()
</script>

<template>
  <article class="surface-row" :data-collection-row-id="row.id" :class="[`surface-row--${density}`, {'surface-row--selected': selected, 'surface-row--previewed': previewed, 'surface-row--informational': !row.to}]" :tabindex="primaryAction === 'preview' || !row.to ? 0 : undefined" :role="primaryAction === 'preview' || !row.to ? 'button' : undefined" :aria-selected="selected ? 'true' : 'false'" @keydown.enter.prevent="primaryAction === 'preview' || !row.to ? emit('open') : undefined" @keydown.space.prevent="primaryAction === 'preview' || !row.to ? emit('open') : undefined" @keydown.p.prevent="primaryAction === 'preview' && emit('preview')">
    <RouterLink v-if="row.to && primaryAction === 'navigate'" :to="row.to" class="surface-row__primary" :aria-label="`Open ${row.title}`" @click.stop @keydown.p.prevent="emit('preview')"><img v-if="row.thumbnailUrl" class="surface-row__thumbnail" :src="row.thumbnailUrl" alt=""><div class="surface-row__main"><div class="surface-row__title-line"><strong>{{ row.title }}</strong><span v-if="row.badge" class="surface-row__badge">{{ row.badge }}</span></div><span class="surface-row__description">{{ row.description }}</span></div><span v-if="row.meta" class="surface-row__meta">{{ row.meta }}</span></RouterLink>
    <button v-else-if="primaryAction === 'preview'" type="button" class="surface-row__primary" :aria-current="selected ? 'true' : undefined" :aria-label="`Preview ${row.title}`" @click="emit('preview')"><img v-if="row.thumbnailUrl" class="surface-row__thumbnail" :src="row.thumbnailUrl" alt=""><div class="surface-row__main"><div class="surface-row__title-line"><strong>{{ row.title }}</strong><span v-if="row.badge" class="surface-row__badge">{{ row.badge }}</span></div><span class="surface-row__description">{{ row.description }}</span></div><span v-if="row.meta" class="surface-row__meta">{{ row.meta }}</span></button>
    <template v-else><img v-if="row.thumbnailUrl" class="surface-row__thumbnail" :src="row.thumbnailUrl" alt=""><div class="surface-row__main"><div class="surface-row__title-line"><strong>{{ row.title }}</strong><span v-if="row.badge" class="surface-row__badge">{{ row.badge }}</span></div><span class="surface-row__description">{{ row.description }}</span></div><span v-if="row.meta" class="surface-row__meta">{{ row.meta }}</span></template>
    <div v-if="$slots.actions || row.visionTo" class="surface-row__actions"><slot name="actions"><RouterLink v-if="row.visionTo" :to="row.visionTo" class="surface-row__vision">Ask Vision</RouterLink></slot></div>
  </article>
</template>

<style scoped>
.surface-row { width: 100%; display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: center; gap: var(--space-2); min-height: var(--dense-row-height-default); border-bottom: 1px solid var(--border-subtle); }.surface-row--compact, .surface-row--compact .surface-row__primary { min-height: var(--dense-row-height-compact); }.surface-row--comfortable, .surface-row--comfortable .surface-row__primary { min-height: var(--dense-row-height-comfortable); }.surface-row--selected { background: var(--surface-selected); box-shadow: inset 2px 0 var(--accent); }.surface-row--previewed:not(.surface-row--selected) { box-shadow: inset 2px 0 var(--accent); }.surface-row__primary { display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: center; gap: var(--space-2); min-width: 0; min-height: var(--dense-row-height-default); padding: var(--space-2) var(--space-3); }.surface-row--compact .surface-row__primary { padding-block: var(--space-1); }.surface-row--comfortable .surface-row__primary { padding-block: var(--space-3); }.surface-row button.surface-row__primary { width:100%;border:0;background:transparent;color:inherit;font:inherit;text-align:left;cursor:pointer; }.surface-row__primary:hover { background: var(--surface-hover); }
.surface-row:focus-visible, .surface-row__primary:focus-visible { outline: var(--focus-ring); outline-offset: -2px; }
.surface-row__main { display: grid; gap: var(--space-1); min-width: 0; }
.surface-row__thumbnail { width: 3rem; height: 3rem; grid-row: 1 / span 2; object-fit: cover; border-radius: var(--radius-control); border: 1px solid var(--border-subtle); background: var(--surface-muted); }
.surface-row__title-line { display: flex; align-items: center; gap: var(--space-2); min-width: 0; }
.surface-row__title-line strong { overflow: hidden; color: var(--text); font-size: var(--text-size-title); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-tight); text-overflow: ellipsis; white-space: nowrap; }
.surface-row__description { overflow: hidden; color: var(--text-muted); font-size: var(--text-size-body); line-height: 1.4; text-overflow: ellipsis; white-space: nowrap; }
.surface-row__meta { color: var(--text-soft); font-size: var(--text-size-meta); font-variant-numeric: tabular-nums; text-align: right; white-space: nowrap; }
.surface-row__badge { display: inline-flex; padding: 0 var(--space-1); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); color: var(--text-muted); font-size: var(--text-size-label); font-weight: var(--text-weight-medium); line-height: 1.5; }
.surface-row__vision { grid-column: 2; grid-row: 1 / span 2; border: 1px solid var(--border-subtle); border-radius: var(--radius-control); padding: var(--space-1) var(--space-2); color: var(--text-muted); font-size: var(--text-size-meta); font-weight: var(--text-weight-semibold); }
.surface-row--informational > .surface-row__main, .surface-row--informational > .surface-row__meta { padding-inline: var(--space-3); }
.surface-row__actions { display: flex; align-items: center; justify-content: flex-end; gap: var(--space-1); min-width: 0; padding-right: var(--space-3); }
.surface-row__actions :deep(a), .surface-row__actions :deep(button) { max-width: 100%; }
@media (max-width: 700px) { .surface-row { grid-template-columns: minmax(0, 1fr); } .surface-row__meta { text-align: left; } .surface-row__vision { grid-column: 1; grid-row: auto; justify-self: start; } .surface-row__actions { justify-content: flex-start; flex-wrap: wrap; padding: 0 var(--space-3) var(--space-2); } }
</style>
