import type {RouteLocationRaw} from "vue-router"
import {buildAppPrimaryNavItems, buildAppSecondaryNavItems, topLevelNavigationPromotionPolicy} from "./shellRouteRegistry.ts"

export type AppPrimaryNavId = "home" | "work" | "chat" | "calendar" | "business" | "circles" | "things" | "rides" | "profile"

export type AppSurfaceId =
  | "home"
  | "work"
  | "work-quests"
  | "work-applications"
  | "chat"
  | "chat-conversation"
  | "calendar"
  | "business"
  | "business-profile"
  | "business-bookings"
  | "business-calendar"
  | "circles"
  | "people"
  | "business-discovery"
  | "things"
  | "rides"
  | "profile"
  | "profile-settings"
  | "notifications"
  | "activity"

export type AppSurfaceArchetype = "home" | "work" | "chat" | "calendar" | "business" | "circles" | "profile" | "things"

export type AppSurfaceAction = {
  label: string
  to: RouteLocationRaw
  tone?: "primary" | "secondary" | "vision"
  description?: string
}

export type AppSurfaceConfig = {
  id: AppSurfaceId
  archetype: AppSurfaceArchetype
  navId: AppPrimaryNavId
  eyebrow: string
  title: string
  actions: AppSurfaceAction[]
  hubArchetype?: "data-workspace" | "section-navigation"
}

export type AppPrimaryNavItem = {
  id: AppPrimaryNavId
  label: string
  icon: string
  description: string
  to: RouteLocationRaw
}

export type AppPersonalShortcut = {
  id: "activity" | "saved-searches"
  label: string
  icon: string
  description: string
  to: RouteLocationRaw
}

export const authenticatedShellContract = {
  landmarks: ["primary-navigation", "workspace-context", "workspace-main"],
  layout: "stable-left-rail-context-header-main-surface",
  vision: "contextual-entry-remains-separate-from-command-navigation"
} as const

export {topLevelNavigationPromotionPolicy}

const visionRoute = (prompt?: string): RouteLocationRaw => prompt
  ? {path: "/vision", query: {prompt, autorun: "1"}}
  : {path: "/vision"}

export const appPrimaryNavItems: AppPrimaryNavItem[] = buildAppPrimaryNavItems()
export const appSecondaryNavItems: AppPrimaryNavItem[] = buildAppSecondaryNavItems()
export const appPersonalShortcuts: AppPersonalShortcut[] = [
  {id: "activity", label: "Activity", icon: "◷", description: "Your viewer-scoped recent activity.", to: {path: "/activity"}},
  {id: "saved-searches", label: "Saved searches", icon: "⌕", description: "Your server-owned saved searches.", to: {path: "/search/saved"}}
]

