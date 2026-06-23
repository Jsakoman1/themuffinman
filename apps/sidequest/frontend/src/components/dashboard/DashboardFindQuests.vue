<script setup lang="ts">
import {computed, ref} from "vue"
import DashboardSectionHeader from "./DashboardSectionHeader.vue"
import {sidequestApi} from "../../api/sidequestApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import type {QuestAudience} from "../../shared/sidequestDomain.ts"
import {normalizeSearchQuery} from "../../lib/searchQuery.ts"
import {useQuestSearchResults} from "../../composables/useQuestSearchResults.ts"
import UiPagination from "../ui/UiPagination.vue"
import {richTextToPlainText} from "../../shared/richText.ts"

const props = withDefaults(defineProps<{
  dashboard: QuestDashboard
  showHeader?: boolean
  boxed?: boolean
}>(), {
  showHeader: true,
  boxed: true,
})

const searchQuery = ref("")
const sortMode = ref<"recommended" | "newest" | "highest">("recommended")
const photoOnly = ref(false)
const scheduledOnly = ref(false)
const audienceFilter = ref<QuestAudience | "ALL">("ALL")
const dateFrom = ref("")
const dateTo = ref("")
const itemsPerPage = 10
const {results: questResults, loadQuests, watchAndReload} = useQuestSearchResults(itemsPerPage, (page) => sidequestApi.getQuestPreset("AVAILABLE", {
  q: normalizeSearchQuery(searchQuery.value),
  audience: audienceFilter.value === "ALL" ? null : audienceFilter.value,
  dateFrom: dateFrom.value || null,
  dateTo: dateTo.value || null,
  withImages: photoOnly.value || undefined,
  scheduledOnly: scheduledOnly.value || undefined,
  sort: sortMode.value,
  page,
  size: itemsPerPage
}))

const pagedQuests = computed(() => questResults.items.value)
const totalItems = questResults.totalItems
const totalPages = questResults.totalPages
const currentPage = questResults.currentPage
const isLoading = questResults.isLoading
const pageStart = questResults.pageStart
const pageEnd = questResults.pageEnd
const hasPreviousPage = questResults.hasPreviousPage
const hasNextPage = questResults.hasNextPage

watchAndReload([searchQuery, sortMode, photoOnly, scheduledOnly, audienceFilter, dateFrom, dateTo])

const previousPage = () => {
  void questResults.previousPage(loadQuests)
}

const nextPage = () => {
  void questResults.nextPage(loadQuests)
}

const previewText = (value: string) => {
  return richTextToPlainText(value).replace(/\s+/g, " ").trim()
}
</script>

<template>
  <section class="stack">
    <div :class="['dashboard-work-panel', 'dashboard-work-panel--find', { card: props.boxed, 'dashboard-work-panel--dialog': !props.boxed }]">
      <DashboardSectionHeader v-if="props.showHeader" title="Find work" subtitle="" />

      <div class="dashboard-find-work-browser">
        <div class="dashboard-find-work-browser__toolbar">
          <label class="field dashboard-find-work__search">
            <span class="label">Search</span>
            <input v-model="searchQuery" class="input" placeholder="Title, person, keyword" />
          </label>

          <label class="field dashboard-find-work__sort">
            <span class="label">Sort</span>
            <select v-model="sortMode" class="input">
              <option value="recommended">Recommended</option>
              <option value="newest">Soonest</option>
              <option value="highest">Highest award</option>
            </select>
          </label>

          <label class="field">
            <span class="label">Audience</span>
            <select v-model="audienceFilter" class="input">
              <option value="ALL">All</option>
              <option v-for="option in props.dashboard.questAudienceOptions" :key="option.value" :value="option.value">
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
        </div>

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
                @click="dashboard.openQuestDialog(quest.id)"
              >
                <div class="find-work-row__main">
                  <div class="find-work-row__titlebar">
                    <strong class="find-work-row__title">{{ quest.title }}</strong>
                    <div class="find-work-row__badges">
                      <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
                      <span v-if="quest.assigneeTarget === null || quest.assigneeTarget > 1" class="badge badge--secondary">
                        {{ quest.assigneeTarget === null ? "Unlimited" : `${quest.assigneeTarget} spots` }}
                      </span>
                    </div>
                  </div>

                  <div class="find-work-row__meta">
                    <span>{{ quest.creatorUsername }}</span>
                    <span>{{ dashboard.formatQuestTermLabel(quest) }}</span>
                    <span>{{ quest.audience === "EVERYONE" ? "Everyone" : "Circles" }}</span>
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
