import type {Ref} from "vue"

type ErrorDetails = string[]

export const createDashboardErrorState = (state: {
  questsError: Ref<string>
  questsErrorDetails: Ref<ErrorDetails>
  applicationsError: Ref<string>
  applicationsErrorDetails: Ref<ErrorDetails>
  newsError: Ref<string>
  newsErrorDetails: Ref<ErrorDetails>
  usersError: Ref<string>
  usersErrorDetails: Ref<ErrorDetails>
}) => {
  const resetErrorState = () => {
    state.questsError.value = ""
    state.questsErrorDetails.value = []
    state.applicationsError.value = ""
    state.applicationsErrorDetails.value = []
    state.newsError.value = ""
    state.newsErrorDetails.value = []
    state.usersError.value = ""
    state.usersErrorDetails.value = []
  }

  return {
    resetErrorState
  }
}
