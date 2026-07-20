import type {Router} from "vue-router"
import type {VisionWebAction} from "../vision/api/visionConversationViewModels.ts"
import {visionWebActionQuery} from "./visionHandoff.ts"

const allowedActions = new Set(["NAVIGATE_TO_SURFACE", "OPEN_ENTITY_DETAIL", "OPEN_ENTITY_PREVIEW"])

const isSafeInternalPath = (path: string) => path.startsWith("/") && !path.startsWith("//") && !path.includes("\\")

export const executeVisionWebAction = async (router: Router, action: VisionWebAction | null | undefined) => {
  if (!action || action.ambiguous || !allowedActions.has(action.action) || !isSafeInternalPath(action.canonicalPath)
    || (action.action === "OPEN_ENTITY_DETAIL" && action.targetId == null)) {
    return false
  }

  const query = visionWebActionQuery(action)
  await router.push(Object.keys(query).length > 0 ? {path: action.canonicalPath, query} : action.canonicalPath)
  return true
}
