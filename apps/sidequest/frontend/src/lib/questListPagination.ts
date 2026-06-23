export const getPaginationState = (totalItems: number, currentPage: number, itemsPerPage: number) => {
  const safeItemsPerPage = Math.max(1, itemsPerPage)
  const totalPages = Math.max(1, Math.ceil(totalItems / safeItemsPerPage))
  const safeCurrentPage = Math.min(Math.max(1, currentPage), totalPages)
  const pageStart = totalItems === 0 ? 0 : (safeCurrentPage - 1) * safeItemsPerPage + 1
  const pageEnd = Math.min(totalItems, safeCurrentPage * safeItemsPerPage)

  return {
    totalPages,
    pageStart,
    pageEnd,
    hasPreviousPage: safeCurrentPage > 1,
    hasNextPage: safeCurrentPage < totalPages,
    currentPage: safeCurrentPage
  }
}
