import type {Router} from "vue-router"
import type {ProfilePrimaryAction} from "../../../contracts/index.ts"
import {routeForNavigationTarget} from "./navigationTargets.ts"

type SocialActionHandlers = {
  close?: () => void
  editProfile?: () => void
  sendInvite?: () => void
  block?: () => void
  unblock?: () => void
}

export const executeSocialAction = async (
  router: Router,
  action: ProfilePrimaryAction | null | undefined,
  handlers: SocialActionHandlers = {}
) => {
  if (!action?.enabled) {
    return
  }

  if (action.navigation) {
    handlers.close?.()
    await router.push(routeForNavigationTarget(action.navigation))
    return
  }

  switch (action.type) {
    case "EDIT_PROFILE":
      handlers.editProfile?.()
      return
    case "SEND_INVITE":
      handlers.sendInvite?.()
      return
    case "BLOCK":
      handlers.block?.()
      return
    case "UNBLOCK":
      handlers.unblock?.()
      return
    default:
      return
  }
}
