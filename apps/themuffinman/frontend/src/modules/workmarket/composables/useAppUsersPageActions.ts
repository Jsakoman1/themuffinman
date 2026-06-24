import type {AppUsersPageState} from "./useAppUsersPageState.ts"
import {useDebouncedWatch} from "../../../composables/useDebouncedWatch.ts"
import {useAppUsersDataActions} from "./app-users/useAppUsersDataActions.ts"
import {useAppUsersMutationActions} from "./app-users/useAppUsersMutationActions.ts"

export const useAppUsersPageActions = (state: AppUsersPageState) => {
  const dataActions = useAppUsersDataActions(state)
  const mutationActions = useAppUsersMutationActions(state, {
    fetchAppUsers: dataActions.fetchAppUsers
  })

  useDebouncedWatch(state.userSearch, () => {
    void dataActions.fetchAppUsers()
  }, 250)

  const init = async () => {
    await dataActions.fetchAppUsers()
  }

  return {
    ...dataActions,
    ...mutationActions,
    init
  }
}
