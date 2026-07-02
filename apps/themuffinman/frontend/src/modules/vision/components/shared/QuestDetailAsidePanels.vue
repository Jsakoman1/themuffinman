<script setup lang="ts">
import {computed} from "vue"
import {formatQuestScheduleForDisplay, formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"
import {formatApplicationPrice, formatQuestReward} from "../../shared/pricing.ts"
import type {Quest, QuestApplication, QuestDetail} from "../../api/visionApi.ts"

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

const hasVisibleActions = computed(() => Boolean(
  props.managementSection?.deleteVisible ||
  props.executionSection?.primaryAction ||
  props.termChangeSection?.actionable,
))

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
  <aside class="vision-terminal-feed">
    <section v-if="quest && showOverview" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">overview</p>
      <p class="vision-terminal-feed__line">Reward: {{ formatQuestReward(quest.awardAmount) }}</p>
      <p class="vision-terminal-feed__line">When: {{ formatQuestScheduleForDisplay(quest.scheduledAt, quest.endsAt) }}</p>
      <p v-if="quest.presentation.locationLabel" class="vision-terminal-feed__line">Location: {{ quest.presentation.locationLabel }}</p>
      <p v-if="quest.presentation.locationVisibilitySummary" class="vision-terminal-feed__line">Location sharing: {{ quest.presentation.locationVisibilitySummary }}</p>
      <p v-if="quest.presentation.assigneeTargetVisible" class="vision-terminal-feed__line">Workers: {{ quest.presentation.assigneeTargetLabel }}</p>
      <p v-if="quest.presentation.slotProgressLabel" class="vision-terminal-feed__line">Filled: {{ quest.presentation.slotProgressLabel }}</p>
      <p v-if="quest.presentation.remainingSlotsLabel" class="vision-terminal-feed__line">Open spots: {{ quest.presentation.remainingSlotsLabel }}</p>
      <p class="vision-terminal-feed__line">Time: {{ quest.presentation.timeTypeLabel }}</p>
      <p v-if="approvedApplicants.length" class="vision-terminal-feed__line">
        Approved workers: {{ approvedApplicants.map((application) => application.applicantUsername).join(", ") }}
      </p>
    </section>

    <section v-if="showMyApplication && myApplication" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">your application</p>
      <p class="vision-terminal-feed__line">{{ myApplication.presentation.statusLabel }}</p>
      <p v-if="myApplication.proposedPrice !== null && myApplication.proposedPrice !== undefined" class="vision-terminal-feed__line">
        {{ formatApplicationPrice(myApplication.proposedPrice) }}
      </p>
      <p v-if="myApplication.message" class="vision-terminal-feed__line">{{ myApplication.message }}</p>
      <button v-if="canOpenApplication" class="vision-terminal-feed__link-button" type="button" @click="emit('open-application')">
        {{ applicationOpenLabel }}
      </button>
    </section>

    <section v-if="termChangeSection?.visible" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">term change</p>
      <button class="vision-terminal-feed__link-button" type="button" @click="emit('toggle-term-change')">
        {{ termChangeSection.summaryLabel }}
      </button>
      <p v-if="showTermChangeDetails" class="vision-terminal-feed__line">
        Current term: {{ formatQuestTermForDisplay(termChangeSection.currentScheduledAt, termChangeSection.currentEndsAt, termChangeSection.currentTermFixed) }}
      </p>
      <p v-if="showTermChangeDetails" class="vision-terminal-feed__line">
        Pending term: {{
          termChangeSection.pendingScheduledAt
            ? formatQuestTermForDisplay(termChangeSection.pendingScheduledAt, termChangeSection.pendingEndsAt, termChangeSection.pendingTermFixed ?? termChangeSection.currentTermFixed)
            : "Not set"
        }}
      </p>
    </section>

    <section v-if="hasVisibleActions" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">actions</p>
      <button v-if="executionSection?.primaryAction" class="vision-terminal-feed__link-button" type="button" :disabled="isSaving" @click="executionSection.primaryAction === 'START' ? emit('start-work') : emit('complete-work')">
        {{ executionSection.primaryActionLabel }}
      </button>
      <button v-if="quest?.presentation.canManuallyAssign" class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('assign-now')">
        Assign now
      </button>
      <button v-if="termChangeSection?.actionable" class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('confirm-term-change')">
        {{ termChangeSection.confirmLabel }}
      </button>
      <button v-if="termChangeSection?.actionable" class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('reject-term-change')">
        {{ termChangeSection.rejectLabel }}
      </button>
      <button v-if="managementSection?.deleteVisible" class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('delete-quest')">
        Delete
      </button>
    </section>

    <section v-if="reviewSection?.visible" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">review</p>
      <p v-if="hasSubmittedReview" class="vision-terminal-feed__line">Saved</p>
      <p v-if="reviewSection.canSubmit" class="vision-terminal-feed__line">{{ reviewSection.introTitle }}</p>
      <div v-if="reviewSection.canSubmit" class="vision-terminal-feed__action-row">
        <button v-for="stars in 5" :key="stars" class="vision-terminal-feed__link-button" type="button" @click="emit('select-review-stars', stars)">
          {{ stars }}★
        </button>
      </div>
      <textarea
        v-if="reviewSection.canSubmit && reviewStars > 0"
        :value="reviewComment"
        class="input vision-terminal-feed__textarea"
        maxlength="500"
        :placeholder="reviewSection.placeholder"
        @input="emit('update:reviewComment', ($event.target as HTMLTextAreaElement).value)"
      />
      <div v-if="reviewSection.canSubmit && reviewStars > 0" class="vision-terminal-feed__action-row">
        <button class="vision-terminal-feed__link-button" type="button" :disabled="isSaving || isActionInProgress" @click="emit('submit-review')">
          {{ reviewSection.submitLabel }}
        </button>
        <span class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ reviewComment.length }}/500</span>
      </div>
      <p v-else class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ reviewSection.emptyStateMessage }}</p>
    </section>

    <slot />
  </aside>
</template>
