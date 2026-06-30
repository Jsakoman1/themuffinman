import {computed, onUnmounted, ref} from "vue"
import {useRouter} from "vue-router"
import {dashboardApi} from "../../workmarket/api/clients/dashboardApi.ts"
import type {DashboardResponse, DashboardVoiceConfig, Quest} from "../../workmarket/api/contracts.ts"
import {formatQuestReward} from "../../workmarket/shared/pricing.ts"
import {formatQuestTermForDisplay} from "../../../shared/questSchedule.ts"
import {routeForNavigationTarget} from "../../workmarket/shared/navigationTargets.ts"

export type VisionVoiceState = "idle" | "listening" | "processing" | "speaking"
export type VisionSurfaceMode = "browse" | "compare" | "focus"
export type VisionFilter = "best-match" | "today" | "nearby"

export type VisionQuestCard = {
  id: number
  title: string
  payoff: string
  distance: string
  schedule: string
  trust: string
  tags: string[]
  tone: "coral" | "sky" | "mint"
  quest: Quest
}

type BrowserSpeechRecognition = {
  continuous: boolean
  interimResults: boolean
  lang: string
  maxAlternatives: number
  onstart: (() => void) | null
  onend: (() => void) | null
  onerror: ((event: {error?: string}) => void) | null
  onresult: ((event: {
    resultIndex: number
    results: ArrayLike<ArrayLike<{transcript: string}> & {isFinal?: boolean}>
  }) => void) | null
  start: () => void
  stop: () => void
}

type BrowserSpeechRecognitionConstructor = new () => BrowserSpeechRecognition

declare global {
  interface Window {
    webkitSpeechRecognition?: BrowserSpeechRecognitionConstructor
    SpeechRecognition?: BrowserSpeechRecognitionConstructor
  }
}

const tones: VisionQuestCard["tone"][] = ["coral", "sky", "mint"]

const buildQuestTags = (quest: Quest) => {
  const tags: string[] = []

  if (quest.images?.length) {
    tags.push("with photos")
  }

  if (!quest.termFixed) {
    tags.push("flexible")
  }

  if (quest.locationLabel?.toLowerCase().includes("remote")) {
    tags.push("remote")
  }

  if (quest.presentation.locationLabel && tags.length < 3) {
    tags.push("local")
  }

  if (quest.audience === "CIRCLES") {
    tags.push("trusted")
  }

  return tags.slice(0, 3)
}

const buildQuestTrust = (quest: Quest) => {
  if (quest.audience === "CIRCLES") {
    return "Circle-scoped opportunity"
  }

  if (quest.presentation.locationLabel) {
    return "Location-aware match"
  }

  return "Open work match"
}

const buildQuestDistance = (quest: Quest) => {
  return quest.presentation.locationLabel ?? quest.locationLabel ?? "Location on request"
}

const buildCards = (response: DashboardResponse): VisionQuestCard[] => {
  return response.availableQuests.map((quest, index) => ({
    id: quest.id,
    title: quest.title,
    payoff: formatQuestReward(quest.awardAmount),
    distance: buildQuestDistance(quest),
    schedule: formatQuestTermForDisplay(quest.scheduledAt, quest.endsAt, quest.termFixed),
    trust: buildQuestTrust(quest),
    tags: buildQuestTags(quest),
    tone: tones[index % tones.length],
    quest
  }))
}

