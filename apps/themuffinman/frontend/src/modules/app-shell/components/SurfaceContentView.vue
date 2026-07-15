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
    <div class="surface-content__hero">
      <div class="surface-content__hero-copy">
        <p class="surface-content__eyebrow">{{ config.eyebrow }}</p>
        <h2 class="surface-content__title">{{ config.title }}</h2>
        <p v-if="detailLabel" class="surface-content__detail-label">{{ detailLabel }}</p>
        <p class="surface-content__description">{{ config.description }}</p>
        <p class="surface-content__supporting">{{ config.supportingText }}</p>
      </div>

      <div class="surface-content__actions">
        <RouterLink
          v-for="action in config.actions"
          :key="action.label"
          :to="action.to"
          class="surface-content__action"
          :class="`surface-content__action--${action.tone ?? 'secondary'}`"
        >
          <span class="surface-content__action-label">{{ action.label }}</span>
          <span class="surface-content__action-description">{{ action.description }}</span>
        </RouterLink>
      </div>
    </div>

    <div v-if="metrics.length > 0" class="surface-content__metrics">
      <article
        v-for="metric in metrics"
        :key="metric.label"
        class="surface-content__metric"
        :class="{ 'surface-content__metric--emphasis': metric.tone === 'emphasis' }"
      >
        <p class="surface-content__metric-label">{{ metric.label }}</p>
        <p class="surface-content__metric-value">{{ metric.value }}</p>
        <p class="surface-content__metric-detail">{{ metric.detail }}</p>
      </article>
    </div>

    <div v-if="loading" class="surface-content__status-card">
      Loading live surface data.
    </div>
    <div v-else-if="error" class="surface-content__status-card surface-content__status-card--error">
      {{ error }}
    </div>

    <div v-if="note" class="surface-content__status-card">
      {{ note }}
    </div>

    <div class="surface-content__sections">
      <article
        v-for="section in sections"
        :key="section.title"
        class="surface-content__card"
      >
        <div class="surface-content__card-header">
          <h3 class="surface-content__card-title">{{ section.title }}</h3>
          <p class="surface-content__card-description">{{ section.description }}</p>
        </div>

        <div v-if="section.rows.length > 0" class="surface-content__rows">
          <div
            v-for="row in section.rows"
            :key="row.id"
            class="surface-content__row"
          >
            <div class="surface-content__row-copy">
              <div class="surface-content__row-heading">
                <p class="surface-content__row-title">{{ row.title }}</p>
                <span v-if="row.badge" class="surface-content__badge">{{ row.badge }}</span>
              </div>
              <p class="surface-content__row-description">{{ row.description }}</p>
              <p v-if="row.meta" class="surface-content__row-meta">{{ row.meta }}</p>
            </div>

            <div v-if="row.to || row.visionTo" class="surface-content__row-actions">
              <RouterLink v-if="row.to" :to="row.to" class="surface-content__row-link">
                Open
              </RouterLink>
              <RouterLink v-if="row.visionTo" :to="row.visionTo" class="surface-content__row-link surface-content__row-link--vision">
                Vision
              </RouterLink>
            </div>
          </div>
        </div>
        <p v-else class="surface-content__empty-state">{{ section.emptyState }}</p>
      </article>
    </div>
    <p v-if="sections.length === 0" class="surface-content__empty-surface">
      No live items are available for this surface yet.
    </p>
  </section>
</template>

<style scoped>
.surface-content {
  display: grid;
  gap: var(--space-4);
}

.surface-content--chat .surface-content__sections {
  grid-template-columns: minmax(0, 1fr);
}

.surface-content--calendar .surface-content__sections,
.surface-content--business .surface-content__sections {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.surface-content--profile .surface-content__hero,
.surface-content--circles .surface-content__hero {
  grid-template-columns: minmax(0, 1fr) minmax(14rem, 0.72fr);
}

.surface-content__hero,
.surface-content__metric,
.surface-content__card,
.surface-content__status-card {
  border-radius: var(--radius-card);
  border: 1px solid rgba(23, 34, 26, 0.08);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-card);
}

.surface-content__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(16rem, 0.92fr);
  gap: var(--space-4);
  padding: var(--space-5);
}

.surface-content__hero-copy,
.surface-content__actions {
  min-width: 0;
}

