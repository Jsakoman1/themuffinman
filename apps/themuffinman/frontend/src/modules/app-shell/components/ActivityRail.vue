<script setup lang="ts">
import {formatDateTime} from "../../../services/formatters.ts"

export type ActivityRailItem = {
  kind: string
  title: string
  summary?: string
  occurredAt: string
  route?: string
}

withDefaults(defineProps<{
  items: ActivityRailItem[]
  title?: string
  emptyLabel?: string
}>(), {
  title: "Activity",
  emptyLabel: "No activity yet.",
})

const formatOccurredAt = (value: string) => formatDateTime(value, "Unknown time")
</script>

<template>
  <section class="activity-rail" :aria-label="title">
    <header><h2>{{ title }}</h2></header>
    <ol v-if="items.length" class="activity-rail__list">
      <li v-for="item in items" :key="`${item.kind}-${item.occurredAt}-${item.title}`" class="activity-rail__item">
        <RouterLink v-if="item.route" :to="item.route" class="activity-rail__link">
          <strong>{{ item.title }}</strong><span v-if="item.summary">{{ item.summary }}</span><time :datetime="item.occurredAt">{{ formatOccurredAt(item.occurredAt) }}</time>
        </RouterLink>
        <div v-else class="activity-rail__content"><strong>{{ item.title }}</strong><span v-if="item.summary">{{ item.summary }}</span><time :datetime="item.occurredAt">{{ formatOccurredAt(item.occurredAt) }}</time></div>
      </li>
    </ol>
    <p v-else class="activity-rail__empty">{{ emptyLabel }}</p>
  </section>
</template>

<style scoped>
.activity-rail { min-width: 0; }.activity-rail header { padding: var(--space-3); border-bottom: 1px solid var(--border-subtle); }.activity-rail h2 { margin: 0; color: var(--text-muted); font-size: var(--text-size-label); font-weight: var(--text-weight-semibold); letter-spacing: var(--tracking-label); text-transform: uppercase; }.activity-rail__list { display: grid; margin: 0; padding: 0; list-style: none; }.activity-rail__item + .activity-rail__item { border-top: 1px solid var(--border-subtle); }.activity-rail__link, .activity-rail__content { display: grid; gap: var(--space-1); padding: var(--space-3); color: var(--text-muted); font-size: var(--text-size-meta); }.activity-rail__link:hover { background: var(--surface-hover); }.activity-rail strong { color: var(--text); font-size: var(--text-size-body); font-weight: var(--text-weight-medium); }.activity-rail time { color: var(--text-soft); }.activity-rail__empty { margin: 0; padding: var(--space-3); color: var(--text-soft); font-size: var(--text-size-meta); }
</style>
