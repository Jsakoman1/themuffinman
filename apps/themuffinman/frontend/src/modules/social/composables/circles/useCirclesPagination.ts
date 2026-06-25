import type {Ref} from "vue"
import {movePagedValue} from "./circlePagination.ts"

type PaginationState = {
  inboxTab: {value: "incoming" | "outgoing"}
  incomingPage: Ref<number>
  outgoingPage: Ref<number>
  incomingPages: {value: number}
  outgoingPages: {value: number}
  connectionsPage: Ref<number>
  connectionsPages: {value: number}
}

export const useCirclesPagination = (
  state: PaginationState,
  loaders: {
    loadInboxPage: () => Promise<void>
    loadConnectionsPage: () => Promise<void>
  }
) => {
  const previousInboxPage = () => {
    if (state.inboxTab.value === "incoming") {
      movePagedValue(state.incomingPage, state.incomingPages.value, -1, () => void loaders.loadInboxPage())
      return
    }

    movePagedValue(state.outgoingPage, state.outgoingPages.value, -1, () => void loaders.loadInboxPage())
  }

  const nextInboxPage = () => {
    if (state.inboxTab.value === "incoming") {
      movePagedValue(state.incomingPage, state.incomingPages.value, 1, () => void loaders.loadInboxPage())
      return
    }

    movePagedValue(state.outgoingPage, state.outgoingPages.value, 1, () => void loaders.loadInboxPage())
  }

  const previousConnectionsPage = () => {
    movePagedValue(state.connectionsPage, state.connectionsPages.value, -1, () => void loaders.loadConnectionsPage())
  }

  const nextConnectionsPage = () => {
    movePagedValue(state.connectionsPage, state.connectionsPages.value, 1, () => void loaders.loadConnectionsPage())
  }

  return {
    previousInboxPage,
    nextInboxPage,
    previousConnectionsPage,
    nextConnectionsPage
  }
}
