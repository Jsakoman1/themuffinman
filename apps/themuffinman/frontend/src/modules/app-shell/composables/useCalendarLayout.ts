import type {CalendarEvent} from "../../../contracts/index.ts"
import {calendarDayKey, calendarMinutes, type CalendarDay} from "../../../services/calendarTime.ts"

export const useCalendarLayout = (timezone: () => string) => {
  const eventsForDay = (events: CalendarEvent[], day: CalendarDay) => events.filter(event => calendarDayKey(event.startsAt, timezone()) === day.key)
  const eventStyle = (event: CalendarEvent) => {
    const top = calendarMinutes(event.startsAt, timezone())
    const end = calendarMinutes(event.endsAt, timezone())
    const duration = Math.max(30, end >= top ? end - top : 24 * 60 - top)
    return {top: `${top}px`, height: `${Math.min(24 * 60 - top, duration)}px`}
  }
  const layoutTimedEvents = (events: CalendarEvent[]) => {
    const sorted = [...events].filter(event => !event.allDay).sort((left, right) => calendarMinutes(left.startsAt, timezone()) - calendarMinutes(right.startsAt, timezone()))
    const laidOut: Array<{event: CalendarEvent; column: number; columns: number}> = []
    let group: typeof sorted = []
    let groupEnd = -1
    const finishGroup = () => {
      const activeEnds: number[] = []
      const placements = group.map(event => {
        const start = calendarMinutes(event.startsAt, timezone())
        const end = Math.max(start + 1, calendarMinutes(event.endsAt, timezone()))
        activeEnds.forEach((activeEnd, index) => { if (activeEnd <= start) activeEnds[index] = -1 })
        const availableColumn = activeEnds.findIndex(activeEnd => activeEnd < 0)
        const column = availableColumn < 0 ? activeEnds.length : availableColumn
        activeEnds[column] = end
        return {event, column}
      })
      const columns = Math.max(1, activeEnds.length)
      laidOut.push(...placements.map(placement => ({...placement, columns})))
      group = []
      groupEnd = -1
    }
    sorted.forEach(event => {
      const start = calendarMinutes(event.startsAt, timezone())
      const end = Math.max(start + 1, calendarMinutes(event.endsAt, timezone()))
      if (group.length && start >= groupEnd) finishGroup()
      group.push(event)
      groupEnd = Math.max(groupEnd, end)
    })
    if (group.length) finishGroup()
    return laidOut
  }

  return {eventsForDay, eventStyle, layoutTimedEvents}
}
