<script setup lang="ts">
import UiDialog from "../ui/UiDialog.vue"
import DashboardMyApplications from "./DashboardMyApplications.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"

defineProps<{
  dashboard: QuestDashboard
}>()
</script>

<template>
  <UiDialog
    :open="dashboard.isApplicationsDialogOpen"
    title="Pending applications"
    subtitle="Applications that are still waiting for a response."
    size="lg"
    @close="dashboard.closeApplicationsDialog()"
  >
    <DashboardMyApplications
      :dashboard="dashboard"
      title="Pending applications"
      subtitle="Review the applications that still need action."
      empty-message="No pending applications right now."
      :applications="dashboard.pendingWorkApplications"
      :show-header="false"
      :boxed="false"
    />
  </UiDialog>
</template>
