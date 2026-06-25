<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import UiInfoGrid from "../../../../components/ui/UiInfoGrid.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import {richTextHasContent} from "../../../../shared/richText.ts"
import type {QuestApplication} from "../../api/workmarketApi.ts"

withDefaults(defineProps<{
  application: QuestApplication
  messageEyebrow?: string
  contextEyebrow?: string
  questPath?: RouteLocationRaw | null
  questLabel?: string
  postedByLabel?: string
  showStatus?: boolean
  showTerm?: boolean
  showWorkers?: boolean
}>(), {
  messageEyebrow: "Message",
  contextEyebrow: "Context",
  questPath: null,
  questLabel: "",
  postedByLabel: "",
  showStatus: false,
  showTerm: true,
  showWorkers: false,
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
      <div v-if="questPath && questLabel" class="field">
        <span class="label">Quest</span>
        <RouterLink class="profile-link profile-link--text" :to="questPath">
          {{ questLabel }}
        </RouterLink>
      </div>

      <div v-if="postedByLabel" class="field">
        <span class="label">Posted by</span>
        <button class="ui-inline-link" type="button" @click="$emit('open-posted-by')">
          {{ postedByLabel }}
        </button>
      </div>

      <div v-if="showStatus" class="field">
        <span class="label">Status</span>
        <strong>{{ application.presentation.statusLabel }}</strong>
      </div>

      <div v-if="showTerm" class="field">
        <span class="label">Term</span>
        <strong>{{ application.presentation.questTermLabel }}</strong>
      </div>

      <div v-if="showWorkers && application.presentation.questAssigneeTargetVisible" class="field">
        <span class="label">Workers</span>
        <strong>{{ application.presentation.questAssigneeTargetLabel }}</strong>
      </div>
    </UiInfoGrid>
  </UiSurfaceSection>
</template>
