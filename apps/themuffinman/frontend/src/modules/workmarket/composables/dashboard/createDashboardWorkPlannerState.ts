import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {formatInstantForInput} from "../../../../shared/questSchedule.ts"
import type {NavigationTarget} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../useQuestDashboard.ts"
import {
  endOfMonth,
  parseDate,
  parseDateKey,
  startOfMonth,
  startOfWeek,
  toDateKey
} from "../../../../lib/dashboardCalendar.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

type CalendarEntry = {
  id: string
  questId: number
  title: string
  navigation: NavigationTarget | null
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

const isPlannerKind = (value: string): value is CalendarEntry["kind"] => {
  return value === "incoming" || value === "outgoing"
}

export const createDashboardWorkPlannerState = (dashboard: QuestDashboard) => {
  const router = useRouter()

  const mapPlannerItem = (
    item: NonNullable<QuestDashboard["dashboardSections"]>["planner"]["scheduledItems"][number]
  ): CalendarEntry | null => {
    const scheduled = parseDate(item.scheduledAt)
    if (!scheduled || !item.dateKey) {
      return null
    }

    return {
      id: item.id,
      questId: item.questId,
      title: item.title,
      navigation: "navigation" in item ? item.navigation : null,
      scheduledAt: item.scheduledAt,
      endsAt: item.endsAt,
      timeLabel: item.timeLabel,
      minuteOfDay: scheduled.getHours() * 60 + scheduled.getMinutes(),
      dateKey: item.dateKey,
      kind: isPlannerKind(item.kind) ? item.kind : "incoming",
      hasRange: item.hasRange
    }
  }

  const mapFlexiblePlannerItem = (
    item: NonNullable<QuestDashboard["dashboardSections"]>["planner"]["flexibleItems"][number]
  ): CalendarEntry => ({
    id: item.id,
    questId: item.questId,
    title: item.title,
    navigation: "navigation" in item ? item.navigation : null,
    scheduledAt: item.scheduledAt,
    endsAt: item.endsAt,
    timeLabel: item.timeLabel,
    minuteOfDay: 0,
    dateKey: item.dateKey ?? "flexible",
    kind: isPlannerKind(item.kind) ? item.kind : "incoming",
    hasRange: item.hasRange
  })

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const accountCreatedAt = computed(() => {
    const createdAt = parseDate(dashboard.accountCreatedAt)
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
    const plannerSection = dashboard.dashboardSections?.planner
    return plannerSection?.scheduledItems
      .map(mapPlannerItem)
      .filter((item): item is CalendarEntry => item !== null)
      .sort((left, right) => left.minuteOfDay - right.minuteOfDay) ?? []
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
    dashboard.questScheduledAt = formatInstantForInput(createDate.toISOString())
    dashboard.questEndsAt = ""
    dashboard.setQuestTermMode("start-only")
    dashboard.openCreateJobDialog()
    closeDayDialog()
  }

  const openItem = async (navigation: NavigationTarget | null) => {
    await router.push(routeForNavigationTarget(navigation))
  }

  return {
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
  }
}
