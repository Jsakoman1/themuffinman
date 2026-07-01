<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {invalidEntityMessage} from "../../../shared/clientMessages.ts"
import {workmarketApi, type QuestApplication, type QuestApplicationDetail} from "../api/workmarketApi.ts"
import DetailDialogFrame from "../components/shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../components/shared/DetailUtilitySection.vue"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import ApplicationDetailSections from "../components/shared/ApplicationDetailSections.vue"
import ApplicationSummaryCard from "../components/shared/ApplicationSummaryCard.vue"

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
    detail.value = await workmarketApi.getApplicationDetail(applicationId.value)
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
  <UiAppShellPage>
    <UiDialog
      :open="true"
      :title="application?.questTitle ?? 'Application details'"
      size="xl"
      @close="closeApplicationDetail"
    >
      <UiRequestError :message="error" :details="[]" summary="Debug details" :copied="false" />

      <div v-if="isLoading" class="empty-state">Loading application...</div>

      <div v-else-if="application" class="surface-stack">
        <DetailDialogFrame>
          <template #main>
            <ApplicationDetailSections
              :application="application"
              :navigation-section="navigationSection"
              :context-section="contextSection"
              :quest-path="questPath"
              @open-posted-by="openPostedBy"
            />
          </template>

          <template #side>
            <ApplicationSummaryCard :application="application" include-term />

            <DetailUtilitySection v-if="navigationSection?.canOpenQuest && questPath" title="Actions" tone="actions">
              <div class="ui-action-stack">
                <RouterLink class="button button--secondary" :to="questPath">Open quest</RouterLink>
              </div>
            </DetailUtilitySection>
          </template>
        </DetailDialogFrame>
      </div>
    </UiDialog>
  </UiAppShellPage>
</template>
