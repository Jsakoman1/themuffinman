<script setup lang="ts">
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import CircleCandidateCard from "../components/circles/CircleCandidateCard.vue"
import CirclesConnectionsPanel from "../components/circles/CirclesConnectionsPanel.vue"
import CirclesDirectoryPanel from "../components/circles/CirclesDirectoryPanel.vue"
import CirclesInboxPanel from "../components/circles/CirclesInboxPanel.vue"
import DashboardProfileDialog from "../../workmarket/components/dashboard/DashboardProfileDialog.vue"
import UserProfileDialog from "../components/profile/UserProfileDialog.vue"
import UiInfoGrid from "../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../components/ui/UiStatCard.vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiSplitLayout from "../../../components/ui/UiSplitLayout.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {useCirclesView} from "../composables/useCirclesView.ts"

const {
  dashboard,
  circles,
  searchQuery,
  newCircleName,
  activeCircleFilter,
  inboxTab,
  isLoading,
  isSearching,
  isSaving,
  error,
  message,
  messageTone,
  circlesCount,
  connectionsCount,
  incomingCount,
  outgoingCount,
  suggestions,
  connectionsItems,
  connectionsPages,
  overviewUnassignedConnectionCount,
  activeCircleName,
  currentInboxItems,
  currentInboxPage,
  currentInboxPages,
  searchHasQuery,
  connectionsPage,
  getSelectedCircleIds,
  createCircle,
  deleteCircle,
  toggleConnectionCircle,
  resetConnectionCircles,
  hasPendingCircleChanges,
  saveConnectionCircles,
  sendRequest,
  blockUser,
  unblockUser,
  acceptRequest,
  removeRequest,
  previousInboxPage,
  nextInboxPage,
  previousConnectionsPage,
  nextConnectionsPage
} = useCirclesView()
</script>

<template>
  <UiDashboardPage>
        <section class="surface-stack">
          <UiSurfaceSection class="card app-hero-surface app-hero-surface--circles" soft>
            <UiSplitLayout>
              <div class="surface-stack">
                <AppPageHeader
                  eyebrow="Relationship map"
                  title="Circles"
                />
              </div>

              <form class="ui-inline-form" @submit.prevent="createCircle">
                <input v-model="newCircleName" class="input" maxlength="80" placeholder="Create a new circle" />
                <button class="button" type="submit" :disabled="isSaving">Add circle</button>
              </form>
            </UiSplitLayout>

            <UiInfoGrid :columns="4">
              <UiStatCard label="Circles" :value="circlesCount" />
              <UiStatCard label="Connections" :value="connectionsCount" />
              <UiStatCard label="Incoming" :value="incomingCount" />
              <UiStatCard label="Outgoing" :value="outgoingCount" />
            </UiInfoGrid>
          </UiSurfaceSection>

          <UiStatusBanner :message="message" :tone="messageTone" />

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>

          <UiWorkspace v-else variant="triad">
            <CirclesDirectoryPanel
              :circles="circles"
              :active-circle-filter="activeCircleFilter"
              :connections-count="connectionsCount"
              :overview-unassigned-connection-count="overviewUnassignedConnectionCount"
              :is-saving="isSaving"
              @select-filter="activeCircleFilter = $event"
              @open-user="dashboard.openUserProfileDialog($event)"
              @delete-circle="deleteCircle($event)"
            />

            <CirclesConnectionsPanel
              :title="activeCircleName"
              :search-query="searchQuery"
              :circles="circles"
              :connections-items="connectionsItems"
              :connections-pages="connectionsPages"
              :connections-page="connectionsPage"
              :is-saving="isSaving"
              :get-selected-circle-ids="getSelectedCircleIds"
              :has-pending-circle-changes="hasPendingCircleChanges"
              @update:search-query="searchQuery = $event"
              @open-user="dashboard.openUserProfileDialog($event)"
              @toggle-circle="toggleConnectionCircle($event.connection, $event.circleId)"
              @save-connection="saveConnectionCircles($event)"
              @reset-connection="resetConnectionCircles($event)"
              @remove-connection="removeRequest($event, 'warning')"
              @block-user="blockUser($event)"
              @previous-page="previousConnectionsPage"
              @next-page="nextConnectionsPage"
            />

            <aside class="surface-stack ui-sticky">
              <UiSurfaceSection
                tag="section"
                class="surface-stack"
                compact
                :title="searchHasQuery ? 'Search results' : 'Suggestions'"
              >
                <div v-if="isSearching" class="empty-state">Searching...</div>
                <div v-else-if="suggestions.length" class="surface-list">
                  <CircleCandidateCard
                    v-for="user in suggestions"
                    :key="user.id"
                    :user="user"
                    :saving="isSaving"
                    @open-profile="dashboard.openUserProfileDialog"
                    @invite="sendRequest"
                    @block="blockUser"
                    @unblock="unblockUser"
                  />
                </div>
                <div v-else class="empty-state empty-state--soft">
                  {{ searchHasQuery ? "No people match your search." : "No suggestions right now." }}
                </div>
              </UiSurfaceSection>

              <CirclesInboxPanel
                :inbox-tab="inboxTab"
                :current-inbox-items="currentInboxItems"
                :current-inbox-page="currentInboxPage"
                :current-inbox-pages="currentInboxPages"
                :is-saving="isSaving"
                @update:inbox-tab="inboxTab = $event"
                @open-user="dashboard.openUserProfileDialog($event)"
                @accept="acceptRequest($event)"
                @remove="removeRequest($event)"
                @previous="previousInboxPage"
                @next="nextInboxPage"
              />
            </aside>
          </UiWorkspace>

          <DashboardProfileDialog :dashboard="dashboard" />
          <UserProfileDialog
            :open="dashboard.userProfileDialogId !== null"
            :user-id="dashboard.userProfileDialogId"
            @close="dashboard.closeUserProfileDialog()"
            @edit-profile="dashboard.openProfileEditDialog()"
          />
        </section>
  </UiDashboardPage>
</template>
