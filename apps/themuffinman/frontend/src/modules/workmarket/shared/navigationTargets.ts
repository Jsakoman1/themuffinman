import type {RouteLocationRaw} from "vue-router"
import type {NavigationTarget, NavigationTargetType} from "../api/workmarketApi.ts"

const routeForTargetType = (type: NavigationTargetType, entityId: number | null | undefined): RouteLocationRaw => {
  switch (type) {
    case "QUEST_DETAIL":
      return entityId === null || entityId === undefined ? "/quests" : `/quests/${entityId}`
    case "APPLICATION_DETAIL":
      return entityId === null || entityId === undefined ? "/quests" : `/applications/${entityId}`
    case "USER_PROFILE":
      return entityId === null || entityId === undefined ? "/quests" : `/users/${entityId}`
    case "QUEST_LIST":
      return "/quests"
    case "CIRCLES":
      return "/circles"
    default:
      return "/quests"
  }
}

export const routeForNavigationTarget = (target: NavigationTarget | null | undefined): RouteLocationRaw => {
  if (!target) {
    return "/quests"
  }

  return routeForTargetType(target.type, target.entityId)
}
