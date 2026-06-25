<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import DashboardMyApplications from "./DashboardMyApplications.vue"
import type {DashboardApplicationsDialogFacade} from "../../composables/dashboard/dashboardFacades.ts"

defineProps<{
  dashboard: DashboardApplicationsDialogFacade
}>()
</script>

<template>
  <UiDialog
    :open="dashboard.isApplicationsDialogOpen"
    title="Pending applications"
    subtitle=""
    size="lg"
    @close="dashboard.closeApplicationsDialog()"
  >
    <DashboardMyApplications
      :dashboard="dashboard"
      title="Pending applications"
      subtitle=""
      empty-message="No pending applications right now."
      :applications="dashboard.pendingWorkApplications"
      :show-header="false"
      :boxed="false"
    />
  </UiDialog>
</template>
