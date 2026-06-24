<script setup lang="ts">
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import RichTextEditor from "../../../../components/editor/RichTextEditor.vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"

defineProps<{
  dashboard: QuestDashboard
}>()
</script>

<template>
  <UiDialog
    :open="dashboard.isProfileEditDialogOpen"
    title="Edit profile"
    subtitle="Update your public details and profile text."
    size="xl"
    @close="dashboard.closeProfileEditDialog"
  >
    <form @submit.prevent="dashboard.saveProfile">
      <DashboardEditSheet minimal>
        <div class="profile-editor">
          <ProfileAvatar :username="dashboard.currentUser?.username" :avatar-data-url="dashboard.profileAvatarDataUrl" :size="96" />

          <div class="profile-editor__content">
            <div class="field dashboard-edit-field dashboard-edit-field--profile-email">
              <span class="label">Email</span>
              <strong>{{ dashboard.currentUser?.email }}</strong>
            </div>

            <label class="field dashboard-edit-field dashboard-edit-field--profile-username">
              <span class="label">Username</span>
              <input v-model="dashboard.profileUsername" class="input" />
            </label>

            <label class="field dashboard-edit-field">
              <span class="label">Profile image</span>
              <input
                class="input"
                type="file"
                accept="image/*"
                @change="dashboard.updateProfileAvatarFromFile(($event.target as HTMLInputElement).files?.[0] ?? null)"
              />
              <div class="button-row mt-2">
                <button class="button button--secondary" type="button" @click="dashboard.clearProfileAvatar">Remove image</button>
              </div>
              <p class="muted mt-2 mb-0">Images are automatically resized before saving.</p>
            </label>

            <label class="field dashboard-edit-field dashboard-edit-field--profile-description">
              <span class="label">Profile description</span>
              <RichTextEditor v-model="dashboard.profileDescription" placeholder="" toolbar-label="Profile tools" />
            </label>
          </div>
        </div>

        <template #actions>
          <button class="button button--action" type="submit">Save changes</button>
        </template>
      </DashboardEditSheet>
    </form>
  </UiDialog>
</template>
