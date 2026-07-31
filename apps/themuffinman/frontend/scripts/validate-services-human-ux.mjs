import {readFileSync} from "node:fs"
import {resolve} from "node:path"

const root = resolve(import.meta.dirname, "..")
const read = (path) => readFileSync(resolve(root, path), "utf8")
const checks = [
  ["Public calendar has Month Week and Day views", read("src/modules/app-shell/components/BusinessAvailabilityCalendar.vue").includes("['MONTH','WEEK','DAY']")],
  ["Services accessibility contract covers calendar and reviews", read("src/modules/app-shell/components/BusinessAvailabilityCalendar.vue").includes("availability in text") && read("src/modules/app-shell/components/BusinessReviewList.vue").includes("narrow screens")],
  ["Services discovery uses provider cards", read("src/modules/app-shell/views/BusinessDiscoveryView.vue").includes("BusinessDiscoveryCard")],
  ["Provider card has a direct public-page route", read("src/modules/app-shell/components/BusinessDiscoveryCard.vue").includes("/business/public/") === false && read("src/modules/app-shell/views/BusinessDiscoveryView.vue").includes("/business/public/")],
  ["Provider card has an image fallback", read("src/modules/app-shell/components/BusinessDiscoveryCard.vue").includes("business-discovery-card__fallback")],
  ["Provider card states booking availability in plain language", read("src/modules/app-shell/components/BusinessDiscoveryCard.vue").includes("Appointments can be requested online")],
  ["Discovery keeps Services tabs", read("src/modules/app-shell/views/BusinessDiscoveryView.vue").includes("getModuleTabs(\"services\")")],
  ["Public profile has local tabs", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("BusinessPublicSectionTabs")],
  ["Public profile shows rating summary", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("ratingSummary")],
  ["Public profile has service price cards", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("BusinessServiceCard")],
  ["Public booking uses an availability calendar", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("BusinessAvailabilityCalendar")]
  ,["Booking keeps a visible selection summary", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("BusinessBookingSelectionSummary")]
  ,["Public Reviews shows verified review records", read("src/modules/app-shell/views/BusinessPublicView.vue").includes("BusinessReviewList")]
  ,["Completed bookings offer a review form", read("src/modules/app-shell/views/BusinessMyBookingsView.vue").includes("BusinessReviewForm")]
]

const failures = checks.filter(([, passed]) => !passed).map(([label]) => label)
if (failures.length) {
  console.error(`Services human UX validation failed:\n- ${failures.join("\n- ")}`)
  process.exit(1)
}

console.log(`Services human UX validation passed (${checks.length} checks).`)
