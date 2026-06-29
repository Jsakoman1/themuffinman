<script setup lang="ts">
import {defineAsyncComponent, onMounted} from "vue"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const DashboardFindQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardFindQuests.vue"))
const DashboardMyApplications = defineAsyncComponent(() => import("../components/dashboard/DashboardMyApplications.vue"))
const DashboardMyQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardMyQuests.vue"))
const DashboardWorkPlanner = defineAsyncComponent(() => import("../components/dashboard/DashboardWorkPlanner.vue"))
const DashboardWorkPlannerFlexible = defineAsyncComponent(() => import("../components/dashboard/DashboardWorkPlannerFlexible.vue"))
const DashboardPostWork = defineAsyncComponent(() => import("../components/dashboard/DashboardPostWork.vue"))
const DashboardApplicationsDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationsDialog.vue"))
const DashboardOpenWorkDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardOpenWorkDialog.vue"))
const DashboardQuestDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardQuestDialog.vue"))
const DashboardApplicationDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationDialog.vue"))
const UserProfileDialog = defineAsyncComponent(() => import("../../../modules/social/components/profile/UserProfileDialog.vue"))

const dashboard = useQuestDashboard()

onMounted(dashboard.init)
</script>

<template>
  <UiAppShellPage>
        <UiToast :message="dashboard.feedback" :tone="dashboard.feedbackType" />

        <UiRequestError :message="dashboard.questsError" :details="dashboard.questsErrorDetails" summary="Quest request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.questsErrorDetails)" />
        <UiRequestError :message="dashboard.applicationsError" :details="dashboard.applicationsErrorDetails" summary="Application request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.applicationsErrorDetails)" />
        <UiRequestError :message="dashboard.usersError" :details="dashboard.usersErrorDetails" summary="User request debug details" :copied="dashboard.copiedDebug" @copy="dashboard.copyDebugInfo(dashboard.usersErrorDetails)" />

        <div v-if="dashboard.isLoadingQuests || dashboard.isLoadingApplications || dashboard.isLoadingUsers" class="empty-state">
          <div>Loading dashboard...</div>
        </div>

        <section v-if="dashboard.activeTab === 'calendar'" class="surface-stack">
          <UiWorkspace variant="detail" class="dashboard-calendar-workspace">
            <div class="surface-stack">
              <DashboardWorkPlanner :dashboard="dashboard" :show-header="false" :show-flexible="false" />
            </div>

            <aside class="surface-stack dashboard-calendar-sidebar">
              <DashboardWorkPlannerFlexible :dashboard="dashboard" />
            </aside>
          </UiWorkspace>
        </section>

        <section v-else-if="dashboard.activeTab === 'side-job'" class="surface-stack dashboard-sidejob-screen">
          <div class="dashboard-sidejob-layout">
            <UiSurfaceSection class="dashboard-sidejob-panel" title="My jobs">
              <div class="dashboard-sidejob-panel__cta">
                <button class="button dashboard-sidejob-panel__cta-button" type="button" @click="dashboard.openCreateJobDialog()">
                  Create new job
                </button>
              </div>

              <DashboardMyQuests
                :dashboard="dashboard"
                title="My jobs"
                subtitle=""
                empty-message="No jobs posted yet."
                :show-header="false"
                :boxed="false"
              />
            </UiSurfaceSection>

            <UiSurfaceSection class="dashboard-sidejob-panel" title="My applications">
              <div class="dashboard-sidejob-panel__cta">
                <button class="button dashboard-sidejob-panel__cta-button" type="button" @click="dashboard.openFindWorkDialog()">
                  Find job
                </button>
              </div>

              <DashboardMyApplications
                :dashboard="dashboard"
                title="My applications"
                subtitle=""
                empty-message="No applications yet."
                :show-header="false"
                :boxed="false"
              />
            </UiSurfaceSection>
          </div>
        </section>

        <UserProfileDialog
          v-if="dashboard.userProfileDialogId !== null"
          :open="dashboard.userProfileDialogId !== null"
          :user-id="dashboard.userProfileDialogId"
          @close="dashboard.closeUserProfileDialog()"
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
          title="Find job"
          size="xl"
          @close="dashboard.closeFindWorkDialog()"
        >
          <DashboardFindQuests v-if="dashboard.isFindWorkDialogOpen" :dashboard="dashboard" :show-header="false" :boxed="false" />
        </UiDialog>

        <DashboardApplicationsDialog v-if="dashboard.isApplicationsDialogOpen" :dashboard="dashboard" />
        <DashboardOpenWorkDialog v-if="dashboard.isOpenWorkDialogOpen" :dashboard="dashboard" />
        <DashboardQuestDialog v-if="dashboard.questDialogId !== null" :dashboard="dashboard" />
        <DashboardApplicationDialog v-if="dashboard.applicationDialogId !== null" :dashboard="dashboard" />
  </UiAppShellPage>
</template>
