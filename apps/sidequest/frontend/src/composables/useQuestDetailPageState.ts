import {computed, ref} from "vue"
import {useRoute, useRouter} from "vue-router"
import {buildRequestDebugInfo, formatDebugInfo} from "../httpDebug.ts"
import {API_BASE_URL} from "../api/httpClient.ts"
import {type Quest, type QuestApplication, type QuestApplicationsView, type UserReview} from "../api/sidequestApi.ts"
import {useTimedBanner} from "./useTimedBanner.ts"

export const useQuestDetailPageState = () => {
  const route = useRoute()
  const router = useRouter()

  const quest = ref<Quest | null>(null)
  const myApplication = ref<QuestApplication | null>(null)
  const applications = ref<QuestApplication[]>([])
  const applicationsView = ref<QuestApplicationsView | null>(null)
  const review = ref<UserReview | null>(null)
  const isLoading = ref(false)
  const error = ref("")
  const errorDetails = ref<string[]>([])
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)
  const isSaving = ref(false)

  const questId = computed(() => Number(route.params.id))

  const copyDebugInfo = async () => {
    if (!errorDetails.value.length) {
      return
    }

    await navigator.clipboard.writeText(formatDebugInfo(errorDetails.value))
    copiedDebugBanner.show("Copied")
  }

  const setNotFoundErrorDetails = (fetchError: unknown) => {
    errorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/quests/${questId.value}/detail`, "GET", fetchError)
  }

  return {
    router,
    questId,
    quest,
    myApplication,
    applications,
    applicationsView,
    review,
    isLoading,
    error,
    errorDetails,
    copiedDebug,
    isSaving,
    copyDebugInfo,
    setNotFoundErrorDetails
  }
}

export type QuestDetailPageState = ReturnType<typeof useQuestDetailPageState>
