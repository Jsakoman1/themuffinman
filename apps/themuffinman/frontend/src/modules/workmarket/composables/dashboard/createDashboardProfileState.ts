import {computed, ref} from "vue"
import {useAutoDismissFeedback} from "../../../../composables/useAutoDismissFeedback.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createDashboardProfileState = () => {
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)

  const feedbackState = useAutoDismissFeedback<"error" | "success">(5000, "success")
  const feedback = feedbackState.message
  const feedbackType = feedbackState.tone
  const successPulseTarget = ref("")

  return {
    copiedDebugBanner,
    copiedDebug,
    feedback,
    feedbackType,
    showFeedback: feedbackState.show,
    successPulseTarget
  }
}
