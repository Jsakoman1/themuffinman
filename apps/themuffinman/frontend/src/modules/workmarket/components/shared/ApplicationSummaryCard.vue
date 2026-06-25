<script setup lang="ts">
import UiMetricPills from "../../../../components/ui/UiMetricPills.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import type {QuestApplication} from "../../api/workmarketApi.ts"

withDefaults(defineProps<{
  application: QuestApplication
  kicker?: string
  includeTerm?: boolean
}>(), {
  kicker: "Application",
  includeTerm: false,
})
</script>

<template>
  <UiSurfaceSection soft>
    <div class="surface-inline-spread">
      <span :class="application.presentation.statusBadgeClass">
        {{ application.presentation.statusLabel }}
      </span>
      <span class="surface-kicker">{{ kicker }}</span>
    </div>

    <div class="surface-title">
      {{ application.questTitle }}
    </div>

    <UiMetricPills
      :items="[
        { label: 'Price', value: `$ ${application.proposedPrice}` },
        { label: 'Quest status', value: application.presentation.questStatusLabel },
        ...(includeTerm ? [{ label: 'Term', value: application.presentation.questTermLabel }] : []),
      ]"
    />
  </UiSurfaceSection>
</template>
