import type {RouteLocationRaw} from "vue-router"
import type {AppPrimaryNavId, AppPrimaryNavItem, AppSurfaceId} from "./shellDefinitions.ts"

export type ShellNavigationPromotionPolicy = {
  requiredBackendReadModel: string[]
  requiredRepeatJourneySignals: string[]
  requiredCognitiveLoadSignals: string[]
  requiredMobileFitSignals: string[]
  blockedByDefault: string[]
}

export type ShellSurfaceOwnership = {
  id: AppSurfaceId
  primaryNavId: AppPrimaryNavId
  primaryNavLabel: string
  primaryNavDescription: string
  canonicalEntryRoute: RouteLocationRaw
  canonicalDetailRoute?: RouteLocationRaw | ((targetId: number) => RouteLocationRaw)
  visionPrompt: string
  moduleSpaceRule: string
  visionRule: string
  topLevelNavEligible: boolean
}

export type ShellNavigationGroup = {
  id: "primary" | "secondary"
  label: string
  items: AppPrimaryNavItem[]
}

export const topLevelNavigationPromotionPolicy: ShellNavigationPromotionPolicy = {
  requiredBackendReadModel: [
    "stable backend read model",
    "explicit summary/read surface",
    "deterministic list/detail ownership"
  ],
  requiredRepeatJourneySignals: [
    "regular user journey",
    "clear habitual entry point",
    "repeated cross-session value"
  ],
  requiredCognitiveLoadSignals: [
    "reduces tab noise",
    "reduces route ambiguity",
    "reduces duplicate detail stacks"
  ],
  requiredMobileFitSignals: [
    "fits compact mobile nav",
    "stays legible in a short nav rail",
    "does not add a second grammar"
  ],
  blockedByDefault: [
    "one-off utility surface",
    "thin experimental surface",
    "surface without stable backend read model"
  ]
}

export const topLevelNavigationSurfaceIds: AppSurfaceId[] = [
  "home",
  "work",
  "chat",
  "calendar",
  "business",
  "circles",
  "things",
  "rides",
  "profile"
]

export const primaryNavigationSurfaceIds: AppSurfaceId[] = ["home", "work", "chat", "calendar"]
export const secondaryNavigationSurfaceIds: AppSurfaceId[] = ["business", "circles", "things", "rides"]

