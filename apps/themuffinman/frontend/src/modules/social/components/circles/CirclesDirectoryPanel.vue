<script setup lang="ts">
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {CircleGroup} from "../../../workmarket/api/workmarketApi.ts"

defineProps<{
  circles: CircleGroup[]
  activeCircleFilter: number | "all" | "unassigned"
  connectionsCount: number
  overviewUnassignedConnectionCount: number
  isSaving: boolean
}>()

const emit = defineEmits<{
  (event: "select-filter", value: number | "all" | "unassigned"): void
  (event: "open-user", userId: number): void
  (event: "delete-circle", circleId: number): void
}>()
</script>

<template>
  <UiSurfaceSection tag="aside" class="ui-sticky" soft title="Your circles">
    <div class="ui-directory-list">
      <button
        class="ui-directory-list__item"
        :class="{ 'ui-directory-list__item--active': activeCircleFilter === 'all' }"
        type="button"
        @click="emit('select-filter', 'all')"
      >
        <div>
          <strong>All connections</strong>
          <div class="muted">Everything you have organized</div>
        </div>
        <span class="badge">{{ connectionsCount }}</span>
      </button>

      <button
        class="ui-directory-list__item"
        :class="{ 'ui-directory-list__item--active': activeCircleFilter === 'unassigned' }"
        type="button"
        @click="emit('select-filter', 'unassigned')"
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
        class="ui-directory-list__card"
        :class="{ 'ui-directory-list__card--active': activeCircleFilter === circle.id }"
      >
        <button class="ui-directory-list__item ui-directory-list__item--card" type="button" @click="emit('select-filter', circle.id)">
          <div>
            <strong>{{ circle.name }}</strong>
            <div class="muted">{{ circle.memberCount }} {{ circle.memberCount === 1 ? "person" : "people" }}</div>
          </div>
          <span class="badge badge--accent">{{ circle.memberCount }}</span>
        </button>

        <div v-if="circle.members.slice(0, 3).length" class="ui-directory-list__avatars">
          <button
            v-for="member in circle.members.slice(0, 3)"
            :key="member.userId"
            class="ui-directory-list__avatar"
            type="button"
            @click="emit('open-user', member.userId)"
          >
            <ProfileAvatar :username="member.username" :avatar-data-url="member.profileAvatarDataUrl" :size="32" />
          </button>
        </div>

        <button class="button button--ghost ui-directory-list__delete" type="button" :disabled="isSaving" @click="emit('delete-circle', circle.id)">
          Delete
        </button>
      </article>

      <div v-if="!circles.length" class="empty-state empty-state--soft">Create your first circle to start grouping people.</div>
    </div>
  </UiSurfaceSection>
</template>
