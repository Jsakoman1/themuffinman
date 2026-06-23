<script setup lang="ts">
import {onMounted} from "vue"
import {useRouter} from "vue-router"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import DashboardAdmin from "../components/dashboard/DashboardAdmin.vue"
import DashboardApplicationDialog from "../components/dashboard/DashboardApplicationDialog.vue"
import DashboardQuestDialog from "../components/dashboard/DashboardQuestDialog.vue"
import UiRequestError from "../components/ui/UiRequestError.vue"
import UiToast from "../components/ui/UiToast.vue"
import {logoutUser} from "../auth.ts"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const dashboard = useQuestDashboard()
const router = useRouter()

const handleLogout = () => {
  logoutUser()
  router.push("/login")
}

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
          :on-logout="handleLogout"
        />

        <UiToast :message="dashboard.feedback" :tone="dashboard.feedbackType" />

        <UiRequestError :message="dashboard.questsError" :details="dashboard.questsErrorDetails" summary="Quest request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.questsErrorDetails)" />
        <UiRequestError :message="dashboard.applicationsError" :details="dashboard.applicationsErrorDetails" summary="Application request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.applicationsErrorDetails)" />

        <div v-if="dashboard.isLoadingQuests || dashboard.isLoadingApplications" class="empty-state">
          <div>Loading dashboard...</div>
          <div class="debug-inline mt-2">GET /dashboard/me</div>
        </div>

        <DashboardAdmin :dashboard="dashboard" />

        <DashboardQuestDialog :dashboard="dashboard" />
        <DashboardApplicationDialog :dashboard="dashboard" />
      </main>
    </div>
  </div>
</template>
