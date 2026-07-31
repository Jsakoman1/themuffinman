import {userShellApi} from "./userShellApi.ts"

/** Calendar compatibility facade; projection rules remain entirely backend-owned. */
export const calendarApi = {
  getCalendarProjection: userShellApi.getCalendarProjection.bind(userShellApi),
  getBusinessOwnerCalendar: userShellApi.getBusinessOwnerCalendar.bind(userShellApi),
  executeCalendarBookingAction: userShellApi.executeCalendarBookingAction.bind(userShellApi)
}
