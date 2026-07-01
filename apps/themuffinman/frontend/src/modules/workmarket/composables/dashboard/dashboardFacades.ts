import type {QuestDashboard} from "../useQuestDashboard.ts"

export type DashboardQuestEditFacade = Pick<
  QuestDashboard,
  | "saveEditedQuest"
  | "cancelEditingQuest"
  | "selectedQuestDialog"
  | "editQuestTitle"
  | "editQuestDescription"
  | "editQuestAwardAmount"
  | "editQuestAssigneeTarget"
  | "editQuestShowApprovedApplicants"
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
  | "selectedApplicationDetail"
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

export type DashboardQuestDialogFacade = Pick<
  QuestDashboard,
  | "selectedQuestDialog"
  | "selectedQuestDetail"
  | "applicationsForQuest"
  | "approvedApplicationsForQuest"
  | "applicationMessages"
  | "myApplications"
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
