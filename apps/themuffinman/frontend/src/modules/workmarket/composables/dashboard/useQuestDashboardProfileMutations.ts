import {currentUser} from "../../../../auth.ts"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {updateSessionUser} from "../../../../services/sessionService.ts"
import {compressProfileAvatar} from "../../../../shared/imageCompression.ts"
import {PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE} from "../../../../shared/clientMessages.ts"
import {workmarketApi} from "../../api/workmarketApi.ts"
import type {QuestDashboardState} from "../useQuestDashboardState.ts"

export const useQuestDashboardProfileMutations = (state: QuestDashboardState) => {
  const saveProfile = async () => {
    if (!currentUser.value) {
      return
    }

    try {
      const response = await workmarketApi.updateCurrentAppUser({
        email: currentUser.value.email,
        username: state.profileUsername.value.trim(),
        profileDescription: state.profileDescription.value.trim(),
        profileAvatarDataUrl: state.profileAvatarDataUrl.value || null
      })

      const updatedUser = {
        email: response.email,
        username: response.username,
        profileDescription: response.profileDescription,
        profileAvatarDataUrl: response.profileAvatarDataUrl,
        createdAt: response.createdAt ?? currentUser.value.createdAt,
        role: response.role ?? currentUser.value.role
      }

      updateSessionUser(updatedUser)
      state.profileUsername.value = response.username
      state.profileDescription.value = response.profileDescription ?? ""
      state.profileAvatarDataUrl.value = response.profileAvatarDataUrl ?? ""
      state.showFeedback("Profile updated.", "success")
      state.closeProfileEditDialog()
    } catch (requestError) {
      state.showFeedback(getApiErrorMessage(requestError, "Could not update profile."), "error")
    }
  }

  const updateProfileAvatarFromFile = async (file: File | null) => {
    if (!file) {
      state.profileAvatarDataUrl.value = ""
      return
    }

    try {
      state.profileAvatarDataUrl.value = await compressProfileAvatar(file)
    } catch {
      state.showFeedback(PROFILE_IMAGE_PROCESSING_ERROR_MESSAGE, "error")
    }
  }

  const clearProfileAvatar = () => {
    state.profileAvatarDataUrl.value = ""
  }

  return {
    saveProfile,
    updateProfileAvatarFromFile,
    clearProfileAvatar
  }
}
