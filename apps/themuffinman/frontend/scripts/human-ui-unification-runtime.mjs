import {chromium} from "playwright"
import {mkdirSync, writeFileSync} from "node:fs"
import {fileURLToPath} from "node:url"
import {dirname, resolve} from "node:path"

const mode = process.env.HUMAN_UI_VIEWPORT === "mobile" ? "mobile" : "desktop"
const viewport = mode === "mobile" ? {width: 390, height: 844} : {width: 1440, height: 1000}
const root = resolve(dirname(fileURLToPath(import.meta.url)), "../../../..");
const evidenceRoot = resolve(root, "docs/runtime-evidence")
const baseUrl = "http://localhost:5173"
const apiUrl = "http://localhost:8080"
const result = {evidenceVersion: "human-ui-unification-v1", capturedAt: new Date().toISOString(), mode, viewport, routes: [], browserErrors: [], result: "passed"}

mkdirSync(evidenceRoot, {recursive: true})

const browser = await chromium.launch({headless: true})
try {
  const page = await browser.newPage({viewport, reducedMotion: "reduce"})
  page.on("pageerror", error => result.browserErrors.push(error.message))
  const login = await page.request.post(`${apiUrl}/auth/login`, {data: {email: "test@test.com", password: "test123"}})
  const user = await login.json()
  if (login.status() !== 200 || !user.token) throw new Error("Seeded authentication failed")
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)

  const inspect = async (name, route) => {
    await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle"})
    await page.waitForTimeout(200)
    const dimensions = await page.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
    const screenshot = resolve(evidenceRoot, `human-ui-unification-${mode}-${name}.png`)
    await page.screenshot({path: screenshot, fullPage: false})
    result.routes.push({name, route, screenshot: `docs/runtime-evidence/${screenshot.split("/").pop()}`, overflowFree: dimensions.document <= dimensions.viewport && dimensions.body <= dimensions.viewport, dimensions})
  }
  const resolveVisibleLink = async (route, matcher, label) => {
    await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle"})
    const href = await page.locator("a[href]").evaluateAll((links, source) => links.map(link => link.getAttribute("href") || "").find(href => new RegExp(source).test(href)), matcher)
    if (!href) throw new Error(`No canonical visible ${label} destination was available from ${route}`)
    return href
  }

  await inspect("home", "/home")
  await inspect("work", "/work/find")
  await inspect("calendar", "/calendar")
  const thingsResponse = await page.request.get(`${apiUrl}/things/listings`, {headers: {Authorization: `Bearer ${user.token}`}})
  const things = await thingsResponse.json()
  const listingId = things?.items?.[0]?.id
  if (thingsResponse.status() !== 200 || !listingId) throw new Error("No authenticated seeded object detail was available")
  const detailRoute = `/things/${encodeURIComponent(String(listingId))}`
  await inspect("detail", detailRoute)
  const businessesResponse = await page.request.get(`${apiUrl}/business/profiles/me/all`, {headers: {Authorization: `Bearer ${user.token}`}})
  const businesses = await businessesResponse.json()
  const businessId = Array.isArray(businesses) ? businesses[0]?.id : null
  if (businessesResponse.status() !== 200 || !businessId) throw new Error("No authenticated seeded Business setup context was available")
  const setupRoute = `/business/service-setup?businessId=${encodeURIComponent(String(businessId))}`
  await inspect("setup", setupRoute)
  if (result.browserErrors.length || result.routes.some(route => !route.overflowFree)) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  writeFileSync(resolve(evidenceRoot, `human-ui-unification-${mode}-runtime.json`), `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}

if (result.result !== "passed") process.exitCode = 1
