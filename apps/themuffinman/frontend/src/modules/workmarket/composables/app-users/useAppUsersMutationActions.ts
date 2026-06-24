import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import type {AppUser} from "../../api/workmarketApi.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {AppUsersPageState} from "../useAppUsersPageState.ts"

export const useAppUsersMutationActions = (
  state: AppUsersPageState,
  dependencies: {fetchAppUsers: () => Promise<void>}
) => {
  const resetCreateForm = () => {
    state.email.value = ""
    state.username.value = ""
    state.password.value = ""
    state.role.value = "USER"
  }

  const resetEditForm = () => {
    state.editingAppUserId.value = null
    state.editAppUserEmail.value = ""
    state.editAppUserUsername.value = ""
    state.editAppUserRole.value = "USER"
    state.editAppUserPassword.value = ""
  }

  const createAppUser = async () => {
    try {
      const result = await workmarketApi.createAppUser({
        email: state.email.value.trim(),
        username: state.username.value.trim(),
        password: state.password.value,
        role: state.role.value
      })

      resetCreateForm()
      state.showFeedback(result.message, "success")
      state.closeCreateUserDialog()
      await dependencies.fetchAppUsers()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not create user."), "error")
    }
  }

  const deleteAppUser = async (id: number) => {
    try {
      const result = await workmarketApi.deleteAppUser(id)
      state.showFeedback(result.message, "success")
      await dependencies.fetchAppUsers()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not delete user."), "error")
    }
  }

  const handleDelete = async (id: number) => {
    state.openDeleteUserDialog(id)
  }

  const cancelDelete = () => {
    state.closeDeleteUserDialog()
  }

  const confirmDelete = async () => {
    const userId = state.deleteCandidateUserId.value
    if (userId === null) {
      return
    }

    state.closeDeleteUserDialog()
    await deleteAppUser(userId)
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
      const result = await workmarketApi.updateAppUser(state.editingAppUserId.value, {
        email: state.editAppUserEmail.value.trim(),
        username: state.editAppUserUsername.value.trim(),
        role: state.editAppUserRole.value,
        password: state.editAppUserPassword.value || undefined
      })

      resetEditForm()
      state.showFeedback(result.message, "success")
      await dependencies.fetchAppUsers()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not update user."), "error")
    }
  }

  const cancelEdit = () => {
    state.editingAppUserId.value = null
  }

  return {
    createAppUser,
    deleteAppUser,
    handleDelete,
    cancelDelete,
    confirmDelete,
    startEdit,
    updateAppUser,
    cancelEdit
  }
}
