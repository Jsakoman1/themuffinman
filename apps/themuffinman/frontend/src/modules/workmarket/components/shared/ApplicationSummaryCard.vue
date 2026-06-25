<script setup lang="ts">
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
  <section class="quest-overview-panel">
    <div class="quest-overview-aside">
      <div class="surface-inline-spread">
        <span class="surface-kicker">{{ kicker }}</span>
        <span :class="application.presentation.statusBadgeClass">
          {{ application.presentation.statusLabel }}
        </span>
      </div>

      <div class="surface-price-pill surface-price-pill--hero quest-overview-aside__reward">
        <span class="surface-price-pill__label">Proposed price</span>
        <span class="surface-price-pill__amount">$ {{ application.proposedPrice }}</span>
      </div>

      <div class="quest-overview-aside__row quest-overview-aside__row--stack">
        <span class="quest-overview-aside__label">Quest</span>
        <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ application.questTitle }}</span>
      </div>

      <div class="quest-overview-aside__row">
        <span class="quest-overview-aside__label">Quest status</span>
        <span class="quest-overview-aside__value">{{ application.presentation.questStatusLabel }}</span>
      </div>

      <div v-if="includeTerm" class="quest-overview-aside__row quest-overview-aside__row--stack">
        <span class="quest-overview-aside__label">Time</span>
        <span class="quest-overview-aside__value quest-overview-aside__value--multiline">{{ application.presentation.questTermLabel }}</span>
      </div>
    </div>
  </section>
</template>
