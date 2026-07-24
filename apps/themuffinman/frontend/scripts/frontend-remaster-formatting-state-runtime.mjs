import {chromium} from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidenceRoot = new URL("../../../../docs/runtime-evidence/", import.meta.url).pathname
const evidencePath = `${evidenceRoot}frontend-remaster-formatting-state-2026-07-24.json`
const routes = ["/work", "/things", "/chat", "/business", "/calendar"]
const browserErrors = []
const result = {evidenceVersion: "frontend-remaster-formatting-state-v1", capturedAt: new Date().toISOString(), routes: [], browserErrors, result: "passed"}
const browser = await chromium.launch({headless: true})

try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", error => browserErrors.push(error.message))
  const authResponse = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await authResponse.json()
  if (authResponse.status() !== 200 || !user.token) throw new Error("seeded auth failed")
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  for (const route of routes) {
    await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle", timeout: 30000})
    const text = await page.locator("body").innerText()
    result.routes.push({route, finalPath: new URL(page.url()).pathname, noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), noLegacyLoadingCopy: !text.includes("Loading.") && !text.includes("Loading…"), sharedStatusNodes: await page.locator("[data-state]").count()})
  }
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", error => browserErrors.push(`mobile: ${error.message}`))
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  await mobile.goto(`${baseUrl}/work`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: `${evidenceRoot}frontend-remaster-formatting-state-390.png`, fullPage: false})
  result.mobile = {noHorizontalOverflow: await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth), sharedStatusNodes: await mobile.locator("[data-state]").count()}
  await page.screenshot({path: `${evidenceRoot}frontend-remaster-formatting-state-1440.png`, fullPage: false})
  result.browserErrors = browserErrors
  if (browserErrors.length || result.routes.some(item => !item.noHorizontalOverflow || !item.noLegacyLoadingCopy) || !result.mobile.noHorizontalOverflow) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
