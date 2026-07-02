<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue"
import {useRoute, useRouter} from "vue-router"
import {currentUser} from "../../../auth.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import {updateSessionUser} from "../../../services/sessionService.ts"
import {visionApi} from "../api/visionApi.ts"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import {formatQuestReward} from "../shared/pricing.ts"
import {formatQuestTermForDisplay} from "../../../shared/questSchedule.ts"
import {formatReviewedRoleLabel, formatUserRatingSummary} from "../../../shared/profileFormatting.ts"
import {compressProfileAvatar} from "../../../shared/imageCompression.ts"
import {PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../shared/clientMessages.ts"
import type {AppUser} from "../../../contracts/index.ts"
import {executeSocialAction} from "../shared/socialActions.ts"
import {useUserProfileView} from "../composables/useUserProfileView.ts"
import {getProfileInitials} from "../../../shared/profileFormatting.ts"

const route = useRoute()
const router = useRouter()
const profileImageInputRef = ref<HTMLInputElement | null>(null)
const profileDescriptionDraft = ref("")
const profileAvatarDraft = ref("")
const originalProfileDescription = ref("")
const originalProfileAvatar = ref("")

const userId = computed(() => {
  const parsed = Number(route.params.id)
  return Number.isFinite(parsed) ? parsed : null
})

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
  showMessage,
  clearProfile,
  fetchProfile,
  sendInvite,
  blockProfile,
  unblockProfile
} = useUserProfileView({
  userId: () => userId.value,
  enabled: () => true,
  loadErrorMessage: "Could not load profile."
})

const profileTitle = computed(() => profile.value?.username ?? "Profile")

const summaryLine = computed(() => {
  if (!profile.value) {
    return ""
  }

  const locationLabel = profile.value.locationSettings?.label
    || profile.value.locationSettings?.locality
    || profile.value.locationSettings?.country
    || "Hidden"

  return [
    profile.value.email,
    profile.value.role,
    locationLabel,
    profile.value.locationSettings?.visibilitySummary || "Hidden"
  ].filter(Boolean).join(" · ")
})

const overviewLines = computed(() => {
  if (!profile.value) {
    return []
  }

  return [
    `Location: ${profile.value.locationSettings?.label || profile.value.locationSettings?.locality || profile.value.locationSettings?.country || "Hidden"}`,
    `Visibility: ${profile.value.locationSettings?.visibilitySummary || "Hidden"}`,
    `As employer: ${formatUserRatingSummary(employerRating.value)}`,
    `As worker: ${formatUserRatingSummary(workerRating.value)}`
  ]
})

const openQuest = async (quest: AppUser["openQuests"][number]) => {
  await router.push(routeForNavigationTarget(quest.questNavigation))
}

const handleSocialAction = async (action = primaryAction.value) => {
  await executeSocialAction(router, action, {
    editProfile: async () => {
      await router.push("/vision/profile")
    },
    sendInvite: () => {
      void sendInvite()
    },
    block: () => {
      void blockProfile()
    },
    unblock: () => {
      void unblockProfile()
    }
  })
}

const syncOwnProfileDraft = () => {
  if (!profile.value || !isOwnProfile.value) {
    return
  }

  profileDescriptionDraft.value = profile.value.profileDescription ?? ""
  profileAvatarDraft.value = profile.value.profileAvatarDataUrl ?? ""
  originalProfileDescription.value = profileDescriptionDraft.value
  originalProfileAvatar.value = profileAvatarDraft.value
}

const hasUnsavedOwnProfileChanges = computed(() =>
  profileDescriptionDraft.value !== originalProfileDescription.value
  || profileAvatarDraft.value !== originalProfileAvatar.value
)

const updateProfileAvatarFromFile = async (file: File | null) => {
  if (!file) {
    profileAvatarDraft.value = ""
    return
  }

  try {
    profileAvatarDraft.value = await compressProfileAvatar(file)
  } catch {
    showMessage(PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE, "warning")
  }
}

const clearProfileAvatar = () => {
  profileAvatarDraft.value = ""
}

const openProfileImagePicker = () => {
  profileImageInputRef.value?.click()
}

const profileAvatarStyle = (size: number) => ({
  "--profile-avatar-size": `${size}px`
})

const discardOwnProfileChanges = () => {
  syncOwnProfileDraft()
}

