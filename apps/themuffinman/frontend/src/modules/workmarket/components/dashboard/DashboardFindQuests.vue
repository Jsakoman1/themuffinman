<script setup lang="ts">
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiListItem from "../../../../components/ui/UiListItem.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
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
</script>

<template>
  <section class="surface-stack">
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
  </section>
</template>
