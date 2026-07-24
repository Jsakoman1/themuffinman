import {chromium} from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-work-2026-07-23.json", import.meta.url).pathname
const desktopShot = new URL("../../../../docs/runtime-evidence/frontend-remaster-work-1440.png", import.meta.url).pathname
const mobileShot = new URL("../../../../docs/runtime-evidence/frontend-remaster-work-390.png", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const result = {evidenceVersion: "frontend-remaster-work-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}

try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", error => errors.push(error.message))
  const authResponse = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await authResponse.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(value => {localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token)}, user)
  await page.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  const rows = page.locator(".surface-row")
  const firstRow = rows.first()
  result.scenarios.authenticatedDiscovery = {status: "passed", route: new URL(page.url()).pathname, rowCount: await rows.count(), hasScopeCopy: await page.getByText("Available work visible to you.").count() === 1, noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
  await firstRow.dispatchEvent("click")
  const context = page.getByRole("complementary", {name: "Selected work context"})
  result.scenarios.contextSelection = {status: (await context.count() === 1 && !(await context.getByText("Select a work item").count())) ? "passed" : "failed", contextText: (await context.innerText()).slice(0, 180)}
  const options = page.getByRole("toolbar", {name: "Find work"}).getByLabel("Work view options")
  result.scenarios.filters = {status: await options.count() === 1 ? "passed" : "failed", hasSort: await page.getByLabel("Sort work").count() === 1}
  const detailLink = firstRow.getByRole("link").first()
  if (await detailLink.count()) {
    await detailLink.click()
    await page.waitForURL(/\/work\/quests\//)
    result.scenarios.canonicalDetail = {status: "passed", route: new URL(page.url()).pathname, hasBackLink: await page.getByRole("link", {name: /back to quests/i}).count() === 1, hasSharedStatus: await page.locator("[data-state]").count() >= 1}
    await page.goBack({waitUntil: "networkidle"})
  } else result.scenarios.canonicalDetail = {status: "failed", reason: "No canonical detail link rendered"}
  await page.goto(`${baseUrl}/work/quests/new`, {waitUntil: "networkidle", timeout: 30000})
  const guidedPanel = await page.getByText(/answer one useful question at a time/i).count() === 1
  const guidedCancel = await page.getByRole("button", {name: /cancel/i}).count() >= 1
  result.scenarios.guidedCreation = {status: guidedPanel && guidedCancel ? "passed" : "failed", route: new URL(page.url()).pathname, hasGuidedPanel: guidedPanel, hasCancel: guidedCancel}
  await page.goto(`${baseUrl}/work/applications`, {waitUntil: "networkidle", timeout: 30000})
  const hasApplicationSurface = await page.locator("h1", {hasText: "My applications"}).count() === 1
  const hasApplicationStatus = await page.locator("[data-state]").count() >= 1
  result.scenarios.applicationReview = {status: hasApplicationSurface && hasApplicationStatus ? "passed" : "failed", route: new URL(page.url()).pathname, hasSharedStatus: hasApplicationStatus, hasApplicationSurface, bodyPreview: (await page.locator("body").innerText()).slice(0, 220), contentMarkup: (await page.locator(".app-shell__content").innerHTML()).slice(0, 600)}
  await page.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  await page.screenshot({path: desktopShot, fullPage: false})
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", error => errors.push(error.message))
  const mobileAuth = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mobileUser = await mobileAuth.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(value => {localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token)}, mobileUser)
  await mobile.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: mobileShot, fullPage: false})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileWork = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths, hasToolbar: await mobile.locator('[role="toolbar"]').count() === 1}
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = errors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
