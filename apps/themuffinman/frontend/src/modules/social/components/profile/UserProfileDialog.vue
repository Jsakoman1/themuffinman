<script setup lang="ts">
import {computed, ref, watch} from "vue"
import {useRouter} from "vue-router"
import ProfileOpenQuestItem from "../../../../components/profile/ProfileOpenQuestItem.vue"
import ProfileReviewItem from "../../../../components/profile/ProfileReviewItem.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import ProfileSummaryCard from "../../../../components/profile/ProfileSummaryCard.vue"
import UiInfoGrid from "../../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../../components/ui/UiStatCard.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import {currentUser} from "../../../../auth.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {updateSessionUser} from "../../../../services/sessionService.ts"
import {compressProfileAvatar} from "../../../../shared/imageCompression.ts"
import {PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../../shared/clientMessages.ts"
import type {AppUser} from "../../../workmarket/api/workmarketApi.ts"
import {workmarketApi} from "../../../workmarket/api/workmarketApi.ts"
import ProfileDescriptionEditor from "../../../workmarket/components/shared/ProfileDescriptionEditor.vue"
import ProfileIdentityEditor from "../../../workmarket/components/shared/ProfileIdentityEditor.vue"
import DetailDialogFrame from "../../../workmarket/components/shared/DetailDialogFrame.vue"
import DetailUtilitySection from "../../../workmarket/components/shared/DetailUtilitySection.vue"
import {formatReviewedRoleLabel, formatUserRatingSummary} from "../../../../shared/profileFormatting.ts"
import {routeForNavigationTarget} from "../../../workmarket/shared/navigationTargets.ts"
import {executeSocialAction} from "../../shared/socialActions.ts"
import {useUserProfileView} from "../../composables/useUserProfileView.ts"

const props = defineProps<{
  open: boolean
  userId: number | null
}>()
const router = useRouter()

const emit = defineEmits<{
  (event: "close"): void
}>()

const profileEmail = ref("")
const profileUsername = ref("")
const profileDescription = ref("")
const profileAvatarDataUrl = ref("")
const originalProfileEmail = ref("")
const originalProfileUsername = ref("")
const originalProfileDescription = ref("")
const originalProfileAvatarDataUrl = ref("")

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
  userId: () => props.userId,
  enabled: () => props.open,
  loadErrorMessage: "Could not load profile."
})

const handlePrimaryAction = () => void executeSocialAction(router, primaryAction.value, {
  close: () => emit("close"),
  sendInvite: () => {
    void sendInvite()
  },
  unblock: () => {
    void unblockProfile()
  }
})

const openQuest = async (quest: AppUser["openQuests"][number]) => {
  emit("close")
  await router.push(routeForNavigationTarget(quest.questNavigation))
}

const syncOwnProfileDraft = () => {
  if (!profile.value || !isOwnProfile.value) {
    return
  }

  profileEmail.value = profile.value.email ?? currentUser.value?.email ?? ""
  profileUsername.value = profile.value.username ?? currentUser.value?.username ?? ""
  profileDescription.value = profile.value.profileDescription ?? ""
  profileAvatarDataUrl.value = profile.value.profileAvatarDataUrl ?? ""
  originalProfileEmail.value = profileEmail.value
  originalProfileUsername.value = profileUsername.value
  originalProfileDescription.value = profileDescription.value
  originalProfileAvatarDataUrl.value = profileAvatarDataUrl.value
}

const hasUnsavedChanges = computed(() =>
  profileEmail.value.trim() !== originalProfileEmail.value.trim()
  || profileUsername.value.trim() !== originalProfileUsername.value.trim()
  || profileDescription.value.trim() !== originalProfileDescription.value.trim()
  || profileAvatarDataUrl.value !== originalProfileAvatarDataUrl.value
)

const updateProfileAvatarFromFile = async (file: File | null) => {
  if (!file) {
    profileAvatarDataUrl.value = ""
    return
  }

  try {
    profileAvatarDataUrl.value = await compressProfileAvatar(file)
  } catch {
    showMessage(PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE, "warning")
  }
}

const clearProfileAvatar = () => {
  profileAvatarDataUrl.value = ""
}

