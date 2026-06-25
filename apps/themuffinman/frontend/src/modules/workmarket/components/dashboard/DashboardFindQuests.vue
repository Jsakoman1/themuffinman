<script setup lang="ts">
import {computed} from "vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiListItem from "../../../../components/ui/UiListItem.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import {useDashboardFindQuestsBrowser} from "../../composables/dashboard/useDashboardFindQuestsBrowser.ts"
import type {DashboardFindQuestsFacade} from "../../composables/dashboard/dashboardFacades.ts"

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
  scheduledOnly,
  audienceFilter,
  dateFrom,
  dateTo,
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
  previewText,
  openQuest
} = useDashboardFindQuestsBrowser()

const activeFilters = computed(() => {
  const items: string[] = []
  if (photoOnly.value) {
    items.push("Photos only")
  }
  if (scheduledOnly.value) {
    items.push("Scheduled only")
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
  return items
})
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
          <UiFilterBar :columns="5">
            <UiFieldGroup label="Search" field-class="dashboard-find-work__search">
              <input v-model="searchQuery" class="input" placeholder="Title, person, keyword" />
            </UiFieldGroup>

            <UiFieldGroup label="Sort" field-class="dashboard-find-work__sort">
              <select v-model="sortMode" class="input">
                <option v-for="option in props.dashboard.questSortOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </UiFieldGroup>

            <UiFieldGroup label="Audience">
              <select v-model="audienceFilter" class="input">
                <option v-for="option in props.dashboard.questAudienceFilterOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </UiFieldGroup>

            <UiFieldGroup label="From">
              <input v-model="dateFrom" class="input" type="date" />
            </UiFieldGroup>

            <UiFieldGroup label="To">
              <input v-model="dateTo" class="input" type="date" />
            </UiFieldGroup>
          </UiFilterBar>

          <div class="dashboard-find-work__filters">
            <button class="segment" :class="{ 'segment--active': !photoOnly }" type="button" @click="photoOnly = false">All</button>
            <button class="segment" :class="{ 'segment--active': photoOnly }" type="button" @click="photoOnly = true">Photos</button>
            <button class="segment" :class="{ 'segment--active': !scheduledOnly }" type="button" @click="scheduledOnly = false">Any time</button>
            <button class="segment" :class="{ 'segment--active': scheduledOnly }" type="button" @click="scheduledOnly = true">Scheduled</button>
          </div>

          <div class="dashboard-find-work-browser__summary">
            <strong>{{ isLoading ? "Loading" : `${pageStart}-${pageEnd}` }}</strong>
            <span class="muted">of {{ totalItems }} open jobs</span>
          </div>

          <div v-if="isLoading" class="empty-state">Loading quests...</div>

          <template v-else>
            <div v-if="!pagedQuests.length" class="empty-state">No matching open jobs.</div>

            <template v-else>
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
                    <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
                    <span v-if="quest.presentation.assigneeTargetVisible" class="badge badge--secondary">
                      {{ quest.presentation.assigneeTargetLabel }}
                    </span>
                  </template>

                  <template #meta>
                    <span>{{ quest.creatorUsername }}</span>
                    <span>{{ quest.presentation.termLabel }}</span>
                    <span>{{ quest.presentation.audienceLabel }}</span>
                    <span v-if="quest.images?.length">{{ quest.images.length }} photo{{ quest.images.length === 1 ? "" : "s" }}</span>
                  </template>

                  <p v-if="previewText(quest.description)" class="find-work-row__description">
                    {{ previewText(quest.description) }}
                  </p>
                </UiListItem>
              </div>

              <UiPagination
                v-if="totalPages > 1"
                class="dashboard-find-work__pagination--bottom"
                :label="`Page ${currentPage} of ${totalPages}`"
                :has-previous="hasPreviousPage"
                :has-next="hasNextPage"
                @previous="previousPage"
                @next="nextPage"
              />
            </template>
          </template>
        </div>
      </UiSurfaceSection>
    </template>

    <template #side>
      <DetailUtilitySection title="Summary" tone="summary">
        <div class="quest-overview-aside quest-overview-aside--compact">
          <div class="quest-overview-aside__row">
            <span class="quest-overview-aside__label">Open jobs</span>
            <span class="quest-overview-aside__value">{{ totalItems }}</span>
          </div>
          <div class="quest-overview-aside__row">
            <span class="quest-overview-aside__label">Visible now</span>
            <span class="quest-overview-aside__value">{{ pagedQuests.length }}</span>
          </div>
          <div class="quest-overview-aside__row">
            <span class="quest-overview-aside__label">Page</span>
            <span class="quest-overview-aside__value">{{ currentPage }} / {{ totalPages || 1 }}</span>
          </div>
        </div>
      </DetailUtilitySection>

      <DetailUtilitySection title="Filters">
        <div v-if="activeFilters.length" class="ui-utility-badge-list">
          <span v-for="filterLabel in activeFilters" :key="filterLabel" class="badge badge--secondary">
            {{ filterLabel }}
          </span>
        </div>
        <div v-else class="empty-state empty-state--soft">No extra filters.</div>
      </DetailUtilitySection>
    </template>
  </DetailDialogFrame>
</template>
