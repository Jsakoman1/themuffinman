import type {VisionCanvasBlock, VisionConversationTurnResponse, VisionQuestReview} from "./api/visionConversationApi.ts"

export type VisionPreviewField = {
  slotId: string
  label: string
  value: string
}

const hasItems = (block: VisionCanvasBlock) => (block.items?.length ?? 0) > 0

export const selectVisionPreviewBlock = (response: VisionConversationTurnResponse | null) => {
  if (!response) {
    return null
  }

  const preferredBlock = response.blocks.find((block) =>
    hasItems(block)
    && block.type === "result_summary"
    && block.title !== "Collected so far")

  if (preferredBlock) {
    return preferredBlock
  }

  const fallbackBlock = response.blocks.find((block) =>
    hasItems(block)
    && (block.type === "result_summary" || block.type === "review_summary" || block.type === "success" || block.type === "info"))

  return fallbackBlock ?? null
}

export const buildVisionPreviewFields = (response: VisionConversationTurnResponse | null): VisionPreviewField[] => {
  const previewBlock = selectVisionPreviewBlock(response)
  if ((previewBlock?.items?.length ?? 0) > 0) {
    return (previewBlock?.items ?? []).map((item) => ({
      slotId: item.slotId,
      label: item.label,
      value: item.value ?? ""
    }))
  }

  const review = response?.review
  if (review) {
    return reviewFields(review)
  }

  return (response?.slotSummaries ?? []).map((item) => ({
    slotId: item.slotId,
    label: item.label,
    value: item.value ?? ""
  }))
}

export const hasVisionPreviewContent = (response: VisionConversationTurnResponse | null) => {
  if (!response) {
    return false
  }
  return Boolean(
    response.canvasMode === "review"
    || response.canvasMode === "results"
    || response.canvasMode === "complete"
    || response.canvasMode === "blocked"
    || response.review
    || response.executionCandidate
    || response.questDiscovery
    || response.memoryTrail
  )
}

export const shouldShowVisionFlowDebugRail = (
  response: VisionConversationTurnResponse | null,
  lastTranscript: string,
  voiceState: string
) => Boolean(response || lastTranscript.trim() || voiceState !== "idle")

export const reviewFields = (review: VisionQuestReview) => [
  {slotId: "quest_title", label: "Title", value: review.title ?? ""},
  {slotId: "quest_description", label: "Description", value: review.description ?? ""},
  {slotId: "reward_amount", label: "Reward", value: review.rewardLabel ?? ""},
  {slotId: "visibility", label: "Visibility", value: review.visibility ?? ""},
  {slotId: "scheduled_at", label: "Schedule", value: review.schedule ?? ""},
  {slotId: "location_label", label: "Location", value: review.location ?? ""}
].filter((field) => field.value.trim().length > 0)
