<script setup lang="ts">
import {getProfileInitials} from "../../../../shared/profileFormatting.ts"
import type {CircleCandidate, ProfilePrimaryAction} from "../../../../contracts/index.ts"

const props = defineProps<{
  user: CircleCandidate
  saving: boolean
}>()

const emit = defineEmits<{
  openProfile: [id: number]
  invite: [id: number]
  block: [id: number]
  unblock: [id: number]
}>()

const handleAction = (action: ProfilePrimaryAction | null | undefined) => {
  switch (action?.type) {
    case "SEND_INVITE":
      emit("invite", props.user.id)
      return
    case "BLOCK":
      emit("block", props.user.id)
      return
    case "UNBLOCK":
      emit("unblock", props.user.id)
      return
    default:
      return
  }
}

const meta = [
  props.user.distanceLabel,
  props.user.locationLabel,
  props.user.email
].filter((value, index, values) => Boolean(value) && values.indexOf(value) === index).join(" • ")

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})
</script>

<template>
  <article class="profile-entity-card">
    <div class="profile-entity-card__top">
      <button class="profile-link profile-link--button" type="button" @click="emit('openProfile', user.id)">
        <span class="profile-avatar" :style="profileAvatarStyle(56)">
          <img v-if="props.user.profileAvatarDataUrl" class="profile-avatar__image" :src="props.user.profileAvatarDataUrl" :alt="`${props.user.username || 'User'} avatar`" />
          <span v-else class="profile-avatar__fallback">{{ getProfileInitials(props.user.username) }}</span>
        </span>
        <div class="stack profile-entity-card__identity">
          <strong>{{ props.user.username }}</strong>
          <div v-if="meta" class="muted">{{ meta }}</div>
        </div>
      </button>

      <div class="profile-entity-card__badge">
        <span :class="props.user.relationBadgeClass">
          {{ props.user.relationLabel }}
        </span>
      </div>
    </div>

    <div class="profile-entity-card__body"></div>

    <div class="profile-entity-card__actions">
      <button
        v-if="props.user.primaryAction?.label"
        class="button"
        type="button"
        :disabled="props.saving || !props.user.primaryAction?.enabled"
        @click="handleAction(props.user.primaryAction)"
      >
        {{ props.user.primaryAction?.label }}
      </button>
      <button
        v-if="props.user.secondaryAction?.label"
        class="button button--secondary"
        type="button"
        :disabled="props.saving || !props.user.secondaryAction?.enabled"
        @click="handleAction(props.user.secondaryAction)"
      >
        {{ props.user.secondaryAction?.label }}
      </button>
    </div>
  </article>
</template>
