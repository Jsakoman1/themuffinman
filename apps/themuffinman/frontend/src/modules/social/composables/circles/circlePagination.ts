import type {Ref} from "vue"

export const movePagedValue = (
  page: Ref<number>,
  totalPages: number,
  delta: number,
  reload: () => void
) => {
  page.value = Math.min(Math.max(1, page.value + delta), totalPages)
  reload()
}
