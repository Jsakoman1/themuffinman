<script setup lang="ts">
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiListItem from "../../../../components/ui/UiListItem.vue"
import UiPagination from "../../../../components/ui/UiPagination.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {CircleRequest, ProfilePrimaryAction} from "../../../../contracts/index.ts"

defineProps<{
  incomingItems: CircleRequest[]
  incomingPage: number
  incomingPages: number
  incomingTotal?: number
  outgoingItems: CircleRequest[]
  outgoingPage: number
  outgoingPages: number
  outgoingTotal?: number
  incomingCount?: number
  outgoingCount?: number
  isSaving: boolean
}>()

const emit = defineEmits<{
  (event: "open-user", userId: number): void
  (event: "accept", requestId: number): void
  (event: "remove", requestId: number): void
  (event: "previous-incoming"): void
  (event: "next-incoming"): void
  (event: "previous-outgoing"): void
  (event: "next-outgoing"): void
}>()

const handleAction = (requestId: number, action: ProfilePrimaryAction | null | undefined) => {
  switch (action?.type) {
    case "ACCEPT_REQUEST":
      emit("accept", requestId)
      return
    case "DECLINE_REQUEST":
    case "CANCEL_REQUEST":
      emit("remove", requestId)
      return
    default:
      return
  }
}
</script>

<template>
  <UiSurfaceSection
    tag="section"
    class="surface-stack circles-page__panel"
    compact
    plain
    title="Requests"
  >
    <template #actions>
      <div class="circles-section-actions">
        <span class="badge">{{ incomingCount ?? 0 }} incoming</span>
        <span class="badge">{{ outgoingCount ?? 0 }} outgoing</span>
      </div>
    </template>

    <div class="circles-inbox-grid">
      <section class="surface-stack">
        <div class="circles-subsection-heading">
          <strong>Incoming</strong>
          <span class="muted">{{ incomingTotal ?? 0 }}</span>
        </div>

        <div v-if="incomingItems.length" class="surface-list">
          <UiListItem v-for="request in incomingItems" :key="request.id">
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
              <button
                v-if="request.primaryAction?.label"
                class="button"
                type="button"
                :disabled="isSaving || !request.primaryAction.enabled"
                @click="handleAction(request.id, request.primaryAction)"
              >
                {{ request.primaryAction.label }}
              </button>
              <button
                v-if="request.secondaryAction?.label"
                class="button button--ghost"
                type="button"
                :disabled="isSaving || !request.secondaryAction.enabled"
                @click="handleAction(request.id, request.secondaryAction)"
              >
                {{ request.secondaryAction.label }}
              </button>
            </template>
          </UiListItem>
        </div>
        <div v-else class="empty-state empty-state--soft">
          No incoming requests.
        </div>

        <UiPagination
          v-if="incomingPages > 1"
          :label="`Page ${incomingPage} of ${incomingPages}`"
          :has-previous="incomingPage > 1"
          :has-next="incomingPage < incomingPages"
          @previous="emit('previous-incoming')"
          @next="emit('next-incoming')"
        />
      </section>

      <section class="surface-stack">
        <div class="circles-subsection-heading">
          <strong>Outgoing</strong>
          <span class="muted">{{ outgoingTotal ?? 0 }}</span>
        </div>

        <div v-if="outgoingItems.length" class="surface-list">
          <UiListItem v-for="request in outgoingItems" :key="request.id">
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
              <button
                v-if="request.primaryAction?.label"
                class="button button--ghost"
                type="button"
                :disabled="isSaving || !request.primaryAction.enabled"
                @click="handleAction(request.id, request.primaryAction)"
              >
                {{ request.primaryAction.label }}
              </button>
              <button
                v-if="request.secondaryAction?.label"
                class="button"
                type="button"
                :disabled="isSaving || !request.secondaryAction.enabled"
                @click="handleAction(request.id, request.secondaryAction)"
              >
                {{ request.secondaryAction.label }}
              </button>
            </template>
          </UiListItem>
        </div>
        <div v-else class="empty-state empty-state--soft">
          No outgoing requests.
        </div>

        <UiPagination
          v-if="outgoingPages > 1"
          :label="`Page ${outgoingPage} of ${outgoingPages}`"
          :has-previous="outgoingPage > 1"
          :has-next="outgoingPage < outgoingPages"
          @previous="emit('previous-outgoing')"
          @next="emit('next-outgoing')"
        />
      </section>
    </div>
  </UiSurfaceSection>
</template>
