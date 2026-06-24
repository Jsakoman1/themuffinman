import {computed} from "vue"
import {useRoute, useRouter} from "vue-router"
import {buildRequestDebugInfo, formatDebugInfo} from "../../../../httpDebug.ts"
import {API_BASE_URL} from "../../../../api/httpClient.ts"
import {useTimedBanner} from "../../../../composables/useTimedBanner.ts"

export const createQuestDetailDebugState = (state: {errorDetails: {value: string[]}}) => {
  const route = useRoute()
  const router = useRouter()
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)
  const questId = computed(() => Number(route.params.id))

  const copyDebugInfo = async () => {
    if (!state.errorDetails.value.length) {
      return
    }

    await navigator.clipboard.writeText(formatDebugInfo(state.errorDetails.value))
    copiedDebugBanner.show("Copied")
  }

  const setNotFoundErrorDetails = (fetchError: unknown) => {
    state.errorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/quests/${questId.value}/detail`, "GET", fetchError)
  }

  return {
    router,
    questId,
    copiedDebug,
    copyDebugInfo,
    setNotFoundErrorDetails
  }
}
