<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import DashboardMyApplications from "./DashboardMyApplications.vue"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import type {DashboardApplicationsDialogFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardApplicationsDialogFacade
}>()
</script>

<template>
  <UiDialog
    :open="dashboard.isApplicationsDialogOpen"
    title="Pending applications"
    subtitle=""
    size="xl"
    @close="dashboard.closeApplicationsDialog()"
  >
    <DetailDialogFrame>
      <template #main>
        <DashboardMyApplications
          :dashboard="dashboard"
          title="Pending applications"
          subtitle=""
          empty-message="No pending applications right now."
          :applications="dashboard.pendingWorkApplications"
          :show-header="false"
          :boxed="false"
        />
      </template>

      <template #side>
        <DetailUtilitySection title="Summary" tone="summary">
          <div class="quest-overview-aside quest-overview-aside--compact">
            <div class="quest-overview-aside__row">
              <span class="quest-overview-aside__label">Pending</span>
              <span class="quest-overview-aside__value">{{ props.dashboard.pendingWorkApplications.length }}</span>
            </div>
          </div>
        </DetailUtilitySection>
      </template>
    </DetailDialogFrame>
  </UiDialog>
</template>
