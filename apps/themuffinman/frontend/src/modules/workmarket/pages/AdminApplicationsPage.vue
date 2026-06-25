<script setup lang="ts">
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiAdminPageSection from "../../../components/ui/UiAdminPageSection.vue"
import UiAdminTableShell from "../../../components/ui/UiAdminTableShell.vue"
import UiAdminTableSection from "../../../components/ui/UiAdminTableSection.vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {formatInstantForDisplay} from "../../../shared/questSchedule.ts"
import {useAdminApplicationsPage} from "../composables/useAdminApplicationsPage.ts"

const {
  applications,
  isLoading,
  error,
  searchQuery,
  statusFilter,
  statusFilterOptions,
  feedback,
  feedbackTone,
  totalItems,
  totalPages,
  currentPage,
  hasPreviousPage,
  hasNextPage,
  pageStart,
  pageEnd,
  openQuest,
  openApplicant,
  approveApplication,
  declineApplication,
  previousPage,
  nextPage
} = useAdminApplicationsPage()
</script>

<template>
  <UiDashboardPage admin>
        <AdminShellHeader
          title="Applications"
          subtitle=""
        />

        <UiToast :message="feedback" :tone="feedbackTone" />

        <UiAdminPageSection title="Applications">
          <template #filters>
            <UiFilterBar :columns="2">
              <UiFieldGroup label="Search">
                <input v-model="searchQuery" class="input" placeholder="Quest, applicant, status..." />
              </UiFieldGroup>

              <UiFieldGroup label="Status">
                <select v-model="statusFilter" class="input">
                  <option v-for="option in statusFilterOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </UiFieldGroup>
            </UiFilterBar>
          </template>

          <div v-if="isLoading" class="empty-state">Loading applications...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>
          <UiAdminTableSection
            v-else
            title="Application results"
            :has-items="applications.length > 0"
            empty-message="No applications match this search."
          >
            <UiAdminTableShell
              :top-label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`"
              :bottom-label="totalPages > 1 ? `Page ${currentPage} of ${totalPages}` : ''"
              :has-previous="hasPreviousPage"
              :has-next="hasNextPage"
              :show-bottom-pagination="totalPages > 1"
              @previous="previousPage"
              @next="nextPage"
            >
              <table class="admin-table">
                <thead>
                  <tr>
                    <th>Quest</th>
                    <th>Applicant</th>
                    <th>Status</th>
                    <th>Quest status</th>
                    <th>Pay</th>
                    <th>Term</th>
                    <th>Created</th>
                    <th>Message</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="application in applications" :key="application.id">
                    <td>
                      <strong>{{ application.questTitle }}</strong>
                    </td>
                    <td>{{ application.applicantUsername }}</td>
                    <td>
                      <span :class="application.presentation.statusBadgeClass">{{ application.presentation.statusLabel }}</span>
                    </td>
                    <td>{{ application.presentation.questStatusLabel }}</td>
                    <td>$ {{ application.proposedPrice }}</td>
                    <td>{{ application.presentation.questTermLabel }}</td>
                    <td>{{ formatInstantForDisplay(application.createdAt) }}</td>
                    <td class="admin-table__message">{{ application.message }}</td>
                    <td>
                      <div class="admin-table__actions">
                        <button class="button button--secondary" type="button" @click="openQuest(application)">Quest</button>
                        <button class="button button--secondary" type="button" @click="openApplicant(application)">User</button>
                        <button v-if="application.presentation.canApprove" class="button" type="button" @click="approveApplication(application)">Approve</button>
                        <button v-if="application.presentation.canDecline" class="button button--ghost" type="button" @click="declineApplication(application)">Decline</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </UiAdminTableShell>
          </UiAdminTableSection>
        </UiAdminPageSection>
  </UiDashboardPage>
</template>
