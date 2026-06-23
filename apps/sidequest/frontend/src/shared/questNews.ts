import type {QuestNewsType} from "./sidequestDomain.ts"

export type {QuestNewsType} from "./sidequestDomain.ts"

const questNewsTypeLabels: Record<QuestNewsType, string> = {
  APPLICATION_CREATED: "New application",
  APPLICATION_UPDATED: "Application updated",
  APPLICATION_WITHDRAWN: "Application withdrawn",
  APPLICATION_APPROVED: "Application approved",
  APPLICATION_DECLINED: "Application declined",
  QUEST_TERM_CONFIRMATION_REQUESTED: "Time confirmation needed",
  QUEST_TERM_CONFIRMED: "Time confirmed",
  QUEST_TERM_REJECTED: "Time rejected",
  QUEST_STARTED: "Quest started",
  QUEST_COMPLETED: "Quest completed",
  QUEST_REOPENED: "Quest reopened",
  QUEST_DELETED: "Quest deleted",
  CIRCLE_REQUEST_ACCEPTED: "Circle request accepted"
}

const questNewsTypeToneClasses: Record<QuestNewsType, string> = {
  APPLICATION_CREATED: "badge--accent",
  APPLICATION_UPDATED: "badge--accent",
  APPLICATION_WITHDRAWN: "badge--danger",
  APPLICATION_APPROVED: "badge--success",
  APPLICATION_DECLINED: "badge--danger",
  QUEST_TERM_CONFIRMATION_REQUESTED: "badge--warning",
  QUEST_TERM_CONFIRMED: "badge--success",
  QUEST_TERM_REJECTED: "badge--danger",
  QUEST_STARTED: "badge--accent",
  QUEST_COMPLETED: "badge--success",
  QUEST_REOPENED: "badge--warning",
  QUEST_DELETED: "badge--danger",
  CIRCLE_REQUEST_ACCEPTED: "badge--success"
}

export const formatQuestNewsType = (type: QuestNewsType) => questNewsTypeLabels[type] ?? type

export const questNewsBadgeClass = (type: QuestNewsType) => questNewsTypeToneClasses[type] ?? "badge--accent"
