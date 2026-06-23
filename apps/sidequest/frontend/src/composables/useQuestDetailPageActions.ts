import {sidequestApi} from "../api/sidequestApi.ts"
import type {QuestDetailPageState} from "./useQuestDetailPageState.ts"

export const useQuestDetailPageActions = (state: QuestDetailPageState) => {
  const fetchQuestDetail = async () => {
    state.isLoading.value = true
    state.error.value = ""
    state.errorDetails.value = []

    try {
      const detail = await sidequestApi.getQuestDetail(state.questId.value)
      state.quest.value = detail.summary
      state.myApplication.value = detail.sections.myApplication
      state.applicationsView.value = detail.sections.applicationsView
      state.applications.value = detail.sections.applicationsView?.visibleApplications ?? []
      state.review.value = null
    } catch (error) {
      state.quest.value = null
      state.myApplication.value = null
      state.applications.value = []
      state.applicationsView.value = null
      state.review.value = null
      state.error.value = "Quest not found."
      state.setNotFoundErrorDetails(error)
    } finally {
      state.isLoading.value = false
    }
  }

  const updateStatus = async (action: "start" | "complete") => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      state.quest.value = action === "start"
        ? await sidequestApi.startQuest(state.questId.value)
        : await sidequestApi.completeQuest(state.questId.value)
    } catch {
      state.error.value = "Could not update quest."
    } finally {
      state.isSaving.value = false
    }
  }

  const confirmQuestTermChange = async () => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      state.quest.value = await sidequestApi.confirmQuestTermChange(state.questId.value)
      return true
    } catch {
      state.error.value = "Could not confirm quest term."
      return false
    } finally {
      state.isSaving.value = false
    }
  }

  const rejectQuestTermChange = async () => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      state.quest.value = await sidequestApi.rejectQuestTermChange(state.questId.value)
      return true
    } catch {
      state.error.value = "Could not reject quest term."
      return false
    } finally {
      state.isSaving.value = false
    }
  }

  const deleteQuest = async (): Promise<boolean> => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      await sidequestApi.deleteQuest(state.questId.value)
      return true
    } catch {
      state.error.value = "Could not delete quest."
      return false
    } finally {
      state.isSaving.value = false
    }
  }

  const submitReview = async (reviewedUserId: number, stars: number, comment: string) => {
    state.isSaving.value = true
    state.error.value = ""

    try {
      state.review.value = await sidequestApi.createQuestReview(state.questId.value, {
        reviewedUserId,
        stars,
        comment
      })
      return true
    } catch {
      state.error.value = "Could not save review."
      return false
    } finally {
      state.isSaving.value = false
    }
  }

  const init = async () => {
    await fetchQuestDetail()
  }

  return {
    fetchQuestDetail,
    updateStatus,
    confirmQuestTermChange,
    rejectQuestTermChange,
    deleteQuest,
    submitReview,
    init
  }
}
