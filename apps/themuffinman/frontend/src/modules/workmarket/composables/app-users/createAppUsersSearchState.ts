import {ref} from "vue"

export const createAppUsersSearchState = () => {
  const userSearch = ref("")

  return {
    userSearch
  }
}
