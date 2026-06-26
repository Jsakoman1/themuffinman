<script setup lang="ts">
import UiEventPill from "../../../../components/ui/UiEventPill.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {createDashboardWorkPlannerState} from "../../composables/dashboard/createDashboardWorkPlannerState.ts"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import type {DashboardWorkPlannerFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardWorkPlannerFacade
  showHeader?: boolean
  showFlexible?: boolean
}>()
const {
  weekdayLabels,
  viewMode,
  periodLabel,
  canGoPrevious,
  setViewMode,
  shiftBack,
  shiftForward,
  flexibleItems,
  selectedDay,
  selectedDayItems,
  selectedDayLabel,
  selectedDayLocked,
  selectedDayCanCreate,
  openDayDialog,
  closeDayDialog,
  openCreateOnSelectedDay,
  openItem,
  monthCells,
  timelineDays,
  timelineHours,
  timelineBodyHeight,
  timelineNowOffset,
  timelineSpanItems,
  timelineSpanLaneCount,
} = createDashboardWorkPlannerState(props.dashboard)
</script>

<template>
  <UiSurfaceSection
    tag="article"
    class="dashboard-overview__planner"
    compact
    plain
    :eyebrow="props.showHeader === false ? '' : 'Planner'"
    :title="props.showHeader === false ? '' : 'Calendar'"
  >
    <div v-if="props.showHeader === false" class="dashboard-calendar__hero">
      <h1 class="dashboard-calendar__hero-title">Calendar</h1>

      <div class="dashboard-calendar__hero-controls">
        <div class="dashboard-calendar__view-toggle" role="tablist" aria-label="Calendar view">
          <button
            class="dashboard-calendar__view-button"
            :class="{'dashboard-calendar__view-button--active': viewMode === 'month'}"
            type="button"
            @click="setViewMode('month')"
          >
            Month
          </button>
          <button
            class="dashboard-calendar__view-button"
            :class="{'dashboard-calendar__view-button--active': viewMode === 'week'}"
            type="button"
            @click="setViewMode('week')"
          >
            Week
          </button>
          <button
            class="dashboard-calendar__view-button"
            :class="{'dashboard-calendar__view-button--active': viewMode === 'day'}"
            type="button"
            @click="setViewMode('day')"
          >
            Day
          </button>
        </div>

        <div class="dashboard-calendar__toolbar dashboard-calendar__toolbar--hero">
          <button
            class="button button--ghost dashboard-calendar__nav-button"
            type="button"
            :disabled="!canGoPrevious"
            aria-label="Previous"
            @click="shiftBack"
          >
            <span aria-hidden="true">←</span>
          </button>

          <div class="dashboard-calendar__title">
            <strong>{{ periodLabel }}</strong>
          </div>

          <button class="button button--ghost dashboard-calendar__nav-button" type="button" aria-label="Next" @click="shiftForward">
            <span aria-hidden="true">→</span>
          </button>
        </div>
      </div>
    </div>

    <template #actions>
      <div v-if="props.showHeader !== false" class="dashboard-calendar__toolbar">
        <button
          class="button button--ghost dashboard-calendar__nav-button"
          type="button"
          :disabled="!canGoPrevious"
          aria-label="Previous"
          @click="shiftBack"
        >
          <span aria-hidden="true">←</span>
        </button>

        <div class="dashboard-calendar__title">
          <strong>{{ periodLabel }}</strong>
        </div>

        <button class="button button--ghost dashboard-calendar__nav-button" type="button" aria-label="Next" @click="shiftForward">
          <span aria-hidden="true">→</span>
        </button>
      </div>
    </template>

    <div v-if="viewMode === 'month'" class="dashboard-calendar__frame">
      <div class="dashboard-calendar__head">
        <div v-for="weekday in weekdayLabels" :key="weekday" class="dashboard-calendar__head-cell">
          {{ weekday }}
        </div>
      </div>

      <div class="dashboard-calendar__grid">
        <article
          v-for="cell in monthCells"
          :key="cell.key"
          class="dashboard-calendar__cell"
          role="button"
          tabindex="0"
          :class="{
            'dashboard-calendar__cell--outside': !cell.inMonth,
            'dashboard-calendar__cell--today': cell.isToday,
            'dashboard-calendar__cell--past': cell.isPast && cell.inMonth,
            'dashboard-calendar__cell--locked': cell.isLocked
          }"
          @click="openDayDialog(cell.key)"
          @keydown.enter.prevent="openDayDialog(cell.key)"
          @keydown.space.prevent="openDayDialog(cell.key)"
        >
          <div class="dashboard-calendar__cell-top">
            <span class="dashboard-calendar__cell-day">{{ cell.dayNumber }}</span>
          </div>

          <div v-if="cell.items.length" class="dashboard-calendar__cell-items">
            <button
              v-for="item in cell.items.slice(0, 2)"
              :key="item.id"
              class="button-reset"
              type="button"
              @click.stop="openItem(item)"
            >
              <UiEventPill
                :time="item.timeLabel"
                :title="item.title"
                :tone="item.tone"
                month
                :muted="cell.isPast"
                :range="item.hasRange"
              />
            </button>

            <div v-if="cell.items.length > 2" class="dashboard-calendar__cell-more">
              +{{ cell.items.length - 2 }}
            </div>
          </div>
        </article>
      </div>
    </div>

    <div v-else class="dashboard-calendar__timeline-shell">
      <div class="dashboard-calendar__timeline-top">
        <div class="dashboard-calendar__timeline-spacer" />
        <div class="dashboard-calendar__timeline-day-headings" :style="{'--calendar-day-count': String(timelineDays.length)}">
          <button
            v-for="day in timelineDays"
            :key="day.key"
            class="dashboard-calendar__timeline-heading"
            :class="{
              'dashboard-calendar__timeline-heading--today': day.isToday,
              'dashboard-calendar__timeline-heading--past': day.isPast
            }"
            type="button"
            @click="openDayDialog(day.key)"
          >
            <span class="dashboard-calendar__timeline-heading-weekday">{{ day.weekdayLabel }}</span>
            <span class="dashboard-calendar__timeline-heading-date">{{ day.dayNumber }}</span>
          </button>
        </div>
      </div>

      <div
        v-if="timelineSpanItems.length"
        class="dashboard-calendar__spans"
        :style="{
          '--calendar-day-count': String(timelineDays.length),
          '--calendar-span-lanes': String(timelineSpanLaneCount)
        }"
      >
        <div class="dashboard-calendar__timeline-spacer" />
        <div class="dashboard-calendar__spans-grid">
          <button
            v-for="item in timelineSpanItems"
            :key="`${item.id}-${item.lane}`"
            class="dashboard-calendar__span-item"
            :class="`dashboard-calendar__span-item--${item.kind}`"
            type="button"
            :style="{
              gridColumn: `${item.startColumn} / span ${item.columnSpan}`,
              gridRow: String(item.lane)
            }"
            @click="openItem(item)"
          >
            <span class="dashboard-calendar__span-time">{{ item.timeLabel }}</span>
            <span class="dashboard-calendar__span-title">{{ item.title }}</span>
          </button>
        </div>
      </div>

      <div class="dashboard-calendar__timeline">
        <div class="dashboard-calendar__timeline-rail">
          <div class="dashboard-calendar__timeline-rail-body" :style="{height: `${timelineBodyHeight}px`}">
            <div
              v-for="hour in timelineHours"
              :key="hour.hour"
              class="dashboard-calendar__timeline-hour"
              :style="{top: `${hour.offset}px`}"
            >
              {{ hour.label }}
            </div>
          </div>
        </div>

        <div class="dashboard-calendar__timeline-columns" :style="{'--calendar-day-count': String(timelineDays.length)}">
          <section
            v-for="day in timelineDays"
            :key="day.key"
            class="dashboard-calendar__timeline-day"
            :class="{
              'dashboard-calendar__timeline-day--today': day.isToday,
              'dashboard-calendar__timeline-day--past': day.isPast
            }"
          >
            <button
              class="dashboard-calendar__timeline-day-surface"
              type="button"
              :style="{height: `${timelineBodyHeight}px`}"
              @click="openDayDialog(day.key)"
            >
              <span
                v-for="hour in timelineHours"
                :key="`${day.key}-${hour.hour}`"
                class="dashboard-calendar__timeline-line"
                :style="{top: `${hour.offset}px`}"
              />
              <span
                v-if="day.isToday && timelineNowOffset !== null"
                class="dashboard-calendar__timeline-now"
                :style="{top: `${timelineNowOffset}px`}"
              />
            </button>

            <button
              v-for="entry in day.entries"
              :key="entry.id"
              class="dashboard-calendar__timeline-entry"
              :class="[
                `dashboard-calendar__timeline-entry--${entry.kind}`,
                {
                  'dashboard-calendar__timeline-entry--range': entry.hasRange,
                  'dashboard-calendar__timeline-entry--starts-before': entry.startsBefore,
                  'dashboard-calendar__timeline-entry--ends-after': entry.endsAfter
                }
              ]"
              type="button"
              :style="{
                top: `${entry.top}px`,
                height: `${entry.height}px`,
                left: `calc(${entry.column} * (100% / ${entry.columnCount}))`,
                width: `calc((100% / ${entry.columnCount}) - 6px)`
              }"
              @click.stop="openItem(entry)"
            >
              <span class="dashboard-calendar__timeline-entry-time">{{ entry.timeLabel }}</span>
              <span class="dashboard-calendar__timeline-entry-title">{{ entry.title }}</span>
            </button>
          </section>
        </div>
      </div>
    </div>

    <UiSurfaceSection v-if="props.showFlexible !== false && flexibleItems.length" class="dashboard-calendar__flexible" compact plain title="Flexible">
      <div class="dashboard-calendar__flexible-list">
        <button
          v-for="item in flexibleItems"
          :key="item.id"
          class="button-reset"
          type="button"
          @click="openItem(item)"
        >
          <UiEventPill
            :time="item.timeLabel"
            :title="item.title"
            :tone="item.tone"
            month
          />
        </button>
      </div>
    </UiSurfaceSection>

    <UiDialog :open="!!selectedDay" :title="selectedDayLabel" size="xl" @close="closeDayDialog">
      <DetailDialogFrame>
        <template #main>
          <div class="dashboard-calendar__day-dialog">
            <div class="dashboard-calendar__day-header">
              <span class="dashboard-calendar__day-kicker">Schedule</span>
              <span v-if="selectedDayLocked" class="badge badge--danger">Past day</span>
              <span v-else-if="selectedDayItems.length" class="badge badge--neutral">{{ selectedDayItems.length }} item{{ selectedDayItems.length === 1 ? '' : 's' }}</span>
            </div>

            <div v-if="selectedDayItems.length" class="dashboard-calendar__day-list">
              <button
              v-for="item in selectedDayItems"
              :key="item.id"
              class="button-reset"
              type="button"
              @click="openItem(item)"
              >
                <UiEventPill
                  :time="item.timeLabel"
                  :title="item.title"
                  :tone="item.tone"
                  dialog
                  :range="item.hasRange"
                />
              </button>
            </div>

            <p v-else class="dashboard-calendar__day-empty">
              Quiet day.
            </p>
          </div>
        </template>

        <template #side>
          <DetailUtilitySection title="Summary" tone="summary">
            <div class="quest-overview-aside quest-overview-aside--compact">
              <div class="quest-overview-aside__row">
                <span class="quest-overview-aside__label">Items</span>
                <span class="quest-overview-aside__value">{{ selectedDayItems.length }}</span>
              </div>
              <div class="quest-overview-aside__row">
                <span class="quest-overview-aside__label">Create new</span>
                <span class="quest-overview-aside__value">{{ selectedDayCanCreate && !selectedDayLocked ? "Available" : "Locked" }}</span>
              </div>
            </div>
          </DetailUtilitySection>

          <DetailUtilitySection title="Actions" tone="actions">
            <div class="ui-action-stack">
              <button
                class="button button--secondary"
                type="button"
                :disabled="selectedDayLocked || !selectedDayCanCreate"
                @click="openCreateOnSelectedDay"
              >
                Create new
              </button>
              <button class="button button--ghost" type="button" @click="closeDayDialog">Close</button>
            </div>
          </DetailUtilitySection>
        </template>
      </DetailDialogFrame>
    </UiDialog>
  </UiSurfaceSection>
</template>
