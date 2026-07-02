<script setup lang="ts">
import {computed, ref} from "vue"
import {getProfileInitials} from "../../../../shared/profileFormatting.ts"
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
const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})

const runBulkAssign = (action: "ADD" | "REMOVE") => {
  if (bulkCircleId.value === null || visibleUserIds.value.length === 0) {
    return
  }

  emit("bulk-assign", {circleId: bulkCircleId.value, userIds: visibleUserIds.value, action})
}
</script>

<template>
  <section class="vision-terminal-feed__block">
    <p class="vision-terminal-feed__block-title">people</p>
    <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ visibleCountLabel }}</p>

    <div class="vision-terminal-feed__action-row">
      <button
        class="vision-terminal-feed__link-button"
        type="button"
        :class="{ 'vision-terminal-feed__list-button--active': activeCircleFilter === 'all' }"
        @click="emit('select-filter', 'all')"
      >
        All ({{ connectionsCount }})
      </button>
      <button
        class="vision-terminal-feed__link-button"
        type="button"
        :class="{ 'vision-terminal-feed__list-button--active': activeCircleFilter === 'unassigned' }"
        @click="emit('select-filter', 'unassigned')"
      >
        Unassigned ({{ overviewUnassignedConnectionCount }})
      </button>
      <button
        v-for="circle in circles"
        :key="circle.id"
        class="vision-terminal-feed__link-button"
        type="button"
        :class="{ 'vision-terminal-feed__list-button--active': activeCircleFilter === circle.id }"
        @click="emit('select-filter', circle.id)"
      >
        {{ circle.name }} ({{ circle.memberCount }})
      </button>
    </div>

    <div v-if="circles.length && connectionsItems.length" class="vision-terminal-feed__action-row">
      <select v-model="bulkCircleId" class="input">
        <option :value="null" disabled>Choose circle</option>
        <option v-for="circle in circles" :key="circle.id" :value="circle.id">{{ circle.name }}</option>
      </select>
      <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('ADD')">Add all</button>
      <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || bulkCircleId === null" @click="runBulkAssign('REMOVE')">Remove all</button>
    </div>

    <article v-for="connection in connectionsItems" :key="connection.relationId" class="vision-terminal-feed__entry">
      <button class="vision-terminal-feed__link-button" type="button" @click="emit('open-user', connection.userId)">
        <span class="profile-avatar" :style="profileAvatarStyle(44)">
          <img v-if="connection.profileAvatarDataUrl" class="profile-avatar__image" :src="connection.profileAvatarDataUrl" :alt="`${connection.username || 'User'} avatar`" />
          <span v-else class="profile-avatar__fallback">{{ getProfileInitials(connection.username) }}</span>
        </span>
      </button>
      <div class="vision-terminal-feed__entry-body">
        <p class="vision-terminal-feed__line">{{ connection.username }}</p>
        <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ connection.circleSummaryLabel }}</p>
      </div>
      <div class="vision-terminal-feed__action-row">
        <button
          v-for="circle in circles"
          :key="circle.id"
          class="vision-terminal-feed__link-button"
          type="button"
          :class="{ 'vision-terminal-feed__list-button--active': getSelectedCircleIds(connection).includes(circle.id) }"
          :disabled="isSaving"
          @click="emit('toggle-circle', {connection, circleId: circle.id})"
        >
          {{ circle.name }}
        </button>
        <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving" @click="emit('save-connection', connection)">Save</button>
        <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving" @click="emit('reset-connection', connection)">Reset</button>
        <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving" @click="emit('remove-connection', connection.relationId)">Remove</button>
        <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving" @click="emit('block-user', connection.userId)">Block</button>
      </div>
    </article>

    <p v-if="!connectionsItems.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No connections match this view.</p>

    <div v-if="connectionsPages > 1" class="vision-pagination">
      <div class="muted">{{ `Page ${connectionsPage} of ${connectionsPages}` }}</div>
      <div class="button-row">
        <button class="button button--secondary" type="button" :disabled="connectionsPage <= 1" @click="emit('previous-page')">Previous</button>
        <button class="button button--secondary" type="button" :disabled="connectionsPage >= connectionsPages" @click="emit('next-page')">Next</button>
      </div>
    </div>
  </section>
</template>
