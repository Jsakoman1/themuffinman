<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {createDashboardWorkPlannerState} from "../../composables/dashboard/createDashboardWorkPlannerState.ts"

const props = defineProps<{
  dashboard: QuestDashboard
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
  <article class="card overview-panel overview-panel--planner">
    <div class="calendar-toolbar">
      <div class="calendar-toolbar__month">
        <button
          class="button button--ghost calendar-nav-button"
          type="button"
          :disabled="!canGoPrevious"
          aria-label="Previous"
          @click="shiftBack"
        >
          <span aria-hidden="true">←</span>
        </button>

        <div class="calendar-toolbar__title">
          <strong>{{ monthLabel }}</strong>
        </div>

        <button class="button button--ghost calendar-nav-button" type="button" aria-label="Next" @click="shiftForward">
          <span aria-hidden="true">→</span>
        </button>
      </div>
    </div>

    <div class="calendar-month-frame mt-3">
      <div class="calendar-month-head">
        <div v-for="weekday in weekdayLabels" :key="weekday" class="calendar-month-head__cell">
          {{ weekday }}
        </div>
      </div>

      <div class="calendar-month-grid">
        <article
          v-for="cell in monthCells"
          :key="cell.key"
          class="calendar-month-cell"
          role="button"
          tabindex="0"
          :class="{
            'calendar-month-cell--outside': !cell.inMonth,
            'calendar-month-cell--today': cell.isToday,
            'calendar-month-cell--past': cell.isPast && cell.inMonth,
            'calendar-month-cell--locked': cell.isLocked
          }"
          @click="openDayDialog(cell.key)"
          @keydown.enter.prevent="openDayDialog(cell.key)"
          @keydown.space.prevent="openDayDialog(cell.key)"
        >
          <div class="calendar-month-cell__top">
            <span class="calendar-month-cell__day">{{ cell.dayNumber }}</span>
            <span v-if="cell.items.length" class="calendar-month-cell__count">{{ cell.items.length }}</span>
          </div>

          <div v-if="cell.items.length" class="calendar-month-cell__items">
            <button
              v-for="item in cell.items.slice(0, 2)"
              :key="item.id"
              class="calendar-event calendar-event--month"
              :class="[item.kind === 'incoming' ? 'calendar-event--incoming' : 'calendar-event--outgoing', { 'calendar-event--muted': cell.isPast, 'calendar-event--range': item.hasRange }]"
              type="button"
              @click.stop="openItem(item.navigation)"
            >
              <span class="calendar-event__time">{{ item.timeLabel }}</span>
              <span class="calendar-event__title">{{ item.title }}</span>
            </button>

            <div v-if="cell.items.length > 2" class="calendar-month-cell__more">
              +{{ cell.items.length - 2 }}
            </div>
          </div>
        </article>
      </div>
    </div>

    <div v-if="flexibleItems.length" class="calendar-flexible mt-3">
      <div class="calendar-flexible__header">
        <span class="badge">Flexible</span>
        <span class="badge">{{ flexibleItems.length }}</span>
      </div>

      <div class="calendar-flexible__list">
        <button
          v-for="item in flexibleItems"
          :key="item.id"
          class="calendar-event calendar-event--month"
          :class="[item.kind === 'incoming' ? 'calendar-event--incoming' : 'calendar-event--outgoing']"
          type="button"
          @click="openItem(item.navigation)"
        >
          <span class="calendar-event__time">{{ item.timeLabel }}</span>
          <span class="calendar-event__title">{{ item.title }}</span>
        </button>
      </div>
    </div>

    <UiDialog :open="!!selectedDay" :title="selectedDayLabel" :subtitle="''" @close="closeDayDialog">
      <div class="calendar-day-dialog">
        <div class="calendar-day-dialog__header">
          <span class="calendar-day-dialog__kicker">
            {{ selectedDayItems.length ? `${selectedDayItems.length} event${selectedDayItems.length === 1 ? '' : 's'}` : 'No events' }}
          </span>
          <span v-if="selectedDayLocked" class="badge badge--danger">Locked</span>
          <span v-else class="badge badge--success">Open</span>
        </div>

        <div v-if="selectedDayItems.length" class="calendar-day-dialog__list">
          <button
            v-for="item in selectedDayItems"
            :key="item.id"
            class="calendar-event calendar-event--dialog"
            :class="[item.kind === 'incoming' ? 'calendar-event--incoming' : 'calendar-event--outgoing', { 'calendar-event--range': item.hasRange }]"
            type="button"
            @click="openItem(item.navigation)"
          >
            <span class="calendar-event__time">{{ item.timeLabel }}</span>
            <span class="calendar-event__title">{{ item.title }}</span>
          </button>
        </div>

        <p v-else class="calendar-day-dialog__empty">
          Quiet day.
        </p>

        <div class="calendar-day-dialog__actions">
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
  </article>
</template>
