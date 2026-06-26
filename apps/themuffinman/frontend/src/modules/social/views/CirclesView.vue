<script setup lang="ts">
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import CircleCandidateCard from "../components/circles/CircleCandidateCard.vue"
import CirclesConnectionsPanel from "../components/circles/CirclesConnectionsPanel.vue"
import CirclesDirectoryPanel from "../components/circles/CirclesDirectoryPanel.vue"
import CirclesInboxPanel from "../components/circles/CirclesInboxPanel.vue"
import UserProfileDialog from "../components/profile/UserProfileDialog.vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiPagination from "../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {useCirclesView} from "../composables/useCirclesView.ts"

const {
  dashboard,
  circles,
  directoryQuery,
  discoverQuery,
  newCircleName,
  activeCircleFilter,
  inboxTab,
  isLoading,
  isSearching,
  isSaving,
  error,
  message,
  messageTone,
  connectionsCount,
  suggestions,
  connectionsItems,
  connectionsPages,
  overviewUnassignedConnectionCount,
  activeCircleName,
  currentInboxItems,
  currentInboxPage,
  currentInboxPages,
  blockedItems,
  blockedPages,
  blockedPage,
  nearbyItems,
  nearbyPages,
  nearbyPage,
  nearbyRadiusKm,
  discoverHasQuery,
  connectionsPage,
  getSelectedCircleIds,
  createCircle,
  deleteCircle,
  renameCircle,
  toggleConnectionCircle,
  resetConnectionCircles,
  hasPendingCircleChanges,
  saveConnectionCircles,
  bulkUpdateConnections,
  sendRequest,
  blockUser,
  unblockUser,
  acceptRequest,
  removeRequest,
  previousInboxPage,
  nextInboxPage,
  previousConnectionsPage,
  nextConnectionsPage,
  previousBlockedPage,
  nextBlockedPage,
  previousNearbyPage,
  nextNearbyPage
} = useCirclesView()
</script>

