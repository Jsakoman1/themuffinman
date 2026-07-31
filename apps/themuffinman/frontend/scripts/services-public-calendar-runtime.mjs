import {chromium} from "playwright"
import fs from "node:fs"

const desktop = process.argv.includes("--viewport=desktop")
const prefix = process.env.WEB_VISUAL_EVIDENCE_PREFIX ?? `docs/runtime-evidence/services-public-calendar-${desktop ? "desktop" : "mobile"}`
const evidencePath = process.env.WEB_RUNTIME_EVIDENCE_PATH ?? `${prefix}-runtime.json`
const browser = await chromium.launch({headless: true})
const result = {capturedAt: new Date().toISOString(), viewport: desktop ? "desktop" : "mobile", scenarios: {}, browserErrors: [], calendarResponses: [], result: "passed"}

const forbiddenPublicFields = ["booking", "customer", "note", "resource", "reason", "capacity", "owner"]
const containsForbiddenField = value => {
  if (Array.isArray(value)) return value.some(containsForbiddenField)
  if (!value || typeof value !== "object") return false
  return Object.entries(value).some(([key, child]) => forbiddenPublicFields.some(field => key.toLowerCase().includes(field)) || containsForbiddenField(child))
}

try {
  const page = await browser.newPage({viewport: desktop ? {width: 1440, height: 1000} : {width: 390, height: 844}})
  page.on("pageerror", error => result.browserErrors.push(error.message))
  page.on("response", async response => {
    if (!response.url().includes("/availability/calendar")) return
    const payload = await response.json()
    result.calendarResponses.push({status: response.status(), forbiddenFieldsPresent: containsForbiddenField(payload), view: payload.view, dayCount: payload.days?.length ?? 0})
  })
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  if (!user.token) throw new Error("runtime user authentication failed")
  await page.goto("http://localhost:5173/login", {waitUntil: "networkidle"})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  await page.goto("http://localhost:5173/business/public/runtime-flexible-services?section=services", {waitUntil: "networkidle"})
  await page.locator("[data-service-row] button").first().click()
  const requiredDemandFields = page.locator(".demand-field input[required]")
  for (let index = 0; index < await requiredDemandFields.count(); index += 1) await requiredDemandFields.nth(index).fill("Runtime answer")
  // The calendar read is the scenario under test; schema-specific fixture validation is covered by backend tests.
  await page.locator("[data-booking-model] form").evaluate(form => { form.noValidate = true })
  await page.getByRole("button", {name: "Continue"}).click()
  await page.getByRole("button", {name: "Week"}).click()
  await page.getByRole("button", {name: "Day"}).click()
  await page.getByRole("button", {name: "Month"}).click()
  await page.screenshot({path: `${prefix}.png`, fullPage: true})
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
  const text = await page.locator("body").innerText()
  const views = ["Month", "Week", "Day"].every(label => text.includes(label))
  const privateDataAbsent = !/customer note|resource assignment|exception reason/i.test(text)
  const safeResponses = result.calendarResponses.length >= 4 && result.calendarResponses.every(item => item.status === 200 && !item.forbiddenFieldsPresent && ["MONTH", "WEEK", "DAY"].includes(item.view))
  result.scenarios.publicCalendar = {status: views && safeResponses && privateDataAbsent && !overflow && !result.browserErrors.length ? "passed" : "failed", views, safeResponses, privateDataAbsent, overflow, text: text.slice(0, 1200)}
  if (result.scenarios.publicCalendar.status === "failed") result.result = "failed"
  await page.close()
} catch (error) { result.result = "failed"; result.failure = error instanceof Error ? error.message : String(error) }
finally { fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
