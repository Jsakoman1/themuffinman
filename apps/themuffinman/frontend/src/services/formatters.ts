const defaultDateTimeOptions: Intl.DateTimeFormatOptions = {
  month: "short",
  day: "numeric",
  hour: "numeric",
  minute: "2-digit",
}

export const formatDateTime = (
  value: string | null | undefined,
  fallback = "Flexible",
  options: Intl.DateTimeFormatOptions = defaultDateTimeOptions,
): string => {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? fallback : new Intl.DateTimeFormat(undefined, options).format(date)
}
