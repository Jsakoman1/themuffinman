<script setup lang="ts">
import {computed, onMounted, watch} from "vue"
import {RouterLink, useRoute, useRouter} from "vue-router"
import AppPageHeader from "../../../components/app/AppPageHeader.vue"
import ProfileAvatar from "../../../components/profile/ProfileAvatar.vue"
import ProfileBio from "../../../components/profile/ProfileBio.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import {useTimedBanner} from "../../../composables/useTimedBanner.ts"
import {formatReviewedRoleLabel, formatUserRatingSummary} from "../../../shared/profileFormatting.ts"
import {routeForNavigationTarget} from "../../workmarket/shared/navigationTargets.ts"
import {invalidEntityMessage} from "../../../shared/clientMessages.ts"
import {executeSocialAction} from "../shared/socialActions.ts"
import {useUserProfileView} from "../composables/useUserProfileView.ts"

const route = useRoute()
const router = useRouter()
const copyBanner = useTimedBanner(1500)

const userId = computed(() => Number(route.params.id))
const {
  profile,
  profileView,
  isLoading,
  isSaving,
  error,
  isOwnProfile,
  primaryAction,
  showBlockAction,
  blockActionEnabled,
  employerRating,
  workerRating,
  recentReviews,
  bannerMessage,
  bannerTone,
  fetchProfile,
  sendInvite,
  blockProfile,
  unblockProfile
} = useUserProfileView({
  userId,
  loadErrorMessage: "Profile not found.",
  invalidErrorMessage: invalidEntityMessage("profile")
})
const profileLink = computed(() => `${window.location.origin}/users/${profile.value?.id ?? userId.value}`)
const copied = computed(() => !!copyBanner.message.value)

const copyProfileLink = async () => {
  if (!profile.value) {
    return
  }

  await navigator.clipboard.writeText(profileLink.value)
  copyBanner.show("Link copied.")
}

const openProfileEditor = () => {
  void router.push({
    path: "/circles",
    query: {profile: "edit"}
  })
}

const handleCircleAction = () => void executeSocialAction(router, primaryAction.value, {
  editProfile: () => {
    openProfileEditor()
  },
  sendInvite: () => {
    void sendInvite()
  },
  unblock: () => {
    void unblockProfile()
  }
})

watch(() => route.params.id, () => {
  void fetchProfile()
})

onMounted(() => {
  void fetchProfile()
})
</script>

<template>
  <div class="page">
    <AppPageHeader title="Profile" subtitle="Public profile card for quests and applications.">
      <template #actions>
        <button class="button button--secondary" type="button" @click="router.back()">Back</button>
        <button class="button button--secondary" type="button" @click="copyProfileLink">
          {{ copied ? "Link copied" : "Copy link" }}
        </button>
        <button v-if="isOwnProfile" class="button" type="button" @click="openProfileEditor">
          Edit profile
        </button>
      </template>
    </AppPageHeader>

    <div v-if="isLoading" class="empty-state">Loading profile...</div>
    <div v-else-if="error" class="alert alert--error">{{ error }}</div>

    <div v-else-if="profile" class="card profile-page">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

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
        <button class="button" type="button" :disabled="isSaving || !primaryAction.enabled" @click="handleCircleAction">
          {{ primaryAction.label }}
        </button>
        <button
          v-if="showBlockAction"
          class="button button--secondary"
          type="button"
          :disabled="isSaving || !blockActionEnabled"
          @click="blockProfile"
        >
          {{ profileView?.blockActionLabel ?? "Block" }}
        </button>
      </div>

      <div class="profile-stats">
        <div class="profile-stat">
          <span class="label">Open jobs</span>
          <strong>{{ profile.openQuestCount }}</strong>
        </div>
        <div class="profile-stat">
          <span class="label">As employer</span>
          <strong>{{ formatUserRatingSummary(employerRating) }}</strong>
          <span class="muted">{{ employerRating.reviewCount }} reviews</span>
        </div>
        <div class="profile-stat">
          <span class="label">As worker</span>
          <strong>{{ formatUserRatingSummary(workerRating) }}</strong>
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
                <div class="muted">{{ "★".repeat(review.stars) }}{{ "☆".repeat(5 - review.stars) }} · {{ formatReviewedRoleLabel(review.reviewedRole) }}</div>
              </div>
              <RouterLink class="badge badge--accent" :to="routeForNavigationTarget(review.questNavigation)">
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
                <div class="muted">$ {{ quest.awardAmount }} · {{ quest.presentation.termLabel }}</div>
              </div>
              <span :class="quest.presentation.statusBadgeClass">{{ quest.presentation.statusLabel }}</span>
            </div>
            <p class="profile-open-quest__description">{{ quest.description }}</p>
            <div class="button-row">
              <RouterLink class="button button--secondary" :to="routeForNavigationTarget(quest.questNavigation)">
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
