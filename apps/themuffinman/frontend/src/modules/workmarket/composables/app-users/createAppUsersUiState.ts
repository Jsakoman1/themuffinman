import {computed, ref} from "vue"
import {useAutoDismissFeedback} from "../../../../composables/useAutoDismissFeedback.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createAppUsersUiState = () => {
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)
  const feedbackState = useAutoDismissFeedback<"error" | "success">(5000, "success")
  const feedback = feedbackState.message
  const feedbackType = feedbackState.tone
  const isCreateUserDialogOpen = ref(false)
  const deleteCandidateUserId = ref<number | null>(null)
  const showFeedback = feedbackState.show

  const showCopiedDebug = () => {
    copiedDebugBanner.show("Copied")
  }

  const openCreateUserDialog = () => {
    isCreateUserDialogOpen.value = true
  }

  const closeCreateUserDialog = () => {
    isCreateUserDialogOpen.value = false
  }

  const openDeleteUserDialog = (userId: number) => {
    deleteCandidateUserId.value = userId
  }

  const closeDeleteUserDialog = () => {
    deleteCandidateUserId.value = null
  }

  return {
    copiedDebug,
    feedback,
    feedbackType,
    isCreateUserDialogOpen,
    deleteCandidateUserId,
    showFeedback,
    showCopiedDebug,
    openCreateUserDialog,
    closeCreateUserDialog,
    openDeleteUserDialog,
    closeDeleteUserDialog
  }
}
