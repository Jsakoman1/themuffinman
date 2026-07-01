import {computed, type Ref} from "vue"
import type {VisionConversationSummary, VisionConversationTurnResponse} from "../api/visionApi.ts"
import type {VisionVoiceState} from "./useVisionConversation.ts"

type RecentConversationGroup = {
  key: string
  title: string
  items: VisionConversationSummary[]
}

type VisionSurfaceStateParams = {
  voiceState: Ref<VisionVoiceState>
  voiceRuntimeError: Ref<string>
  translationWarning: Ref<string>
  response: Ref<VisionConversationTurnResponse | null>
  currentMessage: Ref<string>
  currentSlotLabel: Ref<string>
  recentConversations: Ref<VisionConversationSummary[]>
  contextPinned: Ref<boolean>
}

export const useVisionSurfaceState = (params: VisionSurfaceStateParams) => {
  const agentCaption = computed(() => {
    if (params.voiceState.value === "listening") {
      return "Listening for the next turn."
    }
    if (params.voiceState.value === "processing") {
      return "Updating the persisted backend conversation."
    }
    if (params.voiceState.value === "speaking") {
      return "Speaking the current backend response."
    }
    return "Ready to begin."
  })

  const nextActionLabel = computed(() => {
    if (!params.response.value) {
      return "Ready"
    }
    if (params.response.value.nextAction === "SHOW_REVIEW") {
      return "Review"
    }
    if (params.response.value.nextAction === "SHOW_RESULTS") {
      return "Discover"
    }
    if (params.response.value.nextAction === "COMPLETE") {
      return "Complete"
    }
    if (params.response.value.nextAction === "BLOCKED") {
      return "Blocked"
    }
    return "Clarify"
  })

  const surfaceStatusLabel = computed(() => {
    if (params.voiceState.value === "listening") {
      return "Listening"
    }
    if (params.voiceState.value === "processing") {
      return "Processing"
    }
    if (params.voiceState.value === "speaking") {
      return "Speaking"
    }
    return nextActionLabel.value
  })

  const surfaceStatusDetail = computed(() => {
    if (params.voiceRuntimeError.value) {
      return params.voiceRuntimeError.value
    }
    if (params.translationWarning.value) {
      return params.translationWarning.value
    }
    if (!params.response.value) {
      return "Ready to begin."
    }
    if (params.response.value?.questDiscovery?.summary && params.response.value.canvasMode === "results") {
      return params.response.value.questDiscovery.summary
    }
    if (params.response.value?.executionCandidate?.summary && params.response.value.canvasMode === "review") {
      return params.response.value.executionCandidate.summary
    }
    return agentCaption.value
  })

  const contextVisible = computed(() => params.contextPinned.value)

  const showContextToggle = computed(() => true)

  const surfaceToneClass = computed(() => {
    const canvasMode = params.response.value?.canvasMode
    if (canvasMode === "review") {
      return "vision-surface--review"
    }
    if (canvasMode === "results") {
      return "vision-surface--results"
    }
    if (canvasMode === "blocked") {
      return "vision-surface--blocked"
    }
    if (canvasMode === "complete") {
      return "vision-surface--complete"
    }
    if (params.voiceState.value === "processing") {
      return "vision-surface--processing"
    }
    if (params.voiceState.value === "listening") {
      return "vision-surface--listening"
    }
    if (params.voiceState.value === "speaking") {
      return "vision-surface--speaking"
    }
    return "vision-surface--calm"
  })

  const recentConversationGroups = computed<RecentConversationGroup[]>(() => {
    const groups = [
      {
        key: "active",
        title: "In progress",
        items: params.recentConversations.value.filter((conversation) => conversation.groupKey === "active")
      },
      {
        key: "review_ready",
        title: "Ready for review",
        items: params.recentConversations.value.filter((conversation) => conversation.groupKey === "review_ready")
      },
      {
        key: "blocked",
        title: "Blocked",
        items: params.recentConversations.value.filter((conversation) => conversation.groupKey === "blocked")
      },
      {
        key: "completed",
        title: "Completed",
        items: params.recentConversations.value.filter((conversation) => conversation.groupKey === "completed")
      }
    ]
    return groups.filter((group) => group.items.length > 0)
  })

  return {
    agentCaption,
    nextActionLabel,
    surfaceStatusLabel,
    surfaceStatusDetail,
    contextVisible,
    showContextToggle,
    surfaceToneClass,
    recentConversationGroups
  } as const
}
