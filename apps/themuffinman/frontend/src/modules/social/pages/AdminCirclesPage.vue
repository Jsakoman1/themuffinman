<script setup lang="ts">
import {computed, onMounted, ref} from "vue"
import AdminShellHeader from "../../workmarket/components/admin/AdminShellHeader.vue"
import UiAdminPageSection from "../../../components/ui/UiAdminPageSection.vue"
import UiAdminTableShell from "../../../components/ui/UiAdminTableShell.vue"
import UiAdminTableSection from "../../../components/ui/UiAdminTableSection.vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiFilterBar from "../../../components/ui/UiFilterBar.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useAutoDismissFeedback} from "../../../composables/useAutoDismissFeedback.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {createFeedbackMutationRunner} from "../../../composables/createFeedbackMutationRunner.ts"
import {workmarketApi} from "../../workmarket/api/workmarketApi.ts"
import type {AdminCircleOverview} from "../../../contracts/index.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"

const overview = ref<AdminCircleOverview | null>(null)
const isLoading = ref(false)
const error = ref("")
const searchQuery = ref("")
const feedbackState = useAutoDismissFeedback<"success" | "error" | "warning">(5000, "success")
const feedback = feedbackState.message
const feedbackTone = feedbackState.tone
const setFeedback = feedbackState.show
const {runWithFeedback} = createFeedbackMutationRunner({showFeedback: setFeedback})

const relationSections = [
  {
    key: "accepted",
    title: "Accepted connections",
    emptyMessage: "No accepted connections match this search.",
    items: () => overview.value?.acceptedConnections ?? [],
  },
  {
    key: "pending",
    title: "Pending requests",
    emptyMessage: "No pending requests match this search.",
    items: () => overview.value?.pendingRequests ?? [],
  },
  {
    key: "blocked",
    title: "Blocked relations",
    emptyMessage: "No blocked relations match this search.",
    items: () => overview.value?.blockedRelations ?? [],
  },
] as const

const relationRows = computed(() => relationSections.flatMap((section) =>
  section.items().map((item) => ({
    ...item,
    typeLabel: section.title,
  }))
))

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
  await runWithFeedback({
    run: () => workmarketApi.deleteAdminCircle(id),
    successMessage: (result) => result.message,
    errorMessage: "Could not delete circle.",
    afterSuccess: loadOverview
  })
}

const deleteRequest = async (id: number) => {
  await runWithFeedback({
    run: () => workmarketApi.deleteAdminCircleRequest(id),
    successMessage: (result) => result.message,
    errorMessage: "Could not delete relation.",
    afterSuccess: loadOverview
  })
}

onMounted(() => {
  void loadOverview()
})

useDebouncedWatch(searchQuery, () => {
  void loadOverview()
}, 250)
</script>

<template>
  <UiAppShellPage admin>
        <AdminShellHeader
          title="Circles"
          subtitle=""
        />

        <UiToast :message="feedback" :tone="feedbackTone" />

        <UiAdminPageSection title="Circles">
          <template #actions>
            <div class="admin-filter-summary">
              {{ overview?.circles.length ?? 0 }} circles, {{ relationRows.length }} relations
            </div>
          </template>
          <template #filters>
            <UiFilterBar :columns="2">
              <UiFieldGroup label="Search">
                <input v-model="searchQuery" class="input" placeholder="Circle, owner, member..." />
              </UiFieldGroup>
            </UiFilterBar>
          </template>

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>
          <div v-else class="surface-stack">
            <UiAdminTableSection title="Circles" :has-items="!!overview?.circles.length" empty-message="No circles match this search.">
              <UiAdminTableShell v-if="overview?.circles.length" compact :has-previous="false" :has-next="false" :show-bottom-pagination="false">
                <table class="admin-table admin-table--compact">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Owner</th>
                      <th>Members</th>
                      <th>People</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="circle in overview.circles" :key="circle.id">
                      <td>{{ circle.id }}</td>
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
              </UiAdminTableShell>
            </UiAdminTableSection>

            <UiAdminTableSection
              title="Relations"
              :has-items="relationRows.length > 0"
              empty-message="No relations match this search."
            >
              <UiAdminTableShell v-if="relationRows.length" compact :has-previous="false" :has-next="false" :show-bottom-pagination="false">
                <table class="admin-table admin-table--compact">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Type</th>
                      <th>Requester</th>
                      <th>Recipient</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in relationRows" :key="item.id">
                      <td>{{ item.id }}</td>
                      <td>{{ item.typeLabel }}</td>
                      <td>{{ item.requesterUsername }}</td>
                      <td>{{ item.recipientUsername }}</td>
                      <td><span :class="['badge', item.statusBadgeClass]">{{ item.statusLabel }}</span></td>
                      <td><div class="admin-table__actions"><button class="button button--ghost" type="button" @click="deleteRequest(item.id)">Delete</button></div></td>
                    </tr>
                  </tbody>
                </table>
              </UiAdminTableShell>
            </UiAdminTableSection>
          </div>
        </UiAdminPageSection>
  </UiAppShellPage>
</template>
