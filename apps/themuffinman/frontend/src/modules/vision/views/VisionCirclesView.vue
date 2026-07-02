<script setup lang="ts">
import {computed, ref} from "vue"
import {useRouter} from "vue-router"
import {useCirclesView} from "../composables/useCirclesView.ts"
import type {CircleContact, CircleRequest, ProfilePrimaryAction} from "../../../contracts/index.ts"

const {
  circles,
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
  overviewUnassignedConnectionCount,
  incomingItems,
  incomingPage,
  incomingPages,
  outgoingItems,
  outgoingPage,
  outgoingPages,
  blockedItems,
  blockedPages,
  blockedPage,
  nearbyItems,
  nearbyPages,
  nearbyPage,
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
const bulkCircleId = ref<number | null>(null)

const openUserProfile = async (userId: number) => {
  await router.push(`/vision/users/${userId}`)
}

const closeCircles = async () => {
  await router.push("/vision")
}

const filterButtons = computed(() => [
  {label: "All", value: "all" as const, count: connectionsCount.value},
  {label: "Unassigned", value: "unassigned" as const, count: overviewUnassignedConnectionCount.value},
  ...circles.value.map((circle) => ({label: circle.name, value: circle.id, count: circle.memberCount}))
])

const handleRequestAction = (requestId: number, action: ProfilePrimaryAction | null | undefined) => {
  switch (action?.type) {
    case "ACCEPT_REQUEST":
      acceptRequest(requestId)
      return
    case "DECLINE_REQUEST":
    case "CANCEL_REQUEST":
      removeRequest(requestId)
      return
    default:
      return
  }
}

const circleToggleState = (connection: CircleContact, circleId: number) => getSelectedCircleIds(connection).includes(circleId)

const renameCirclePrompt = (circleId: number, currentName: string) => {
  if (typeof window === "undefined") {
    return
  }

  const nextName = window.prompt("Rename circle", currentName)
  if (nextName !== null && nextName.trim()) {
    renameCircle(circleId, nextName.trim())
  }
}

const runBulkAssign = (action: "ADD" | "REMOVE") => {
  if (bulkCircleId.value === null || connectionsItems.value.length === 0) {
    return
  }

  bulkUpdateConnections(bulkCircleId.value, connectionsItems.value.map((connection) => connection.userId), action)
}

const requestMeta = (request: CircleRequest) => [
  request.counterpartUsername,
  request.counterpartProfileAvatarDataUrl ? "avatar" : "",
  request.primaryAction?.label ?? "",
  request.secondaryAction?.label ?? ""
].filter(Boolean).join(" · ")
</script>

<template>
  <section class="vision-circles-terminal">
    <header class="vision-circles-terminal__header">
      <div>
        <p class="vision-circles-terminal__eyebrow">Vision</p>
        <h1 class="vision-circles-terminal__title">Circles</h1>
      </div>
      <button class="vision-circles-terminal__back" type="button" @click="closeCircles">
        > back
      </button>
    </header>

    <div class="vision-circles-terminal__feed">
      <p class="vision-circles-terminal__line">> circles</p>
      <p v-if="message" :class="['vision-circles-terminal__line', `vision-circles-terminal__line--${messageTone}`]">
        {{ message }}
      </p>
      <p v-if="error" class="vision-circles-terminal__line vision-circles-terminal__line--error">{{ error }}</p>
      <p v-else-if="isLoading" class="vision-circles-terminal__line">loading circles...</p>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">requests</p>
        <p class="vision-circles-terminal__line">incoming {{ incomingCount }} · outgoing {{ outgoingCount }}</p>

        <div class="vision-circles-terminal__subblock">
          <p class="vision-circles-terminal__subblock-title">incoming</p>
          <div v-if="incomingItems.length" class="vision-circles-terminal__rows">
            <div v-for="request in incomingItems" :key="request.id" class="vision-circles-terminal__row">
              <div class="vision-circles-terminal__row-main">
                <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(request.counterpartUserId)">
                  @{{ request.counterpartUsername }}
                </button>
                <span class="vision-circles-terminal__muted">{{ requestMeta(request) }}</span>
              </div>
              <div class="vision-circles-terminal__row-actions">
                <button
                  v-if="request.primaryAction?.label"
                  class="vision-circles-terminal__action"
                  type="button"
                  :disabled="isSaving || !request.primaryAction.enabled"
                  @click="handleRequestAction(request.id, request.primaryAction)"
                >
                  {{ request.primaryAction.label }}
                </button>
                <button
                  v-if="request.secondaryAction?.label"
                  class="vision-circles-terminal__action vision-circles-terminal__action--ghost"
                  type="button"
                  :disabled="isSaving || !request.secondaryAction.enabled"
                  @click="handleRequestAction(request.id, request.secondaryAction)"
                >
                  {{ request.secondaryAction.label }}
                </button>
              </div>
            </div>
          </div>
          <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">No incoming requests.</p>

          <div v-if="incomingPages > 1" class="vision-pagination vision-circles-terminal__pagination">
            <div class="muted">{{ `Page ${incomingPage} of ${incomingPages}` }}</div>
            <div class="button-row">
              <button class="button button--secondary" type="button" :disabled="incomingPage <= 1" @click="previousIncomingPage">Previous</button>
              <button class="button button--secondary" type="button" :disabled="incomingPage >= incomingPages" @click="nextIncomingPage">Next</button>
            </div>
          </div>
        </div>

        <div class="vision-circles-terminal__subblock">
          <p class="vision-circles-terminal__subblock-title">outgoing</p>
          <div v-if="outgoingItems.length" class="vision-circles-terminal__rows">
            <div v-for="request in outgoingItems" :key="request.id" class="vision-circles-terminal__row">
              <div class="vision-circles-terminal__row-main">
                <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(request.counterpartUserId)">
                  @{{ request.counterpartUsername }}
                </button>
                <span class="vision-circles-terminal__muted">{{ requestMeta(request) }}</span>
              </div>
              <div class="vision-circles-terminal__row-actions">
                <button
                  v-if="request.primaryAction?.label"
                  class="vision-circles-terminal__action vision-circles-terminal__action--ghost"
                  type="button"
                  :disabled="isSaving || !request.primaryAction.enabled"
                  @click="handleRequestAction(request.id, request.primaryAction)"
                >
                  {{ request.primaryAction.label }}
                </button>
                <button
                  v-if="request.secondaryAction?.label"
                  class="vision-circles-terminal__action"
                  type="button"
                  :disabled="isSaving || !request.secondaryAction.enabled"
                  @click="handleRequestAction(request.id, request.secondaryAction)"
                >
                  {{ request.secondaryAction.label }}
                </button>
              </div>
            </div>
          </div>
          <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">No outgoing requests.</p>

          <div v-if="outgoingPages > 1" class="vision-pagination vision-circles-terminal__pagination">
            <div class="muted">{{ `Page ${outgoingPage} of ${outgoingPages}` }}</div>
            <div class="button-row">
              <button class="button button--secondary" type="button" :disabled="outgoingPage <= 1" @click="previousOutgoingPage">Previous</button>
              <button class="button button--secondary" type="button" :disabled="outgoingPage >= outgoingPages" @click="nextOutgoingPage">Next</button>
            </div>
          </div>
        </div>
      </section>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">circles</p>
        <form class="vision-circles-terminal__inline-form" @submit.prevent="createCircle">
          <input
            v-model="newCircleName"
            class="vision-circles-terminal__input"
            maxlength="80"
            placeholder="new circle name"
          />
          <button class="vision-circles-terminal__action" type="submit" :disabled="isSaving || !newCircleName.trim()">
            add
          </button>
        </form>

        <div v-if="circles.length" class="vision-circles-terminal__rows">
          <div v-for="circle in circles" :key="circle.id" class="vision-circles-terminal__row">
            <div class="vision-circles-terminal__row-main">
              <strong>{{ circle.name }}</strong>
              <span class="vision-circles-terminal__muted">{{ circle.memberCount }} members</span>
            </div>
            <div class="vision-circles-terminal__row-actions">
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving" @click="renameCirclePrompt(circle.id, circle.name)">
                rename
              </button>
              <button class="vision-circles-terminal__action" type="button" :disabled="isSaving" @click="deleteCircle(circle.id)">
                delete
              </button>
            </div>
          </div>
        </div>
        <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">Create your first circle to start grouping people.</p>
      </section>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">connections</p>
        <div class="vision-circles-terminal__filter-row">
          <button
            v-for="button in filterButtons"
            :key="String(button.value)"
            class="vision-circles-terminal__action"
            :class="{'vision-circles-terminal__action--active': activeCircleFilter === button.value}"
            type="button"
            @click="activeCircleFilter = button.value"
          >
            {{ button.label }} {{ button.count }}
          </button>
        </div>

        <div v-if="circles.length && connectionsItems.length" class="vision-circles-terminal__bulk-row">
          <select v-model="bulkCircleId" class="vision-circles-terminal__input vision-circles-terminal__input--compact">
            <option :value="null" disabled>choose circle</option>
            <option v-for="circle in circles" :key="circle.id" :value="circle.id">{{ circle.name }}</option>
          </select>
          <button class="vision-circles-terminal__action" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('ADD')">add visible</button>
          <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('REMOVE')">remove visible</button>
        </div>

        <div v-if="connectionsItems.length" class="vision-circles-terminal__rows">
          <div v-for="connection in connectionsItems" :key="connection.relationId" class="vision-circles-terminal__row vision-circles-terminal__row--stacked">
            <div class="vision-circles-terminal__row-main">
              <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(connection.userId)">
                @{{ connection.username }}
              </button>
              <span class="vision-circles-terminal__muted">{{ connection.circleSummaryLabel }}</span>
              <span v-if="hasPendingCircleChanges(connection)" class="vision-circles-terminal__muted">unsaved</span>
            </div>

            <div v-if="circles.length" class="vision-circles-terminal__filter-row">
              <button
                v-for="circle in circles"
                :key="circle.id"
                class="vision-circles-terminal__action"
                :class="{'vision-circles-terminal__action--active': circleToggleState(connection, circle.id)}"
                type="button"
                :disabled="isSaving"
                @click="toggleConnectionCircle(connection, circle.id)"
              >
                {{ circle.name }}
              </button>
            </div>

            <div class="vision-circles-terminal__row-actions">
              <button class="vision-circles-terminal__action" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="saveConnectionCircles(connection)">
                save
              </button>
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="resetConnectionCircles(connection)">
                reset
              </button>
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving" @click="removeRequest(connection.relationId)">
                remove
              </button>
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving" @click="blockUser(connection.userId)">
                block
              </button>
            </div>
          </div>
        </div>
        <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">No connections match this view.</p>

        <div v-if="connectionsPages > 1" class="vision-pagination vision-circles-terminal__pagination">
          <div class="muted">{{ `Page ${connectionsPage} of ${connectionsPages}` }}</div>
          <div class="button-row">
            <button class="button button--secondary" type="button" :disabled="connectionsPage <= 1" @click="previousConnectionsPage">Previous</button>
            <button class="button button--secondary" type="button" :disabled="connectionsPage >= connectionsPages" @click="nextConnectionsPage">Next</button>
          </div>
        </div>
      </section>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">discover</p>
        <input
          v-model="discoverQuery"
          class="vision-circles-terminal__input"
          placeholder="search by username or email"
        />

        <p class="vision-circles-terminal__line vision-circles-terminal__line--soft">
          {{ isSearching ? "searching..." : discoverHasQuery ? "search results" : "suggested people" }}
        </p>

        <div v-if="suggestions.length" class="vision-circles-terminal__rows">
          <div v-for="user in suggestions" :key="user.id" class="vision-circles-terminal__row">
            <div class="vision-circles-terminal__row-main">
              <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(user.id)">
                @{{ user.username }}
              </button>
              <span class="vision-circles-terminal__muted">{{ [user.distanceLabel, user.locationLabel, user.email].filter(Boolean).join(" · ") }}</span>
            </div>
            <div class="vision-circles-terminal__row-actions">
              <button class="vision-circles-terminal__action" type="button" :disabled="isSaving" @click="sendRequest(user.id)">
                invite
              </button>
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving" @click="blockUser(user.id)">
                block
              </button>
            </div>
          </div>
        </div>
        <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">
          {{ discoverHasQuery ? "No people match your search." : "No suggestions right now." }}
        </p>
      </section>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">nearby</p>
        <div v-if="nearbyItems.length" class="vision-circles-terminal__rows">
          <div v-for="user in nearbyItems" :key="`nearby-${user.id}`" class="vision-circles-terminal__row">
            <div class="vision-circles-terminal__row-main">
              <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(user.id)">
                @{{ user.username }}
              </button>
              <span class="vision-circles-terminal__muted">{{ [user.distanceLabel, user.locationLabel, user.email].filter(Boolean).join(" · ") }}</span>
            </div>
            <div class="vision-circles-terminal__row-actions">
              <button class="vision-circles-terminal__action" type="button" :disabled="isSaving" @click="sendRequest(user.id)">
                invite
              </button>
              <button class="vision-circles-terminal__action vision-circles-terminal__action--ghost" type="button" :disabled="isSaving" @click="blockUser(user.id)">
                block
              </button>
            </div>
          </div>
        </div>
        <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">No nearby users.</p>

        <div v-if="nearbyPages > 1" class="vision-pagination vision-circles-terminal__pagination">
          <div class="muted">{{ `Page ${nearbyPage} of ${nearbyPages}` }}</div>
          <div class="button-row">
            <button class="button button--secondary" type="button" :disabled="nearbyPage <= 1" @click="previousNearbyPage">Previous</button>
            <button class="button button--secondary" type="button" :disabled="nearbyPage >= nearbyPages" @click="nextNearbyPage">Next</button>
          </div>
        </div>
      </section>

      <section class="vision-circles-terminal__block">
        <p class="vision-circles-terminal__block-title">blocked</p>
        <div v-if="blockedItems.length" class="vision-circles-terminal__rows">
          <div v-for="user in blockedItems" :key="`blocked-${user.id}`" class="vision-circles-terminal__row">
            <div class="vision-circles-terminal__row-main">
              <button class="vision-circles-terminal__link" type="button" @click="openUserProfile(user.id)">
                @{{ user.username }}
              </button>
              <span class="vision-circles-terminal__muted">{{ [user.distanceLabel, user.locationLabel, user.email].filter(Boolean).join(" · ") }}</span>
            </div>
            <div class="vision-circles-terminal__row-actions">
              <button class="vision-circles-terminal__action" type="button" :disabled="isSaving" @click="unblockUser(user.id)">
                unblock
              </button>
            </div>
          </div>
        </div>
        <p v-else class="vision-circles-terminal__line vision-circles-terminal__line--soft">No blocked users.</p>

        <div v-if="blockedPages > 1" class="vision-pagination vision-circles-terminal__pagination">
          <div class="muted">{{ `Page ${blockedPage} of ${blockedPages}` }}</div>
          <div class="button-row">
            <button class="button button--secondary" type="button" :disabled="blockedPage <= 1" @click="previousBlockedPage">Previous</button>
            <button class="button button--secondary" type="button" :disabled="blockedPage >= blockedPages" @click="nextBlockedPage">Next</button>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.vision-circles-terminal {
  width: min(72rem, 100%);
  display: grid;
  gap: 1rem;
}

.vision-circles-terminal__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.vision-circles-terminal__eyebrow {
  margin: 0 0 0.15rem;
  color: var(--vision-surface-ink-muted);
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.vision-circles-terminal__title {
  margin: 0;
  font-size: clamp(1.7rem, 2.8vw, 2.4rem);
  letter-spacing: -0.05em;
}

.vision-circles-terminal__back {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: rgba(24, 36, 47, 0.68);
  font: inherit;
  font-size: 0.76rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
}

.vision-circles-terminal__feed {
  display: grid;
  gap: 1rem;
}

.vision-circles-terminal__line {
  margin: 0;
  color: var(--vision-surface-ink);
  white-space: pre-wrap;
  line-height: 1.5;
}

.vision-circles-terminal__line--soft,
.vision-circles-terminal__muted {
  color: var(--vision-surface-ink-soft);
}

.vision-circles-terminal__line--error {
  color: #b04f43;
}

.vision-circles-terminal__block {
  display: grid;
  gap: 0.75rem;
  padding-top: 0.15rem;
}

.vision-circles-terminal__block-title,
.vision-circles-terminal__subblock-title {
  margin: 0;
  color: rgba(24, 36, 47, 0.48);
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.vision-circles-terminal__subblock {
  display: grid;
  gap: 0.5rem;
}

.vision-circles-terminal__rows {
  display: grid;
  gap: 0.55rem;
}

.vision-circles-terminal__row {
  display: grid;
  gap: 0.45rem;
  padding-left: 0.35rem;
}

.vision-circles-terminal__row--stacked {
  gap: 0.55rem;
}

.vision-circles-terminal__row-main {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.5rem;
}

.vision-circles-terminal__row-actions,
.vision-circles-terminal__filter-row,
.vision-circles-terminal__bulk-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-circles-terminal__inline-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.vision-circles-terminal__input {
  min-width: min(24rem, 100%);
  border: 0;
  border-bottom: 1px solid rgba(24, 36, 47, 0.16);
  border-radius: 0;
  background: transparent;
  padding: 0.45rem 0;
  font: inherit;
  color: var(--vision-surface-ink);
  outline: none;
}

.vision-circles-terminal__input--compact {
  min-width: min(12rem, 100%);
}

.vision-circles-terminal__input::placeholder {
  color: rgba(24, 36, 47, 0.28);
}

.vision-circles-terminal__action {
  appearance: none;
  border: 0;
  border-bottom: 1px solid rgba(24, 36, 47, 0.18);
  background: transparent;
  color: var(--vision-surface-ink);
  padding: 0.28rem 0;
  font: inherit;
  cursor: pointer;
}

.vision-circles-terminal__action--ghost {
  color: rgba(24, 36, 47, 0.68);
}

.vision-circles-terminal__action--active {
  border-bottom-color: var(--vision-surface-ink);
  color: var(--vision-surface-ink);
  font-weight: 700;
}

.vision-circles-terminal__link {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 0.18em;
}

.vision-circles-terminal__pagination {
  margin-top: 0.35rem;
}

@media (max-width: 720px) {
  .vision-circles-terminal__header {
    flex-direction: column;
  }
}
</style>
