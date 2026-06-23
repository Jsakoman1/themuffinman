<script setup lang="ts">
import {computed, ref} from "vue"
import UiDialog from "../ui/UiDialog.vue"
import UiStatusBanner from "../ui/UiStatusBanner.vue"
import ProfileBio from "../profile/ProfileBio.vue"
import DashboardQuestApplications from "./DashboardQuestApplications.vue"
import DashboardQuestApplyForm from "./DashboardQuestApplyForm.vue"
import DashboardQuestEditForm from "./DashboardQuestEditForm.vue"
import {richTextHasContent} from "../../shared/richText.ts"
import {useDialogActionState} from "../../composables/useDialogActionState.ts"
import type {QuestDashboard} from "../../composables/useQuestDashboard.ts"
import {closeAfterDelay} from "../../lib/dialogFlow.ts"

const props = defineProps<{
  dashboard: QuestDashboard
}>()

const quest = computed(() => props.dashboard.selectedQuestDialog)
const applications = computed(() => {
  if (!quest.value) {
    return []
  }

  return props.dashboard.applicationsForQuest(quest.value.id)
})

const isEditing = ref(false)
const isDeleting = ref(false)
const isTermDecisioning = ref(false)
const showTermChangeDetails = ref(false)
const actionBanner = useDialogActionState(quest, () => {
  isEditing.value = false
  isDeleting.value = false
  isTermDecisioning.value = false
})
const actionMessage = actionBanner.message
const actionMessageTone = actionBanner.tone

const canEdit = computed(() => quest.value?.allowedActions.includes("EDIT") ?? false)

const canApply = computed(() => quest.value?.allowedActions.includes("APPLY") ?? false)

const applicationMessage = computed(() => {
  if (!quest.value) {
    return ""
  }

  return props.dashboard.applicationMessages[quest.value.id] ?? ""
})

const canSubmitApplication = computed(() => richTextHasContent(applicationMessage.value))

const hasApplied = computed(() => {
  return quest.value?.hasApplied ?? false
})

const myApplication = computed(() => {
  if (!quest.value) {
    return null
  }

  return props.dashboard.myApplications.find((application) => application.id === quest.value?.myApplicationId) ?? null
})

const featuredApplication = computed(() => quest.value ? props.dashboard.featuredApplicationForQuest(quest.value.id) : null)

const canShowApplications = computed(() => quest.value?.canViewApplications ?? false)

const canRespondToTermChange = computed(() => quest.value?.allowedActions.includes("CONFIRM_TERM_CHANGE") ?? false)

const canManageExecution = computed(() => {
  return quest.value?.allowedActions.includes("START") || quest.value?.allowedActions.includes("COMPLETE") || false
})

const beginEditQuest = () => {
  if (!quest.value) {
    return
  }

  props.dashboard.startEditingQuest(quest.value)
  isEditing.value = true
}

const setActionMessage = (message: string, tone: "success" | "warning" = "success") => {
  actionBanner.show(message, tone)
}

const closeQuest = () => {
  if (!quest.value) {
    return
  }

  const confirmed = window.confirm("Are you sure you want to delete this quest? This cannot be undone.")
  if (!confirmed) {
    return
  }

  isDeleting.value = true
  setActionMessage("Deleting quest...", "warning")

  const questId = quest.value.id
  void (async () => {
    const deleted = await props.dashboard.deleteQuest(questId)
    if (!deleted) {
      isDeleting.value = false
      return
    }

    setActionMessage("Quest deleted.")
    closeAfterDelay(() => {
      props.dashboard.closeQuestDialog()
      isDeleting.value = false
    })
  })()
}

const approveApplication = (applicationId: number) => {
  if (!quest.value) {
    return
  }

  const questId = quest.value.id
  void (async () => {
    const approved = await props.dashboard.approveApplication(questId, applicationId)
    if (!approved) {
      return
    }

    setActionMessage("Application approved.")
    closeAfterDelay(() => {
      props.dashboard.closeQuestDialog()
      isEditing.value = false
      isDeleting.value = false
    })
  })()
}

const declineApplication = (applicationId: number) => {
  if (!quest.value) {
    return
  }

  const questId = quest.value.id
  void (async () => {
    const declined = await props.dashboard.declineApplication(questId, applicationId)
    if (!declined) {
      return
    }

    setActionMessage("Application declined.", "warning")
    closeAfterDelay(() => {
      props.dashboard.closeQuestDialog()
      isEditing.value = false
      isDeleting.value = false
    })
  })()
}

const confirmTermChange = () => {
  if (!quest.value) {
    return
  }

  isTermDecisioning.value = true
  setActionMessage("Confirming quest term...", "warning")

  const questId = quest.value.id
  void (async () => {
    const confirmed = await props.dashboard.confirmQuestTermChange(questId)
    if (!confirmed) {
      isTermDecisioning.value = false
      return
    }

    setActionMessage("Quest term confirmed.")
    closeAfterDelay(() => {
      props.dashboard.closeQuestDialog()
      isTermDecisioning.value = false
    })
  })()
}

