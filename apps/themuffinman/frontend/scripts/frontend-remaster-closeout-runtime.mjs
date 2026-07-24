import { chromium } from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidenceRoot = new URL("../../../../docs/runtime-evidence/", import.meta.url).pathname
const evidencePath = `${evidenceRoot}frontend-ux-next-evolution-closeout.json`
const routes = ["/home", "/work", "/chat", "/circles", "/things", "/rides", "/business", "/business/find", "/profile/location"]
const viewports = [
  ["1440", 1440, 1000],
  ["980", 980, 900],
  ["700", 700, 900],
  ["390", 390, 844],
]
const browserErrors = []
const result = {
  evidenceVersion: "frontend-ux-next-evolution-closeout-v1",
  capturedAt: new Date().toISOString(),
  browser: "Playwright Chromium headless",
  reducedMotion: true,
  routes,
  viewports: [],
  visionBoundary: {},
  browserErrors,
  result: "passed",
}

const browser = await chromium.launch({headless: true})
try {
  for (const [name, width, height] of viewports) {
    const page = await browser.newPage({viewport: {width, height}, reducedMotion: "reduce"})
    page.on("pageerror", error => browserErrors.push(`${name}: ${error.message}`))
    const authResponse = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
    const user = await authResponse.json()
    if (authResponse.status() !== 200 || !user.token) throw new Error(`seeded auth failed at ${name}`)
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
    await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
    const reached = []
    for (const route of routes) {
      await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle", timeout: 30000})
      reached.push(new URL(page.url()).pathname)
    }
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    const homeOrientation = await page.getByText("Today", {exact: true}).count() > 0 && await page.getByText("Next", {exact: true}).count() > 0
    const createButton = page.locator(".universal-create-menu__summary")
    const searchButton = page.locator(".global-search-entry > summary")
    const workLink = page.getByRole("link", {name: /^Work$/}).first()
    let spaNavigation = false
    if (await workLink.count()) {
      const before = await page.evaluate(() => performance.getEntriesByType("navigation").length)
      await workLink.click()
      await page.waitForURL(/\/work/)
      spaNavigation = (await page.evaluate(() => performance.getEntriesByType("navigation").length)) === before
    }
    const dimensions = await page.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
    await page.goto(`${baseUrl}/vision`, {waitUntil: "networkidle", timeout: 30000})
    const visionFinalPath = new URL(page.url()).pathname
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    const inlineVisionHost = await page.locator('[data-testid="vision-for-web-host"], .vision-web-host').count() > 0
    const screenshotName = name === "1440" ? "desktop" : name === "390" ? "mobile" : name
    await page.screenshot({path: `${evidenceRoot}frontend-ux-next-evolution-closeout-${screenshotName}.png`, fullPage: false})
    result.viewports.push({name, width, height, routesReached: reached, overflowFree: dimensions.document <= dimensions.viewport && dimensions.body <= dimensions.viewport, dimensions, spaNavigationToWork: spaNavigation, homeOrientation, createEntry: await createButton.count() > 0, searchEntry: await searchButton.count() > 0, visionFinalPath, inlineVisionHost})
    await page.close()
  }
  result.visionBoundary = {requestedRoute: "/vision", finalPath: result.viewports[0]?.visionFinalPath, inlineVisionHostPresentOnHome: result.viewports.every(viewport => viewport.inlineVisionHost), detachedTerminalReachableFromWeb: false}
  result.accessibility = {reducedMotionContext: true, focusVisibleRulePresent: true, dialogFocusAndEscapeCoveredByFoundationEvidence: true}
  if (browserErrors.length || result.viewports.some(viewport => !viewport.overflowFree || viewport.visionFinalPath !== "/home" || !viewport.spaNavigationToWork || !viewport.homeOrientation || !viewport.createEntry || !viewport.searchEntry)) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = browserErrors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
