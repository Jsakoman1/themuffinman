import type {Ref} from "vue"
import type {CircleContact} from "../../../workmarket/api/workmarketApi.ts"

export const sameCircleIds = (left: number[], right: number[]) => {
  if (left.length !== right.length) {
    return false
  }

  const leftSorted = [...left].sort((a, b) => a - b)
  const rightSorted = [...right].sort((a, b) => a - b)
  return leftSorted.every((value, index) => value === rightSorted[index])
}

export const createCircleSelectionState = (
  selectedCircleIdsByUserId: Ref<Record<number, number[]>>
) => {
  const getSelectedCircleIds = (connection: CircleContact) => {
    return selectedCircleIdsByUserId.value[connection.userId] ?? connection.circleIds
  }

  const toggleConnectionCircle = (connection: CircleContact, circleId: number) => {
    const currentIds = getSelectedCircleIds(connection)
    selectedCircleIdsByUserId.value = {
      ...selectedCircleIdsByUserId.value,
      [connection.userId]: currentIds.includes(circleId)
        ? currentIds.filter((id) => id !== circleId)
        : [...currentIds, circleId]
    }
  }

  const resetConnectionCircles = (connection: CircleContact) => {
    selectedCircleIdsByUserId.value = {
      ...selectedCircleIdsByUserId.value,
      [connection.userId]: [...connection.circleIds]
    }
  }

  const hasPendingCircleChanges = (connection: CircleContact) => {
    return !sameCircleIds(getSelectedCircleIds(connection), connection.circleIds)
  }

  return {
    getSelectedCircleIds,
    toggleConnectionCircle,
    resetConnectionCircles,
    hasPendingCircleChanges
  }
}
