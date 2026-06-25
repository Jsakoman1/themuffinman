import {onMounted} from "vue"

export const useMountedAsync = (action: () => Promise<void> | void) => {
  onMounted(() => {
    void action()
  })
}
