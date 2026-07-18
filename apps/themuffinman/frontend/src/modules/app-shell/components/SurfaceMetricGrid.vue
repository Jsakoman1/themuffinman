<script setup lang="ts">
import {RouterLink} from "vue-router"
import type {ShellSurfaceMetric} from "../shellSurfaceData.ts"

defineProps<{metrics: ShellSurfaceMetric[]}>()
</script>

<template>
  <div v-if="metrics.length > 0" class="surface-metric-grid" aria-label="Summary">
    <component v-for="metric in metrics" :key="metric.label" :is="metric.to ? RouterLink : 'article'" v-bind="metric.to ? {to: metric.to} : {}" class="surface-metric" :class="{'surface-metric--emphasis': metric.tone === 'emphasis'}">
      <span class="surface-metric__label">{{ metric.label }}</span><strong class="surface-metric__value">{{ metric.value }}</strong><span class="surface-metric__detail">{{ metric.detail }}</span>
    </component>
  </div>
</template>

<style scoped>
.surface-metric-grid{display:flex;flex-wrap:wrap;gap:.5rem}.surface-metric{display:inline-flex;align-items:baseline;gap:.45rem;min-height:2.45rem;border:1px solid var(--border-subtle);border-radius:999px;background:var(--surface);padding:.45rem .75rem}.surface-metric[href]{cursor:pointer;transition:transform 160ms ease,border-color 160ms ease,box-shadow 160ms ease}.surface-metric[href]:hover,.surface-metric[href]:focus-visible{border-color:var(--border-strong);box-shadow:var(--shadow-card);transform:translateY(-1px)}.surface-metric--emphasis{border-color:var(--accent);background:var(--accent);color:#121217}.surface-metric__label{color:var(--text-muted);font-size:.76rem}.surface-metric--emphasis .surface-metric__label{color:rgba(18,18,23,.72)}.surface-metric__value{font-size:1rem;letter-spacing:-.04em}.surface-metric__detail{display:none}
</style>
