<script setup lang="ts">
import {onMounted} from "vue"
import DashboardFindQuests from "../components/dashboard/DashboardFindQuests.vue"
import DashboardMyQuests from "../components/dashboard/DashboardMyQuests.vue"
import DashboardOverview from "../components/dashboard/DashboardOverview.vue"
import DashboardPostWork from "../components/dashboard/DashboardPostWork.vue"
import DashboardApplicationsDialog from "../components/dashboard/DashboardApplicationsDialog.vue"
import DashboardOpenWorkDialog from "../components/dashboard/DashboardOpenWorkDialog.vue"
import DashboardQuestDialog from "../components/dashboard/DashboardQuestDialog.vue"
import DashboardApplicationDialog from "../components/dashboard/DashboardApplicationDialog.vue"
import DashboardProfileDialog from "../components/dashboard/DashboardProfileDialog.vue"
import UserProfileDialog from "../../../modules/social/components/profile/UserProfileDialog.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const dashboard = useQuestDashboard()

onMounted(dashboard.init)
</script>

<template>
  <div class="page page--dashboard">
    <div class="dashboard-shell">
      <main class="dashboard-main">
        <UiToast :message="dashboard.feedback" :tone="dashboard.feedbackType" />

        <UiRequestError :message="dashboard.questsError" :details="dashboard.questsErrorDetails" summary="Quest request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.questsErrorDetails)" />
        <UiRequestError :message="dashboard.applicationsError" :details="dashboard.applicationsErrorDetails" summary="Application request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.applicationsErrorDetails)" />
        <UiRequestError :message="dashboard.usersError" :details="dashboard.usersErrorDetails" summary="User request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.usersErrorDetails)" />

        <div v-if="dashboard.isLoadingQuests || dashboard.isLoadingApplications || dashboard.isLoadingUsers" class="empty-state">
          <div>Loading dashboard...</div>
        </div>

        <DashboardOverview v-if="dashboard.activeTab === 'overview'" :dashboard="dashboard" />

        <section v-else-if="dashboard.activeTab === 'create-job'" class="stack">
          <button class="dashboard-launch-card dashboard-launch-card--create" type="button" @click="dashboard.openCreateJobDialog()">
            <div class="dashboard-launch-card__copy">
              <div class="dashboard-kicker">Create job</div>
              <h2 class="card__title">Draft a new job</h2>
              <p class="muted mt-2 mb-0">Open the brief form to add title, budget, timing, and visibility.</p>
            </div>
            <div class="dashboard-launch-card__action">
              <span class="dashboard-launch-card__icon" aria-hidden="true">+</span>
              <span>Open form</span>
            </div>
          </button>
          <DashboardMyQuests :dashboard="dashboard" />
        </section>

        <section v-else-if="dashboard.activeTab === 'find-work'" class="stack">
          <DashboardFindQuests :dashboard="dashboard" />
        </section>

        <DashboardProfileDialog :dashboard="dashboard" />
        <UserProfileDialog
          :open="dashboard.userProfileDialogId !== null"
          :user-id="dashboard.userProfileDialogId"
          @close="dashboard.closeUserProfileDialog()"
          @edit-profile="dashboard.openProfileEditDialog()"
        />

        <UiDialog
          :open="dashboard.isCreateJobDialogOpen"
          title="Create job"
          size="xl"
          @close="dashboard.closeCreateJobDialog()"
        >
          <DashboardPostWork :dashboard="dashboard" :boxed="false" />
        </UiDialog>

        <UiDialog
          :open="dashboard.isFindWorkDialogOpen"
          title="Find work"
          subtitle="Filter open jobs and open the ones you want."
          size="xl"
          :default-expanded="true"
          @close="dashboard.closeFindWorkDialog()"
        >
          <DashboardFindQuests :dashboard="dashboard" :show-header="false" :boxed="false" />
        </UiDialog>

        <DashboardApplicationsDialog :dashboard="dashboard" />
        <DashboardOpenWorkDialog :dashboard="dashboard" />
        <DashboardQuestDialog :dashboard="dashboard" />
        <DashboardApplicationDialog :dashboard="dashboard" />
      </main>
    </div>
  </div>
</template>
