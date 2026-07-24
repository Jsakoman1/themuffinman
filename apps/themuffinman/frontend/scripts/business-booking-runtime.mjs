import { chromium } from "playwright"
import fs from "node:fs"

const frontendUrl = "http://localhost:5173"
const backendUrl = "http://localhost:8080"
const evidencePath = new URL("../../../../docs/runtime-evidence/business-booking-browser-2026-07-24.json", import.meta.url).pathname
const browser = await chromium.launch({ headless: true })
const errors = []
const result = { capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: [], result: "passed" }

try {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, reducedMotion: "reduce" })
  page.on("pageerror", error => errors.push(error.message))
  const authResponse = await page.request.post(`${backendUrl}/auth/login`, { data: { email: "test@test.com", password: "test123" } })
  const auth = await authResponse.json()
  if (authResponse.status() !== 200 || !auth.token) throw new Error(`owner auth failed: ${authResponse.status()}`)
  const apiHeaders = { Authorization: `Bearer ${auth.token}` }
  const schemaResponse = await page.request.get(`${backendUrl}/business/public/runtime-flexible-services/offerings/112/schema`, { headers: apiHeaders })
  const schema = await schemaResponse.json()
  const quoteResponse = await page.request.post(`${backendUrl}/business/public/runtime-flexible-services/quote`, { headers: {...apiHeaders, "Content-Type": "application/json"}, data: { businessOfferingId: 112, durationMinutes: 30, quantity: 1, schemaVersion: schema.schemaVersion, answers: { vehicleClass: "SUV" }, selectedOptions: { wax: "true" } } })
  const availabilityResponse = await page.request.get(`${backendUrl}/business/public/runtime-flexible-services/availability?offeringId=112&from=2026-08-01T06:00:00Z&to=2026-08-01T10:00:00Z`, { headers: apiHeaders })
  result.scenarios.runtimeApiContract = { status: schemaResponse.status() === 200 && quoteResponse.status() === 200 && availabilityResponse.status() === 200 ? "passed" : "failed", schemaStatus: schemaResponse.status(), quoteStatus: quoteResponse.status(), availabilityStatus: availabilityResponse.status(), schemaVersion: schema.schemaVersion, demandFields: schema.demandFields?.length ?? 0, options: schema.options?.length ?? 0, slots: (await availabilityResponse.json()).items?.length ?? 0 }
  await page.goto(`${frontendUrl}/login`, { waitUntil: "networkidle", timeout: 30000 })
  await page.evaluate(user => { localStorage.setItem("user", JSON.stringify(user)); localStorage.setItem("token", user.token) }, auth)

  await page.goto(`${frontendUrl}/business/service-setup`, { waitUntil: "networkidle", timeout: 30000 })
  result.scenarios.ownerSetup = { status: new URL(page.url()).pathname === "/business/service-setup" ? "passed" : "failed", route: new URL(page.url()).pathname, visibleText: (await page.locator("body").innerText()).slice(0, 400) }

  await page.goto(`${frontendUrl}/business/calendar`, { waitUntil: "networkidle", timeout: 30000 })
  result.scenarios.ownerCalendar = { status: new URL(page.url()).pathname === "/business/calendar" ? "passed" : "failed", route: new URL(page.url()).pathname, visibleText: (await page.locator("body").innerText()).slice(0, 400) }

  await page.goto(`${frontendUrl}/business/public/runtime-flexible-services`, { waitUntil: "networkidle", timeout: 30000 })
  result.scenarios.publicBusiness = { status: new URL(page.url()).pathname === "/business/public/runtime-flexible-services" ? "passed" : "failed", route: new URL(page.url()).pathname, visibleText: (await page.locator("body").innerText()).slice(0, 400) }

  await page.goto(`${frontendUrl}/vision`, { waitUntil: "networkidle", timeout: 30000 })
  result.scenarios.visionSurface = { status: new URL(page.url()).pathname === "/vision" || new URL(page.url()).pathname === "/home" ? "passed" : "failed", route: new URL(page.url()).pathname, visionHost: await page.locator(".vision-web-host").count() }
  result.scenarios.browserErrors = { status: errors.length ? "failed" : "passed", errors }
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
  await page.close()
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = errors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
