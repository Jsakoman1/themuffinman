import {richTextHasContent} from "../../../shared/richText.ts"

export const canSubmitQuestApplicationDraft = (message: string, proposedPrice: string) => {
  return richTextHasContent(message) && Number(proposedPrice) >= 0.01
}
