<script setup lang="ts">
import {computed} from "vue"
import {RouterLink} from "vue-router"
import type {AppSurfaceConfig} from "../shellDefinitions.ts"
import type {ShellSurfaceSection, ShellSurfaceMetric} from "../shellSurfaceData.ts"

const props = defineProps<{
  config: AppSurfaceConfig
  metrics: ShellSurfaceMetric[]
  sections: ShellSurfaceSection[]
  loading: boolean
  error: string
  detailLabel?: string
  note?: string
}>()

const surfaceClass = computed(() => `surface-content--${props.config.archetype}`)
</script>

<template>
  <section class="surface-content" :class="surfaceClass">
    <header class="surface-content__header">
      <div class="surface-content__location">
        <span class="surface-content__location-mark" aria-hidden="true"></span>
        <p class="surface-content__eyebrow">{{ config.eyebrow }}</p>
        <span v-if="detailLabel" class="surface-content__detail-label">{{ detailLabel }}</span>
      </div>
      <h1 class="surface-content__title">{{ config.title }}</h1>
      <div v-if="config.actions.length > 0" class="surface-content__actions" aria-label="Surface actions">
        <RouterLink
          v-for="action in config.actions.slice(0, 2)"
          :key="action.label"
          :to="action.to"
          class="surface-content__action"
          :class="`surface-content__action--${action.tone ?? 'secondary'}`"
        >
          {{ action.label }}
        </RouterLink>
      </div>
    </header>

    <div v-if="metrics.length > 0" class="surface-content__metrics" aria-label="Summary">
      <article
        v-for="metric in metrics"
        :key="metric.label"
        class="surface-content__metric"
        :class="{ 'surface-content__metric--emphasis': metric.tone === 'emphasis' }"
      >
        <span class="surface-content__metric-label">{{ metric.label }}</span>
        <strong class="surface-content__metric-value">{{ metric.value }}</strong>
      </article>
    </div>

    <div v-if="loading" class="surface-content__status-card" role="status">Loading.</div>
    <div v-else-if="error" class="surface-content__status-card surface-content__status-card--error" role="alert">
      {{ error }}
    </div>

    <div v-if="config.archetype === 'chat'" class="surface-content__inbox-layout">
      <aside class="surface-content__inbox-index" aria-label="Conversation list">
        <h2 class="surface-content__card-title">{{ sections[sections.length > 1 ? 1 : 0]?.title ?? "Inbox" }}</h2>
        <div class="surface-content__rows">
          <div v-for="row in sections[sections.length > 1 ? 1 : 0]?.rows ?? []" :key="row.id" class="surface-content__inbox-row">
            <RouterLink v-if="row.to" :to="row.to" class="surface-content__row-title">{{ row.title }}</RouterLink>
            <span v-else class="surface-content__row-title">{{ row.title }}</span>
            <span v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</span>
          </div>
        </div>
      </aside>
      <section class="surface-content__inbox-focus" aria-label="Selected conversation">
        <h2 class="surface-content__card-title">{{ sections[0]?.title ?? "Conversation" }}</h2>
        <div class="surface-content__rows">
          <div v-for="row in sections[0]?.rows ?? []" :key="row.id" class="surface-content__focus-row">
            <span class="surface-content__row-title">{{ row.title }}</span>
            <span v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</span>
          </div>
        </div>
        <p v-if="sections.length === 0" class="surface-content__empty-state">Select a conversation.</p>
      </section>
    </div>

    <div v-else-if="config.archetype === 'calendar'" class="surface-content__timeline" aria-label="Calendar timeline">
      <section v-for="section in sections" :key="section.title" class="surface-content__timeline-group">
        <h2 class="surface-content__card-title">{{ section.title }}</h2>
        <div class="surface-content__rows">
          <div v-for="row in section.rows" :key="row.id" class="surface-content__timeline-row">
            <span class="surface-content__timeline-dot" aria-hidden="true"></span>
            <div class="surface-content__row-main">
              <span class="surface-content__row-title">{{ row.title }}</span>
              <span v-if="row.badge" class="surface-content__badge">{{ row.badge }}</span>
            </div>
            <span v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</span>
            <RouterLink v-if="row.to" :to="row.to" class="surface-content__row-link">Open</RouterLink>
          </div>
        </div>
      </section>
    </div>

    <div v-else-if="config.archetype === 'business'" class="surface-content__operations" aria-label="Business operations">
      <section v-for="section in sections" :key="section.title" class="surface-content__operations-group">
        <div class="surface-content__operations-heading">
          <h2 class="surface-content__card-title">{{ section.title }}</h2>
          <span>{{ section.rows.length }}</span>
        </div>
        <div class="surface-content__rows">
          <div v-for="row in section.rows" :key="row.id" class="surface-content__row">
            <div class="surface-content__row-main">
              <span class="surface-content__row-title">{{ row.title }}</span>
              <span v-if="row.badge" class="surface-content__badge">{{ row.badge }}</span>
            </div>
            <span v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</span>
            <RouterLink v-if="row.to" :to="row.to" class="surface-content__row-link">Open</RouterLink>
          </div>
        </div>
      </section>
    </div>

    <div v-else class="surface-content__sections">
      <article
        v-for="section in sections"
        :key="section.title"
        class="surface-content__card"
      >
        <h2 class="surface-content__card-title">{{ section.title }}</h2>

        <div v-if="section.rows.length > 0" class="surface-content__rows">
          <div v-for="row in section.rows" :key="row.id" class="surface-content__row">
            <div class="surface-content__row-main">
              <span class="surface-content__row-title">{{ row.title }}</span>
              <span v-if="row.badge" class="surface-content__badge">{{ row.badge }}</span>
            </div>
            <span v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</span>
            <div v-if="row.to || row.visionTo" class="surface-content__row-actions">
              <RouterLink v-if="row.to" :to="row.to" class="surface-content__row-link">Open</RouterLink>
              <RouterLink v-if="row.visionTo" :to="row.visionTo" class="surface-content__row-link surface-content__row-link--vision">Ask</RouterLink>
            </div>
          </div>
        </div>
        <p v-else class="surface-content__empty-state">{{ section.emptyState }}</p>
      </article>
    </div>

    <p v-if="!loading && !error && sections.length === 0" class="surface-content__empty-surface">
      Nothing here yet.
    </p>
  </section>
