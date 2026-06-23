<script setup lang="ts">
import {computed, ref} from "vue"
import DashboardSectionHeader from "./DashboardSectionHeader.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {sidequestApi} from "../../api/sidequestApi.ts"
import type {QuestAudience} from "../../shared/sidequestDomain.ts"
import {normalizeSearchQuery} from "../../lib/searchQuery.ts"
import {useQuestSearchResults} from "../../composables/useQuestSearchResults.ts"
import UiPagination from "../ui/UiPagination.vue"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const questSearch = ref("")
const audienceFilter = ref<QuestAudience | "ALL">("ALL")
const dateFrom = ref("")
const dateTo = ref("")
const itemsPerPage = 8
const {results: questResults, loadQuests, watchAndReload} = useQuestSearchResults(itemsPerPage, (page) => sidequestApi.searchQuests({
  q: normalizeSearchQuery(questSearch.value),
  status: props.dashboard.adminQuestStatusFilter === "ALL" ? null : props.dashboard.adminQuestStatusFilter,
  audience: audienceFilter.value === "ALL" ? null : audienceFilter.value,
  dateFrom: dateFrom.value || null,
  dateTo: dateTo.value || null,
  sort: "recommended",
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

watchAndReload([questSearch, () => props.dashboard.adminQuestStatusFilter, audienceFilter, dateFrom, dateTo])

const previousPage = () => {
  void questResults.previousPage(loadQuests)
}

const nextPage = () => {
  void questResults.nextPage(loadQuests)
}
</script>

<template>
  <section class="stack">
    <article class="card admin-hero-card">
      <DashboardSectionHeader
        title="Admin control center"
        subtitle="Quest workspace for approvals, editing, status control, and term confirmations."
      />

      <div class="button-row">
        <button class="button" type="button" @click="dashboard.refreshDashboardData">Refresh data</button>
      </div>
    </article>

    <article class="card" id="quests">
      <DashboardSectionHeader title="All quests" subtitle="Edit any quest from the list." />

      <div class="grid grid--two admin-toolbar">
        <label class="field">
          <span class="label">Search</span>
          <input v-model="questSearch" class="input" placeholder="Title, creator, status, award..." />
        </label>

        <label class="field">
          <span class="label">Status</span>
          <select v-model="dashboard.adminQuestStatusFilter" class="input">
            <option
              v-for="option in dashboard.questStatusOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
      </div>

      <div class="grid grid--three admin-toolbar admin-toolbar--filters">
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
          <span class="label">From date</span>
          <input v-model="dateFrom" class="input" type="date" />
        </label>

        <label class="field">
          <span class="label">To date</span>
          <input v-model="dateTo" class="input" type="date" />
        </label>
      </div>

      <div v-if="isLoading" class="empty-state mt-4">
        Loading quests...
      </div>

      <template v-else>
        <div v-if="!pagedQuests.length" class="empty-state mt-4">
          No quests in this group.
        </div>

        <template v-else>
          <UiPagination class="mt-4" :label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`" :has-previous="hasPreviousPage" :has-next="hasNextPage" @previous="previousPage" @next="nextPage" />

          <div class="admin-table-shell mt-4">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Creator</th>
                  <th>Status</th>
                  <th>Audience</th>
                  <th>Award</th>
                  <th>Term</th>
                  <th>Workers</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="quest in pagedQuests"
                  :key="quest.id"
                  :class="{ 'ui-pulse': dashboard.successPulseTarget === `quest-${quest.id}` }"
                >
                  <td>
                    <div class="stack">
                      <strong>{{ quest.title }}</strong>
                      <span class="muted text-clamp">{{ quest.description }}</span>
                    </div>
                  </td>
                  <td>{{ quest.creatorUsername }}</td>
                  <td>
                    <div class="admin-table__badges">
                      <span :class="dashboard.statusBadgeClass(quest.status)">{{ dashboard.formatStatus(quest.status) }}</span>
                      <span v-if="quest.reopenedAt && quest.status === 'OPEN'" class="badge badge--warning">Reopened</span>
                      <span v-if="quest.status === 'WAITING_CONFIRMATION'" class="badge badge--warning">Awaiting confirmation</span>
                    </div>
                  </td>
                  <td>{{ quest.audience === "EVERYONE" ? "Everyone" : "Circles" }}</td>
                  <td>$ {{ quest.awardAmount }}</td>
                  <td>{{ dashboard.formatQuestTermLabel(quest) }}</td>
                  <td>{{ quest.assigneeTarget === null ? "Unlimited" : quest.assigneeTarget }}</td>
                  <td>
                    <div class="admin-table__actions">
                      <button class="button button--secondary" type="button" @click="dashboard.openQuestDialog(quest.id)">Open</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <UiPagination class="dashboard-find-work__pagination--bottom mt-4" :label="`Page ${currentPage} of ${totalPages}`" :has-previous="hasPreviousPage" :has-next="hasNextPage" @previous="previousPage" @next="nextPage" />
        </template>
      </template>
    </article>
  </section>
</template>
