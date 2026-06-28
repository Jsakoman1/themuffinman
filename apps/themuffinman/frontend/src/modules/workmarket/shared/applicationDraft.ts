import {richTextHasContent} from "../../../shared/richText.ts"
import {isQuestFree} from "./pricing.ts"

export const canSubmitQuestApplicationDraft = (message: string, proposedPrice: string, questAwardAmount: number | null | undefined) => {
  return richTextHasContent(message) && (isQuestFree(questAwardAmount) || Number(proposedPrice) >= 0.01)
}
