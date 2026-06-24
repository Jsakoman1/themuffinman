export const ALL_FILTER_VALUE = "ALL" as const

export const toOptionalFilterValue = <T>(value: T | typeof ALL_FILTER_VALUE): T | null => {
  return value === ALL_FILTER_VALUE ? null : value
}
