<script setup lang="ts">
import {computed} from "vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiFieldGroup from "../../../../components/ui/UiFieldGroup.vue"
import UiStarRatingInput from "../../../../components/ui/UiStarRatingInput.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {Quest, QuestApplication, QuestDetail} from "../../api/workmarketApi.ts"

const props = withDefaults(defineProps<{
  quest?: Quest | null
  myApplication?: QuestApplication | null
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

const visibleCirclesLabel = computed(() => {
  if (!props.quest || props.quest.audience !== "CIRCLES") {
    return ""
  }

  const names = props.quest.visibleToCircles.map((circle) => circle.name)
  return names.length ? names.join(", ") : "Selected circles"
})

const hasVisibleActions = computed(() => {
  return Boolean(
    props.managementSection?.editVisible ||
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

const emit = defineEmits<{
  (event: "toggle-term-change"): void
  (event: "open-application"): void
  (event: "select-review-stars", stars: number): void
  (event: "update:reviewComment", value: string): void
  (event: "submit-review"): void
  (event: "start-work"): void
  (event: "complete-work"): void
  (event: "edit-quest"): void
  (event: "delete-quest"): void
  (event: "confirm-term-change"): void
  (event: "reject-term-change"): void
}>()
</script>

<template>
  <aside class="surface-stack surface-stack--aside">
    <UiSurfaceSection v-if="quest" tag="article" class="card ui-detail-panel ui-detail-panel--aside ui-detail-panel--summary" compact title="Overview">
      <div class="quest-overview-aside">
        <div class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Status</span>
          <span :class="quest.presentation.statusBadgeClass">
            {{ quest.presentation.statusLabel }}
          </span>
        </div>

        <div class="quest-overview-aside__row">
          <span class="quest-overview-aside__label">Visibility</span>
          <span class="quest-overview-aside__value">{{ quest.presentation.audienceLabel }}</span>
        </div>

        <div v-if="quest.audience === 'CIRCLES'" class="quest-overview-aside__row quest-overview-aside__row--stack">
          <span class="quest-overview-aside__label">Visible to circles</span>
          <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ visibleCirclesLabel }}</span>
        </div>
      </div>
    </UiSurfaceSection>

    <UiSurfaceSection v-if="showMyApplication && myApplication" tag="article" class="card ui-detail-panel ui-detail-panel--aside" compact title="Your application">
      <template #actions>
        <span :class="myApplication.presentation.statusBadgeClass">
          {{ myApplication.presentation.statusLabel }}
        </span>
      </template>

      <div class="surface-price">$ {{ myApplication.proposedPrice }}</div>

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
    </UiSurfaceSection>

    <UiSurfaceSection v-if="termChangeSection?.visible" tag="article" class="card ui-detail-panel ui-detail-panel--aside" compact title="Term change">
      <div class="compact-disclosure">
        <button class="compact-disclosure--launch" type="button" @click="emit('toggle-term-change')">
          {{ termChangeSection.summaryLabel }}
        </button>
        <div v-if="showTermChangeDetails" class="alert alert--warning mt-2">
          <div class="stack">
            <div class="muted">Current term: {{ termChangeSection.currentTermLabel }}</div>
            <div class="muted">Pending term: {{ termChangeSection.pendingTermLabel }}</div>
          </div>
        </div>
      </div>
    </UiSurfaceSection>

    <UiSurfaceSection v-if="hasVisibleActions" tag="article" class="card ui-detail-panel ui-detail-panel--aside ui-detail-panel--muted" compact title="Actions">
      <div class="ui-action-stack">
        <button v-if="managementSection?.editVisible" class="button button--secondary" type="button" :disabled="isSaving || isActionInProgress" @click="emit('edit-quest')">
          Edit
        </button>
        <button v-if="executionSection?.primaryAction" class="button" type="button" :disabled="isSaving" @click="executionSection.primaryAction === 'START' ? emit('start-work') : emit('complete-work')">
          {{ executionSection.primaryActionLabel }}
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
    </UiSurfaceSection>

    <UiSurfaceSection v-if="reviewSection?.visible" tag="section" class="card ui-detail-panel ui-detail-panel--aside ui-detail-panel--review" compact title="Review">
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
    </UiSurfaceSection>

    <slot />
  </aside>
</template>
