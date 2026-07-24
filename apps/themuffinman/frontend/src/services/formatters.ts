const defaultDateTimeOptions: Intl.DateTimeFormatOptions = {
  month: "short",
  day: "numeric",
  hour: "numeric",
  minute: "2-digit",
}

const formatIntl = (value: Date, options: Intl.DateTimeFormatOptions): string =>
  new Intl.DateTimeFormat(undefined, options).format(value)

export const formatDateTime = (
  value: string | null | undefined,
  fallback = "Flexible",
  options: Intl.DateTimeFormatOptions = defaultDateTimeOptions,
): string => {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? fallback : formatIntl(date, options)
}

export const formatDate = (value: string | null | undefined, fallback = "No date"): string =>
  formatDateTime(value, fallback, {month: "short", day: "numeric", year: "numeric"})

export const formatCalendarTitle = (value: Date): string =>
  formatIntl(value, {month: "long", year: "numeric"})

export const formatCalendarDay = (value: Date): string =>
  formatIntl(value, {weekday: "short", month: "short", day: "numeric"})

export const formatTime = (value: string | null | undefined, fallback = ""): string =>
  formatDateTime(value, fallback, {hour: "numeric", minute: "2-digit"})

export const formatNumber = (value: number, locale?: string): string =>
  new Intl.NumberFormat(locale).format(value)

export const formatCurrency = (value: number | null | undefined, currency = "EUR", fallback = "Not specified"): string =>
  value == null ? fallback : new Intl.NumberFormat(undefined, {style: "currency", currency}).format(value)
