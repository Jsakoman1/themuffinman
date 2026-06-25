import {onMounted} from "vue"
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

  const closeQuestDetail = () => {
    void page.router.push(routeForNavigationTarget(page.detail.value?.sections?.navigation?.listNavigation) || "/work")
  }

  return {
    ...page,
    ...edit,
    closeQuestDetail
  }
}
