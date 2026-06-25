<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import ApplicationEditFields from "../shared/ApplicationEditFields.vue"
import type {DashboardApplicationEditFacade} from "../../composables/dashboard/dashboardFacades.ts"
import {createApplicationDialogViewState} from "../../composables/dashboard/createApplicationDialogViewState.ts"
import {useApplicationDialogUiActions} from "../../composables/dashboard/useApplicationDialogUiActions.ts"
import ApplicationDetailSections from "../shared/ApplicationDetailSections.vue"
import ApplicationSummaryCard from "../shared/ApplicationSummaryCard.vue"

const props = defineProps<{
  dashboard: DashboardApplicationEditFacade
}>()

const viewState = createApplicationDialogViewState(props.dashboard)
const {
  application,
  isEditing,
  isWithdrawing,
  actionMessage,
  actionMessageTone,
  canEdit,
  canWithdraw,
  quest
} = viewState
const {
  startEditing,
  discardEditing,
  openQuest,
  withdrawApplication
} = useApplicationDialogUiActions(props.dashboard, viewState)
</script>

<template>
  <UiDialog
    :open="!!application"
    :title="application?.questTitle ?? 'Application'"
    subtitle=""
    size="lg"
    @close="props.dashboard.closeApplicationDialog()"
  >
    <template #actions>
      <button v-if="canEdit && !isEditing" class="button button--secondary" type="button" @click="startEditing">Edit</button>
    </template>

    <div v-if="application" class="surface-stack">
      <ApplicationSummaryCard :application="application" kicker="Your application" />

      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <ApplicationDetailSections
        v-if="quest"
        :application="application"
        context-eyebrow="Quest context"
        :posted-by-label="quest.creatorUsername"
        @open-posted-by="props.dashboard.openUserProfileDialog(quest.creatorId)"
      />

      <form v-if="canEdit && isEditing" class="form-stack form-stack--compact calendar-application-form" @submit.prevent="props.dashboard.saveEditedApplication(application!.questId)">
        <DashboardEditSheet :minimal="true">
          <ApplicationEditFields
            :message="props.dashboard.editApplicationMessage"
            :price="props.dashboard.editApplicationPrice"
            price-placeholder="50"
            @update:message="props.dashboard.editApplicationMessage = $event"
            @update:price="props.dashboard.editApplicationPrice = $event"
          />

          <template #actions>
            <button class="button button--action" type="submit">Save changes</button>
            <button class="button button--ghost" type="button" @click="discardEditing">Discard changes</button>
          </template>
        </DashboardEditSheet>
      </form>

      <UiFormActions v-else align="end">
        <button class="button button--secondary" type="button" @click="openQuest">
          Open quest
        </button>
        <button
          v-if="canWithdraw"
          class="button button--danger"
          type="button"
          :disabled="isWithdrawing"
          @click="withdrawApplication"
        >
          Withdraw application
        </button>
      </UiFormActions>
    </div>
  </UiDialog>
</template>
