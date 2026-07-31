import {chromium} from "playwright"
import fs from "node:fs"

const desktop = process.argv.includes("--viewport=desktop")
// The same scenario is intentionally captured at the narrow mobile viewport for this audit.
const prefix = process.env.WEB_VISUAL_EVIDENCE_PREFIX ?? "docs/runtime-evidence/services-human-ux-desktop"
const evidencePath = process.env.WEB_RUNTIME_EVIDENCE_PATH ?? `${prefix}-runtime.json`
const browser = await chromium.launch({headless: true})
const result = {capturedAt: new Date().toISOString(), viewport: desktop ? "desktop" : "mobile", scenarios: {}, browserErrors: [], result: "passed"}
try {
  const page = await browser.newPage({viewport: desktop ? {width: 1440, height: 1000} : {width: 390, height: 844}})
  page.on("pageerror", error => result.browserErrors.push(error.message))
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  if (!user.token) throw new Error("runtime user authentication failed")
  await page.goto("http://localhost:5173/login", {waitUntil: "networkidle"})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  await page.goto("http://localhost:5173/business/public/runtime-flexible-services?section=services", {waitUntil: "networkidle"})
  await page.screenshot({path: `${prefix}-light.png`, fullPage: true})
  await page.emulateMedia({colorScheme: "dark"})
  await page.screenshot({path: `${prefix}-dark.png`, fullPage: true})
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
  const text = await page.locator("body").innerText()
  result.scenarios.publicServices = {status: text.includes("Services") && text.includes("Runtime Flexible Services") && !overflow && !result.browserErrors.length ? "passed" : "failed", overflow, text: text.slice(0, 800)}
  if (result.scenarios.publicServices.status === "failed") result.result = "failed"
  await page.close()
} catch (error) { result.result = "failed"; result.failure = error instanceof Error ? error.message : String(error) }
finally { fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
