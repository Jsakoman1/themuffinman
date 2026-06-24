<script setup lang="ts">
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiConfirmDialog from "../../../../components/ui/UiConfirmDialog.vue"
import UiFormActions from "../../../../components/ui/UiFormActions.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import DashboardQuestApplications from "./DashboardQuestApplications.vue"
import DashboardQuestApplyForm from "./DashboardQuestApplyForm.vue"
import DashboardQuestEditForm from "./DashboardQuestEditForm.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {createQuestDialogViewState} from "../../composables/dashboard/createQuestDialogViewState.ts"
import {useQuestDialogUiActions} from "../../composables/dashboard/useQuestDialogUiActions.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const viewState = createQuestDialogViewState(props.dashboard)
const {
  quest,
  applications,
  isEditing,
  isDeleting,
  isDeleteConfirmDialogOpen,
  isTermDecisioning,
  showTermChangeDetails,
  actionMessage,
  actionMessageTone,
  canEdit,
  canApply,
  canSubmitApplication,
  myApplication,
  featuredApplication,
  canShowApplications,
  canRespondToTermChange,
  termChangeVisible,
  applicationSentVisible,
  canOpenMyApplication,
  deleteVisible,
  executionPrimaryAction,
  executionHelperText
} = viewState

const {
  beginEditQuest,
  closeQuest,
  cancelDeleteQuest,
  confirmDeleteQuest,
  approveApplication,
  declineApplication,
  confirmTermChange,
  rejectTermChange
} = useQuestDialogUiActions(props.dashboard, viewState)
</script>

