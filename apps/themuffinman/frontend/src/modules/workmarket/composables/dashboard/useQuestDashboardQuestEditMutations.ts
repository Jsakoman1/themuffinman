import {QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, QUEST_IMAGE_TOO_LARGE_MESSAGE} from "../../../../shared/clientMessages.ts"
import {compressQuestImageFile} from "../../../../shared/imageCompression.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestMutationContext} from "./questMutationTypes.ts"

export const useQuestDashboardQuestEditMutations = ({
  state,
  helpers,
  runMutation
}: QuestMutationContext) => {
  const saveEditedQuest = async () => {
    if (state.editingQuestId.value === null) {
      return
    }

    const questId = state.editingQuestId.value
    const scheduledAt = state.editQuestScheduledAt.value ? state.parseInstantFromInput(state.editQuestScheduledAt.value) : null
    const endsAt = state.editQuestEndsAt.value ? state.parseInstantFromInput(state.editQuestEndsAt.value) : null

    const result = await runMutation({
      run: () => workmarketApi.updateQuest(questId, {
        title: state.editQuestTitle.value.trim(),
        description: state.editQuestDescription.value.trim(),
        awardAmount: Number(state.editQuestAwardAmount.value),
        scheduledAt,
        endsAt,
        termFixed: state.editQuestTermFixed.value,
        audience: state.editQuestAudience.value,
        selectedCircleIds: state.editQuestAudience.value === "CIRCLES" ? [...state.editQuestSelectedCircleIds.value] : [],
        images: [...state.editQuestImages.value],
        creatorId: state.adminModeEnabled.value && state.editQuestCreatorId.value ? Number(state.editQuestCreatorId.value) : undefined,
        status: state.adminModeEnabled.value ? state.editQuestStatus.value : undefined
      }),
      successMessage: (response) => response.message,
      errorMessage: "Could not update quest.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        state.closeQuestDisclosure(questId)
        state.closeQuestDialog()
        await helpers.refreshDashboardData()
      }
    })

    return result !== null
  }

  const addEditQuestImages = async (files: FileList | null) => {
    if (!files?.length) {
      return
    }

    const slotsLeft = Math.max(0, 10 - state.editQuestImages.value.length)
    if (slotsLeft === 0) {
      state.showFeedback("A quest can have at most 10 images.", "error")
      return
    }

    const selectedFiles = Array.from(files).slice(0, slotsLeft)

    try {
      const compressed = await Promise.all(selectedFiles.map((file) => compressQuestImageFile(file)))
      state.editQuestImages.value = [...state.editQuestImages.value, ...compressed].slice(0, 10)
    } catch (error) {
      state.showFeedback(error instanceof Error && error.message === QUEST_IMAGE_TOO_LARGE_MESSAGE
        ? error.message
        : QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, "error")
    }
  }

  const removeEditQuestImage = (index: number) => {
    state.editQuestImages.value = state.editQuestImages.value.filter((_, currentIndex) => currentIndex !== index)
  }

  const confirmQuestTermChange = async (questId: number) => {
    const result = await runMutation({
      run: () => workmarketApi.confirmQuestTermChange(questId),
      successMessage: "Quest term confirmed.",
      errorMessage: "Could not confirm quest term.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        await helpers.refreshDashboardData()
        await helpers.loadApplicationsForQuest(questId)
      }
    })

    return result !== null
  }

  const rejectQuestTermChange = async (questId: number) => {
    const result = await runMutation({
      run: () => workmarketApi.rejectQuestTermChange(questId),
      successMessage: "Quest term change rejected.",
      errorMessage: "Could not reject quest term.",
      successPulseTarget: `quest-${questId}`,
      afterSuccess: async () => {
        await helpers.refreshDashboardData()
        await helpers.loadApplicationsForQuest(questId)
      }
    })

    return result !== null
  }

  return {
    saveEditedQuest,
    addEditQuestImages,
    removeEditQuestImage,
    confirmQuestTermChange,
    rejectQuestTermChange
  }
}
