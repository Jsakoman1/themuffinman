<script setup lang="ts">
import {ref} from "vue"
import RichTextEditor from "../../../../components/editor/AsyncRichTextEditor.vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiReadonlyField from "../../../../components/ui/UiReadonlyField.vue"

const props = defineProps<{
  email?: string | null
  username?: string | null
  description: string
  avatarDataUrl?: string | null
}>()

const emit = defineEmits<{
  (event: "update:username", value: string): void
  (event: "update:description", value: string): void
  (event: "pick-image", file: File | null): void
  (event: "clear-image"): void
}>()

const profileImageInputRef = ref<HTMLInputElement | null>(null)

const openProfileImagePicker = () => {
  profileImageInputRef.value?.click()
}
</script>

<template>
  <div class="profile-editor surface-section surface-section--soft">
    <div class="dashboard-profile-avatar-editor">
      <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="96" />
      <button
        v-if="avatarDataUrl"
        class="dashboard-profile-avatar-editor__remove"
        type="button"
        aria-label="Remove profile image"
        title="Remove profile image"
        @click="emit('clear-image')"
      >
        x
      </button>
      <button class="button button--secondary dashboard-profile-avatar-editor__action" type="button" @click="openProfileImagePicker">
        {{ avatarDataUrl ? "Change" : "Choose image" }}
      </button>
    </div>

    <div class="profile-editor__content">
      <UiReadonlyField class="ui-edit-field ui-edit-field--profile-email" label="Email" :value="email" />

      <UiFieldGroup label="Username" field-class="ui-edit-field ui-edit-field--profile-username">
        <input :value="username ?? ''" class="input" @input="emit('update:username', ($event.target as HTMLInputElement).value)" />
      </UiFieldGroup>

      <input
        ref="profileImageInputRef"
        class="visually-hidden"
        type="file"
        accept="image/*"
        @change="emit('pick-image', ($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
      />

      <UiFieldGroup label="Profile description" tag="div" field-class="ui-edit-field ui-edit-field--profile-description">
        <RichTextEditor
          :model-value="description"
          placeholder=""
          toolbar-label="Profile tools"
          @update:model-value="emit('update:description', $event)"
        />
      </UiFieldGroup>
    </div>
  </div>
</template>
