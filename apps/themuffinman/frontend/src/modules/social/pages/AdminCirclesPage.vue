<script setup lang="ts">
import {onMounted, ref} from "vue"
import AdminShellHeader from "../../workmarket/components/admin/AdminShellHeader.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiSectionHeader from "../../../components/ui/UiSectionHeader.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useAutoDismissFeedback} from "../../../composables/useAutoDismissFeedback.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {workmarketApi, type AdminCircleOverview} from "../../workmarket/api/workmarketApi.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"

const overview = ref<AdminCircleOverview | null>(null)
const isLoading = ref(false)
const error = ref("")
const searchQuery = ref("")
const feedbackState = useAutoDismissFeedback<"success" | "error">(5000, "success")
const feedback = feedbackState.message
const feedbackTone = feedbackState.tone
const setFeedback = feedbackState.show

const loadOverview = async () => {
  isLoading.value = true
  error.value = ""

  try {
    overview.value = await workmarketApi.getAdminCircleOverview(searchQuery.value)
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load circles.")
  } finally {
    isLoading.value = false
  }
}

const deleteCircle = async (id: number) => {
  try {
    const result = await workmarketApi.deleteAdminCircle(id)
    setFeedback(result.message, "success")
    await loadOverview()
  } catch (requestError) {
    setFeedback(getApiErrorMessage(requestError, "Could not delete circle."), "error")
  }
}

const deleteRequest = async (id: number) => {
  try {
    const result = await workmarketApi.deleteAdminCircleRequest(id)
    setFeedback(result.message, "success")
    await loadOverview()
  } catch (requestError) {
    setFeedback(getApiErrorMessage(requestError, "Could not delete relation."), "error")
  }
}

onMounted(() => {
  void loadOverview()
})

useDebouncedWatch(searchQuery, () => {
  void loadOverview()
}, 250)
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main dashboard-main--admin">
        <AdminShellHeader
          title="Circles"
          subtitle="Review every circle and every relation from one admin workspace."
        />

        <UiToast :message="feedback" :tone="feedbackTone" />

        <article class="card admin-users-card">
          <UiFilterBar :columns="2">
            <label class="field">
              <span class="label">Search</span>
              <input v-model="searchQuery" class="input" placeholder="Circle, owner, member..." />
            </label>
          </UiFilterBar>

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>
          <div v-else class="stack">
            <section class="stack">
              <UiSectionHeader title="Circles" />

              <div v-if="overview?.circles.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Owner</th>
                      <th>Members</th>
                      <th>People</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="circle in overview.circles" :key="circle.id">
                      <td><strong>{{ circle.name }}</strong></td>
                      <td>{{ circle.ownerUsername }}</td>
                      <td>{{ circle.memberCount }}</td>
                      <td class="admin-table__message">{{ circle.memberPreviewLabel }}</td>
                      <td>
                        <div class="admin-table__actions">
                          <button class="button button--ghost" type="button" @click="deleteCircle(circle.id)">Delete</button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No circles match this search.</div>
            </section>

            <section class="stack">
              <UiSectionHeader title="Accepted connections" />
              <div v-if="overview?.acceptedConnections.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in overview.acceptedConnections" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td><span :class="['badge', item.statusBadgeClass]">{{ item.statusLabel }}</span></td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No accepted connections match this search.</div>
            </section>

            <section class="stack">
              <UiSectionHeader title="Pending requests" />
              <div v-if="overview?.pendingRequests.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in overview.pendingRequests" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td><span :class="['badge', item.statusBadgeClass]">{{ item.statusLabel }}</span></td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No pending requests match this search.</div>
            </section>

            <section class="stack">
              <UiSectionHeader title="Blocked relations" />
              <div v-if="overview?.blockedRelations.length" class="admin-table-shell">
                <table class="admin-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in overview.blockedRelations" :key="item.id">
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td><span :class="['badge', item.statusBadgeClass]">{{ item.statusLabel }}</span></td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="empty-state">No blocked relations match this search.</div>
            </section>
          </div>
        </article>
      </main>
    </div>
  </div>
</template>
