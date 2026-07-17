import fs from "node:fs"
import path from "node:path"

const frontendRoot = path.resolve(new URL(".", import.meta.url).pathname, "..")
const read = (relativePath) => fs.readFileSync(path.join(frontendRoot, relativePath), "utf8")
const router = read("src/router.ts")
const shellDefinitions = read("src/modules/app-shell/shellDefinitions.ts")
const calendarSurface = read("src/modules/app-shell/components/SurfaceContentView.vue")
const shellSurfaceData = read("src/modules/app-shell/shellSurfaceData.ts")
const chatSurface = read("src/modules/app-shell/views/ChatSurfaceView.vue")
const circlesSurface = read("src/modules/app-shell/views/CirclesView.vue")

const requiredRoutes = [
  "path: 'work/find'",
  "path: 'work/quests/new'",
  "path: 'people'",
  "path: 'people/:userId'",
  "path: 'business/find'",
  "path: 'chat'",
  "path: 'circles'",
  "path: 'calendar'"
]
const requiredActions = ["Create new work", "Find work", "Find people", "Find a business"]
const requiredCalendarSignals = ["calendarMode", "month", "week", "day", "calendarTimezone", "Could not load calendar data", "Retry", "!loading && !error"]

const missing = (source, signals) => signals.filter((signal) => !source.includes(signal))
const missingRoutes = missing(router, requiredRoutes)
const missingActions = missing(shellDefinitions, requiredActions)
const missingCalendarSignals = missing(`${calendarSurface}\n${shellSurfaceData}`, requiredCalendarSignals)
const missingRecoverySignals = [
  ...missing(chatSurface, ["HTTP ${response.status}", "Retry"]),
  ...missing(circlesSurface, ["Promise.allSettled", "Retry"])
]

const createRouteIndex = router.indexOf("path: 'work/quests/new'")
const detailRouteIndex = router.indexOf("path: 'work/quests/:questId'")
const orderingError = createRouteIndex < 0 || detailRouteIndex < 0 || createRouteIndex > detailRouteIndex

if (missingRoutes.length || missingActions.length || missingCalendarSignals.length || missingRecoverySignals.length || orderingError) {
  console.error(JSON.stringify({missingRoutes, missingActions, missingCalendarSignals, missingRecoverySignals, orderingError}, null, 2))
  process.exit(1)
}

console.log("Web surface contract passed: canonical routes, visible actions, Calendar modes, and recovery signals are present.")
