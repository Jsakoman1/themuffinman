export type ModuleTab = {
  id: string
  label: string
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
    title: "Work",
    tabs: [
      {id: "discover", label: "Discover", route: "/work", backendScope: "work.discover", emptyState: "No work is available yet.", primaryAction: "Create a work offer"},
      {id: "mine", label: "My work", route: "/work/quests", backendScope: "work.mine", emptyState: "You do not own any work yet.", primaryAction: "Create a work offer"},
      {id: "applications", label: "Applications", route: "/work/applications", backendScope: "work.applications", emptyState: "You have no applications yet."}
    ]
  },
  business: {
    moduleId: "business",
    title: "Business",
    tabs: [
      {id: "discover", label: "Book a business", route: "/business", backendScope: "business.discover", emptyState: "No businesses match this search.", primaryAction: "Find a business"},
      {id: "manage", label: "Manage my business", route: "/business/profile", backendScope: "business.owner", permission: "business.manage", emptyState: "Create your first business.", primaryAction: "Create a business"}
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
      {id: "people", label: "People", route: "/circles?tab=people", backendScope: "circles.people", emptyState: "Search for someone you trust."},
      {id: "groups", label: "Groups", route: "/circles?tab=groups", backendScope: "circles.groups", emptyState: "Create your first circle."},
      {id: "requests", label: "Requests", route: "/circles?tab=requests", backendScope: "circles.requests", emptyState: "No connection requests need your attention."}
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
  return moduleTabRegistry[moduleId]
}