<template>
  <UiDashboardPage>
        <section class="surface-stack">
          <UiStatusBanner :message="message" :tone="messageTone" />

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>

          <UiWorkspace v-else variant="detail" class="circles-page">
            <div class="surface-stack circles-page__main">
              <UiSurfaceSection class="circles-page__intro" plain>
                <AppPageHeader eyebrow="Circles" title="People you keep close" />
              </UiSurfaceSection>

              <UiSurfaceSection class="circles-page__panel" plain title="Filters">
                <UiFieldGroup label="Filter your circles" field-class="ui-search-field">
                  <input
                    v-model="directoryQuery"
                    class="input"
                    placeholder="Search your connections and requests"
                  />
                </UiFieldGroup>
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

              <UiSurfaceSection class="circles-page__panel" plain>
                <details class="compact-disclosure" open>
                  <summary class="circles-section-summary">Organise circles</summary>

                  <div class="surface-stack mt-4">
                    <CirclesDirectoryPanel
                      :circles="circles"
                      :active-circle-filter="activeCircleFilter"
                      :connections-count="connectionsCount"
                      :overview-unassigned-connection-count="overviewUnassignedConnectionCount"
                      :new-circle-name="newCircleName"
                      :is-saving="isSaving"
                      @update:new-circle-name="newCircleName = $event"
                      @create-circle="createCircle"
                      @select-filter="activeCircleFilter = $event"
                      @open-user="dashboard.openUserProfileDialog($event)"
                      @delete-circle="deleteCircle($event)"
                      @rename-circle="renameCircle($event.circleId, $event.name)"
                    />

                    <CirclesConnectionsPanel
                      :title="activeCircleName"
                      :circles="circles"
                      :connections-items="connectionsItems"
                      :connections-pages="connectionsPages"
                      :connections-page="connectionsPage"
                      :is-saving="isSaving"
                      :get-selected-circle-ids="getSelectedCircleIds"
                      :has-pending-circle-changes="hasPendingCircleChanges"
                      @open-user="dashboard.openUserProfileDialog($event)"
                      @toggle-circle="toggleConnectionCircle($event.connection, $event.circleId)"
                      @save-connection="saveConnectionCircles($event)"
                      @reset-connection="resetConnectionCircles($event)"
                      @bulk-assign="bulkUpdateConnections($event.circleId, $event.userIds, $event.action)"
                      @remove-connection="removeRequest($event, 'warning')"
                      @block-user="blockUser($event)"
                      @previous-page="previousConnectionsPage"
                      @next-page="nextConnectionsPage"
                    />
                  </div>
                </details>
              </UiSurfaceSection>

              <UiSurfaceSection class="circles-page__panel" plain>
                <details class="compact-disclosure">
                  <summary class="circles-section-summary">Blocked</summary>

                  <div class="mt-4">
                    <div v-if="blockedItems.length" class="surface-list">
                      <CircleCandidateCard
                        v-for="user in blockedItems"
                        :key="user.id"
                        :user="user"
                        :saving="isSaving"
                        @open-profile="dashboard.openUserProfileDialog"
                        @invite="sendRequest"
                        @block="blockUser"
                        @unblock="unblockUser"
                      />
                    </div>
                    <div v-else class="empty-state empty-state--soft">No blocked users.</div>

                    <UiPagination
                      v-if="blockedPages > 1"
                      class="mt-4"
                      :label="`Page ${blockedPage} of ${blockedPages}`"
                      :has-previous="blockedPage > 1"
                      :has-next="blockedPage < blockedPages"
                      @previous="previousBlockedPage"
                      @next="nextBlockedPage"
                    />
                  </div>
                </details>
              </UiSurfaceSection>
            </div>

            <aside class="surface-stack circles-page__aside">
              <UiSurfaceSection
                tag="section"
                class="surface-stack circles-page__panel"
                compact
                plain
                title="Search people"
              >
                <UiFieldGroup label="Find people" field-class="ui-search-field">
                  <input
                    v-model="discoverQuery"
                    class="input"
                    placeholder="Search by username or email"
                  />
                </UiFieldGroup>

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
                  {{ discoverHasQuery ? "No people match your search." : "No suggestions right now." }}
                </div>
              </UiSurfaceSection>

              <UiSurfaceSection
                tag="section"
                class="surface-stack circles-page__panel"
                compact
                plain
                title="Neighbors"
              >
                <template #actions>
                  <div class="ui-pill-tabs">
                    <button
                      class="ui-pill-tabs__button"
                      :class="{ 'ui-pill-tabs__button--active': nearbyRadiusKm === 2 }"
                      type="button"
                      @click="nearbyRadiusKm = 2"
                    >
                      2 km
                    </button>
                  </div>
                </template>

                <div v-if="nearbyItems.length" class="surface-list">
                  <CircleCandidateCard
                    v-for="user in nearbyItems"
                    :key="`nearby-${user.id}`"
                    :user="user"
                    :saving="isSaving"
                    @open-profile="dashboard.openUserProfileDialog"
                    @invite="sendRequest"
                    @block="blockUser"
                    @unblock="unblockUser"
                  />
                </div>
                <div v-else class="empty-state empty-state--soft">
                  No nearby people with location sharing enabled.
                </div>

                <UiPagination
                  v-if="nearbyPages > 1"
                  :label="`Page ${nearbyPage} of ${nearbyPages}`"
                  :has-previous="nearbyPage > 1"
                  :has-next="nearbyPage < nearbyPages"
                  @previous="previousNearbyPage"
                  @next="nextNearbyPage"
                />
              </UiSurfaceSection>
            </aside>
          </UiWorkspace>

          <UserProfileDialog
            :open="dashboard.userProfileDialogId !== null"
            :user-id="dashboard.userProfileDialogId"
            @close="dashboard.closeUserProfileDialog()"
          />
        </section>
  </UiDashboardPage>
</template>
