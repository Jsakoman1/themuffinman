<script setup lang="ts">
import {getProfileInitials} from "../../../../shared/profileFormatting.ts"
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

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})
</script>

<template>
  <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">requests</p>
      <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ incomingCount ?? 0 }} incoming · {{ outgoingCount ?? 0 }} outgoing</p>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__line">Incoming</p>
      <article v-for="request in incomingItems" :key="request.id" class="vision-terminal-feed__entry">
        <span class="profile-avatar" :style="profileAvatarStyle(44)">
          <img v-if="request.counterpartProfileAvatarDataUrl" class="profile-avatar__image" :src="request.counterpartProfileAvatarDataUrl" :alt="`${request.counterpartUsername || 'User'} avatar`" />
          <span v-else class="profile-avatar__fallback">{{ getProfileInitials(request.counterpartUsername) }}</span>
        </span>
        <div class="vision-terminal-feed__entry-body">
          <p class="vision-terminal-feed__line">{{ request.counterpartUsername }}</p>
          <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ request.primaryAction?.label ?? "Pending" }}</p>
        </div>
        <div class="vision-terminal-feed__action-row">
          <button
            v-if="request.primaryAction?.label"
            class="vision-terminal-feed__link-button"
            type="button"
            :disabled="isSaving || !request.primaryAction.enabled"
            @click="handleAction(request.id, request.primaryAction)"
          >
            {{ request.primaryAction.label }}
          </button>
          <button
            v-if="request.secondaryAction?.label"
            class="vision-terminal-feed__link-button"
            type="button"
            :disabled="isSaving || !request.secondaryAction.enabled"
            @click="handleAction(request.id, request.secondaryAction)"
          >
            {{ request.secondaryAction.label }}
          </button>
        </div>
      </article>
      <p v-if="!incomingItems.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No incoming requests.</p>
      <div v-if="incomingPages > 1" class="vision-pagination">
        <div class="muted">{{ `Page ${incomingPage} of ${incomingPages}` }}</div>
        <div class="button-row">
          <button class="button button--secondary" type="button" :disabled="incomingPage <= 1" @click="emit('previous-incoming')">Previous</button>
          <button class="button button--secondary" type="button" :disabled="incomingPage >= incomingPages" @click="emit('next-incoming')">Next</button>
        </div>
      </div>
    </section>

    <section class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__line">Outgoing</p>
      <article v-for="request in outgoingItems" :key="request.id" class="vision-terminal-feed__entry">
        <span class="profile-avatar" :style="profileAvatarStyle(44)">
          <img v-if="request.counterpartProfileAvatarDataUrl" class="profile-avatar__image" :src="request.counterpartProfileAvatarDataUrl" :alt="`${request.counterpartUsername || 'User'} avatar`" />
          <span v-else class="profile-avatar__fallback">{{ getProfileInitials(request.counterpartUsername) }}</span>
        </span>
        <div class="vision-terminal-feed__entry-body">
          <p class="vision-terminal-feed__line">{{ request.counterpartUsername }}</p>
          <p class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ request.primaryAction?.label ?? "Pending" }}</p>
        </div>
        <div class="vision-terminal-feed__action-row">
          <button
            v-if="request.primaryAction?.label"
            class="vision-terminal-feed__link-button"
            type="button"
            :disabled="isSaving || !request.primaryAction.enabled"
            @click="handleAction(request.id, request.primaryAction)"
          >
            {{ request.primaryAction.label }}
          </button>
          <button
            v-if="request.secondaryAction?.label"
            class="vision-terminal-feed__link-button"
            type="button"
            :disabled="isSaving || !request.secondaryAction.enabled"
            @click="handleAction(request.id, request.secondaryAction)"
          >
            {{ request.secondaryAction.label }}
          </button>
        </div>
      </article>
      <p v-if="!outgoingItems.length" class="vision-terminal-feed__line vision-terminal-feed__line--soft">No outgoing requests.</p>
      <div v-if="outgoingPages > 1" class="vision-pagination">
        <div class="muted">{{ `Page ${outgoingPage} of ${outgoingPages}` }}</div>
        <div class="button-row">
          <button class="button button--secondary" type="button" :disabled="outgoingPage <= 1" @click="emit('previous-outgoing')">Previous</button>
          <button class="button button--secondary" type="button" :disabled="outgoingPage >= outgoingPages" @click="emit('next-outgoing')">Next</button>
        </div>
      </div>
    </section>
  </section>
</template>
