export const isQuestFree = (awardAmount: number | null | undefined) => awardAmount === 0

export const formatQuestReward = (awardAmount: number | null | undefined) => {
  if (awardAmount === null || awardAmount === undefined) {
    return "Not set"
  }

  return isQuestFree(awardAmount) ? "Free" : `$ ${awardAmount}`
}

export const formatApplicationPrice = (proposedPrice: number | null | undefined) => {
  if (proposedPrice === null || proposedPrice === undefined) {
    return "No price"
  }

  return `$ ${proposedPrice}`
}

export const buildApplicationPriceInput = (questAwardAmount: number | null | undefined, proposedPrice: string) => {
  if (isQuestFree(questAwardAmount)) {
    return null
  }

  return Number(proposedPrice)
}

export const buildQuestAwardAmountInput = (awardAmount: string) => {
  return awardAmount.trim() ? Number(awardAmount) : null
}
