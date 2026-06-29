<script setup lang="ts">
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import DashboardAdmin from "../components/dashboard/DashboardAdmin.vue"
import DashboardApplicationDialog from "../components/dashboard/DashboardApplicationDialog.vue"
import DashboardQuestDialog from "../components/dashboard/DashboardQuestDialog.vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import {useMountedAsync} from "../../../composables/useMountedAsync.ts"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const dashboard = useQuestDashboard()

useMountedAsync(dashboard.init)
</script>

<template>
  <UiAppShellPage admin>
        <AdminShellHeader
          title="Quests"
          subtitle=""
        />

        <UiToast :message="dashboard.feedback" :tone="dashboard.feedbackType" />

        <UiRequestError :message="dashboard.questsError" :details="dashboard.questsErrorDetails" summary="Quest request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.questsErrorDetails)" />
        <UiRequestError :message="dashboard.applicationsError" :details="dashboard.applicationsErrorDetails" summary="Application request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.applicationsErrorDetails)" />

        <div v-if="dashboard.isLoadingQuests || dashboard.isLoadingApplications" class="empty-state">
          <div>Loading dashboard...</div>
        </div>

        <DashboardAdmin :dashboard="dashboard" />

        <DashboardQuestDialog :dashboard="dashboard" />
        <DashboardApplicationDialog :dashboard="dashboard" />
  </UiAppShellPage>
</template>
