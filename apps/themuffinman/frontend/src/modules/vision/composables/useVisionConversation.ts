import {computed, onUnmounted, ref} from "vue"
import {
  visionApi,
  type VisionCanvasBlock,
  type VisionConversationSummary,
  type VisionConversationTurnResponse,
  type VisionReviewTarget
} from "../api/visionApi.ts"
import type {DashboardVoiceConfig} from "../../workmarket/api/contracts.ts"

export type VisionVoiceState = "idle" | "listening" | "processing" | "speaking"
export type VisionAttentionState = "quiet" | "listening" | "processing" | "speaking" | "asking" | "review" | "complete" | "blocked"

const slotLabels: Record<string, string> = {
  quest_title: "Title",
  quest_description: "Description",
  reward_amount: "Reward",
  visibility: "Visibility",
  schedule_mode: "Schedule",
  scheduled_at: "Date and time",
  location_mode: "Location",
  location_label: "Custom place",
  location_candidate_confirmation: "Location confirmation"
}

const slotPlaceholders: Record<string, string> = {
  quest_title: "Name the quest in a few words",
  quest_description: "Describe the task clearly",
  reward_amount: "Example: 20 euros or free",
  visibility: "Example: public or circles",
  schedule_mode: "Example: fixed time or by agreement",
  scheduled_at: "Example: 2026-07-03T14:30",
  location_mode: "Example: use profile, hide location, or custom place",
  location_label: "Example: Ban Jelacic Square, Zagreb",
  location_candidate_confirmation: "Choose resolved place or keep typed location"
}

