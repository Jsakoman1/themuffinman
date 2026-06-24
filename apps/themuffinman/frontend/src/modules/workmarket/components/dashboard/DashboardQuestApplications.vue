<script setup lang="ts">
import type {QuestApplication} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"

defineProps<{
  dashboard: QuestDashboard
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
  <div v-if="featuredApplication" class="dialog-application-card dialog-application-card--selected">
    <div class="dialog-application-card__top">
      <button class="dialog-inline-link" type="button" @click="dashboard.openUserProfileDialog(featuredApplication.applicantId)">
        {{ featuredApplication.applicantUsername }}
      </button>
    </div>
    <div class="dialog-application-card__price">$ {{ featuredApplication.proposedPrice }}</div>
    <ProfileBio v-if="richTextHasContent(featuredApplication.message)" class="dialog-application-card__message" :text="featuredApplication.message" />
  </div>

  <div v-if="canShowApplications" class="stack dialog-sheet__applications">
    <div class="dialog-sheet__section-title">Applications</div>
    <div v-if="applications.length" class="stack">
      <div v-for="application in applications" :key="application.id" class="dialog-application-card">
        <div class="dialog-application-card__top">
          <button class="dialog-inline-link" type="button" @click="dashboard.openUserProfileDialog(application.applicantId)">
            {{ application.applicantUsername }}
          </button>
          <span :class="application.presentation.statusBadgeClass">
            {{ application.presentation.statusLabel }}
          </span>
        </div>
        <ProfileBio v-if="richTextHasContent(application.message)" class="dialog-application-card__message" :text="application.message" />
        <div class="dialog-application-card__price">$ {{ application.proposedPrice }}</div>
        <div v-if="application.presentation.showManagementActions" class="button-row">
          <button v-if="application.presentation.canApprove" class="button button--secondary" type="button" @click="$emit('approve', application.id)">Approve</button>
          <button v-if="application.presentation.canDecline" class="button button--danger" type="button" @click="$emit('decline', application.id)">Decline</button>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">Nothing here yet.</div>
  </div>

  <div v-if="dashboard.canRevealHiddenApplicationsForQuest(questId)" class="button-row">
    <button class="button button--secondary" type="button" @click="dashboard.toggleApplicationRevealForQuest(questId)">
      {{ dashboard.applicationRevealLabel(questId) }}
      <span v-if="dashboard.hiddenApplicationsCountForQuest(questId) > 0">
        ({{ dashboard.hiddenApplicationsCountForQuest(questId) }})
      </span>
    </button>
  </div>
</template>
