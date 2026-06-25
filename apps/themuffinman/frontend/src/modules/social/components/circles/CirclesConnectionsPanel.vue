<script setup lang="ts">
import ProfileEntityCard from "../../../../components/profile/ProfileEntityCard.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {CircleContact, CircleGroup} from "../../../workmarket/api/workmarketApi.ts"

defineProps<{
  title: string
  searchQuery: string
  circles: CircleGroup[]
  connectionsItems: CircleContact[]
  connectionsPages: number
  connectionsPage: number
  isSaving: boolean
  getSelectedCircleIds: (connection: CircleContact) => number[]
  hasPendingCircleChanges: (connection: CircleContact) => boolean
}>()

const emit = defineEmits<{
  (event: "update:search-query", value: string): void
  (event: "open-user", userId: number): void
  (event: "toggle-circle", payload: {connection: CircleContact; circleId: number}): void
  (event: "save-connection", connection: CircleContact): void
  (event: "reset-connection", connection: CircleContact): void
  (event: "remove-connection", relationId: number): void
  (event: "block-user", userId: number): void
  (event: "previous-page"): void
  (event: "next-page"): void
}>()
</script>

<template>
  <UiSurfaceSection
    soft
    :title="title"
  >
    <template #actions>
      <UiFieldGroup label="Search people" field-class="ui-search-field">
        <input
          :value="searchQuery"
          class="input"
          placeholder="Username, bio, or circle"
          @input="emit('update:search-query', ($event.target as HTMLInputElement).value)"
        />
      </UiFieldGroup>
    </template>

    <div v-if="connectionsItems.length" class="surface-list">
      <ProfileEntityCard
        v-for="connection in connectionsItems"
        :key="connection.relationId"
        :username="connection.username"
        :avatar-data-url="connection.profileAvatarDataUrl"
        :meta="connection.circleSummaryLabel"
        :description="connection.profileDescription"
        :size="52"
        @open="emit('open-user', connection.userId)"
      >
        <template #badge>
          <span v-if="hasPendingCircleChanges(connection)" class="badge badge--warning">Unsaved</span>
        </template>

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

        <template #actions>
          <div class="button-row">
            <button class="button" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="emit('save-connection', connection)">Save</button>
            <button class="button button--ghost" type="button" :disabled="isSaving || !hasPendingCircleChanges(connection)" @click="emit('reset-connection', connection)">Reset</button>
          </div>

          <div class="button-row">
            <button class="button button--secondary" type="button" :disabled="isSaving" @click="emit('remove-connection', connection.relationId)">Remove</button>
            <button class="button button--secondary" type="button" :disabled="isSaving" @click="emit('block-user', connection.userId)">Block</button>
          </div>
        </template>
      </ProfileEntityCard>
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
