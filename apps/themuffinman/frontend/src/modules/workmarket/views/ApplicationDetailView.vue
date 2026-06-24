<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import ProfileBio from "../../../components/profile/ProfileBio.vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {invalidEntityMessage} from "../../../shared/clientMessages.ts"
import {workmarketApi, type QuestApplication, type QuestApplicationDetail} from "../api/workmarketApi.ts"
import {richTextHasContent} from "../../../shared/richText.ts"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"

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

watch(() => route.params.id, () => {
  void loadApplication()
})

onMounted(() => {
  void loadApplication()
})
</script>

<template>
  <div class="page">
    <AppPageHeader title="Application details" subtitle="Review the application and jump back to the quest.">
      <template #actions>
        <button class="button button--secondary" type="button" @click="router.back()">Back</button>
        <RouterLink class="button button--secondary" :to="questPath">Open quest</RouterLink>
      </template>
    </AppPageHeader>

    <div v-if="isLoading" class="empty-state">Loading application...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>

    <div v-else-if="application" class="card">
      <div class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="application.presentation.statusBadgeClass">
            {{ application.presentation.statusLabel }}
          </span>
          <span class="dialog-focus-card__kicker">Application</span>
        </div>

        <div class="dialog-focus-card__title">
          {{ application.questTitle }}
        </div>

          <div class="dialog-focus-card__meta">
            <span>$ {{ application.proposedPrice }}</span>
            <span>Quest status: {{ application.presentation.questStatusLabel }}</span>
            <span>{{ application.presentation.questTermLabel }}</span>
          </div>
      </div>

      <section class="dialog-focus-card dialog-focus-card--soft mt-4">
        <div class="dialog-focus-card__section-title">Message</div>
        <ProfileBio
          v-if="richTextHasContent(application.message)"
          class="dialog-sheet__description dialog-sheet__description--flat"
          :text="application.message"
        />
      </section>

      <section class="dialog-focus-card dialog-focus-card--soft mt-4">
        <div class="dialog-focus-card__section-title">Context</div>
        <div class="dialog-focus-grid">
          <div class="field">
            <span class="label">Quest</span>
            <RouterLink class="profile-link profile-link--text" :to="questPath">
              {{ application.questTitle }}
            </RouterLink>
          </div>
          <div class="field">
            <span class="label">Status</span>
            <strong>{{ application.presentation.statusLabel }}</strong>
          </div>
          <div class="field">
            <span class="label">Term</span>
            <strong>{{ application.presentation.questTermLabel }}</strong>
          </div>
          <div v-if="contextSection?.showWorkers && application.presentation.questAssigneeTargetVisible" class="field">
            <span class="label">Workers</span>
            <strong>{{ application.presentation.questAssigneeTargetLabel }}</strong>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
