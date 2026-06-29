import {computed, onMounted} from "vue"
import {routeForNavigationTarget} from "../shared/navigationTargets.ts"
import {useQuestDetailPage} from "./useQuestDetailPage.ts"
import {useQuestDetailEdit} from "./quest-detail/useQuestDetailEdit.ts"

export const useQuestDetailView = () => {
  const page = useQuestDetailPage()
  const edit = useQuestDetailEdit({
    quest: page.quest,
    error: page.error,
    isSaving: page.isSaving,
    init: page.init
  })

  onMounted(page.init)
  onMounted(edit.loadEditMetadata)

  const visibleManagementSection = computed(() => {
    if (!page.managementSection.value || edit.canEdit.value) {
      return {
        editVisible: false,
        deleteVisible: false,
        postingSettingsVisible: false,
        audienceLabel: null,
        visibleToCirclesLabel: null,
      }
    }

    return page.managementSection.value
  })
  const approvedApplications = computed(() => page.applicationsView.value?.approvedApplications ?? [])
  const remainingApplications = computed(() => page.applications.value)
  const isOwnerView = computed(() => edit.canEdit.value)
  const showApplicationsSection = computed(() => isOwnerView.value && !!page.applicationsView.value)
  const showOfferSection = computed(() => !isOwnerView.value && (
    page.canApply.value
    || page.applicationSentVisible.value
    || !!page.myApplication.value
  ))
  const showMyApplicationAside = computed(() => isOwnerView.value && !showOfferSection.value)
  const showOverview = computed(() => true)
  const showOverviewStatus = computed(() => isOwnerView.value)

  const closeQuestDetail = () => {
    void page.router.push(routeForNavigationTarget(page.detail.value?.sections?.navigation?.listNavigation) || "/work")
  }

  return {
    ...page,
    ...edit,
    visibleManagementSection,
    approvedApplications,
    remainingApplications,
    isOwnerView,
    showApplicationsSection,
    showOfferSection,
    showMyApplicationAside,
    showOverview,
    showOverviewStatus,
    closeQuestDetail
  }
}
