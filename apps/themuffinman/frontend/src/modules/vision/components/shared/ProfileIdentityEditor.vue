<script setup lang="ts">
import {ref} from "vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"

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
</script>

<template>
  <div class="dashboard-profile-edit-side">
    <div v-if="showAvatar !== false" class="dashboard-profile-avatar-editor">
      <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="96" />
      <button
        class="button button--icon button--secondary button--icon-compact dashboard-profile-avatar-editor__trigger"
        type="button"
        aria-label="Change profile picture"
        title="Change profile picture"
        @click="openProfileImagePicker"
      >
        ✎
      </button>
      <button
        v-if="avatarDataUrl"
        class="button button--icon button--secondary button--icon-compact dashboard-profile-avatar-editor__remove"
        type="button"
        aria-label="Remove profile picture"
        title="Remove profile picture"
        @click="clearProfileImage"
      >
        ×
      </button>
    </div>

    <UiFieldGroup v-if="showEmail !== false" label="Email" field-class="ui-edit-field ui-edit-field--profile-email ui-edit-field--profile-inline">
      <template #headerAction>
        <button class="button button--icon button--secondary button--icon-compact" type="button" aria-label="Edit email" @click="editingEmail = !editingEmail">✎</button>
      </template>

      <input
        v-if="editingEmail"
        :value="email ?? ''"
        class="input"
        type="email"
        @input="emit('update:email', ($event.target as HTMLInputElement).value)"
      />
      <div v-else class="ui-inline-readonly-text">{{ email || "-" }}</div>
    </UiFieldGroup>

    <UiFieldGroup v-if="showUsername !== false" label="Username" field-class="ui-edit-field ui-edit-field--profile-username ui-edit-field--profile-inline">
      <template #headerAction>
        <button class="button button--icon button--secondary button--icon-compact" type="button" aria-label="Edit username" @click="editingUsername = !editingUsername">✎</button>
      </template>

      <input
        v-if="editingUsername"
        :value="username ?? ''"
        class="input"
        @input="emit('update:username', ($event.target as HTMLInputElement).value)"
      />
      <div v-else class="ui-inline-readonly-text">{{ username || "-" }}</div>
    </UiFieldGroup>

    <input
      ref="profileImageInputRef"
      class="visually-hidden"
      type="file"
      accept="image/*"
      @change="emit('pick-image', ($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
    />
  </div>
</template>
