import {computed, type Ref} from "vue"
import type {
  Quest,
  QuestApplication,
  QuestApplicationDetail,
  QuestDetail,
  QuestApplicationsView
} from "../../api/workmarketApi.ts"

export const createDashboardSelectors = (state: {
  quests: Ref<Quest[]>
  myApplications: Ref<QuestApplication[]>
  applicationsByQuestId: Ref<Record<number, QuestApplicationsView>>
  questDetailsById: Ref<Record<number, QuestDetail>>
  applicationDetailsById: Ref<Record<number, QuestApplicationDetail>>
  questDialogId: Ref<number | null>
  applicationDialogId: Ref<number | null>
}) => {
  const applicationsViewForQuest = (questId: number) => (
    state.applicationsByQuestId.value[questId]
    ?? state.questDetailsById.value[questId]?.applicationsView
    ?? null
  )
  const applicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.visibleApplications ?? []
  const approvedApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.approvedApplications ?? []
  const questForId = (questId: number) => state.quests.value.find((quest) => quest.id === questId) ?? null

  const selectedQuestDialog = computed(() => {
    if (state.questDialogId.value === null) {
      return null
    }

    return questForId(state.questDialogId.value)
      ?? state.questDetailsById.value[state.questDialogId.value]?.quest
      ?? null
  })

  const selectedQuestDetail = computed(() => {
    if (state.questDialogId.value === null) {
      return null
    }

    return state.questDetailsById.value[state.questDialogId.value] ?? null
  })

  const selectedApplicationDialog = computed(() => {
    if (state.applicationDialogId.value === null) {
      return null
    }

    return state.myApplications.value.find((application) => application.id === state.applicationDialogId.value) ?? null
  })

  const selectedApplicationDetail = computed(() => {
    if (state.applicationDialogId.value === null) {
      return null
    }

    return state.applicationDetailsById.value[state.applicationDialogId.value] ?? null
  })

  const hiddenApplicationsCountForQuest = (questId: number) => applicationsViewForQuest(questId)?.hiddenApplicationsCount ?? 0
  const canRevealHiddenApplicationsForQuest = (questId: number) => applicationsViewForQuest(questId)?.canRevealHiddenApplications ?? false
  const applicationRevealLabel = (questId: number) => applicationsViewForQuest(questId)?.revealLabel ?? "Show applications"
  return {
    applicationsViewForQuest,
    applicationsForQuest,
    approvedApplicationsForQuest,
    questForId,
    selectedQuestDialog,
    selectedQuestDetail,
    selectedApplicationDialog,
    selectedApplicationDetail,
    hiddenApplicationsCountForQuest,
    canRevealHiddenApplicationsForQuest,
    applicationRevealLabel
  }
}
