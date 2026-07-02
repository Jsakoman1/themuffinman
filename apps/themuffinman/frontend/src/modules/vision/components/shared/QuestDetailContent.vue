<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue"
import type {Quest, QuestApplication, QuestDetail} from "../../api/visionApi.ts"

const props = withDefaults(defineProps<{
  quest: Quest
  myApplication?: QuestApplication | null
  applicationsView?: QuestDetail["applicationsView"] | null
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
  applicationsView: null,
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
  (event: "assign-now"): void
  (event: "confirm-term-change"): void
  (event: "reject-term-change"): void
}>()

const previewIndex = ref<number | null>(null)
const previewImage = computed(() => previewIndex.value === null ? null : props.quest.images?.[previewIndex.value] ?? null)
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
  <section class="vision-terminal-feed">
    <section v-if="showTitle" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">{{ mainSectionEyebrow }}</p>
      <p class="vision-terminal-feed__line">{{ quest.title || "Untitled" }}</p>
      <p v-if="mainSectionSubtitle" class="vision-terminal-feed__line vision-terminal-feed__line--soft">{{ mainSectionSubtitle }}</p>
    </section>

    <section v-if="quest.description" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">description</p>
      <p class="vision-terminal-feed__line">{{ quest.description }}</p>
    </section>

    <section v-if="quest.images?.length" class="vision-terminal-feed__block">
      <p class="vision-terminal-feed__block-title">images</p>
      <div class="vision-terminal-feed__stack">
        <button
          v-for="(image, index) in quest.images"
          :key="`${quest.id}-${index}`"
          type="button"
          class="vision-terminal-feed__list-button"
          @click.stop="openPreview(index)"
        >
          <span>Photo {{ index + 1 }} / {{ quest.images.length }}</span>
          <span>{{ image }}</span>
        </button>
      </div>
    </section>

    <slot name="main-after" />

    <slot name="side-after" />

    <Teleport to="body">
      <div v-if="previewImage" class="quest-gallery__preview-backdrop" @click.self="closePreview">
        <div class="quest-gallery__preview-panel">
          <div class="quest-gallery__preview-toolbar">
            <span class="quest-gallery__preview-count">Photo preview</span>
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
              <img class="quest-gallery__preview-image" :src="previewImage" alt="Quest image preview">
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
  </section>
</template>
