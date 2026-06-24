<script setup lang="ts">
import type {CircleCandidate, ProfilePrimaryAction} from "../../../workmarket/api/workmarketApi.ts"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"

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
</script>

<template>
  <article class="profile-open-quest circles-person-card">
    <div class="profile-open-quest__top">
      <button class="profile-link profile-link--button" type="button" @click="emit('openProfile', user.id)">
        <ProfileAvatar :username="props.user.username" :avatar-data-url="props.user.profileAvatarDataUrl" :size="56" />
        <div class="stack circles-person-card__identity">
          <strong>{{ props.user.username }}</strong>
          <div class="muted">{{ props.user.email }}</div>
        </div>
      </button>
      <span :class="props.user.relationBadgeClass">
        {{ props.user.relationLabel }}
      </span>
    </div>

    <ProfileBio :text="props.user.profileDescription" placeholder="No profile description." />
    <div class="button-row circles-person-card__actions">
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
