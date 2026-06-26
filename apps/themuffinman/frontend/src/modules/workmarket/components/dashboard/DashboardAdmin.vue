<script setup lang="ts">
import UiAdminTableShell from "../../../../components/ui/UiAdminTableShell.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"
import {useDashboardAdminQuestBrowser} from "../../composables/dashboard/useDashboardAdminQuestBrowser.ts"
import type {DashboardAdminFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardAdminFacade
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
  loadQuests,
  previousPage,
  nextPage,
  openQuest
} = useDashboardAdminQuestBrowser(props.dashboard)

const handleDeleteQuest = async (questId: number) => {
  const targetPage = pagedQuests.value.length === 1 && currentPage.value > 1
    ? currentPage.value - 1
    : currentPage.value
  const deleted = await props.dashboard.deleteQuest(questId)
  if (!deleted) {
    return
  }

  await loadQuests(targetPage)
}
</script>

<template>
  <section class="stack">
    <UiSurfaceSection id="quests" title="All quests" plain>
      <UiFilterBar :columns="2">
        <UiFieldGroup label="Search">
          <input v-model="questSearch" class="input" placeholder="Title, creator, status, award..." />
        </UiFieldGroup>

        <UiFieldGroup label="Status">
          <select v-model="dashboard.adminQuestStatusFilter" class="input">
            <option
              v-for="option in dashboard.questStatusFilterOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
        </UiFieldGroup>
      </UiFilterBar>

      <details class="admin-more-filters">
        <summary>More filters</summary>
        <UiFilterBar :columns="3">
          <UiFieldGroup label="Audience">
            <select v-model="audienceFilter" class="input">
              <option v-for="option in props.dashboard.questAudienceFilterOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </UiFieldGroup>

          <UiFieldGroup label="From date">
            <input v-model="dateFrom" class="input" type="date" />
          </UiFieldGroup>

          <UiFieldGroup label="To date">
            <input v-model="dateTo" class="input" type="date" />
          </UiFieldGroup>
        </UiFilterBar>
      </details>

      <div v-if="isLoading" class="empty-state mt-4">
        Loading quests...
      </div>

      <template v-else>
        <div v-if="!pagedQuests.length" class="empty-state mt-4">
          No quests in this group.
        </div>

        <template v-else>
          <UiAdminTableShell
            class="mt-4"
            compact
            :top-label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`"
            :bottom-label="`Page ${currentPage} of ${totalPages}`"
            :has-previous="hasPreviousPage"
            :has-next="hasNextPage"
            :show-bottom-pagination="totalPages > 1"
            @previous="previousPage"
            @next="nextPage"
          >
            <table class="admin-table admin-table--compact">
              <thead>
                <tr>
                  <th>ID</th>
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
                  <td>{{ quest.id }}</td>
                  <td>
                    <button class="button-reset admin-table__title-button" type="button" @click="openQuest(quest.questNavigation)">
                      {{ quest.title }}
                    </button>
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
                  <td>{{ formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</td>
                  <td>{{ quest.presentation.assigneeTargetLabel }}</td>
                  <td>
                    <div class="admin-table__actions">
                      <button class="button button--ghost" type="button" @click="handleDeleteQuest(quest.id)">Delete</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </UiAdminTableShell>
        </template>
      </template>
    </UiSurfaceSection>
  </section>
</template>
