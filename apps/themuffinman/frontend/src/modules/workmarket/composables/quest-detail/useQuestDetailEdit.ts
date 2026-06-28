import {computed, ref, watch, type Ref} from "vue"
import {getApiErrorMessage} from "../../../../api/apiErrors.ts"
import {QUEST_IMAGE_PROCESSING_ERROR_MESSAGE, QUEST_IMAGE_TOO_LARGE_MESSAGE} from "../../../../shared/clientMessages.ts"
import {compressQuestImageFile} from "../../../../shared/imageCompression.ts"
import {formatInstantForInput, parseInstantFromInput} from "../../../../shared/questSchedule.ts"
import {workmarketApi, type CircleGroup, type Quest, type QuestAudienceOption, type QuestLocationVisibilityOption} from "../../api/workmarketApi.ts"
import {buildQuestAwardAmountInput} from "../../shared/pricing.ts"

type QuestDetailEditState = {
  quest: Ref<Quest | null>
  error: Ref<string>
  isSaving: Ref<boolean>
  init: () => Promise<void>
}

const resolveQuestTermMode = (quest: Quest) => (
  quest.termFixed
    ? (quest.endsAt ? "start-end" : "start-only")
    : "flexible"
)

export const useQuestDetailEdit = (state: QuestDetailEditState) => {
  const isEditing = ref(false)
  const editTitle = ref("")
  const editDescription = ref("")
  const editAwardAmount = ref("")
  const editAssigneeTarget = ref("1")
  const editShowApprovedApplicants = ref(false)
  const editScheduledAt = ref("")
  const editEndsAt = ref("")
  const editTermMode = ref<"flexible" | "start-only" | "start-end">("flexible")
  const editAudience = ref<"EVERYONE" | "CIRCLES">("CIRCLES")
  const editSelectedCircleIds = ref<number[]>([])
  const editLocationVisibility = ref<NonNullable<Quest["locationVisibility"]>>("INHERIT")
  const editLocationSource = ref<NonNullable<Quest["locationSource"]>>("PROFILE")
  const editLocationCountry = ref("")
  const editLocationLocality = ref("")
  const editLocationPostalCode = ref("")
  const editLocationStreet = ref("")
  const editLocationHouseNumber = ref("")
  const editImages = ref<string[]>([])
  const circleGroups = ref<CircleGroup[]>([])
  const questAudienceOptions = ref<QuestAudienceOption[]>([])
  const questLocationVisibilityOptions = ref<QuestLocationVisibilityOption[]>([])

  const canEdit = computed(() => state.quest.value?.presentation.canEdit ?? false)

  const syncEditStateFromQuest = () => {
    if (!state.quest.value) {
      return
    }

    editTitle.value = state.quest.value.title
    editDescription.value = state.quest.value.description
    editAwardAmount.value = String(state.quest.value.awardAmount ?? "")
    editAssigneeTarget.value = String(state.quest.value.assigneeTarget ?? 1)
    editShowApprovedApplicants.value = state.quest.value.showApprovedApplicants
    editScheduledAt.value = formatInstantForInput(state.quest.value.scheduledAt)
    editEndsAt.value = formatInstantForInput(state.quest.value.endsAt)
    editTermMode.value = resolveQuestTermMode(state.quest.value)
    editAudience.value = state.quest.value.audience
    editSelectedCircleIds.value = state.quest.value.visibleToCircles.map((circle) => circle.id)
    editLocationVisibility.value = state.quest.value.locationVisibility
    editLocationSource.value = state.quest.value.locationSource ?? "PROFILE"
    editLocationCountry.value = state.quest.value.locationCountry ?? ""
    editLocationLocality.value = state.quest.value.locationLocality ?? ""
    editLocationPostalCode.value = state.quest.value.locationPostalCode ?? ""
    editLocationStreet.value = state.quest.value.locationStreet ?? ""
    editLocationHouseNumber.value = state.quest.value.locationHouseNumber ?? ""
    editImages.value = [...state.quest.value.images]
  }

  const startEditing = () => {
    syncEditStateFromQuest()
    isEditing.value = true
  }

  const cancelEditing = () => {
    isEditing.value = false
    syncEditStateFromQuest()
  }

  const setEditTermMode = (mode: "flexible" | "start-only" | "start-end") => {
    editTermMode.value = mode
    if (mode === "flexible") {
      editScheduledAt.value = ""
      editEndsAt.value = ""
      return
    }

    if (mode === "start-only") {
      editEndsAt.value = ""
    }
  }

  const toggleEditCircle = (circleId: number) => {
    editSelectedCircleIds.value = editSelectedCircleIds.value.includes(circleId)
      ? editSelectedCircleIds.value.filter((id) => id !== circleId)
      : [...editSelectedCircleIds.value, circleId]
  }

  const removeEditImage = (index: number) => {
    editImages.value = editImages.value.filter((_, currentIndex) => currentIndex !== index)
  }

  const handleEditImagesChange = async (event: Event) => {
    const input = event.target as HTMLInputElement | null
    const files = input?.files

    if (!files?.length) {
      if (input) {
        input.value = ""
      }
      return
    }

    const slotsLeft = Math.max(0, 10 - editImages.value.length)
    if (slotsLeft === 0) {
      state.error.value = "A quest can have at most 10 images."
      if (input) {
        input.value = ""
      }
      return
    }

    const selectedFiles = Array.from(files).slice(0, slotsLeft)

    try {
      const compressed = await Promise.all(selectedFiles.map((file) => compressQuestImageFile(file)))
      editImages.value = [...editImages.value, ...compressed].slice(0, 10)
    } catch (error) {
      state.error.value = error instanceof Error && error.message === QUEST_IMAGE_TOO_LARGE_MESSAGE
        ? error.message
        : QUEST_IMAGE_PROCESSING_ERROR_MESSAGE
    } finally {
      if (input) {
        input.value = ""
      }
    }
  }

  const saveEdits = async () => {
    if (!state.quest.value) {
      return
    }

    state.isSaving.value = true
    state.error.value = ""

    try {
      await workmarketApi.updateQuest(state.quest.value.id, {
        title: editTitle.value.trim(),
        description: editDescription.value,
        awardAmount: buildQuestAwardAmountInput(editAwardAmount.value),
        assigneeTarget: Number(editAssigneeTarget.value || "1"),
        showApprovedApplicants: editShowApprovedApplicants.value,
        scheduledAt: editScheduledAt.value ? parseInstantFromInput(editScheduledAt.value) : null,
        endsAt: editEndsAt.value ? parseInstantFromInput(editEndsAt.value) : null,
        termFixed: editTermMode.value !== "flexible",
        audience: editAudience.value,
        selectedCircleIds: editAudience.value === "CIRCLES" ? [...editSelectedCircleIds.value] : [],
        locationVisibility: editLocationVisibility.value,
        locationSource: editLocationSource.value,
        locationCountry: editLocationCountry.value || null,
        locationLocality: editLocationLocality.value || null,
        locationPostalCode: editLocationPostalCode.value || null,
        locationStreet: editLocationStreet.value || null,
        locationHouseNumber: editLocationHouseNumber.value || null,
        images: [...editImages.value]
      })

      await state.init()
      isEditing.value = false
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not update quest.")
    } finally {
      state.isSaving.value = false
    }
  }

  const assignQuestNow = async () => {
    if (!state.quest.value) {
      return
    }

    state.isSaving.value = true
    state.error.value = ""

    try {
      await workmarketApi.updateQuest(state.quest.value.id, {
        title: state.quest.value.title,
        description: state.quest.value.description,
        awardAmount: state.quest.value.awardAmount,
        assigneeTarget: state.quest.value.assigneeTarget ?? 1,
        showApprovedApplicants: state.quest.value.showApprovedApplicants,
        status: "ASSIGNED"
      })
      await state.init()
    } catch (requestError) {
      state.error.value = getApiErrorMessage(requestError, "Could not assign quest.")
    } finally {
      state.isSaving.value = false
    }
  }

  const loadEditMetadata = async () => {
    try {
      const [options, circles] = await Promise.all([
        workmarketApi.getAppUserOptions(),
        workmarketApi.getCircleGroups()
      ])
      questAudienceOptions.value = options.questAudiences
      questLocationVisibilityOptions.value = options.questLocationVisibilities
      circleGroups.value = circles
    } catch {
      // Keep quest detail usable even if supplemental edit metadata fails to load.
    }
  }

  watch(() => state.quest.value?.id, () => {
    if (!state.quest.value) {
      isEditing.value = false
      return
    }

    syncEditStateFromQuest()
    isEditing.value = state.quest.value.presentation.autoOpenEditForm ?? false
  }, {immediate: true})

  return {
    isEditing,
    editTitle,
    editDescription,
    editAwardAmount,
    editAssigneeTarget,
    editShowApprovedApplicants,
    editScheduledAt,
    editEndsAt,
    editTermMode,
    editAudience,
    editSelectedCircleIds,
    editLocationVisibility,
    editLocationSource,
    editLocationCountry,
    editLocationLocality,
    editLocationPostalCode,
    editLocationStreet,
    editLocationHouseNumber,
    editImages,
    circleGroups,
    questAudienceOptions,
    questLocationVisibilityOptions,
    canEdit,
    startEditing,
    cancelEditing,
    setEditTermMode,
    toggleEditCircle,
    removeEditImage,
    handleEditImagesChange,
    saveEdits,
    assignQuestNow,
    loadEditMetadata
  }
}
