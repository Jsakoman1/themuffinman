<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from "vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"

const props = defineProps<{
  email?: string | null
  username?: string | null
  avatarDataUrl?: string | null
}>()

const emit = defineEmits<{
  (event: "update:email", value: string): void
  (event: "update:username", value: string): void
  (event: "pick-image", file: File | null): void
  (event: "clear-image"): void
}>()

const profileImageInputRef = ref<HTMLInputElement | null>(null)
const profileImageMenuRef = ref<HTMLElement | null>(null)
const editingEmail = ref(false)
const editingUsername = ref(false)
const imageActionsOpen = ref(false)

const openProfileImagePicker = () => {
  imageActionsOpen.value = false
  profileImageInputRef.value?.click()
}

const toggleImageActions = () => {
  imageActionsOpen.value = !imageActionsOpen.value
}

const clearProfileImage = () => {
  imageActionsOpen.value = false
  emit("clear-image")
}

const handleDocumentClick = (event: MouseEvent) => {
  if (!imageActionsOpen.value) {
    return
  }

  const menuElement = profileImageMenuRef.value
  const target = event.target
  if (!(menuElement instanceof HTMLElement) || !(target instanceof Node)) {
    imageActionsOpen.value = false
    return
  }

  if (!menuElement.contains(target)) {
    imageActionsOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener("mousedown", handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", handleDocumentClick)
})
</script>

<template>
  <div ref="profileImageMenuRef" class="dashboard-profile-edit-side">
    <div class="dashboard-profile-avatar-editor">
      <ProfileAvatar :username="username" :avatar-data-url="avatarDataUrl" :size="96" />
      <button
        class="button button--icon button--secondary button--icon-compact dashboard-profile-avatar-editor__trigger"
        type="button"
        aria-label="Profile image actions"
        title="Profile image actions"
        @click="toggleImageActions"
      >
        ✎
      </button>
      <div v-if="imageActionsOpen" class="dashboard-profile-avatar-editor__menu">
        <button class="button button--secondary dashboard-profile-avatar-editor__menu-action" type="button" @click="openProfileImagePicker">
          {{ avatarDataUrl ? "Change picture" : "Choose picture" }}
        </button>
        <button
          v-if="avatarDataUrl"
          class="button button--secondary dashboard-profile-avatar-editor__menu-action"
          type="button"
          @click="clearProfileImage"
        >
          Remove picture
        </button>
      </div>
    </div>

    <UiFieldGroup label="Email" field-class="ui-edit-field ui-edit-field--profile-email ui-edit-field--profile-inline">
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

    <UiFieldGroup label="Username" field-class="ui-edit-field ui-edit-field--profile-username ui-edit-field--profile-inline">
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
