<script setup lang="ts">
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {Quest} from "../../api/visionApi.ts"

defineProps<{
  quest: Quest
  showTitle: boolean
}>()
</script>

<template>
  <article class="ui-detail-panel ui-detail-panel--hero ui-detail-panel--hero-plain">
    <div class="surface-hero">
      <div class="surface-hero__copy">
        <h2 v-if="showTitle" class="card__title surface-hero__title">{{ quest.title }}</h2>

        <ProfileBio
          v-if="richTextHasContent(quest.description)"
          class="ui-content-prose ui-content-prose--flat ui-copy-prose ui-copy-prose--hero"
          :text="quest.description"
        />
      </div>
    </div>
  </article>
</template>
