import {computed, type Ref} from "vue"
import type {WorkmarketOptions} from "../../api/workmarketApi.ts"

export const createDashboardOptionState = (dashboardOptions: Ref<WorkmarketOptions | null>) => {
  const questStatusFilterOptions = computed(() => dashboardOptions.value?.questStatusFilters ?? [])
  const questApplicationStatusFilterOptions = computed(() => dashboardOptions.value?.questApplicationStatusFilters ?? [])
  const questStatusOptions = computed(() => dashboardOptions.value?.questStatuses ?? [])
  const questAudienceOptions = computed(() => dashboardOptions.value?.questAudiences ?? [])
  const questAudienceFilterOptions = computed(() => dashboardOptions.value?.questAudienceFilters ?? [])
  const questSortOptions = computed(() => dashboardOptions.value?.questSortOptions ?? [])

  return {
    questStatusFilterOptions,
    questApplicationStatusFilterOptions,
    questStatusOptions,
    questAudienceOptions,
    questAudienceFilterOptions,
    questSortOptions
  }
}
