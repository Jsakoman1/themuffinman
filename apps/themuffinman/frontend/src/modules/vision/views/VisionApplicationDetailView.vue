<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {invalidEntityMessage} from "../../../shared/clientMessages.ts"
import {visionApi, type QuestApplication, type QuestApplicationDetail} from "../api/visionApi.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"

const route = useRoute()
const router = useRouter()
const application = ref<QuestApplication | null>(null)
const detail = ref<QuestApplicationDetail | null>(null)
const isLoading = ref(false)
const error = ref("")

const applicationId = computed(() => Number(route.params.id))
const navigationSection = computed(() => detail.value?.sections.navigation ?? null)
const contextSection = computed(() => detail.value?.sections.context ?? null)
const questPath = computed(() => routeForNavigationTarget(navigationSection.value?.questNavigation))
const postedByPath = computed(() => routeForNavigationTarget(navigationSection.value?.postedByNavigation))

const loadApplication = async () => {
  if (!Number.isFinite(applicationId.value)) {
    error.value = invalidEntityMessage("application")
    application.value = null
    detail.value = null
    return
  }

  isLoading.value = true
  error.value = ""

  try {
    detail.value = await visionApi.getApplicationDetail(applicationId.value)
    application.value = detail.value.summary
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load application.")
    application.value = null
    detail.value = null
  } finally {
    isLoading.value = false
  }
}

const closeApplicationDetail = async () => {
  if (questPath.value) {
    await router.push(questPath.value)
    return
  }

  if (window.history.length > 1) {
    await router.back()
    return
  }

  await router.push("/vision")
}

const openPostedBy = async () => {
  if (!postedByPath.value) {
    return
  }

  await router.push(postedByPath.value)
}

watch(() => route.params.id, () => {
  void loadApplication()
})

onMounted(() => {
  void loadApplication()
})
</script>

<template>
  <VisionDetailSurface
    :title="application?.questTitle ?? 'Application details'"
    @close="closeApplicationDetail"
  >
    <div class="vision-terminal-feed">
      <p class="vision-terminal-feed__line">> application</p>
      <p v-if="isLoading" class="vision-terminal-feed__line vision-terminal-feed__line--soft">Loading application...</p>
      <p v-else-if="error" class="vision-terminal-feed__line vision-terminal-feed__line--error">{{ error }}</p>

      <template v-else-if="application">
        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">message</p>
          <p v-if="application.message?.trim()" class="vision-terminal-feed__line">{{ application.message }}</p>
          <p v-else class="vision-terminal-feed__line vision-terminal-feed__line--soft">No message provided.</p>
        </section>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">context</p>
          <p v-if="questPath" class="vision-terminal-feed__line">
            Quest: <RouterLink class="vision-terminal-feed__link" :to="questPath">{{ contextSection?.questLabel ?? application.questTitle }}</RouterLink>
          </p>
          <p v-if="contextSection?.postedByLabel" class="vision-terminal-feed__line">
            Posted by:
            <button class="vision-terminal-feed__link-button" type="button" :disabled="!postedByPath" @click="openPostedBy">
              {{ contextSection.postedByLabel }}
            </button>
          </p>
          <p v-if="contextSection?.showStatus" class="vision-terminal-feed__line">Status: {{ application.presentation.statusLabel }}</p>
          <p v-if="contextSection?.showTerm" class="vision-terminal-feed__line">Term: {{ application.questScheduledAt }}{{ application.questEndsAt ? ` → ${application.questEndsAt}` : "" }}</p>
          <p v-if="contextSection?.showWorkers && application.presentation.questAssigneeTargetVisible" class="vision-terminal-feed__line">
            Workers: {{ application.presentation.questAssigneeTargetLabel }}
          </p>
        </section>

        <section class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">summary</p>
          <p class="vision-terminal-feed__line">Applicant: {{ application.applicantUsername }}</p>
          <p v-if="application.proposedPrice !== null && application.proposedPrice !== undefined" class="vision-terminal-feed__line">Price: {{ application.proposedPrice }}</p>
          <p class="vision-terminal-feed__line">Quest: {{ application.questTitle }}</p>
        </section>

        <section v-if="navigationSection?.canOpenQuest && questPath" class="vision-terminal-feed__block">
          <p class="vision-terminal-feed__block-title">actions</p>
          <RouterLink class="vision-terminal-feed__link-button" :to="questPath">Open quest</RouterLink>
        </section>
      </template>
    </div>
  </VisionDetailSurface>
</template>
