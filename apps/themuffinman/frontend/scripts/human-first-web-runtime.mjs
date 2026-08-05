import {chromium} from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidence = new URL("../../../../docs/runtime-evidence/", import.meta.url).pathname
const evidencePrefix = process.env.HUMAN_FIRST_EVIDENCE_PREFIX ?? "human-first-web"
const focusedScreenshotName = process.env.HUMAN_FIRST_SCREENSHOT_NAME
const focusedRouteName = process.env.HUMAN_FIRST_SCREENSHOT_ROUTE_NAME ?? "home"
const mode = process.env.HUMAN_FIRST_MODE ?? "desktop"
const viewport = mode === "mobile" ? {width: 390, height: 844} : {width: 1440, height: 1000}
const desktop = [
  ["home", "/home"], ["navigation", "/work/find"], ["notifications", "/notifications"],
  ["discovery", "/things"], ["chat", "/chat"], ["calendar", "/calendar"], ["business-setup", "/business/offerings"]
]
const mobile = [["home", "/home"], ["navigation", "/work/find"], ["notifications", "/notifications"], ["detail", "/things"], ["chat", "/chat"], ["calendar", "/calendar"], ["business-setup", "/business/offerings"]]
const routes = mode === "mobile" ? mobile : desktop
const result = {evidenceVersion: "human-first-web-runtime-v1", capturedAt: new Date().toISOString(), mode, viewport, routes: [], browserErrors: [], result: "passed"}
const browser = await chromium.launch({headless: true})
try {
  const page = await browser.newPage({viewport, reducedMotion: "reduce"})
  page.on("pageerror", error => result.browserErrors.push(error.message))
  const login = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await login.json()
  if (login.status() !== 200 || !user.token) throw new Error("Seeded authentication failed")
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  for (const [name, route] of routes) {
    await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle"})
    const dimensions = await page.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
    result.routes.push({name, route, overflowFree: dimensions.document <= dimensions.viewport && dimensions.body <= dimensions.viewport, dimensions})
    await page.screenshot({path: `${evidence}${focusedScreenshotName && name === focusedRouteName ? focusedScreenshotName : `${evidencePrefix}-${mode}-${name}`}.png`, fullPage: false})
  }
  await page.close()
  if (result.browserErrors.length || result.routes.some(route => !route.overflowFree)) result.result = "failed"
} catch (error) { result.result = "failed"; result.failure = error instanceof Error ? error.message : String(error) }
finally { fs.writeFileSync(`${evidence}${evidencePrefix}-${mode}-runtime.json`, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
