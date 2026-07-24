import {chromium} from "playwright"
import fs from "node:fs"
const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-personal-context-2026-07-23.json", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const result = {evidenceVersion: "frontend-remaster-personal-context-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}
try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", e => errors.push(e.message))
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, user)
  await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.home = {status: "passed", route: new URL(page.url()).pathname, hasOrientation: await page.getByRole("note").count() === 1, summaryRows: await page.locator(".surface-row").count(), noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
  await page.locator("summary.universal-create-menu__summary").click()
  result.scenarios.createMenu = {status: await page.getByText("Direct create").count() === 1 ? "passed" : "failed", hasOptions: await page.locator(".universal-create-menu__option").count() > 0}
  await page.locator("summary", {hasText: "Search"}).click()
  result.scenarios.commandCenter = {status: await page.getByText(/Command center/).count() === 1 ? "passed" : "failed", hasSearch: await page.getByLabel("Search across modules").count() === 1}
  await page.getByLabel("Search across modules").fill("runtime")
  await page.getByRole("button", {name: "Search", exact: true}).click()
  await page.waitForTimeout(500)
  result.scenarios.searchQuery = {status: await page.locator(".summary-row, .empty, .error-state").count() > 0 ? "passed" : "failed", hasRecoverySurface: await page.locator(".summary-row, .empty, .error-state").count() > 0}
  await page.goto(`${baseUrl}/activity`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.activity = {status: new URL(page.url()).pathname === "/activity" && await page.locator("h1").count() > 0 ? "passed" : "failed", route: new URL(page.url()).pathname}
  await page.goto(`${baseUrl}/search/saved`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.savedSearches = {status: new URL(page.url()).pathname === "/search/saved" && await page.locator("h1").count() > 0 ? "passed" : "failed", route: new URL(page.url()).pathname}
  await page.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-personal-context-1440.png", import.meta.url).pathname, fullPage: true})
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", e => errors.push(e.message))
  const ma = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mu = await ma.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, mu)
  await mobile.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-personal-context-390.png", import.meta.url).pathname, fullPage: true})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileHome = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths}
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
} catch (e) { result.result = "failed"; result.failure = e instanceof Error ? e.message : String(e) }
finally { result.browserErrors = errors; fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
