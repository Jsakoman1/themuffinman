export const normalizeSearchQuery = (value: string) => {
  return value.trim().replace(/^@+/, "")
}

export const hasSearchQuery = (value: string) => {
  return normalizeSearchQuery(value).length >= 2
}
