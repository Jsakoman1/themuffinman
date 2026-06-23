<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import ProfileBio from "../components/profile/ProfileBio.vue"
import {sidequestApi, type QuestApplication, type QuestApplicationDetail} from "../api/sidequestApi.ts"
import {formatApplicationStatus, statusBadgeClass} from "../lib/questDashboardRules.ts"
import {richTextHasContent} from "../shared/richText.ts"
import {formatQuestTerm} from "../shared/questSchedule.ts"

const route = useRoute()
const router = useRouter()
const application = ref<QuestApplication | null>(null)
const detail = ref<QuestApplicationDetail | null>(null)
const isLoading = ref(false)
const error = ref("")

const applicationId = computed(() => Number(route.params.id))
const questPath = computed(() => {
  return detail.value ? `/quests/${detail.value.sections.quest.id}` : "/quests"
})

const loadApplication = async () => {
  if (!Number.isFinite(applicationId.value)) {
    error.value = "Invalid application."
    application.value = null
    detail.value = null
    return
  }

  isLoading.value = true
  error.value = ""

  try {
    detail.value = await sidequestApi.getApplicationDetail(applicationId.value)
    application.value = detail.value.summary
  } catch {
    error.value = "Could not load application."
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
    <div class="page-header u-row-between u-items-end u-wrap u-gap-16">
      <div>
        <h1 class="page-title">Application details</h1>
        <p class="page-subtitle">Review the application and jump back to the quest.</p>
      </div>

      <div class="button-row">
        <button class="button button--secondary" type="button" @click="router.back()">Back</button>
        <RouterLink class="button button--secondary" :to="questPath">Open quest</RouterLink>
      </div>
    </div>

    <div v-if="isLoading" class="empty-state">Loading application...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>

    <div v-else-if="application" class="card">
      <div class="dialog-focus-card dialog-focus-card--primary">
        <div class="dialog-focus-card__top u-row-between u-items-center u-wrap u-gap-8">
          <span :class="['badge', statusBadgeClass(application.status)]">
            {{ formatApplicationStatus(application.status) }}
          </span>
          <span class="dialog-focus-card__kicker">Application</span>
        </div>

        <div class="dialog-focus-card__title">
          {{ application.questTitle }}
        </div>

          <div class="dialog-focus-card__meta">
            <span>$ {{ application.proposedPrice }}</span>
            <span>Quest status: {{ application.questStatus }}</span>
            <span>{{ formatQuestTerm(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}</span>
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
            <strong>{{ formatApplicationStatus(application.status) }}</strong>
          </div>
          <div class="field">
            <span class="label">Term</span>
            <strong>{{ formatQuestTerm(application.questScheduledAt, application.questEndsAt, application.questTermFixed) }}</strong>
          </div>
          <div v-if="application.questAssigneeTarget === null || application.questAssigneeTarget > 1" class="field">
            <span class="label">Workers</span>
            <strong>{{ application.questAssigneeTarget === null ? "Unlimited" : application.questAssigneeTarget }}</strong>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
