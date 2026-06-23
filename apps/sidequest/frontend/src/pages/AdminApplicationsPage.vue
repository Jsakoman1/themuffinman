<script setup lang="ts">
import {onMounted, ref, watch} from "vue"
import {useRouter} from "vue-router"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import UiToast from "../components/ui/UiToast.vue"
import UiPagination from "../components/ui/UiPagination.vue"
import {logoutUser} from "../auth.ts"
import {sidequestApi, type QuestApplication} from "../api/sidequestApi.ts"
import {formatApplicationStatus, statusBadgeClass} from "../lib/questDashboardRules.ts"
import {formatInstantForDisplay, formatQuestTerm} from "../shared/questSchedule.ts"
import type {QuestApplicationStatus} from "../shared/sidequestDomain.ts"
import {normalizeSearchQuery} from "../lib/searchQuery.ts"
import {usePaginatedResults} from "../composables/usePaginatedResults.ts"

const router = useRouter()
const itemsPerPage = 20
const results = usePaginatedResults<QuestApplication>(itemsPerPage)
const applications = results.items
const isLoading = ref(false)
const error = ref("")
const searchQuery = ref("")
const statusFilter = ref<QuestApplicationStatus | "ALL">("ALL")
const feedback = ref("")
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
    const response = await sidequestApi.getAdminApplications({
      q: normalizeSearchQuery(searchQuery.value),
      status: statusFilter.value,
      page: Math.max(0, page - 1),
      size: itemsPerPage
    })
    results.applyPage(response)
  } catch {
    error.value = "Could not load applications."
    results.reset()
  } finally {
    isLoading.value = false
  }
}

const handleLogout = () => {
  logoutUser()
  router.push("/login")
}

const openQuest = (questId: number) => {
  void router.push(`/quests/${questId}`)
}

const openApplicant = (userId: number) => {
  void router.push(`/users/${userId}`)
}

const approveApplication = async (application: QuestApplication) => {
  try {
    await sidequestApi.approveApplication(application.questId, application.id)
    feedback.value = "Application approved."
    await loadApplications()
  } catch {
    feedback.value = "Could not approve application."
  }
}

const declineApplication = async (application: QuestApplication) => {
  try {
    await sidequestApi.declineApplication(application.questId, application.id)
    feedback.value = "Application declined."
    await loadApplications()
  } catch {
    feedback.value = "Could not decline application."
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
          :on-logout="handleLogout"
        />

        <UiToast :message="feedback" tone="success" />

        <article class="card admin-users-card">
          <div class="grid grid--two admin-toolbar">
            <label class="field">
              <span class="label">Search</span>
              <input v-model="searchQuery" class="input" placeholder="Quest, applicant, status..." />
            </label>

            <label class="field">
              <span class="label">Status</span>
              <select v-model="statusFilter" class="input">
                <option value="ALL">All</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="DECLINED">Declined</option>
                <option value="WITHDRAWN">Withdrawn</option>
              </select>
            </label>
          </div>

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
                    <span :class="['badge', statusBadgeClass(application.status)]">{{ formatApplicationStatus(application.status) }}</span>
                  </td>
                  <td>{{ application.questStatus }}</td>
                  <td>$ {{ application.proposedPrice }}</td>
                  <td>{{ formatQuestTerm(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}</td>
                  <td>{{ formatInstantForDisplay(application.createdAt) }}</td>
                  <td class="admin-table__message">{{ application.message }}</td>
                  <td>
                    <div class="admin-table__actions">
                      <button class="button button--secondary" type="button" @click="openQuest(application.questId)">Quest</button>
                      <button class="button button--secondary" type="button" @click="openApplicant(application.applicantId)">User</button>
                      <button v-if="application.status === 'PENDING'" class="button" type="button" @click="approveApplication(application)">Approve</button>
                      <button v-if="application.status === 'PENDING'" class="button button--ghost" type="button" @click="declineApplication(application)">Decline</button>
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
