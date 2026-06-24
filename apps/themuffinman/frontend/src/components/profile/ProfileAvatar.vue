<script setup lang="ts">
import {computed} from "vue"
import {getProfileInitials} from "../../shared/profileFormatting.ts"

const props = withDefaults(defineProps<{
  username?: string | null
  avatarDataUrl?: string | null
  size?: number
}>(), {
  username: "",
  avatarDataUrl: "",
  size: 64,
})

const initials = computed(() => getProfileInitials(props.username))
const avatarSizeStyle = computed(() => ({
  "--profile-avatar-size": `${props.size}px`
}))
</script>

<template>
  <span class="profile-avatar" :style="avatarSizeStyle">
    <img v-if="avatarDataUrl" class="profile-avatar__image" :src="avatarDataUrl" :alt="`${username || 'User'} avatar`" />
    <span v-else class="profile-avatar__fallback">{{ initials }}</span>
  </span>
</template>
