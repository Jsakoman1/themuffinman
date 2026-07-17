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
  ["shared icon action primitive", read("src/modules/app-shell/components/AppIconButton.vue").includes("aria-label") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("AppIconButton")],
  ["shared overlay primitive", read("src/modules/app-shell/components/AppDialog.vue").includes("role=\"dialog\"") && read("src/modules/app-shell/views/WorkQuestDetailView.vue").includes("AppDialog")],
  ["shared status primitive", read("src/modules/app-shell/components/AppStatus.vue").includes("app-status--error") && read("src/modules/app-shell/views/RidesView.vue").includes("AppStatus") && read("src/modules/app-shell/views/ThingsDiscoveryView.vue").includes("AppStatus")],
  ["shared rich text editor", read("src/modules/app-shell/components/RichTextEditor.vue").includes("StarterKit") && read("src/modules/app-shell/views/WorkQuestCreateView.vue").includes("RichTextEditor")],
  ["shared rich text preview", read("src/modules/app-shell/components/RichTextPreview.vue").includes("allowed") && read("src/modules/app-shell/views/WorkQuestDetailView.vue").includes("RichTextPreview")],
  ["vision does not locally truncate discovery", !read("src/modules/vision/components/VisionCanvasRenderer.vue").match(/questDiscovery\.items[^\n]*slice\(|searchDiscovery\.items[^\n]*slice\(/)]
  , ["shell core navigation model", read("src/modules/app-shell/shellRouteRegistry.ts").includes('primaryNavigationSurfaceIds: AppSurfaceId[] = ["home", "work", "chat", "calendar"]')]
  , ["shell secondary navigation model", read("src/modules/app-shell/shellRouteRegistry.ts").includes('secondaryNavigationSurfaceIds: AppSurfaceId[] = ["business", "circles", "things", "rides"]')]
  , ["shell exposes account navigation", read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes('to="/profile/settings"')]
  , ["shell renders more modules disclosure", read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes("More modules") && read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes("appSecondaryNavItems")]
  , ["shared card primitive", read("src/modules/app-shell/components/AppCard.vue").includes("app-card--interactive") && read("src/modules/app-shell/components/AppCard.vue").includes("isNestedAction")]
  , ["shared action menu primitive", read("src/modules/app-shell/components/AppActionMenu.vue").includes("More actions") && read("src/modules/app-shell/components/AppActionMenu.vue").includes("<details")]
  , ["shared form field primitive", read("src/modules/app-shell/components/AppFormField.vue").includes("app-form-field__label") && read("src/modules/app-shell/views/WorkQuestCreateView.vue").includes("AppFormField")]
  , ["shared form footer primitive", read("src/modules/app-shell/components/AppFormFooter.vue").includes("app-form-footer") && read("src/modules/app-shell/views/WorkQuestCreateView.vue").includes("AppFormFooter")]
  , ["chat mobile back affordance", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("Back to inbox") && read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("chat-surface__back")]
  , ["calendar accessible mode controls", read("src/modules/app-shell/components/SurfaceContentView.vue").includes("aria-pressed") && read("src/modules/app-shell/components/SurfaceContentView.vue").includes("Calendar controls")]
  , ["design tokens include semantic states", read("src/styles/base.css").includes("--danger") && read("src/styles/base.css").includes("--success") && read("src/styles/base.css").includes("--focus-ring")]
  , ["disabled controls expose affordance", read("src/styles/base.css").includes("button:disabled") && read("src/styles/base.css").includes("aria-disabled")]
  , ["icon actions remain named", read("src/modules/app-shell/components/AppIconButton.vue").includes("aria-label")]
  , ["home does not duplicate navigation actions", read("src/modules/app-shell/shellDefinitions.ts").includes('id: "home", archetype: "home", navId: "home", eyebrow: "Home", title: "Home",\n    actions: []')]
  , ["home metrics have destinations", read("src/modules/app-shell/shellSurfaceData.ts").includes('to: {path: "/work/find"}') && read("src/modules/app-shell/components/SurfaceMetricGrid.vue").includes("metric.to ? RouterLink")]
  , ["business landing has setup recovery", read("src/modules/app-shell/shellSurfaceData.ts").includes("business-setup-profile") && read("src/modules/app-shell/shellSurfaceData.ts").includes("Set up your business profile")]
  , ["find work uses available preset", read("src/modules/app-shell/views/WorkDiscoveryView.vue").includes('preset: isMine.value ? "MY_VISIBLE" : "AVAILABLE"')]
  , ["find work avoids duplicate open action", !read("src/modules/app-shell/views/WorkDiscoveryView.vue").includes('class="work-discovery__open"')]
  , ["surface archetype primitives", read("src/modules/app-shell/components/SurfaceHeader.vue").includes("Surface actions") && read("src/modules/app-shell/components/SurfaceMetricGrid.vue").includes("surface-metric-grid") && read("src/modules/app-shell/components/SurfaceSection.vue").includes("SurfaceRow")]
  , ["surface content delegates shared rendering", read("src/modules/app-shell/components/SurfaceContentView.vue").includes("<SurfaceHeader") && read("src/modules/app-shell/components/SurfaceContentView.vue").includes("<SurfaceMetricGrid") && read("src/modules/app-shell/components/SurfaceContentView.vue").includes("<SurfaceSection")]
  , ["global Vision entry is shell-owned", read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes("<GlobalVisionEntry") && read("src/modules/app-shell/components/GlobalVisionEntry.vue").includes("Microphone input")]
  , ["account menu owns username and logout", read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes("<AccountMenu") && read("src/modules/app-shell/components/AccountMenu.vue").includes("handleLogout")]
  , ["shell has no duplicate inline Vision form", !read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes('<form class="app-shell__vision-form"')]
  , ["global Create entry is shell-owned", read("src/modules/app-shell/views/AuthenticatedShellView.vue").includes("<UniversalCreateMenu") && read("src/modules/app-shell/components/UniversalCreateMenu.vue").includes("Offer work")]
  , ["offer work route is discoverable", read("src/router.ts").includes("path: 'work/offer'") && read("src/modules/app-shell/shellDefinitions.ts").includes('label: "Offer work"')]
]

const failed = checks.filter(([, passed]) => !passed)
if (failed.length > 0) {
  console.error(`Modern surface contract failed: ${failed.map(([name]) => name).join(", ")}`)
  process.exit(1)
}

console.log(`Modern surface contract passed (${checks.length} checks).`)
