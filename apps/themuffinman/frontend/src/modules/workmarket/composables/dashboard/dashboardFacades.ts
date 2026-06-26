import type {QuestDashboard} from "../useQuestDashboard.ts"

export type DashboardQuestCreateFacade = Pick<
  QuestDashboard,
  | "createQuest"
  | "successPulseTarget"
  | "questTitle"
  | "questDescription"
  | "questAwardAmount"
  | "questScheduledAt"
  | "questEndsAt"
  | "questTermMode"
  | "questAudience"
  | "questAudienceOptions"
  | "questSelectedCircleIds"
  | "questLocationSource"
  | "questLocationCountry"
  | "questLocationLocality"
  | "questLocationPostalCode"
  | "questLocationStreet"
  | "questLocationHouseNumber"
  | "questLocationVisibility"
  | "questCreatorId"
  | "questImages"
  | "circles"
  | "appUsers"
  | "adminModeEnabled"
  | "setQuestTermMode"
  | "addQuestImages"
  | "removeQuestImage"
  | "questLocationVisibilityOptions"
>

export type DashboardQuestEditFacade = Pick<
  QuestDashboard,
  | "saveEditedQuest"
  | "cancelEditingQuest"
  | "selectedQuestDialog"
  | "editQuestTitle"
  | "editQuestDescription"
  | "editQuestAwardAmount"
  | "editQuestScheduledAt"
  | "editQuestEndsAt"
  | "editQuestTermMode"
  | "editQuestAudience"
  | "editQuestSelectedCircleIds"
  | "editQuestLocationSource"
  | "editQuestLocationCountry"
  | "editQuestLocationLocality"
  | "editQuestLocationPostalCode"
  | "editQuestLocationStreet"
  | "editQuestLocationHouseNumber"
  | "editQuestLocationVisibility"
  | "editQuestCreatorId"
  | "editQuestStatus"
  | "editQuestImages"
  | "questAudienceOptions"
  | "questLocationVisibilityOptions"
  | "circles"
  | "appUsers"
  | "questStatusOptions"
  | "adminModeEnabled"
  | "setEditQuestTermMode"
  | "addEditQuestImages"
  | "removeEditQuestImage"
>

export type DashboardApplicationEditFacade = Pick<
  QuestDashboard,
  | "editApplicationMessage"
  | "editApplicationPrice"
  | "saveEditedApplication"
  | "closeApplicationDialog"
  | "selectedApplicationDialog"
  | "questForId"
  | "openUserProfileDialog"
  | "withdrawApplication"
>

export type DashboardApplicationApplyFacade = Pick<
  QuestDashboard,
  | "applicationMessages"
  | "proposedPrices"
  | "applyForQuest"
>

export type DashboardQuestApplicationsFacade = Pick<
  QuestDashboard,
  | "openUserProfileDialog"
  | "canRevealHiddenApplicationsForQuest"
  | "toggleApplicationRevealForQuest"
  | "applicationRevealLabel"
  | "hiddenApplicationsCountForQuest"
>

export type DashboardQuestListFacade = Pick<
  QuestDashboard,
  | "dashboardMyQuests"
  | "dashboardSections"
  | "myApplications"
  | "successPulseTarget"
  | "questCreatorUsernameForQuest"
  | "openApplicationDialog"
>

export type DashboardApplicationsDialogFacade = Pick<
  QuestDashboard,
  | "isApplicationsDialogOpen"
  | "closeApplicationsDialog"
  | "pendingWorkApplications"
> & DashboardQuestListFacade

export type DashboardOpenWorkDialogFacade = Pick<
  QuestDashboard,
  | "dashboardSections"
  | "isOpenWorkDialogOpen"
  | "closeOpenWorkDialog"
> & DashboardQuestListFacade

export type DashboardWorkPlannerFacade = Pick<
  QuestDashboard,
  | "dashboardSections"
  | "accountCreatedAt"
  | "questScheduledAt"
  | "questEndsAt"
  | "setQuestTermMode"
  | "openCreateJobDialog"
  | "openQuestDialog"
  | "questForId"
>

export type DashboardQuestDialogFacade = Pick<
  QuestDashboard,
  | "selectedQuestDialog"
  | "applicationsForQuest"
  | "applicationMessages"
  | "myApplications"
  | "featuredApplicationForQuest"
  | "startEditingQuest"
  | "deleteQuest"
  | "closeQuestDialog"
  | "approveApplication"
  | "declineApplication"
  | "confirmQuestTermChange"
  | "rejectQuestTermChange"
  | "openApplicationDialog"
  | "updateQuestStatus"
  | "reopenQuest"
> & DashboardQuestEditFacade & DashboardApplicationApplyFacade & DashboardQuestApplicationsFacade

export type DashboardAdminFacade = Pick<
  QuestDashboard,
  | "refreshDashboardData"
  | "adminQuestStatusFilter"
  | "questStatusFilterOptions"
  | "questAudienceFilterOptions"
  | "successPulseTarget"
  | "deleteQuest"
>

export type DashboardFindQuestsFacade = Pick<
  QuestDashboard,
  | "questSortOptions"
  | "questSearchDefaults"
  | "questAudienceFilterOptions"
>
