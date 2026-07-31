import {userShellApi} from "./userShellApi.ts"

export const profileApi = {
  getOnboardingProgress: userShellApi.getOnboardingProgress.bind(userShellApi),
  updateOnboardingProgress: userShellApi.updateOnboardingProgress.bind(userShellApi),
  resetOnboardingProgress: userShellApi.resetOnboardingProgress.bind(userShellApi),
  getAppearancePreference: userShellApi.getAppearancePreference.bind(userShellApi),
  updateAppearancePreference: userShellApi.updateAppearancePreference.bind(userShellApi)
}