export const surfaceOwnershipMatrix: Record<AppSurfaceId, ShellSurfaceOwnership> = {
  home: {
    id: "home",
    primaryNavId: "home",
    primaryNavLabel: "Home",
    primaryNavDescription: "Orientation and next actions.",
    canonicalEntryRoute: {path: "/home"},
    visionPrompt: "summarize what needs attention today",
    moduleSpaceRule: "Stay in Home for orientation and entry guidance.",
    visionRule: "Escalate into Vision only when the task becomes guided, semantic, or cross-module.",
    topLevelNavEligible: true
  },
  work: {
    id: "work",
    primaryNavId: "work",
    primaryNavLabel: "Work",
    primaryNavDescription: "Browse quests and applications.",
    canonicalEntryRoute: {path: "/work"},
    visionPrompt: "help me find the best side jobs and work opportunities right now",
    moduleSpaceRule: "Stay in Work for browse and scan. `/work/find?scope=open-visible` owns visible open-work results; `/work/quests?scope=owned-active` owns the viewer's active quests, and Home must not mix those scopes.",
    visionRule: "Escalate into Vision when the task becomes create, review, update, or semantic search.",
    topLevelNavEligible: true
  },
  "work-quests": {
    id: "work-quests",
    primaryNavId: "work",
    primaryNavLabel: "Work / My Quests",
    primaryNavDescription: "Owned quests and deterministic browse.",
    canonicalEntryRoute: {path: "/work/quests"},
    canonicalDetailRoute: (targetId) => ({path: `/work/quests/${targetId}`}),
    visionPrompt: "review my active quests, help me create a new quest, and suggest the next action",
    moduleSpaceRule: "Stay in Work for the list and scan view.",
    visionRule: "Use Vision for canonical quest detail, create, review, and correction flows.",
    topLevelNavEligible: false
  },
  "work-applications": {
    id: "work-applications",
    primaryNavId: "work",
    primaryNavLabel: "Work / Applications",
    primaryNavDescription: "Application browsing with Vision detail.",
    canonicalEntryRoute: {path: "/work/applications"},
    canonicalDetailRoute: (targetId) => ({path: `/work/applications/${targetId}`}),
    visionPrompt: "review my applications and tell me which need attention",
    moduleSpaceRule: "Stay in Work for the applications list.",
    visionRule: "Use Vision for semantic discovery and guided review; known application detail stays in Work.",
    topLevelNavEligible: false
  },
  chat: {
    id: "chat",
    primaryNavId: "chat",
    primaryNavLabel: "Chat",
    primaryNavDescription: "Conversations and coordination.",
    canonicalEntryRoute: {path: "/chat"},
    visionPrompt: "help me navigate my conversations and outreach priorities",
    moduleSpaceRule: "Stay in Chat for browsing and thread continuation.",
    visionRule: "Use Vision only for semantic target resolution or guided outreach from intent.",
    topLevelNavEligible: true
  },
  "chat-conversation": {
    id: "chat-conversation",
    primaryNavId: "chat",
    primaryNavLabel: "Chat / Conversation",
    primaryNavDescription: "Thread detail remains chat-owned.",
    canonicalEntryRoute: {path: "/chat"},
    canonicalDetailRoute: (targetId) => ({path: `/chat/${targetId}`}),
    visionPrompt: "help me navigate my conversations and outreach priorities",
    moduleSpaceRule: "Stay in Chat for known thread browsing.",
    visionRule: "Use Vision when the thread needs semantic discovery or outreach planning.",
    topLevelNavEligible: false
  },
  calendar: {
    id: "calendar",
    primaryNavId: "calendar",
    primaryNavLabel: "Calendar",
    primaryNavDescription: "Time across work and business.",
    canonicalEntryRoute: {path: "/calendar"},
    visionPrompt: "plan my schedule across work and business",
    moduleSpaceRule: "Stay in Calendar for time coordination and overview.",
    visionRule: "Use Vision only when planning becomes guided or cross-module.",
    topLevelNavEligible: true
  },
  business: {
    id: "business",
    primaryNavId: "business",
    primaryNavLabel: "Business",
    primaryNavDescription: "Owner profile, bookings, calendar.",
    canonicalEntryRoute: {path: "/business"},
    visionPrompt: "review my business bookings, appointments, and availability",
    moduleSpaceRule: "Stay in Business for owner operations and stable read surfaces.",
    visionRule: "Use Vision when the task becomes guided, semantic, or needs planning help.",
    topLevelNavEligible: true
  },
  "business-profile": {
    id: "business-profile",
    primaryNavId: "business",
    primaryNavLabel: "Business / Profile",
    primaryNavDescription: "Business identity stays business-owned.",
    canonicalEntryRoute: {path: "/business/profile"},
    visionPrompt: "help me improve my business profile",
    moduleSpaceRule: "Stay in Business for owner profile browsing.",
    visionRule: "Use Vision only for guided improvements or semantic editing.",
    topLevelNavEligible: false
  },
  "business-bookings": {
    id: "business-bookings",
    primaryNavId: "business",
    primaryNavLabel: "Business / Bookings",
    primaryNavDescription: "Operational bookings and capacity.",
    canonicalEntryRoute: {path: "/business/bookings"},
    visionPrompt: "review my bookings, booking requests, and booking capacity, then highlight what needs action",
    moduleSpaceRule: "Stay in Business for operational bookings.",
    visionRule: "Use Vision when bookings need guided planning or explanation.",
    topLevelNavEligible: false
  },
  "business-calendar": {
    id: "business-calendar",
    primaryNavId: "business",
    primaryNavLabel: "Business / Calendar",
    primaryNavDescription: "Owner schedule and availability.",
    canonicalEntryRoute: {path: "/business/calendar"},
    visionPrompt: "help me plan my business calendar and appointment availability",
    moduleSpaceRule: "Stay in Business for owner schedule browsing.",
    visionRule: "Use Vision when the task becomes guided planning or semantic scheduling help.",
    topLevelNavEligible: false
  },
  circles: {
    id: "circles",
    primaryNavId: "circles",
    primaryNavLabel: "Circles",
    primaryNavDescription: "Trust, requests, and visibility.",
    canonicalEntryRoute: {path: "/circles"},
    visionPrompt: "help me manage my circles, requests, and outreach",
    moduleSpaceRule: "Stay in Circles for browse and request overview.",
    visionRule: "Use Vision for create, request, rename, delete, or person-resolution heavy work.",
    topLevelNavEligible: true
  },
  people: {
    id: "people",
    primaryNavId: "circles",
    primaryNavLabel: "People",
    primaryNavDescription: "Find people and view trust-aware profiles.",
    canonicalEntryRoute: {path: "/people"},
    visionPrompt: "find people for me",
    moduleSpaceRule: "Stay in People for search and profile browsing.",
    visionRule: "Use Vision for semantic person resolution or guided outreach.",
    topLevelNavEligible: false
  },
  "business-discovery": {
    id: "business-discovery",
    primaryNavId: "business",
    primaryNavLabel: "Business / Discover",
    primaryNavDescription: "Discover public businesses and offerings.",
    canonicalEntryRoute: {path: "/business/find"},
    visionPrompt: "find a business for me",
    moduleSpaceRule: "Stay in Business discovery for public business browsing.",
    visionRule: "Use Vision for semantic business matching or planning.",
    topLevelNavEligible: false
  },
  things: {
    id: "things",
    primaryNavId: "things",
    primaryNavLabel: "Things",
    primaryNavDescription: "Lending listings and borrow requests.",
    canonicalEntryRoute: {path: "/things"},
    canonicalDetailRoute: (targetId) => ({path: `/things/${targetId}`}),
    visionPrompt: "find things to borrow or lend",
    moduleSpaceRule: "Stay in Things for listing and borrowing workflows.",
    visionRule: "Use Vision for semantic matching or guided lending decisions.",
    topLevelNavEligible: false
  },
  rides: {
    id: "rides",
    primaryNavId: "rides",
    primaryNavLabel: "Rides",
    primaryNavDescription: "Voluntary circle-scoped ride coordination.",
    canonicalEntryRoute: {path: "/rides"},
    canonicalDetailRoute: (targetId) => ({path: `/rides/${targetId}`}),
    visionPrompt: "find a ride or offer a ride",
    moduleSpaceRule: "Stay in Rides for deterministic discovery and lifecycle actions.",
    visionRule: "Use Vision for semantic route matching and guided confirmations.",
    topLevelNavEligible: false
  },
  profile: {
    id: "profile",
    primaryNavId: "profile",
    primaryNavLabel: "Profile",
    primaryNavDescription: "Identity, settings, and preferences.",
    canonicalEntryRoute: {path: "/profile"},
    visionPrompt: "review my profile and suggest improvements",
    moduleSpaceRule: "Stay in Profile for self-review and settings entry.",
    visionRule: "Use Vision for guided self-mutation or semantic help.",
    topLevelNavEligible: true
  },
  "profile-settings": {
    id: "profile-settings",
    primaryNavId: "profile",
    primaryNavLabel: "Profile / Settings",
    primaryNavDescription: "Nested settings remain quiet.",
    canonicalEntryRoute: {path: "/profile/settings"},
    visionPrompt: "help me review my settings",
    moduleSpaceRule: "Stay in Profile for settings browsing.",
    visionRule: "Use Vision only when the settings task needs guided help.",
    topLevelNavEligible: false
  }
}

