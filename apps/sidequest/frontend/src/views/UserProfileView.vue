<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import ProfileAvatar from "../components/profile/ProfileAvatar.vue"
import ProfileBio from "../components/profile/ProfileBio.vue"
import UiStatusBanner from "../components/ui/UiStatusBanner.vue"
import {sidequestApi, type AppUser, type UserProfileView, type UserRatingSummary, type UserReview} from "../api/sidequestApi.ts"
import {useTimedBanner} from "../composables/useTimedBanner.ts"
import {formatQuestTerm} from "../shared/questSchedule.ts"

const route = useRoute()
const router = useRouter()
const profile = ref<AppUser | null>(null)
const profileView = ref<UserProfileView | null>(null)
const isLoading = ref(false)
const error = ref("")
const isSendingCircleRequest = ref(false)
const copyBanner = useTimedBanner(1500)
const circleBanner = useTimedBanner(3500)

const userId = computed(() => Number(route.params.id))
const isOwnProfile = computed(() => profileView.value?.ownProfile ?? false)
const primaryAction = computed(() => profileView.value?.primaryAction ?? null)
const showBlockAction = computed(() => profileView.value?.showBlockAction ?? false)
const blockActionEnabled = computed(() => profileView.value?.blockActionEnabled ?? false)
const employerRating = computed<UserRatingSummary>(() => profileView.value?.employerRating ?? {averageStars: 0, reviewCount: 0})
const workerRating = computed<UserRatingSummary>(() => profileView.value?.workerRating ?? {averageStars: 0, reviewCount: 0})
const recentReviews = computed<UserReview[]>(() => profileView.value?.recentReviews ?? [])
const profileLink = computed(() => `${window.location.origin}/users/${profile.value?.id ?? userId.value}`)
const copied = computed(() => !!copyBanner.message.value)
const circleRequestMessage = computed(() => circleBanner.message.value)
const circleRequestTone = computed(() => circleBanner.tone.value)
const formatRating = (summary: UserRatingSummary) => summary.reviewCount ? `${summary.averageStars.toFixed(1)} / 5` : "No reviews"

const copyProfileLink = async () => {
  if (!profile.value) {
    return
  }

  await navigator.clipboard.writeText(profileLink.value)
  copyBanner.show("Link copied.")
}

const showCircleMessage = (message: string, tone: "success" | "warning" = "success") => {
  circleBanner.show(message, tone)
}

const loadProfileView = async () => {
  if (!Number.isFinite(userId.value)) {
    error.value = "Invalid profile."
    profile.value = null
    profileView.value = null
    return
  }

  const response = await sidequestApi.getUserProfileView(userId.value)
  profileView.value = response
  profile.value = response.profile
}

const sendCircleRequest = async () => {
  if (!profile.value) {
    return
  }

  isSendingCircleRequest.value = true
  try {
    await sidequestApi.createCircleRequest({recipientId: profile.value.id})
    showCircleMessage("Connection invite sent.")
    await loadProfileView()
  } catch {
    showCircleMessage("Could not send connection invite.", "warning")
  } finally {
    isSendingCircleRequest.value = false
  }
}

const blockProfile = async () => {
  if (!profile.value) {
    return
  }

  try {
    await sidequestApi.blockCircleUser({blockedUserId: profile.value.id})
    showCircleMessage("User blocked.")
    await loadProfileView()
  } catch {
    showCircleMessage("Could not block user.", "warning")
  }
}

const unblockProfile = async () => {
  if (!profile.value) {
    return
  }

  try {
    await sidequestApi.unblockCircleUser(profile.value.id)
    showCircleMessage("User unblocked.")
    await loadProfileView()
  } catch {
    showCircleMessage("Could not unblock user.", "warning")
  }
}

const handleCircleAction = () => {
  switch (primaryAction.value?.type) {
    case "EDIT_PROFILE":
      void router.push("/quests")
      return
    case "UNBLOCK":
      void unblockProfile()
      return
    case "OPEN_CIRCLES":
      void router.push("/circles")
      return
    case "SEND_INVITE":
      void sendCircleRequest()
      return
    default:
      return
  }
}

const fetchProfile = async () => {
  isLoading.value = true
  error.value = ""

  try {
    await loadProfileView()
  } catch {
    profile.value = null
    profileView.value = null
    error.value = "Profile not found."
  } finally {
    isLoading.value = false
  }
}

watch(() => route.params.id, () => {
  void fetchProfile()
})

onMounted(() => {
  void fetchProfile()
})
</script>

