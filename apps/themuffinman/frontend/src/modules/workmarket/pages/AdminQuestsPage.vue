<script setup lang="ts">
import {onMounted} from "vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import DashboardAdmin from "../components/dashboard/DashboardAdmin.vue"
import DashboardApplicationDialog from "../components/dashboard/DashboardApplicationDialog.vue"
import DashboardQuestDialog from "../components/dashboard/DashboardQuestDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const dashboard = useQuestDashboard()

onMounted(() => {
  void dashboard.init()
})
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main dashboard-main--admin">
        <AdminShellHeader
          title="Quests"
          subtitle="Review, edit, reopen, approve, and manage every quest from a single admin workspace."
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
      </main>
    </div>
  </div>
</template>
