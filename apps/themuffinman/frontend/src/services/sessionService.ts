import {ref} from "vue"
import type {AuthResponse} from "../modules/identity/api/authApi.ts"

const USER_STORAGE_KEY = "user"
const TOKEN_STORAGE_KEY = "token"

export type SessionUser = AuthResponse

const loadUser = () => {
  const storedUser = localStorage.getItem(USER_STORAGE_KEY)
  if (!storedUser) {
    return null
  }

  try {
    const parsed = JSON.parse(storedUser) as Partial<SessionUser>
    if (typeof parsed.id !== "number" || typeof parsed.email !== "string" || typeof parsed.username !== "string") {
      return null
    }

    return {
      id: parsed.id,
      email: parsed.email,
      username: parsed.username,
      profileDescription: parsed.profileDescription ?? null,
      profileAvatarDataUrl: parsed.profileAvatarDataUrl ?? null,
      createdAt: typeof parsed.createdAt === "string" ? parsed.createdAt : new Date().toISOString(),
      role: parsed.role ?? "USER",
      token: parsed.token ?? null
    }
  } catch {
    return null
  }
}

const loadToken = () => {
  if (currentUser.value?.token) {
    return currentUser.value.token
  }

  return localStorage.getItem(TOKEN_STORAGE_KEY)
}

export const currentUser = ref<SessionUser | null>(loadUser())
export const token = ref<string | null>(loadToken())

export const saveSession = (user: SessionUser) => {
  currentUser.value = user
  token.value = user.token
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))

  if (user.token) {
    localStorage.setItem(TOKEN_STORAGE_KEY, user.token)
    return
  }

  localStorage.removeItem(TOKEN_STORAGE_KEY)
}

export const updateSessionUser = (updates: Partial<Omit<SessionUser, "id" | "token">>) => {
  if (!currentUser.value) {
    return
  }

  saveSession({
    ...currentUser.value,
    ...updates,
    token: token.value
  })
}

export const clearSession = () => {
  currentUser.value = null
  token.value = null
  localStorage.removeItem(USER_STORAGE_KEY)
  localStorage.removeItem(TOKEN_STORAGE_KEY)
}
