<script setup lang="ts">
import {computed} from "vue"
import UiDialog from "../ui/UiDialog.vue"
import DashboardMyQuests from "./DashboardMyQuests.vue"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const openQuests = computed(() => props.dashboard.myQuests.filter((quest) => quest.status === "OPEN"))
const waitingQuests = computed(() => props.dashboard.myQuests.filter((quest) => quest.status === "WAITING_CONFIRMATION"))
</script>

<template>
  <UiDialog
    :open="dashboard.isOpenWorkDialogOpen"
    title="Open work"
    subtitle="Jobs that still need a reply or confirmation."
    size="xl"
    @close="dashboard.closeOpenWorkDialog()"
  >
    <div class="stack dashboard-open-work">
      <section v-if="waitingQuests.length" class="dashboard-open-work__section">
        <h3 class="dashboard-open-work__title">Needs confirmation</h3>
        <DashboardMyQuests
          :dashboard="dashboard"
          :quests="waitingQuests"
          empty-message="No jobs waiting for confirmation."
          :show-header="false"
          :boxed="false"
        />
      </section>

      <section v-if="openQuests.length" class="dashboard-open-work__section">
        <h3 class="dashboard-open-work__title">Open for agreement</h3>
        <DashboardMyQuests
          :dashboard="dashboard"
          :quests="openQuests"
          empty-message="No open jobs right now."
          :show-header="false"
          :boxed="false"
        />
      </section>

      <div v-if="!waitingQuests.length && !openQuests.length" class="empty-state">
        No open work right now.
      </div>
    </div>
  </UiDialog>
</template>