const saveOwnProfileChanges = async () => {
  if (!currentUser.value || !profile.value || !isOwnProfile.value) {
    return
  }

  try {
    const response = await visionApi.updateCurrentAppUser({
      email: profile.value.email,
      username: profile.value.username,
      profileDescription: profileDescriptionDraft.value,
      profileAvatarDataUrl: profileAvatarDraft.value || null,
      locationSettings: profile.value.locationSettings ?? null
    })

    updateSessionUser({
      email: response.email,
      username: response.username,
      profileDescription: response.profileDescription,
      profileAvatarDataUrl: response.profileAvatarDataUrl,
      createdAt: response.createdAt ?? currentUser.value.createdAt,
      role: response.role ?? currentUser.value.role
    })

    await fetchProfile()
  } catch (requestError) {
    showMessage(getApiErrorMessage(requestError, "Could not update profile."), "warning")
  }
}

watch(() => userId.value, () => {
  void fetchProfile()
}, {immediate: true})

watch(() => [profile.value?.id, isOwnProfile.value] as const, () => {
  syncOwnProfileDraft()
}, {immediate: true})

onBeforeUnmount(() => {
  clearProfile()
})
</script>

<template>
  <section class="vision-profile-terminal">
    <header class="vision-profile-terminal__header">
      <div>
        <p class="vision-profile-terminal__eyebrow">Vision</p>
        <h1 class="vision-profile-terminal__title">{{ profileTitle }}</h1>
      </div>
      <button class="vision-profile-terminal__back" type="button" @click="router.push('/vision/circles')">
        > back
      </button>
    </header>

    <div class="vision-profile-terminal__feed">
      <p v-if="bannerMessage" :class="['vision-profile-terminal__line', `vision-profile-terminal__line--${bannerTone}`]">
        {{ bannerMessage }}
      </p>
      <p v-if="isLoading" class="vision-profile-terminal__line">loading profile...</p>
      <p v-else-if="error" class="vision-profile-terminal__line vision-profile-terminal__line--error">{{ error }}</p>
      <template v-else-if="profile">
        <p class="vision-profile-terminal__line">> profile</p>
        <p class="vision-profile-terminal__line vision-profile-terminal__line--soft">{{ summaryLine }}</p>

        <section class="vision-profile-terminal__block">
          <p class="vision-profile-terminal__block-title">overview</p>
          <p v-for="line in overviewLines" :key="line" class="vision-profile-terminal__line">{{ line }}</p>
        </section>

        <section v-if="isOwnProfile" class="vision-profile-terminal__block">
          <p class="vision-profile-terminal__block-title">edit</p>
          <div class="vision-profile-terminal__row">
            <span class="profile-avatar" :style="profileAvatarStyle(96)">
              <img
                v-if="profileAvatarDraft"
                class="profile-avatar__image"
                :src="profileAvatarDraft"
                :alt="`${profile.username || 'User'} avatar`"
              />
              <span v-else class="profile-avatar__fallback">{{ getProfileInitials(profile.username) }}</span>
            </span>
            <div class="vision-profile-terminal__row-actions">
              <button class="vision-profile-terminal__action" type="button" @click="openProfileImagePicker">change avatar</button>
              <button v-if="profileAvatarDraft" class="vision-profile-terminal__action vision-profile-terminal__action--ghost" type="button" @click="clearProfileAvatar">remove avatar</button>
            </div>
          </div>

          <textarea
            v-model="profileDescriptionDraft"
            class="vision-profile-terminal__textarea"
            rows="4"
            placeholder="Write a short description..."
          ></textarea>

          <div v-if="hasUnsavedOwnProfileChanges" class="vision-profile-terminal__row-actions">
            <button class="vision-profile-terminal__action" type="button" @click="saveOwnProfileChanges">save changes</button>
            <button class="vision-profile-terminal__action vision-profile-terminal__action--ghost" type="button" @click="discardOwnProfileChanges">discard</button>
          </div>
        </section>

        <section v-else class="vision-profile-terminal__block">
          <p class="vision-profile-terminal__block-title">actions</p>
          <div class="vision-profile-terminal__row-actions">
            <button
              v-if="primaryAction"
              class="vision-profile-terminal__action"
              type="button"
              :disabled="isSaving || !primaryAction.enabled"
              @click="handleSocialAction(primaryAction)"
            >
              {{ primaryAction.label }}
            </button>
            <button
              v-if="showBlockAction"
              class="vision-profile-terminal__action vision-profile-terminal__action--ghost"
              type="button"
              :disabled="isSaving || !blockActionEnabled"
              @click="profileView?.relation.blockedByCurrentUser ? unblockProfile() : blockProfile()"
            >
              {{ profileView?.blockActionLabel ?? "block" }}
            </button>
          </div>
        </section>

        <section class="vision-profile-terminal__block">
          <p class="vision-profile-terminal__block-title">recent reviews</p>
          <div v-if="recentReviews.length" class="vision-profile-terminal__rows">
            <article v-for="review in recentReviews" :key="review.id" class="vision-profile-terminal__row">
              <div class="vision-profile-terminal__row-main">
                <strong>{{ review.reviewerUsername }}</strong>
                <span class="vision-profile-terminal__muted">★{{ review.stars }} · {{ formatReviewedRoleLabel(review.reviewedRole) }} · {{ review.questTitle }}</span>
              </div>
              <p class="vision-profile-terminal__message">{{ review.comment || "No comment." }}</p>
            </article>
          </div>
          <p v-else class="vision-profile-terminal__line vision-profile-terminal__line--soft">No reviews yet.</p>
        </section>

        <section v-if="!isOwnProfile" class="vision-profile-terminal__block">
          <p class="vision-profile-terminal__block-title">open jobs</p>
          <div v-if="profile.openQuests.length" class="vision-profile-terminal__rows">
            <article v-for="quest in profile.openQuests" :key="quest.id" class="vision-profile-terminal__row">
              <div class="vision-profile-terminal__row-main">
                <strong>{{ quest.title }}</strong>
                <span class="vision-profile-terminal__muted">
                  {{ formatQuestReward(quest.awardAmount) }} · {{ formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed) }}
                </span>
                <p class="vision-profile-terminal__message">{{ quest.description }}</p>
              </div>
              <div class="vision-profile-terminal__row-actions">
                <button class="vision-profile-terminal__action" type="button" @click="openQuest(quest)">open job</button>
              </div>
            </article>
          </div>
          <p v-else class="vision-profile-terminal__line vision-profile-terminal__line--soft">No open jobs right now.</p>
        </section>
      </template>
    </div>

    <input
      ref="profileImageInputRef"
      class="visually-hidden"
      type="file"
      accept="image/*"
      @change="updateProfileAvatarFromFile(($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
    >
  </section>
