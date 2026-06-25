<script setup lang="ts">
import DashboardEditSheet from "./DashboardEditSheet.vue"
import QuestEditFields from "../shared/QuestEditFields.vue"
import type {DashboardQuestCreateFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {useDashboardPostWorkState} from "../../composables/dashboard/useDashboardPostWorkState.ts"

const props = withDefaults(defineProps<{
  dashboard: DashboardQuestCreateFacade
  boxed?: boolean
}>(), {
  boxed: true,
})

const {
  handleQuestImagesChange,
  toggleQuestAudience,
  toggleQuestCircle
} = useDashboardPostWorkState(props.dashboard)
</script>

<template>
  <section class="surface-stack">
    <article :class="[
      'dashboard-work-panel',
      'surface-form-shell',
      {
        card: props.boxed,
        'dashboard-work-panel--dialog': !props.boxed,
        'ui-pulse': dashboard.successPulseTarget === 'create-job'
      }
    ]">
      <form class="form-stack" @submit.prevent="dashboard.createQuest">
        <DashboardEditSheet :minimal="true">
          <QuestEditFields
            :title="dashboard.questTitle"
            :description="dashboard.questDescription"
            :award-amount="dashboard.questAwardAmount"
            :term-mode="dashboard.questTermMode"
            :scheduled-at="dashboard.questScheduledAt"
            :ends-at="dashboard.questEndsAt"
            :audience="dashboard.questAudience"
            :audience-options="dashboard.questAudienceOptions"
            :circles="dashboard.circles"
            :selected-circle-ids="dashboard.questSelectedCircleIds"
            :images="dashboard.questImages"
            show-images
            :show-admin-fields="dashboard.adminModeEnabled"
            :creator-id="dashboard.questCreatorId"
            :creator-options="dashboard.appUsers"
            @update:title="dashboard.questTitle = $event"
            @update:description="dashboard.questDescription = $event"
            @update:award-amount="dashboard.questAwardAmount = $event"
            @update:term-mode="dashboard.setQuestTermMode($event)"
            @update:scheduled-at="dashboard.questScheduledAt = $event"
            @update:ends-at="dashboard.questEndsAt = $event"
            @update:audience="toggleQuestAudience($event)"
            @toggle:circle="toggleQuestCircle($event)"
            @change:images="handleQuestImagesChange"
            @remove:image="dashboard.removeQuestImage($event)"
            @update:creator-id="dashboard.questCreatorId = $event"
          />

          <template #actions>
            <button class="button button--action button--flat-primary" type="submit">Publish job</button>
          </template>
        </DashboardEditSheet>
      </form>
    </article>
  </section>
</template>
