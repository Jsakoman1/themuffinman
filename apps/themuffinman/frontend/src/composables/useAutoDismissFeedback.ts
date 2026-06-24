import {onBeforeUnmount, ref} from "vue"

export const useAutoDismissFeedback = <Tone extends string>(
  durationMs: number,
  defaultTone: Tone
) => {
  const message = ref("")
  const tone = ref<Tone>(defaultTone)
  let timeoutId: number | null = null

  const show = (nextMessage: string, nextTone: Tone) => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId)
    }

    message.value = nextMessage
    tone.value = nextTone
    timeoutId = window.setTimeout(() => {
      message.value = ""
      timeoutId = null
    }, durationMs)
  }

  onBeforeUnmount(() => {
    if (timeoutId !== null) {
      window.clearTimeout(timeoutId)
    }
  })

  return {
    message,
    tone,
    show
  }
}
