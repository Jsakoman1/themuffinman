import {ref} from "vue"
import type {QuestAudience, QuestStatus, QuestStatusFilter, OverviewFocus} from "../../domain/workmarketDomain.ts"

export const createDashboardQuestState = () => {
  const questTitle = ref("")
  const questDescription = ref("")
  const questAwardAmount = ref("")
  const questScheduledAt = ref("")
  const questEndsAt = ref("")
  const questTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const questTermFixed = ref(false)
  const questAudience = ref<QuestAudience>("CIRCLES")
  const questSelectedCircleIds = ref<number[]>([])
  const questCreatorId = ref("")
  const questImages = ref<string[]>([])

  const adminQuestStatusFilter = ref<QuestStatusFilter>("ALL")

  const applicationMessages = ref<Record<number, string>>({})
  const proposedPrices = ref<Record<number, string>>({})
  const openApplicationsQuestIds = ref<Record<number, boolean>>({})
  const showAllApplicationsQuestIds = ref<Record<number, boolean>>({})
  const questDisclosureRefs = ref<Record<number, HTMLDetailsElement | null>>({})

  const editingQuestId = ref<number | null>(null)
  const editQuestTitle = ref("")
  const editQuestDescription = ref("")
  const editQuestAwardAmount = ref("")
  const editQuestScheduledAt = ref("")
  const editQuestEndsAt = ref("")
  const editQuestTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const editQuestTermFixed = ref(false)
  const editQuestAudience = ref<QuestAudience>("CIRCLES")
  const editQuestSelectedCircleIds = ref<number[]>([])
  const editQuestCreatorId = ref("")
  const editQuestStatus = ref<QuestStatus>("OPEN")
  const editQuestImages = ref<string[]>([])

  const editingApplicationId = ref<number | null>(null)
  const editApplicationMessage = ref("")
  const editApplicationPrice = ref("")

  const overviewFocus = ref<OverviewFocus | null>(null)
  const questDialogId = ref<number | null>(null)
  const applicationDialogId = ref<number | null>(null)
  const userProfileDialogId = ref<number | null>(null)
  const isCreateJobDialogOpen = ref(false)
  const isFindWorkDialogOpen = ref(false)
  const isOpenWorkDialogOpen = ref(false)
  const isApplicationsDialogOpen = ref(false)

  return {
    questTitle,
    questDescription,
    questAwardAmount,
    questScheduledAt,
    questEndsAt,
    questTermMode,
    questTermFixed,
    questAudience,
    questSelectedCircleIds,
    questCreatorId,
    questImages,
    adminQuestStatusFilter,
    applicationMessages,
    proposedPrices,
    openApplicationsQuestIds,
    showAllApplicationsQuestIds,
    questDisclosureRefs,
    editingQuestId,
    editQuestTitle,
    editQuestDescription,
    editQuestAwardAmount,
    editQuestScheduledAt,
    editQuestEndsAt,
    editQuestTermMode,
    editQuestTermFixed,
    editQuestAudience,
    editQuestSelectedCircleIds,
    editQuestCreatorId,
    editQuestStatus,
    editQuestImages,
    editingApplicationId,
    editApplicationMessage,
    editApplicationPrice,
    overviewFocus,
    questDialogId,
    applicationDialogId,
    userProfileDialogId,
    isCreateJobDialogOpen,
    isFindWorkDialogOpen,
    isOpenWorkDialogOpen,
    isApplicationsDialogOpen
  }
}