const rejectTermChange = () => {
  if (!quest.value) {
    return
  }

  isTermDecisioning.value = true
  setActionMessage("Rejecting quest term...", "warning")

  const questId = quest.value.id
  void (async () => {
    const rejected = await props.dashboard.rejectQuestTermChange(questId)
    if (!rejected) {
      isTermDecisioning.value = false
      return
    }

    setActionMessage("Quest term change rejected.", "warning")
    closeAfterDelay(() => {
      props.dashboard.closeQuestDialog()
      isTermDecisioning.value = false
    })
  })()
}
</script>

<template>
  <UiDialog
    :open="!!quest"
    :title="quest?.title ?? 'Quest'"
    subtitle=""
    size="lg"
    @close="props.dashboard.closeQuestDialog()"
  >
    <template v-if="canEdit && !isEditing" #actions>
      <button class="button button--secondary" type="button" @click="beginEditQuest">Edit</button>
    </template>

    <div v-if="quest" class="stack dialog-sheet">
      <section v-if="myApplication && !isEditing" class="dialog-focus-card dialog-focus-card--application">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <div class="dialog-focus-card__meta">
            <span :class="['badge', props.dashboard.statusBadgeClass(myApplication.status)]">
              {{ props.dashboard.formatApplicationStatus(myApplication.status) }}
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
            v-if="myApplication.status === 'PENDING'"
            class="button button--secondary"
            type="button"
            @click="props.dashboard.openApplicationDialog(myApplication.id)"
          >
            Open my application
          </button>
          <RouterLink class="button button--ghost" :to="`/quests/${quest.id}`">
            Open quest page
          </RouterLink>
        </div>
      </section>

      <section class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <div class="dialog-focus-card__meta">
            <span :class="['badge', props.dashboard.statusBadgeClass(quest.status)]">
              {{ props.dashboard.formatStatus(quest.status) }}
            </span>
            <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
          </div>
          <button class="dialog-inline-link" type="button" @click="props.dashboard.openUserProfileDialog(quest.creatorId)">
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
          <strong>{{ props.dashboard.formatQuestTermLabel(quest) }}</strong>
        </div>
        <div class="field">
          <span class="label">Type</span>
          <strong>{{ quest.termFixed ? "Fixed" : "Negotiable" }}</strong>
        </div>
        <div v-if="quest.assigneeTarget === null || quest.assigneeTarget > 1" class="field">
          <span class="label">Workers</span>
          <strong>{{ quest.assigneeTarget === null ? "Unlimited" : quest.assigneeTarget }}</strong>
        </div>
      </div>

      <div v-if="quest.status === 'WAITING_CONFIRMATION'" class="compact-disclosure">
        <button class="compact-disclosure--launch" type="button" @click="showTermChangeDetails = !showTermChangeDetails">
          Term change waiting
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning">
          <div class="muted">Current: {{ props.dashboard.formatQuestTermLabel(quest) }}</div>
          <div class="muted">Pending: {{ props.dashboard.formatQuestTermFromParts(quest.pendingScheduledAt, quest.pendingEndsAt, quest.pendingTermFixed ?? quest.termFixed) }}</div>
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

      <div v-else-if="quest.status === 'OPEN' && hasApplied" class="empty-state">
        Application sent. Check My applications.
      </div>

      <div v-else-if="quest.status !== 'CANCELLED'" class="stack dialog-sheet__section">
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

      <div v-if="canManageExecution" class="dialog-sheet__footer">
        <div class="button-row">
          <button
            v-if="quest.status === 'ASSIGNED'"
            class="button"
            type="button"
            @click="props.dashboard.updateQuestStatus(quest.id, 'start')"
          >
            Start work
          </button>
          <button
            v-if="quest.status === 'IN_PROGRESS'"
            class="button"
            type="button"
            @click="props.dashboard.updateQuestStatus(quest.id, 'complete')"
          >
            Mark complete
          </button>
        </div>
      </div>

      <div v-if="quest.allowedActions.includes('DELETE') && !isEditing" class="dialog-sheet__footer">
        <button class="button button--danger" type="button" :disabled="isDeleting" @click="closeQuest">Delete quest</button>
      </div>

      <div v-if="canRespondToTermChange" class="dialog-sheet__footer">
        <div class="button-row">
          <button class="button button--secondary" type="button" :disabled="isTermDecisioning" @click="confirmTermChange">
            Confirm term change
          </button>
          <button class="button button--danger" type="button" :disabled="isTermDecisioning" @click="rejectTermChange">
            Reject term change
          </button>
        </div>
      </div>

    </div>
  </UiDialog>
</template>
