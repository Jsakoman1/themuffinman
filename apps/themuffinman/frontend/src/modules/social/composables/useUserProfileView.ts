import {computed, ref, toValue, type MaybeRefOrGetter} from "vue"
import {workmarketApi, type AppUser, type UserProfileView, type UserRatingSummary, type UserReview} from "../../workmarket/api/workmarketApi.ts"
import {createFeedbackMutationRunner} from "../../../composables/createFeedbackMutationRunner.ts"
import {useTimedBanner} from "../../../composables/useTimedBanner.ts"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"

type UseUserProfileViewOptions = {
  userId: MaybeRefOrGetter<number | null | undefined>
  enabled?: MaybeRefOrGetter<boolean>
  loadErrorMessage: string
  invalidErrorMessage?: string
}

export const useUserProfileView = (options: UseUserProfileViewOptions) => {
  const profile = ref<AppUser | null>(null)
  const profileView = ref<UserProfileView | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)
  const error = ref("")
  const circleBanner = useTimedBanner(3500)

  const isOwnProfile = computed(() => profileView.value?.ownProfile ?? false)
  const primaryAction = computed(() => profileView.value?.primaryAction ?? null)
  const showBlockAction = computed(() => profileView.value?.showBlockAction ?? false)
  const blockActionEnabled = computed(() => profileView.value?.blockActionEnabled ?? false)
  const employerRating = computed<UserRatingSummary>(() => profileView.value?.employerRating ?? {averageStars: 0, reviewCount: 0})
  const workerRating = computed<UserRatingSummary>(() => profileView.value?.workerRating ?? {averageStars: 0, reviewCount: 0})
  const recentReviews = computed<UserReview[]>(() => profileView.value?.recentReviews ?? [])
  const bannerMessage = computed(() => circleBanner.message.value)
  const bannerTone = computed(() => circleBanner.tone.value)

  const clearProfile = () => {
    profile.value = null
    profileView.value = null
    error.value = ""
  }

  const currentUserId = () => toValue(options.userId)
  const isEnabled = () => toValue(options.enabled) ?? true

  const showMessage = (message: string, tone: "success" | "warning" = "success") => {
    circleBanner.show(message, tone)
  }
  const {runWithFeedback} = createFeedbackMutationRunner({
    showFeedback: (message, tone) => showMessage(message, tone === "error" ? "warning" : tone)
  })

  const loadProfileView = async () => {
    const userId = currentUserId()
    if (!isEnabled() || userId === null || userId === undefined) {
      return
    }

    if (!Number.isFinite(userId)) {
      error.value = options.invalidErrorMessage ?? options.loadErrorMessage
      profile.value = null
      profileView.value = null
      return
    }

    const response = await workmarketApi.getUserProfileView(userId)
    profileView.value = response
    profile.value = response.profile
  }

  const fetchProfile = async () => {
    if (!isEnabled() || currentUserId() === null || currentUserId() === undefined) {
      clearProfile()
      return
    }

    isLoading.value = true
    error.value = ""

    try {
      await loadProfileView()
    } catch (requestError) {
      profile.value = null
      profileView.value = null
      error.value = getApiErrorMessage(requestError, options.loadErrorMessage)
    } finally {
      isLoading.value = false
    }
  }

  const runProfileMutation = async (
    action: (userId: number) => Promise<{message: string}>,
    fallbackMessage: string
  ) => {
    const userId = profile.value?.id
    if (!userId) {
      return
    }

    isSaving.value = true
    try {
      await runWithFeedback({
        run: () => action(userId),
        successMessage: (result) => result.message,
        errorMessage: fallbackMessage,
        successTone: "success",
        afterSuccess: loadProfileView
      })
    } finally {
      isSaving.value = false
    }
  }

  const sendInvite = async () => runProfileMutation(
    (userId) => workmarketApi.createCircleRequest({recipientId: userId}),
    "Could not send connection invite."
  )

  const blockProfile = async () => runProfileMutation(
    (userId) => workmarketApi.blockCircleUser({blockedUserId: userId}),
    "Could not block user."
  )

  const unblockProfile = async () => runProfileMutation(
    (userId) => workmarketApi.unblockCircleUser(userId),
    "Could not unblock user."
  )

  return {
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
    loadProfileView,
    sendInvite,
    blockProfile,
    unblockProfile
  }
}
