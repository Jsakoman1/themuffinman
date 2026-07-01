<script setup lang="ts">
import {computed, ref} from "vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {CircleContact, CircleGroup} from "../../../../contracts/index.ts"

const props = defineProps<{
  circles: CircleGroup[]
  activeCircleFilter: number | "all" | "unassigned"
  connectionsItems: CircleContact[]
  connectionsPages: number
  connectionsPage: number
  connectionsTotalItems: number
  connectionsCount: number
  overviewUnassignedConnectionCount: number
  isSaving: boolean
  getSelectedCircleIds: (connection: CircleContact) => number[]
  hasPendingCircleChanges: (connection: CircleContact) => boolean
}>()

const emit = defineEmits<{
  (event: "select-filter", value: number | "all" | "unassigned"): void
  (event: "open-user", userId: number): void
  (event: "toggle-circle", payload: {connection: CircleContact; circleId: number}): void
  (event: "save-connection", connection: CircleContact): void
  (event: "reset-connection", connection: CircleContact): void
  (event: "bulk-assign", payload: {circleId: number; userIds: number[]; action: "ADD" | "REMOVE"}): void
  (event: "remove-connection", relationId: number): void
  (event: "block-user", userId: number): void
  (event: "previous-page"): void
  (event: "next-page"): void
}>()

const bulkCircleId = ref<number | null>(null)
const visibleUserIds = computed(() => props.connectionsItems.map((connection) => connection.userId))
const visibleCountLabel = computed(() => `${props.connectionsItems.length} of ${props.connectionsTotalItems}`)

const runBulkAssign = (action: "ADD" | "REMOVE") => {
  if (bulkCircleId.value === null || visibleUserIds.value.length === 0) {
    return
  }

  emit("bulk-assign", {circleId: bulkCircleId.value, userIds: visibleUserIds.value, action})
}
</script>

<template>
  <UiSurfaceSection
    plain
    title="People"
  >
    <template #actions>
      <span class="muted">{{ visibleCountLabel }}</span>
    </template>

    <div class="circles-filter-tabs">
      <button
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === 'all' }"
        type="button"
        @click="emit('select-filter', 'all')"
      >
        All
        <span class="badge">{{ connectionsCount }}</span>
      </button>

      <button
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === 'unassigned' }"
        type="button"
        @click="emit('select-filter', 'unassigned')"
      >
        Unassigned
        <span class="badge">{{ overviewUnassignedConnectionCount }}</span>
      </button>

      <button
        v-for="circle in circles"
        :key="circle.id"
        class="circles-filter-tabs__button"
        :class="{ 'circles-filter-tabs__button--active': activeCircleFilter === circle.id }"
        type="button"
        @click="emit('select-filter', circle.id)"
      >
        {{ circle.name }}
        <span class="badge badge--accent">{{ circle.memberCount }}</span>
      </button>
    </div>

    <div v-if="circles.length && connectionsItems.length" class="circles-bulk-bar">
      <select v-model="bulkCircleId" class="input circles-bulk-bar__select">
        <option :value="null" disabled>Choose circle</option>
        <option v-for="circle in circles" :key="circle.id" :value="circle.id">{{ circle.name }}</option>
      </select>
      <button class="button" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('ADD')">Add all</button>
      <button class="button button--ghost" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('REMOVE')">Remove all</button>
    </div>

    <div v-if="connectionsItems.length" class="surface-list">
      <article v-for="connection in connectionsItems" :key="connection.relationId" class="circles-connection-row">
        <button class="circles-connection-row__identity" type="button" @click="emit('open-user', connection.userId)">
          <ProfileAvatar
            :username="connection.username"
            :avatar-data-url="connection.profileAvatarDataUrl"
            :size="46"
          />
          <div class="stack stack--xs">
            <div class="circles-connection-row__title">
              <strong>{{ connection.username }}</strong>
              <span v-if="hasPendingCircleChanges(connection)" class="badge badge--warning">Unsaved</span>
            </div>
            <div class="muted">{{ connection.circleSummaryLabel }}</div>
          </div>
        </button>

        <div v-if="circles.length" class="circles-chip-cloud">
          <button
            v-for="circle in circles"
            :key="circle.id"
            class="ui-chip"
            :class="{ 'ui-chip--active': getSelectedCircleIds(connection).includes(circle.id) }"
            type="button"
            :disabled="isSaving"
            @click="emit('toggle-circle', {connection, circleId: circle.id})"
          >
            {{ circle.name }}
          </button>
        </div>

        <div class="circles-connection-row__actions">
          <template v-if="hasPendingCircleChanges(connection)">
            <button class="button" type="button" :disabled="isSaving" @click="emit('save-connection', connection)">Save</button>
            <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('reset-connection', connection)">Reset</button>
          </template>
          <button class="button button--secondary" type="button" :disabled="isSaving" @click="emit('remove-connection', connection.relationId)">Remove</button>
          <button class="button button--secondary" type="button" :disabled="isSaving" @click="emit('block-user', connection.userId)">Block</button>
        </div>
      </article>
    </div>

    <div v-else class="empty-state surface-empty-panel">
      No connections match this view.
    </div>

    <UiPagination
      v-if="connectionsPages > 1"
      class="mt-4"
      :label="`Page ${connectionsPage} of ${connectionsPages}`"
      :has-previous="connectionsPage > 1"
      :has-next="connectionsPage < connectionsPages"
      @previous="emit('previous-page')"
      @next="emit('next-page')"
    />
  </UiSurfaceSection>
</template>
