import {ref} from "vue"
import type {AppUser, AppUserRoleOption} from "../../api/workmarketApi.ts"

export const createAppUsersDataState = () => {
  const appUsers = ref<AppUser[]>([])
  const roleOptions = ref<AppUserRoleOption[]>([])
  const isLoadingUsers = ref(false)
  const pageError = ref("")
  const pageErrorDetails = ref<string[]>([])

  return {
    appUsers,
    roleOptions,
    isLoadingUsers,
    pageError,
    pageErrorDetails
  }
}
