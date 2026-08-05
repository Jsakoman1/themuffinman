import {chromium} from "playwright"
import {
  attachBrowserErrorCollection,
  authenticateSeededUser,
  inspectOverflow,
  runtimeUrls,
  withBrowser,
  writeRuntimeEvidence,
} from "./runtime-harness.mjs"

const desktop = process.argv.includes("--viewport=desktop")
const prefix = process.env.WEB_VISUAL_EVIDENCE_PREFIX ?? `docs/runtime-evidence/services-public-calendar-${desktop ? "desktop" : "mobile"}`
const evidencePath = process.env.WEB_RUNTIME_EVIDENCE_PATH ?? `${prefix}-runtime.json`
const {baseUrl, apiUrl} = runtimeUrls()
const viewport = desktop ? {width: 1440, height: 1000} : {width: 390, height: 844}
const result = {capturedAt: new Date().toISOString(), viewport: desktop ? "desktop" : "mobile", scenarios: {}, browserErrors: [], calendarResponses: [], result: "passed"}

const forbiddenPublicFields = ["booking", "customer", "note", "resource", "reason", "capacity", "owner"]
const containsForbiddenField = value => {
  if (Array.isArray(value)) return value.some(containsForbiddenField)
  if (!value || typeof value !== "object") return false
  return Object.entries(value).some(([key, child]) => forbiddenPublicFields.some(field => key.toLowerCase().includes(field)) || containsForbiddenField(child))
}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    const page = await browser.newPage({viewport})
    attachBrowserErrorCollection(page, result, result.viewport)
    page.on("response", async response => {
      if (!response.url().includes("/availability/calendar")) return
      const payload = await response.json()
      result.calendarResponses.push({status: response.status(), forbiddenFieldsPresent: containsForbiddenField(payload), view: payload.view, dayCount: payload.days?.length ?? 0})
    })
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
    await authenticateSeededUser(page, apiUrl)
    await page.goto(`${baseUrl}/business/public/runtime-flexible-services?section=services`, {waitUntil: "networkidle"})
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
    const {overflowFree} = await inspectOverflow(page)
    const text = await page.locator("body").innerText()
    const views = ["Month", "Week", "Day"].every(label => text.includes(label))
    const privateDataAbsent = !/customer note|resource assignment|exception reason/i.test(text)
    const safeResponses = result.calendarResponses.length >= 4 && result.calendarResponses.every(item => item.status === 200 && !item.forbiddenFieldsPresent && ["MONTH", "WEEK", "DAY"].includes(item.view))
    result.scenarios.publicCalendar = {status: views && safeResponses && privateDataAbsent && overflowFree && !result.browserErrors.length ? "passed" : "failed", views, safeResponses, privateDataAbsent, overflowFree, text: text.slice(0, 1200)}
    if (result.scenarios.publicCalendar.status === "failed") result.result = "failed"
    await page.close()
  })
} catch (error) { result.result = "failed"; result.failure = error instanceof Error ? error.message : String(error) }
finally { writeRuntimeEvidence(evidencePath, result) }
if (result.result !== "passed") process.exitCode = 1
