<script setup lang="ts">
import {computed, ref} from "vue"
import UiDialog from "../ui/UiDialog.vue"
import {formatInstantForInput} from "../../shared/questSchedule.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {
  endOfMonth,
  formatTimeLabel,
  parseDate,
  parseDateKey,
  startOfMonth,
  startOfWeek,
  toDateKey
} from "../../lib/dashboardCalendar.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

type CalendarEntry = {
  id: string
  questId: number
  title: string
  scheduledAt: string | null
  endsAt: string | null
  timeLabel: string
  minuteOfDay: number
  dateKey: string
  kind: "incoming" | "outgoing"
  hasRange: boolean
}

type MonthCell = {
  key: string
  dayNumber: string
  inMonth: boolean
  isToday: boolean
  isPast: boolean
  isLocked: boolean
  items: CalendarEntry[]
}

const today = new Date()
today.setHours(0, 0, 0, 0)

const accountCreatedAt = computed(() => {
  const createdAt = parseDate(props.dashboard.accountCreatedAt)
  if (!createdAt) {
    return new Date(today)
  }

  createdAt.setHours(0, 0, 0, 0)
  return createdAt
})

const focusDate = ref(new Date(today))
const selectedDateKey = ref<string | null>(null)
const visibleMonth = computed(() => startOfMonth(focusDate.value))

const weekdayLabels = computed(() =>
  Array.from({length: 7}, (_, index) => {
    const date = new Date(2024, 0, 1 + index)
    return new Intl.DateTimeFormat("en-GB", {weekday: "short"}).format(date)
  })
)

const monthLabel = computed(() =>
    new Intl.DateTimeFormat("en-GB", {
      month: "long",
      year: "numeric"
    }).format(focusDate.value)
)

const canGoPrevious = computed(() => {
  const previousMonth = startOfMonth(new Date(focusDate.value))
  previousMonth.setMonth(previousMonth.getMonth() - 1)
  const previousMonthEnd = endOfMonth(previousMonth)
  return previousMonthEnd.getTime() >= accountCreatedAt.value.getTime()
})

const shiftBack = () => {
  const next = startOfMonth(new Date(focusDate.value))
  next.setMonth(next.getMonth() - 1)
  if (endOfMonth(next).getTime() >= accountCreatedAt.value.getTime()) {
    focusDate.value = next
  }
}

const shiftForward = () => {
  const next = startOfMonth(new Date(focusDate.value))
  next.setMonth(next.getMonth() + 1)
  focusDate.value = next
}

const calendarEntries = computed<CalendarEntry[]>(() => {
  const entries: CalendarEntry[] = []

  for (const quest of props.dashboard.incomingWorkQuests) {
    const scheduled = parseDate(quest.scheduledAt)
    if (!scheduled) {
      continue
    }
    const dateKey = toDateKey(scheduled)
    const minuteOfDay = scheduled.getHours() * 60 + scheduled.getMinutes()

    entries.push({
      id: `quest-${quest.id}`,
      questId: quest.id,
      title: quest.title,
      scheduledAt: quest.scheduledAt ?? null,
      endsAt: quest.endsAt ?? null,
      timeLabel: formatTimeLabel(quest.scheduledAt, quest.endsAt),
      minuteOfDay,
      dateKey,
      kind: "incoming",
      hasRange: !!quest.endsAt
    })
  }

  for (const application of props.dashboard.outgoingWorkApplications) {
    const quest = props.dashboard.questForId(application.questId)
    const scheduled = parseDate(quest?.scheduledAt)
    if (!scheduled) {
      continue
    }
    const dateKey = toDateKey(scheduled)
    const minuteOfDay = scheduled.getHours() * 60 + scheduled.getMinutes()

    entries.push({
      id: `application-${application.id}`,
      questId: application.questId,
      title: application.questTitle,
      scheduledAt: quest?.scheduledAt ?? null,
      endsAt: quest?.endsAt ?? null,
      timeLabel: formatTimeLabel(quest?.scheduledAt, quest?.endsAt),
      minuteOfDay,
      dateKey,
      kind: "outgoing",
      hasRange: !!quest?.endsAt
    })
  }

  return entries.sort((left, right) => left.minuteOfDay - right.minuteOfDay)
})

