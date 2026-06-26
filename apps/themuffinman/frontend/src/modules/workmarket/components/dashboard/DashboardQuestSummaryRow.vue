<script setup lang="ts">
import {computed} from "vue"
import {richTextToPlainText} from "../../../../shared/richText.ts"

const props = withDefaults(defineProps<{
  primaryLabel: string
  primaryValue: string | number
  title: string
  description?: string
  primaryIcon?: string
  secondaryLabel?: string
  secondaryValue?: string | number | null
  secondaryIcon?: string
  moneyTone?: "income" | "expense" | "neutral"
  descriptionClass?: string
  showLabels?: boolean
  reserveSecondarySpace?: boolean
  reserveDescriptionSpace?: boolean
  compactInline?: boolean
}>(), {
  primaryIcon: "",
  secondaryLabel: "",
  secondaryValue: null,
  secondaryIcon: "",
  moneyTone: "neutral",
  descriptionClass: "text-clamp",
  showLabels: false,
  reserveSecondarySpace: false,
  reserveDescriptionSpace: false,
  compactInline: false,
})

const descriptionText = computed(() => richTextToPlainText(props.description ?? ""))
</script>

<template>
  <div :class="['quest-summary-row', { 'quest-summary-row--compact-inline': compactInline }]">
    <div class="quest-summary-row__leading">
      <div class="quest-summary-row__pill quest-summary-row__pill--primary">
        <span
          v-if="primaryIcon"
          :class="['quest-summary-row__symbol', `quest-summary-row__symbol--${moneyTone}`]"
          aria-hidden="true"
        >
          {{ primaryIcon }}
        </span>
        <div class="quest-summary-row__copy">
          <span v-if="showLabels" class="label">{{ primaryLabel }}</span>
          <strong>{{ primaryValue }}</strong>
        </div>
      </div>

      <div
        v-if="secondaryValue !== null && secondaryValue !== '' || reserveSecondarySpace"
        :class="[
          'quest-summary-row__pill',
          'quest-summary-row__pill--secondary',
          { 'quest-summary-row__pill--placeholder': secondaryValue === null || secondaryValue === '' },
        ]"
      >
        <div class="quest-summary-row__copy">
          <span v-if="showLabels" class="label">{{ secondaryLabel }}</span>
          <strong>
            <span
              v-if="secondaryIcon && secondaryValue !== null && secondaryValue !== ''"
              :class="['quest-summary-row__currency', `quest-summary-row__currency--${moneyTone}`]"
              aria-hidden="true"
            >
              {{ secondaryIcon }}
            </span>{{ secondaryValue ?? "\u00A0" }}
          </strong>
        </div>
      </div>
    </div>

    <div class="quest-summary-row__main">
      <strong class="quest-summary-row__title">{{ title }}</strong>
      <div
        v-if="descriptionText || reserveDescriptionSpace"
        :class="[
          'muted',
          'mt-1',
          descriptionClass,
          { 'quest-summary-row__description--placeholder': !descriptionText },
        ]"
      >
        {{ descriptionText || "\u00A0" }}
      </div>
    </div>

    <div class="quest-summary-row__meta">
      <slot name="meta" />
    </div>
  </div>
</template>
