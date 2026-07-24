import type {RouteLocationRaw} from "vue-router"
import type {AppSurfaceId} from "./shellDefinitions.ts"
import {getSurfaceVisionPrompt, resolveSurfaceDetailRoute, surfaceOwnershipMatrix} from "./shellRouteRegistry.ts"

export type VisionLaunchOptions = {
  prompt?: string
  context?: string
  source?: string
  returnTo?: string
  contextFamily?: "circle" | "conversation" | "work" | "business" | "personal"
}

export const workspaceVisionHandoffContractVersion = "workspace-v1" as const
export type WorkspaceVisionHandoff = {
  contractVersion: typeof workspaceVisionHandoffContractVersion
  contextLabel: string | null
  source: string | null
  returnTo: string | null
  contextFamily: VisionLaunchOptions["contextFamily"] | null
}

const trimQueryValue = (value?: string | null) => {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

const safeWorkspaceReturnTo = (value?: string | null) => {
  const returnTo = trimQueryValue(value)
  return returnTo && returnTo.startsWith("/") && !returnTo.startsWith("//") && !returnTo.includes("://") && !returnTo.includes("\\")
    ? returnTo
    : undefined
}

export const buildVisionRoute = (options: VisionLaunchOptions = {}): RouteLocationRaw => {
  const query: Record<string, string> = {}
  const prompt = trimQueryValue(options.prompt)
  const context = trimQueryValue(options.context)
  const source = trimQueryValue(options.source)
  const returnTo = safeWorkspaceReturnTo(options.returnTo)
  const contextFamily = options.contextFamily

  if (prompt) {
    query.visionPrompt = prompt
    query.visionAutorun = "1"
  }
  if (context) {
    query.visionContext = context
  }
  if (source) {
    query.visionSource = source
  }
  if (returnTo) {
    query.visionReturnTo = returnTo
  }
  if (contextFamily) query.visionContextFamily = contextFamily

  return Object.keys(query).length > 0
    ? {path: "/home", query}
    : {path: "/home"}
}

export const buildSurfaceVisionPrompt = (surfaceId: AppSurfaceId) => {
  return getSurfaceVisionPrompt(surfaceId)
}

export const buildSurfaceVisionRoute = (surfaceId: AppSurfaceId, currentPath: string, contextLabel: string) => {
  const contextFamily = surfaceId === "circles" ? "circle" : surfaceId === "chat" ? "conversation" : surfaceId.startsWith("work") ? "work" : surfaceId === "business" ? "business" : "personal"
  return buildVisionRoute({
    prompt: buildSurfaceVisionPrompt(surfaceId),
    context: contextLabel,
    source: `shell.surface.${surfaceId}`,
    returnTo: currentPath,
    contextFamily
  })
}

export const normalizeWorkspaceVisionHandoff = (value: Partial<WorkspaceVisionHandoff>): WorkspaceVisionHandoff => ({
  contractVersion: workspaceVisionHandoffContractVersion,
  contextLabel: trimQueryValue(value.contextLabel) ?? null,
  source: trimQueryValue(value.source) ?? null,
  returnTo: safeWorkspaceReturnTo(value.returnTo) ?? null,
  contextFamily: value.contextFamily ?? null,
})

export const resolveVisionEntityRoute = (entityFamily: string, targetId: number): RouteLocationRaw | null => {
  if (!Number.isInteger(targetId) || targetId <= 0) return null
  const normalized = entityFamily.trim().toLowerCase()

  if (normalized === "quest") {
    return resolveSurfaceDetailRoute("work-quests", targetId)
  }
  if (normalized === "application") {
    return resolveSurfaceDetailRoute("work-applications", targetId)
  }
  if (normalized === "user" || normalized === "profile" || normalized === "person") {
    return {path: `/people/${targetId}`}
  }
  if (normalized === "circle" || normalized === "circles") {
    return surfaceOwnershipMatrix.circles.canonicalEntryRoute
  }
  if (normalized === "chat" || normalized === "conversation") {
    return surfaceOwnershipMatrix.chat.canonicalEntryRoute
  }
  if (normalized === "business") {
    return surfaceOwnershipMatrix.business.canonicalEntryRoute
  }
  if (normalized === "thing" || normalized === "things") {
    return {path: `/things/${targetId}`}
  }

  return null
}

export const visionWebActionQuery = (action: {focus?: string | null; preview?: boolean; filters?: Record<string, string>}): Record<string, string> => {
  const query: Record<string, string> = {}
  Object.entries(action.filters ?? {}).forEach(([key, value]) => {
    if (key.trim() && value.trim()) query[key.trim()] = value.trim()
  })
  if (action.focus?.trim()) query.focus = action.focus.trim()
  if (action.preview) query.preview = "1"
  return query
}
