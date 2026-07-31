export type ModuleTab = {
  id: string
  label: string
  badge?: string | number
  badgeLabel?: string
  route: string
  backendScope: string
  permission?: string
  emptyState: string
  primaryAction?: string
}

export type ModuleTabDefinition = {
  moduleId: string
  title: string
  tabs: ModuleTab[]
}

export const moduleTabRegistry: Readonly<Record<string, ModuleTabDefinition>> = {
  work: {
    moduleId: "work",
    title: "SideJobs",
    tabs: [
      {id: "discover", label: "Find SideJobs", route: "/work/find", backendScope: "work.discover", emptyState: "No SideJobs are available yet.", primaryAction: "Post a SideJob"},
      {id: "mine", label: "My SideJobs", route: "/work/quests", backendScope: "work.mine", emptyState: "You have not posted a SideJob yet.", primaryAction: "Post a SideJob"},
      {id: "applications", label: "My activity", route: "/work/applications", backendScope: "work.applications", emptyState: "You have no SideJob activity yet."}
    ]
  },
  business: {
    moduleId: "business",
    title: "Business",
    tabs: [
      {id: "overview", label: "My businesses", route: "/business", backendScope: "business.owner", emptyState: "Create your first business.", primaryAction: "Create a business"}
    ]
  },
  services: {
    moduleId: "services",
    title: "Services",
    tabs: [
      {id: "find", label: "Find service", route: "/business/find", backendScope: "business.discover", emptyState: "No businesses match this search.", primaryAction: "Search services"},
      {id: "favorites", label: "Favorites", route: "/business/favorites", backendScope: "business.favorites", emptyState: "You have no favorite businesses yet."}
    ]
  },
  share: {
    moduleId: "share",
    title: "Share",
    tabs: [
      {id: "things", label: "Things", route: "/share/things", backendScope: "things.discovery", emptyState: "Nothing is available to borrow yet.", primaryAction: "Offer a thing"},
      {id: "rides", label: "Rides", route: "/share/rides", backendScope: "rides.suggestions", emptyState: "No ride suggestions yet."},
      {id: "requests", label: "Requests", route: "/share/requests", backendScope: "share.requests", emptyState: "No requests need your attention."}
    ]
  },
  circles: {
    moduleId: "circles",
    title: "Circles",
    tabs: [
      {id: "people", label: "Find people", route: "/people/find", backendScope: "people.find", emptyState: "Search for someone you trust."},
      {id: "groups", label: "Circles", route: "/people/circles", backendScope: "people.circles", emptyState: "Create your first circle."},
      {id: "requests", label: "Requests", route: "/people/requests", backendScope: "people.requests", emptyState: "No connection requests need your attention."}
    ]
  },
  profile: {
    moduleId: "profile",
    title: "Profile",
    tabs: [
      {id: "profile", label: "Profile", route: "/profile", backendScope: "profile.self", emptyState: "Complete your profile."},
      {id: "settings", label: "Settings", route: "/profile/settings", backendScope: "profile.settings", emptyState: "No additional settings are available."}
    ]
  }
}

export function getModuleTabs(moduleId: string): ModuleTabDefinition | undefined {
  // SideJobs uses one canonical vocabulary: find, post, and personal activity.
  return moduleTabRegistry[moduleId]
}
