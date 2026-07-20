import {useRouter} from "vue-router"
import type {VisionWebAction} from "../../vision/api/visionConversationViewModels.ts"
import {executeVisionWebAction} from "../visionWebAction.ts"

export type VisionForWebContext = {
  workspaceContext: string | null
  workspaceSource: string | null
  workspaceReturnTo: string | null
}

export const useVisionForWeb = () => {
  const router = useRouter()
  const contextFor = (context: string, source: string, returnTo: string): VisionForWebContext => ({
    workspaceContext: context.trim() || null,
    workspaceSource: source.trim() || null,
    workspaceReturnTo: returnTo.startsWith("/") ? returnTo : null
  })
  const execute = async (action: VisionWebAction | null | undefined) => {
    if (!action?.canonicalPath) {
      return false
    }
    return executeVisionWebAction(router, action)
  }
  return {execute, contextFor}
}