<template>
  <div class="page">
    <div class="page-header u-row-between u-items-end u-wrap u-gap-16">
      <div>
        <h1 class="page-title">Profile</h1>
        <p class="page-subtitle">Public profile card for quests and applications.</p>
      </div>

      <div class="button-row">
        <button class="button button--secondary" type="button" @click="router.back()">Back</button>
        <button class="button button--secondary" type="button" @click="copyProfileLink">
          {{ copied ? "Link copied" : "Copy link" }}
        </button>
        <button v-if="isOwnProfile" class="button" type="button" @click="router.push('/quests')">
          Edit profile
        </button>
      </div>
    </div>

    <div v-if="isLoading" class="empty-state">Loading profile...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>

    <div v-else-if="profile" class="card profile-page">
      <UiStatusBanner :message="circleRequestMessage" :tone="circleRequestTone" />

      <div class="profile-page__header">
        <ProfileAvatar :username="profile.username" :avatar-data-url="profile.profileAvatarDataUrl" :size="108" />
        <div class="stack">
          <h2 class="card__title">{{ profile.username }}</h2>
          <div class="muted">{{ profile.role }}</div>
          <div class="muted">{{ profile.email }}</div>
        </div>
      </div>

      <div class="field">
        <span class="label">About</span>
        <ProfileBio :text="profile.profileDescription" placeholder="This user has not added a profile description yet." />
      </div>

      <div class="profile-page__notice">
        <strong>Visible on SideQuest</strong>
        <p class="muted mb-0">
          This is the profile other people see when they open your quests or applications.
        </p>
      </div>

      <div v-if="!isOwnProfile && primaryAction" class="button-row mt-4">
        <button class="button" type="button" :disabled="isSendingCircleRequest || !primaryAction.enabled" @click="handleCircleAction">
          {{ primaryAction.label }}
        </button>
        <button
          v-if="showBlockAction"
          class="button button--secondary"
          type="button"
          :disabled="isSendingCircleRequest || !blockActionEnabled"
          @click="blockProfile"
        >
          Block
        </button>
      </div>

      <div class="profile-stats">
        <div class="profile-stat">
          <span class="label">Open jobs</span>
          <strong>{{ profile.openQuestCount }}</strong>
        </div>
        <div class="profile-stat">
          <span class="label">As employer</span>
          <strong>{{ formatRating(employerRating) }}</strong>
          <span class="muted">{{ employerRating.reviewCount }} reviews</span>
        </div>
        <div class="profile-stat">
          <span class="label">As worker</span>
          <strong>{{ formatRating(workerRating) }}</strong>
          <span class="muted">{{ workerRating.reviewCount }} reviews</span>
        </div>
      </div>

      <div class="profile-open-quests">
        <div class="field">
          <span class="label">Recent reviews</span>
        </div>

        <div v-if="recentReviews.length" class="stack">
          <article v-for="review in recentReviews" :key="review.id" class="profile-review">
            <div class="profile-open-quest__top">
              <div class="stack">
                <strong>{{ review.reviewerUsername }}</strong>
                <div class="muted">{{ "★".repeat(review.stars) }}{{ "☆".repeat(5 - review.stars) }} · {{ review.reviewedRole === 'EMPLOYER' ? "As employer" : "As worker" }}</div>
              </div>
              <RouterLink class="badge badge--accent" :to="`/quests/${review.questId}`">
                {{ review.questTitle }}
              </RouterLink>
            </div>
            <p v-if="review.comment" class="profile-open-quest__description">{{ review.comment }}</p>
          </article>
        </div>

        <div v-else class="empty-state">
          No reviews yet.
        </div>
      </div>

      <div class="profile-open-quests">
        <div class="field">
          <span class="label">Open jobs you can apply for</span>
        </div>

        <div v-if="profile.openQuests.length" class="stack">
          <article v-for="quest in profile.openQuests" :key="quest.id" class="profile-open-quest">
            <div class="profile-open-quest__top">
              <div class="stack">
                <strong>{{ quest.title }}</strong>
                <div class="muted">$ {{ quest.awardAmount }} · {{ formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</div>
              </div>
              <span class="badge badge--accent">Open</span>
            </div>
            <p class="profile-open-quest__description">{{ quest.description }}</p>
            <div class="button-row">
              <RouterLink class="button button--secondary" :to="`/quests/${quest.id}`">
                View job
              </RouterLink>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          No open jobs right now.
        </div>
      </div>

    </div>
  </div>
</template>
