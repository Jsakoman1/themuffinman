import type {AppearancePreference} from "../modules/app-shell/api/userShellApi.ts"

const APPEARANCE_STORAGE_KEY = "appearance-theme"
const DEFAULT_THEME: AppearancePreference["theme"] = "SYSTEM"

const isAppearanceTheme = (value: unknown): value is AppearancePreference["theme"] =>
  value === "SYSTEM" || value === "DARK" || value === "LIGHT"

export const readCachedAppearanceTheme = (): AppearancePreference["theme"] => {
  try {
    const cached = localStorage.getItem(APPEARANCE_STORAGE_KEY)
    return isAppearanceTheme(cached) ? cached : DEFAULT_THEME
  } catch {
    return DEFAULT_THEME
  }
}

export const applyAppearanceTheme = (theme: AppearancePreference["theme"]) => {
  document.documentElement.dataset.theme = theme.toLowerCase()
  document.documentElement.style.colorScheme = theme === "SYSTEM" ? "light dark" : theme.toLowerCase()

  try {
    localStorage.setItem(APPEARANCE_STORAGE_KEY, theme)
  } catch {
    // Theme application must still work when browser storage is unavailable.
  }
}
