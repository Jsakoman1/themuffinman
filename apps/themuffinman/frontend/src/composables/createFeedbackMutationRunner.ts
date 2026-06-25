import {getApiErrorMessage} from "../api/apiErrors.ts"

type FeedbackMutationState = {
  showFeedback: (message: string, tone: "success" | "error" | "warning") => void
}

type FeedbackMutationOptions<T> = {
  run: () => Promise<T>
  successMessage: string | ((result: T) => string)
  errorMessage: string
  successTone?: "success" | "error" | "warning"
  afterSuccess?: (result: T) => Promise<void> | void
}

export const createFeedbackMutationRunner = (state: FeedbackMutationState) => {
  const runWithFeedback = async <T>(options: FeedbackMutationOptions<T>) => {
    try {
      const result = await options.run()
      state.showFeedback(
        typeof options.successMessage === "function" ? options.successMessage(result) : options.successMessage,
        options.successTone ?? "success"
      )
      await options.afterSuccess?.(result)
      return result
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, options.errorMessage), "error")
      return null
    }
  }

  return {
    runWithFeedback
  }
}
