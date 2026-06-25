<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import DashboardMyQuests from "./DashboardMyQuests.vue"
import DetailDialogFrame from "../shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../shared/DetailUtilitySection.vue"
import {createDashboardOpenWorkState} from "../../composables/dashboard/createDashboardOpenWorkState.ts"
import type {DashboardOpenWorkDialogFacade} from "../../composables/dashboard/dashboardFacades.ts"

const props = defineProps<{
  dashboard: DashboardOpenWorkDialogFacade
}>()

const {openQuests, waitingQuests} = createDashboardOpenWorkState(props.dashboard)
</script>

<template>
  <UiDialog
    :open="dashboard.isOpenWorkDialogOpen"
    title="Open work"
    subtitle=""
    size="xl"
    @close="dashboard.closeOpenWorkDialog()"
  >
    <DetailDialogFrame>
      <template #main>
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
      </template>

      <template #side>
        <DetailUtilitySection title="Summary" tone="summary">
          <div class="quest-overview-aside quest-overview-aside--compact">
            <div class="quest-overview-aside__row">
              <span class="quest-overview-aside__label">Needs confirmation</span>
              <span class="quest-overview-aside__value">{{ waitingQuests.length }}</span>
            </div>
            <div class="quest-overview-aside__row">
              <span class="quest-overview-aside__label">Open for agreement</span>
              <span class="quest-overview-aside__value">{{ openQuests.length }}</span>
            </div>
          </div>
        </DetailUtilitySection>
      </template>
    </DetailDialogFrame>
  </UiDialog>
</template>
