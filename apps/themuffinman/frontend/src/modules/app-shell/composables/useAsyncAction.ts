import {ref} from "vue"

export const useAsyncAction = () => {
  const pending = ref(false)
  const error = ref("")

  const execute = async <T>(operation: () => Promise<T>, fallbackMessage: string): Promise<T | undefined> => {
    pending.value = true
    error.value = ""
    try {
      return await operation()
    } catch {
      error.value = fallbackMessage
      return undefined
    } finally {
      pending.value = false
    }
  }

  return {pending, error, execute}
}
