<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"
import type {QuestApplication, QuestApplicationDetail} from "../../api/visionApi.ts"

withDefaults(defineProps<{
  application: QuestApplication
  messageEyebrow?: string
  contextEyebrow?: string
  navigationSection?: QuestApplicationDetail["sections"]["navigation"] | null
  contextSection?: QuestApplicationDetail["sections"]["context"] | null
  questPath?: RouteLocationRaw | null
}>(), {
  messageEyebrow: "Message",
  contextEyebrow: "Context",
  navigationSection: null,
  contextSection: null,
  questPath: null,
})

defineEmits<{
  (event: "open-posted-by"): void
}>()
</script>

<template>
  <section class="vision-terminal-feed__block">
    <p class="vision-terminal-feed__block-title">{{ messageEyebrow }}</p>
    <p class="vision-terminal-feed__line">{{ application.message || "No message." }}</p>
  </section>

  <section class="vision-terminal-feed__block">
    <p class="vision-terminal-feed__block-title">{{ contextEyebrow }}</p>
    <p v-if="questPath && contextSection?.questLabel" class="vision-terminal-feed__line">
      Quest: <RouterLink class="vision-terminal-feed__link" :to="questPath">{{ contextSection.questLabel }}</RouterLink>
    </p>
    <p v-if="contextSection?.postedByLabel" class="vision-terminal-feed__line">
      Posted by:
      <button class="vision-terminal-feed__link-button" type="button" :disabled="!navigationSection?.canOpenPostedBy" @click="$emit('open-posted-by')">
        {{ contextSection.postedByLabel }}
      </button>
    </p>
    <p v-if="contextSection?.showStatus" class="vision-terminal-feed__line">Status: {{ application.presentation.statusLabel }}</p>
    <p v-if="contextSection?.showTerm" class="vision-terminal-feed__line">
      Term: {{ formatQuestTermForDisplay(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}
    </p>
    <p v-if="contextSection?.showWorkers && application.presentation.questAssigneeTargetVisible" class="vision-terminal-feed__line">
      Workers: {{ application.presentation.questAssigneeTargetLabel }}
    </p>
  </section>
</template>
