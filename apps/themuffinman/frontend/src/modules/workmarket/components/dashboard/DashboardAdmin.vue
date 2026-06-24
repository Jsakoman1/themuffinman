<script setup lang="ts">
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSectionHeader from "../../../../components/ui/UiSectionHeader.vue"
import {useDashboardAdminQuestBrowser} from "../../composables/dashboard/useDashboardAdminQuestBrowser.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const {
  questSearch,
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
  openQuest
} = useDashboardAdminQuestBrowser(props.dashboard)
</script>

<template>
  <section class="stack">
    <article class="card admin-hero-card">
      <UiSectionHeader
        title="Admin control center"
        subtitle="Quest workspace for approvals, editing, status control, and term confirmations."
      />

      <UiFormActions>
        <button class="button" type="button" @click="dashboard.refreshDashboardData">Refresh data</button>
      </UiFormActions>
    </article>

    <article class="card" id="quests">
      <UiSectionHeader title="All quests" subtitle="Edit any quest from the list." />

      <UiFilterBar :columns="2">
        <label class="field">
          <span class="label">Search</span>
          <input v-model="questSearch" class="input" placeholder="Title, creator, status, award..." />
        </label>

        <label class="field">
          <span class="label">Status</span>
          <select v-model="dashboard.adminQuestStatusFilter" class="input">
            <option
              v-for="option in dashboard.questStatusFilterOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </label>
      </UiFilterBar>

      <UiFilterBar :columns="3">
        <label class="field">
          <span class="label">Audience</span>
          <select v-model="audienceFilter" class="input">
            <option v-for="option in props.dashboard.questAudienceFilterOptions" :key="option.value" :value="option.value">
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
      </UiFilterBar>

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
                      <span :class="quest.presentation.statusBadgeClass">{{ quest.presentation.statusLabel }}</span>
                      <span v-if="quest.presentation.reopenedBadgeVisible" class="badge badge--warning">Reopened</span>
                      <span v-if="quest.presentation.awaitingConfirmationBadgeVisible" class="badge badge--warning">Awaiting confirmation</span>
                    </div>
                  </td>
                  <td>{{ quest.presentation.audienceLabel }}</td>
                  <td>$ {{ quest.awardAmount }}</td>
                  <td>{{ quest.presentation.termLabel }}</td>
                  <td>{{ quest.presentation.assigneeTargetLabel }}</td>
                  <td>
                    <div class="admin-table__actions">
                      <button class="button button--secondary" type="button" @click="openQuest(quest.questNavigation)">Open</button>
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
