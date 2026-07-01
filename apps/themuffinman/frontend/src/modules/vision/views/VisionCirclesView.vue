<script setup lang="ts">
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import {useRouter} from "vue-router"
import CircleCandidateCard from "../components/circles/CircleCandidateCard.vue"
import CirclesConnectionsPanel from "../components/circles/CirclesConnectionsPanel.vue"
import CirclesDirectoryPanel from "../components/circles/CirclesDirectoryPanel.vue"
import CirclesInboxPanel from "../components/circles/CirclesInboxPanel.vue"
import UiPagination from "../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"
import {useCirclesView} from "../composables/useCirclesView.ts"

const {
  circles,
  directoryQuery,
  discoverQuery,
  newCircleName,
  activeCircleFilter,
  isLoading,
  isSearching,
  isSaving,
  error,
  message,
  messageTone,
  connectionsCount,
  incomingCount,
  outgoingCount,
  suggestions,
  connectionsItems,
  connectionsPages,
  connectionsTotalItems,
  overviewUnassignedConnectionCount,
  incomingItems,
  incomingPage,
  incomingPages,
  incomingTotalItems,
  outgoingItems,
  outgoingPage,
  outgoingPages,
  outgoingTotalItems,
  blockedItems,
  blockedPages,
  blockedPage,
  blockedTotalItems,
  nearbyItems,
  nearbyPages,
  nearbyPage,
  nearbyTotalItems,
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
  previousIncomingPage,
  nextIncomingPage,
  previousOutgoingPage,
  nextOutgoingPage,
  previousConnectionsPage,
  nextConnectionsPage,
  previousBlockedPage,
  nextBlockedPage,
  previousNearbyPage,
  nextNearbyPage
} = useCirclesView()
const router = useRouter()

const openUserProfile = async (userId: number) => {
  await router.push(`/vision/users/${userId}`)
}

const closeCircles = async () => {
  await router.push("/vision")
}
</script>

<template>
  <VisionDetailSurface
    title="Circles"
    @close="closeCircles"
  >
    <UiStatusBanner :message="message" :tone="messageTone" />

    <div v-if="isLoading" class="empty-state">Loading circles...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>

    <UiWorkspace v-else variant="detail" class="circles-page">
      <div class="surface-stack circles-page__main">
        <UiSurfaceSection class="circles-page__intro" plain>
          <AppPageHeader title="Circles" />
        </UiSurfaceSection>

        <UiSurfaceSection class="circles-page__panel" plain>
          <div class="circles-toolbar">
            <input
              v-model="directoryQuery"
              class="input circles-toolbar__search"
              placeholder="Search connections, requests, or blocked people"
            />
          </div>
        </UiSurfaceSection>

        <CirclesInboxPanel
          :incoming-items="incomingItems"
          :incoming-page="incomingPage"
          :incoming-pages="incomingPages"
          :incoming-total="incomingTotalItems"
          :outgoing-items="outgoingItems"
          :outgoing-page="outgoingPage"
          :outgoing-pages="outgoingPages"
          :outgoing-total="outgoingTotalItems"
          :incoming-count="incomingCount"
          :outgoing-count="outgoingCount"
          :is-saving="isSaving"
          @open-user="openUserProfile"
          @accept="acceptRequest($event)"
          @remove="removeRequest($event)"
          @previous-incoming="previousIncomingPage"
          @next-incoming="nextIncomingPage"
          @previous-outgoing="previousOutgoingPage"
          @next-outgoing="nextOutgoingPage"
        />

        <UiSurfaceSection class="circles-page__panel" plain>
          <details class="compact-disclosure" open>
            <summary class="circles-section-summary">
              <span>Organise circles</span>
              <span class="badge badge--accent">{{ circles.length }}</span>
            </summary>

            <div class="surface-stack mt-4">
              <CirclesDirectoryPanel
                :circles="circles"
                :new-circle-name="newCircleName"
                :is-saving="isSaving"
                @update:new-circle-name="newCircleName = $event"
                @create-circle="createCircle"
                @delete-circle="deleteCircle($event)"
                @rename-circle="renameCircle($event.circleId, $event.name)"
              />

              <CirclesConnectionsPanel
                :circles="circles"
                :active-circle-filter="activeCircleFilter"
                :connections-items="connectionsItems"
                :connections-pages="connectionsPages"
                :connections-page="connectionsPage"
                :connections-total-items="connectionsTotalItems"
                :connections-count="connectionsCount"
                :overview-unassigned-connection-count="overviewUnassignedConnectionCount"
                :is-saving="isSaving"
                :get-selected-circle-ids="getSelectedCircleIds"
                :has-pending-circle-changes="hasPendingCircleChanges"
                @select-filter="activeCircleFilter = $event"
                @open-user="openUserProfile"
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
            <summary class="circles-section-summary">
              <span>Blocked</span>
              <span class="badge">{{ blockedTotalItems }}</span>
            </summary>

            <div class="mt-4">
              <div v-if="blockedItems.length" class="surface-list">
                <CircleCandidateCard
                  v-for="user in blockedItems"
                  :key="user.id"
                  :user="user"
                  :saving="isSaving"
                  @open-profile="openUserProfile"
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
          title="Find people"
        >
          <template #actions>
            <span class="muted">{{ suggestions.length }}</span>
          </template>

          <input
            v-model="discoverQuery"
            class="input circles-toolbar__search"
            placeholder="Search by username or email"
          />

          <div v-if="isSearching" class="empty-state">Searching...</div>
          <div v-else-if="suggestions.length" class="surface-list">
            <CircleCandidateCard
              v-for="user in suggestions"
              :key="user.id"
              :user="user"
              :saving="isSaving"
              @open-profile="openUserProfile"
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
            <span class="muted">{{ nearbyTotalItems }} nearby</span>
          </template>

          <div v-if="nearbyItems.length" class="surface-list">
            <CircleCandidateCard
              v-for="user in nearbyItems"
              :key="`nearby-${user.id}`"
              :user="user"
              :saving="isSaving"
              @open-profile="openUserProfile"
              @invite="sendRequest"
              @block="blockUser"
              @unblock="unblockUser"
            />
          </div>
          <div v-else class="empty-state empty-state--soft">No nearby users.</div>

          <UiPagination
            v-if="nearbyPages > 1"
            class="mt-4"
            :label="`Page ${nearbyPage} of ${nearbyPages}`"
            :has-previous="nearbyPage > 1"
            :has-next="nearbyPage < nearbyPages"
            @previous="previousNearbyPage"
            @next="nextNearbyPage"
          />
        </UiSurfaceSection>
      </aside>
    </UiWorkspace>
  </VisionDetailSurface>
</template>