const saveProfile = async () => {
  if (!currentUser.value || !isOwnProfile.value) {
    return
  }

  isSaving.value = true
  try {
    const response = await workmarketApi.updateCurrentAppUser({
      email: profileEmail.value.trim(),
      username: profileUsername.value.trim(),
      profileDescription: profileDescription.value.trim(),
      profileAvatarDataUrl: profileAvatarDataUrl.value || null
    })

    updateSessionUser({
      email: response.email,
      username: response.username,
      profileDescription: response.profileDescription,
      profileAvatarDataUrl: response.profileAvatarDataUrl,
      createdAt: response.createdAt ?? currentUser.value.createdAt,
      role: response.role ?? currentUser.value.role
    })

    profileEmail.value = response.email
    profileUsername.value = response.username
    profileDescription.value = response.profileDescription ?? ""
    profileAvatarDataUrl.value = response.profileAvatarDataUrl ?? ""
    originalProfileEmail.value = profileEmail.value
    originalProfileUsername.value = profileUsername.value
    originalProfileDescription.value = profileDescription.value
    originalProfileAvatarDataUrl.value = profileAvatarDataUrl.value
    showMessage("Profile updated.", "success")
    await fetchProfile()
  } catch (requestError) {
    showMessage(getApiErrorMessage(requestError, "Could not update profile."), "warning")
  } finally {
    isSaving.value = false
  }
}

watch(() => [props.open, props.userId] as const, () => {
  if (!props.open) {
    clearProfile()
    return
  }

  void fetchProfile()
}, {immediate: true})

watch(() => [profile.value?.id, isOwnProfile.value] as const, () => {
  syncOwnProfileDraft()
}, {immediate: true})
</script>

<template>
  <UiDialog
    :open="open"
    :title="profile?.username ?? 'Profile'"
    size="xl"
    @close="$emit('close')"
  >
    <div class="surface-stack">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

      <div v-if="isLoading" class="empty-state">Loading profile...</div>
      <div v-else-if="error" class="alert alert--error">{{ error }}</div>

      <template v-else-if="profile">
        <DetailDialogFrame>
          <template #main>
            <section class="surface-stack surface-stack--content">
              <UiSurfaceSection v-if="isOwnProfile" compact eyebrow="My profile">
                <ProfileDescriptionEditor :description="profileDescription" @update:description="profileDescription = $event" />
              </UiSurfaceSection>

              <ProfileSummaryCard
                v-else
                :username="profile.username"
                :avatar-data-url="profile.profileAvatarDataUrl"
                :badge-label="`${profile.openQuestCount} open`"
                :role="profile.role"
                :email="profile.email"
                :description="profile.profileDescription"
                description-placeholder="This user has not added a profile description yet."
                :size="108"
              >
                <template #actions>
                  <div v-if="primaryAction" class="button-row mt-4">
                    <button class="button" type="button" :disabled="isSaving || !primaryAction.enabled" @click="handlePrimaryAction">
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
                </template>
              </ProfileSummaryCard>

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

              <UiSurfaceSection v-if="!isOwnProfile" compact eyebrow="Open jobs you can apply for">
                <div v-if="profile.openQuests.length" class="surface-stack">
                  <ProfileOpenQuestItem
                    v-for="quest in profile.openQuests"
                    :key="quest.id"
                    :title="quest.title"
                    :meta="`$ ${quest.awardAmount} · ${quest.presentation.termLabel}`"
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
            </section>
          </template>

          <template #side>
            <DetailUtilitySection v-if="isOwnProfile" title="Profile details">
              <ProfileIdentityEditor
                :email="profileEmail"
                :username="profileUsername"
                :avatar-data-url="profileAvatarDataUrl"
                @update:email="profileEmail = $event"
                @update:username="profileUsername = $event"
                @pick-image="updateProfileAvatarFromFile"
                @clear-image="clearProfileAvatar"
              />
            </DetailUtilitySection>

            <DetailUtilitySection title="Overview" tone="summary">
              <UiInfoGrid :columns="1">
                <UiStatCard label="Open jobs" :value="profile.openQuestCount" />
                <UiStatCard label="As employer" :value="formatUserRatingSummary(employerRating)" :hint="`${employerRating.reviewCount} reviews`" />
                <UiStatCard label="As worker" :value="formatUserRatingSummary(workerRating)" :hint="`${workerRating.reviewCount} reviews`" />
              </UiInfoGrid>
            </DetailUtilitySection>

            <DetailUtilitySection v-if="isOwnProfile && hasUnsavedChanges" title="Actions" tone="actions">
              <div class="ui-action-stack">
                <button class="button button--action" type="button" :disabled="isSaving" @click="saveProfile">
                  Save changes
                </button>
              </div>
            </DetailUtilitySection>
          </template>
        </DetailDialogFrame>
      </template>
    </div>
  </UiDialog>
</template>
