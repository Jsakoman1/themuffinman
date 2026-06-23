<script setup lang="ts">
import DashboardSectionHeader from "./DashboardSectionHeader.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import ProfileAvatar from "../profile/ProfileAvatar.vue"
import ProfileBio from "../profile/ProfileBio.vue"

defineProps<{
  dashboard: QuestDashboard
}>()
</script>

<template>
  <section class="card">
    <DashboardSectionHeader title="Profile" subtitle="Your details." />

    <div class="button-row button-row--end mt-2">
      <button class="button button--icon button--secondary" type="button" aria-label="Edit profile" @click="dashboard.openProfileEditDialog()">✎</button>
    </div>

    <div class="profile-card">
      <ProfileAvatar
        :username="dashboard.currentUser?.username"
        :avatar-data-url="dashboard.currentUser?.profileAvatarDataUrl"
        :size="88"
      />

      <div class="stack">
        <div class="field">
          <span class="label">Username</span>
          <strong>{{ dashboard.currentUser?.username }}</strong>
        </div>
        <div class="field">
          <span class="label">Email</span>
          <strong>{{ dashboard.currentUser?.email }}</strong>
        </div>
      </div>
    </div>

    <div class="field mt-4">
      <span class="label">Profile description</span>
      <ProfileBio :text="dashboard.currentUser?.profileDescription" />
    </div>
  </section>
</template>
