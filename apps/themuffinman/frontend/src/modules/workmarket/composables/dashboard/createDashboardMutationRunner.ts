import {getApiErrorMessage} from "../../../../api/apiErrors.ts"

type MutationState = {
  showFeedback: (message: string, tone: "error" | "success") => void
  triggerSuccessPulse: (target: string) => void
}

type RunMutationOptions<T> = {
  run: () => Promise<T>
  successMessage: string | ((result: T) => string)
  errorMessage: string
  successPulseTarget?: string
  afterSuccess?: (result: T) => Promise<void> | void
}

export const createDashboardMutationRunner = (state: MutationState) => {
  const runMutation = async <T>(options: RunMutationOptions<T>) => {
    try {
      const result = await options.run()

      if (options.successPulseTarget) {
        state.triggerSuccessPulse(options.successPulseTarget)
      }

      state.showFeedback(
        typeof options.successMessage === "function" ? options.successMessage(result) : options.successMessage,
        "success"
      )

      await options.afterSuccess?.(result)
      return result
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, options.errorMessage), "error")
      return null
    }
  }

  return {
    runMutation
  }
}
