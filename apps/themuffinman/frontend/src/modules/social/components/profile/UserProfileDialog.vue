<script setup lang="ts">
import {watch} from "vue"
import {useRouter} from "vue-router"
import ProfileOpenQuestItem from "../../../../components/profile/ProfileOpenQuestItem.vue"
import ProfileReviewItem from "../../../../components/profile/ProfileReviewItem.vue"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import ProfileSummaryCard from "../../../../components/profile/ProfileSummaryCard.vue"
import UiInfoGrid from "../../../../components/ui/UiInfoGrid.vue"
import UiStatCard from "../../../../components/ui/UiStatCard.vue"
import UiSurfaceSection from "../../../../components/ui/UiSurfaceSection.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import UiWorkspace from "../../../../components/ui/UiWorkspace.vue"
import type {AppUser} from "../../../workmarket/api/workmarketApi.ts"
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
  (event: "editProfile"): void
}>()

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
  editProfile: () => emit("editProfile"),
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

watch(() => [props.open, props.userId] as const, () => {
  if (!props.open) {
    clearProfile()
    return
  }

  void fetchProfile()
}, {immediate: true})
</script>

<template>
  <UiDialog
    :open="open"
    :title="profile?.username ?? 'Profile'"
    size="xl"
    :default-expanded="true"
    @close="$emit('close')"
  >
    <div class="surface-stack">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

      <div v-if="isLoading" class="empty-state">Loading profile...</div>
      <div v-else-if="error" class="alert alert--error">{{ error }}</div>

      <template v-else-if="profile">
        <UiWorkspace variant="detail">
          <section class="surface-stack surface-stack--content">
            <ProfileSummaryCard
              :username="profile.username"
              :avatar-data-url="profile.profileAvatarDataUrl"
              :badge-label="!isOwnProfile ? `${profile.openQuestCount} open` : undefined"
              :role="profile.role"
              :email="profile.email"
              :description="profile.profileDescription"
              description-placeholder="This user has not added a profile description yet."
              :size="108"
            >
              <template #actions>
                <div v-if="!isOwnProfile && primaryAction" class="button-row mt-4">
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
                <div v-else-if="isOwnProfile && primaryAction" class="button-row mt-4">
                  <button class="button" type="button" :disabled="isSaving || !primaryAction.enabled" @click="handlePrimaryAction">
                    {{ primaryAction.label }}
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

            <UiSurfaceSection compact eyebrow="Open jobs you can apply for">
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

          <aside class="surface-stack surface-stack--aside">
            <UiSurfaceSection compact eyebrow="Overview">
              <UiInfoGrid :columns="1">
                <UiStatCard label="Open jobs" :value="profile.openQuestCount" />
                <UiStatCard label="As employer" :value="formatUserRatingSummary(employerRating)" :hint="`${employerRating.reviewCount} reviews`" />
                <UiStatCard label="As worker" :value="formatUserRatingSummary(workerRating)" :hint="`${workerRating.reviewCount} reviews`" />
              </UiInfoGrid>
            </UiSurfaceSection>
          </aside>
        </UiWorkspace>
      </template>
    </div>
  </UiDialog>
</template>
