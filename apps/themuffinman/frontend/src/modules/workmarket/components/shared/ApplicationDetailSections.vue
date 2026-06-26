<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiInfoGrid from "../../../../components/ui/UiInfoGrid.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {QuestApplication, QuestApplicationDetail} from "../../api/workmarketApi.ts"
import {formatQuestTermForDisplay} from "../../../../shared/questSchedule.ts"

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
  <UiSurfaceSection compact :eyebrow="messageEyebrow">
    <ProfileBio
      v-if="richTextHasContent(application.message)"
      class="ui-content-prose ui-content-prose--flat"
      :text="application.message"
    />
  </UiSurfaceSection>

  <UiSurfaceSection compact :eyebrow="contextEyebrow">
    <UiInfoGrid :columns="2">
      <div v-if="questPath && contextSection?.questLabel" class="field">
        <span class="label">Quest</span>
        <RouterLink class="profile-link profile-link--text" :to="questPath">
          {{ contextSection?.questLabel }}
        </RouterLink>
      </div>

      <div v-if="contextSection?.postedByLabel" class="field">
        <span class="label">Posted by</span>
        <button
          class="ui-inline-link"
          type="button"
          :disabled="!navigationSection?.canOpenPostedBy"
          @click="$emit('open-posted-by')"
        >
          {{ contextSection.postedByLabel }}
        </button>
      </div>

      <div v-if="contextSection?.showStatus" class="field">
        <span class="label">Status</span>
        <strong>{{ application.presentation.statusLabel }}</strong>
      </div>

      <div v-if="contextSection?.showTerm" class="field">
        <span class="label">Term</span>
        <strong>{{ formatQuestTermForDisplay(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}</strong>
      </div>

      <div v-if="contextSection?.showWorkers && application.presentation.questAssigneeTargetVisible" class="field">
        <span class="label">Workers</span>
        <strong>{{ application.presentation.questAssigneeTargetLabel }}</strong>
      </div>
    </UiInfoGrid>
  </UiSurfaceSection>
</template>
