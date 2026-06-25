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
    const workModule = productModules.find((module) => module.key === "work")
    const remainingModules = productModules.filter((module) => module.key !== "work")

    return [
      ...(workModule ? [workModule] : []),
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
      active: isModuleActive(item.path)
    }))
  })

  return {
    homeTarget,
    isAuthenticated,
    topbarNavItems
  }
}