export const buildAppNavigationItems = (surfaceIds: AppSurfaceId[]): AppPrimaryNavItem[] => surfaceIds.map((surfaceId) => {
  const ownership = surfaceOwnershipMatrix[surfaceId]
  const icons: Record<AppPrimaryNavId, string> = {
    home: "⌂", work: "▤", chat: "◌", calendar: "□", business: "◇",
    circles: "◎", things: "▣", rides: "↗", profile: "◒"
  }

  return {
    id: ownership.primaryNavId,
    label: ownership.primaryNavLabel,
    icon: icons[ownership.primaryNavId],
    description: ownership.primaryNavDescription,
    to: ownership.canonicalEntryRoute
  }
})

export const buildAppPrimaryNavItems = (): AppPrimaryNavItem[] => buildAppNavigationItems(primaryNavigationSurfaceIds)
export const buildAppSecondaryNavItems = (): AppPrimaryNavItem[] => buildAppNavigationItems(secondaryNavigationSurfaceIds)

export const buildAppNavigationGroups = (): ShellNavigationGroup[] => [
  {id: "primary", label: "Core", items: buildAppPrimaryNavItems()},
  {id: "secondary", label: "Modules", items: buildAppSecondaryNavItems()}
]

export const getSurfaceOwnership = (surfaceId: AppSurfaceId) => surfaceOwnershipMatrix[surfaceId]

export const getSurfaceVisionPrompt = (surfaceId: AppSurfaceId) => surfaceOwnershipMatrix[surfaceId].visionPrompt

export type VisionWebRouteContract = {
  routeKey: string
  action: "NAVIGATE_TO_SURFACE" | "OPEN_ENTITY_DETAIL" | "OPEN_ENTITY_PREVIEW" | "OPEN_CONVERSATION"
  pathPattern: RegExp
  requiresTarget: boolean
}

export const visionWebActionContractVersion = "vision-web-action-v2"

