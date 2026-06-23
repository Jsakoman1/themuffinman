import {reactive} from "vue"
import {useAppUsersPageState} from "./useAppUsersPageState.ts"
import {useAppUsersPageActions} from "./useAppUsersPageActions.ts"

export const useAppUsersPage = () => {
  const state = useAppUsersPageState()
  const actions = useAppUsersPageActions(state)

  return reactive({
    ...state,
    ...actions
  })
}
