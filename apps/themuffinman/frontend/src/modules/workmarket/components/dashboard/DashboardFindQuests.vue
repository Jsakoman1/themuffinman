<script setup lang="ts">
import {computed} from "vue"
import UiChoiceChips from "../../../../components/ui/UiChoiceChips.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiListItem from "../../../../components/ui/UiListItem.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import {useDashboardFindQuestsBrowser} from "../../composables/dashboard/useDashboardFindQuestsBrowser.ts"
import type {DashboardFindQuestsFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"

const props = withDefaults(defineProps<{
  dashboard: DashboardFindQuestsFacade
  showHeader?: boolean
  boxed?: boolean
}>(), {
  showHeader: true,
  boxed: true,
})

const {
  searchQuery,
  sortMode,
  photoOnly,
  flexibleOnly,
  nearbyOnly,
  audienceFilter,
  dateFrom,
  dateTo,
  hasViewerLocation,
  radiusKm,
  pagedQuests,
  totalItems,
  totalPages,
  currentPage,
  isLoading,
  pageStart,
  pageEnd,
  hasPreviousPage,
  hasNextPage,
  previousPage,
  nextPage,
  openQuest
} = useDashboardFindQuestsBrowser(props.dashboard)

const activeFilters = computed(() => {
  const items: string[] = []
  if (photoOnly.value) {
    items.push("Photos only")
  }
  if (flexibleOnly.value) {
    items.push("Only flexible")
  }
  if (audienceFilter.value !== "ALL") {
    const selected = props.dashboard.questAudienceFilterOptions.find((option) => option.value === audienceFilter.value)
    if (selected) {
      items.push(selected.label)
    }
  }
  if (dateFrom.value) {
    items.push(`From ${dateFrom.value}`)
  }
  if (dateTo.value) {
    items.push(`To ${dateTo.value}`)
  }
  if (nearbyOnly.value && radiusKm.value) {
    items.push(`Nearby ${radiusKm.value} km`)
  }
  return items
})

const visibleSortOptions = computed(() => props.dashboard.questSortOptions)
</script>

<template>
  <DetailDialogFrame>
    <template #main>
      <UiSurfaceSection
        :class="['dashboard-work-panel', 'dashboard-work-panel--find', { card: props.boxed }]"
        :soft="true"
        :title="props.showHeader ? 'Find work' : ''"
        :compact="false"
      >
        <div class="dashboard-find-work-browser">
          <div v-if="isLoading" class="empty-state">Loading quests...</div>

          <template v-else>
            <div v-if="!pagedQuests.length" class="empty-state">No matching open jobs.</div>

            <template v-else>
              <div class="dashboard-find-work-browser__results">
                <div class="find-work-list">
                  <UiListItem
                    v-for="quest in pagedQuests"
                    :key="quest.id"
                    tag="button"
                    clickable
                    @click="openQuest(quest.questNavigation)"
                  >
                    <template #header>
                      <strong class="find-work-row__title">{{ quest.title }}</strong>
                    </template>

                    <template #badges>
                      <span class="find-work-row__award">$ {{ quest.awardAmount }}</span>
                    </template>

                    <template #meta>
                      <span class="find-work-row__meta-primary">{{ formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</span>
                      <span v-if="quest.presentation.locationLabel">{{ quest.presentation.locationLabel }}</span>
                      <span>{{ quest.creatorUsername }}</span>
                      <span v-if="quest.images?.length">{{ quest.images.length }} photo{{ quest.images.length === 1 ? "" : "s" }}</span>
                    </template>
                  </UiListItem>
                </div>
              </div>

              <div class="dashboard-find-work-browser__footer">
                <UiPagination
                  v-if="totalPages > 1"
                  class="dashboard-find-work__pagination--bottom"
                  :label="`Page ${currentPage} of ${totalPages}`"
                  :has-previous="hasPreviousPage"
                  :has-next="hasNextPage"
                  @previous="previousPage"
                  @next="nextPage"
                />

                <div class="dashboard-find-work-browser__footnote">
                  <span>{{ isLoading ? "Loading" : `${pageStart}-${pageEnd}` }}</span>
                  <span>of {{ totalItems }} open jobs</span>
                </div>
              </div>
            </template>
          </template>
        </div>
      </UiSurfaceSection>
    </template>

    <template #side>
      <DetailUtilitySection title="Search" tone="actions">
        <UiFieldGroup label="Find job" field-class="dashboard-find-work__search">
          <input v-model="searchQuery" class="input" placeholder="Title or keyword" />
        </UiFieldGroup>
      </DetailUtilitySection>

      <DetailUtilitySection title="Sort" tone="actions">
        <div class="dashboard-find-work__sort-panel">
          <UiFieldGroup label="Sort" field-class="dashboard-find-work__sort">
            <select v-model="sortMode" class="input">
              <option v-for="option in visibleSortOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </UiFieldGroup>
        </div>
      </DetailUtilitySection>

      <DetailUtilitySection title="Filters" tone="actions">
        <div class="dashboard-find-work__filter-panel">
          <UiFieldGroup label="From who">
            <select v-model="audienceFilter" class="input">
              <option v-for="option in props.dashboard.questAudienceFilterOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </UiFieldGroup>

          <label class="checkbox-field dashboard-find-work__checkbox">
            <input v-model="photoOnly" type="checkbox" />
            <span>Only with photos</span>
          </label>

          <label class="checkbox-field dashboard-find-work__checkbox">
            <input v-model="flexibleOnly" type="checkbox" />
            <span>Only flexible</span>
          </label>

          <label v-if="hasViewerLocation" class="checkbox-field dashboard-find-work__checkbox">
            <input v-model="nearbyOnly" type="checkbox" />
            <span>Only nearby</span>
          </label>

          <UiFieldGroup v-if="nearbyOnly && hasViewerLocation" label="Distance">
            <UiChoiceChips
              :model-value="String(radiusKm)"
              :options="(props.dashboard.questSearchDefaults?.radiusOptionsKm ?? [5, 10, 20, 30]).map((value) => ({value: String(value), label: `${value} km`}))"
              @update:model-value="radiusKm = Number($event)"
            />
          </UiFieldGroup>

          <div class="dashboard-find-work__date-fields dashboard-find-work__date-fields--inline">
            <UiFieldGroup label="From date">
              <input v-model="dateFrom" class="input" type="date" />
            </UiFieldGroup>

            <UiFieldGroup label="To date">
              <input v-model="dateTo" class="input" type="date" />
            </UiFieldGroup>
          </div>

          <div v-if="activeFilters.length" class="ui-utility-badge-list">
            <span v-for="filterLabel in activeFilters" :key="filterLabel" class="badge badge--secondary">
              {{ filterLabel }}
            </span>
          </div>
        </div>
      </DetailUtilitySection>
    </template>
  </DetailDialogFrame>
</template>
