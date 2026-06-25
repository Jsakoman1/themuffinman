<script setup lang="ts">
import UiEventPill from "../../../../components/ui/UiEventPill.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {createDashboardWorkPlannerState} from "../../composables/dashboard/createDashboardWorkPlannerState.ts"
import type {DashboardWorkPlannerFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardWorkPlannerFacade
}>()
const {
  weekdayLabels,
  monthLabel,
  canGoPrevious,
  shiftBack,
  shiftForward,
  monthCells,
  flexibleItems,
  selectedDay,
  selectedDayItems,
  selectedDayLabel,
  selectedDayLocked,
  selectedDayCanCreate,
  openDayDialog,
  closeDayDialog,
  openCreateOnSelectedDay,
  openItem
} = createDashboardWorkPlannerState(props.dashboard)
</script>

<template>
  <UiSurfaceSection tag="article" class="dashboard-overview__planner" compact eyebrow="Planner" title="Calendar">
    <template #actions>
      <div class="dashboard-calendar__toolbar">
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
          <strong>{{ monthLabel }}</strong>
        </div>

        <button class="button button--ghost dashboard-calendar__nav-button" type="button" aria-label="Next" @click="shiftForward">
          <span aria-hidden="true">→</span>
        </button>
      </div>
    </template>

    <div class="dashboard-calendar__frame">
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
              @click.stop="openItem(item.navigation)"
            >
              <UiEventPill
                :time="item.timeLabel"
                :title="item.title"
                :tone="item.kind === 'managed' ? 'outgoing' : 'incoming'"
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

    <UiSurfaceSection v-if="flexibleItems.length" class="dashboard-calendar__flexible" compact title="Flexible">
      <div class="dashboard-calendar__flexible-list">
        <button
          v-for="item in flexibleItems"
          :key="item.id"
          class="button-reset"
          type="button"
          @click="openItem(item.navigation)"
        >
          <UiEventPill
            :time="item.timeLabel"
            :title="item.title"
            :tone="item.kind === 'managed' ? 'outgoing' : 'incoming'"
            month
          />
        </button>
      </div>
    </UiSurfaceSection>

    <UiDialog :open="!!selectedDay" :title="selectedDayLabel" @close="closeDayDialog">
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
            @click="openItem(item.navigation)"
          >
            <UiEventPill
              :time="item.timeLabel"
              :title="item.title"
              :tone="item.kind === 'managed' ? 'outgoing' : 'incoming'"
              dialog
              :range="item.hasRange"
            />
          </button>
        </div>

        <p v-else class="dashboard-calendar__day-empty">
          Quiet day.
        </p>

        <div class="dashboard-calendar__day-actions">
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
      </div>
    </UiDialog>
  </UiSurfaceSection>
</template>
