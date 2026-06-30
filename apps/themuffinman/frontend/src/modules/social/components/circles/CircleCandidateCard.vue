<script setup lang="ts">
import type {CircleCandidate, ProfilePrimaryAction} from "../../../../contracts/index.ts"
import ProfileEntityCard from "../../../../components/profile/ProfileEntityCard.vue"

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
</script>

<template>
  <ProfileEntityCard
    :username="props.user.username"
    :avatar-data-url="props.user.profileAvatarDataUrl"
    :meta="meta"
    :description="''"
    description-placeholder=""
    @open="emit('openProfile', user.id)"
  >
    <template #badge>
      <span :class="props.user.relationBadgeClass">
        {{ props.user.relationLabel }}
      </span>
    </template>
    <template #actions>
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
    </template>
  </ProfileEntityCard>
</template>
