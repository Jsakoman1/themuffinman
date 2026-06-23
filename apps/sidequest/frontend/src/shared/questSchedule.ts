const pad = (value: number) => String(value).padStart(2, "0")

export const formatInstantForDisplay = (value?: string | null) => {
  if (!value) {
    return "Not set"
  }

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value))
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

export const formatQuestTerm = (scheduledAt: string | null | undefined, endsAt: string | null | undefined, termFixed: boolean) => {
  if (!scheduledAt) {
    return termFixed ? "Fixed time not set" : "By agreement"
  }

  const startLabel = formatInstantForDisplay(scheduledAt)
  const endLabel = endsAt ? formatInstantForDisplay(endsAt) : null

  if (termFixed) {
    return endLabel ? `Fixed from ${startLabel} to ${endLabel}` : `Fixed for ${startLabel}`
  }

  return endLabel ? `Proposed from ${startLabel} to ${endLabel}` : `Proposed for ${startLabel}`
}
