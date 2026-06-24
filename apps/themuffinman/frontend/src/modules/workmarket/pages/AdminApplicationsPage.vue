<script setup lang="ts">
import {onMounted, ref, watch} from "vue"
import {useRouter} from "vue-router"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import UiPagination from "../../../components/ui/UiPagination.vue"
import {useAutoDismissFeedback} from "../../../composables/useAutoDismissFeedback.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {workmarketApi, type QuestApplication, type WorkmarketOptions} from "../api/workmarketApi.ts"
import {formatInstantForDisplay} from "../../../shared/questSchedule.ts"
import type {QuestApplicationStatus} from "../domain/workmarketDomain.ts"
import {normalizeSearchQuery} from "../../../lib/searchQuery.ts"
import {usePaginatedResults} from "../../../composables/usePaginatedResults.ts"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"

const router = useRouter()
const itemsPerPage = 20
const results = usePaginatedResults<QuestApplication>(itemsPerPage)
const applications = results.items
const isLoading = ref(false)
const error = ref("")
const searchQuery = ref("")
const statusFilter = ref<QuestApplicationStatus | "ALL">("ALL")
const statusFilterOptions = ref<WorkmarketOptions["questApplicationStatusFilters"]>([])
const feedbackState = useAutoDismissFeedback<"success" | "error">(5000, "success")
const feedback = feedbackState.message
const feedbackTone = feedbackState.tone
const setFeedback = feedbackState.show
const totalItems = results.totalItems
const totalPages = results.totalPages
const currentPage = results.currentPage
const hasPreviousPage = results.hasPreviousPage
const hasNextPage = results.hasNextPage
const pageStart = results.pageStart
const pageEnd = results.pageEnd

const loadApplications = async (page = 1) => {
  isLoading.value = true
  error.value = ""

  try {
    const response = await workmarketApi.getAdminApplications({
      q: normalizeSearchQuery(searchQuery.value),
      status: statusFilter.value,
      page: Math.max(0, page - 1),
      size: itemsPerPage
    })
    results.applyPage(response)
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load applications.")
    results.reset()
  } finally {
    isLoading.value = false
  }
}

const loadStatusFilterOptions = async () => {
  try {
    const options = await workmarketApi.getAppUserOptions()
    statusFilterOptions.value = options.questApplicationStatusFilters
  } catch (requestError) {
    setFeedback(getApiErrorMessage(requestError, "Could not load application filters."), "error")
    statusFilterOptions.value = []
  }
}

const openQuest = (application: QuestApplication) => {
  void router.push(routeForNavigationTarget(application.questNavigation))
}

const openApplicant = (application: QuestApplication) => {
  void router.push(routeForNavigationTarget(application.applicantNavigation))
}

const approveApplication = async (application: QuestApplication) => {
  try {
    const result = await workmarketApi.approveApplication(application.questId, application.id)
    setFeedback(result.message, "success")
    await loadApplications()
  } catch (requestError) {
    setFeedback(getApiErrorMessage(requestError, "Could not approve application."), "error")
  }
}

const declineApplication = async (application: QuestApplication) => {
  try {
    const result = await workmarketApi.declineApplication(application.questId, application.id)
    setFeedback(result.message, "success")
    await loadApplications()
  } catch (requestError) {
    setFeedback(getApiErrorMessage(requestError, "Could not decline application."), "error")
  }
}

const previousPage = () => {
  void results.previousPage(loadApplications)
}

const nextPage = () => {
  void results.nextPage(loadApplications)
}

watch([searchQuery, statusFilter], () => {
  void loadApplications(1)
})

onMounted(() => {
  void loadStatusFilterOptions()
  void loadApplications()
})

</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main dashboard-main--admin">
        <AdminShellHeader
          title="Applications"
          subtitle="Search, review, approve, and decline applications from one table."
        />

        <UiToast :message="feedback" :tone="feedbackTone" />

        <article class="card admin-users-card">
          <UiFilterBar :columns="2">
            <label class="field">
              <span class="label">Search</span>
              <input v-model="searchQuery" class="input" placeholder="Quest, applicant, status..." />
            </label>

            <label class="field">
              <span class="label">Status</span>
              <select v-model="statusFilter" class="input">
                <option v-for="option in statusFilterOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
          </UiFilterBar>

          <div v-if="isLoading" class="empty-state">Loading applications...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>
          <div v-else-if="!applications.length" class="empty-state">No applications match this search.</div>

          <div v-else class="admin-table-shell">
            <UiPagination
              class="mb-4"
              :label="`Showing ${pageStart}-${pageEnd} of ${totalItems}`"
              :has-previous="hasPreviousPage"
              :has-next="hasNextPage"
              @previous="previousPage"
              @next="nextPage"
            />

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

            <UiPagination
              v-if="totalPages > 1"
              class="mt-4"
              :label="`Page ${currentPage} of ${totalPages}`"
              :has-previous="hasPreviousPage"
              :has-next="hasNextPage"
              @previous="previousPage"
              @next="nextPage"
            />
          </div>
        </article>
      </main>
    </div>
  </div>
</template>
