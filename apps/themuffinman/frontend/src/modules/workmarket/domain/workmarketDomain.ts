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

const DASHBOARD_TABS = values("calendar", "side-job")
const QUEST_SORT_MODES = values("recommended", "newest", "highest")

export type DashboardTab = typeof DASHBOARD_TABS[number]
export type QuestSortMode = typeof QUEST_SORT_MODES[number]
export type QuestStatusFilter = QuestStatus | "ALL"

export const dashboardTabs: Array<{id: DashboardTab; title: string; description: string}> = [
  {id: "calendar", title: "Calendar", description: "See scheduled work and open days"},
  {id: "side-job", title: "SideJob", description: "Offer jobs, track applications, and open the right flow when needed"}
]
