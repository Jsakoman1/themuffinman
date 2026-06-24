import {onBeforeUnmount, ref} from "vue"

export const useTimedBanner = (timeoutMs = 1800) => {
  const message = ref("")
  const tone = ref<"success" | "warning">("success")
  let timeoutId: number | undefined

  const clear = () => {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId)
      timeoutId = undefined
    }

    message.value = ""
  }

  const show = (nextMessage: string, nextTone: "success" | "warning" = "success") => {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId)
    }

    message.value = nextMessage
    tone.value = nextTone
    timeoutId = window.setTimeout(() => {
      message.value = ""
      timeoutId = undefined
    }, timeoutMs)
  }

  onBeforeUnmount(() => {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId)
    }
  })

  return {
    message,
    tone,
    show,
    clear
  }
}
