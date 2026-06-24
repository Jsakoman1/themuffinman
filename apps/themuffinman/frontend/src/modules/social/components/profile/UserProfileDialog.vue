<script setup lang="ts">
import {watch} from "vue"
import {useRouter} from "vue-router"
import UiDialog from "../../../../components/ui/UiDialog.vue"
import UiStatusBanner from "../../../../components/ui/UiStatusBanner.vue"
import ProfileAvatar from "../../../../components/profile/ProfileAvatar.vue"
import ProfileBio from "../../../../components/profile/ProfileBio.vue"
import type {AppUser} from "../../../workmarket/api/workmarketApi.ts"
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
    :subtitle="isOwnProfile ? 'Your public profile' : 'Public profile'"
    size="lg"
    @close="$emit('close')"
  >
    <div class="stack">
      <UiStatusBanner :message="bannerMessage" :tone="bannerTone" />

      <div v-if="isLoading" class="empty-state">Loading profile...</div>
      <div v-else-if="error" class="alert alert--error">{{ error }}</div>

      <template v-else-if="profile">
        <section class="profile-dialog-card">
          <div class="profile-dialog-card__header">
            <ProfileAvatar :username="profile.username" :avatar-data-url="profile.profileAvatarDataUrl" :size="88" />

            <div class="profile-dialog-card__identity">
              <div class="profile-dialog-card__name-row">
                <h3 class="profile-dialog-card__name">{{ profile.username }}</h3>
                <span v-if="!isOwnProfile" class="badge badge--secondary">{{ profile.openQuestCount }} open</span>
              </div>
              <div v-if="!isOwnProfile" class="profile-dialog-card__meta">{{ profile.role }}</div>
              <div v-else class="profile-dialog-card__meta">{{ profile.email }}</div>
            </div>
          </div>

          <ProfileBio :text="profile.profileDescription" placeholder="No profile description yet." />

          <div class="profile-dialog-card__actions">
            <button class="button" type="button" :disabled="isSaving || !primaryAction?.enabled" @click="handlePrimaryAction">
              {{ primaryAction?.label ?? "Edit profile" }}
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
        </section>

        <section v-if="profile.openQuests.length" class="stack dialog-sheet__section">
          <div class="dialog-sheet__section-title">Open jobs</div>
          <button
            v-for="quest in profile.openQuests.slice(0, 4)"
            :key="quest.id"
            type="button"
            class="profile-dialog-quest"
            @click="openQuest(quest)"
          >
            <div class="profile-dialog-quest__main">
              <strong>{{ quest.title }}</strong>
              <span class="muted">{{ quest.presentation.termLabel }}</span>
            </div>
            <span :class="quest.presentation.statusBadgeClass">{{ quest.presentation.statusLabel }}</span>
          </button>
        </section>
      </template>
    </div>
  </UiDialog>
</template>
