<script setup lang="ts">
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import CircleCandidateCard from "../components/circles/CircleCandidateCard.vue"
import DashboardProfileDialog from "../../workmarket/components/dashboard/DashboardProfileDialog.vue"
import ProfileAvatar from "../../../components/profile/ProfileAvatar.vue"
import ProfileBio from "../../../components/profile/ProfileBio.vue"
import UserProfileDialog from "../components/profile/UserProfileDialog.vue"
import UiPagination from "../../../components/ui/UiPagination.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
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
  connectionsTotalItems,
  overviewUnassignedConnectionCount,
  activeCircleName,
  currentInboxItems,
  currentInboxPage,
  currentInboxPages,
  currentInboxTotal,
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
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main">
        <section class="stack">
          <div class="card circles-masthead">
            <div class="circles-masthead__top">
              <div class="circles-masthead__copy">
                <AppPageHeader
                  eyebrow="Relationship map"
                  title="Circles"
                  subtitle="Shape your network the way you actually think about it: family, close friends, work, neighbors, or any private mix you need."
                />
              </div>

              <form class="circles-masthead__create" @submit.prevent="createCircle">
                <input v-model="newCircleName" class="input circles-masthead__input" maxlength="80" placeholder="Create a new circle" />
                <button class="button" type="submit" :disabled="isSaving">Add circle</button>
              </form>
            </div>

            <div class="circles-masthead__stats">
              <article class="circles-stat-card">
                <span class="circles-stat-card__label">Circles</span>
                <strong class="circles-stat-card__value">{{ circlesCount }}</strong>
              </article>
              <article class="circles-stat-card">
                <span class="circles-stat-card__label">Connections</span>
                <strong class="circles-stat-card__value">{{ connectionsCount }}</strong>
              </article>
              <article class="circles-stat-card">
                <span class="circles-stat-card__label">Incoming</span>
                <strong class="circles-stat-card__value">{{ incomingCount }}</strong>
              </article>
              <article class="circles-stat-card">
                <span class="circles-stat-card__label">Outgoing</span>
                <strong class="circles-stat-card__value">{{ outgoingCount }}</strong>
              </article>
            </div>
          </div>

          <UiStatusBanner :message="message" :tone="messageTone" />

          <div v-if="isLoading" class="empty-state">Loading circles...</div>
          <div v-else-if="error" class="alert alert--error">{{ error }}</div>

          <div v-else class="circles-workspace">
            <aside class="circles-panel circles-panel--rail">
              <div class="circles-panel__header">
                <div>
                  <h2 class="circles-panel__title">Your circles</h2>
                  <p class="circles-panel__subtitle">Pick one to focus the people list instantly.</p>
                </div>
              </div>

              <div class="circles-directory">
                <button
                  class="circles-directory__item"
                  :class="{ 'circles-directory__item--active': activeCircleFilter === 'all' }"
                  type="button"
                  @click="activeCircleFilter = 'all'"
                >
                  <div>
                    <strong>All connections</strong>
                    <div class="muted">Everything you have organized</div>
                  </div>
                  <span class="badge">{{ connectionsCount }}</span>
                </button>

                <button
                  class="circles-directory__item"
                  :class="{ 'circles-directory__item--active': activeCircleFilter === 'unassigned' }"
                  type="button"
                  @click="activeCircleFilter = 'unassigned'"
                >
                  <div>
                    <strong>Unassigned</strong>
                    <div class="muted">People not placed anywhere yet</div>
                  </div>
                  <span class="badge">{{ overviewUnassignedConnectionCount }}</span>
                </button>

                <article
                  v-for="circle in circles"
                  :key="circle.id"
                  class="circles-directory__card"
                  :class="{ 'circles-directory__card--active': activeCircleFilter === circle.id }"
                >
                  <button class="circles-directory__item circles-directory__item--card" type="button" @click="activeCircleFilter = circle.id">
                    <div>
                      <strong>{{ circle.name }}</strong>
                      <div class="muted">{{ circle.memberCount }} {{ circle.memberCount === 1 ? "person" : "people" }}</div>
                    </div>
                    <span class="badge badge--accent">{{ circle.memberCount }}</span>
                  </button>

                  <div v-if="circle.members.slice(0, 3).length" class="circles-directory__avatars">
                    <button
                      v-for="member in circle.members.slice(0, 3)"
                      :key="member.userId"
                      class="circles-directory__avatar"
                      type="button"
                      @click="dashboard.openUserProfileDialog(member.userId)"
                    >
                      <ProfileAvatar :username="member.username" :avatar-data-url="member.profileAvatarDataUrl" :size="32" />
                    </button>
                  </div>

                  <button class="button button--ghost circles-directory__delete" type="button" :disabled="isSaving" @click="deleteCircle(circle.id)">
                    Delete
                  </button>
                </article>

                <div v-if="!circles.length" class="empty-state empty-state--soft">Create your first circle to start grouping people.</div>
              </div>
            </aside>

            <section class="circles-panel circles-panel--main">
              <div class="circles-panel__header circles-panel__header--main">
                <div>
                  <h2 class="circles-panel__title">{{ activeCircleName }}</h2>
                  <p class="circles-panel__subtitle">{{ connectionsTotalItems }} matching connection{{ connectionsTotalItems === 1 ? "" : "s" }}</p>
                </div>

                <label class="field circles-panel__search">
                  <span class="label">Search people</span>
                  <input v-model="searchQuery" class="input" placeholder="Username, bio, or circle" />
                </label>
              </div>

              <div v-if="connectionsItems.length" class="circles-connection-list">
                <article v-for="connection in connectionsItems" :key="connection.relationId" class="circles-connection-card">
                  <div class="circles-connection-card__top">
                    <button class="profile-link profile-link--button" type="button" @click="dashboard.openUserProfileDialog(connection.userId)">
                      <ProfileAvatar :username="connection.username" :avatar-data-url="connection.profileAvatarDataUrl" :size="52" />
                      <div class="stack circles-person-card__identity">
                        <strong>{{ connection.username }}</strong>
                        <div class="muted">{{ connection.circleSummaryLabel }}</div>
                      </div>
                    </button>

                    <span v-if="hasPendingCircleChanges(connection)" class="badge badge--warning">Unsaved</span>
                  </div>

                  <ProfileBio :text="connection.profileDescription" placeholder="No profile description." />

                  <div v-if="circles.length" class="circles-chip-cloud">
                    <button
                      v-for="circle in circles"
                      :key="circle.id"
                      class="dashboard-create-job__chip"
                      :class="{ 'dashboard-create-job__chip--active': getSelectedCircleIds(connection).includes(circle.id) }"
                      type="button"
                      :disabled="isSaving"
                      @click="toggleConnectionCircle(connection, circle.id)"
                    >
                      {{ circle.name }}
                    </button>
                  </div>

                  <div class="circles-connection-card__actions">
                    <div class="button-row">
                      <button class="button" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="saveConnectionCircles(connection)">Save</button>
                      <button class="button button--ghost" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="resetConnectionCircles(connection)">Reset</button>
                    </div>

                    <div class="button-row">
                      <button class="button button--secondary" type="button" :disabled="isSaving" @click="removeRequest(connection.relationId, 'warning')">Remove</button>
                      <button class="button button--secondary" type="button" :disabled="isSaving" @click="blockUser(connection.userId)">Block</button>
                    </div>
                  </div>
                </article>
              </div>

              <div v-else class="empty-state circles-panel__empty">
                No connections match this view.
              </div>

              <UiPagination
                v-if="connectionsPages > 1"
                class="mt-4"
                :label="`Page ${connectionsPage} of ${connectionsPages}`"
                :has-previous="connectionsPage > 1"
                :has-next="connectionsPage < connectionsPages"
                @previous="previousConnectionsPage"
                @next="nextConnectionsPage"
              />
            </section>

            <aside class="circles-panel circles-panel--side">
              <section class="circles-panel__stack">
                <div class="circles-panel__header">
                  <div>
                    <h2 class="circles-panel__title">{{ searchHasQuery ? "Search results" : "Suggestions" }}</h2>
                    <p class="circles-panel__subtitle">
                      {{ searchHasQuery ? `${suggestions.length} people found` : `${suggestions.length} people you may know` }}
                    </p>
                  </div>
                </div>

                <div v-if="isSearching" class="empty-state">Searching...</div>
                <div v-else-if="suggestions.length" class="circles-side-list">
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
              </section>

              <section class="circles-panel__stack">
                <div class="circles-panel__header">
                  <div>
                    <h2 class="circles-panel__title">Inbox</h2>
                    <p class="circles-panel__subtitle">{{ currentInboxTotal }} request{{ currentInboxTotal === 1 ? "" : "s" }}</p>
                  </div>

                  <div class="circles-inbox-tabs">
                    <button class="circles-inbox-tabs__button" :class="{ 'circles-inbox-tabs__button--active': inboxTab === 'incoming' }" type="button" @click="inboxTab = 'incoming'">
                      Incoming
                    </button>
                    <button class="circles-inbox-tabs__button" :class="{ 'circles-inbox-tabs__button--active': inboxTab === 'outgoing' }" type="button" @click="inboxTab = 'outgoing'">
                      Outgoing
                    </button>
                  </div>
                </div>

                <div v-if="currentInboxItems.length" class="circles-inbox-list">
                  <article v-for="request in currentInboxItems" :key="request.id" class="circles-inbox-card">
                    <button
                      class="profile-link profile-link--button"
                      type="button"
                      @click="dashboard.openUserProfileDialog(request.counterpartUserId)"
                    >
                      <ProfileAvatar
                        :username="request.counterpartUsername"
                        :avatar-data-url="request.counterpartProfileAvatarDataUrl"
                        :size="44"
                      />
                      <div class="stack circles-person-card__identity">
                        <strong>{{ request.counterpartUsername }}</strong>
                        <div class="muted">{{ request.requestSummaryLabel }}</div>
                      </div>
                    </button>

                    <div class="button-row circles-inbox-card__actions">
                      <template v-if="inboxTab === 'incoming'">
                        <button class="button" type="button" :disabled="isSaving" @click="acceptRequest(request.id)">Accept</button>
                        <button class="button button--ghost" type="button" :disabled="isSaving" @click="removeRequest(request.id)">Decline</button>
                      </template>
                      <template v-else>
                        <button class="button button--ghost" type="button" :disabled="isSaving" @click="removeRequest(request.id)">Cancel</button>
                      </template>
                    </div>
                  </article>
                </div>
                <div v-else class="empty-state empty-state--soft">
                  No {{ inboxTab }} requests.
                </div>

                <UiPagination
                  v-if="currentInboxPages > 1"
                  :label="`Page ${currentInboxPage} of ${currentInboxPages}`"
                  :has-previous="currentInboxPage > 1"
                  :has-next="currentInboxPage < currentInboxPages"
                  @previous="previousInboxPage"
                  @next="nextInboxPage"
                />
              </section>
            </aside>
          </div>

          <DashboardProfileDialog :dashboard="dashboard" />
          <UserProfileDialog
            :open="dashboard.userProfileDialogId !== null"
            :user-id="dashboard.userProfileDialogId"
            @close="dashboard.closeUserProfileDialog()"
            @edit-profile="dashboard.openProfileEditDialog()"
          />
        </section>
      </main>
    </div>
  </div>
</template>
