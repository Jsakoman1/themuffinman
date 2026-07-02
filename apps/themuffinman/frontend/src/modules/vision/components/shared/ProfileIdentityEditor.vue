<script setup lang="ts">
import {ref} from "vue"
import {getProfileInitials} from "../../../../shared/profileFormatting.ts"

const props = defineProps<{
  email?: string | null
  username?: string | null
  avatarDataUrl?: string | null
  showAvatar?: boolean
  showEmail?: boolean
  showUsername?: boolean
}>()

const emit = defineEmits<{
  (event: "update:email", value: string): void
  (event: "update:username", value: string): void
  (event: "pick-image", file: File | null): void
  (event: "clear-image"): void
}>()

const profileImageInputRef = ref<HTMLInputElement | null>(null)
const editingEmail = ref(false)
const editingUsername = ref(false)

const openProfileImagePicker = () => {
  profileImageInputRef.value?.click()
}

const clearProfileImage = () => {
  emit("clear-image")
}

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})
</script>

<template>
  <div class="vision-profile-edit-side">
    <div v-if="showAvatar !== false" class="vision-profile-avatar-editor">
      <span class="profile-avatar" :style="profileAvatarStyle(96)">
        <img v-if="avatarDataUrl" class="profile-avatar__image" :src="avatarDataUrl" :alt="`${username || 'User'} avatar`" />
        <span v-else class="profile-avatar__fallback">{{ getProfileInitials(username) }}</span>
      </span>
      <button
        class="button button--icon button--secondary button--icon-compact vision-profile-avatar-editor__trigger"
        type="button"
        aria-label="Change profile picture"
        title="Change profile picture"
        @click="openProfileImagePicker"
      >
        ✎
      </button>
      <button
        v-if="avatarDataUrl"
        class="button button--icon button--secondary button--icon-compact vision-profile-avatar-editor__remove"
        type="button"
        aria-label="Remove profile picture"
        title="Remove profile picture"
        @click="clearProfileImage"
      >
        ×
      </button>
    </div>

    <div v-if="showEmail !== false" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">email</p>
      <button class="vision-terminal-feed__link-button" type="button" @click="editingEmail = !editingEmail">
        {{ editingEmail ? "Close" : "Edit email" }}
      </button>
      <input
        v-if="editingEmail"
        :value="email ?? ''"
        class="input"
        type="email"
        @input="emit('update:email', ($event.target as HTMLInputElement).value)"
      />
      <div v-else class="ui-inline-readonly-text">{{ email || "-" }}</div>
    </div>

    <div v-if="showUsername !== false" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">username</p>
      <button class="vision-terminal-feed__link-button" type="button" @click="editingUsername = !editingUsername">
        {{ editingUsername ? "Close" : "Edit username" }}
      </button>
      <input
        v-if="editingUsername"
        :value="username ?? ''"
        class="input"
        @input="emit('update:username', ($event.target as HTMLInputElement).value)"
      />
      <div v-else class="ui-inline-readonly-text">{{ username || "-" }}</div>
    </div>

    <input
      ref="profileImageInputRef"
      class="visually-hidden"
      type="file"
      accept="image/*"
      @change="emit('pick-image', ($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
    />
  </div>
</template>
