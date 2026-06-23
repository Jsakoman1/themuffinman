import {watch, type Ref} from "vue"
import {useTimedBanner} from "./useTimedBanner.ts"

export const useDialogActionState = <T>(source: Ref<T | null | undefined>, reset: () => void) => {
  const banner = useTimedBanner()

  watch(source, () => {
    reset()
    banner.clear()
  })

  return banner
}
