<script setup lang="ts">
import {computed, ref, watch} from "vue"
import {useRouter} from "vue-router"
import {sidequestApi, type AppUser, type UserProfileView} from "../../api/sidequestApi.ts"
import UiDialog from "../ui/UiDialog.vue"
import UiStatusBanner from "../ui/UiStatusBanner.vue"
import ProfileAvatar from "./ProfileAvatar.vue"
import ProfileBio from "./ProfileBio.vue"
import {useTimedBanner} from "../../composables/useTimedBanner.ts"
import {formatQuestTerm} from "../../shared/questSchedule.ts"

const props = defineProps<{
  open: boolean
  userId: number | null
}>()
const router = useRouter()

const emit = defineEmits<{
  (event: "close"): void
  (event: "openQuest", questId: number): void
  (event: "editProfile"): void
}>()

const profile = ref<AppUser | null>(null)
const profileView = ref<UserProfileView | null>(null)
const isLoading = ref(false)
const isSending = ref(false)
const error = ref("")
const circleBanner = useTimedBanner(3500)

const isOwnProfile = computed(() => profileView.value?.ownProfile ?? false)
const primaryAction = computed(() => profileView.value?.primaryAction ?? null)
const showBlockAction = computed(() => profileView.value?.showBlockAction ?? false)
const blockActionEnabled = computed(() => profileView.value?.blockActionEnabled ?? false)
const bannerMessage = computed(() => circleBanner.message.value)
const bannerTone = computed(() => circleBanner.tone.value)

const showMessage = (message: string, tone: "success" | "warning" = "success") => {
  circleBanner.show(message, tone)
}

const loadProfileView = async () => {
  if (!props.userId) {
    return
  }

  const response = await sidequestApi.getUserProfileView(props.userId)
  profileView.value = response
  profile.value = response.profile
}

const loadProfile = async () => {
  if (!props.open || !props.userId) {
    return
  }

  isLoading.value = true
  error.value = ""

  try {
    await loadProfileView()
  } catch {
    profile.value = null
    profileView.value = null
    error.value = "Could not load profile."
  } finally {
    isLoading.value = false
  }
}

const sendInvite = async () => {
  if (!profile.value) {
    return
  }

  isSending.value = true
  try {
    await sidequestApi.createCircleRequest({recipientId: profile.value.id})
    showMessage("Connection invite sent.")
    await loadProfileView()
  } catch {
    showMessage("Could not send connection invite.", "warning")
  } finally {
    isSending.value = false
  }
}

const blockProfile = async () => {
  if (!profile.value) {
    return
  }

  isSending.value = true
  try {
    await sidequestApi.blockCircleUser({blockedUserId: profile.value.id})
    showMessage("User blocked.")
    await loadProfileView()
  } catch {
    showMessage("Could not block user.", "warning")
  } finally {
    isSending.value = false
  }
}

const unblockProfile = async () => {
  if (!profile.value) {
    return
  }

  isSending.value = true
  try {
    await sidequestApi.unblockCircleUser(profile.value.id)
    showMessage("User unblocked.")
    await loadProfileView()
  } catch {
    showMessage("Could not unblock user.", "warning")
  } finally {
    isSending.value = false
  }
}

const handlePrimaryAction = () => {
  switch (primaryAction.value?.type) {
    case "EDIT_PROFILE":
      emit("editProfile")
      return
    case "UNBLOCK":
      void unblockProfile()
      return
    case "OPEN_CIRCLES":
      emit("close")
      void router.push("/circles")
      return
    case "SEND_INVITE":
      void sendInvite()
      return
    default:
      return
  }
}

watch(() => [props.open, props.userId] as const, () => {
  if (!props.open) {
    profile.value = null
    profileView.value = null
    error.value = ""
    return
  }

  void loadProfile()
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
            <button class="button" type="button" :disabled="isSending || !primaryAction?.enabled" @click="handlePrimaryAction">
              {{ primaryAction?.label ?? "Edit profile" }}
            </button>
            <button
              v-if="showBlockAction"
              class="button button--secondary"
              type="button"
              :disabled="isSending || !blockActionEnabled"
              @click="blockProfile"
            >
              Block
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
            @click="$emit('openQuest', quest.id)"
          >
            <div class="profile-dialog-quest__main">
              <strong>{{ quest.title }}</strong>
              <span class="muted">{{ formatQuestTerm(quest.scheduledAt, quest.endsAt, quest.termFixed) }}</span>
            </div>
            <span class="badge badge--accent">$ {{ quest.awardAmount }}</span>
          </button>
        </section>
      </template>
    </div>
  </UiDialog>
</template>
