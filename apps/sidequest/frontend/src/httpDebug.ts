import {currentUser, token} from "./auth.ts";

export const buildRequestDebugInfo = (endpoint: string, method: string, error: unknown) => {
  const lines = [
    `request: ${method} ${endpoint}`,
    `auth: ${token.value ? "token present" : "no token"}`,
    `user: ${currentUser.value ? `${currentUser.value.username} (${currentUser.value.role})` : "not signed in"}`
  ]

  if (error instanceof Error) {
    lines.push(`error: ${error.message}`)
  }

  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { status?: number; data?: unknown } }).response
    if (response?.status) {
      lines.push(`status: ${response.status}`)
    }
    if (response?.data) {
      lines.push(`response: ${JSON.stringify(response.data)}`)
    }
  }

  return lines
}

export const formatDebugInfo = (lines: string[]) => lines.join("\n")
