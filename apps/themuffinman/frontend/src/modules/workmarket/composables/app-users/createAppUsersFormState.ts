import {ref} from "vue"
import type {AppUserRole} from "../../domain/workmarketDomain.ts"

export const createAppUsersFormState = () => {
  const email = ref("")
  const username = ref("")
  const password = ref("")
  const role = ref<AppUserRole>("USER")

  const editingAppUserId = ref<number | null>(null)
  const editAppUserEmail = ref("")
  const editAppUserUsername = ref("")
  const editAppUserRole = ref<AppUserRole>("USER")
  const editAppUserPassword = ref("")

  return {
    email,
    username,
    password,
    role,
    editingAppUserId,
    editAppUserEmail,
    editAppUserUsername,
    editAppUserRole,
    editAppUserPassword
  }
}
