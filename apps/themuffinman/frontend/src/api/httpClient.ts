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
    const isPublicAuthRequest = ["/auth/login", "/auth/register", "/auth/password-recovery", "/auth/password-reset"]
      .some(path => requestUrl.includes(path))

    if (token.value && status === 401 && !isPublicAuthRequest) {
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

export type MutationRequestOptions = {
  requestId?: string
  correlationId?: string
  idempotencyKey?: string
  operationKey?: string
}

const newRequestId = () => crypto.randomUUID()

export const withMutation = (options: MutationRequestOptions = {}) => {
  const requestId = options.requestId || newRequestId()
  return {
    headers: {
      ...authHeader(),
      "X-Request-Id": requestId,
      "X-Correlation-Id": options.correlationId || requestId,
      ...(options.idempotencyKey ? {"Idempotency-Key": options.idempotencyKey} : {}),
      ...(options.operationKey ? {"X-Operation-Key": options.operationKey} : {})
    }
  }
}

export const correlationIdFromError = (error: unknown): string | undefined => {
  const response = (error as { response?: { data?: { correlationId?: unknown }, headers?: Record<string, unknown> } } | null)?.response
  const bodyCorrelationId = response?.data?.correlationId
  if (typeof bodyCorrelationId === "string" && bodyCorrelationId.length > 0) {
    return bodyCorrelationId
  }
  const headerCorrelationId = response?.headers?.["x-correlation-id"]
  return typeof headerCorrelationId === "string" && headerCorrelationId.length > 0 ? headerCorrelationId : undefined
}
