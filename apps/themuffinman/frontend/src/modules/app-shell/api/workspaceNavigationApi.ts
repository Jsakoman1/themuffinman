import {api, withAuth} from "../../../api/httpClient.ts"
import type {WorkspaceNavigationResponse} from "../../../contracts/index.ts"

export const workspaceNavigationApi = {
  async get(signal?: AbortSignal): Promise<WorkspaceNavigationResponse> {
    return (await api.get<WorkspaceNavigationResponse>("/workspace/navigation", {signal, ...withAuth()})).data
  }
}
