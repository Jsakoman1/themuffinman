import {ref} from "vue"

export const useAsyncAction = () => {
  const pending = ref(false)
  const error = ref("")
  const completed = ref(false)

  const execute = async <T>(operation: () => Promise<T>, fallbackMessage: string): Promise<T | undefined> => {
    pending.value = true
    error.value = ""
    completed.value = false
    try {
      const result = await operation()
      completed.value = true
      return result
    } catch {
      error.value = fallbackMessage
      return undefined
    } finally {
      pending.value = false
    }
  }

  return {pending, error, completed, execute, interactionModel: "pending-success-error" as const}
}
