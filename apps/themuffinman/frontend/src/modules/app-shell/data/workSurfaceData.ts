import type {AppSurfaceId} from "../shellDefinitions.ts"
import type {ShellSurfaceRow, ShellSurfaceViewModel} from "../shellSurfaceData.ts"
import {userShellApi} from "../api/userShellApi.ts"
import {formatDate, formatDateTime, formatNumber} from "../../../services/formatters.ts"
import {resolveSurfaceDetailRoute} from "../shellRouteRegistry.ts"

const questRoute = (id: number) => resolveSurfaceDetailRoute("work-quests", id) ?? {path: `/work/quests/${id}`}
const applicationRoute = (id: number) => resolveSurfaceDetailRoute("work-applications", id) ?? {path: `/work/applications/${id}`}
const questRow = (quest: Awaited<ReturnType<typeof userShellApi.getDashboard>>["availableQuests"][number]): ShellSurfaceRow => ({id: `quest-${quest.id}`, title: quest.title, description: quest.presentation.statusLabel, meta: quest.scheduledAt ? formatDateTime(quest.scheduledAt) : quest.presentation.timeTypeLabel, badge: quest.status, to: questRoute(quest.id)})
const applicationRow = (item: Awaited<ReturnType<typeof userShellApi.getMyApplications>>["items"][number]): ShellSurfaceRow => ({id: `application-${item.id}`, title: item.questTitle, description: item.presentation.statusLabel, meta: `${item.questCreatorUsername} · ${formatDate(item.createdAt)}`, badge: item.status, to: applicationRoute(item.id)})

export const loadWorkSurfaceData = async (surfaceId: AppSurfaceId): Promise<ShellSurfaceViewModel> => {
  const [dashboard, applications] = await Promise.all([userShellApi.getDashboard(), surfaceId === "work-applications" || surfaceId === "work-quests" ? userShellApi.getMyApplications() : Promise.resolve(null)])
  const discover = dashboard.availableQuests.map(questRow)
  const mine = (dashboard.sections.recentMyQuests.length ? dashboard.sections.recentMyQuests : dashboard.myQuests).map(questRow)
  const applied = (applications?.items ?? (dashboard.sections.recentMyApplications.length ? dashboard.sections.recentMyApplications : dashboard.myApplications)).map(applicationRow)
  if (surfaceId === "work-quests") return {metrics: [{label: "Active quests", value: formatNumber(dashboard.summary.activeMyQuestsCount, "en-US"), detail: "Your owned quests still in motion.", tone: "emphasis"}], sections: [{title: "My work", description: "Your active work.", emptyState: "No owned quests are available right now.", rows: mine}, {title: "My applications", description: "Work you applied for.", emptyState: "No applications are visible right now.", rows: applied}]}
  if (surfaceId === "work-applications") return {metrics: [{label: "Pending applications", value: formatNumber(dashboard.summary.pendingWorkApplicationsCount, "en-US"), detail: "Applications waiting for progress.", tone: "emphasis"}], sections: [{title: "My applications", description: "Applications with their current status.", emptyState: "No applications are visible right now.", rows: applied}]}
  return {metrics: [{label: "Open quests", value: formatNumber(dashboard.summary.openQuestCount, "en-US"), detail: "Available work visible right now.", tone: "emphasis"}], sections: [{title: "Discover", description: "Available work from the backend dashboard.", emptyState: "No open work is available right now.", rows: discover}, {title: "My work", description: "Owned work.", emptyState: "No owned quests are visible right now.", rows: mine}, {title: "My applications", description: "Applications you submitted.", emptyState: "No applications are visible right now.", rows: applied}]}
}
