<script setup lang="ts">
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {QuestApplication} from "../../api/visionApi.ts"
import {formatApplicationPrice} from "../../shared/pricing.ts"

withDefaults(defineProps<{
  application: QuestApplication
  selected?: boolean
  showStatus?: boolean
}>(), {
  selected: false,
  showStatus: true,
})

defineEmits<{
  (event: "open-applicant"): void
}>()
</script>

<template>
  <UiSurfaceSection :class="{ 'ui-selection-surface': selected }" compact>
    <div class="surface-inline-spread">
      <button class="ui-inline-link" type="button" @click="$emit('open-applicant')">
        {{ application.applicantUsername }}
      </button>
      <span v-if="showStatus" :class="application.presentation.statusBadgeClass">
        {{ application.presentation.statusLabel }}
      </span>
    </div>

    <div v-if="application.proposedPrice !== null && application.proposedPrice !== undefined" class="surface-price">
      {{ formatApplicationPrice(application.proposedPrice) }}
    </div>

    <ProfileBio
      v-if="richTextHasContent(application.message)"
      class="ui-copy-compact"
      :text="application.message"
    />

    <div v-if="$slots.actions" class="surface-actions">
      <slot name="actions" />
    </div>
  </UiSurfaceSection>
</template>