const entriesByDate = computed<Record<string, CalendarEntry[]>>(() => {
  return calendarEntries.value.reduce<Record<string, CalendarEntry[]>>((accumulator, entry) => {
    if (!accumulator[entry.dateKey]) {
      accumulator[entry.dateKey] = []
    }

    accumulator[entry.dateKey].push(entry)
    return accumulator
  }, {})
})

const monthCells = computed<MonthCell[]>(() => {
  const monthStart = visibleMonth.value
  const monthEnd = endOfMonth(visibleMonth.value)
  const gridStart = startOfWeek(monthStart)
  const gridEnd = new Date(startOfWeek(monthEnd))
  gridEnd.setDate(gridEnd.getDate() + 6)

  const cells: MonthCell[] = []
  const cursor = new Date(gridStart)

  while (cursor <= gridEnd) {
    const current = new Date(cursor)
    current.setHours(0, 0, 0, 0)
    const key = toDateKey(current)
    const inMonth = current.getMonth() === visibleMonth.value.getMonth() && current.getFullYear() === visibleMonth.value.getFullYear()
    const isLocked = current.getTime() < accountCreatedAt.value.getTime()

    cells.push({
      key,
      dayNumber: String(current.getDate()),
      inMonth,
      isToday: key === toDateKey(today),
      isPast: current.getTime() < today.getTime(),
      isLocked,
      items: isLocked ? [] : entriesByDate.value[key] ?? []
    })

    cursor.setDate(cursor.getDate() + 1)
  }

  return cells
})

const flexibleItems = computed(() => {
  const items: CalendarEntry[] = []

  for (const quest of props.dashboard.incomingWorkQuests) {
    if (!quest.scheduledAt) {
      items.push({
        id: `flex-quest-${quest.id}`,
        questId: quest.id,
        title: quest.title,
        scheduledAt: null,
        endsAt: null,
        timeLabel: "All day",
        minuteOfDay: 0,
        dateKey: "flexible",
        kind: "incoming",
        hasRange: false
      })
    }
  }

  for (const application of props.dashboard.outgoingWorkApplications) {
    const quest = props.dashboard.questForId(application.questId)
    if (!quest?.scheduledAt) {
      items.push({
        id: `flex-app-${application.id}`,
        questId: application.questId,
        title: application.questTitle,
        scheduledAt: null,
        endsAt: null,
        timeLabel: "All day",
        minuteOfDay: 0,
        dateKey: "flexible",
        kind: "outgoing",
        hasRange: false
      })
    }
  }

  return items
})

const selectedDay = computed(() => parseDateKey(selectedDateKey.value))

const selectedDayKey = computed(() => {
  if (!selectedDay.value) {
    return ""
  }

  return toDateKey(selectedDay.value)
})

const selectedDayItems = computed(() => {
  if (!selectedDayKey.value) {
    return []
  }

  return entriesByDate.value[selectedDayKey.value] ?? []
})

const selectedDayLabel = computed(() => {
  if (!selectedDay.value) {
    return ""
  }

  return new Intl.DateTimeFormat("en-GB", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
  }).format(selectedDay.value)
})

const selectedDayLocked = computed(() => {
  if (!selectedDay.value) {
    return false
  }

  return selectedDay.value.getTime() < accountCreatedAt.value.getTime()
})

const selectedDayCanCreate = computed(() => {
  if (!selectedDay.value) {
    return false
  }

  return selectedDay.value.getTime() >= accountCreatedAt.value.getTime()
})

const openDayDialog = (dateKey: string) => {
  selectedDateKey.value = dateKey
}

const closeDayDialog = () => {
  selectedDateKey.value = null
}

const openCreateOnSelectedDay = () => {
  if (!selectedDay.value) {
    return
  }

  const createDate = new Date(selectedDay.value)
  createDate.setHours(9, 0, 0, 0)
  props.dashboard.questScheduledAt = formatInstantForInput(createDate.toISOString())
  props.dashboard.questEndsAt = ""
  props.dashboard.setQuestTermMode("start-only")
  props.dashboard.openCreateJobDialog()
  closeDayDialog()
}

const openItem = (questId: number) => {
  props.dashboard.openQuestDialog(questId)
}
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
              @click.stop="openItem(item.questId)"
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
          @click="openItem(item.questId)"
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
            @click="openItem(item.questId)"
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
