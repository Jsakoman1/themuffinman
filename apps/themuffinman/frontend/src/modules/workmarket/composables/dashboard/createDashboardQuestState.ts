import {ref} from "vue"
import type {Quest} from "../../api/workmarketApi.ts"
import type {QuestAudience, QuestStatus, QuestStatusFilter} from "../../domain/workmarketDomain.ts"

export const createDashboardQuestState = () => {
  const questTitle = ref("")
  const questDescription = ref("")
  const questAwardAmount = ref("")
  const questAssigneeTarget = ref("1")
  const questShowApprovedApplicants = ref(false)
  const questScheduledAt = ref("")
  const questEndsAt = ref("")
  const questTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const questTermFixed = ref(false)
  const questAudience = ref<QuestAudience>("EVERYONE")
  const questSelectedCircleIds = ref<number[]>([])
  const questLocationVisibility = ref<NonNullable<Quest["locationVisibility"]>>("INHERIT")
  const questLocationSource = ref<NonNullable<Quest["locationSource"]>>("PROFILE")
  const questLocationCountry = ref("")
  const questLocationLocality = ref("")
  const questLocationPostalCode = ref("")
  const questLocationStreet = ref("")
  const questLocationHouseNumber = ref("")
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
  const editQuestAssigneeTarget = ref("1")
  const editQuestShowApprovedApplicants = ref(false)
  const editQuestScheduledAt = ref("")
  const editQuestEndsAt = ref("")
  const editQuestTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const editQuestTermFixed = ref(false)
  const editQuestAudience = ref<QuestAudience>("CIRCLES")
  const editQuestSelectedCircleIds = ref<number[]>([])
  const editQuestLocationVisibility = ref<NonNullable<Quest["locationVisibility"]>>("INHERIT")
  const editQuestLocationSource = ref<NonNullable<Quest["locationSource"]>>("PROFILE")
  const editQuestLocationCountry = ref("")
  const editQuestLocationLocality = ref("")
  const editQuestLocationPostalCode = ref("")
  const editQuestLocationStreet = ref("")
  const editQuestLocationHouseNumber = ref("")
  const editQuestCreatorId = ref("")
  const editQuestStatus = ref<QuestStatus>("OPEN")
  const editQuestImages = ref<string[]>([])

  const editingApplicationId = ref<number | null>(null)
  const editApplicationMessage = ref("")
  const editApplicationPrice = ref("")

  const questDialogId = ref<number | null>(null)
  const applicationDialogId = ref<number | null>(null)
  const userProfileDialogId = ref<number | null>(null)

  return {
    questTitle,
    questDescription,
    questAwardAmount,
    questAssigneeTarget,
    questShowApprovedApplicants,
    questScheduledAt,
    questEndsAt,
    questTermMode,
    questTermFixed,
    questAudience,
    questSelectedCircleIds,
    questLocationVisibility,
    questLocationSource,
    questLocationCountry,
    questLocationLocality,
    questLocationPostalCode,
    questLocationStreet,
    questLocationHouseNumber,
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
    editQuestAssigneeTarget,
    editQuestShowApprovedApplicants,
    editQuestScheduledAt,
    editQuestEndsAt,
    editQuestTermMode,
    editQuestTermFixed,
    editQuestAudience,
    editQuestSelectedCircleIds,
    editQuestLocationVisibility,
    editQuestLocationSource,
    editQuestLocationCountry,
    editQuestLocationLocality,
    editQuestLocationPostalCode,
    editQuestLocationStreet,
    editQuestLocationHouseNumber,
    editQuestCreatorId,
    editQuestStatus,
    editQuestImages,
    editingApplicationId,
    editApplicationMessage,
    editApplicationPrice,
    questDialogId,
    applicationDialogId,
    userProfileDialogId
  }
}
