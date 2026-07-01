import type {RouteLocationRaw} from "vue-router"
import type {NavigationTarget, NavigationTargetType} from "../api/workmarketApi.ts"

const routeForTargetType = (type: NavigationTargetType, entityId: number | null | undefined): RouteLocationRaw => {
  switch (type) {
    case "QUEST_DETAIL":
      return entityId === null || entityId === undefined ? "/vision" : `/work/${entityId}`
    case "APPLICATION_DETAIL":
      return entityId === null || entityId === undefined ? "/vision" : `/applications/${entityId}`
    case "USER_PROFILE":
      return entityId === null || entityId === undefined ? "/vision" : `/users/${entityId}`
    case "QUEST_LIST":
      return "/vision"
    case "CIRCLES":
      return "/circles"
    default:
      return "/vision"
  }
}

export const routeForNavigationTarget = (target: NavigationTarget | null | undefined): RouteLocationRaw => {
  if (!target) {
    return "/vision"
  }

  return routeForTargetType(target.type, target.entityId)
}
