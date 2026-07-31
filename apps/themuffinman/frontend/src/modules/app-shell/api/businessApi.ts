import {userShellApi} from "./userShellApi.ts"

/** Business-only compatibility facade while legacy callers migrate away from userShellApi. */
export const businessApi = {
  getBusinessDashboard: userShellApi.getBusinessDashboard.bind(userShellApi),
  getBusinessProfile: userShellApi.getBusinessProfile.bind(userShellApi),
  getMyBusinessProfiles: userShellApi.getMyBusinessProfiles.bind(userShellApi),
  getBusinessOfferings: userShellApi.getBusinessOfferings.bind(userShellApi),
  getBusinessOfferingSetup: userShellApi.getBusinessOfferingSetup.bind(userShellApi),
  getBusinessAvailabilityRules: userShellApi.getBusinessAvailabilityRules.bind(userShellApi),
  getBusinessOwnerBookings: userShellApi.getBusinessOwnerBookings.bind(userShellApi),
  getMyBusinessBookings: userShellApi.getMyBusinessBookings.bind(userShellApi),
  getPublicBusinessPage: userShellApi.getPublicBusinessPage.bind(userShellApi),
  createCustomerBooking: userShellApi.createCustomerBooking.bind(userShellApi)
}
