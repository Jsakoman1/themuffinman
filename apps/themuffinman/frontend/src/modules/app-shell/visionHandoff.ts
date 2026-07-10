import type {RouteLocationRaw} from "vue-router"

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

export const buildSurfaceVisionPrompt = (surfaceId: string) => {
  switch (surfaceId) {
    case "home":
      return "summarize what needs attention today"
    case "work":
      return "help me find the best side jobs and work opportunities right now"
    case "work-quests":
      return "review my active quests, help me create a new quest, and suggest the next action"
    case "work-applications":
      return "review my applications and tell me which need attention"
    case "chat":
    case "chat-conversation":
      return "help me navigate my conversations and outreach priorities"
    case "calendar":
      return "plan my schedule across work and business"
    case "business":
      return "review my business bookings, appointments, and availability"
    case "business-profile":
      return "help me improve my business profile"
    case "business-bookings":
      return "review my bookings, booking requests, and booking capacity, then highlight what needs action"
    case "business-calendar":
      return "help me plan my business calendar and appointment availability"
    case "circles":
      return "help me manage my circles, requests, and outreach"
    case "profile":
      return "review my profile and suggest improvements"
    case "profile-settings":
      return "help me review my settings"
    default:
      return "help me continue from here"
  }
}

export const buildSurfaceVisionRoute = (surfaceId: string, currentPath: string, contextLabel: string) => {
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
    return {path: `/vision/quests/${targetId}`}
  }
  if (normalized === "application") {
    return {path: `/vision/applications/${targetId}`}
  }
  if (normalized === "user" || normalized === "profile" || normalized === "person") {
    return {path: `/vision/users/${targetId}`}
  }
  if (normalized === "circle" || normalized === "circles") {
    return {path: "/circles"}
  }
  if (normalized === "chat" || normalized === "conversation") {
    return {path: "/chat"}
  }
  if (normalized === "business") {
    return {path: "/business"}
  }

  return null
}
