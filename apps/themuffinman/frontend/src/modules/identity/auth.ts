import {clearSession, currentUser, saveSession, token, updateSessionUser} from "../../services/sessionService.ts"
import type {SessionUser} from "../../services/sessionService.ts"

export {currentUser, token, updateSessionUser}

export const loginUser = (user: SessionUser) => {
  saveSession(user)
}

export const logoutUser = () => {
  clearSession()
}

export const authHeader = () => {
  if (!token.value) {
    return {}
  }

  return {
    Authorization: `Bearer ${token.value}`
  }
}

export const isLoggedIn = () => {
  return token.value !== null
}

export const isAdmin = () => {
  return currentUser.value?.role === "ADMIN"
}
