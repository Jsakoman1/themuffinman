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
import ProfileOpenQuestItem from "../../../components/profile/ProfileOpenQuestItem.vue"
import ProfileReviewItem from "../../../components/profile/ProfileReviewItem.vue"
import ProfileSummaryCard from "../../../components/profile/ProfileSummaryCard.vue"
import UiInfoGrid from "../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../components/ui/UiStatCard.vue"
import UiStatusBanner from "../../../components/ui/UiStatusBanner.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import VisionDetailSurface from "../components/VisionDetailSurface.vue"
import ProfileDescriptionEditor from "../components/shared/ProfileDescriptionEditor.vue"

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

const profileTitle = computed(() => {
  if (isOwnProfile.value) {
    return "My profile"
  }

  return profile.value?.username ?? "Profile"
})

const profileRole = computed(() => profile.value?.role ?? "")

const profileDescription = computed(() => profile.value?.profileDescription ?? "")

const summaryBadge = computed(() => {
  if (!profile.value || isOwnProfile.value) {
    return ""
  }

  return `${profile.value.openQuestCount} open`
})

const overviewItems = computed(() => {
  if (!profile.value) {
    return []
  }

  const locationLabel = profile.value.locationSettings?.label
    || profile.value.locationSettings?.locality
    || profile.value.locationSettings?.country
    || "Hidden"
  const visibilityLabel = profile.value.locationSettings?.visibilitySummary || "Hidden"

  return [
    {
      label: "Location",
      value: locationLabel,
      hint: ""
    },
    {
      label: "Visibility",
      value: visibilityLabel,
      hint: ""
    },
    {
      label: "As employer",
      value: formatUserRatingSummary(employerRating.value),
      hint: ""
    },
    {
      label: "As worker",
      value: formatUserRatingSummary(workerRating.value),
      hint: ""
    }
  ]
})

const handleSocialAction = async (action = primaryAction.value) => {
  await executeSocialAction(router, action, {
    editProfile: async () => {
      await router.push("/vision/settings")
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

const openQuest = async (quest: AppUser["openQuests"][number]) => {
  await router.push(routeForNavigationTarget(quest.questNavigation))
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
  <VisionDetailSurface
    :title="profileTitle"
    @close="router.push('/vision/circles')"
  >
    <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

    <div v-if="isLoading" class="empty-state">
      Loading profile...
    </div>
    <div v-else-if="error" class="alert alert--error">
      {{ error }}
    </div>
    <div v-else-if="profile" class="vision-profile-view">
      <div class="vision-profile-view__main">
        <UiSurfaceSection compact eyebrow="Profile">
          <ProfileSummaryCard
            :username="profile.username"
            :avatar-data-url="isOwnProfile ? profileAvatarDraft : profile.profileAvatarDataUrl"
            :badge-label="summaryBadge"
            :role="profileRole"
            :email="profile.email"
            :description="isOwnProfile ? '' : profileDescription"
            :description-placeholder="isOwnProfile ? '' : 'This user has not added a profile description yet.'"
            :size="108"
            :soft="false"
          >
            <template v-if="isOwnProfile" #avatarActions>
              <button
                class="button button--icon button--secondary button--icon-compact"
                type="button"
                aria-label="Change profile picture"
                title="Change profile picture"
                @click="openProfileImagePicker"
              >
                ✎
              </button>
              <button
                v-if="profileAvatarDraft"
                class="button button--icon button--secondary button--icon-compact"
                type="button"
                aria-label="Remove profile picture"
                title="Remove profile picture"
                @click="clearProfileAvatar"
              >
                ×
              </button>
            </template>

            <template #actions>
              <div v-if="!isOwnProfile" class="button-row mt-4">
                <button
                  v-if="primaryAction"
                  class="button"
                  type="button"
                  :disabled="isSaving || !primaryAction.enabled"
                  @click="handleSocialAction(primaryAction)"
                >
                  {{ primaryAction.label }}
                </button>
                <button
                  v-if="showBlockAction"
                  class="button button--secondary"
                  type="button"
                  :disabled="isSaving || !blockActionEnabled"
                  @click="profileView?.relation.blockedByCurrentUser ? unblockProfile() : blockProfile()"
                >
                  {{ profileView?.blockActionLabel ?? "Block" }}
                </button>
              </div>
              <div v-else class="button-row mt-4">
                <button class="button button--secondary" type="button" @click="router.push('/vision/settings')">
                  Open settings
                </button>
              </div>
            </template>
          </ProfileSummaryCard>
        </UiSurfaceSection>

        <UiSurfaceSection v-if="isOwnProfile" compact eyebrow="About me">
          <ProfileDescriptionEditor
            :description="profileDescriptionDraft"
            @update:description="profileDescriptionDraft = $event"
          />
        </UiSurfaceSection>

        <UiSurfaceSection compact eyebrow="Recent reviews">
          <div v-if="recentReviews.length" class="surface-stack">
            <ProfileReviewItem
              v-for="review in recentReviews"
              :key="review.id"
              :reviewer-username="review.reviewerUsername"
              :stars="review.stars"
              :role-label="formatReviewedRoleLabel(review.reviewedRole)"
              :quest-title="review.questTitle"
              :quest-path="routeForNavigationTarget(review.questNavigation)"
              :comment="review.comment"
            />
          </div>
          <div v-else class="empty-state">
            No reviews yet.
          </div>
        </UiSurfaceSection>

        <UiSurfaceSection v-if="!isOwnProfile" compact eyebrow="Open jobs">
          <div v-if="profile.openQuests.length" class="surface-stack">
            <ProfileOpenQuestItem
              v-for="quest in profile.openQuests"
              :key="quest.id"
              :title="quest.title"
              :meta="`${formatQuestReward(quest.awardAmount)} · ${formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed)}`"
              :status-class="quest.presentation.statusBadgeClass"
              :status-label="quest.presentation.statusLabel"
              :description="quest.description"
              action-label="Open job"
              :action-path="routeForNavigationTarget(quest.questNavigation)"
              interactive
              @open="openQuest(quest)"
            />
          </div>
          <div v-else class="empty-state">
            No open jobs right now.
          </div>
        </UiSurfaceSection>
      </div>

      <aside class="vision-profile-view__side">
        <UiSurfaceSection compact eyebrow="Overview">
          <UiInfoGrid :columns="1">
            <UiStatCard
              v-for="item in overviewItems"
              :key="item.label"
              :label="item.label"
              :value="item.value"
              :hint="item.hint"
            />
          </UiInfoGrid>
        </UiSurfaceSection>

        <UiSurfaceSection v-if="isOwnProfile && hasUnsavedOwnProfileChanges" compact eyebrow="Actions">
          <div class="button-row">
            <button class="button button--action" type="button" @click="saveOwnProfileChanges">
              Save changes
            </button>
            <button class="button button--secondary" type="button" @click="discardOwnProfileChanges">
              Discard
            </button>
          </div>
        </UiSurfaceSection>
      </aside>
    </div>

    <input
      ref="profileImageInputRef"
      class="visually-hidden"
      type="file"
      accept="image/*"
      @change="updateProfileAvatarFromFile(($event.target as HTMLInputElement).files?.[0] ?? null); ($event.target as HTMLInputElement).value = ''"
    >
  </VisionDetailSurface>
</template>

<style scoped>
.vision-profile-view {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(18rem, 24rem);
  gap: 1.25rem;
}

.vision-profile-view__main,
.vision-profile-view__side {
  display: grid;
  gap: 1rem;
}

@media (max-width: 920px) {
  .vision-profile-view {
    grid-template-columns: 1fr;
  }
}
</style>
