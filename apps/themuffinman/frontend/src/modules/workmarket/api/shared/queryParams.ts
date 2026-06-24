type QueryValue = string | number | boolean | null | undefined

export const buildQueryParams = <T extends object>(values: T) => {
  const params: Record<string, string | number | boolean> = {}

  for (const [key, value] of Object.entries(values as Record<string, QueryValue>)) {
    if (value === undefined || value === null || value === "") {
      continue
    }

    params[key] = value
  }

  return params
}
