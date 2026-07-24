import {chromium} from "playwright"
import fs from "node:fs"
const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-profile-location-2026-07-23.json", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const result = {evidenceVersion: "frontend-remaster-profile-location-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}
try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", e => errors.push(e.message))
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, user)
  await page.goto(`${baseUrl}/profile/settings`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.profileSettings = {status: new URL(page.url()).pathname === "/profile/settings" && await page.getByRole("form", {name: "Profile and location settings"}).count() === 1 && await page.locator(".settings-section-nav a").count() === 4 ? "passed" : "failed", route: new URL(page.url()).pathname, form: await page.getByRole("form", {name: "Profile and location settings"}).count() === 1, sectionLinks: await page.locator(".settings-section-nav a").count(), notificationLink: await page.getByRole("link", {name: "Notification preferences"}).count() === 1, noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
  const location = page.getByLabel("Location mode")
  await location.selectOption("EXACT")
  result.scenarios.visibilityDisclosure = {status: await page.getByLabel("Exact address visibility").count() === 1 ? "passed" : "failed", exactScope: await page.getByLabel("Exact address visibility").inputValue()}
  await page.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-profile-location-1440.png", import.meta.url).pathname})
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", e => errors.push(e.message))
  const ma = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mu = await ma.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, mu)
  await mobile.goto(`${baseUrl}/profile/settings`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-profile-location-390.png", import.meta.url).pathname})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileSettings = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
} catch (e) { result.result = "failed"; result.failure = e instanceof Error ? e.message : String(e) }
finally { result.browserErrors = errors; fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
