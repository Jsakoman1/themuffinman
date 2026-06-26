import {computed, ref} from "vue"
import {formatInstantForInput} from "../../../../shared/questSchedule.ts"
import type {DashboardWorkPlannerFacade} from "./dashboardFacades.ts"
import {
  endOfMonth,
  parseDate,
  parseDateKey,
  startOfMonth,
  startOfWeek,
  toDateKey
} from "../../../../lib/dashboardCalendar.ts"

type CalendarEntry = {
  id: string
  questId: number
  title: string
  scheduledAt: string | null
  endsAt: string | null
  timeLabel: string
  minuteOfDay: number
  dateKey: string
  kind: "managed" | "accepted"
  tone: "incoming" | "outgoing"
  kindLabel: string
  hasRange: boolean
}

type NormalizedCalendarEntry = CalendarEntry & {
  startDate: Date
  endDate: Date
  startKey: string
  endKey: string
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

type TimelineHour = {
  hour: number
  label: string
  offset: number
}

type TimelineEntryLayout = {
  column: number
  columnCount: number
  top: number
  height: number
  startsBefore: boolean
  endsAfter: boolean
}

type TimelineEntry = CalendarEntry & TimelineEntryLayout

type TimelineLayoutSource = NormalizedCalendarEntry & {
  startMinute: number
  endMinute: number
  startsBefore: boolean
  endsAfter: boolean
  column?: number
}

type TimelineDay = {
  key: string
  date: Date
  dayNumber: string
  weekdayLabel: string
  fullLabel: string
  isToday: boolean
  isPast: boolean
  entries: TimelineEntry[]
}

type TimelineSpanItem = CalendarEntry & {
  lane: number
  startColumn: number
  columnSpan: number
  startsBefore: boolean
  endsAfter: boolean
}

type PlannerViewMode = "month" | "week" | "day"

type ScheduledPlannerItem = NonNullable<DashboardWorkPlannerFacade["dashboardSections"]>["planner"]["scheduledItems"][number]
type FlexiblePlannerItem = NonNullable<DashboardWorkPlannerFacade["dashboardSections"]>["planner"]["flexibleItems"][number]

const MINUTES_PER_DAY = 24 * 60
const DEFAULT_EVENT_DURATION_MINUTES = 60
const DEFAULT_TIMELINE_START_HOUR = 7
const DEFAULT_TIMELINE_END_HOUR = 22
const TIMELINE_WEEK_PIXELS_PER_MINUTE = 0.9
const TIMELINE_DAY_PIXELS_PER_MINUTE = 1.1

const isPlannerKind = (value: string): value is CalendarEntry["kind"] => {
  return value === "managed" || value === "accepted"
}

const isPlannerTone = (value: string): value is CalendarEntry["tone"] => {
  return value === "incoming" || value === "outgoing"
}

const startOfDay = (date: Date) => {
  const copy = new Date(date)
  copy.setHours(0, 0, 0, 0)
  return copy
}

const addDays = (date: Date, amount: number) => {
  const copy = new Date(date)
  copy.setDate(copy.getDate() + amount)
  return copy
}

const addMinutes = (date: Date, amount: number) => {
  const copy = new Date(date)
  copy.setMinutes(copy.getMinutes() + amount)
  return copy
}

const formatHourLabel = (hour: number) => {
  return `${String(hour).padStart(2, "0")}:00`
}

const overlapsRange = (startDate: Date, endDate: Date, rangeStart: Date, rangeEndExclusive: Date) => {
  return startDate < rangeEndExclusive && endDate > rangeStart
}

const formatWeekdayLabel = (date: Date) => {
  return new Intl.DateTimeFormat("en-GB", {weekday: "short"}).format(date)
}

const formatDayHeaderLabel = (date: Date) => {
  return new Intl.DateTimeFormat("en-GB", {
    weekday: "short",
    day: "numeric",
    month: "short"
  }).format(date)
}

const formatTimeOnly = (date: Date) => {
  return new Intl.DateTimeFormat("en-GB", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(date)
}

const formatPlannerTimeLabel = (startDate: Date, endDate: Date | null) => {
  const startLabel = formatTimeOnly(startDate)
  if (!endDate || endDate.getTime() <= startDate.getTime()) {
    return startLabel
  }

  return `${startLabel}-${formatTimeOnly(endDate)}`
}

const normalizeCalendarEntry = (entry: CalendarEntry): NormalizedCalendarEntry | null => {
  const startDate = parseDate(entry.scheduledAt)
  if (!startDate) {
    return null
  }

  const parsedEndDate = parseDate(entry.endsAt)
  const endDate = parsedEndDate && parsedEndDate.getTime() > startDate.getTime()
    ? parsedEndDate
    : addMinutes(startDate, DEFAULT_EVENT_DURATION_MINUTES)

  return {
    ...entry,
    startDate,
    endDate,
    startKey: toDateKey(startDate),
    endKey: toDateKey(new Date(endDate.getTime() - 1)),
  }
}

const buildTimelineEntryLayout = (
  entries: TimelineLayoutSource[],
  timelineStartMinute: number,
  pixelsPerMinute: number
): TimelineEntry[] => {
  const layouts: TimelineEntry[] = []
  let cluster: TimelineLayoutSource[] = []
  let active: TimelineLayoutSource[] = []
  let clusterColumnCount = 1

  const finalizeCluster = () => {
    if (!cluster.length) {
      return
    }

    layouts.push(...cluster.map((entry) => ({
      id: entry.id,
      questId: entry.questId,
      title: entry.title,
      scheduledAt: entry.scheduledAt,
      endsAt: entry.endsAt,
      timeLabel: entry.timeLabel,
      minuteOfDay: entry.minuteOfDay,
      dateKey: entry.dateKey,
      kind: entry.kind,
      tone: entry.tone,
      kindLabel: entry.kindLabel,
      hasRange: entry.hasRange,
      column: entry.column ?? 0,
      columnCount: clusterColumnCount,
      top: Math.max(0, (entry.startMinute - timelineStartMinute) * pixelsPerMinute),
      height: Math.max(40, (entry.endMinute - entry.startMinute) * pixelsPerMinute),
      startsBefore: entry.startsBefore,
      endsAfter: entry.endsAfter,
    })))

    cluster = []
    active = []
    clusterColumnCount = 1
  }

  for (const entry of entries) {
    active = active.filter((activeEntry) => activeEntry.endMinute > entry.startMinute)

    if (!active.length && cluster.length) {
      finalizeCluster()
    }

    const usedColumns = new Set(active.map((activeEntry) => activeEntry.column ?? 0))
    let column = 0
    while (usedColumns.has(column)) {
      column += 1
    }

    entry.column = column
    active.push(entry)
    cluster.push(entry)
    clusterColumnCount = Math.max(clusterColumnCount, active.length)
  }

  finalizeCluster()
  return layouts
}

const buildSpanLayout = (
  entries: Array<NormalizedCalendarEntry & {
    startColumn: number
    endColumn: number
  }>
): TimelineSpanItem[] => {
  const layouts: TimelineSpanItem[] = []
  const lanes: Array<number[]> = []

  for (const entry of entries) {
    let laneIndex = 0
    while (laneIndex < lanes.length && lanes[laneIndex].some((column) => column >= entry.startColumn && column <= entry.endColumn)) {
      laneIndex += 1
    }

    if (!lanes[laneIndex]) {
      lanes[laneIndex] = []
    }

    for (let column = entry.startColumn; column <= entry.endColumn; column += 1) {
      lanes[laneIndex].push(column)
    }

    layouts.push({
      id: entry.id,
      questId: entry.questId,
      title: entry.title,
      scheduledAt: entry.scheduledAt,
      endsAt: entry.endsAt,
      timeLabel: entry.timeLabel,
      minuteOfDay: entry.minuteOfDay,
      dateKey: entry.dateKey,
      kind: entry.kind,
      tone: entry.tone,
      kindLabel: entry.kindLabel,
      hasRange: entry.hasRange,
      lane: laneIndex + 1,
      startColumn: entry.startColumn + 1,
      columnSpan: entry.endColumn - entry.startColumn + 1,
      startsBefore: false,
      endsAfter: false,
    })
  }

  return layouts
}

export const createDashboardWorkPlannerState = (dashboard: DashboardWorkPlannerFacade) => {
  const mapPlannerItem = (item: ScheduledPlannerItem): CalendarEntry | null => {
    const scheduled = parseDate(item.scheduledAt)
    if (!scheduled) {
      return null
    }
    const endsAt = parseDate(item.endsAt)

    return {
      id: item.id,
      questId: item.questId,
      title: item.title,
      scheduledAt: item.scheduledAt,
      endsAt: item.endsAt,
      timeLabel: formatPlannerTimeLabel(scheduled, endsAt),
      minuteOfDay: scheduled.getHours() * 60 + scheduled.getMinutes(),
      dateKey: toDateKey(scheduled),
      kind: isPlannerKind(item.kind) ? item.kind : "managed",
      tone: isPlannerTone(item.tone) ? item.tone : "outgoing",
      kindLabel: item.kindLabel || "Planned",
      hasRange: item.hasRange
    }
  }

const mapFlexiblePlannerItem = (item: FlexiblePlannerItem): CalendarEntry => ({
  id: item.id,
  questId: item.questId,
  title: item.title,
  scheduledAt: item.scheduledAt,
  endsAt: item.endsAt,
  timeLabel: "All day",
  minuteOfDay: 0,
  dateKey: "flexible",
  kind: isPlannerKind(item.kind) ? item.kind : "managed",
  tone: isPlannerTone(item.tone) ? item.tone : "outgoing",
  kindLabel: item.kindLabel || "Planned",
  hasRange: item.hasRange
})

  const today = startOfDay(new Date())
  const focusDate = ref(new Date(today))
  const selectedDateKey = ref<string | null>(null)
  const viewMode = ref<PlannerViewMode>("month")
  const visibleMonth = computed(() => startOfMonth(focusDate.value))
  const visibleWeek = computed(() => startOfWeek(focusDate.value))
  const visibleDay = computed(() => startOfDay(focusDate.value))

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

  const weekLabel = computed(() => {
    const weekStart = visibleWeek.value
    const weekEnd = addDays(weekStart, 6)
    const sameMonth = weekStart.getMonth() === weekEnd.getMonth() && weekStart.getFullYear() === weekEnd.getFullYear()
    const sameYear = weekStart.getFullYear() === weekEnd.getFullYear()

    if (sameMonth) {
      const monthYear = new Intl.DateTimeFormat("en-GB", {
        month: "long",
        year: "numeric"
      }).format(weekStart)
      return `${weekStart.getDate()}-${weekEnd.getDate()} ${monthYear}`
    }

    if (sameYear) {
      const startLabel = new Intl.DateTimeFormat("en-GB", {
        day: "numeric",
        month: "short"
      }).format(weekStart)
      const endLabel = new Intl.DateTimeFormat("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric"
      }).format(weekEnd)
      return `${startLabel} - ${endLabel}`
    }

    const startLabel = new Intl.DateTimeFormat("en-GB", {
      day: "numeric",
      month: "short",
      year: "numeric"
    }).format(weekStart)
    const endLabel = new Intl.DateTimeFormat("en-GB", {
      day: "numeric",
      month: "short",
      year: "numeric"
    }).format(weekEnd)
    return `${startLabel} - ${endLabel}`
  })

  const dayLabel = computed(() =>
    new Intl.DateTimeFormat("en-GB", {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric"
    }).format(visibleDay.value)
  )

  const periodLabel = computed(() => {
    if (viewMode.value === "week") {
      return weekLabel.value
    }

    if (viewMode.value === "day") {
      return dayLabel.value
    }

    return monthLabel.value
  })

  const canGoPrevious = computed(() => true)

  const shiftBack = () => {
    const next = new Date(focusDate.value)
    if (viewMode.value === "week") {
      next.setDate(next.getDate() - 7)
    } else if (viewMode.value === "day") {
      next.setDate(next.getDate() - 1)
    } else {
      const month = startOfMonth(next)
      month.setMonth(month.getMonth() - 1)
      next.setTime(month.getTime())
    }
    focusDate.value = next
  }

  const shiftForward = () => {
    const next = new Date(focusDate.value)
    if (viewMode.value === "week") {
      next.setDate(next.getDate() + 7)
    } else if (viewMode.value === "day") {
      next.setDate(next.getDate() + 1)
    } else {
      const month = startOfMonth(next)
      month.setMonth(month.getMonth() + 1)
      next.setTime(month.getTime())
    }
    focusDate.value = next
  }

  const calendarEntries = computed<CalendarEntry[]>(() => {
    const plannerSection = dashboard.dashboardSections?.planner
    return plannerSection?.scheduledItems
      .map(mapPlannerItem)
      .filter((item): item is CalendarEntry => item !== null)
      .sort((left, right) => left.minuteOfDay - right.minuteOfDay) ?? []
  })

  const normalizedEntries = computed(() =>
    calendarEntries.value
      .map(normalizeCalendarEntry)
      .filter((entry): entry is NormalizedCalendarEntry => entry !== null)
      .sort((left, right) => left.startDate.getTime() - right.startDate.getTime())
  )

  const entriesByDate = computed<Record<string, CalendarEntry[]>>(() => {
    return normalizedEntries.value.reduce<Record<string, CalendarEntry[]>>((accumulator, entry) => {
      const cursor = startOfDay(entry.startDate)
      const lastDay = startOfDay(new Date(entry.endDate.getTime() - 1))

      while (cursor.getTime() <= lastDay.getTime()) {
        const key = toDateKey(cursor)
        if (!accumulator[key]) {
          accumulator[key] = []
        }

        const isStartDay = key === entry.startKey
        const isEndDay = key === entry.endKey
        let timeLabel = entry.timeLabel

        if (!isStartDay && !isEndDay) {
          timeLabel = "All day"
        } else if (!isStartDay && isEndDay) {
          timeLabel = `Until ${formatTimeOnly(entry.endDate)}`
        } else if (isStartDay && !isEndDay) {
          timeLabel = `${formatTimeOnly(entry.startDate)} onward`
        }

        accumulator[key].push({
          id: entry.id,
          questId: entry.questId,
          title: entry.title,
          scheduledAt: entry.scheduledAt,
          endsAt: entry.endsAt,
          timeLabel,
          minuteOfDay: isStartDay ? entry.minuteOfDay : 0,
          dateKey: key,
          kind: entry.kind,
          tone: entry.tone,
          kindLabel: entry.kindLabel,
          hasRange: entry.hasRange
        })

        cursor.setDate(cursor.getDate() + 1)
      }

      return accumulator
    }, {})
  })

  const monthCells = computed<MonthCell[]>(() => {
    const monthStart = visibleMonth.value
    const monthEnd = endOfMonth(visibleMonth.value)
    const gridStart = startOfWeek(monthStart)
    const gridEnd = addDays(startOfWeek(monthEnd), 6)

    const cells: MonthCell[] = []
    const cursor = new Date(gridStart)

    while (cursor <= gridEnd) {
      const current = startOfDay(cursor)
      const key = toDateKey(current)
      const inMonth = current.getMonth() === visibleMonth.value.getMonth() && current.getFullYear() === visibleMonth.value.getFullYear()
      cells.push({
        key,
        dayNumber: String(current.getDate()),
        inMonth,
        isToday: key === toDateKey(today),
        isPast: current.getTime() < today.getTime(),
        isLocked: false,
        items: entriesByDate.value[key] ?? []
      })

      cursor.setDate(cursor.getDate() + 1)
    }

    return cells
  })

  const timelineDays = computed<TimelineDay[]>(() => {
    const baseDays = viewMode.value === "day"
      ? [visibleDay.value]
      : Array.from({length: 7}, (_, index) => addDays(visibleWeek.value, index))

    return baseDays.map((date) => {
      const key = toDateKey(date)
      return {
        key,
        date,
        dayNumber: String(date.getDate()),
        weekdayLabel: formatWeekdayLabel(date),
        fullLabel: formatDayHeaderLabel(date),
        isToday: key === toDateKey(today),
        isPast: date.getTime() < today.getTime(),
        entries: []
      }
    })
  })

  const timelineRangeStart = computed(() => timelineDays.value[0]?.date ?? visibleDay.value)
  const timelineRangeEndExclusive = computed(() => addDays(timelineDays.value[timelineDays.value.length - 1]?.date ?? visibleDay.value, 1))

  const timelineTimedSource = computed(() => {
    return normalizedEntries.value.filter((entry) => overlapsRange(entry.startDate, entry.endDate, timelineRangeStart.value, timelineRangeEndExclusive.value))
  })

  const timelineHourBounds = computed(() => {
    if (!timelineTimedSource.value.length) {
      return {
        startMinute: DEFAULT_TIMELINE_START_HOUR * 60,
        endMinute: DEFAULT_TIMELINE_END_HOUR * 60
      }
    }

    const minMinute = Math.min(...timelineTimedSource.value.map((entry) => {
      const entryStart = entry.startDate < timelineRangeStart.value ? timelineRangeStart.value : entry.startDate
      return entryStart.getHours() * 60 + entryStart.getMinutes()
    }))
    const maxMinute = Math.max(...timelineTimedSource.value.map((entry) => {
      const clippedEnd = entry.endDate > timelineRangeEndExclusive.value ? timelineRangeEndExclusive.value : entry.endDate
      const minutes = clippedEnd.getHours() * 60 + clippedEnd.getMinutes()
      return Math.min(MINUTES_PER_DAY, minutes || MINUTES_PER_DAY)
    }))

    const startHour = Math.max(0, Math.min(DEFAULT_TIMELINE_START_HOUR, Math.floor(minMinute / 60) - 1))
    const endHour = Math.min(24, Math.max(DEFAULT_TIMELINE_END_HOUR, Math.ceil(maxMinute / 60) + 1))

    return {
      startMinute: startHour * 60,
      endMinute: endHour * 60
    }
  })

  const timelinePixelsPerMinute = computed(() => viewMode.value === "day" ? TIMELINE_DAY_PIXELS_PER_MINUTE : TIMELINE_WEEK_PIXELS_PER_MINUTE)
  const timelineBodyHeight = computed(() => (timelineHourBounds.value.endMinute - timelineHourBounds.value.startMinute) * timelinePixelsPerMinute.value)
  const currentMinuteOfDay = new Date().getHours() * 60 + new Date().getMinutes()
  const timelineNowOffset = computed(() => {
    if (currentMinuteOfDay < timelineHourBounds.value.startMinute || currentMinuteOfDay > timelineHourBounds.value.endMinute) {
      return null
    }

    return (currentMinuteOfDay - timelineHourBounds.value.startMinute) * timelinePixelsPerMinute.value
  })

  const timelineHours = computed<TimelineHour[]>(() => {
    const hours: TimelineHour[] = []
    for (let minute = timelineHourBounds.value.startMinute; minute <= timelineHourBounds.value.endMinute; minute += 60) {
      const hour = Math.floor(minute / 60)
      hours.push({
        hour,
        label: formatHourLabel(hour),
        offset: (minute - timelineHourBounds.value.startMinute) * timelinePixelsPerMinute.value
      })
    }
    return hours
  })

  const timelineDaysWithEntries = computed<TimelineDay[]>(() => {
    const days = timelineDays.value.map((day) => ({...day, entries: [] as TimelineEntry[]}))

    for (const day of days) {
      const dayStart = day.date
      const dayEndExclusive = addDays(dayStart, 1)
      const dayEntries = timelineTimedSource.value
        .filter((entry) => overlapsRange(entry.startDate, entry.endDate, dayStart, dayEndExclusive))
        .map((entry) => ({
          ...entry,
          startMinute: Math.max(
            timelineHourBounds.value.startMinute,
            entry.startDate <= dayStart ? 0 : entry.startDate.getHours() * 60 + entry.startDate.getMinutes()
          ),
          endMinute: Math.min(
            timelineHourBounds.value.endMinute,
            entry.endDate >= dayEndExclusive ? MINUTES_PER_DAY : (entry.endDate.getHours() * 60 + entry.endDate.getMinutes() || MINUTES_PER_DAY)
          ),
          startsBefore: entry.startDate < dayStart,
          endsAfter: entry.endDate > dayEndExclusive,
        }))
        .filter((entry) => entry.endMinute > entry.startMinute)

      day.entries = buildTimelineEntryLayout(dayEntries, timelineHourBounds.value.startMinute, timelinePixelsPerMinute.value)
    }

    return days
  })

  const timelineSpanItems = computed<TimelineSpanItem[]>(() => {
    const dayIndexByKey = new Map(timelineDays.value.map((day, index) => [day.key, index]))
    const rangeStart = timelineRangeStart.value
    const rangeEndExclusive = timelineRangeEndExclusive.value

    const spanSource = normalizedEntries.value
      .filter((entry) => entry.startKey !== entry.endKey && overlapsRange(entry.startDate, entry.endDate, rangeStart, rangeEndExclusive))
      .map((entry) => {
        const visibleStart = startOfDay(entry.startDate > rangeStart ? entry.startDate : rangeStart)
        const visibleEnd = startOfDay(addMinutes(entry.endDate < rangeEndExclusive ? entry.endDate : addMinutes(rangeEndExclusive, -1), -1))
        const startColumn = dayIndexByKey.get(toDateKey(visibleStart))
        const endColumn = dayIndexByKey.get(toDateKey(visibleEnd))

        if (startColumn === undefined || endColumn === undefined) {
          return null
        }

        return {
          ...entry,
          startColumn,
          endColumn,
        }
      })
      .filter((entry): entry is NormalizedCalendarEntry & {startColumn: number; endColumn: number} => entry !== null)
      .sort((left, right) => left.startColumn - right.startColumn || left.endColumn - right.endColumn)

    return buildSpanLayout(spanSource).map((entry) => ({
      ...entry,
      startsBefore: parseDate(entry.scheduledAt)?.getTime() !== undefined && parseDate(entry.scheduledAt)!.getTime() < rangeStart.getTime(),
      endsAfter: parseDate(entry.endsAt)?.getTime() !== undefined && parseDate(entry.endsAt)!.getTime() > rangeEndExclusive.getTime(),
    }))
  })

  const timelineSpanLaneCount = computed(() => {
    return Math.max(0, ...timelineSpanItems.value.map((entry) => entry.lane))
  })

  const setViewMode = (nextMode: PlannerViewMode) => {
    focusDate.value = new Date(today)
    viewMode.value = nextMode
  }

  const flexibleItems = computed(() => {
    const plannerSection = dashboard.dashboardSections?.planner
    return plannerSection?.flexibleItems.map(mapFlexiblePlannerItem) ?? []
  })

  const selectedDay = computed(() => parseDateKey(selectedDateKey.value))
  const selectedDayKey = computed(() => selectedDay.value ? toDateKey(selectedDay.value) : "")
  const selectedDayItems = computed(() => selectedDayKey.value ? entriesByDate.value[selectedDayKey.value] ?? [] : [])
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

  const selectedDayLocked = computed(() => false)

  const selectedDayCanCreate = computed(() => {
    if (!selectedDay.value) {
      return false
    }

    return selectedDay.value.getTime() >= today.getTime()
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
    dashboard.questScheduledAt = formatInstantForInput(createDate.toISOString())
    dashboard.questEndsAt = ""
    dashboard.setQuestTermMode("start-only")
    dashboard.openCreateJobDialog()
    closeDayDialog()
  }

  const openPlannerItem = async (item: CalendarEntry) => {
    await dashboard.openQuestDialog(item.questId)
  }

  return {
    viewMode,
    weekdayLabels,
    periodLabel,
    canGoPrevious,
    setViewMode,
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
    openItem: openPlannerItem,
    timelineDays: timelineDaysWithEntries,
    timelineHours,
    timelineBodyHeight,
    timelineNowOffset,
    timelineSpanItems,
    timelineSpanLaneCount,
  }
}
