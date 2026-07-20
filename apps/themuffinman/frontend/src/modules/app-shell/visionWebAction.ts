import type {Router} from "vue-router"
import type {VisionWebAction} from "../vision/api/visionConversationViewModels.ts"
import {visionWebActionQuery} from "./visionHandoff.ts"
import {isVisionWebActionRouteAllowed, visionWebActionContractVersion} from "./shellRouteRegistry.ts"

const isSafeInternalPath = (path: string) => path.startsWith("/") && !path.startsWith("//") && !path.includes("\\")
const isSupportedContract = (contractVersion: string | null | undefined) => contractVersion === visionWebActionContractVersion

export const executeVisionWebAction = async (router: Router, action: VisionWebAction | null | undefined) => {
  if (!action || !isSupportedContract(action.contractVersion) || action.ambiguous
    || !isSafeInternalPath(action.canonicalPath) || !isVisionWebActionRouteAllowed(action)) {
    return false
  }

  const query = visionWebActionQuery(action)
  await router.push(Object.keys(query).length > 0 ? {path: action.canonicalPath, query} : action.canonicalPath)
  return true
}
