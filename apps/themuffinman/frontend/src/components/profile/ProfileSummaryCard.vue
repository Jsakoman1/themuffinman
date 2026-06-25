<script setup lang="ts">
import ProfileAvatar from "./ProfileAvatar.vue"
import ProfileBio from "./ProfileBio.vue"
import UiSurfaceSection from "../ui/UiSurfaceSection.vue"

withDefaults(defineProps<{
  username: string
  avatarDataUrl?: string | null
  role?: string
  email?: string
  badgeLabel?: string
  description?: string | null
  descriptionPlaceholder?: string
  size?: number
  soft?: boolean
}>(), {
  avatarDataUrl: null,
  role: "",
  email: "",
  badgeLabel: "",
  description: "",
  descriptionPlaceholder: "No profile description yet.",
  size: 88,
  soft: true,
})
</script>

<template>
  <UiSurfaceSection class="ui-profile-summary" :soft="soft">
    <div class="ui-profile-summary__header">
      <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="size" />

      <div class="ui-profile-summary__identity">
        <div class="ui-profile-summary__name-row">
          <h3 class="ui-profile-summary__name">{{ username }}</h3>
          <span v-if="badgeLabel" class="badge badge--secondary">{{ badgeLabel }}</span>
        </div>
        <div v-if="role" class="ui-profile-summary__meta">{{ role }}</div>
        <div v-if="email" class="ui-profile-summary__meta">{{ email }}</div>
      </div>
    </div>

    <ProfileBio :text="description" :placeholder="descriptionPlaceholder" />

    <div v-if="$slots.actions" class="ui-profile-summary__actions">
      <slot name="actions" />
    </div>
  </UiSurfaceSection>
</template>
