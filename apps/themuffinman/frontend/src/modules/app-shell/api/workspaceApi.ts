import {userShellApi} from "./userShellApi.ts"

export const workspaceApi = {
  getWorkspaceHome: userShellApi.getWorkspaceHome.bind(userShellApi),
  getWorkspaceNavigation: userShellApi.getWorkspaceNavigation.bind(userShellApi),
  getWorkspaceCommandCatalog: userShellApi.getWorkspaceCommandCatalog.bind(userShellApi),
  persistWorkspaceContext: userShellApi.persistWorkspaceContext.bind(userShellApi),
  getDashboard: userShellApi.getDashboard.bind(userShellApi),
  getDashboardSummary: userShellApi.getDashboardSummary.bind(userShellApi)
}
