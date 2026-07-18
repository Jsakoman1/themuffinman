import {confirmAction} from "./useActionDialog.ts"

export type ObjectActionTone = "primary" | "neutral" | "danger"
export type ObjectActionDescriptor = {id: string; label: string; tone?: ObjectActionTone; disabled?: boolean; visible?: boolean; confirmation?: {title: string; message: string}; run: () => void | Promise<void>}

export const invokeObjectAction = async (action: ObjectActionDescriptor) => {
  if (action.visible === false || action.disabled || action.confirmation && !await confirmAction(action.confirmation.message, action.confirmation.title)) return false
  await action.run()
  return true
}

export const isEditableTarget = (target: EventTarget | null) => target instanceof HTMLElement && Boolean(target.closest("input,textarea,select,[contenteditable='true']"))

export const matchesObjectShortcut = (event: KeyboardEvent, key: string) =>
  !event.metaKey && !event.ctrlKey && !event.altKey && event.key.toLowerCase() === key.toLowerCase()
