import {userShellApi} from "./userShellApi.ts"
export const socialApi = {getCirclesOverview: userShellApi.getCirclesOverview.bind(userShellApi), getCircleGroups: userShellApi.getCircleGroups.bind(userShellApi), createCircleRequest: userShellApi.createCircleRequest.bind(userShellApi), searchCircleUsers: userShellApi.searchCircleUsers.bind(userShellApi)}