</template>

<style scoped>
.surface-content {
  display: grid;
  gap: var(--space-4);
  min-width: 0;
}

.surface-content__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 0.75rem 1rem;
  padding: 0.35rem 0.1rem;
}

.surface-content__location {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
  grid-column: 1 / -1;
}

.surface-content__location-mark {
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 50%;
  background: var(--accent, #17221a);
}

.surface-content__eyebrow,
.surface-content__title,
.surface-content__detail-label,
.surface-content__metric-label,
.surface-content__metric-value,
.surface-content__card-title,
.surface-content__row-title,
.surface-content__row-meta,
.surface-content__empty-state,
.surface-content__empty-surface {
  margin: 0;
}

.surface-content__eyebrow {
  color: rgba(23, 34, 26, 0.55);
  font-size: 0.76rem;
  font-weight: 650;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.surface-content__detail-label {
  color: rgba(23, 34, 26, 0.55);
  font-size: 0.84rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.surface-content__title {
  min-width: 0;
  font-size: clamp(1.55rem, 2.5vw, 2.3rem);
  letter-spacing: -0.075em;
  line-height: 1;
}

.surface-content__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.45rem;
}

.surface-content__action,
.surface-content__row-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2.25rem;
  border: 1px solid rgba(23, 34, 26, 0.11);
  border-radius: 999px;
  padding: 0.45rem 0.78rem;
  font-size: 0.82rem;
  font-weight: 650;
  white-space: nowrap;
}

.surface-content__action--primary,
.surface-content__action--vision,
.surface-content__row-link--vision {
  border-color: #17221a;
  background: #17221a;
  color: #f8f8f4;
}

.surface-content__metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.surface-content__metric {
  display: inline-flex;
  align-items: baseline;
  gap: 0.45rem;
  min-height: 2.45rem;
  border: 1px solid rgba(23, 34, 26, 0.08);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.66);
  padding: 0.45rem 0.75rem;
}

.surface-content__metric--emphasis {
  border-color: #17221a;
  background: #17221a;
  color: #f8f8f4;
}

.surface-content__metric-label {
  color: rgba(23, 34, 26, 0.58);
  font-size: 0.76rem;
}

.surface-content__metric--emphasis .surface-content__metric-label {
  color: rgba(248, 248, 244, 0.7);
}

.surface-content__metric-value {
  font-size: 1rem;
  letter-spacing: -0.04em;
}

.surface-content__status-card {
  border-radius: 0.85rem;
  background: rgba(255, 255, 255, 0.62);
  padding: 0.7rem 0.85rem;
  color: rgba(23, 34, 26, 0.62);
  font-size: 0.86rem;
}

