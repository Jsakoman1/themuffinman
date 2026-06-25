<script setup lang="ts">
import {computed, ref} from "vue"
import QuestDetailAsidePanels from "./QuestDetailAsidePanels.vue"
import QuestDetailHero from "./QuestDetailHero.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import UiWorkspace from "../../../../components/ui/UiWorkspace.vue"
import type {Quest, QuestApplication, QuestDetail} from "../../api/workmarketApi.ts"

const props = withDefaults(defineProps<{
  quest: Quest
  myApplication?: QuestApplication | null
  showTitle?: boolean
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
  myApplication: null,
  showTitle: true,
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

const previewIndex = ref<number | null>(null)

const previewImage = computed(() => {
  if (previewIndex.value === null) {
    return null
  }

  return props.quest.images?.[previewIndex.value] ?? null
})

const previewLabel = computed(() => {
  if (previewIndex.value === null || !props.quest.images?.length) {
    return "Photo preview"
  }

  return `Photo ${previewIndex.value + 1} of ${props.quest.images.length}`
})

const hasMultipleImages = computed(() => (props.quest.images?.length ?? 0) > 1)

const openPreview = (index: number) => {
  previewIndex.value = index
}

const closePreview = () => {
  previewIndex.value = null
}

const showPreviousImage = () => {
  if (previewIndex.value === null || !props.quest.images?.length) {
    return
  }

  previewIndex.value = (previewIndex.value + props.quest.images.length - 1) % props.quest.images.length
}

const showNextImage = () => {
  if (previewIndex.value === null || !props.quest.images?.length) {
    return
  }

  previewIndex.value = (previewIndex.value + 1) % props.quest.images.length
}
</script>

<template>
  <UiWorkspace variant="detail">
    <section class="surface-stack surface-stack--content">
      <QuestDetailHero :quest="quest" :show-title="showTitle" />

      <UiSurfaceSection v-if="quest.images?.length" tag="article" class="card ui-detail-panel ui-detail-panel--gallery" compact title="Photos">
        <template v-if="hasMultipleImages" #actions>
          <span class="quest-gallery__hint">{{ quest.images.length }} photos · swipe or scroll</span>
        </template>

        <div class="quest-gallery quest-gallery--dialog">
          <button
            v-for="(image, index) in quest.images"
            :key="`${quest.id}-${index}`"
            class="ui-media-tile ui-media-tile--dialog quest-gallery__preview-trigger"
            type="button"
            :aria-label="`Open photo ${index + 1}`"
            @click="openPreview(index)"
          >
            <img class="quest-gallery__image" :src="image" :alt="`Quest photo ${index + 1}`">
            <span v-if="hasMultipleImages" class="quest-gallery__badge">{{ index + 1 }} / {{ quest.images.length }}</span>
          </button>
        </div>
      </UiSurfaceSection>

      <slot name="main-after" />
    </section>

    <QuestDetailAsidePanels
      :quest="quest"
      :my-application="myApplication"
      :show-my-application="showMyApplication"
      :can-open-application="canOpenApplication"
      :application-open-label="applicationOpenLabel"
      :show-term-change-details="showTermChangeDetails"
      :execution-section="executionSection"
      :term-change-section="termChangeSection"
      :management-section="managementSection"
      :review-section="reviewSection"
      :is-saving="isSaving"
      :is-action-in-progress="isActionInProgress"
      :has-submitted-review="hasSubmittedReview"
      :review-stars="reviewStars"
      :review-comment="reviewComment"
      @toggle-term-change="emit('toggle-term-change')"
      @open-application="emit('open-application')"
      @select-review-stars="emit('select-review-stars', $event)"
      @update:review-comment="emit('update:reviewComment', $event)"
      @submit-review="emit('submit-review')"
      @start-work="emit('start-work')"
      @complete-work="emit('complete-work')"
      @edit-quest="emit('edit-quest')"
      @delete-quest="emit('delete-quest')"
      @confirm-term-change="emit('confirm-term-change')"
      @reject-term-change="emit('reject-term-change')"
    >
      <slot name="side-after" />
    </QuestDetailAsidePanels>

    <UiDialog :open="!!previewImage" :title="previewLabel" size="lg" @close="closePreview">
      <div v-if="previewImage" class="quest-gallery__preview-dialog">
        <div class="quest-gallery__preview-frame">
          <img class="quest-gallery__preview-image" :src="previewImage" :alt="previewLabel">
        </div>

        <div v-if="hasMultipleImages" class="quest-gallery__preview-actions">
          <button class="button button--ghost" type="button" @click="showPreviousImage">Previous</button>
          <span class="quest-gallery__preview-count">{{ previewLabel }}</span>
          <button class="button button--ghost" type="button" @click="showNextImage">Next</button>
        </div>
      </div>
    </UiDialog>
  </UiWorkspace>
</template>
