import fs from "node:fs"
import path from "node:path"

const root = path.resolve(import.meta.dirname, "..")
const read = (file) => fs.readFileSync(path.join(root, file), "utf8")
const checks = [
  ["generic preview regression audit", read("scripts/validate-preview-free-contract.mjs").includes("CollectionContextRail")],
  ["desktop navigation exposes calendar and chat destinations", read("src/modules/app-shell/components/WorkspaceModuleRail.vue").includes('calendar: "calendar"') && read("src/modules/app-shell/components/WorkspaceModuleRail.vue").includes('chat: "chat"')],
  ["notifications open their destination directly", read("src/modules/app-shell/views/NotificationsView.vue").includes("router.push")],
  ["discovery rows provide direct details", read("src/modules/app-shell/components/SurfaceRow.vue").includes("RouterLink")],
  ["chat has its dedicated conversation surface", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("chat-surface__composer")],
  ["calendar has explicit day week month controls", read("src/modules/app-shell/views/CalendarPage.vue").includes('mode.value === "day"') && read("src/modules/app-shell/views/CalendarPage.vue").includes('mode.value === "week"') && read("src/modules/app-shell/views/CalendarPage.vue").includes('mode.value === "month"')],
  ["business actions are backend-prepared", read("src/modules/app-shell/components/ClientActionList.vue").includes("ClientActionDTO")],
  ["renderer archetypes are explicit", read("src/modules/app-shell/components/SurfaceContentView.vue").includes("DashboardSurfaceRenderer")]
]
const failed = checks.filter(([, passed]) => !passed)
if (failed.length) throw new Error(`Human-first Web contract failed: ${failed.map(([name]) => name).join(", ")}`)
console.log(`Human-first Web contract passed (${checks.length} checks).`)