export const useVisionSurface = () => {
  const router = useRouter()

  const isLoading = ref(true)
  const error = ref("")
  const dashboard = ref<DashboardResponse | null>(null)
  const voiceConfig = ref<DashboardVoiceConfig | null>(null)
  const voiceRuntimeError = ref("")

  const voiceState = ref<VisionVoiceState>("idle")
  const surfaceMode = ref<VisionSurfaceMode>("browse")
  const showAllResults = ref(false)
  const activeFilter = ref<VisionFilter>("best-match")
  const recognizedPrompt = ref("Find me a calm side job for today, close to home, no late-night shifts.")
  const speechSummary = ref("")

  const recognition = ref<BrowserSpeechRecognition | null>(null)
  const activeSpeechUtterance = ref<SpeechSynthesisUtterance | null>(null)

  const voiceEnabled = computed(() => voiceConfig.value?.enabled ?? false)
  const speechToTextEnabled = computed(() => voiceConfig.value?.speechToTextEnabled ?? false)
  const textToSpeechEnabled = computed(() => voiceConfig.value?.textToSpeechEnabled ?? false)
  const speechLocale = computed(() => voiceConfig.value?.preferredLocale || navigator.language || "en-US")
  const speechRecognitionSupported = computed(() => typeof window !== "undefined"
    && !!(window.SpeechRecognition || window.webkitSpeechRecognition))
  const speechSynthesisSupported = computed(() => typeof window !== "undefined"
    && "speechSynthesis" in window
    && typeof SpeechSynthesisUtterance !== "undefined")

  const cards = computed(() => {
    if (!dashboard.value) {
      return []
    }

    return buildCards(dashboard.value)
  })

  const filteredCards = computed(() => {
    const base = cards.value.filter((card) => {
      if (activeFilter.value === "today") {
        return card.quest.scheduledAt !== null
      }

      if (activeFilter.value === "nearby") {
        return !card.distance.toLowerCase().includes("remote")
      }

      return true
    })

    return showAllResults.value ? base : base.slice(0, 3)
  })

  const visibleJobsLabel = computed(() => {
    return dashboard.value?.summary.openQuestCount ?? filteredCards.value.length
  })

  const speechStatusLabel = computed(() => {
    if (!voiceEnabled.value) {
      return "Backend voice config is disabled."
    }

    if (!speechRecognitionSupported.value && !speechSynthesisSupported.value) {
      return "This browser does not expose speech recognition or synthesis."
    }

    if (voiceRuntimeError.value) {
      return voiceRuntimeError.value
    }

    if (voiceState.value === "listening") {
      return "Listening through browser speech recognition."
    }

    if (voiceState.value === "processing") {
      return "Turning speech into a useful visual summary."
    }

    if (voiceState.value === "speaking") {
      return "Reading the current result summary aloud."
    }

    return "Voice is ready when you choose to start."
  })

  const updateSpeechSummary = () => {
    const visibleCards = filteredCards.value
    if (!visibleCards.length) {
      speechSummary.value = "No matching jobs are visible right now."
      return
    }

    const leadCards = visibleCards.slice(0, 3).map((card) =>
      `${card.title}, ${card.payoff}, ${card.distance}, ${card.schedule}`
    )
    speechSummary.value = `I found ${visibleCards.length} visible job options. ${leadCards.join(". ")}.`
  }

  const stopListening = () => {
    recognition.value?.stop()
    if (voiceState.value === "listening") {
      voiceState.value = "idle"
    }
  }

  const stopSpeaking = () => {
    if (typeof window !== "undefined" && "speechSynthesis" in window) {
      window.speechSynthesis.cancel()
    }
    activeSpeechUtterance.value = null
    if (voiceState.value === "speaking") {
      voiceState.value = "idle"
    }
  }

  const startListening = () => {
    voiceRuntimeError.value = ""

    if (!voiceEnabled.value || !speechToTextEnabled.value) {
      voiceRuntimeError.value = "Speech recognition is disabled by backend config."
      return
    }

    if (!speechRecognitionSupported.value) {
      voiceRuntimeError.value = "Speech recognition is not available in this browser."
      return
    }

    stopSpeaking()

    if (!recognition.value) {
      const RecognitionCtor = window.SpeechRecognition || window.webkitSpeechRecognition
      if (!RecognitionCtor) {
        voiceRuntimeError.value = "Speech recognition is not available in this browser."
        return
      }

      const instance = new RecognitionCtor()
      instance.continuous = voiceConfig.value?.continuousRecognition ?? false
      instance.interimResults = voiceConfig.value?.interimResults ?? true
      instance.lang = speechLocale.value
      instance.maxAlternatives = voiceConfig.value?.maxAlternatives ?? 1
      instance.onstart = () => {
        voiceState.value = "listening"
      }
      instance.onend = () => {
        if (voiceState.value === "listening") {
          voiceState.value = "idle"
        }
      }
      instance.onerror = (event) => {
        voiceRuntimeError.value = `Speech recognition error: ${event.error ?? "unknown_error"}`
        voiceState.value = "idle"
      }
      instance.onresult = (event) => {
        const transcript = Array.from(event.results)
          .slice(event.resultIndex)
          .flatMap((result) => Array.from(result))
          .map((alternative) => alternative.transcript)
          .join(" ")
          .trim()

        if (transcript) {
          recognizedPrompt.value = transcript
        }
        voiceState.value = "processing"
        window.setTimeout(() => {
          if (voiceState.value === "processing") {
            voiceState.value = "idle"
          }
        }, 450)
      }
      recognition.value = instance
    }

    recognition.value.lang = speechLocale.value
    recognition.value.interimResults = voiceConfig.value?.interimResults ?? true
    recognition.value.continuous = voiceConfig.value?.continuousRecognition ?? false
    recognition.value.maxAlternatives = voiceConfig.value?.maxAlternatives ?? 1
    recognition.value.start()
  }

  const speakSummary = () => {
    voiceRuntimeError.value = ""

    if (!voiceEnabled.value || !textToSpeechEnabled.value) {
      voiceRuntimeError.value = "Speech synthesis is disabled by backend config."
      return
    }

    if (!speechSynthesisSupported.value) {
      voiceRuntimeError.value = "Speech synthesis is not available in this browser."
      return
    }

    stopListening()
    stopSpeaking()
    updateSpeechSummary()

    const utterance = new SpeechSynthesisUtterance(speechSummary.value)
    utterance.lang = speechLocale.value
    utterance.onstart = () => {
      voiceState.value = "speaking"
    }
    utterance.onend = () => {
      activeSpeechUtterance.value = null
      if (voiceState.value === "speaking") {
        voiceState.value = "idle"
      }
    }
    utterance.onerror = () => {
      voiceRuntimeError.value = "Speech synthesis failed in this browser."
      activeSpeechUtterance.value = null
      voiceState.value = "idle"
    }

    activeSpeechUtterance.value = utterance
    window.speechSynthesis.speak(utterance)
  }

  const openQuest = async (card: VisionQuestCard) => {
    await router.push(routeForNavigationTarget(card.quest.questNavigation))
  }

  const init = async () => {
    isLoading.value = true
    error.value = ""

    try {
      const [dashboardResponse, voiceConfigResponse] = await Promise.all([
        dashboardApi.getDashboard(),
        dashboardApi.getDashboardVoiceConfig()
      ])

      dashboard.value = dashboardResponse
      voiceConfig.value = voiceConfigResponse
      updateSpeechSummary()

      if (voiceConfigResponse.autoSpeakResponses && voiceConfigResponse.enabled && voiceConfigResponse.textToSpeechEnabled) {
        window.setTimeout(() => {
          void speakSummary()
        }, 150)
      }
    } catch (caught) {
      console.error(caught)
      error.value = "Unable to load adaptive work surface."
    } finally {
      isLoading.value = false
    }
  }

  onUnmounted(() => {
    stopListening()
    stopSpeaking()
  })

  return {
    isLoading,
    error,
    dashboard,
    voiceConfig,
    voiceState,
    surfaceMode,
    showAllResults,
    activeFilter,
    recognizedPrompt,
    filteredCards,
    visibleJobsLabel,
    voiceEnabled,
    speechToTextEnabled,
    textToSpeechEnabled,
    speechRecognitionSupported,
    speechSynthesisSupported,
    speechStatusLabel,
    voiceRuntimeError,
    speechSummary,
    init,
    openQuest,
    startListening,
    stopListening,
    speakSummary,
    stopSpeaking
  }
}
