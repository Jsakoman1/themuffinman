<script setup lang="ts">
import {computed} from "vue"
import type {QuestApplication} from "../../api/workmarketApi.ts"
import type {DashboardQuestApplicationsFacade} from "../../composables/dashboard/dashboardFacades.ts"
import ApplicationManagementCard from "../shared/ApplicationManagementCard.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"

const props = defineProps<{
  dashboard: DashboardQuestApplicationsFacade
  questId: number
  applications: QuestApplication[]
  approvedApplications: QuestApplication[]
  canShowApplications: boolean
  eyebrow?: string
  title?: string
}>()
const remainingApplications = computed(() => props.applications)

defineEmits<{
  approve: [id: number]
  decline: [id: number]
}>()
</script>

<template>
  <UiSurfaceSection
    v-if="canShowApplications || approvedApplications.length || dashboard.canRevealHiddenApplicationsForQuest(questId)"
    compact
    :eyebrow="eyebrow ?? 'Applications'"
    :title="title ?? 'What people offer'"
  >
    <div class="surface-stack surface-stack--compact">
      <section v-if="approvedApplications.length" class="surface-stack surface-stack--compact">
        <div class="surface-inline-spread">
          <strong>Approved</strong>
          <span class="muted">{{ approvedApplications.length }}</span>
        </div>
        <ApplicationManagementCard
          v-for="application in approvedApplications"
          :key="application.id"
          :application="application"
          :selected="true"
          :show-status="false"
          @open-applicant="dashboard.openUserProfileDialog(application.applicantId)"
        />
      </section>

      <section v-if="remainingApplications.length" class="surface-stack surface-stack--compact">
        <div class="surface-inline-spread">
          <strong>Pending and other</strong>
          <span class="muted">{{ remainingApplications.length }}</span>
        </div>

        <ApplicationManagementCard
          v-for="application in remainingApplications"
          :key="application.id"
          :application="application"
          @open-applicant="dashboard.openUserProfileDialog(application.applicantId)"
        >
          <template v-if="application.presentation.showManagementActions" #actions>
            <button v-if="application.presentation.canApprove" class="button button--secondary" type="button" @click="$emit('approve', application.id)">Approve</button>
            <button v-if="application.presentation.canDecline" class="button button--danger" type="button" @click="$emit('decline', application.id)">Decline</button>
          </template>
        </ApplicationManagementCard>
      </section>

      <div v-if="!approvedApplications.length && !remainingApplications.length" class="empty-state">Nothing here yet.</div>

      <div v-if="dashboard.canRevealHiddenApplicationsForQuest(questId)" class="button-row">
        <button class="button button--secondary" type="button" @click="dashboard.toggleApplicationRevealForQuest(questId)">
          {{ dashboard.applicationRevealLabel(questId) }}
          <span v-if="dashboard.hiddenApplicationsCountForQuest(questId) > 0">
            ({{ dashboard.hiddenApplicationsCountForQuest(questId) }})
          </span>
        </button>
      </div>
    </div>
  </UiSurfaceSection>
</template>
