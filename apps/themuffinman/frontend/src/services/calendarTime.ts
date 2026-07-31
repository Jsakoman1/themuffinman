export type CalendarDay = {key: string; label: string; weekday: string}

const partsFor = (value: string | Date, timezone: string) => Object.fromEntries(new Intl.DateTimeFormat("en-CA", {
  timeZone: timezone,
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
  hourCycle: "h23"
}).formatToParts(new Date(value)).filter(part => part.type !== "literal").map(part => [part.type, part.value]))

const keyFor = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`

export const calendarDayKey = (value: string | Date, timezone: string) => {
  const parts = partsFor(value, timezone)
  return `${parts.year}-${parts.month}-${parts.day}`
}

export const calendarMinutes = (value: string | Date, timezone: string) => {
  const parts = partsFor(value, timezone)
  return Number(parts.hour) * 60 + Number(parts.minute)
}

export const calendarDays = (cursor: Date, mode: "day" | "week" | "month", timezone: string): CalendarDay[] => {
  const start = new Date(cursor.getFullYear(), cursor.getMonth(), cursor.getDate())
  if (mode === "week") start.setDate(start.getDate() - start.getDay())
  if (mode === "month") start.setDate(1)
  const count = mode === "month" ? new Date(start.getFullYear(), start.getMonth() + 1, 0).getDate() : mode === "week" ? 7 : 1
  return Array.from({length: count}, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const key = keyFor(date)
    return {
      key,
      label: new Intl.DateTimeFormat(undefined, {timeZone: timezone, day: "numeric", month: "short"}).format(new Date(`${key}T12:00:00Z`)),
      weekday: new Intl.DateTimeFormat(undefined, {timeZone: timezone, weekday: "short"}).format(new Date(`${key}T12:00:00Z`))
    }
  })
}
