<script setup lang="ts">
import {computed} from "vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {Quest} from "../../api/workmarketApi.ts"

const props = defineProps<{
  quest: Quest
  showTitle: boolean
}>()

const questTypeLabel = computed(() => props.quest.termFixed ? "Fixed" : "Negotiable")
const detailTermLabel = computed(() => props.quest.presentation.termScheduleLabel)
</script>

<template>
  <article class="card ui-detail-panel ui-detail-panel--primary ui-detail-panel--hero">
    <div class="surface-hero">
      <div class="surface-hero__copy">
        <div class="surface-hero__summary">
          <div class="surface-price-pill surface-price-pill--hero">
            <span class="surface-price-pill__label">Reward</span>
            <strong class="surface-price-pill__amount">$ {{ quest.awardAmount }}</strong>
          </div>

          <div class="surface-term-block">
            <span class="surface-term-block__label">When</span>
            <strong class="surface-term-block__value">{{ detailTermLabel }}</strong>
            <span class="surface-term-block__type">{{ questTypeLabel }}</span>
          </div>
        </div>

        <h2 v-if="showTitle" class="card__title surface-hero__title">{{ quest.title }}</h2>

        <ProfileBio
          v-if="richTextHasContent(quest.description)"
          class="ui-content-prose ui-content-prose--panel ui-copy-prose ui-copy-prose--hero"
          :text="quest.description"
        />
      </div>
    </div>
  </article>
</template>