.surface-content__status-card--error {
  background: rgba(255, 245, 241, 0.92);
  color: #7c2a1d;
}

.surface-content__sections {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.7rem;
}

.surface-content__inbox-layout {
  display: grid;
  grid-template-columns: minmax(14rem, 0.8fr) minmax(0, 1.6fr);
  gap: 0.7rem;
}

.surface-content__inbox-index,
.surface-content__inbox-focus,
.surface-content__timeline-group,
.surface-content__operations-group {
  min-width: 0;
  border: 1px solid rgba(23, 34, 26, 0.08);
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.62);
  padding: 0.85rem;
}

.surface-content__inbox-row,
.surface-content__focus-row {
  display: grid;
  gap: 0.2rem;
  border-top: 1px solid rgba(23, 34, 26, 0.07);
  padding: 0.7rem 0;
}

.surface-content__inbox-row:first-child,
.surface-content__focus-row:first-child {
  border-top: none;
}

.surface-content__timeline,
.surface-content__operations {
  display: grid;
  gap: 0.7rem;
}

.surface-content__timeline-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 0.65rem;
  border-top: 1px solid rgba(23, 34, 26, 0.07);
  padding: 0.75rem 0;
}

.surface-content__timeline-row:first-child {
  border-top: none;
}

.surface-content__timeline-dot {
  width: 0.48rem;
  height: 0.48rem;
  border-radius: 50%;
  background: #1d5c49;
}

.surface-content__operations-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.surface-content__operations-heading > span {
  color: rgba(23, 34, 26, 0.5);
  font-size: 0.76rem;
}

.surface-content--chat .surface-content__sections,
.surface-content--calendar .surface-content__sections,
.surface-content--business .surface-content__sections {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.surface-content--work .surface-content__sections {
  grid-template-columns: minmax(0, 1fr);
}

.surface-content__card {
  min-width: 0;
  border: 1px solid rgba(23, 34, 26, 0.08);
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.62);
  padding: 0.85rem;
}

.surface-content__card-title {
  font-size: 0.83rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.surface-content__rows {
  display: grid;
  gap: 0;
  margin-top: 0.45rem;
}

.surface-content__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 0.55rem;
  min-width: 0;
  border-top: 1px solid rgba(23, 34, 26, 0.07);
  padding: 0.72rem 0;
}

.surface-content__row:first-child {
  border-top: none;
}

.surface-content__row-main {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
}

.surface-content__row-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.9rem;
  font-weight: 650;
  letter-spacing: -0.025em;
}

.surface-content__row-meta {
  color: rgba(23, 34, 26, 0.54);
  font-size: 0.76rem;
  white-space: nowrap;
}

.surface-content__badge {
  flex: 0 0 auto;
  border-radius: 999px;
  background: rgba(23, 34, 26, 0.08);
  padding: 0.18rem 0.45rem;
  color: rgba(23, 34, 26, 0.62);
  font-size: 0.68rem;
  white-space: nowrap;
}

.surface-content__row-actions {
  display: flex;
  gap: 0.35rem;
}

.surface-content__row-link {
  min-height: 1.9rem;
  padding: 0.3rem 0.58rem;
  font-size: 0.72rem;
}

.surface-content__empty-state,
.surface-content__empty-surface {
  color: rgba(23, 34, 26, 0.52);
  font-size: 0.84rem;
}

@media (max-width: 760px) {
  .surface-content__header {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .surface-content__actions {
    justify-content: flex-start;
  }

  .surface-content__sections,
  .surface-content--chat .surface-content__sections,
  .surface-content--calendar .surface-content__sections,
  .surface-content--business .surface-content__sections {
    grid-template-columns: minmax(0, 1fr);
  }

  .surface-content__inbox-layout {
    grid-template-columns: minmax(0, 1fr);
  }

  .surface-content__inbox-focus {
    order: -1;
  }

  .surface-content__timeline-row {
    grid-template-columns: auto minmax(0, 1fr) auto;
  }

  .surface-content__timeline-row .surface-content__row-meta {
    grid-column: 2;
  }

  .surface-content__timeline-row .surface-content__row-link {
    grid-column: 3;
    grid-row: 1 / span 2;
  }

  .surface-content__row {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  .surface-content__row-meta {
    grid-column: 1;
  }

  .surface-content__row-actions {
    grid-column: 2;
    grid-row: 1 / span 2;
  }
}
</style>
