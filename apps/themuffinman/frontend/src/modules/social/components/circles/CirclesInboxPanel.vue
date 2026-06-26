<script setup lang="ts">
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiListItem from "../../../../components/ui/UiListItem.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {CircleRequest} from "../../../workmarket/api/workmarketApi.ts"

defineProps<{
  inboxTab: "incoming" | "outgoing"
  currentInboxItems: CircleRequest[]
  currentInboxPage: number
  currentInboxPages: number
  isSaving: boolean
}>()

const emit = defineEmits<{
  (event: "update:inbox-tab", value: "incoming" | "outgoing"): void
  (event: "open-user", userId: number): void
  (event: "accept", requestId: number): void
  (event: "remove", requestId: number): void
  (event: "previous"): void
  (event: "next"): void
}>()
</script>

<template>
  <UiSurfaceSection
    tag="section"
    class="surface-stack circles-page__panel"
    compact
    plain
    title="Inbox"
  >
    <template #actions>
      <div class="ui-pill-tabs">
        <button class="ui-pill-tabs__button" :class="{ 'ui-pill-tabs__button--active': inboxTab === 'incoming' }" type="button" @click="emit('update:inbox-tab', 'incoming')">
          Incoming
        </button>
        <button class="ui-pill-tabs__button" :class="{ 'ui-pill-tabs__button--active': inboxTab === 'outgoing' }" type="button" @click="emit('update:inbox-tab', 'outgoing')">
          Outgoing
        </button>
      </div>
    </template>

    <div v-if="currentInboxItems.length" class="surface-list">
      <UiListItem v-for="request in currentInboxItems" :key="request.id">
        <template #header>
          <button
            class="profile-link profile-link--button"
            type="button"
            @click="emit('open-user', request.counterpartUserId)"
          >
            <ProfileAvatar
              :username="request.counterpartUsername"
              :avatar-data-url="request.counterpartProfileAvatarDataUrl"
              :size="44"
            />
            <div class="stack stack--xs profile-entity-card__identity">
              <strong>{{ request.counterpartUsername }}</strong>
            </div>
          </button>
        </template>

        <template #actions>
          <template v-if="inboxTab === 'incoming'">
            <button class="button" type="button" :disabled="isSaving" @click="emit('accept', request.id)">Accept</button>
            <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('remove', request.id)">Decline</button>
          </template>
          <template v-else>
            <button class="button button--ghost" type="button" :disabled="isSaving" @click="emit('remove', request.id)">Cancel</button>
          </template>
        </template>
      </UiListItem>
    </div>
    <div v-else class="empty-state empty-state--soft">
      No {{ inboxTab }} requests.
    </div>

    <UiPagination
      v-if="currentInboxPages > 1"
      :label="`Page ${currentInboxPage} of ${currentInboxPages}`"
      :has-previous="currentInboxPage > 1"
      :has-next="currentInboxPage < currentInboxPages"
      @previous="emit('previous')"
      @next="emit('next')"
    />
  </UiSurfaceSection>
</template>
