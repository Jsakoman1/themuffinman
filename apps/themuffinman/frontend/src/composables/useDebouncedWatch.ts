import {onBeforeUnmount, watch, type WatchSource} from "vue"

export const useDebouncedWatch = <T>(
  source: WatchSource<T>,
  effect: (value: T) => void,
  delayMs = 250
) => {
  let timeoutId: number | undefined

  watch(source, (value) => {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId)
    }

    timeoutId = window.setTimeout(() => {
      effect(value)
    }, delayMs)
  })

  onBeforeUnmount(() => {
    if (timeoutId !== undefined) {
      window.clearTimeout(timeoutId)
    }
  })
}