.surface-content__eyebrow,
.surface-content__title,
.surface-content__detail-label,
.surface-content__description,
.surface-content__supporting,
.surface-content__metric-label,
.surface-content__metric-value,
.surface-content__metric-detail,
.surface-content__card-title,
.surface-content__card-description,
.surface-content__row-title,
.surface-content__row-description,
.surface-content__row-meta {
  margin: 0;
}

.surface-content__hero-copy {
  display: grid;
  gap: var(--space-1);
}

.surface-content__eyebrow {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: rgba(23, 34, 26, 0.45);
}

.surface-content__title {
  font-size: clamp(1.45rem, 1.95vw, 1.85rem);
  letter-spacing: -0.06em;
}

.surface-content__detail-label {
  color: rgba(23, 34, 26, 0.7);
  font-weight: 600;
}

.surface-content__description,
.surface-content__supporting,
.surface-content__metric-detail,
.surface-content__card-description,
.surface-content__row-description,
.surface-content__row-meta,
.surface-content__empty-state {
  line-height: 1.5;
}

.surface-content__supporting,
.surface-content__metric-detail,
.surface-content__card-description,
.surface-content__row-description,
.surface-content__row-meta,
.surface-content__empty-state {
  color: rgba(23, 34, 26, 0.62);
}

.surface-content__actions,
.surface-content__metrics,
.surface-content__sections,
.surface-content__rows {
  display: grid;
  gap: 0.8rem;
}

.surface-content__action {
  display: grid;
  gap: 0.2rem;
  padding: 0.92rem 1rem;
  border-radius: 1.05rem;
  border: 1px solid rgba(23, 34, 26, 0.08);
  background: rgba(247, 249, 243, 0.92);
}

.surface-content__action--primary,
.surface-content__action--vision {
  background: rgba(23, 34, 26, 0.94);
  color: #f8f8f4;
}

.surface-content__action--primary .surface-content__action-description,
.surface-content__action--vision .surface-content__action-description {
  color: rgba(248, 248, 244, 0.72);
}

.surface-content__action-label {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.surface-content__action-description {
  color: rgba(23, 34, 26, 0.62);
}

.surface-content__metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.surface-content__metric {
  padding: 1rem;
  display: grid;
  gap: 0.28rem;
}

.surface-content__metric--emphasis {
  background: rgba(23, 34, 26, 0.96);
  color: #f8f8f4;
}

.surface-content__metric--emphasis .surface-content__metric-detail,
.surface-content__metric--emphasis .surface-content__metric-label {
  color: rgba(248, 248, 244, 0.74);
}

.surface-content__metric-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(23, 34, 26, 0.46);
}

.surface-content__metric-value {
  font-size: clamp(1.35rem, 2vw, 1.8rem);
  letter-spacing: -0.06em;
  font-weight: 700;
}

.surface-content__status-card {
  padding: 0.9rem 1rem;
}

.surface-content__status-card--error {
  border-color: rgba(146, 45, 28, 0.15);
  background: rgba(255, 245, 241, 0.92);
  color: #7c2a1d;
}

.surface-content__sections {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.surface-content__card {
  display: grid;
  gap: 0.85rem;
  align-content: start;
  padding: 1rem;
}

.surface-content__card-header {
  display: grid;
  gap: 0.28rem;
}

.surface-content__card-title {
  font-size: 1.02rem;
  letter-spacing: -0.04em;
}

.surface-content__rows {
  gap: 0.7rem;
}

.surface-content__row {
  display: grid;
  gap: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(23, 34, 26, 0.08);
}

.surface-content__row:first-child {
  padding-top: 0;
  border-top: none;
}

.surface-content__row-heading {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  justify-content: space-between;
}

.surface-content__row-title {
  font-weight: 600;
  letter-spacing: -0.03em;
}

.surface-content__badge {
  display: inline-flex;
  align-items: center;
  padding: 0.18rem 0.5rem;
  border-radius: 999px;
  background: rgba(23, 34, 26, 0.08);
  color: rgba(23, 34, 26, 0.74);
  font-size: 0.72rem;
  white-space: nowrap;
}

.surface-content__row-actions {
  display: flex;
  gap: 0.55rem;
  flex-wrap: wrap;
}

.surface-content__row-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 2.25rem;
  padding: 0.45rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(23, 34, 26, 0.12);
  background: rgba(247, 249, 243, 0.92);
}

.surface-content__row-link--vision {
  background: rgba(23, 34, 26, 0.94);
  color: #f8f8f4;
}

@media (max-width: 1080px) {
  .surface-content__hero,
  .surface-content__metrics,
  .surface-content__sections {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
