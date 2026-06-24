<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import DashboardEditSheet from "./DashboardEditSheet.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import RichTextEditor from "../../../../components/editor/RichTextEditor.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {createApplicationDialogViewState} from "../../composables/dashboard/createApplicationDialogViewState.ts"
import {useApplicationDialogUiActions} from "../../composables/dashboard/useApplicationDialogUiActions.ts"

const props = defineProps<{
  dashboard: QuestDashboard
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

    <div v-if="application" class="stack dialog-sheet">
      <section class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="application.presentation.statusBadgeClass">
            {{ application.presentation.statusLabel }}
          </span>
          <span class="dialog-focus-card__kicker">Your application</span>
        </div>

        <div class="dialog-focus-card__title">
          {{ application!.questTitle }}
        </div>

        <div class="dialog-focus-card__meta">
          <span>$ {{ application!.proposedPrice }}</span>
          <span>Quest status: {{ application.presentation.questStatusLabel }}</span>
        </div>
      </section>

      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <section class="dialog-focus-card dialog-focus-card--soft">
        <div class="dialog-focus-card__section-title">Message</div>
        <ProfileBio
          v-if="richTextHasContent(application!.message)"
          class="dialog-sheet__description dialog-sheet__description--flat"
          :text="application!.message"
        />
      </section>

      <section v-if="quest" class="dialog-focus-card dialog-focus-card--soft">
        <div class="dialog-focus-card__section-title">Quest context</div>
        <div class="dialog-focus-grid">
          <div class="field">
            <span class="label">Scheduled time</span>
            <strong>{{ application.presentation.questTermLabel }}</strong>
          </div>
          <div class="field">
            <span class="label">Posted by</span>
            <button class="dialog-inline-link" type="button" @click="props.dashboard.openUserProfileDialog(quest.creatorId)">
              {{ quest.creatorUsername }}
            </button>
          </div>
        </div>
      </section>

      <form v-if="canEdit && isEditing" class="stack calendar-application-form" @submit.prevent="props.dashboard.saveEditedApplication(application!.questId)">
        <DashboardEditSheet :minimal="true">
          <div class="dashboard-edit-form dashboard-edit-form--dialog dashboard-edit-form--application">
            <label class="field dashboard-edit-field dashboard-edit-field--message">
              <span class="label">Message</span>
              <RichTextEditor
                v-model="props.dashboard.editApplicationMessage"
                placeholder=""
                toolbar-label="Message tools"
              />
            </label>

            <label class="field dashboard-edit-field dashboard-edit-field--price">
              <span class="label">Proposed price</span>
              <div class="dashboard-edit-amount">
                <span class="dashboard-edit-amount__symbol" aria-hidden="true">$</span>
                <input
                  v-model="props.dashboard.editApplicationPrice"
                  class="input dashboard-edit-amount__input"
                  inputmode="decimal"
                  placeholder="50"
                />
              </div>
            </label>
          </div>

          <template #actions>
            <button class="button button--action" type="submit">Save changes</button>
            <button class="button button--ghost" type="button" @click="discardEditing">Discard changes</button>
          </template>
        </DashboardEditSheet>
      </form>

      <div v-else class="dialog-sheet__footer">
        <UiFormActions>
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
    </div>
  </UiDialog>
</template>
