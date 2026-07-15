import type {RouteLocationRaw} from "vue-router"
import {buildAppPrimaryNavItems, topLevelNavigationPromotionPolicy} from "./shellRouteRegistry.ts"

export type AppPrimaryNavId = "home" | "work" | "chat" | "calendar" | "business" | "circles" | "profile"

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
  | "profile"
  | "profile-settings"

export type AppSurfaceArchetype = "home" | "work" | "chat" | "calendar" | "business" | "circles" | "profile"

export type AppSurfaceAction = {
  label: string
  to: RouteLocationRaw
  tone?: "primary" | "secondary" | "vision"
}

export type AppSurfaceConfig = {
  id: AppSurfaceId
  archetype: AppSurfaceArchetype
  navId: AppPrimaryNavId
  eyebrow: string
  title: string
  actions: AppSurfaceAction[]
}

export type AppPrimaryNavItem = {
  id: AppPrimaryNavId
  label: string
  description: string
  to: RouteLocationRaw
}

export {topLevelNavigationPromotionPolicy}

const visionRoute = (prompt?: string): RouteLocationRaw => prompt
  ? {path: "/vision", query: {prompt, autorun: "1"}}
  : {path: "/vision"}

export const appPrimaryNavItems: AppPrimaryNavItem[] = buildAppPrimaryNavItems()

const appSurfaceConfigs: Record<AppSurfaceId, AppSurfaceConfig> = {
  home: {
    id: "home", archetype: "home", navId: "home", eyebrow: "Home", title: "Home",
    actions: [
      {label: "Work", to: {path: "/work"}, tone: "primary"},
      {label: "Ask Vision", to: visionRoute(), tone: "vision"}
    ]
  },
  work: {
    id: "work", archetype: "work", navId: "work", eyebrow: "Work", title: "Work",
    actions: [
      {label: "My quests", to: {path: "/work/quests"}},
      {label: "Ask Vision", to: visionRoute("find work for me"), tone: "vision"}
    ]
  },
  "work-quests": {
    id: "work-quests", archetype: "work", navId: "work", eyebrow: "Work / Mine", title: "My quests",
    actions: [{label: "New quest", to: {path: "/work/quests/new"}}, {label: "Work", to: {path: "/work"}}]
  },
  "work-applications": {
    id: "work-applications", archetype: "work", navId: "work", eyebrow: "Work / Applications", title: "Applications",
    actions: [{label: "Work", to: {path: "/work"}}]
  },
  chat: {
    id: "chat", archetype: "chat", navId: "chat", eyebrow: "Chat", title: "Chat",
    actions: [{label: "Ask Vision", to: visionRoute("open chat"), tone: "vision"}]
  },
  "chat-conversation": {
    id: "chat-conversation", archetype: "chat", navId: "chat", eyebrow: "Chat / Conversation", title: "Conversation",
    actions: [{label: "Chat", to: {path: "/chat"}}]
  },
  calendar: {
    id: "calendar", archetype: "calendar", navId: "calendar", eyebrow: "Calendar", title: "Calendar",
    actions: [{label: "Ask Vision", to: visionRoute("plan my time"), tone: "vision"}]
  },
  business: {
    id: "business", archetype: "business", navId: "business", eyebrow: "Business", title: "Business",
    actions: [
      {label: "Bookings", to: {path: "/business/bookings"}},
      {label: "Calendar", to: {path: "/business/calendar"}}
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
    actions: [{label: "Ask Vision", to: visionRoute("show circles"), tone: "vision"}]
  },
  profile: {
    id: "profile", archetype: "profile", navId: "profile", eyebrow: "Profile", title: "Profile",
    actions: [{label: "Settings", to: {path: "/profile/settings"}}]
  },
  "profile-settings": {
    id: "profile-settings", archetype: "profile", navId: "profile", eyebrow: "Profile / Settings", title: "Settings",
    actions: [{label: "Profile", to: {path: "/profile"}}]
  }
}

export const getAppSurfaceConfig = (surfaceId: AppSurfaceId) => appSurfaceConfigs[surfaceId]
