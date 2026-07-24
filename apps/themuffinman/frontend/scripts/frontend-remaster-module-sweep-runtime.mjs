import {chromium} from "playwright"
import fs from "node:fs"
const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-module-sweep-2026-07-23.json", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const apiResponses = []
const result = {evidenceVersion: "frontend-remaster-module-sweep-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}
try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", e => errors.push(e.message))
  page.on("response", response => { if (response.url().includes("/things/")) apiResponses.push({url: response.url(), status: response.status()}) })
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, user)
  await page.reload({waitUntil: "networkidle", timeout: 30000})
  for (const path of ["/circles", "/things", "/rides", "/business"]) {
    await page.goto(`${baseUrl}${path}`, {waitUntil: "networkidle", timeout: 30000})
    result.scenarios[`route:${path}`] = {status: "passed", route: new URL(page.url()).pathname, heading: await page.locator("h1").first().textContent().catch(() => null), noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
  }
  await page.goto(`${baseUrl}/things`, {waitUntil: "networkidle", timeout: 30000})
  const listButton = page.getByRole("button", {name: "List a thing"})
  await listButton.click()
  const thingDialog = page.getByRole("dialog", {name: "List a thing"})
  const hasGuidedThing = await thingDialog.count() === 1
  await thingDialog.getByRole("button", {name: "Cancel"}).click()
  result.scenarios.thingsDialog = {status: hasGuidedThing && await page.locator('[data-surface="listing-request-context"]').count() === 1 ? "passed" : "failed", hasGuidedThing}
  await page.goto(`${baseUrl}/circles`, {waitUntil: "networkidle", timeout: 30000})
  const circleCreate = page.getByRole("button", {name: "New circle"})
  await circleCreate.click()
  const circleDialog = page.getByRole("dialog", {name: "Create a circle"})
  const hasGuidedCircle = await circleDialog.count() === 1
  await circleDialog.getByRole("button", {name: "Cancel"}).click()
  result.scenarios.circleDialog = {status: hasGuidedCircle && await page.locator('[data-surface="trust-boundary-context"]').count() === 1 ? "passed" : "failed", hasGuidedCircle}
  result.scenarios.thingsApiResponses = apiResponses
  await page.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-module-sweep-1440.png", import.meta.url).pathname, fullPage: true})
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", e => errors.push(e.message))
  const ma = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mu = await ma.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, mu)
  await mobile.reload({waitUntil: "networkidle", timeout: 30000})
  await mobile.goto(`${baseUrl}/things`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-module-sweep-390.png", import.meta.url).pathname, fullPage: true})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileThings = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths}
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
} catch (e) { result.result = "failed"; result.failure = e instanceof Error ? e.message : String(e) }
finally { result.browserErrors = errors; fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
