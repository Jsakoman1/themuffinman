import {ref} from "vue"
import type {Quest, QuestApplication, QuestApplicationsView, QuestDetail, UserReview} from "../../api/workmarketApi.ts"

export const createQuestDetailDataState = () => {
  const detail = ref<QuestDetail | null>(null)
  const quest = ref<Quest | null>(null)
  const myApplication = ref<QuestApplication | null>(null)
  const applications = ref<QuestApplication[]>([])
  const applicationsView = ref<QuestApplicationsView | null>(null)
  const review = ref<UserReview | null>(null)
  const isLoading = ref(false)
  const isSaving = ref(false)
  const error = ref("")
  const errorDetails = ref<string[]>([])

  return {
    detail,
    quest,
    myApplication,
    applications,
    applicationsView,
    review,
    isLoading,
    isSaving,
    error,
    errorDetails
  }
}
