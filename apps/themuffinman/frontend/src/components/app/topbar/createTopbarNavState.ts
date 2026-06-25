import {computed} from "vue"
import type {RouteLocationNormalizedLoaded} from "vue-router"
import {currentUser, isAdmin} from "../../../auth.ts"
import {productModules} from "../../../modules/moduleRegistry.ts"

export const createTopbarNavState = (route: RouteLocationNormalizedLoaded) => {
  const homeTarget = computed(() => {
    if (!currentUser.value) {
      return "/login"
    }

    return isAdmin() ? "/admin/work" : "/work"
  })

  const isAuthenticated = computed(() => currentUser.value !== null)

  const isModuleActive = (path: string) => {
    if (path === "/work") {
      return route.path.startsWith("/work")
        || route.path.startsWith("/quests")
        || route.path.startsWith("/applications")
        || route.path.startsWith("/users")
        || route.path.startsWith("/admin")
    }

    return route.path.startsWith(path)
  }

  const topbarNavItems = computed(() => {
    const remainingModules = productModules.filter((module) => module.key !== "work")

    return [
      {
        key: "calendar",
        title: "Calendar",
        shortTitle: "Calendar",
        path: "/work?tab=calendar",
        description: "Scheduled work and your planner.",
        state: "live" as const
      },
      {
        key: "create-job",
        title: "Create job",
        shortTitle: "Create job",
        path: "/work?tab=create-job",
        description: "Post and manage your jobs.",
        state: "live" as const
      },
      {
        key: "find-work",
        title: "Find job",
        shortTitle: "Find job",
        path: "/work?tab=find-work",
        description: "Browse open jobs.",
        state: "live" as const
      },
      {
        key: "circles",
        title: "Circles",
        shortTitle: "Circles",
        path: "/circles",
        description: "Trusted circles and social connections.",
        state: "live" as const
      },
      ...remainingModules
    ].map((item) => ({
      ...item,
      active: item.key === "calendar"
        ? route.path.startsWith("/work") && (route.query.tab === undefined || route.query.tab === "calendar")
        : item.key === "create-job"
          ? route.path.startsWith("/work") && route.query.tab === "create-job"
          : item.key === "find-work"
            ? route.path.startsWith("/work") && route.query.tab === "find-work"
            : isModuleActive(item.path)
    }))
  })

  return {
    homeTarget,
    isAuthenticated,
    topbarNavItems
  }
}
