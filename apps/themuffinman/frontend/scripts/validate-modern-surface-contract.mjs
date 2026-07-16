import fs from "node:fs"
import path from "node:path"

const root = path.resolve(import.meta.dirname, "..")
const read = (relative) => fs.readFileSync(path.join(root, relative), "utf8")
const checks = [
  ["work applications route", read("src/router.ts").includes("WorkApplicationsView")],
  ["work quest detail route", read("src/router.ts").includes("WorkQuestDetailView")],
  ["work quest create route", read("src/router.ts").includes("WorkQuestCreateView")],
  ["work quest application management route", read("src/router.ts").includes("WorkQuestApplicationsView")],
  ["business booking operations route", read("src/router.ts").includes("BusinessBookingsView")],
  ["business profile route", read("src/router.ts").includes("BusinessProfileView")],
  ["business offerings route", read("src/router.ts").includes("BusinessOfferingsView")],
  ["business availability route", read("src/router.ts").includes("BusinessAvailabilityView")],
  ["business public route", read("src/router.ts").includes("BusinessPublicView")],
  ["business customer bookings route", read("src/router.ts").includes("BusinessMyBookingsView")],
  ["business availability exceptions route", read("src/router.ts").includes("BusinessAvailabilityExceptionsView")],
  ["work review surface", read("src/modules/app-shell/views/WorkQuestDetailView.vue").includes("submitQuestReview")],
  ["notifications route", read("src/router.ts").includes("NotificationsView")],
  ["chat message composer", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("sendChatMessage")],
  ["circles route", read("src/router.ts").includes("CirclesView")],
  ["chat surface route", read("src/router.ts").includes("ChatSurfaceView")],
  ["applications load more", read("src/modules/app-shell/views/WorkApplicationsView.vue").includes("Load more")],
  ["chat conversation pagination", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("Load older messages")],
  ["chat sync recovery", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("getChatConversationSync") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("visibilitychange") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("online")],
  ["profile location settings route", read("src/router.ts").includes("ProfileLocationSettingsView") && read("src/modules/app-shell/views/ProfileLocationSettingsView.vue").includes("useCurrentLocation") && read("src/modules/app-shell/views/ProfileLocationSettingsView.vue").includes("updateCurrentAppUser")],
  ["profile exact visibility scope", read("src/modules/app-shell/views/ProfileLocationSettingsView.vue").includes("exactVisibilityScope") && read("src/modules/app-shell/views/ProfileLocationSettingsView.vue").includes("Selected circles") && read("src/modules/app-shell/views/ProfileLocationSettingsView.vue").includes("Selected people")],
  ["things listing routes", read("src/router.ts").includes("ThingsDiscoveryView") && read("src/router.ts").includes("ThingDetailView") && read("src/modules/app-shell/views/ThingsDiscoveryView.vue").includes("requestThingBorrow") && read("src/modules/app-shell/views/ThingDetailView.vue").includes("getThingListing")],
  ["circle privacy explanation", read("src/modules/app-shell/views/CirclesView.vue").includes("Circles are a trust boundary") && read("src/modules/app-shell/views/CirclesView.vue").includes("exact address")],
  ["chat attachment composer", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("uploadChatAttachment") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("type=\"file\"") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("attachmentName")],
  ["chat image attachment preview", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("attachmentPreviewUrl") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("URL.revokeObjectURL") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("accept=\"image/*")],
  ["work request cancellation", read("src/modules/app-shell/views/WorkDiscoveryView.vue").includes("AbortController")],
  ["vision result contract", read("src/modules/vision/api/visionConversationApi.ts").includes("hasMore: boolean")],
  ["vision fetch-more action", read("src/modules/vision/components/VisionCanvasRenderer.vue").includes("emit('fetchMore')") && read("src/modules/vision/composables/useVisionConversation.ts").includes("FETCH_MORE_RESULTS")],
  ["vision safe retry", read("src/modules/vision/components/VisionCanvasRenderer.vue").includes("emit('retry')") && read("src/modules/vision/composables/useVisionConversation.ts").includes("retryLastRequest")],
  ["shell does not locally truncate collections", !read("src/modules/app-shell/shellSurfaceData.ts").includes("slice(")],
  ["vision does not locally truncate discovery", !read("src/modules/vision/components/VisionCanvasRenderer.vue").match(/questDiscovery\.items[^\n]*slice\(|searchDiscovery\.items[^\n]*slice\(/)]
]

const failed = checks.filter(([, passed]) => !passed)
if (failed.length > 0) {
  console.error(`Modern surface contract failed: ${failed.map(([name]) => name).join(", ")}`)
  process.exit(1)
}

console.log(`Modern surface contract passed (${checks.length} checks).`)
