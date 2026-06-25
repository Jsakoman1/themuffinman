<script setup lang="ts">
import type {QuestApplication} from "../../api/workmarketApi.ts"
import type {DashboardQuestApplicationsFacade} from "../../composables/dashboard/dashboardFacades.ts"
import ApplicationManagementCard from "../shared/ApplicationManagementCard.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"

defineProps<{
  dashboard: DashboardQuestApplicationsFacade
  questId: number
  applications: QuestApplication[]
  featuredApplication: QuestApplication | null
  canShowApplications: boolean
}>()

defineEmits<{
  approve: [id: number]
  decline: [id: number]
}>()
</script>

<template>
  <ApplicationManagementCard
    v-if="featuredApplication"
    :application="featuredApplication"
    :selected="true"
    :show-status="false"
    @open-applicant="dashboard.openUserProfileDialog(featuredApplication.applicantId)"
  />

  <UiSurfaceSection v-if="canShowApplications" compact title="Applications">
    <div v-if="applications.length" class="surface-stack surface-stack--compact">
      <ApplicationManagementCard
        v-for="application in applications"
        :key="application.id"
        :application="application"
        @open-applicant="dashboard.openUserProfileDialog(application.applicantId)"
      >
        <template v-if="application.presentation.showManagementActions" #actions>
          <button v-if="application.presentation.canApprove" class="button button--secondary" type="button" @click="$emit('approve', application.id)">Approve</button>
          <button v-if="application.presentation.canDecline" class="button button--danger" type="button" @click="$emit('decline', application.id)">Decline</button>
        </template>
      </ApplicationManagementCard>
    </div>
    <div v-else class="empty-state">Nothing here yet.</div>
  </UiSurfaceSection>

  <div v-if="dashboard.canRevealHiddenApplicationsForQuest(questId)" class="button-row">
    <button class="button button--secondary" type="button" @click="dashboard.toggleApplicationRevealForQuest(questId)">
      {{ dashboard.applicationRevealLabel(questId) }}
      <span v-if="dashboard.hiddenApplicationsCountForQuest(questId) > 0">
        ({{ dashboard.hiddenApplicationsCountForQuest(questId) }})
      </span>
    </button>
  </div>
</template>