const appSurfaceConfigs: Record<AppSurfaceId, AppSurfaceConfig> = {
  home: {
    id: "home", archetype: "home", navId: "home", eyebrow: "Home", title: "Home",
    actions: []
  },
  work: {
    id: "work", archetype: "work", navId: "work", eyebrow: "Work", title: "Work",
    actions: [
      {label: "Offer work", to: {path: "/work/offer"}, tone: "primary"},
      {label: "Find work", to: {path: "/work/find"}},
      {label: "My work", to: {path: "/work/quests"}},
      {label: "Ask Vision", to: visionRoute("find work for me"), tone: "vision"}
    ]
  },
  "work-quests": {
    id: "work-quests", archetype: "work", navId: "work", eyebrow: "Work / Mine", title: "My work",
    actions: [{label: "Offer work", to: {path: "/work/offer"}}, {label: "Work", to: {path: "/work"}}]
  },
  "work-applications": {
    id: "work-applications", archetype: "work", navId: "work", eyebrow: "Work / My applications", title: "My applications",
    actions: [{label: "Work", to: {path: "/work"}}]
  },
  chat: {
    id: "chat", archetype: "chat", navId: "chat", eyebrow: "Chat", title: "Chat",
    actions: [{label: "Open chat", to: {path: "/chat"}, tone: "primary"}, {label: "Ask Vision", to: visionRoute("open chat"), tone: "vision"}]
  },
  "chat-conversation": {
    id: "chat-conversation", archetype: "chat", navId: "chat", eyebrow: "Chat / Conversation", title: "Conversation",
    actions: [{label: "Chat", to: {path: "/chat"}}]
  },
  calendar: {
    id: "calendar", archetype: "calendar", navId: "calendar", eyebrow: "Calendar", title: "Calendar",
    hubArchetype: "section-navigation",
    actions: [{label: "Ask Vision", to: visionRoute("plan my time"), tone: "vision"}]
  },
  business: {
    id: "business", archetype: "business", navId: "business", eyebrow: "Business", title: "Business",
    hubArchetype: "section-navigation",
    actions: [
      {label: "Find a business", to: {path: "/business/find"}, tone: "primary"},
      {label: "My bookings", to: {path: "/business/my-bookings"}},
      {label: "Business profile", to: {path: "/business/profile"}},
      {label: "Bookings", to: {path: "/business/bookings"}},
      {label: "Calendar", to: {path: "/business/calendar"}},
      {label: "Ask Vision", to: visionRoute("find a business to book"), tone: "vision"}
    ]
  },
  "business-profile": {
    id: "business-profile", archetype: "business", navId: "business", eyebrow: "Business / Profile", title: "Business profile",
    actions: [{label: "Business", to: {path: "/business"}}]
  },
  "business-bookings": {
    id: "business-bookings", archetype: "business", navId: "business", eyebrow: "Business / Bookings", title: "Bookings",
    actions: [{label: "Business", to: {path: "/business"}}]
  },
  "business-calendar": {
    id: "business-calendar", archetype: "business", navId: "business", eyebrow: "Business / Calendar", title: "Business calendar",
    actions: [{label: "Business", to: {path: "/business"}}]
  },
  circles: {
    id: "circles", archetype: "circles", navId: "circles", eyebrow: "Circles", title: "Circles",
    actions: [
      {label: "Create circle", to: {path: "/circles", query: {create: "1"}}, tone: "primary"},
      {label: "Find people", to: {path: "/people"}},
      {label: "Ask Vision", to: visionRoute("show circles"), tone: "vision"}
    ]
  },
  people: {
    id: "people", archetype: "circles", navId: "circles", eyebrow: "People", title: "Find people",
    actions: [{label: "Circles", to: {path: "/circles"}}, {label: "Ask Vision", to: visionRoute("find people for me"), tone: "vision"}]
  },
  "business-discovery": {
    id: "business-discovery", archetype: "business", navId: "business", eyebrow: "Business / Discover", title: "Find a business",
    actions: [{label: "Business", to: {path: "/business"}}, {label: "Ask Vision", to: visionRoute("find a business for me"), tone: "vision"}]
  },
  things: {
    id: "things", archetype: "things", navId: "things", eyebrow: "Things", title: "Things",
    actions: [{label: "List a thing", to: {path: "/things", query: {create: "1"}}, tone: "primary"}, {label: "My things", to: {path: "/things/mine"}}, {label: "Ask Vision", to: visionRoute("find things to borrow"), tone: "vision"}]
  },
  rides: {
    id: "rides", archetype: "things", navId: "rides", eyebrow: "Rides", title: "Rides",
    actions: [{label: "Offer a ride", to: {path: "/rides", query: {create: "1"}}, tone: "primary"}, {label: "My rides", to: {path: "/rides/mine"}}, {label: "Ask Vision", to: visionRoute("find a ride or offer a ride"), tone: "vision"}]
  },
  profile: {
    id: "profile", archetype: "profile", navId: "profile", eyebrow: "Profile", title: "Profile",
    hubArchetype: "section-navigation",
    actions: [{label: "Settings", to: {path: "/profile/settings"}}]
  },
  "profile-settings": {
    id: "profile-settings", archetype: "profile", navId: "profile", eyebrow: "Profile / Settings", title: "Settings",
    actions: [{label: "Profile", to: {path: "/profile"}}]
  },
  notifications: {
    id: "notifications", archetype: "profile", navId: "profile", eyebrow: "Notifications", title: "Notifications",
    actions: [{label: "Profile", to: {path: "/profile"}}]
  },
  activity: {
    id: "activity", archetype: "profile", navId: "profile", eyebrow: "Activity", title: "Activity",
    actions: [{label: "Profile", to: {path: "/profile"}}]
  }
}

export const getAppSurfaceConfig = (surfaceId: AppSurfaceId) => appSurfaceConfigs[surfaceId]
