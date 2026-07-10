import type {RouteLocationRaw} from "vue-router"
import type {AppSurfaceId} from "./shellDefinitions.ts"
import {getSurfaceVisionPrompt, resolveSurfaceDetailRoute, surfaceOwnershipMatrix} from "./shellRouteRegistry.ts"

type VisionLaunchOptions = {
  prompt?: string
  context?: string
  source?: string
  returnTo?: string
}

const trimQueryValue = (value?: string) => {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

export const buildVisionRoute = (options: VisionLaunchOptions = {}): RouteLocationRaw => {
  const query: Record<string, string> = {}
  const prompt = trimQueryValue(options.prompt)
  const context = trimQueryValue(options.context)
  const source = trimQueryValue(options.source)
  const returnTo = trimQueryValue(options.returnTo)

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

export const resolveVisionEntityRoute = (entityFamily: string, targetId: number): RouteLocationRaw | null => {
  const normalized = entityFamily.trim().toLowerCase()

  if (normalized === "quest") {
    return resolveSurfaceDetailRoute("work-quests", targetId)
  }
  if (normalized === "application") {
    return resolveSurfaceDetailRoute("work-applications", targetId)
  }
  if (normalized === "user" || normalized === "profile" || normalized === "person") {
    return {path: `/vision/users/${targetId}`}
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

  return null
}
