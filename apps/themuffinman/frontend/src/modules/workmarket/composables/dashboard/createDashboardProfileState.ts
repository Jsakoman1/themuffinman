import {computed, ref} from "vue"
import {currentUser} from "../../../../auth.ts"
import {useAutoDismissFeedback} from "../../../../composables/useAutoDismissFeedback.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createDashboardProfileState = () => {
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)

  const feedbackState = useAutoDismissFeedback<"error" | "success">(5000, "success")
  const feedback = feedbackState.message
  const feedbackType = feedbackState.tone
  const isProfileEditDialogOpen = ref(false)
  const isNotificationsDialogOpen = ref(false)
  const successPulseTarget = ref("")

  const profileUsername = ref("")
  const profileDescription = ref(currentUser.value?.profileDescription ?? "")
  const profileAvatarDataUrl = ref(currentUser.value?.profileAvatarDataUrl ?? "")
  const accountCreatedAt = computed(() => currentUser.value?.createdAt ?? new Date().toISOString())

  return {
    copiedDebugBanner,
    copiedDebug,
    feedback,
    feedbackType,
    showFeedback: feedbackState.show,
    isProfileEditDialogOpen,
    isNotificationsDialogOpen,
    successPulseTarget,
    profileUsername,
    profileDescription,
    profileAvatarDataUrl,
    accountCreatedAt
  }
}
