import {ref} from "vue"
import type {AppUserRole} from "../shared/sidequestDomain.ts"

export interface SessionUser {
  id: number
  email: string
  username: string
  profileDescription: string | null
  profileAvatarDataUrl: string | null
  createdAt: string
  role: AppUserRole
  token: string | null
}

const loadUser = () => {
  const storedUser = localStorage.getItem("user")
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
  return localStorage.getItem("token")
}

export const currentUser = ref<SessionUser | null>(loadUser())
export const token = ref<string | null>(loadToken())

export const saveSession = (user: SessionUser) => {
  currentUser.value = user
  token.value = user.token
  localStorage.setItem("user", JSON.stringify(user))

  if (user.token) {
    localStorage.setItem("token", user.token)
    return
  }

  localStorage.removeItem("token")
}

export const clearSession = () => {
  currentUser.value = null
  token.value = null
  localStorage.removeItem("user")
  localStorage.removeItem("token")
}
