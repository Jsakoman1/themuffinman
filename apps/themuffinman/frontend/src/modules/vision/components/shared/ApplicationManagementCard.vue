<script setup lang="ts">
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
  <article class="vision-terminal-feed__block" :class="{ 'vision-terminal-feed__list-button--active': selected }">
    <button class="vision-terminal-feed__link-button" type="button" @click="$emit('open-applicant')">
      {{ application.applicantUsername }}
    </button>
    <p v-if="showStatus" class="vision-terminal-feed__line">{{ application.presentation.statusLabel }}</p>
    <p v-if="application.proposedPrice !== null && application.proposedPrice !== undefined" class="vision-terminal-feed__line">
      {{ formatApplicationPrice(application.proposedPrice) }}
    </p>
    <p v-if="application.message" class="vision-terminal-feed__line">{{ application.message }}</p>
    <div v-if="$slots.actions" class="vision-terminal-feed__action-row">
      <slot name="actions" />
    </div>
  </article>
</template>
