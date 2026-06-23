const padTwoDigits = (value: number) => String(value).padStart(2, "0")

export const toDateKey = (date: Date) => {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`
}

export const startOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth(), 1)

export const endOfMonth = (date: Date) => new Date(date.getFullYear(), date.getMonth() + 1, 0)

export const startOfWeek = (date: Date) => {
  const copy = new Date(date)
  copy.setHours(0, 0, 0, 0)
  const day = copy.getDay() === 0 ? 7 : copy.getDay()
  copy.setDate(copy.getDate() - day + 1)
  return copy
}

export const parseDate = (value: string | null | undefined) => {
  if (!value) {
    return null
  }

  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

export const parseDateKey = (value: string | null | undefined) => {
  if (!value) {
    return null
  }

  const [year, month, day] = value.split("-").map((part) => Number(part))
  if (!year || !month || !day) {
    return null
  }

  const date = new Date(year, month - 1, day)
  date.setHours(0, 0, 0, 0)
  return date
}

export const formatTimeLabel = (startValue: string | null | undefined, endValue?: string | null | undefined) => {
  const startDate = parseDate(startValue)
  if (!startDate) {
    return "All day"
  }

  const startLabel = new Intl.DateTimeFormat("en-GB", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(startDate)

  const endDate = parseDate(endValue)
  if (!endDate) {
    return startLabel
  }

  const endLabel = new Intl.DateTimeFormat("en-GB", {
    hour: "2-digit",
    minute: "2-digit"
  }).format(endDate)

  return `${startLabel}-${endLabel}`
}
