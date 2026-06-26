import {computed} from "vue"
import type {RouteLocationNormalizedLoaded, Router} from "vue-router"
import {
  parseInstantFromInput
} from "../../../../shared/questSchedule.ts"
import {
  dashboardTabs
} from "../../domain/workmarketDomain.ts"
import {createDashboardDataState} from "./createDashboardDataState.ts"
import {createDashboardErrorState} from "./createDashboardErrorState.ts"
import {createDashboardInteractions} from "./createDashboardInteractions.ts"
import {createDashboardOptionState} from "./createDashboardOptionState.ts"
import {createDashboardProfileState} from "./createDashboardProfileState.ts"
import {createDashboardQuestState} from "./createDashboardQuestState.ts"
import {createDashboardSelectors} from "./createDashboardSelectors.ts"
import {createDashboardTabState} from "./createDashboardTabState.ts"

export const createQuestDashboardStateModules = (
  route: RouteLocationNormalizedLoaded,
  router: Router
) => {
  const tabState = createDashboardTabState(route)
  const dataState = createDashboardDataState()
  const optionState = createDashboardOptionState(dataState.dashboardOptions)
  const profileState = createDashboardProfileState()
  const questState = createDashboardQuestState()

  const selectors = createDashboardSelectors({
    activeTab: tabState.activeTab,
    quests: dataState.quests,
    dashboardMyQuests: dataState.dashboardMyQuests,
    dashboardAvailableQuests: dataState.dashboardAvailableQuests,
    myApplications: dataState.myApplications,
    newsItems: dataState.newsItems,
    dashboardSummary: dataState.dashboardSummary,
    dashboardSections: dataState.dashboardSections,
    incomingCircleRequests: dataState.incomingCircleRequests,
    adminQuestStatusFilter: questState.adminQuestStatusFilter,
    applicationsByQuestId: dataState.applicationsByQuestId,
    questDialogId: questState.questDialogId,
    applicationDialogId: questState.applicationDialogId,
    dashboardTabs
  })

  const adminModeEnabled = computed(() => dataState.dashboardSummary.value?.adminModeEnabled ?? false)

  const interactions = createDashboardInteractions({
    route,
    router,
    adminModeEnabled,
    activeTab: tabState.activeTab,
    isNotificationsDialogOpen: profileState.isNotificationsDialogOpen,
    questDialogId: questState.questDialogId,
    applicationDialogId: questState.applicationDialogId,
    userProfileDialogId: questState.userProfileDialogId,
    isCreateJobDialogOpen: questState.isCreateJobDialogOpen,
    isFindWorkDialogOpen: questState.isFindWorkDialogOpen,
    isOpenWorkDialogOpen: questState.isOpenWorkDialogOpen,
    isApplicationsDialogOpen: questState.isApplicationsDialogOpen,
    applicationMessages: questState.applicationMessages,
    proposedPrices: questState.proposedPrices,
    questDisclosureRefs: questState.questDisclosureRefs,
    editingQuestId: questState.editingQuestId,
    openApplicationsQuestIds: questState.openApplicationsQuestIds,
    showAllApplicationsQuestIds: questState.showAllApplicationsQuestIds,
    editingApplicationId: questState.editingApplicationId,
    editApplicationMessage: questState.editApplicationMessage,
    editApplicationPrice: questState.editApplicationPrice,
    editQuestTitle: questState.editQuestTitle,
    editQuestDescription: questState.editQuestDescription,
    editQuestAwardAmount: questState.editQuestAwardAmount,
    editQuestScheduledAt: questState.editQuestScheduledAt,
    editQuestEndsAt: questState.editQuestEndsAt,
    editQuestTermMode: questState.editQuestTermMode,
    editQuestTermFixed: questState.editQuestTermFixed,
    editQuestAudience: questState.editQuestAudience,
    editQuestSelectedCircleIds: questState.editQuestSelectedCircleIds,
    editQuestLocationSource: questState.editQuestLocationSource,
    editQuestLocationCountry: questState.editQuestLocationCountry,
    editQuestLocationLocality: questState.editQuestLocationLocality,
    editQuestLocationPostalCode: questState.editQuestLocationPostalCode,
    editQuestLocationStreet: questState.editQuestLocationStreet,
    editQuestLocationHouseNumber: questState.editQuestLocationHouseNumber,
    editQuestLocationVisibility: questState.editQuestLocationVisibility,
    editQuestCreatorId: questState.editQuestCreatorId,
    editQuestStatus: questState.editQuestStatus,
    editQuestImages: questState.editQuestImages,
    questTitle: questState.questTitle,
    questDescription: questState.questDescription,
    questAwardAmount: questState.questAwardAmount,
    questScheduledAt: questState.questScheduledAt,
    questEndsAt: questState.questEndsAt,
    questTermMode: questState.questTermMode,
    questTermFixed: questState.questTermFixed,
    questAudience: questState.questAudience,
    questSelectedCircleIds: questState.questSelectedCircleIds,
    questLocationSource: questState.questLocationSource,
    questLocationCountry: questState.questLocationCountry,
    questLocationLocality: questState.questLocationLocality,
    questLocationPostalCode: questState.questLocationPostalCode,
    questLocationStreet: questState.questLocationStreet,
    questLocationHouseNumber: questState.questLocationHouseNumber,
    questLocationVisibility: questState.questLocationVisibility,
    questCreatorId: questState.questCreatorId,
    questImages: questState.questImages,
    showFeedback: profileState.showFeedback,
    successPulseTarget: profileState.successPulseTarget,
    copiedDebugBanner: profileState.copiedDebugBanner,
    questForId: selectors.questForId,
    myApplications: dataState.myApplications
  })

  const errorState = createDashboardErrorState({
    questsError: dataState.questsError,
    questsErrorDetails: dataState.questsErrorDetails,
    applicationsError: dataState.applicationsError,
    applicationsErrorDetails: dataState.applicationsErrorDetails,
    newsError: dataState.newsError,
    newsErrorDetails: dataState.newsErrorDetails,
    usersError: dataState.usersError,
    usersErrorDetails: dataState.usersErrorDetails
  })

  return {
    tabState,
    dataState,
    optionState,
    profileState,
    questState,
    selectors,
    interactions,
    errorState,
    adminModeEnabled,
    parseInstantFromInput,
    dashboardTabs
  }
}
