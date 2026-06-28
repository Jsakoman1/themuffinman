<script setup lang="ts">
import {ref} from "vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiAdminPageSection from "../../../components/ui/UiAdminPageSection.vue"
import UiAdminTableShell from "../../../components/ui/UiAdminTableShell.vue"
import UiAdminTableSection from "../../../components/ui/UiAdminTableSection.vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import DetailDialogFrame from "../components/shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../components/shared/DetailUtilitySection.vue"
import ApplicationEditFields from "../components/shared/ApplicationEditFields.vue"
import {formatInstantForDisplay, formatQuestTermForDisplay} from "../../../shared/questSchedule.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {workmarketApi, type QuestApplication} from "../api/workmarketApi.ts"
import type {QuestApplicationStatus} from "../domain/workmarketDomain.ts"
import {useAdminApplicationsPage} from "../composables/useAdminApplicationsPage.ts"
import {formatApplicationPrice} from "../shared/pricing.ts"

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
  loadApplications,
  previousPage,
  nextPage
} = useAdminApplicationsPage()

const selectedApplication = ref<QuestApplication | null>(null)
const editMessage = ref("")
const editPrice = ref("")
const editStatus = ref<QuestApplicationStatus>("PENDING")
const dialogError = ref("")
const isSavingDialog = ref(false)

const openEditDialog = (application: QuestApplication) => {
  selectedApplication.value = application
  editMessage.value = application.message ?? ""
  editPrice.value = String(application.proposedPrice ?? "")
  editStatus.value = application.status
  dialogError.value = ""
}

const closeEditDialog = () => {
  selectedApplication.value = null
  dialogError.value = ""
}

const saveApplication = async () => {
  if (!selectedApplication.value) {
    return
  }

  isSavingDialog.value = true
  dialogError.value = ""
  try {
    await workmarketApi.updateAdminApplication(selectedApplication.value.id, {
      message: editMessage.value,
      proposedPrice: selectedApplication.value.proposedPrice == null ? null : Number(editPrice.value),
      status: editStatus.value
    })
    await loadApplications(currentPage.value)
    closeEditDialog()
  } catch (error) {
    dialogError.value = getApiErrorMessage(error, "Could not update application.")
  } finally {
    isSavingDialog.value = false
  }
}

const deleteApplication = async (application: QuestApplication) => {
  try {
    await workmarketApi.deleteAdminApplication(application.id)
    await loadApplications(currentPage.value)
  } catch {
    // page toast stays handled by generic app error patterns elsewhere
  }
}
</script>

<template>
  <UiDashboardPage admin>
        <AdminShellHeader
          title="Applications"
          subtitle=""
        />

        <UiToast :message="feedback" :tone="feedbackTone" />

        <UiAdminPageSection title="Applications">
          <template #actions>
            <div class="admin-filter-summary">{{ totalItems }} applications</div>
          </template>
          <template #filters>
            <UiFilterBar :columns="2">
              <UiFieldGroup label="Search">
                <input v-model="searchQuery" class="input" placeholder="Quest, creator, applicant, status..." />
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
              compact
              :top-label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`"
              :bottom-label="totalPages > 1 ? `Page ${currentPage} of ${totalPages}` : ''"
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
                    <th>Quest</th>
                    <th>Applicant</th>
                    <th>Status</th>
                    <th>Pay</th>
                    <th>Term</th>
                    <th>Created</th>
                    <th>Message</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="application in applications" :key="application.id">
                    <td>{{ application.id }}</td>
                    <td>
                      <strong>{{ application.questTitle }}</strong>
                    </td>
                    <td>{{ application.applicantUsername }}</td>
                    <td>
                      <span :class="application.presentation.statusBadgeClass">{{ application.presentation.statusLabel }}</span>
                    </td>
                    <td>{{ formatApplicationPrice(application.proposedPrice) }}</td>
                    <td>{{ formatQuestTermForDisplay(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}</td>
                    <td>{{ formatInstantForDisplay(application.createdAt) }}</td>
                    <td class="admin-table__message">{{ application.message }}</td>
                    <td>
                      <div class="admin-table__actions">
                        <button class="button button--secondary" type="button" @click="openQuest(application)">Quest</button>
                        <button class="button button--secondary" type="button" @click="openApplicant(application)">User</button>
                        <button class="button button--secondary" type="button" @click="openEditDialog(application)">Edit</button>
                        <button v-if="application.presentation.canApprove" class="button" type="button" @click="approveApplication(application)">Approve</button>
                        <button v-if="application.presentation.canDecline" class="button button--ghost" type="button" @click="declineApplication(application)">Decline</button>
                        <button class="button button--ghost" type="button" @click="deleteApplication(application)">Delete</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </UiAdminTableShell>
          </UiAdminTableSection>
        </UiAdminPageSection>

        <UiDialog
          :open="!!selectedApplication"
          title="Edit application"
          size="xl"
          @close="closeEditDialog"
        >
          <DetailDialogFrame v-if="selectedApplication">
            <template #main>
              <div class="surface-stack">
                <div v-if="dialogError" class="alert alert--error">{{ dialogError }}</div>
                <ApplicationEditFields
                  :message="editMessage"
                  :price="editPrice"
                  price-placeholder="50"
                  :show-price="selectedApplication.proposedPrice != null"
                  @update:message="editMessage = $event"
                  @update:price="editPrice = $event"
                />
              </div>
            </template>

            <template #side>
              <DetailUtilitySection title="Status" tone="summary">
                <select v-model="editStatus" class="input">
                  <option v-for="option in statusFilterOptions.filter((item) => item.value !== 'ALL')" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </DetailUtilitySection>

              <DetailUtilitySection title="Actions" tone="actions">
                <div class="ui-action-stack">
                  <button class="button button--action" type="button" :disabled="isSavingDialog" @click="saveApplication">
                    Save changes
                  </button>
                  <button class="button button--secondary" type="button" :disabled="isSavingDialog" @click="closeEditDialog">
                    Cancel
                  </button>
                </div>
              </DetailUtilitySection>
            </template>
          </DetailDialogFrame>
        </UiDialog>
  </UiDashboardPage>
</template>
