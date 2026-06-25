<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import QuestDetailAsidePanels from "./QuestDetailAsidePanels.vue"
import QuestDetailHero from "./QuestDetailHero.vue"
import DetailDialogFrame from "./DetailDialogFrame.vue"
import type {Quest, QuestApplication, QuestDetail} from "../../api/workmarketApi.ts"

const props = withDefaults(defineProps<{
  quest: Quest
  myApplication?: QuestApplication | null
  showTitle?: boolean
  showOverview?: boolean
  showMyApplication?: boolean
  mainSectionEyebrow?: string
  mainSectionTitle?: string
  mainSectionSubtitle?: string
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
  showOverview: true,
  showMyApplication: true,
  mainSectionEyebrow: "Quest",
  mainSectionTitle: "What they need",
  mainSectionSubtitle: "",
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
const isPreviewOpen = computed(() => previewIndex.value !== null && !!previewImage.value)

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

const handlePreviewKeydown = (event: KeyboardEvent) => {
  if (!isPreviewOpen.value) {
    return
  }

  if (event.key === "Escape") {
    closePreview()
    return
  }

  if (event.key === "ArrowLeft") {
    showPreviousImage()
    return
  }

  if (event.key === "ArrowRight") {
    showNextImage()
  }
}

onMounted(() => {
  window.addEventListener("keydown", handlePreviewKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener("keydown", handlePreviewKeydown)
})
</script>

<template>
  <DetailDialogFrame>
    <template #main>
      <QuestDetailHero :quest="quest" :show-title="showTitle" />

      <section v-if="quest.images?.length" class="quest-detail-gallery">
        <div class="quest-gallery quest-gallery--dialog">
          <button
            v-for="(image, index) in quest.images"
            :key="`${quest.id}-${index}`"
            class="ui-media-tile ui-media-tile--dialog quest-gallery__preview-trigger"
            type="button"
            :aria-label="`Open photo ${index + 1}`"
            @click.stop="openPreview(index)"
          >
            <img class="quest-gallery__image" :src="image" :alt="`Quest photo ${index + 1}`">
            <span v-if="hasMultipleImages" class="quest-gallery__badge">{{ index + 1 }} / {{ quest.images.length }}</span>
          </button>
        </div>
      </section>

      <slot name="main-after" />
    </template>

    <template #side>
      <QuestDetailAsidePanels
      :quest="quest"
      :my-application="myApplication"
      :show-overview="showOverview"
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
      @delete-quest="emit('delete-quest')"
      @confirm-term-change="emit('confirm-term-change')"
      @reject-term-change="emit('reject-term-change')"
      >
        <slot name="side-after" />
      </QuestDetailAsidePanels>
    </template>

    <Teleport to="body">
      <div v-if="previewImage" class="quest-gallery__preview-backdrop" @click.self="closePreview">
        <div class="quest-gallery__preview-panel">
          <div class="quest-gallery__preview-toolbar">
            <span class="quest-gallery__preview-count">{{ previewLabel }}</span>
            <button class="button button--ghost" type="button" @click="closePreview">Close</button>
          </div>

          <div class="quest-gallery__preview-dialog">
            <button
              v-if="hasMultipleImages"
              class="button button--ghost quest-gallery__preview-nav"
              type="button"
              aria-label="Previous image"
              @click.stop="showPreviousImage"
            >
              Previous
            </button>

            <div class="quest-gallery__preview-frame">
              <img class="quest-gallery__preview-image" :src="previewImage" :alt="previewLabel">
            </div>

            <button
              v-if="hasMultipleImages"
              class="button button--ghost quest-gallery__preview-nav"
              type="button"
              aria-label="Next image"
              @click.stop="showNextImage"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </DetailDialogFrame>
</template>
