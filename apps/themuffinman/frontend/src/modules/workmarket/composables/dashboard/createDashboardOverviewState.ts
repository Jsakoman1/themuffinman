import {computed} from "vue"
import {useRouter} from "vue-router"
import type {NavigationTarget} from "../../api/workmarketApi.ts"
import type {QuestDashboard} from "../useQuestDashboard.ts"
import {routeForNavigationTarget} from "../../shared/navigationTargets.ts"

type RailItem = {
  id: string
  questId: number
  title: string
  when: string
  navigation: NavigationTarget | null
}

type RailBucket = {
  key: string
  label: string
  tone: "success" | "warning" | "accent"
  items: RailItem[]
}

const isRailTone = (value: string): value is RailBucket["tone"] => {
  return value === "success" || value === "warning" || value === "accent"
}

export const createDashboardOverviewState = (dashboard: QuestDashboard) => {
  const router = useRouter()

  const mapSectionBucket = (bucket: NonNullable<QuestDashboard["dashboardSections"]>["overview"]["postedBuckets"][number]): RailBucket => ({
    key: bucket.key,
    label: bucket.label,
    tone: isRailTone(bucket.tone) ? bucket.tone : "accent",
    items: bucket.items.map((item) => ({
      id: item.id,
      questId: item.questId,
      title: item.title,
      when: item.whenLabel,
      navigation: "navigation" in item ? item.navigation : null
    }))
  })

  const postedBuckets = computed<RailBucket[]>(() => {
    const sectionBuckets = dashboard.dashboardSections?.overview?.postedBuckets
    return sectionBuckets?.map(mapSectionBucket) ?? []
  })

  const workBuckets = computed<RailBucket[]>(() => {
    const sectionBuckets = dashboard.dashboardSections?.overview?.workBuckets
    return sectionBuckets?.map(mapSectionBucket) ?? []
  })

  const openQuest = async (navigation: NavigationTarget | null) => {
    await router.push(routeForNavigationTarget(navigation))
  }

  return {
    postedBuckets,
    workBuckets,
    openQuest
  }
}
