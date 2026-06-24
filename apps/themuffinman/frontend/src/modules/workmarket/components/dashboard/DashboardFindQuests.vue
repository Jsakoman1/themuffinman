<script setup lang="ts">
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSectionHeader from "../../../../components/ui/UiSectionHeader.vue"
import {useDashboardFindQuestsBrowser} from "../../composables/dashboard/useDashboardFindQuestsBrowser.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
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
  <section class="stack">
    <div :class="['dashboard-work-panel', 'dashboard-work-panel--find', { card: props.boxed, 'dashboard-work-panel--dialog': !props.boxed }]">
      <UiSectionHeader v-if="props.showHeader" title="Find work" />

      <div class="dashboard-find-work-browser">
        <UiFilterBar :columns="5">
          <label class="field dashboard-find-work__search">
            <span class="label">Search</span>
            <input v-model="searchQuery" class="input" placeholder="Title, person, keyword" />
          </label>

          <label class="field dashboard-find-work__sort">
            <span class="label">Sort</span>
            <select v-model="sortMode" class="input">
              <option v-for="option in props.dashboard.questSortOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label class="field">
            <span class="label">Audience</span>
            <select v-model="audienceFilter" class="input">
              <option v-for="option in props.dashboard.questAudienceFilterOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>

          <label class="field">
            <span class="label">From</span>
            <input v-model="dateFrom" class="input" type="date" />
          </label>

          <label class="field">
            <span class="label">To</span>
            <input v-model="dateTo" class="input" type="date" />
          </label>
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
              <button
                v-for="quest in pagedQuests"
                :key="quest.id"
                type="button"
                class="find-work-row"
                @click="openQuest(quest.questNavigation)"
              >
                <div class="find-work-row__main">
                  <div class="find-work-row__titlebar">
                    <strong class="find-work-row__title">{{ quest.title }}</strong>
                    <div class="find-work-row__badges">
                      <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
                      <span v-if="quest.presentation.assigneeTargetVisible" class="badge badge--secondary">
                        {{ quest.presentation.assigneeTargetLabel }}
                      </span>
                    </div>
                  </div>

                  <div class="find-work-row__meta">
                    <span>{{ quest.creatorUsername }}</span>
                    <span>{{ quest.presentation.termLabel }}</span>
                    <span>{{ quest.presentation.audienceLabel }}</span>
                    <span v-if="quest.images?.length">{{ quest.images.length }} photo{{ quest.images.length === 1 ? "" : "s" }}</span>
                  </div>

                  <p v-if="previewText(quest.description)" class="find-work-row__description">
                    {{ previewText(quest.description) }}
                  </p>
                </div>
              </button>
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
    </div>
  </section>
</template>
