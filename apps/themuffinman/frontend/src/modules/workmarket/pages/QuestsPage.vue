<script setup lang="ts">
import {computed, defineAsyncComponent, onMounted} from "vue"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import UiToast from "../../../components/ui/UiToast.vue"
import UiWorkspace from "../../../components/ui/UiWorkspace.vue"
import DetailUtilitySection from "../components/shared/DetailUtilitySection.vue"
import {useQuestDashboard} from "../composables/useQuestDashboard.ts"

const DashboardFindQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardFindQuests.vue"))
const DashboardMyApplications = defineAsyncComponent(() => import("../components/dashboard/DashboardMyApplications.vue"))
const DashboardMyQuests = defineAsyncComponent(() => import("../components/dashboard/DashboardMyQuests.vue"))
const DashboardWorkPlanner = defineAsyncComponent(() => import("../components/dashboard/DashboardWorkPlanner.vue"))
const DashboardPostWork = defineAsyncComponent(() => import("../components/dashboard/DashboardPostWork.vue"))
const DashboardApplicationsDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationsDialog.vue"))
const DashboardOpenWorkDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardOpenWorkDialog.vue"))
const DashboardQuestDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardQuestDialog.vue"))
const DashboardApplicationDialog = defineAsyncComponent(() => import("../components/dashboard/DashboardApplicationDialog.vue"))
const UserProfileDialog = defineAsyncComponent(() => import("../../../modules/social/components/profile/UserProfileDialog.vue"))

const dashboard = useQuestDashboard()
const plannerScheduledCount = computed(() => dashboard.dashboardSections?.planner?.scheduledItems.length ?? 0)
const plannerFlexibleCount = computed(() => dashboard.dashboardSections?.planner?.flexibleItems.length ?? 0)

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

        <section v-if="dashboard.activeTab === 'calendar'" class="surface-stack">
          <UiWorkspace variant="detail">
            <div class="surface-stack">
              <UiSurfaceSection eyebrow="Calendar" title="Planned work" subtitle="See upcoming jobs, review past days, and jump into the right quest flow.">
                <template #actions>
                  <button class="button button--action" type="button" @click="dashboard.openCreateJobDialog()">
                    Create job
                  </button>
                </template>
              </UiSurfaceSection>

              <DashboardWorkPlanner :dashboard="dashboard" />
            </div>

            <aside class="surface-stack">
              <DetailUtilitySection title="Summary" tone="summary">
                <div class="quest-overview-aside quest-overview-aside--compact">
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Scheduled</span>
                    <span class="quest-overview-aside__value">{{ plannerScheduledCount }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Flexible</span>
                    <span class="quest-overview-aside__value">{{ plannerFlexibleCount }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">My jobs</span>
                    <span class="quest-overview-aside__value">{{ dashboard.dashboardMyQuests.length }}</span>
                  </div>
                </div>
              </DetailUtilitySection>

              <DetailUtilitySection title="Actions" tone="actions">
                <div class="ui-action-stack">
                  <button class="button" type="button" @click="dashboard.openCreateJobDialog()">
                    Create job
                  </button>
                  <button class="button button--secondary" type="button" @click="dashboard.openFindWorkDialog()">
                    Find job
                  </button>
                </div>
              </DetailUtilitySection>
            </aside>
          </UiWorkspace>
        </section>

        <section v-else-if="dashboard.activeTab === 'create-job'" class="surface-stack">
          <UiWorkspace variant="detail">
            <div class="surface-stack">
              <UiSurfaceSection eyebrow="Create job" title="Your offered jobs" subtitle="Manage the jobs you posted and open the compose flow only when you want to create a new one.">
                <template #actions>
                  <button class="button button--action" type="button" @click="dashboard.openCreateJobDialog()">
                    Create job
                  </button>
                </template>
              </UiSurfaceSection>

              <DashboardMyQuests
                :dashboard="dashboard"
                title="Your jobs"
                subtitle=""
                empty-message="No jobs posted yet."
                :show-header="false"
              />
            </div>

            <aside class="surface-stack">
              <DetailUtilitySection title="Summary" tone="summary">
                <div class="quest-overview-aside quest-overview-aside--compact">
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Posted jobs</span>
                    <span class="quest-overview-aside__value">{{ dashboard.dashboardMyQuests.length }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Active</span>
                    <span class="quest-overview-aside__value">{{ dashboard.activeMyQuestsCount }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Completed</span>
                    <span class="quest-overview-aside__value">{{ dashboard.completedMyQuestsCount }}</span>
                  </div>
                </div>
              </DetailUtilitySection>

              <DetailUtilitySection title="Actions" tone="actions">
                <div class="ui-action-stack">
                  <button class="button" type="button" @click="dashboard.openCreateJobDialog()">
                    Create job
                  </button>
                </div>
              </DetailUtilitySection>
            </aside>
          </UiWorkspace>
        </section>

        <section v-else-if="dashboard.activeTab === 'find-work'" class="surface-stack">
          <UiWorkspace variant="detail">
            <div class="surface-stack">
              <UiSurfaceSection eyebrow="Find job" title="Your applications" subtitle="Track your applications here and open search only when you want to browse more jobs.">
                <template #actions>
                  <button class="button button--action" type="button" @click="dashboard.openFindWorkDialog()">
                    Find job
                  </button>
                </template>
              </UiSurfaceSection>

              <DashboardMyApplications
                :dashboard="dashboard"
                title="Your applications"
                subtitle=""
                empty-message="No applications yet."
                :show-header="false"
              />
            </div>

            <aside class="surface-stack">
              <DetailUtilitySection title="Summary" tone="summary">
                <div class="quest-overview-aside quest-overview-aside--compact">
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Applications</span>
                    <span class="quest-overview-aside__value">{{ dashboard.myApplications.length }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Pending</span>
                    <span class="quest-overview-aside__value">{{ dashboard.pendingWorkApplicationsCount }}</span>
                  </div>
                  <div class="quest-overview-aside__row">
                    <span class="quest-overview-aside__label">Active work</span>
                    <span class="quest-overview-aside__value">{{ dashboard.activeWorkApplicationsCount }}</span>
                  </div>
                </div>
              </DetailUtilitySection>

              <DetailUtilitySection title="Actions" tone="actions">
                <div class="ui-action-stack">
                  <button class="button" type="button" @click="dashboard.openFindWorkDialog()">
                    Find job
                  </button>
                </div>
              </DetailUtilitySection>
            </aside>
          </UiWorkspace>
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
          title="Find work"
          size="xl"
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
