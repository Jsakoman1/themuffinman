import type {
  QuestStatus,
} from "../../../contracts/index.ts"

export type {
  AppUserRole,
  CircleRelationStatus,
  QuestAllowedAction,
  QuestApplicationStatus,
  QuestAudience,
  QuestNewsType,
  QuestStatus,
  QuestViewerRelation,
  ReviewRole
} from "../../../contracts/index.ts"

const values = <T extends readonly string[]>(...items: T) => items

const DASHBOARD_TABS = values("overview", "create-job", "find-work", "circles")
const OVERVIEW_FOCUSES = values("active-work", "posted-work", "applied-tasks", "completed")
const QUEST_SORT_MODES = values("recommended", "newest", "highest")

export type DashboardTab = typeof DASHBOARD_TABS[number]
export type OverviewFocus = typeof OVERVIEW_FOCUSES[number]
export type QuestSortMode = typeof QUEST_SORT_MODES[number]
export type QuestStatusFilter = QuestStatus | "ALL"

export const dashboardTabs: Array<{id: DashboardTab; title: string; description: string}> = [
  {id: "overview", title: "Overview", description: ""},
  {id: "create-job", title: "Create job", description: "Post and manage your jobs"},
  {id: "find-work", title: "Find work", description: "Browse open jobs"},
  {id: "circles", title: "Circles", description: "Organize connections into private work groups"}
]
