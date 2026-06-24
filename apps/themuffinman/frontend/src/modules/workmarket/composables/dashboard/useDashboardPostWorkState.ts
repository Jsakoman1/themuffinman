import type {QuestDashboard} from "../useQuestDashboard.ts"

export const useDashboardPostWorkState = (dashboard: QuestDashboard) => {
  const handleQuestImagesChange = (event: Event) => {
    const input = event.target as HTMLInputElement | null
    dashboard.addQuestImages(input?.files ?? null)

    if (input) {
      input.value = ""
    }
  }

  const toggleQuestAudience = (audience: QuestDashboard["questAudience"]) => {
    dashboard.questAudience = audience
  }

  const toggleQuestCircle = (circleId: number) => {
    dashboard.questSelectedCircleIds = dashboard.questSelectedCircleIds.includes(circleId)
      ? dashboard.questSelectedCircleIds.filter((id) => id !== circleId)
      : [...dashboard.questSelectedCircleIds, circleId]
  }

  return {
    handleQuestImagesChange,
    toggleQuestAudience,
    toggleQuestCircle
  }
}
