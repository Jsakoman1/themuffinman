<script setup lang="ts">
import type {DashboardProfileEditFacade} from "../../composables/dashboard/dashboardFacades.ts"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"
import ProfileEditFields from "../shared/ProfileEditFields.vue"

const props = defineProps<{
  dashboard: DashboardProfileEditFacade
}>()
</script>

<template>
  <UiDialog
    :open="dashboard.isProfileEditDialogOpen"
    title="Edit profile"
    size="xl"
    @close="dashboard.closeProfileEditDialog"
  >
    <form class="form-stack" @submit.prevent="props.dashboard.saveProfile">
      <DashboardEditSheet minimal>
        <ProfileEditFields
          :email="props.dashboard.currentUser?.email"
          :username="props.dashboard.profileUsername"
          :description="props.dashboard.profileDescription"
          :avatar-data-url="props.dashboard.profileAvatarDataUrl"
          @update:username="props.dashboard.profileUsername = $event"
          @update:description="props.dashboard.profileDescription = $event"
          @pick-image="props.dashboard.updateProfileAvatarFromFile($event)"
          @clear-image="props.dashboard.clearProfileAvatar"
        />

        <template #actions>
          <button class="button button--action" type="submit">Save changes</button>
        </template>
      </DashboardEditSheet>
    </form>
  </UiDialog>
</template>
