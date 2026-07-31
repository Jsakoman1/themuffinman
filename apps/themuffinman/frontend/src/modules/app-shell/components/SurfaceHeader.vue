<script setup lang="ts">
import {computed} from "vue"
import type {AppSurfaceConfig} from "../shellDefinitions.ts"
import PageOrientationHeader from "./PageOrientationHeader.vue"

const props = defineProps<{config: AppSurfaceConfig; detailLabel?: string; title?: string; description?: string}>()
const primaryAction = computed(() => props.config.actions.find(action => action.tone === "primary") ?? props.config.actions[0])
const secondaryActions = computed(() => props.config.actions.filter(action => action !== primaryAction.value))
</script>

<template>
  <PageOrientationHeader
    :eyebrow="props.config.eyebrow"
    :title="props.title ?? props.config.title"
    :description="props.description"
    :detail-label="props.detailLabel"
    :primary-action="primaryAction"
    :secondary-actions="secondaryActions"
  >
    <template #utility><slot name="utility" /></template>
  </PageOrientationHeader>
</template>
