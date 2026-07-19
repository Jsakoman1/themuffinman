import type {RouteLocationRaw} from "vue-router"
import type {AppSurfaceId} from "./shellDefinitions.ts"
import {getSurfaceVisionPrompt, resolveSurfaceDetailRoute, surfaceOwnershipMatrix} from "./shellRouteRegistry.ts"

export type VisionLaunchOptions = {
  prompt?: string
  context?: string
  source?: string
  returnTo?: string
}

export const workspaceVisionHandoffContractVersion = "workspace-v1" as const
export type WorkspaceVisionHandoff = {
  contractVersion: typeof workspaceVisionHandoffContractVersion
  contextLabel: string | null
  source: string | null
  returnTo: string | null
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

  if (prompt) {
    query.prompt = prompt
    query.autorun = "1"
  }
  if (context) {
    query.context = context
  }
  if (source) {
    query.source = source
  }
  if (returnTo) {
    query.returnTo = returnTo
  }

  return Object.keys(query).length > 0
    ? {path: "/vision", query}
    : {path: "/vision"}
}

export const buildSurfaceVisionPrompt = (surfaceId: AppSurfaceId) => {
  return getSurfaceVisionPrompt(surfaceId)
}

export const buildSurfaceVisionRoute = (surfaceId: AppSurfaceId, currentPath: string, contextLabel: string) => {
  return buildVisionRoute({
    prompt: buildSurfaceVisionPrompt(surfaceId),
    context: contextLabel,
    source: `shell.surface.${surfaceId}`,
    returnTo: currentPath
  })
}

export const normalizeWorkspaceVisionHandoff = (value: Partial<WorkspaceVisionHandoff>): WorkspaceVisionHandoff => ({
  contractVersion: workspaceVisionHandoffContractVersion,
  contextLabel: trimQueryValue(value.contextLabel) ?? null,
  source: trimQueryValue(value.source) ?? null,
  returnTo: safeWorkspaceReturnTo(value.returnTo) ?? null,
})

export const resolveVisionEntityRoute = (entityFamily: string, targetId: number): RouteLocationRaw | null => {
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
