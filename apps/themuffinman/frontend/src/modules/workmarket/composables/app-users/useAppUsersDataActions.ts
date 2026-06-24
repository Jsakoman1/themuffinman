import {API_BASE_URL} from "../../../../api/httpClient.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {buildRequestDebugInfo, formatDebugInfo} from "../../../../httpDebug.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {AppUsersPageState} from "../useAppUsersPageState.ts"

export const useAppUsersDataActions = (state: AppUsersPageState) => {
  const copyDebugInfo = async () => {
    if (!state.pageErrorDetails.value.length) {
      return
    }

    await navigator.clipboard.writeText(formatDebugInfo(state.pageErrorDetails.value))
    state.showCopiedDebug()
  }

  const fetchAppUsers = async () => {
    state.isLoadingUsers.value = true
    state.pageError.value = ""
    state.pageErrorDetails.value = []

    try {
      const [appUsers, options] = await Promise.all([
        workmarketApi.getAppUsers(state.userSearch.value),
        workmarketApi.getAppUserOptions()
      ])
      state.appUsers.value = appUsers
      state.roleOptions.value = options.appUserRoles
    } catch (error) {
      state.pageError.value = getApiErrorMessage(error, "Could not load users.")
      state.pageErrorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/app_users`, "GET", error)
      state.roleOptions.value = []
    } finally {
      state.isLoadingUsers.value = false
    }
  }

  return {
    copyDebugInfo,
    fetchAppUsers
  }
}
