<script setup lang="ts">
import DashboardQuestSummaryRow from "../dashboard/DashboardQuestSummaryRow.vue"

withDefaults(defineProps<{
  pulse?: boolean
  statusSurfaceClass?: string
  primaryLabel: string
  primaryValue: string | number
  title: string
  description?: string
  primaryIcon?: string
  secondaryLabel?: string
  secondaryValue?: string | number | null
  secondaryIcon?: string
  moneyTone?: "income" | "expense" | "neutral"
  compactInline?: boolean
}>(), {
  pulse: false,
  statusSurfaceClass: "",
  description: "",
  primaryIcon: "",
  secondaryLabel: "",
  secondaryValue: null,
  secondaryIcon: "",
  moneyTone: "neutral",
  compactInline: false,
})

const emit = defineEmits<{
  (event: "click"): void
}>()
</script>

<template>
  <button
    type="button"
    class="compact-disclosure compact-disclosure--tight compact-disclosure--launch"
    :class="[statusSurfaceClass, { 'ui-pulse': pulse }]"
    @click="emit('click')"
  >
    <DashboardQuestSummaryRow
      :primary-label="primaryLabel"
      :primary-value="primaryValue"
      :primary-icon="primaryIcon"
      :secondary-label="secondaryLabel"
      :secondary-value="secondaryValue"
      :secondary-icon="secondaryIcon"
      :money-tone="moneyTone"
      :title="title"
      :description="description"
      :compact-inline="compactInline"
    >
      <template v-if="$slots.meta" #meta>
        <slot name="meta" />
      </template>
    </DashboardQuestSummaryRow>
  </button>
</template>
