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
.surface-metric-grid { display: flex; flex-wrap: wrap; gap: var(--space-2); }
.surface-metric { display: inline-flex; align-items: baseline; gap: var(--space-2); min-height: var(--control-height-default); border: 1px solid var(--border-subtle); border-radius: var(--radius-control); background: var(--surface-base); padding: var(--space-1) var(--space-2); }
.surface-metric[href] { cursor: pointer; }
.surface-metric[href]:hover, .surface-metric[href]:focus-visible { border-color: var(--border-strong); }
.surface-metric--emphasis { border-color: var(--accent); background: var(--accent); color: var(--canvas); }
.surface-metric__label { color: var(--text-muted); font-size: var(--text-size-meta); }
.surface-metric--emphasis .surface-metric__label { color: color-mix(in srgb, var(--canvas) 70%, transparent); }
.surface-metric__value { font-size: var(--text-size-body); letter-spacing: var(--tracking-tight); }
.surface-metric__detail { display: none; }
</style>
