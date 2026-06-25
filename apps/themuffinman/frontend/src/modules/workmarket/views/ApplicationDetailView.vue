<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import UiDialog from "../../../components/ui/UiDialog.vue"
import UiRequestError from "../../../components/ui/UiRequestError.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {invalidEntityMessage} from "../../../shared/clientMessages.ts"
import {workmarketApi, type QuestApplication, type QuestApplicationDetail} from "../api/workmarketApi.ts"
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
const questPath = computed(() => routeForNavigationTarget(detail.value?.sections.navigation?.questNavigation))
const contextSection = computed(() => detail.value?.sections.context ?? null)

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

  await router.push("/work")
}

watch(() => route.params.id, () => {
  void loadApplication()
})

onMounted(() => {
  void loadApplication()
})
</script>

<template>
  <div class="page">
    <UiDialog
      :open="true"
      :title="application?.questTitle ?? 'Application details'"
      size="lg"
      :default-expanded="true"
      @close="closeApplicationDetail"
    >
      <UiRequestError :message="error" :details="[]" summary="Debug details" :copied="false" />

      <div v-if="isLoading" class="empty-state">Loading application...</div>

      <div v-else-if="application" class="surface-stack">
        <ApplicationSummaryCard :application="application" include-term />

        <ApplicationDetailSections
          :application="application"
          :quest-path="questPath"
          :quest-label="application.questTitle"
          :show-status="true"
          :show-workers="!!contextSection?.showWorkers"
        />

        <div class="button-row button-row--end">
          <RouterLink v-if="questPath" class="button button--secondary" :to="questPath">Open quest</RouterLink>
        </div>
      </div>
    </UiDialog>
  </div>
</template>
