import {computed, onUnmounted, ref} from "vue"
import {useRouter} from "vue-router"
import {dashboardApi, type DashboardVisionPromptResponse, type DashboardVoiceTranscription} from "../../workmarket/api/clients/dashboardApi.ts"
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

const tones: VisionQuestCard["tone"][] = ["coral", "sky", "mint"]

const supportedRecordingMimeTypes = [
  "audio/webm;codecs=opus",
  "audio/webm",
  "audio/mp4;codecs=mp4a.40.2",
  "audio/mp4",
  "audio/aac"
]

const chooseRecordingMimeType = () => {
  if (typeof MediaRecorder === "undefined") {
    return ""
  }

  return supportedRecordingMimeTypes.find((mimeType) => MediaRecorder.isTypeSupported(mimeType)) ?? ""
}

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

  const mediaRecorder = ref<MediaRecorder | null>(null)
  const activeMediaStream = ref<MediaStream | null>(null)
  const activeSpeechAudio = ref<HTMLAudioElement | null>(null)
  const activeSpeechAudioUrl = ref("")
  const recordedChunks = ref<Blob[]>([])
  const recordedAudioBytes = ref(0)
  const recordingTimeoutId = ref<number | null>(null)
  const discardPendingRecording = ref(false)
  const composerExpanded = ref(true)

  const voiceEnabled = computed(() => voiceConfig.value?.enabled ?? false)
  const speechToTextEnabled = computed(() => voiceConfig.value?.speechToTextEnabled ?? false)
  const textToSpeechEnabled = computed(() => voiceConfig.value?.textToSpeechEnabled ?? false)
  const speechRecognitionSupported = computed(() => typeof window !== "undefined"
    && typeof navigator !== "undefined"
    && typeof navigator.mediaDevices?.getUserMedia === "function"
    && typeof MediaRecorder !== "undefined")
  const speechSynthesisSupported = computed(() => typeof window !== "undefined"
    && typeof Audio !== "undefined")
  const maxRecordingMillis = computed(() => voiceConfig.value?.maxRecordingMillis ?? 20_000)
  const maxAudioBytes = computed(() => voiceConfig.value?.maxAudioBytes ?? 2_000_000)
  const maxSpeechTextLength = computed(() => voiceConfig.value?.maxSpeechTextLength ?? 1_000)
  const promptComposerVisible = computed(() => composerExpanded.value
    || voiceState.value !== "idle"
    || recognizedPrompt.value.trim().length > 0
    || speechSummary.value.trim().length > 0)
  const agentAttentionLevel = computed(() => {
    if (voiceState.value === "processing") {
      return "processing"
    }

    if (voiceState.value === "listening") {
      return "listening"
    }

    if (voiceState.value === "speaking") {
      return "speaking"
    }

    if (speechSummary.value.trim().length > 0) {
      return "ready"
    }

    return "quiet"
  })

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
      return "This browser does not expose microphone input or audio playback."
    }

    if (voiceRuntimeError.value) {
      return voiceRuntimeError.value
    }

    if (voiceState.value === "listening") {
      return "Recording audio for backend transcription."
    }

    if (voiceState.value === "processing") {
      return "Sending voice to the backend."
    }

    if (voiceState.value === "speaking") {
      return "Playing backend-generated speech."
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

  const applyVisionPromptResponse = (response: DashboardVisionPromptResponse) => {
    recognizedPrompt.value = response.normalizedPrompt
    activeFilter.value = response.activeFilter
    surfaceMode.value = response.surfaceMode
    speechSummary.value = response.assistantNote
    composerExpanded.value = true
  }

  const processPrompt = async (prompt: string, source = "text") => {
    const trimmedPrompt = prompt.trim()
    if (!trimmedPrompt) {
      return
    }

    voiceRuntimeError.value = ""

    try {
      voiceState.value = "processing"
      const response = await dashboardApi.processVisionPrompt(trimmedPrompt, source)
      applyVisionPromptResponse(response)

      if (voiceConfig.value?.autoSpeakResponses && voiceEnabled.value && textToSpeechEnabled.value) {
        await speakVoiceText(response.assistantNote)
      } else {
        voiceState.value = "idle"
      }
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Prompt processing failed in the backend."
      voiceState.value = "idle"
    }
  }

  const resetRecordingState = () => {
    if (recordingTimeoutId.value !== null) {
      window.clearTimeout(recordingTimeoutId.value)
      recordingTimeoutId.value = null
    }
    if (activeMediaStream.value) {
      activeMediaStream.value.getTracks().forEach((track) => track.stop())
      activeMediaStream.value = null
    }
    mediaRecorder.value = null
    recordedChunks.value = []
    recordedAudioBytes.value = 0
    discardPendingRecording.value = false
  }

  const openComposer = () => {
    composerExpanded.value = true
  }

  const closeComposer = () => {
    if (voiceState.value !== "idle" || recognizedPrompt.value.trim().length > 0 || speechSummary.value.trim().length > 0) {
      return
    }

    composerExpanded.value = false
  }

  const speakVoiceText = async (text: string) => {
    const trimmedText = text.trim()
    if (!trimmedText) {
      return
    }

    try {
      voiceState.value = "processing"
      const safeText = trimmedText.slice(0, maxSpeechTextLength.value)
      const audioBuffer = await dashboardApi.speakVoiceText(safeText)
      const audioBlob = new Blob([audioBuffer], {type: "audio/mpeg"})
      const audioUrl = URL.createObjectURL(audioBlob)
      activeSpeechAudioUrl.value = audioUrl
      const audio = new Audio(audioUrl)
      activeSpeechAudio.value = audio
      audio.onended = () => {
        if (activeSpeechAudioUrl.value === audioUrl) {
          URL.revokeObjectURL(audioUrl)
          activeSpeechAudioUrl.value = ""
        }
        activeSpeechAudio.value = null
        if (voiceState.value === "speaking") {
          voiceState.value = "idle"
        }
      }
      audio.onerror = () => {
        if (activeSpeechAudioUrl.value === audioUrl) {
          URL.revokeObjectURL(audioUrl)
          activeSpeechAudioUrl.value = ""
        }
        voiceRuntimeError.value = "Speech playback failed in the backend."
        activeSpeechAudio.value = null
        voiceState.value = "idle"
      }
      audio.oncanplaythrough = () => {
        voiceState.value = "speaking"
        void audio.play().catch((caught) => {
          console.error(caught)
          voiceRuntimeError.value = "Speech playback could not start."
          activeSpeechAudio.value = null
          voiceState.value = "idle"
          if (activeSpeechAudioUrl.value === audioUrl) {
            URL.revokeObjectURL(audioUrl)
            activeSpeechAudioUrl.value = ""
          }
        })
      }
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Speech synthesis failed in the backend."
      voiceState.value = "idle"
    }
  }

  const stopListening = (discard = false) => {
    if (discard) {
      discardPendingRecording.value = true
    }

    if (mediaRecorder.value && mediaRecorder.value.state !== "inactive") {
      mediaRecorder.value.stop()
      return
    }

    if (discard) {
      resetRecordingState()
      if (voiceState.value === "listening") {
        voiceState.value = "idle"
      }
    }
  }

  const stopSpeaking = () => {
    if (activeSpeechAudio.value) {
      activeSpeechAudio.value.pause()
      activeSpeechAudio.value.currentTime = 0
    }
    if (activeSpeechAudioUrl.value) {
      URL.revokeObjectURL(activeSpeechAudioUrl.value)
      activeSpeechAudioUrl.value = ""
    }
    activeSpeechAudio.value = null
    if (voiceState.value === "speaking") {
      voiceState.value = "idle"
    }
  }

  const transcribeRecordedAudio = async (audioBlob: Blob) => {
    if (maxAudioBytes.value > 0 && audioBlob.size > maxAudioBytes.value) {
      voiceRuntimeError.value = "Recording is too large. Try a shorter voice prompt."
      voiceState.value = "idle"
      resetRecordingState()
      return
    }

    try {
      const transcription: DashboardVoiceTranscription = await dashboardApi.transcribeVoiceAudio(audioBlob)
      recognizedPrompt.value = transcription.text
      await processPrompt(transcription.text, "voice")
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Speech transcription failed in the backend."
      voiceState.value = "idle"
    } finally {
      resetRecordingState()
    }
  }

  const startListening = async () => {
    voiceRuntimeError.value = ""

    if (!voiceEnabled.value || !speechToTextEnabled.value) {
      voiceRuntimeError.value = "Speech transcription is disabled by backend config."
      return
    }

    if (!speechRecognitionSupported.value) {
      voiceRuntimeError.value = "Microphone input is not available in this browser."
      return
    }

    stopSpeaking()
    stopListening(true)

    try {
      const stream = await navigator.mediaDevices.getUserMedia({audio: true})
      activeMediaStream.value = stream
      recordedChunks.value = []
      recordedAudioBytes.value = 0

      const mimeType = chooseRecordingMimeType()

      const recorder = mimeType
        ? new MediaRecorder(stream, {mimeType})
        : new MediaRecorder(stream)

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          recordedAudioBytes.value += event.data.size
          if (maxAudioBytes.value > 0 && recordedAudioBytes.value > maxAudioBytes.value) {
            voiceRuntimeError.value = "Recording is too large. Try a shorter voice prompt."
            stopListening(true)
            return
          }
          recordedChunks.value.push(event.data)
        }
      }
      recorder.onerror = (event) => {
        voiceRuntimeError.value = `Speech transcription error: ${event.error?.name ?? "unknown_error"}`
        voiceState.value = "idle"
      }
      recorder.onstart = () => {
        voiceState.value = "listening"
      }
      recorder.onstop = () => {
        if (discardPendingRecording.value) {
          resetRecordingState()
          if (voiceState.value === "listening") {
            voiceState.value = "idle"
          }
          return
        }

        const audioBlob = new Blob(recordedChunks.value, {type: recorder.mimeType || mimeType || "audio/webm"})
        voiceState.value = "processing"
        void transcribeRecordedAudio(audioBlob)
      }

      mediaRecorder.value = recorder
      recorder.start(1000)
      if (maxRecordingMillis.value > 0) {
        recordingTimeoutId.value = window.setTimeout(() => {
          if (voiceState.value === "listening") {
            stopListening()
          }
        }, maxRecordingMillis.value)
      }
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Microphone access failed."
      voiceState.value = "idle"
      if (activeMediaStream.value) {
        activeMediaStream.value.getTracks().forEach((track) => track.stop())
        activeMediaStream.value = null
      }
      mediaRecorder.value = null
      recordedChunks.value = []
    }
  }

  const speakSummary = async () => {
    voiceRuntimeError.value = ""

    if (!voiceEnabled.value || !textToSpeechEnabled.value) {
      voiceRuntimeError.value = "Speech playback is disabled by backend config."
      return
    }

    if (!speechSynthesisSupported.value) {
      voiceRuntimeError.value = "Audio playback is not available in this browser."
      return
    }

    stopListening()
    stopSpeaking()
    if (!speechSummary.value.trim().length) {
      updateSpeechSummary()
    }

    await speakVoiceText(speechSummary.value)
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
    stopListening(true)
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
    promptComposerVisible,
    agentAttentionLevel,
    speechStatusLabel,
    voiceRuntimeError,
    speechSummary,
    init,
    openQuest,
    processPrompt,
    startListening,
    stopListening,
    speakSummary,
    stopSpeaking,
    openComposer,
    closeComposer
  }
}
