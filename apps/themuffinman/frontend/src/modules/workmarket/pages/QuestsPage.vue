<script setup lang="ts">
import {defineAsyncComponent, onMounted} from "vue"
import UiLaunchCard from "../../../components/ui/UiLaunchCard.vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const DashboardFindQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardFindQuests.vue"))
const DashboardMyQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardMyQuests.vue"))
const DashboardOverview = defineAsyncComponent(() => import("../components/dashboard/DashboardOverview.vue"))
const DashboardPostWork = defineAsyncComponent(() => import("../components/dashboard/DashboardPostWork.vue"))
const DashboardApplicationsDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationsDialog.vue"))
const DashboardOpenWorkDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardOpenWorkDialog.vue"))
const DashboardQuestDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardQuestDialog.vue"))
const DashboardApplicationDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationDialog.vue"))
const DashboardProfileDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardProfileDialog.vue"))
const UserProfileDialog = defineAsyncComponent(() => import("../../../modules/social/components/profile/UserProfileDialog.vue"))

const dashboard = useQuestDashboard()

onMounted(dashboard.init)
</script>

<template>
  <UiDashboardPage>
        <UiToast :message="dashboard.feedback" :tone="dashboard.feedbackType" />

        <UiRequestError :message="dashboard.questsError" :details="dashboard.questsErrorDetails" summary="Quest request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.questsErrorDetails)" />
        <UiRequestError :message="dashboard.applicationsError" :details="dashboard.applicationsErrorDetails" summary="Application request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.applicationsErrorDetails)" />
        <UiRequestError :message="dashboard.usersError" :details="dashboard.usersErrorDetails" summary="User request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.usersErrorDetails)" />

        <div v-if="dashboard.isLoadingQuests || dashboard.isLoadingApplications || dashboard.isLoadingUsers" class="empty-state">
          <div>Loading dashboard...</div>
        </div>

        <DashboardOverview v-if="dashboard.activeTab === 'overview'" :dashboard="dashboard" />

        <section v-else-if="dashboard.activeTab === 'create-job'" class="surface-stack">
          <UiLaunchCard
            eyebrow="Create job"
            title="Draft a new job"
            description="Open the brief form to add title, budget, timing, and visibility."
            action-label="Open form"
            @click="dashboard.openCreateJobDialog()"
          />
          <DashboardMyQuests :dashboard="dashboard" />
        </section>

        <section v-else-if="dashboard.activeTab === 'find-work'" class="surface-stack">
          <DashboardFindQuests :dashboard="dashboard" />
        </section>

        <DashboardProfileDialog v-if="dashboard.isProfileEditDialogOpen" :dashboard="dashboard" />
        <UserProfileDialog
          v-if="dashboard.userProfileDialogId !== null"
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
          <DashboardPostWork v-if="dashboard.isCreateJobDialogOpen" :dashboard="dashboard" :boxed="false" />
        </UiDialog>

        <UiDialog
          :open="dashboard.isFindWorkDialogOpen"
          title="Find work"
          size="xl"
          :default-expanded="true"
          @close="dashboard.closeFindWorkDialog()"
        >
          <DashboardFindQuests v-if="dashboard.isFindWorkDialogOpen" :dashboard="dashboard" :show-header="false" :boxed="false" />
        </UiDialog>

        <DashboardApplicationsDialog v-if="dashboard.isApplicationsDialogOpen" :dashboard="dashboard" />
        <DashboardOpenWorkDialog v-if="dashboard.isOpenWorkDialogOpen" :dashboard="dashboard" />
        <DashboardQuestDialog v-if="dashboard.questDialogId !== null" :dashboard="dashboard" />
        <DashboardApplicationDialog v-if="dashboard.applicationDialogId !== null" :dashboard="dashboard" />
  </UiDashboardPage>
</template>
