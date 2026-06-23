export const formatQuestStatus = (status: string) => {
  if (status === "WAITING_CONFIRMATION") {
    return "Waiting confirmation"
  }

  return status.replaceAll("_", " ")
}

export const formatQuestLifecycleLabel = (status: string) => {
  if (status === "OPEN") {
    return "Open for applications"
  }

  if (status === "ASSIGNED") {
    return "Assigned to a worker"
  }

  if (status === "WAITING_CONFIRMATION") {
    return "Waiting on time confirmation"
  }

  if (status === "IN_PROGRESS") {
    return "Work in progress"
  }

  if (status === "COMPLETED") {
    return "Completed"
  }

  if (status === "CANCELLED") {
    return "Cancelled"
  }

  return formatQuestStatus(status)
}

export const formatApplicationStatus = (status: string) => {
  if (status === "PENDING") {
    return "Open"
  }

  if (status === "APPROVED") {
    return "Approved"
  }

  if (status === "DECLINED") {
    return "Declined"
  }

  if (status === "WITHDRAWN") {
    return "Withdrawn"
  }

  return status.replaceAll("_", " ")
}

export const statusBadgeClass = (status: string) => {
  if (status === "APPROVED") {
    return "badge badge--success"
  }

  if (status === "WAITING_CONFIRMATION") {
    return "badge badge--warning"
  }

  if (status === "DECLINED" || status === "WITHDRAWN") {
    return "badge badge--danger"
  }

  return "badge"
}

export const statusSurfaceClass = (status: string) => {
  if (status === "OPEN" || status === "PENDING") {
    return "status-surface status-surface--open"
  }

  if (status === "ASSIGNED" || status === "APPROVED") {
    return "status-surface status-surface--assigned"
  }

  if (status === "WAITING_CONFIRMATION") {
    return "status-surface status-surface--waiting"
  }

  if (status === "IN_PROGRESS") {
    return "status-surface status-surface--progress"
  }

  if (status === "COMPLETED") {
    return "status-surface status-surface--done"
  }

  if (status === "DECLINED" || status === "WITHDRAWN" || status === "CANCELLED") {
    return "status-surface status-surface--cancelled"
  }

  return "status-surface status-surface--open"
}
