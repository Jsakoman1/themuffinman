import {createFeedbackMutationRunner} from "../../../../composables/createFeedbackMutationRunner.ts"
import {normalizeSearchQuery} from "../../../../lib/searchQuery.ts"
import {workmarketApi, type ActionResult, type CircleContact} from "../../../workmarket/api/workmarketApi.ts"

type CirclesMutationState = {
  isSaving: {value: boolean}
  showMessage: (text: string, tone?: "success" | "warning") => void
  newCircleName: {value: string}
  activeCircleFilter: {value: number | "all" | "unassigned"}
}

type CirclesMutationHelpers = {
  refreshWorkspace: () => Promise<void>
  refreshOverviewAndData: () => Promise<void>
  getSelectedCircleIds: (connection: CircleContact) => number[]
}

export const useCirclesMutationActions = (
  state: CirclesMutationState,
  helpers: CirclesMutationHelpers
) => {
  const {runWithFeedback} = createFeedbackMutationRunner({
    showFeedback: (message, tone) => {
      state.showMessage(message, tone === "error" ? "warning" : tone)
    }
  })

  const runCircleMutation = async (
    action: () => Promise<ActionResult>,
    fallbackMessage: string,
    options: {
      tone?: "success" | "warning"
      refresh?: () => Promise<void>
      onSuccess?: () => void
    } = {}
  ) => {
    state.isSaving.value = true
    try {
      await runWithFeedback({
        run: action,
        successMessage: (result) => result.message,
        errorMessage: fallbackMessage,
        successTone: options.tone ?? "success",
        afterSuccess: async (result) => {
          void result
          options.onSuccess?.()
          await (options.refresh ?? helpers.refreshOverviewAndData)()
        }
      })
    } finally {
      state.isSaving.value = false
    }
  }

  const createCircle = async () => {
    await runCircleMutation(
      () => workmarketApi.createCircle({name: normalizeSearchQuery(state.newCircleName.value)}),
      "Could not create circle.",
      {
        refresh: helpers.refreshWorkspace,
        onSuccess: () => {
          state.newCircleName.value = ""
        }
      }
    )
  }

  const deleteCircle = async (circleId: number) => {
    await runCircleMutation(
      () => workmarketApi.deleteCircle(circleId),
      "Could not delete circle.",
      {
        refresh: helpers.refreshWorkspace,
        onSuccess: () => {
          if (state.activeCircleFilter.value === circleId) {
            state.activeCircleFilter.value = "all"
          }
        }
      }
    )
  }

  const saveConnectionCircles = async (connection: CircleContact) => {
    const nextCircleIds = helpers.getSelectedCircleIds(connection)
    await runCircleMutation(
      () => workmarketApi.updateConnectionCircles(connection.userId, {circleIds: nextCircleIds}),
      "Could not update circles.",
      {refresh: helpers.refreshWorkspace}
    )
  }

  const sendRequest = async (id: number) => {
    await runCircleMutation(() => workmarketApi.createCircleRequest({recipientId: id}), "Could not send invite.")
  }

  const blockUser = async (id: number) => {
    await runCircleMutation(() => workmarketApi.blockCircleUser({blockedUserId: id}), "Could not block user.")
  }

  const unblockUser = async (id: number) => {
    await runCircleMutation(() => workmarketApi.unblockCircleUser(id), "Could not unblock user.")
  }

  const acceptRequest = async (requestId: number) => {
    await runCircleMutation(() => workmarketApi.acceptCircleRequest(requestId), "Could not accept invite.")
  }

  const removeRequest = async (requestId: number, tone: "success" | "warning" = "warning") => {
    await runCircleMutation(
      () => workmarketApi.deleteCircleRequest(requestId),
      "Could not update connection.",
      {tone}
    )
  }

  return {
    createCircle,
    deleteCircle,
    saveConnectionCircles,
    sendRequest,
    blockUser,
    unblockUser,
    acceptRequest,
    removeRequest
  }
}
