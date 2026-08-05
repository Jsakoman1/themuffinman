import type {RouteLocationRaw} from "vue-router"
import {buildAppPrimaryNavItems, buildAppSecondaryNavItems, canonicalRouteForSurface, topLevelNavigationPromotionPolicy} from "./shellRouteRegistry.ts"

export const desktopCommandModel = Object.freeze({shortcut: "Mod+K", source: "backend-prepared", oneOpenSurface: true})

export type AppPrimaryNavId = "home" | "work" | "chat" | "calendar" | "business" | "services" | "circles" | "things" | "rides" | "profile"

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
  | "business-service-setup"
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
  canonicalRoute?: RouteLocationRaw
}

export type AppPrimaryNavItem = {
  id: AppPrimaryNavId
  label: string
  icon: string
  description: string
  to: RouteLocationRaw
}

export const authenticatedShellContract = {
  landmarks: ["primary-navigation", "workspace-context", "workspace-main"],
  layout: "stable-left-rail-context-header-main-surface",
  vision: "contextual-entry-remains-separate-from-command-navigation",
  header: "brand-and-context-only",
  navigation: "visible-desktop-personal-and-account-rail-with-module-owned-tabs",
  mobileNavigation: ["Home", "Explore", "Calendar", "Chat", "Profile"]
} as const

// Closeout disposition: authenticated module pages own their tabs and actions;
// Vision is intentionally not duplicated in individual page toolbars.

export {topLevelNavigationPromotionPolicy}

export const appPrimaryNavItems: AppPrimaryNavItem[] = buildAppPrimaryNavItems()
export const appSecondaryNavItems: AppPrimaryNavItem[] = buildAppSecondaryNavItems()
// Activity and saved searches remain direct-route compatibility surfaces. They are
// intentionally absent from ordinary navigation until each has a complete user journey.

const appSurfaceConfigs: Record<AppSurfaceId, AppSurfaceConfig> = {
  home: {
    id: "home", archetype: "home", navId: "home", eyebrow: "Home", title: "Home",
    actions: []
  },
  work: {
    id: "work", archetype: "work", navId: "work", eyebrow: "SideJobs", title: "SideJobs",
    actions: [
      {label: "Post a SideJob", to: {path: "/work/offer"}, tone: "primary"},
      {label: "Find SideJobs", to: {path: "/work/find"}},
      {label: "My SideJobs", to: {path: "/work/quests"}},
    ]
  },
  "work-quests": {
    id: "work-quests", archetype: "work", navId: "work", eyebrow: "SideJobs / My posts", title: "My posted SideJobs",
    actions: [{label: "Post a SideJob", to: {path: "/work/offer"}}, {label: "Find a SideJob", to: {path: "/work/find"}}]
  },
  "work-applications": {
    id: "work-applications", archetype: "work", navId: "work", eyebrow: "SideJobs / My help offers", title: "My help offers",
    actions: [{label: "Find a SideJob", to: {path: "/work/find"}}]
  },
  chat: {
    id: "chat", archetype: "chat", navId: "chat", eyebrow: "Chat", title: "Chat",
    actions: [{label: "Open chat", to: {path: "/chat"}, tone: "primary"}]
  },
  "chat-conversation": {
    id: "chat-conversation", archetype: "chat", navId: "chat", eyebrow: "Chat / Conversation", title: "Conversation",
    actions: [{label: "Chat", to: {path: "/chat"}}]
  },
  calendar: {
    id: "calendar", archetype: "calendar", navId: "calendar", eyebrow: "Calendar", title: "Calendar",
    hubArchetype: "section-navigation",
    actions: []
  },
  business: {
    id: "business", archetype: "business", navId: "business", eyebrow: "Services you offer", title: "Offer services",
    hubArchetype: "section-navigation",
    actions: [
      {label: "Find a business", to: {path: "/business/find"}, tone: "primary"},
      {label: "My bookings", to: {path: "/business/my-bookings"}}
    ]
  },
  "business-profile": {
    id: "business-profile", archetype: "business", navId: "business", eyebrow: "Business / Profile", title: "Business profile",
    actions: [{label: "Business", to: {path: "/business"}}]
  },
  "business-service-setup": {
    id: "business-service-setup", archetype: "business", navId: "business", eyebrow: "Business / Service setup", title: "Service setup",
    actions: [{label: "Business", to: {path: "/business"}}, {label: "Offerings", to: {path: "/business/offerings"}}]
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
    ]
  },
  people: {
    id: "people", archetype: "circles", navId: "circles", eyebrow: "People", title: "Find people",
    actions: [{label: "Circles", to: {path: "/circles"}}]
  },
  "business-discovery": {
    id: "business-discovery", archetype: "business", navId: "services", eyebrow: "Book a service", title: "Find a service",
    actions: [{label: "Find service", to: {path: "/business/find"}}]
  },
  things: {
    id: "things", archetype: "things", navId: "things", eyebrow: "Things", title: "Things",
    actions: [{label: "List a thing", to: {path: "/things", query: {create: "1"}}, tone: "primary"}, {label: "My things", to: {path: "/things/mine"}}]
  },
  rides: {
    id: "rides", archetype: "things", navId: "rides", eyebrow: "Rides", title: "Rides",
    actions: [{label: "Offer a ride", to: {path: "/rides", query: {create: "1"}}, tone: "primary"}, {label: "My rides", to: {path: "/rides/mine"}}]
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

export const getAppSurfaceConfig = (surfaceId: AppSurfaceId): AppSurfaceConfig => ({
  ...appSurfaceConfigs[surfaceId],
  canonicalRoute: canonicalRouteForSurface(surfaceId)
})

export const getCanonicalSurfaceRoute = (surfaceId: AppSurfaceId): RouteLocationRaw =>
  getAppSurfaceConfig(surfaceId).canonicalRoute!