export const useVisionConversation = () => {
  const isLoading = ref(true)
  const error = ref("")
  const inputText = ref("")
  const response = ref<VisionConversationTurnResponse | null>(null)
  const recentConversations = ref<VisionConversationSummary[]>([])
  const conversationId = ref<number | null>(null)
  const voiceConfig = ref<DashboardVoiceConfig | null>(null)
  const voiceRuntimeError = ref("")
  const voiceState = ref<VisionVoiceState>("idle")
  const composerExpanded = ref(true)
  const lastTranscript = ref("")

  const mediaRecorder = ref<MediaRecorder | null>(null)
  const activeMediaStream = ref<MediaStream | null>(null)
  const activeSpeechAudio = ref<HTMLAudioElement | null>(null)
  const activeSpeechAudioUrl = ref("")
  const recordedChunks = ref<Blob[]>([])
  const discardPendingRecording = ref(false)

  const voiceEnabled = computed(() => voiceConfig.value?.enabled ?? false)
  const speechToTextEnabled = computed(() => voiceConfig.value?.speechToTextEnabled ?? false)
  const textToSpeechEnabled = computed(() => voiceConfig.value?.textToSpeechEnabled ?? false)
  const speechRecognitionSupported = computed(() => typeof window !== "undefined"
    && typeof navigator !== "undefined"
    && typeof navigator.mediaDevices?.getUserMedia === "function"
    && typeof MediaRecorder !== "undefined")
  const speechSynthesisSupported = computed(() => typeof window !== "undefined" && typeof Audio !== "undefined")

  const fieldRequestBlock = computed(() => response.value?.blocks.find((block) => block.type === "field_request") ?? null)
  const promptComposerVisible = computed(() => composerExpanded.value
    || voiceState.value !== "idle"
    || !!fieldRequestBlock.value
    || response.value?.canvasMode === "review")

  const currentSlotLabel = computed(() => {
    const slotId = fieldRequestBlock.value?.fieldId
    return slotId ? (slotLabels[slotId] ?? fieldRequestBlock.value?.title ?? "Next field") : ""
  })

  const currentPlaceholder = computed(() => {
    const slotId = fieldRequestBlock.value?.fieldId
    if (slotId) {
      return fieldRequestBlock.value?.placeholder ?? slotPlaceholders[slotId] ?? "Type the next detail"
    }
    return "Tell the agent what you want to do"
  })

  const currentFieldKind = computed(() => fieldRequestBlock.value?.fieldKind ?? "short_text")

  const currentMessage = computed(() => response.value?.message ?? "The agent is waiting for a prompt.")

  const attentionState = computed<VisionAttentionState>(() => {
    if (voiceState.value === "processing") {
      return "processing"
    }
    if (voiceState.value === "listening") {
      return "listening"
    }
    if (voiceState.value === "speaking") {
      return "speaking"
    }
    if (response.value?.agentState === "BLOCKED") {
      return "blocked"
    }
    if (response.value?.agentState === "COMPLETE") {
      return "complete"
    }
    if (response.value?.agentState === "REVIEW_READY") {
      return "review"
    }
    if (response.value?.agentState === "ASKING") {
      return "asking"
    }
    return "quiet"
  })

  const translationWarning = computed(() => {
    if (!response.value) {
      return ""
    }
    const warningBlock = response.value.blocks.find((block) => block.type === "warning")
    if (warningBlock?.body) {
      return warningBlock.body
    }
    return ""
  })

  const displayBlocks = computed<VisionCanvasBlock[]>(() => {
    if (!response.value) {
      return []
    }
    return response.value.blocks.filter((block) => block.type !== "agent_message")
  })

  const canSend = computed(() => inputText.value.trim().length > 0 && voiceState.value !== "processing")
  const canConfirm = computed(() => response.value?.canvasMode === "review"
    && response.value.executionEnabled
    && voiceState.value === "idle")

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
      return "Sending input to the persisted backend conversation."
    }
    if (voiceState.value === "speaking") {
      return "Playing backend-generated speech."
    }
    return "Voice is ready when you choose to start."
  })

  const init = async () => {
    error.value = ""
    isLoading.value = true

    try {
      voiceConfig.value = await visionApi.getVoiceConfig()
      recentConversations.value = (await visionApi.getRecentConversations()).items
    } catch (caught) {
      console.error(caught)
      error.value = "Vision bootstrap failed."
    } finally {
      isLoading.value = false
    }
  }

  const processPrompt = async (
    prompt: string,
    source = "text",
    action = "SUBMIT_PROMPT",
    reviewTarget: VisionReviewTarget | null = null
  ) => {
    const trimmedPrompt = prompt.trim()
    if (action === "SUBMIT_PROMPT" && !trimmedPrompt) {
      return
    }

    voiceRuntimeError.value = ""

    try {
      voiceState.value = "processing"
      const next = await visionApi.processConversationTurn(trimmedPrompt, conversationId.value, source, action, reviewTarget)
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      inputText.value = ""
      composerExpanded.value = true

      if (voiceConfig.value?.autoSpeakResponses && voiceEnabled.value && textToSpeechEnabled.value) {
        await speakVoiceText(next.message)
      } else {
        voiceState.value = "idle"
      }
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Vision conversation processing failed in the backend."
      voiceState.value = "idle"
    }
  }

  const confirmReview = async () => {
    await processPrompt("", "text", "CONFIRM_REVIEW")
  }

  const requestReviewChange = async (reviewTarget: VisionReviewTarget) => {
    await processPrompt("", "text", "REQUEST_REVIEW_EDIT", reviewTarget)
  }

  const resetConversation = async () => {
    if (conversationId.value != null) {
      try {
        voiceState.value = "processing"
        const next = await visionApi.resetConversation(conversationId.value)
        response.value = next
        conversationId.value = next.conversationId
        recentConversations.value = next.recentConversations
        inputText.value = ""
        composerExpanded.value = true
        voiceState.value = "idle"
      } catch (caught) {
        console.error(caught)
        voiceRuntimeError.value = "Vision conversation reset failed in the backend."
        voiceState.value = "idle"
      }
      return
    }
    stopSpeaking()
    conversationId.value = null
    response.value = null
    recentConversations.value = []
    inputText.value = ""
    lastTranscript.value = ""
    composerExpanded.value = true
    voiceRuntimeError.value = ""
  }

  const cancelConversation = async () => {
    if (conversationId.value == null) {
      await resetConversation()
      return
    }
    try {
      voiceState.value = "processing"
      const next = await visionApi.cancelConversation(conversationId.value)
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      inputText.value = ""
      composerExpanded.value = true
      voiceState.value = "idle"
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Vision conversation cancel failed in the backend."
      voiceState.value = "idle"
    }
  }

  const resumeConversation = async (nextConversationId: number) => {
    try {
      voiceState.value = "processing"
      const next = await visionApi.getConversation(nextConversationId)
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      composerExpanded.value = true
      voiceState.value = "idle"
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Vision conversation resume failed in the backend."
      voiceState.value = "idle"
    }
  }

  const resetRecordingState = () => {
    if (activeMediaStream.value) {
      activeMediaStream.value.getTracks().forEach((track) => track.stop())
      activeMediaStream.value = null
    }
    mediaRecorder.value = null
    recordedChunks.value = []
    discardPendingRecording.value = false
  }

  const stopSpeaking = () => {
    if (activeSpeechAudio.value) {
      activeSpeechAudio.value.pause()
      activeSpeechAudio.value = null
    }
    if (activeSpeechAudioUrl.value) {
      URL.revokeObjectURL(activeSpeechAudioUrl.value)
      activeSpeechAudioUrl.value = ""
    }
    if (voiceState.value === "speaking") {
      voiceState.value = "idle"
    }
  }

  const speakVoiceText = async (text: string) => {
    if (!text.trim() || !voiceEnabled.value || !textToSpeechEnabled.value || !speechSynthesisSupported.value) {
      return
    }

    try {
      stopSpeaking()
      voiceState.value = "speaking"
      const audioBytes = await visionApi.speakVoiceText(text)
      const audioBlob = new Blob([audioBytes], {type: "audio/mpeg"})
      const audioUrl = URL.createObjectURL(audioBlob)
      const audio = new Audio(audioUrl)
      activeSpeechAudio.value = audio
      activeSpeechAudioUrl.value = audioUrl
      audio.onended = () => {
        stopSpeaking()
      }
      audio.onerror = () => {
        voiceRuntimeError.value = "Backend speech playback failed."
        stopSpeaking()
      }
      await audio.play()
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Backend speech synthesis failed."
      stopSpeaking()
    }
  }

  const speakSummary = async () => {
    if (!response.value) {
      return
    }
    await speakVoiceText(response.value.message)
  }

  const stopListening = (discard = false) => {
    discardPendingRecording.value = discard
    mediaRecorder.value?.stop()
  }

  const finalizeRecording = async () => {
    if (!recordedChunks.value.length || discardPendingRecording.value) {
      voiceState.value = "idle"
      resetRecordingState()
      return
    }

    const audioBlob = new Blob(recordedChunks.value, {type: "audio/webm"})

    try {
      voiceState.value = "processing"
      const transcription = await visionApi.transcribeVoiceAudio(audioBlob)
      lastTranscript.value = transcription.text
      await processPrompt(transcription.text, "voice")
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Voice transcription failed in the backend."
      voiceState.value = "idle"
    } finally {
      resetRecordingState()
    }
  }

  const startListening = async () => {
    if (!voiceEnabled.value || !speechToTextEnabled.value || !speechRecognitionSupported.value) {
      return
    }

    try {
      stopSpeaking()
      voiceRuntimeError.value = ""
      const stream = await navigator.mediaDevices.getUserMedia({audio: true})
      activeMediaStream.value = stream
      recordedChunks.value = []
      discardPendingRecording.value = false

      const recorder = new MediaRecorder(stream)
      mediaRecorder.value = recorder

      recorder.addEventListener("dataavailable", (event) => {
        if (event.data.size > 0) {
          recordedChunks.value.push(event.data)
        }
      })

      recorder.addEventListener("stop", () => {
        void finalizeRecording()
      })

      recorder.start()
      voiceState.value = "listening"
      composerExpanded.value = true
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Microphone access failed."
      resetRecordingState()
      voiceState.value = "idle"
    }
  }

  const openComposer = () => {
    composerExpanded.value = true
  }

  const closeComposer = () => {
    if (voiceState.value !== "idle" || !!fieldRequestBlock.value || response.value?.canvasMode === "review") {
      return
    }
    composerExpanded.value = false
  }

  onUnmounted(() => {
    stopListening(true)
    stopSpeaking()
    resetRecordingState()
  })

  return {
    isLoading,
    error,
    inputText,
    response,
    voiceState,
    voiceEnabled,
    speechToTextEnabled,
    textToSpeechEnabled,
    speechRecognitionSupported,
    speechSynthesisSupported,
    promptComposerVisible,
    displayBlocks,
    currentSlotLabel,
    currentPlaceholder,
    currentFieldKind,
    currentMessage,
    attentionState,
    translationWarning,
    speechStatusLabel,
    voiceRuntimeError,
    lastTranscript,
    canSend,
    canConfirm,
    recentConversations,
    init,
    processPrompt,
    confirmReview,
    requestReviewChange,
    resetConversation,
    cancelConversation,
    resumeConversation,
    startListening,
    stopListening,
    speakSummary,
    stopSpeaking,
    openComposer,
    closeComposer
  }
}
