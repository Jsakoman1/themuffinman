import {isAdmin} from "../auth.ts"
import {API_BASE_URL} from "../api/httpClient.ts"
import {sidequestApi, type AppUser} from "../api/sidequestApi.ts"
import {buildRequestDebugInfo, formatDebugInfo} from "../httpDebug.ts"
import type {AppUsersPageState} from "./useAppUsersPageState.ts"

export const useAppUsersPageActions = (state: AppUsersPageState) => {
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
      state.appUsers.value = await sidequestApi.getAppUsers()
    } catch (error) {
      state.pageError.value = "Could not load users."
      state.pageErrorDetails.value = buildRequestDebugInfo(`${API_BASE_URL}/app_users`, "GET", error)
    } finally {
      state.isLoadingUsers.value = false
    }
  }

  const createAppUser = async () => {
    try {
      await sidequestApi.createAppUser({
        email: state.email.value.trim(),
        username: state.username.value.trim(),
        password: state.password.value,
        role: state.role.value
      })

      state.email.value = ""
      state.username.value = ""
      state.password.value = ""
      state.role.value = "USER"
      state.showFeedback("User created.", "success")
      state.closeCreateUserDialog()
      await fetchAppUsers()
    } catch {
      state.showFeedback("Could not create user.", "error")
    }
  }

  const deleteAppUser = async (id: number) => {
    try {
      await sidequestApi.deleteAppUser(id)
      state.showFeedback("User deleted.", "success")
      await fetchAppUsers()
    } catch {
      state.showFeedback("Could not delete user.", "error")
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this user?")) {
      return
    }

    await deleteAppUser(id)
  }

  const startEdit = (appUser: AppUser) => {
    state.editingAppUserId.value = appUser.id
    state.editAppUserEmail.value = appUser.email
    state.editAppUserUsername.value = appUser.username
    state.editAppUserRole.value = appUser.role
    state.editAppUserPassword.value = ""
  }

  const updateAppUser = async () => {
    if (state.editingAppUserId.value === null) {
      return
    }

    try {
      await sidequestApi.updateAppUser(state.editingAppUserId.value, {
        email: state.editAppUserEmail.value.trim(),
        username: state.editAppUserUsername.value.trim(),
        role: state.editAppUserRole.value,
        password: state.editAppUserPassword.value || undefined
      })

      state.editingAppUserId.value = null
      state.editAppUserEmail.value = ""
      state.editAppUserUsername.value = ""
      state.editAppUserRole.value = "USER"
      state.editAppUserPassword.value = ""
      state.showFeedback("User updated.", "success")
      await fetchAppUsers()
    } catch {
      state.showFeedback("Could not update user.", "error")
    }
  }

  const cancelEdit = () => {
    state.editingAppUserId.value = null
  }

  const init = async () => {
    if (isAdmin()) {
      await fetchAppUsers()
    }
  }

  return {
    copyDebugInfo,
    fetchAppUsers,
    createAppUser,
    deleteAppUser,
    handleDelete,
    startEdit,
    updateAppUser,
    cancelEdit,
    init,
    isAdmin
  }
}
