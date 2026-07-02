<script setup lang="ts">
import {onMounted, computed, ref} from "vue"
import {useRouter} from "vue-router"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {visionApi, type QuestApplication} from "../api/visionApi.ts"
import {formatApplicationPrice} from "../shared/pricing.ts"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"

const router = useRouter()
const applications = ref<QuestApplication[]>([])
const isLoading = ref(false)
const error = ref("")

const loadApplications = async () => {
  isLoading.value = true
  error.value = ""

  try {
    applications.value = await visionApi.getMyApplications()
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not load your applications.")
    applications.value = []
  } finally {
    isLoading.value = false
  }
}

const openApplication = async (applicationId: number) => {
  await router.push(`/vision/applications/${applicationId}`)
}

const openQuest = async (questId: number) => {
  await router.push(`/vision/quests/${questId}`)
}

const lines = computed(() => applications.value.map((application) => ({
  id: application.id,
  questId: application.questId,
  title: application.questTitle,
  meta: [
    application.questCreatorUsername,
    application.presentation.questStatusLabel,
    application.proposedPrice !== null && application.proposedPrice !== undefined
      ? formatApplicationPrice(application.proposedPrice)
      : "",
    application.presentation.statusLabel
  ].filter(Boolean).join(" · "),
  message: application.message
})))

onMounted(() => {
  void loadApplications()
})
</script>

<template>
  <VisionDetailSurface
    eyebrow="Vision route"
    title="My applications"
    subtitle="A text feed of the applications you have already created inside Vision."
    @close="router.push('/vision')"
  >
    <section class="vision-applications-terminal">
      <p class="vision-applications-terminal__line">> applications</p>
      <p v-if="error" class="vision-applications-terminal__line vision-applications-terminal__line--error">{{ error }}</p>
      <p v-else-if="isLoading" class="vision-applications-terminal__line">loading applications...</p>

      <div v-if="lines.length" class="vision-applications-terminal__rows">
        <article v-for="application in lines" :key="application.id" class="vision-applications-terminal__row">
          <div class="vision-applications-terminal__row-main">
            <strong>{{ application.title }}</strong>
            <span class="vision-applications-terminal__muted">{{ application.meta }}</span>
            <p class="vision-applications-terminal__message">{{ application.message }}</p>
          </div>

          <div class="vision-applications-terminal__row-actions">
            <button class="vision-applications-terminal__action" type="button" @click="openApplication(application.id)">
              open application
            </button>
            <button class="vision-applications-terminal__action vision-applications-terminal__action--ghost" type="button" @click="openQuest(application.questId)">
              open quest
            </button>
          </div>
        </article>
      </div>

      <p v-else-if="!isLoading" class="vision-applications-terminal__line vision-applications-terminal__line--soft">
        You have not created any applications yet.
      </p>
    </section>
  </VisionDetailSurface>
</template>

<style scoped>
.vision-applications-terminal {
  width: min(72rem, 100%);
  display: grid;
  gap: 0.9rem;
}

.vision-applications-terminal__line {
  margin: 0;
  line-height: 1.5;
}

.vision-applications-terminal__line--soft,
.vision-applications-terminal__muted {
  color: var(--vision-surface-ink-soft);
}

.vision-applications-terminal__line--error {
  color: #b04f43;
}

.vision-applications-terminal__rows {
  display: grid;
  gap: 0.75rem;
}

.vision-applications-terminal__row {
  display: grid;
  gap: 0.45rem;
  padding-left: 0.35rem;
}

.vision-applications-terminal__row-main {
  display: grid;
  gap: 0.25rem;
}

.vision-applications-terminal__message {
  margin: 0;
  color: var(--vision-surface-ink);
  line-height: 1.5;
}

.vision-applications-terminal__row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-applications-terminal__action {
  appearance: none;
  border: 0;
  border-bottom: 1px solid rgba(24, 36, 47, 0.18);
  background: transparent;
  padding: 0.28rem 0;
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
}

.vision-applications-terminal__action--ghost {
  color: rgba(24, 36, 47, 0.68);
}
</style>
