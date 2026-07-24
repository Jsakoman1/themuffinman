import { chromium } from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-foundation-2026-07-23.json", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const browserErrors = []
const result = {evidenceVersion: "frontend-remaster-foundation-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors, result: "passed"}

try {
  const desktop = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  desktop.on("pageerror", error => browserErrors.push(error.message))
  await desktop.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  const authResponse = await desktop.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const auth = {status: authResponse.status(), user: await authResponse.json()}
  if (auth.status !== 200 || !auth.user.token) throw new Error(`seeded auth failed: ${auth.status}`)
  await desktop.evaluate(user => { localStorage.setItem("user", JSON.stringify(user)); localStorage.setItem("token", user.token) }, auth.user)
  await desktop.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
  if (!desktop.url().endsWith("/home")) throw new Error(`authenticated shell did not reach home: ${desktop.url()}`)
  await desktop.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-foundation-1440.png", import.meta.url).pathname, fullPage: true})
  result.scenarios.authenticatedHome = {status: "passed", route: new URL(desktop.url()).pathname, noHorizontalOverflow: await desktop.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), reducedMotion: await desktop.evaluate(() => getComputedStyle(document.documentElement).scrollBehavior === "auto" || matchMedia("(prefers-reduced-motion: reduce)").matches)}
  await desktop.goto(`${baseUrl}/vision`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.visionWebBoundary = {status: new URL(desktop.url()).pathname === "/home" && await desktop.locator(".vision-web-host").count() === 1 && await desktop.locator(".vision-surface").count() === 0 ? "passed" : "failed", route: new URL(desktop.url()).pathname, inlineHost: await desktop.locator(".vision-web-host").count(), detachedTerminalSurface: await desktop.locator(".vision-surface").count()}
  await desktop.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  const toolbar = desktop.locator('[role="toolbar"]')
  result.scenarios.collectionToolbar = {status: await toolbar.count() ? "passed" : "failed", ariaBusy: await toolbar.getAttribute("aria-busy"), label: await toolbar.getAttribute("aria-label"), noHorizontalOverflow: await desktop.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)}
  await desktop.goto(`${baseUrl}/circles`, {waitUntil: "networkidle", timeout: 30000})
  const createCircle = desktop.getByRole("button", {name: /create circle/i}).first()
  if (await createCircle.count()) {
    await createCircle.click()
    const dialog = desktop.getByRole("dialog")
    const focusedInside = await dialog.evaluate(el => el.contains(document.activeElement))
    await desktop.keyboard.press("Escape")
    result.scenarios.dialogFocusAndEscape = {status: !await dialog.isVisible() && focusedInside ? "passed" : "failed", focusedInside, restoredFocus: await desktop.evaluate(() => document.activeElement?.textContent?.trim() || document.activeElement?.getAttribute("aria-label"))}
  } else result.scenarios.dialogFocusAndEscape = {status: "not_observed", reason: "No seeded create-circle action available"}

  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", error => browserErrors.push(error.message))
  const mobileAuthResponse = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mobileUser = await mobileAuthResponse.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(user => { localStorage.setItem("user", JSON.stringify(user)); localStorage.setItem("token", user.token) }, mobileUser)
  await mobile.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-foundation-390.png", import.meta.url).pathname, fullPage: true})
  const widths = await mobile.evaluate(() => ({viewport: window.innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileResponsive = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths, toolbarLayout: await mobile.locator('[role="toolbar"]').evaluate(el => getComputedStyle(el).display).catch(() => "not_observed")}
  result.scenarios.browserErrors = {status: browserErrors.length ? "failed" : "passed", errors: browserErrors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
  await desktop.close()
  await mobile.close()
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = browserErrors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
