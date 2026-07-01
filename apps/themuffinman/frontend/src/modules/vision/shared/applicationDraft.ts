import {richTextHasContent} from "../../../shared/richText.ts"

type QuestApplicationDraftRules = {
  messageRequired?: boolean
  proposedPriceRequired?: boolean
  minimumProposedPrice?: number | null
}

export const canSubmitQuestApplicationDraft = (
  message: string,
  proposedPrice: string,
  rules: QuestApplicationDraftRules | null | undefined
) => {
  if (rules?.messageRequired !== false && !richTextHasContent(message)) {
    return false
  }

  if (!rules?.proposedPriceRequired) {
    return true
  }

  const minimum = rules.minimumProposedPrice ?? 0.01
  const parsedPrice = Number(proposedPrice)
  return Number.isFinite(parsedPrice) && parsedPrice >= minimum
}
