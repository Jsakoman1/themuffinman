import {chromium} from "playwright"
import {expect} from "playwright/test"

const baseURL = process.env.WEB_BASE_URL || "http://localhost:5173"
const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
const account = {email: `business-smoke-${suffix}@sidequest.test`, username: `businesssmoke${suffix}`, password: "SmokeTest-password-123!"}
const browser = await chromium.launch({headless: true})
const context = await browser.newContext({recordHar: {path: "./web-runtime-smoke-multi-business.har", mode: "minimal"}})
const page = await context.newPage()
const main = page.locator("main")

try {
  await page.goto(`${baseURL}/register`, {waitUntil: "networkidle"})
  await page.getByLabel("Email").fill(account.email)
  await page.getByLabel("Username").fill(account.username)
  await page.getByLabel("Password").fill(account.password)
  await page.getByRole("button", {name: "Create account"}).click()
  await page.waitForURL(/\/vision$/)
  await page.goto(`${baseURL}/business/profile`, {waitUntil: "networkidle"})
  await page.getByLabel("Business name").fill(`Primary Business ${suffix}`)
  await page.getByLabel("Timezone").fill("Europe/Zurich")
  await page.getByRole("button", {name: "Save profile", exact: true}).click()
  await expect(page.getByText("Profile updated.", {exact: true})).toBeVisible()

  page.once("dialog", dialog => dialog.accept(`Secondary Business ${suffix}`))
  await page.getByRole("button", {name: "Create business", exact: true}).click()
  await expect(page.getByText("Business created.", {exact: true})).toBeVisible()
  const selector = page.locator(".business-profile__switcher select")
  await expect(selector.locator("option")).toHaveCount(2)
  const labels = await selector.locator("option").allTextContents()
  const primary = labels.find(label => label.startsWith("Primary Business"))
  const secondary = labels.find(label => label.startsWith("Secondary Business"))
  if (!primary || !secondary) throw new Error(`Expected two owned business labels, got ${JSON.stringify(labels)}`)
  await selector.selectOption({label: secondary})
  await page.getByRole("button", {name: "Switch business", exact: true}).click()
  await expect(page.getByText("Business selected.", {exact: true})).toBeVisible()
  await expect(page.getByLabel("Business name")).toHaveValue(secondary)
  await selector.selectOption({label: primary})
  await page.getByRole("button", {name: "Switch business", exact: true}).click()
  await expect(page.getByText("Business selected.", {exact: true})).toBeVisible()
  await expect(page.getByLabel("Business name")).toHaveValue(primary)
  console.log(JSON.stringify({result: "passed", account: account.username, businesses: [primary, secondary]}, null, 2))
} finally {
  await context.close()
  await browser.close()
}
