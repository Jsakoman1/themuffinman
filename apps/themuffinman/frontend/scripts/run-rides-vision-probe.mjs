import {chromium} from "playwright"

const baseURL = process.env.WEB_BASE_URL || "http://localhost:5173"
const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
const account = {email: `rides-vision-${suffix}@sidequest.test`, username: `ridesvision${suffix}`, password: "SmokeTest-password-123!"}
const browser = await chromium.launch({headless: true})
const context = await browser.newContext()
const page = await context.newPage()
try {
  const registration = await context.request.post("http://localhost:8080/auth/register", {data: account})
  if (!registration.ok()) throw new Error(`registration failed: ${registration.status()}`)
  await page.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await page.getByLabel("Email").fill(account.email); await page.getByLabel("Password").fill(account.password); await page.getByRole("button", {name: "Enter"}).click(); await page.waitForURL(/\/vision$/)
  await page.goto(`${baseURL}/vision`, {waitUntil: "networkidle"})
  const input = page.locator("textarea.vision-console__input")
  await input.fill(`offer a ride from Probe origin ${suffix} to Probe destination ${suffix} at 2099-07-20T10:00:00Z seats 1`)
  const responsePromise = page.waitForResponse(response => response.url().includes("/vision/conversations/turns") && response.request().method() === "POST")
  await input.press("Enter")
  const response = await responsePromise
  const review = await response.json()
  const confirmationPromise = page.waitForResponse(next => next.url().includes("/vision/conversations/turns") && next.request().method() === "POST")
  await page.getByRole("button", {name: "Confirm and create", exact: true}).click()
  const confirmation = await confirmationPromise
  console.log(JSON.stringify({review: {status: response.status(), body: review}, confirmation: {status: confirmation.status(), body: await confirmation.json()}, text: await page.locator("main").innerText()}, null, 2))
} finally { await context.close(); await browser.close() }
