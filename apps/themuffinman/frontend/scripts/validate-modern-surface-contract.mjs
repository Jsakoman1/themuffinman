import fs from "node:fs"
import path from "node:path"

const root = path.resolve(import.meta.dirname, "..")
const read = (relative) => fs.readFileSync(path.join(root, relative), "utf8")
const checks = [
  ["work applications route", read("src/router.ts").includes("WorkApplicationsView")],
  ["chat surface route", read("src/router.ts").includes("ChatSurfaceView")],
  ["applications load more", read("src/modules/app-shell/views/WorkApplicationsView.vue").includes("Load more")],
  ["chat conversation pagination", read("src/modules/app-shell/views/ChatSurfaceView.vue").includes("Load older messages")],
  ["work request cancellation", read("src/modules/app-shell/views/WorkDiscoveryView.vue").includes("AbortController")],
  ["vision result contract", read("src/modules/vision/api/visionConversationApi.ts").includes("hasMore: boolean")],
  ["shell does not locally truncate collections", !read("src/modules/app-shell/shellSurfaceData.ts").includes("slice(")],
  ["vision does not locally truncate discovery", !read("src/modules/vision/components/VisionCanvasRenderer.vue").match(/questDiscovery\.items[^\n]*slice\(|searchDiscovery\.items[^\n]*slice\(/)]
]

const failed = checks.filter(([, passed]) => !passed)
if (failed.length > 0) {
  console.error(`Modern surface contract failed: ${failed.map(([name]) => name).join(", ")}`)
  process.exit(1)
}

console.log(`Modern surface contract passed (${checks.length} checks).`)
