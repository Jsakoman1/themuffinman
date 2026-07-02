<script setup lang="ts">
import {computed} from "vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import {renderProfileText} from "../../../../shared/profileFormatting.ts"
import type {Quest} from "../../api/visionApi.ts"

const props = defineProps<{
  quest: Quest
  showTitle: boolean
}>()

const renderedDescription = computed(() => renderProfileText(props.quest.description))
</script>

<template>
  <article class="ui-detail-panel ui-detail-panel--hero ui-detail-panel--hero-plain">
    <div class="surface-hero">
      <div class="surface-hero__copy">
        <h2 v-if="showTitle" class="card__title surface-hero__title">{{ quest.title }}</h2>

        <div
          v-if="richTextHasContent(props.quest.description)"
          class="profile-bio ui-content-prose ui-content-prose--flat ui-copy-prose ui-copy-prose--hero"
          v-html="renderedDescription"
        />
      </div>
    </div>
  </article>
</template>
