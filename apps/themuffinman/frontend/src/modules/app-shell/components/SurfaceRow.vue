<script setup lang="ts">
import type {ShellSurfaceRow} from "../shellSurfaceData.ts"
import {RouterLink} from "vue-router"
import AppCard from "./AppCard.vue"

defineProps<{row: ShellSurfaceRow}>()
</script>

<template>
  <AppCard v-if="row.to" :to="row.to" :label="row.title" class="surface-row">
    <div class="surface-row__main">
      <div class="surface-row__title-line"><strong>{{ row.title }}</strong><span v-if="row.badge" class="surface-row__badge">{{ row.badge }}</span></div>
      <span class="surface-row__description">{{ row.description }}</span>
    </div>
    <span v-if="row.meta" class="surface-row__meta">{{ row.meta }}</span>
    <template #actions><RouterLink v-if="row.visionTo" :to="row.visionTo" class="surface-row__vision">Ask Vision</RouterLink></template>
  </AppCard>
  <article v-else class="surface-row surface-row--informational">
    <div class="surface-row__main">
      <div class="surface-row__title-line"><strong>{{ row.title }}</strong><span v-if="row.badge" class="surface-row__badge">{{ row.badge }}</span></div>
      <span class="surface-row__description">{{ row.description }}</span>
    </div>
    <span v-if="row.meta" class="surface-row__meta">{{ row.meta }}</span>
    <RouterLink v-if="row.visionTo" :to="row.visionTo" class="surface-row__vision">Ask Vision</RouterLink>
  </article>
</template>

<style scoped>
.surface-row{width:100%;display:grid;grid-template-columns:minmax(0,1fr) auto;align-items:center;gap:.65rem;padding:.8rem 1rem;border-radius:.9rem}.surface-row__main{display:grid;gap:.2rem;min-width:0}.surface-row__title-line{display:flex;align-items:center;gap:.45rem;min-width:0}.surface-row__title-line strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.surface-row__description,.surface-row__meta{color:var(--text-muted);font-size:.82rem}.surface-row__meta{text-align:right}.surface-row__badge{display:inline-flex;padding:.2rem .45rem;border-radius:999px;background:var(--surface-muted);color:var(--text-muted);font-size:.7rem}.surface-row__vision{grid-column:2;grid-row:1 / span 2;border:1px solid var(--border-subtle);border-radius:999px;padding:.4rem .65rem;font-size:.75rem;font-weight:650}.surface-row--informational{border:1px solid var(--border-subtle);background:rgba(255,255,255,.45)}@media(max-width:640px){.surface-row{grid-template-columns:minmax(0,1fr)}.surface-row__meta{text-align:left}.surface-row__vision{grid-column:1;grid-row:auto;justify-self:start}}
</style>
