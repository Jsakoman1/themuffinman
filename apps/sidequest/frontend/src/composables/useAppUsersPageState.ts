import {computed, ref} from "vue"
import type {AppUser} from "../api/sidequestApi.ts"
import {appUserRoleOptions, type AppUserRole} from "../shared/sidequestDomain.ts"
import {useTimedBanner} from "./useTimedBanner.ts"

export const useAppUsersPageState = () => {
  const appUsers = ref<AppUser[]>([])
  const isLoadingUsers = ref(false)
  const pageError = ref("")
  const pageErrorDetails = ref<string[]>([])
  const copiedDebugBanner = useTimedBanner(1500)
  const copiedDebug = computed(() => !!copiedDebugBanner.message.value)
  const feedback = ref("")
  const feedbackType = ref<"error" | "success">("success")
  const isCreateUserDialogOpen = ref(false)

  const email = ref("")
  const username = ref("")
  const password = ref("")
  const role = ref<AppUserRole>("USER")

  const editingAppUserId = ref<number | null>(null)
  const editAppUserEmail = ref("")
  const editAppUserUsername = ref("")
  const editAppUserRole = ref<AppUserRole>("USER")
  const editAppUserPassword = ref("")

  const showFeedback = (message: string, type: "error" | "success") => {
    feedback.value = message
    feedbackType.value = type
  }

  const showCopiedDebug = () => {
    copiedDebugBanner.show("Copied")
  }

  const openCreateUserDialog = () => {
    isCreateUserDialogOpen.value = true
  }

  const closeCreateUserDialog = () => {
    isCreateUserDialogOpen.value = false
  }

  return {
    appUsers,
    isLoadingUsers,
    pageError,
    pageErrorDetails,
    copiedDebug,
    feedback,
    feedbackType,
    isCreateUserDialogOpen,
    email,
    username,
    password,
    role,
    roleOptions: appUserRoleOptions,
    editingAppUserId,
    editAppUserEmail,
    editAppUserUsername,
    editAppUserRole,
    editAppUserPassword,
    showCopiedDebug,
    showFeedback,
    openCreateUserDialog,
    closeCreateUserDialog
  }
}

export type AppUsersPageState = ReturnType<typeof useAppUsersPageState>
