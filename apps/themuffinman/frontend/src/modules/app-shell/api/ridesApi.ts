import {userShellApi} from "./userShellApi.ts"
export const ridesApi = {getRideSuggestions: userShellApi.getRideSuggestions.bind(userShellApi), getCalendarProjection: userShellApi.getCalendarProjection.bind(userShellApi)}
