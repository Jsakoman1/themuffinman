import {currentUser} from "../../../../auth.ts"
import {compressQuestImageFile} from "../../../../shared/imageCompression.ts"
import {QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, QUEST_IMAGE_TOO_LARGE_MESSAGE} from "../../../../shared/clientMessages.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import {resetCreateQuestDraft} from "./questDraftState.ts"
import type {QuestMutationContext} from "./questMutationTypes.ts"

export const useQuestDashboardQuestDraftMutations = ({
  state,
  helpers,
  runMutation
}: QuestMutationContext) => {
  const createQuest = async () => {
    if (!currentUser.value) {
      state.showFeedback("You must be signed in to create a quest.", "error")
      return
    }

    const scheduledAt = state.questScheduledAt.value ? state.parseInstantFromInput(state.questScheduledAt.value) : null
    const endsAt = state.questEndsAt.value ? state.parseInstantFromInput(state.questEndsAt.value) : null

    const result = await runMutation({
      run: () => workmarketApi.createQuest({
        title: state.questTitle.value.trim(),
        description: state.questDescription.value,
        awardAmount: Number(state.questAwardAmount.value),
        assigneeTarget: Number(state.questAssigneeTarget.value || "1"),
        showApprovedApplicants: state.questShowApprovedApplicants.value,
        scheduledAt,
        endsAt,
        termFixed: state.questTermFixed.value,
        audience: state.questAudience.value,
        selectedCircleIds: state.questAudience.value === "CIRCLES" ? [...state.questSelectedCircleIds.value] : [],
        locationVisibility: state.questLocationVisibility.value,
        locationSource: state.questLocationSource.value,
        locationCountry: state.questLocationCountry.value || null,
        locationLocality: state.questLocationLocality.value || null,
        locationPostalCode: state.questLocationPostalCode.value || null,
        locationStreet: state.questLocationStreet.value || null,
        locationHouseNumber: state.questLocationHouseNumber.value || null,
        creatorId: state.adminModeEnabled.value && state.questCreatorId.value ? Number(state.questCreatorId.value) : undefined,
        images: [...state.questImages.value]
      }),
      successMessage: (response) => response.message,
      errorMessage: "Could not create quest.",
      successPulseTarget: "side-job",
      afterSuccess: async () => {
        resetCreateQuestDraft(state, state.adminModeEnabled.value)
        state.closeCreateJobDialog()
        await helpers.refreshDashboardData()
      }
    })

    return result !== null
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
      const compressed = await Promise.all(selectedFiles.map((file) => compressQuestImageFile(file)))
      state.questImages.value = [...state.questImages.value, ...compressed].slice(0, 10)
    } catch (error) {
      state.showFeedback(error instanceof Error && error.message === QUEST_IMAGE_TOO_LARGE_MESSAGE
        ? error.message
        : QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, "error")
    }
  }

  const removeQuestImage = (index: number) => {
    state.questImages.value = state.questImages.value.filter((_, currentIndex) => currentIndex !== index)
  }

  const init = async () => {
    if (state.adminModeEnabled.value && currentUser.value) {
      state.questCreatorId.value = String(currentUser.value.id)
    }

    await helpers.refreshDashboardData()
  }

  return {
    createQuest,
    addQuestImages,
    removeQuestImage,
    init
  }
}
