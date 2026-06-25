<script setup lang="ts">
import type {DashboardProfileSummaryFacade} from "../../composables/dashboard/dashboardFacades.ts"
import ProfileSummaryCard from "../../../../components/profile/ProfileSummaryCard.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"

defineProps<{
  dashboard: DashboardProfileSummaryFacade
}>()
</script>

<template>
  <UiSurfaceSection class="card" soft title="Profile">
    <ProfileSummaryCard
      :username="dashboard.currentUser?.username ?? ''"
      :avatar-data-url="dashboard.currentUser?.profileAvatarDataUrl"
      :email="dashboard.currentUser?.email ?? ''"
      :description="dashboard.currentUser?.profileDescription ?? ''"
      description-placeholder="Add a profile description."
      :size="88"
      :soft="false"
    >
      <template #actions>
        <UiFormActions align="end">
          <button class="button button--icon button--secondary" type="button" aria-label="Edit profile" @click="dashboard.openProfileEditDialog()">✎</button>
        </UiFormActions>
      </template>
    </ProfileSummaryCard>
  </UiSurfaceSection>
</template>
