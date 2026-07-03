import {computed, onMounted, onUnmounted, ref} from "vue"
import {
  visionConversationApi,
  type VisionCanvasBlock,
  type VisionConversationSummary,
  type VisionConversationTurnResponse,
  type VisionReviewTarget
} from "../api/visionConversationApi.ts"
import type {DashboardVoiceConfig} from "../api/contracts.ts"

export type VisionVoiceState = "idle" | "listening" | "processing" | "speaking"
export type VisionAttentionState = "quiet" | "listening" | "processing" | "speaking" | "asking" | "discovering" | "review" | "complete" | "blocked"

const slotLabels: Record<string, string> = {
  circle_name: "Circle name",
  target_circle_query: "Circle",
  target_user: "Person",
  quest_title: "Title",
  quest_description: "Description",
  target_quest_query: "Quest",
  application_message: "Application message",
  application_proposed_price: "Proposed price",
  profile_username: "Username",
  profile_description: "Profile description",
  profile_location_mode: "Location mode",
  profile_location_label: "Location",
  reward_amount: "Reward",
  visibility: "Visibility",
  schedule_mode: "Schedule",
  scheduled_date: "Date",
  scheduled_time: "Time",
  location_mode: "Location",
  location_label: "Custom place",
  location_candidate_confirmation: "Location confirmation"
}

const slotPlaceholders: Record<string, string> = {
  circle_name: "Name the circle in a few words",
  target_circle_query: "Say the exact circle name or circle id",
  target_user: "Say the exact username, email, or name fragment",
  quest_title: "Name the quest in a few words",
  quest_description: "Describe the task clearly",
  target_quest_query: "Say the exact quest title or quest id",
  application_message: "Write the message you want to send",
  application_proposed_price: "Example: 20 or 20.50",
  profile_username: "Choose the username to show on your profile",
  profile_description: "Write a short profile description",
  profile_location_mode: "Choose off, approximate, or exact",
  profile_location_label: "Example: Zurich, Switzerland",
  reward_amount: "Example: 20 euros or free",
  visibility: "Example: public or circles",
  schedule_mode: "Example: fixed time or by agreement",
  scheduled_date: "Example: 2026-07-03 or next Tuesday",
  scheduled_time: "Example: 14:30 or 2 pm",
  location_mode: "Example: use profile, hide location, or custom place",
  location_label: "Example: Ban Jelacic Square, Zagreb",
  location_candidate_confirmation: "Choose resolved place or keep typed location"
}

const intentFamilyLabels: Record<string, string> = {
  CREATE_QUEST: "quest",
  DISCOVER_QUESTS: "quest discovery",
  VIEW_QUEST_DETAIL: "quest detail",
  CREATE_CIRCLE: "circle",
  CREATE_CIRCLE_REQUEST: "circle request",
  ACCEPT_CIRCLE_REQUEST: "circle request",
  DELETE_CIRCLE_REQUEST: "circle request",
  UPDATE_CIRCLE: "circle",
  DELETE_CIRCLE: "circle",
  CREATE_APPLICATION: "application",
  UPDATE_APPLICATION: "application",
  WITHDRAW_APPLICATION: "application",
  APPROVE_APPLICATION: "application",
  DECLINE_APPLICATION: "application",
  UPDATE_PROFILE: "profile",
  UPDATE_PROFILE_LOCATION: "profile location",
  VIEW_PROFILE: "profile",
  VIEW_SETTINGS: "settings",
  VIEW_CIRCLES: "circles",
  VIEW_CIRCLE_DETAIL: "circle detail",
  VIEW_APPLICATIONS: "applications",
  VIEW_APPLICATION_DETAIL: "application detail",
  OPEN_CHAT: "chat",
  VIEW_CHAT_WORKSPACE: "chat"
}

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

const cancelPromptSignals = new Set([
  "cancel",
  "cancel quest",
  "cancel conversation",
  "stop",
  "stop task",
  "odustani",
  "prekini",
  "ponisti",
  "poništi",
  "reset",
  "reset conversation"
])

