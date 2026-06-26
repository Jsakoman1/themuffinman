const pad = (value: number) => String(value).padStart(2, "0")

const formatLocalDateTime = (value: string) => {
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value))
}

export const formatInstantForDisplay = (value?: string | null) => {
  if (!value) {
    return "Not set"
  }

  return formatLocalDateTime(value)
}

export const formatQuestScheduleForDisplay = (scheduledAt?: string | null, endsAt?: string | null) => {
  if (!scheduledAt) {
    return "By agreement"
  }

  const startLabel = formatLocalDateTime(scheduledAt)
  if (!endsAt) {
    return startLabel
  }

  return `${startLabel} to ${formatLocalDateTime(endsAt)}`
}

export const formatQuestTermForDisplay = (
  scheduledAt?: string | null,
  endsAt?: string | null,
  termFixed?: boolean | null
) => {
  if (!scheduledAt) {
    return termFixed ? "Fixed time not set" : "By agreement"
  }

  const startLabel = formatLocalDateTime(scheduledAt)
  const endLabel = endsAt ? formatLocalDateTime(endsAt) : null

  if (termFixed) {
    return endLabel == null
      ? `Fixed for ${startLabel}`
      : `Fixed from ${startLabel} to ${endLabel}`
  }

  return endLabel == null
    ? `Proposed for ${startLabel}`
    : `Proposed from ${startLabel} to ${endLabel}`
}

export const formatInstantForInput = (value?: string | null) => {
  if (!value) {
    return ""
  }

  const date = new Date(value)
  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate())
  ].join("-") + `T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export const parseInstantFromInput = (value: string) => {
  if (!value) {
    return null
  }

  return new Date(value).toISOString()
}
