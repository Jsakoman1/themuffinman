import {computed, reactive} from "vue"

type ActionDialogState = {
  open: boolean
  mode: "confirm" | "notice"
  title: string
  message: string
  confirmLabel: string
  cancelLabel: string
  tone: "primary" | "danger" | "vision"
  resolve: ((value: boolean) => void) | null
}

const state = reactive<ActionDialogState>({
  open: false,
  mode: "confirm",
  title: "Confirm action",
  message: "",
  confirmLabel: "Continue",
  cancelLabel: "Cancel",
  tone: "primary",
  resolve: null
})

const close = (result: boolean) => {
  const resolver = state.resolve
  state.open = false
  state.resolve = null
  resolver?.(result)
}

export const useActionDialog = () => ({
  state: computed(() => state),
  close,
  confirm(options: Partial<Pick<ActionDialogState, "title" | "message" | "confirmLabel" | "cancelLabel" | "tone">> = {}) {
    if (state.open) close(false)
    Object.assign(state, {
      mode: "confirm",
      title: options.title ?? "Confirm action",
      message: options.message ?? "Are you sure you want to continue?",
      confirmLabel: options.confirmLabel ?? "Continue",
      cancelLabel: options.cancelLabel ?? "Cancel",
      tone: options.tone ?? "primary",
      open: true
    })
    return new Promise<boolean>((resolve) => { state.resolve = resolve })
  },
  notice(message: string, title = "System notice") {
    if (state.open) close(false)
    Object.assign(state, {mode: "notice", title, message, confirmLabel: "Close", cancelLabel: "", tone: "vision", open: true})
    return new Promise<void>((resolve) => { state.resolve = () => resolve() })
  }
})

export const confirmAction = (message: string, title = "Confirm action") => useActionDialog().confirm({title, message})
export const showActionNotice = (message: string, title = "System notice") => useActionDialog().notice(message, title)
