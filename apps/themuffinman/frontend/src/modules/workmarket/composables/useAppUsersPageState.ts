import {createAppUsersDataState} from "./app-users/createAppUsersDataState.ts"
import {createAppUsersFormState} from "./app-users/createAppUsersFormState.ts"
import {createAppUsersSearchState} from "./app-users/createAppUsersSearchState.ts"
import {createAppUsersUiState} from "./app-users/createAppUsersUiState.ts"

export const useAppUsersPageState = () => {
  const dataState = createAppUsersDataState()
  const formState = createAppUsersFormState()
  const uiState = createAppUsersUiState()
  const searchState = createAppUsersSearchState()

  return {
    ...dataState,
    ...formState,
    ...uiState,
    ...searchState
  }
}

export type AppUsersPageState = ReturnType<typeof useAppUsersPageState>
