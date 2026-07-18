import {confirmAction} from "./useActionDialog.ts"

export type ObjectActionTone = "primary" | "neutral" | "danger"
export type ObjectActionDescriptor = {id: string; label: string; tone?: ObjectActionTone; disabled?: boolean; visible?: boolean; serverAllowed?: boolean; confirmation?: {title: string; message: string}; run: () => void | Promise<void>}
export type InlineEditContract = {serverAllowed: boolean; disabled?: boolean; saving?: boolean}

export const invokeObjectAction = async (action: ObjectActionDescriptor) => {
  if (action.visible === false || action.serverAllowed === false || action.disabled || action.confirmation && !await confirmAction(action.confirmation.message, action.confirmation.title)) return false
  await action.run()
  return true
}

/** Server permission is authoritative; this helper only removes actions already denied by the supplied contract. */
export const isObjectActionAvailable = (action: ObjectActionDescriptor) => action.visible !== false && action.serverAllowed !== false

/** Inline editors may open only when the detail/read contract explicitly allows that field mutation. */
export const canBeginInlineEdit = (contract: InlineEditContract) => contract.serverAllowed && contract.disabled !== true && contract.saving !== true

export const splitObjectActions = (actions: ObjectActionDescriptor[]) => ({
  primary: actions.filter((action) => isObjectActionAvailable(action) && action.tone === "primary"),
  secondary: actions.filter((action) => isObjectActionAvailable(action) && (!action.tone || action.tone === "neutral")),
  destructive: actions.filter((action) => isObjectActionAvailable(action) && action.tone === "danger"),
})

export const isEditableTarget = (target: EventTarget | null) => target instanceof HTMLElement && Boolean(target.closest("input,textarea,select,[contenteditable='true']"))

export const matchesObjectShortcut = (event: KeyboardEvent, key: string) =>
  !event.metaKey && !event.ctrlKey && !event.altKey && event.key.toLowerCase() === key.toLowerCase()
