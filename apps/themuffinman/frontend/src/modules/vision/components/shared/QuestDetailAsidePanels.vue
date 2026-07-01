<script setup lang="ts">
import {computed} from "vue"
import DetailUtilitySection from "./DetailUtilitySection.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiStarRatingInput from "../../../../components/ui/UiStarRatingInput.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {Quest, QuestApplication, QuestDetail} from "../../api/visionApi.ts"
import {formatQuestScheduleForDisplay, formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"
import {formatApplicationPrice, formatQuestReward} from "../../shared/pricing.ts"

const props = withDefaults(defineProps<{
  quest?: Quest | null
  myApplication?: QuestApplication | null
  applicationsView?: QuestDetail["applicationsView"] | null
  showOverview?: boolean
  showOverviewStatus?: boolean
  showMyApplication?: boolean
  canOpenApplication?: boolean
  applicationOpenLabel?: string
  showTermChangeDetails?: boolean
  executionSection?: QuestDetail["sections"]["execution"] | null
  termChangeSection?: QuestDetail["sections"]["termChange"] | null
  managementSection?: QuestDetail["sections"]["management"] | null
  reviewSection?: QuestDetail["sections"]["review"] | null
  isSaving?: boolean
  isActionInProgress?: boolean
  hasSubmittedReview?: boolean
  reviewStars?: number
  reviewComment?: string
}>(), {
  quest: null,
  myApplication: null,
  applicationsView: null,
  showOverview: true,
  showOverviewStatus: true,
  showMyApplication: true,
  canOpenApplication: false,
  applicationOpenLabel: "Open application",
  showTermChangeDetails: false,
  executionSection: null,
  termChangeSection: null,
  managementSection: null,
  reviewSection: null,
  isSaving: false,
  isActionInProgress: false,
  hasSubmittedReview: false,
  reviewStars: 0,
  reviewComment: "",
})

const hasVisibleActions = computed(() => {
  return Boolean(
    props.managementSection?.deleteVisible ||
    props.executionSection?.primaryAction ||
    props.termChangeSection?.actionable,
  )
})

const actionHelperText = computed(() => {
  if (!props.executionSection?.primaryAction) {
    return ""
  }

  return props.executionSection.helperText ?? ""
})

const showPostingSettings = computed(() => props.managementSection?.postingSettingsVisible ?? false)
const approvedApplicants = computed(() => props.applicationsView?.approvedApplications ?? [])

const emit = defineEmits<{
  (event: "toggle-term-change"): void
  (event: "open-application"): void
  (event: "select-review-stars", stars: number): void
  (event: "update:reviewComment", value: string): void
  (event: "submit-review"): void
  (event: "start-work"): void
  (event: "complete-work"): void
  (event: "delete-quest"): void
  (event: "assign-now"): void
  (event: "confirm-term-change"): void
  (event: "reject-term-change"): void
}>()
</script>

<template>
  <aside class="surface-stack surface-stack--aside">
    <section v-if="quest && showOverview" class="quest-overview-panel">
      <div class="quest-overview-aside">
        <div class="surface-price-pill surface-price-pill--hero quest-overview-aside__reward">
          <span class="surface-price-pill__label">Reward</span>
          <span class="surface-price-pill__amount">{{ formatQuestReward(quest.awardAmount) }}</span>
        </div>

        <div class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">When</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ formatQuestScheduleForDisplay(quest.scheduledAt, quest.endsAt) }}</span>
        </div>

        <div v-if="quest.presentation.locationLabel" class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">Location</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ quest.presentation.locationLabel }}</span>
        </div>

        <div v-if="quest.presentation.locationVisibilitySummary" class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">Location sharing</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ quest.presentation.locationVisibilitySummary }}</span>
        </div>

        <div v-if="quest.presentation.assigneeTargetVisible" class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Workers</span>
          <span class="quest-overview-aside__value">{{ quest.presentation.assigneeTargetLabel }}</span>
        </div>

        <div v-if="quest.presentation.slotProgressLabel" class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Filled</span>
          <span class="quest-overview-aside__value">{{ quest.presentation.slotProgressLabel }}</span>
        </div>

        <div v-if="quest.presentation.remainingSlotsLabel" class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Open spots</span>
          <span class="quest-overview-aside__value">{{ quest.presentation.remainingSlotsLabel }}</span>
        </div>

        <div class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">Time</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ quest.presentation.timeTypeLabel }}</span>
        </div>

        <div
          v-if="quest.presentation.approvedApplicantsVisible && approvedApplicants.length"
          class="quest-overview-aside__row quest-overview-aside__row--stack"
        >
          <span class="quest-overview-aside__label">Approved workers</span>
          <div class="ui-chip-group ui-chip-group--stack">
            <span
              v-for="application in approvedApplicants"
              :key="application.id"
              class="ui-chip ui-chip--wide ui-chip--active"
            >
              {{ application.applicantUsername }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <DetailUtilitySection v-if="showPostingSettings" title="Posting settings">
      <div class="quest-overview-aside">
        <div class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Visibility</span>
          <span class="quest-overview-aside__value">{{ managementSection?.audienceLabel }}</span>
        </div>

        <div v-if="quest?.presentation.locationSourceSummary" class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">Location source</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ quest.presentation.locationSourceSummary }}</span>
        </div>

        <div
          v-if="managementSection?.visibleToCirclesLabel"
          class="quest-overview-aside__row quest-overview-aside__row--stack"
        >
          <span class="quest-overview-aside__label">Visible to circles</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">
            {{ managementSection.visibleToCirclesLabel }}
          </span>
        </div>
      </div>
    </DetailUtilitySection>

    <DetailUtilitySection v-if="showMyApplication && myApplication" title="Your application" tone="summary">
      <template #actions>
        <span :class="myApplication.presentation.statusBadgeClass">
          {{ myApplication.presentation.statusLabel }}
        </span>
      </template>

      <div v-if="myApplication.proposedPrice !== null && myApplication.proposedPrice !== undefined" class="surface-price">
        {{ formatApplicationPrice(myApplication.proposedPrice) }}
      </div>

      <ProfileBio
        v-if="richTextHasContent(myApplication.message)"
        class="ui-content-prose ui-content-prose--flat ui-copy-block"
        :text="myApplication.message"
      />

      <div v-if="canOpenApplication" class="surface-actions">
        <button class="button button--secondary" type="button" @click="emit('open-application')">
          {{ applicationOpenLabel }}
        </button>
      </div>
    </DetailUtilitySection>

    <DetailUtilitySection v-if="termChangeSection?.visible" title="Term change">
      <div class="compact-disclosure">
        <button class="compact-disclosure--launch" type="button" @click="emit('toggle-term-change')">
          {{ termChangeSection.summaryLabel }}
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning mt-2">
          <div class="stack">
            <div class="muted">
              Current term:
              {{ formatQuestTermForDisplay(termChangeSection.currentScheduledAt, termChangeSection.currentEndsAt, termChangeSection.currentTermFixed) }}
            </div>
            <div class="muted">
              Pending term:
              {{ termChangeSection.pendingScheduledAt
                ? formatQuestTermForDisplay(termChangeSection.pendingScheduledAt, termChangeSection.pendingEndsAt, termChangeSection.pendingTermFixed ?? termChangeSection.currentTermFixed)
                : "Not set" }}
            </div>
          </div>
        </div>
      </div>
    </DetailUtilitySection>

    <DetailUtilitySection v-if="hasVisibleActions" title="Actions" tone="actions">
      <div class="ui-action-stack">
        <button v-if="executionSection?.primaryAction" class="button" type="button" :disabled="isSaving" @click="executionSection.primaryAction === 'START' ? emit('start-work') : emit('complete-work')">
          {{ executionSection.primaryActionLabel }}
        </button>
        <button v-if="quest?.presentation.canManuallyAssign" class="button button--secondary" type="button" :disabled="isSaving || isActionInProgress" @click="emit('assign-now')">
          Assign now
        </button>
        <span v-if="actionHelperText" class="muted">{{ actionHelperText }}</span>

        <button v-if="termChangeSection?.actionable" class="button button--secondary" type="button" :disabled="isSaving || isActionInProgress" @click="emit('confirm-term-change')">
          {{ termChangeSection.confirmLabel }}
        </button>
        <button v-if="termChangeSection?.actionable" class="button button--danger" type="button" :disabled="isSaving || isActionInProgress" @click="emit('reject-term-change')">
          {{ termChangeSection.rejectLabel }}
        </button>

        <button v-if="managementSection?.deleteVisible" class="button button--danger" type="button" :disabled="isSaving || isActionInProgress" @click="emit('delete-quest')">
          Delete
        </button>
      </div>
    </DetailUtilitySection>

    <DetailUtilitySection v-if="reviewSection?.visible" title="Review">
      <template #actions>
        <span v-if="hasSubmittedReview" class="badge badge--success">Saved</span>
      </template>

      <div v-if="reviewSection.canSubmit" class="review-form ui-review-stack">
        <div class="ui-review-stack__intro">
          <strong>{{ reviewSection.introTitle }}</strong>
          <span v-if="reviewSection.introSubtitle" class="muted">{{ reviewSection.introSubtitle }}</span>
        </div>

        <UiStarRatingInput :model-value="reviewStars" @update:model-value="emit('select-review-stars', $event)" />

        <UiFieldGroup v-if="reviewStars > 0" label="Comment" field-class="quest-detail-review__field">
          <textarea
            :value="reviewComment"
            class="input review-form__textarea"
            maxlength="500"
            :placeholder="reviewSection.placeholder"
            @input="emit('update:reviewComment', ($event.target as HTMLTextAreaElement).value)"
          />
        </UiFieldGroup>

        <div v-if="reviewStars > 0" class="button-row ui-review-stack__actions">
          <button class="button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('submit-review')">
            {{ reviewSection.submitLabel }}
          </button>
          <div class="muted">{{ reviewComment.length }}/500</div>
        </div>
      </div>

      <div v-else class="empty-state empty-state--soft">
        {{ reviewSection.emptyStateMessage }}
      </div>
    </DetailUtilitySection>

    <slot />
  </aside>
</template>
