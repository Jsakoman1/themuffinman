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
        key: "side-job",
        title: "SideJob",
        shortTitle: "SideJob",
        path: "/work?tab=side-job",
        description: "Offer jobs and track applications.",
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
        : item.key === "side-job"
          ? route.path.startsWith("/work") && ["side-job", "create-job", "find-work"].includes(String(route.query.tab ?? ""))
          : isModuleActive(item.path)
    }))
  })

  return {
    homeTarget,
    isAuthenticated,
    topbarNavItems
  }
}