const isCancelCommand = (prompt: string) => {
  const normalized = prompt.trim().toLowerCase().replace(/[.!?]+$/g, "")
  return cancelPromptSignals.has(normalized)
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
  const recordedAudioBytes = ref(0)
  const recordingTimeoutId = ref<number | null>(null)
  const discardPendingRecording = ref(false)

  const voiceEnabled = computed(() => voiceConfig.value?.enabled ?? false)
  const speechToTextEnabled = computed(() => voiceConfig.value?.speechToTextEnabled ?? false)
  const textToSpeechEnabled = computed(() => voiceConfig.value?.textToSpeechEnabled ?? false)
  const speechRecognitionSupported = computed(() => typeof window !== "undefined"
    && typeof navigator !== "undefined"
    && typeof navigator.mediaDevices?.getUserMedia === "function"
    && typeof MediaRecorder !== "undefined")
  const speechSynthesisSupported = computed(() => typeof window !== "undefined" && typeof Audio !== "undefined")
  const maxRecordingMillis = computed(() => voiceConfig.value?.maxRecordingMillis ?? 20_000)
  const maxAudioBytes = computed(() => voiceConfig.value?.maxAudioBytes ?? 2_000_000)
  const maxSpeechTextLength = computed(() => voiceConfig.value?.maxSpeechTextLength ?? 1_000)
  const clientStateVersion = "vision-surface-v1"
  const clientLocale = computed(() => {
    if (typeof navigator !== "undefined" && navigator.language) {
      return navigator.language
    }
    return voiceConfig.value?.preferredLocale?.trim() || "en"
  })
  const clientTimezone = computed(() => {
    if (typeof Intl !== "undefined") {
      return Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC"
    }
    return "UTC"
  })

  const clientCapabilities = computed(() => {
    const capabilities = ["text_input"]
    if (speechRecognitionSupported.value) {
      capabilities.push("voice_input")
    }
    if (speechSynthesisSupported.value) {
      capabilities.push("voice_output")
    }
    if (voiceEnabled.value) {
      capabilities.push("voice_turns")
    }
    return capabilities
  })

  const fieldRequestBlock = computed(() => response.value?.blocks.find((block) => block.type === "field_request") ?? null)
  const promptComposerVisible = computed(() => !response.value
    || composerExpanded.value
    || voiceState.value !== "idle"
    || !!fieldRequestBlock.value
    || response.value?.canvasMode === "review"
    || response.value?.canvasMode === "results")

  const currentSlotId = computed(() => fieldRequestBlock.value?.fieldId ?? "")

  const currentSlotLabel = computed(() => {
    const slotId = currentSlotId.value
    return slotId ? (slotLabels[slotId] ?? fieldRequestBlock.value?.title ?? "Next field") : ""
  })

  const currentPlaceholder = computed(() => {
    const slotId = fieldRequestBlock.value?.fieldId
    if (slotId) {
      return fieldRequestBlock.value?.placeholder ?? slotPlaceholders[slotId] ?? "Type the next detail"
    }
    const family = response.value?.memoryTrail?.activeEntityFamily?.trim()?.toLowerCase()
    if (family) {
      return `Type or speak the next ${family} detail. Enter sends.`
    }
    return "Type or speak what you want to do. Enter sends."
  })

  const currentFieldKind = computed(() => fieldRequestBlock.value?.fieldKind ?? "short_text")

  const slotSummaries = computed(() => response.value?.slotSummaries ?? [])

  const transcriptTargetLabel = computed(() => currentSlotLabel.value || "the current prompt")

  const currentSlotSummary = computed(() => {
    const slotId = currentSlotId.value
    if (!slotId) {
      return null
    }
    const appliedSummary = response.value?.appliedSlotSummaries.find((slot) => slot.slotId === slotId)
    if (appliedSummary) {
      return appliedSummary
    }
    return slotSummaries.value.find((slot) => slot.slotId === slotId) ?? null
  })

  const currentSlotValue = computed(() => currentSlotSummary.value?.value ?? "")

  const transcriptTargetDetail = computed(() => {
    if (!response.value) {
      return "The backend will map speech and text to the active task as soon as you start."
    }

    if (currentSlotSummary.value?.value) {
      return `Current value: ${currentSlotSummary.value.value}`
    }

    if (response.value.translationApplied) {
      return response.value.normalizedPrompt && response.value.normalizedPrompt !== lastTranscript.value
        ? `Normalized to: ${response.value.normalizedPrompt}`
        : "The backend normalized the turn before slot extraction."
    }

    if (currentFieldKind.value === "date") {
      return "This slot expects a calendar day."
    }

    if (currentFieldKind.value === "time") {
      return "This slot expects a time of day."
    }

    return "The current turn is being routed into the active slot."
  })

  const activeEntityFamilyLabel = computed(() => {
    const family = response.value?.memoryTrail?.activeEntityFamily?.trim()
    if (family) {
      return family
    }
    const intent = response.value?.intent?.trim()
    if (intent && intentFamilyLabels[intent]) {
      return intentFamilyLabels[intent]
    }
    return ""
  })

  const activeEntityContextLabel = computed(() => {
    const familyLabel = activeEntityFamilyLabel.value
    const intent = response.value?.intent?.trim()
    if (!familyLabel && !intent) {
      return ""
    }
    if (familyLabel && intent) {
      return `${familyLabel} · ${intent.toLowerCase().replaceAll("_", " ")}`
    }
    return familyLabel || (intent ? intent.toLowerCase().replaceAll("_", " ") : "")
  })

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
    if (response.value?.agentState === "RECOMMENDING") {
      return "discovering"
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
    && (response.value.executionCandidate?.executionReady ?? response.value.executionEnabled)
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
      voiceConfig.value = await visionConversationApi.getVoiceConfig()
      recentConversations.value = (await visionConversationApi.getRecentConversations()).items
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
    reviewTarget: VisionReviewTarget | null = null,
    selectedOptionId: string | null = null,
    fieldValue: string | null = null,
    confirmation: boolean | null = null
  ) => {
    const trimmedPrompt = prompt.trim()
    if (action === "SUBMIT_PROMPT" && !trimmedPrompt) {
      return
    }

    if (action === "SUBMIT_PROMPT" && isCancelCommand(trimmedPrompt)) {
      await cancelConversation()
      return
    }

    voiceRuntimeError.value = ""

    try {
      voiceState.value = "processing"
      const next = await visionConversationApi.processConversationTurn({
        conversationId: conversationId.value,
        inputType: source === "voice" ? "voice" : "text",
        text: trimmedPrompt,
        clientCapabilities: clientCapabilities.value,
        clientStateVersion,
        clientLocale: clientLocale.value,
        clientTimezone: clientTimezone.value,
        selectedOptionId,
        fieldValue,
        confirmation,
        action,
        reviewTarget
      })
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      inputText.value = ""
      if (source !== "voice") {
        lastTranscript.value = ""
      }
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
    await processPrompt("", "text", "CONFIRM_REVIEW", null, null, null, true)
  }

  const requestReviewChange = async (reviewTarget: VisionReviewTarget) => {
    await processPrompt("", "text", "REQUEST_REVIEW_EDIT", reviewTarget)
  }

  const resetConversation = async () => {
    if (conversationId.value != null) {
      try {
        voiceState.value = "processing"
        const next = await visionConversationApi.resetConversation(conversationId.value)
        response.value = next
        conversationId.value = next.conversationId
        recentConversations.value = next.recentConversations
        inputText.value = ""
        lastTranscript.value = ""
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
      const next = await visionConversationApi.cancelConversation(conversationId.value)
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      inputText.value = ""
      lastTranscript.value = ""
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
      const next = await visionConversationApi.getConversation(nextConversationId)
      response.value = next
      conversationId.value = next.conversationId
      recentConversations.value = next.recentConversations
      lastTranscript.value = ""
      composerExpanded.value = true
      voiceState.value = "idle"
    } catch (caught) {
      console.error(caught)
      voiceRuntimeError.value = "Vision conversation resume failed in the backend."
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
      const audioBytes = await visionConversationApi.speakVoiceText(text.trim().slice(0, maxSpeechTextLength.value))
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

    const audioBlob = new Blob(recordedChunks.value, {type: mediaRecorder.value?.mimeType || chooseRecordingMimeType() || "audio/webm"})
    if (maxAudioBytes.value > 0 && audioBlob.size > maxAudioBytes.value) {
      voiceRuntimeError.value = "Recording is too large. Try a shorter voice prompt."
      voiceState.value = "idle"
      resetRecordingState()
      return
    }

    try {
      voiceState.value = "processing"
      const transcription = await visionConversationApi.transcribeVoiceAudio(audioBlob)
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
      recordedAudioBytes.value = 0
      discardPendingRecording.value = false

      const mimeType = chooseRecordingMimeType()
      const recorder = mimeType
        ? new MediaRecorder(stream, {mimeType})
        : new MediaRecorder(stream)
      mediaRecorder.value = recorder

      recorder.addEventListener("dataavailable", (event) => {
        if (event.data.size > 0) {
          recordedAudioBytes.value += event.data.size
          if (maxAudioBytes.value > 0 && recordedAudioBytes.value > maxAudioBytes.value) {
            voiceRuntimeError.value = "Recording is too large. Try a shorter voice prompt."
            stopListening(true)
            return
          }
          recordedChunks.value.push(event.data)
        }
      })

      recorder.addEventListener("stop", () => {
        void finalizeRecording()
      })

      recorder.start(1000)
      if (maxRecordingMillis.value > 0) {
        recordingTimeoutId.value = window.setTimeout(() => {
          if (voiceState.value === "listening") {
            stopListening()
          }
        }, maxRecordingMillis.value)
      }
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
    if (!response.value
      || voiceState.value !== "idle"
      || !!fieldRequestBlock.value
      || response.value?.canvasMode === "review") {
      return
    }
    composerExpanded.value = false
  }

  const handleGlobalKeydown = (event: KeyboardEvent) => {
    if (event.key !== "Escape" || event.defaultPrevented || event.repeat) {
      return
    }

    if (!response.value && conversationId.value == null && !inputText.value.trim()) {
      return
    }

    event.preventDefault()
    void cancelConversation()
  }

  onMounted(() => {
    window.addEventListener("keydown", handleGlobalKeydown)
  })

  onUnmounted(() => {
    window.removeEventListener("keydown", handleGlobalKeydown)
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
    slotSummaries,
    currentSlotLabel,
    currentSlotId,
    currentSlotSummary,
    currentSlotValue,
    currentPlaceholder,
    activeEntityFamilyLabel,
    activeEntityContextLabel,
    currentFieldKind,
    transcriptTargetLabel,
    transcriptTargetDetail,
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
