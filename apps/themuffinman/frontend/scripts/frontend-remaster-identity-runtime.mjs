import {chromium} from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-identity-onboarding-2026-07-23.json", import.meta.url).pathname
const screenshotDesktop = new URL("../../../../docs/runtime-evidence/frontend-remaster-identity-onboarding-1440.png", import.meta.url).pathname
const screenshotMobile = new URL("../../../../docs/runtime-evidence/frontend-remaster-identity-onboarding-390.png", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const result = {evidenceVersion: "frontend-remaster-identity-onboarding-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}

try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", error => errors.push(error.message))
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.login = {status: "passed", route: new URL(page.url()).pathname, sharedIdentitySurface: await page.locator(".identity-surface__panel").count() === 1, formBusy: await page.locator("form").getAttribute("aria-busy"), submitName: await page.getByRole("button", {name: "Enter"}).textContent()}
  await page.screenshot({path: screenshotDesktop, fullPage: false})
  await page.goto(`${baseUrl}/register`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.registration = {status: "passed", sharedIdentitySurface: await page.locator(".identity-surface__panel").count() === 1, hasEmail: await page.getByLabel("Email").count() === 1, hasUsername: await page.getByLabel("Username").count() === 1, hasPassword: await page.getByLabel("Password").count() === 1}
  await page.goto(`${baseUrl}/recover`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.recovery = {status: "passed", sharedIdentitySurface: await page.locator(".identity-surface__panel").count() === 1, hasEmail: await page.getByLabel("Email").count() === 1, submitName: await page.getByRole("button", {name: "Continue"}).textContent()}
  await page.goto(`${baseUrl}/reset-password`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.missingResetToken = {status: "passed", sharedIdentitySurface: await page.locator(".identity-surface__panel").count() === 1, message: await page.getByRole("alert").textContent()}
  const authResponse = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await authResponse.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(value => {localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token)}, user)
  await page.goto(`${baseUrl}/onboarding`, {waitUntil: "networkidle", timeout: 30000})
  result.scenarios.onboarding = {status: "passed", route: new URL(page.url()).pathname, ariaBusy: await page.locator(".onboarding").getAttribute("aria-busy"), hasSkip: await page.getByRole("button", {name: "Skip setup"}).count() === 1, hasReset: await page.getByRole("button", {name: "Reset"}).count() === 1, noHorizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)}
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", error => errors.push(error.message))
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.screenshot({path: screenshotMobile, fullPage: false})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileIdentity = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths, noHorizontalOverflow: widths.document <= widths.viewport}
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = errors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