</template>

<style scoped>
.vision-profile-terminal {
  width: min(72rem, 100%);
  display: grid;
  gap: 1rem;
}

.vision-profile-terminal__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.vision-profile-terminal__eyebrow {
  margin: 0 0 0.15rem;
  color: var(--vision-surface-ink-muted);
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.vision-profile-terminal__title {
  margin: 0;
  font-size: clamp(1.7rem, 2.8vw, 2.4rem);
  letter-spacing: -0.05em;
}

.vision-profile-terminal__back {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: rgba(24, 36, 47, 0.68);
  font: inherit;
  font-size: 0.76rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
}

.vision-profile-terminal__feed {
  display: grid;
  gap: 1rem;
}

.vision-profile-terminal__line,
.vision-profile-terminal__message {
  margin: 0;
  line-height: 1.5;
  white-space: pre-wrap;
}

.vision-profile-terminal__line--soft,
.vision-profile-terminal__muted {
  color: var(--vision-surface-ink-soft);
}

.vision-profile-terminal__line--error {
  color: #b04f43;
}

.vision-profile-terminal__block {
  display: grid;
  gap: 0.7rem;
}

.vision-profile-terminal__block-title {
  margin: 0;
  color: rgba(24, 36, 47, 0.48);
  font-size: 0.68rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.vision-profile-terminal__rows {
  display: grid;
  gap: 0.65rem;
}

.vision-profile-terminal__row {
  display: grid;
  gap: 0.35rem;
  padding-left: 0.35rem;
}

.vision-profile-terminal__row-main {
  display: grid;
  gap: 0.25rem;
}

.vision-profile-terminal__row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.vision-profile-terminal__action {
  appearance: none;
  border: 0;
  border-bottom: 1px solid rgba(24, 36, 47, 0.18);
  background: transparent;
  padding: 0.28rem 0;
  color: var(--vision-surface-ink);
  font: inherit;
  cursor: pointer;
}

.vision-profile-terminal__action--ghost {
  color: rgba(24, 36, 47, 0.68);
}

.vision-profile-terminal__textarea {
  width: 100%;
  border: 0;
  border-bottom: 1px solid rgba(24, 36, 47, 0.16);
  border-radius: 0;
  background: transparent;
  padding: 0.35rem 0;
  color: var(--vision-surface-ink);
  font: inherit;
  line-height: 1.6;
  resize: vertical;
  outline: none;
}

@media (max-width: 720px) {
  .vision-profile-terminal__header {
    flex-direction: column;
  }
}
</style>