<template>
  <UiDialog
    :open="!!quest"
    :title="quest?.title ?? 'Quest'"
    subtitle=""
    size="lg"
    @close="props.dashboard.closeQuestDialog()"
  >
    <UiConfirmDialog
      :open="isDeleteConfirmDialogOpen"
      title="Delete quest"
      message="Are you sure you want to delete this quest? This cannot be undone."
      confirm-label="Delete"
      confirm-tone="danger"
      :busy="isDeleting"
      @close="cancelDeleteQuest"
      @confirm="confirmDeleteQuest"
    />

    <template v-if="canEdit && !isEditing" #actions>
      <button class="button button--secondary" type="button" @click="beginEditQuest">Edit</button>
    </template>

    <div v-if="quest" class="stack dialog-sheet">
      <section v-if="myApplication && !isEditing" class="dialog-focus-card dialog-focus-card--application">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <div class="dialog-focus-card__meta">
            <span :class="myApplication.presentation.statusBadgeClass">
              {{ myApplication.presentation.statusLabel }}
            </span>
            <span>$ {{ myApplication.proposedPrice }}</span>
          </div>
        </div>

        <ProfileBio
          v-if="richTextHasContent(myApplication.message)"
          class="dialog-sheet__description dialog-sheet__description--flat"
          :text="myApplication.message"
        />

        <div class="dialog-focus-card__footer u-row u-wrap u-gap-8">
          <button
            v-if="canOpenMyApplication"
            class="button button--secondary"
            type="button"
            @click="props.dashboard.openApplicationDialog(myApplication.id)"
          >
            Open my application
          </button>
          <RouterLink class="button button--ghost" :to="routeForNavigationTarget(quest.questNavigation)">
            Open quest page
          </RouterLink>
        </div>
      </section>

      <section class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <div class="dialog-focus-card__meta">
            <span :class="quest.presentation.statusBadgeClass">
              {{ quest.presentation.statusLabel }}
            </span>
            <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
          </div>
          <button class="dialog-inline-link" type="button" @click="props.dashboard.openUserProfileDialog(quest.creatorNavigation.entityId ?? quest.creatorId)">
            {{ quest.creatorUsername }}
          </button>
        </div>
      </section>

      <UiStatusBanner :message="actionMessage" :tone="actionMessageTone" />

      <div v-if="quest.images?.length" class="quest-gallery quest-gallery--dialog">
        <div v-for="(image, index) in quest.images" :key="`${quest.id}-${index}`" class="quest-gallery__item quest-gallery__item--dialog">
          <img class="quest-gallery__image" :src="image" alt="Quest image">
        </div>
      </div>

      <section v-if="richTextHasContent(quest.description)" class="dialog-focus-card dialog-focus-card--soft">
        <ProfileBio class="dialog-sheet__description dialog-sheet__description--flat" :text="quest.description" />
      </section>

      <div class="dialog-focus-grid">
        <div class="field">
          <span class="label">When</span>
          <strong>{{ quest.presentation.termLabel }}</strong>
        </div>
        <div class="field">
          <span class="label">Type</span>
          <strong>{{ quest.presentation.timeTypeLabel }}</strong>
        </div>
        <div v-if="quest.presentation.assigneeTargetVisible" class="field">
          <span class="label">Workers</span>
          <strong>{{ quest.presentation.assigneeTargetLabel }}</strong>
        </div>
      </div>

      <div v-if="termChangeVisible" class="compact-disclosure">
        <button class="compact-disclosure--launch" type="button" @click="showTermChangeDetails = !showTermChangeDetails">
          Term change waiting
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning">
          <div class="muted">Current: {{ quest.presentation.termLabel }}</div>
          <div class="muted">Pending: {{ quest.presentation.pendingTermLabel }}</div>
        </div>
      </div>

      <DashboardQuestEditForm
        v-if="canEdit && isEditing"
        :dashboard="props.dashboard"
        @discard="props.dashboard.cancelEditingQuest(); isEditing = false"
      />

      <DashboardQuestApplyForm
        v-else-if="canApply"
        :dashboard="props.dashboard"
        :quest="quest"
        :can-submit="canSubmitApplication"
      />

      <div v-else-if="applicationSentVisible" class="empty-state">
        Application sent. Check My applications.
      </div>

      <div v-else-if="!applicationSentVisible && quest.status !== 'CANCELLED'" class="stack dialog-sheet__section">
        <DashboardQuestApplications
          :dashboard="props.dashboard"
          :quest-id="quest.id"
          :applications="applications"
          :featured-application="featuredApplication"
          :can-show-applications="canShowApplications"
          @approve="approveApplication"
          @decline="declineApplication"
        />
      </div>

      <div v-else class="stack">
        <button class="button button--secondary" type="button" @click="props.dashboard.reopenQuest(quest)">
          Copy to Create work
        </button>
      </div>

      <div v-if="executionPrimaryAction || executionHelperText" class="dialog-sheet__footer">
        <UiFormActions>
          <button
            v-if="executionPrimaryAction === 'START'"
            class="button"
            type="button"
            @click="props.dashboard.updateQuestStatus(quest.id, 'start')"
          >
            Start work
          </button>
          <button
            v-if="executionPrimaryAction === 'COMPLETE'"
            class="button"
            type="button"
            @click="props.dashboard.updateQuestStatus(quest.id, 'complete')"
          >
            Mark complete
          </button>
          <span v-if="executionHelperText" class="muted">{{ executionHelperText }}</span>
        </UiFormActions>
      </div>

      <div v-if="deleteVisible && !isEditing" class="dialog-sheet__footer">
        <button class="button button--danger" type="button" :disabled="isDeleting" @click="closeQuest">Delete quest</button>
      </div>

      <div v-if="canRespondToTermChange" class="dialog-sheet__footer">
        <UiFormActions>
          <button class="button button--secondary" type="button" :disabled="isTermDecisioning" @click="confirmTermChange">
            Confirm term change
          </button>
          <button class="button button--danger" type="button" :disabled="isTermDecisioning" @click="rejectTermChange">
            Reject term change
          </button>
        </UiFormActions>
      </div>

    </div>
  </UiDialog>
</template>
