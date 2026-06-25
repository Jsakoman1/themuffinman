import axios from "axios"
import {authHeader} from "../auth.ts"
import {clearSession, token} from "../services/sessionService.ts"

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"

export const api = axios.create({
  baseURL: API_BASE_URL
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const requestUrl = String(error.config?.url ?? "")
    const isAuthSessionCheck = requestUrl.includes("/auth/me")

    if (token.value && status === 401 && isAuthSessionCheck) {
      clearSession()

      const currentPath = window.location.pathname
      if (currentPath !== "/login" && currentPath !== "/register") {
        window.location.assign("/login")
      }
    }

    return Promise.reject(error)
  }
)

export const withAuth = () => ({
  headers: authHeader()
})
