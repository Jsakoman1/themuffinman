<script setup lang="ts">
import UiAdminTableShell from "../../../../components/ui/UiAdminTableShell.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../../components/ui/UiFilterBar.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
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
  previousPage,
  nextPage,
  openQuest
} = useDashboardAdminQuestBrowser(props.dashboard)
</script>

<template>
  <section class="stack">
    <UiSurfaceSection title="Admin control center" soft>

      <UiFormActions>
        <button class="button" type="button" @click="dashboard.refreshDashboardData">Refresh data</button>
      </UiFormActions>
    </UiSurfaceSection>

    <UiSurfaceSection id="quests" title="All quests">
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
            :top-label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`"
            :bottom-label="`Page ${currentPage} of ${totalPages}`"
            :has-previous="hasPreviousPage"
            :has-next="hasNextPage"
            :show-bottom-pagination="totalPages > 1"
            @previous="previousPage"
            @next="nextPage"
          >
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
          </UiAdminTableShell>
        </template>
      </template>
    </UiSurfaceSection>
  </section>
</template>
