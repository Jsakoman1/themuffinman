import axios from "axios"
import type {ApiErrorResponseDTO} from "../contracts/index.ts"

const isApiErrorResponse = (value: unknown): value is ApiErrorResponseDTO => {
  if (!value || typeof value !== "object") {
    return false
  }

  const candidate = value as Partial<ApiErrorResponseDTO>
  return typeof candidate.message === "string" && Array.isArray(candidate.fieldErrors)
}

export const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (axios.isAxiosError(error) && isApiErrorResponse(error.response?.data)) {
    const apiError = error.response.data
    const fieldMessage = apiError.fieldErrors.find((fieldError) => !!fieldError.message)?.message
    return fieldMessage || apiError.message || fallback
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return fallback
}
