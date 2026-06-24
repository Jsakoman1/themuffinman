import {currentUser} from "../../../../auth.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {compressImageFile} from "../../../../shared/imageCompression.ts"
import {QUEST_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../../shared/clientMessages.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardQuestMutations = (
  state: QuestDashboardState,
  helpers: {
    refreshDashboardData: () => Promise<void>
    loadApplicationsForQuest: (questId: number) => Promise<void>
  }
) => {
  const {refreshDashboardData, loadApplicationsForQuest} = helpers

  const createQuest = async () => {
    if (!currentUser.value) {
      state.showFeedback("You must be signed in to create a quest.", "error")
      return
    }

    const scheduledAt = state.questScheduledAt.value ? state.parseInstantFromInput(state.questScheduledAt.value) : null
    const endsAt = state.questEndsAt.value ? state.parseInstantFromInput(state.questEndsAt.value) : null

    try {
      const result = await workmarketApi.createQuest({
        title: state.questTitle.value.trim(),
        description: state.questDescription.value.trim(),
        awardAmount: Number(state.questAwardAmount.value),
        scheduledAt,
        endsAt,
        termFixed: state.questTermFixed.value,
        audience: state.questAudience.value,
        selectedCircleIds: state.questAudience.value === "CIRCLES" ? [...state.questSelectedCircleIds.value] : [],
        creatorId: state.adminModeEnabled.value && state.questCreatorId.value ? Number(state.questCreatorId.value) : undefined,
        images: [...state.questImages.value]
      })

      state.questTitle.value = ""
      state.questDescription.value = ""
      state.questAwardAmount.value = ""
      state.questScheduledAt.value = ""
      state.questEndsAt.value = ""
      state.questTermMode.value = "flexible"
      state.questTermFixed.value = false
      state.questAudience.value = "CIRCLES"
      state.questSelectedCircleIds.value = []
      state.questImages.value = []
      if (state.adminModeEnabled.value && currentUser.value) {
        state.questCreatorId.value = String(currentUser.value.id)
      }
      state.triggerSuccessPulse("create-job")
      state.showFeedback(result.message, "success")
      state.closeCreateJobDialog()
      await refreshDashboardData()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not create quest."), "error")
    }
  }

  const addQuestImages = async (files: FileList | null) => {
    if (!files?.length) {
      return
    }

    const slotsLeft = Math.max(0, 10 - state.questImages.value.length)
    if (slotsLeft === 0) {
      state.showFeedback("A quest can have at most 10 images.", "error")
      return
    }

    const selectedFiles = Array.from(files).slice(0, slotsLeft)
    try {
      const compressed = await Promise.all(selectedFiles.map((file) => compressImageFile(file, 1400, 0.84)))
      state.questImages.value = [...state.questImages.value, ...compressed].slice(0, 10)
    } catch {
      state.showFeedback(QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, "error")
    }
  }

  const removeQuestImage = (index: number) => {
    state.questImages.value = state.questImages.value.filter((_, currentIndex) => currentIndex !== index)
  }

  const updateQuestStatus = async (questId: number, action: "start" | "complete") => {
    try {
      const result = await (action === "start" ? workmarketApi.startQuest(questId) : workmarketApi.completeQuest(questId))
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback(result.message, "success")
      await refreshDashboardData()
      return true
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, `Could not ${action} quest.`), "error")
      return false
    }
  }

  const deleteQuest = async (questId: number) => {
    try {
      const result = await workmarketApi.deleteQuest(questId)
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback(result.message, "success")
      await refreshDashboardData()
      return true
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not delete quest."), "error")
      return false
    }
  }

  const saveEditedQuest = async () => {
    if (state.editingQuestId.value === null) {
      return
    }

    const questId = state.editingQuestId.value
    const scheduledAt = state.editQuestScheduledAt.value ? state.parseInstantFromInput(state.editQuestScheduledAt.value) : null
    const endsAt = state.editQuestEndsAt.value ? state.parseInstantFromInput(state.editQuestEndsAt.value) : null

    try {
      const result = await workmarketApi.updateQuest(questId, {
        title: state.editQuestTitle.value.trim(),
        description: state.editQuestDescription.value.trim(),
        awardAmount: Number(state.editQuestAwardAmount.value),
        scheduledAt,
        endsAt,
        termFixed: state.editQuestTermFixed.value,
        audience: state.editQuestAudience.value,
        selectedCircleIds: state.editQuestAudience.value === "CIRCLES" ? [...state.editQuestSelectedCircleIds.value] : [],
        creatorId: state.adminModeEnabled.value && state.editQuestCreatorId.value ? Number(state.editQuestCreatorId.value) : undefined,
        status: state.adminModeEnabled.value ? state.editQuestStatus.value : undefined
      })

      state.closeQuestDisclosure(questId)
      state.closeQuestDialog()
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback(result.message, "success")
      await refreshDashboardData()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not update quest."), "error")
    }
  }

  const confirmQuestTermChange = async (questId: number) => {
    try {
      await workmarketApi.confirmQuestTermChange(questId)
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback("Quest term confirmed.", "success")
      await Promise.all([refreshDashboardData(), loadApplicationsForQuest(questId)])
      return true
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not confirm quest term."), "error")
      return false
    }
  }

  const rejectQuestTermChange = async (questId: number) => {
    try {
      await workmarketApi.rejectQuestTermChange(questId)
      state.triggerSuccessPulse(`quest-${questId}`)
      state.showFeedback("Quest term change rejected.", "success")
      await Promise.all([refreshDashboardData(), loadApplicationsForQuest(questId)])
      return true
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not reject quest term."), "error")
      return false
    }
  }

  const init = async () => {
    if (state.adminModeEnabled.value && currentUser.value) {
      state.questCreatorId.value = String(currentUser.value.id)
    }

    await refreshDashboardData()
  }

  return {
    createQuest,
    addQuestImages,
    removeQuestImage,
    updateQuestStatus,
    deleteQuest,
    saveEditedQuest,
    confirmQuestTermChange,
    rejectQuestTermChange,
    init
  }
}