// Backend publishes routeKey/action/canonicalPath. The Web client only accepts
// combinations declared here; it never turns a natural-language target into a URL.
export const visionWebRouteContracts: VisionWebRouteContract[] = [
  {routeKey: "work.find", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/work\/find$/, requiresTarget: false},
  {routeKey: "work.my_quests", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/work\/quests$/, requiresTarget: false},
  {routeKey: "work.applications", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/work\/applications$/, requiresTarget: false},
  {routeKey: "work.application_detail", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/work\/applications\/\d+$/, requiresTarget: true},
  {routeKey: "work.quest_detail", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/work\/quests\/\d+$/, requiresTarget: true},
  {routeKey: "circles.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/circles$/, requiresTarget: false},
  {routeKey: "circles.list", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/circles$/, requiresTarget: false},
  {routeKey: "people.profile", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/people\/\d+$/, requiresTarget: true},
  {routeKey: "profile.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/profile$/, requiresTarget: false},
  {routeKey: "profile.settings", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/profile\/settings$/, requiresTarget: false},
  {routeKey: "things.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/things$/, requiresTarget: false},
  {routeKey: "things.list", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/things$/, requiresTarget: false},
  {routeKey: "things.mine", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/things\/mine$/, requiresTarget: false},
  {routeKey: "things.detail", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/things\/\d+$/, requiresTarget: true},
  {routeKey: "things.borrow", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/things\/requests$/, requiresTarget: false},
  {routeKey: "rides.list", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/rides$/, requiresTarget: false},
  {routeKey: "rides.mine", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/rides\/mine$/, requiresTarget: false},
  {routeKey: "rides.detail", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/rides\/\d+$/, requiresTarget: true},
  {routeKey: "business.bookings", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/bookings$/, requiresTarget: false},
  {routeKey: "business.my_bookings", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/my-bookings$/, requiresTarget: false},
  {routeKey: "business.discovery", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/find$/, requiresTarget: false},
  {routeKey: "business.public_profile", action: "OPEN_ENTITY_DETAIL", pathPattern: /^\/business\/public\/[^/]+$/, requiresTarget: false},
  {routeKey: "business.owner_profile", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/profile$/, requiresTarget: false},
  {routeKey: "business.my_bookings", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/my-bookings$/, requiresTarget: false},
  {routeKey: "business.calendar", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/business\/calendar$/, requiresTarget: false},
  {routeKey: "chat.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/chat$/, requiresTarget: false},
  {routeKey: "chat.workspace", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/chat$/, requiresTarget: false},
  {routeKey: "chat.conversation", action: "OPEN_CONVERSATION", pathPattern: /^\/chat\/\d+$/, requiresTarget: true},
  {routeKey: "notifications.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/notifications$/, requiresTarget: false},
  {routeKey: "attention.notifications", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/notifications$/, requiresTarget: false},
  {routeKey: "activity.index", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/activity$/, requiresTarget: false},
  {routeKey: "attention.activity", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/activity$/, requiresTarget: false},
  {routeKey: "search.saved", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/search\/saved$/, requiresTarget: false},
  {routeKey: "profile.notifications", action: "NAVIGATE_TO_SURFACE", pathPattern: /^\/profile\/settings\/notifications$/, requiresTarget: false}
]

export const isVisionWebActionRouteAllowed = (action: {action: string; routeKey: string; canonicalPath: string; targetId: number | null}) => {
  const contract = visionWebRouteContracts.find((candidate) => candidate.routeKey === action.routeKey && candidate.action === action.action)
  const pathTargetId = action.canonicalPath.match(/\/(\d+)$/)?.[1] ?? null
  return contract !== undefined
    && contract.pathPattern.test(action.canonicalPath)
    && (!contract.requiresTarget || action.targetId != null)
    && (!contract.requiresTarget || pathTargetId === String(action.targetId))
}

export const resolveSurfaceDetailRoute = (surfaceId: AppSurfaceId, targetId: number): RouteLocationRaw | null => {
  const ownership = surfaceOwnershipMatrix[surfaceId]
  if (!ownership.canonicalDetailRoute) {
    return null
  }

  return typeof ownership.canonicalDetailRoute === "function"
    ? ownership.canonicalDetailRoute(targetId)
    : ownership.canonicalDetailRoute
}

export type VisionBridgeRouteDefinition = {
  path: string
  prompt: string | ((to: {params: Record<string, string | number>}) => string)
}

export const visionBridgeRouteDefinitions: VisionBridgeRouteDefinition[] = [
  {
    path: "/vision/users/:id",
    prompt: (to) => `show user #${to.params.id}`
  },
  {
    path: "/vision/quests/:id",
    prompt: (to) => `show quest #${to.params.id}`
  },
  {
    path: "/vision/applications/:id",
    prompt: (to) => `show application #${to.params.id}`
  }
]
