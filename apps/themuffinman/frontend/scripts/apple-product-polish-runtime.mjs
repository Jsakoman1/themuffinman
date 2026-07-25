import {chromium} from "playwright"
import fs from "node:fs"
// Runtime gate reviewed 2026-07-24: the same harness owns Work, Calendar, and native-laptop traces.

const baseUrl = process.env.WEB_BASE_URL || "http://localhost:5173"
const evidencePaths = {
  calendar: new URL("../../../../docs/runtime-evidence/apple-product-polish-calendar.json", import.meta.url).pathname,
  "native-laptop": new URL("../../../../docs/runtime-evidence/apple-product-polish-native-laptop.json", import.meta.url).pathname,
  closeout: new URL("../../../../docs/runtime-evidence/apple-product-polish-closeout.json", import.meta.url).pathname,
  "work-scope": new URL("../../../../docs/runtime-evidence/apple-product-polish-work-scope.json", import.meta.url).pathname,
}
const browser = await chromium.launch({headless: true})
const result = {evidenceVersion: "apple-product-polish-runtime-v2", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: [], result: "passed"}
const scenario = process.argv.includes("--scenario=calendar") ? "calendar" : process.argv.includes("--scenario=native-laptop") ? "native-laptop" : process.argv.includes("--scenario=closeout") ? "closeout" : "work-scope"
const evidencePath = process.env.WEB_RUNTIME_EVIDENCE_PATH || evidencePaths[scenario]
try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, colorScheme: "light", reducedMotion: "reduce"})
  page.on("pageerror", error => result.browserErrors.push(error.message))
  const authResponse = await page.request.post("http://127.0.0.1:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  if (!authResponse.ok()) throw new Error(`Login failed with ${authResponse.status()}`)
  const user = await authResponse.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(value => {localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token)}, user)
  await page.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  if (scenario === "calendar") {
    const calendarEvidence = process.env.WEB_RUNTIME_EVIDENCE_PATH || new URL("../../../../docs/runtime-evidence/apple-product-polish-calendar.json", import.meta.url).pathname
    await page.goto(`${baseUrl}/calendar`, {waitUntil: "networkidle", timeout: 30000})
    const views = ["day", "week", "month"]
    for (const view of views) { await page.getByRole("button", {name: view, exact: true}).click(); await page.waitForTimeout(250); await page.screenshot({path: new URL(`../../../../docs/runtime-evidence/apple-product-polish-calendar-${view}.png`, import.meta.url).pathname, fullPage: false}) }
    const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
    await mobile.goto(`${baseUrl}/calendar`, {waitUntil: "networkidle", timeout: 30000}); await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/apple-product-polish-calendar-mobile.png", import.meta.url).pathname, fullPage: false})
    result.scenarios.calendarViews = {status: true, views, mobileNoOverflow: await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
    fs.mkdirSync(new URL("../../../../docs/runtime-evidence/", import.meta.url), {recursive: true}); fs.writeFileSync(calendarEvidence, `${JSON.stringify(result, null, 2)}\n`)
  } else if (scenario === "native-laptop") {
    const nativeEvidence = process.env.WEB_RUNTIME_EVIDENCE_PATH || new URL("../../../../docs/runtime-evidence/apple-product-polish-native-laptop.json", import.meta.url).pathname
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    const frame = await page.locator("[data-native-frame]").count()
    await page.keyboard.press("Control+K")
    const commandOpen = await page.locator(".global-search-entry").count() > 0
    await page.keyboard.press("Escape")
    await page.waitForTimeout(100)
    const focusRestored = await page.evaluate(() => document.activeElement?.tagName === "SUMMARY" || document.activeElement?.tagName === "BUTTON")
    await page.screenshot({path: new URL("../../../../docs/runtime-evidence/apple-product-polish-native-laptop.png", import.meta.url).pathname, fullPage: false})
    const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
    await mobile.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    const mobileNoOverflow = await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth)
    result.scenarios.nativeLaptop = {status: frame > 0 && commandOpen && focusRestored && mobileNoOverflow, frame, commandOpen, focusRestored, mobileNoOverflow}
    fs.mkdirSync(new URL("../../../../docs/runtime-evidence/", import.meta.url), {recursive: true}); fs.writeFileSync(nativeEvidence, `${JSON.stringify(result, null, 2)}\n`)
  } else if (scenario === "closeout") {
    const closeoutEvidence = new URL("../../../../docs/runtime-evidence/apple-product-polish-closeout.json", import.meta.url).pathname
    const desktopRoutes = ["/home", "/work/find", "/calendar", "/business"]
    const routeResults = []
    for (const route of desktopRoutes) { await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle", timeout: 30000}); routeResults.push({route, pathname: new URL(page.url()).pathname, frame: await page.locator("[data-native-frame]").count()}) }
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    await page.screenshot({path: new URL("../../../../docs/runtime-evidence/apple-product-polish-closeout-desktop.png", import.meta.url).pathname, fullPage: false})
    const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
    await mobile.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
    await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/apple-product-polish-closeout-mobile.png", import.meta.url).pathname, fullPage: false})
    result.scenarios.closeout = {status: routeResults.every(item => item.pathname === item.route) && await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth), routeResults, mobileNoOverflow: await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth)}
    fs.mkdirSync(new URL("../../../../docs/runtime-evidence/", import.meta.url), {recursive: true}); fs.writeFileSync(closeoutEvidence, `${JSON.stringify(result, null, 2)}\n`)
  } else {
  const routes = ["/work/find", "/work/quests", "/work/applications", "/work/quests", "/work/find"]
  const observations = []
  await page.goto(`${baseUrl}${routes[0]}`, {waitUntil: "networkidle", timeout: 30000})
  const tab = label => page.locator('[data-work-navigation="canonical-find-mine-applications"] .module-tabs__tab').filter({hasText: label})
  const record = route => observations.push({route, pathname: new URL(page.url()).pathname, scope: page.locator("[data-work-scope]").getAttribute("data-work-scope"), firstResult: page.locator(".surface-row__title-line strong").first().textContent(), body: page.locator("body").innerText().then(text => text.slice(0, 180))})
  await record(routes[0])
  for (const route of routes.slice(1)) {
    const label = route === "/work/quests" ? "My work" : route === "/work/applications" ? "My applications" : "Find work"
    await tab(label).click()
    await page.waitForLoadState("networkidle", {timeout: 30000}).catch(() => undefined)
    await page.waitForTimeout(300)
    await record(route)
  }
  const expectedPaths = routes
  const expectedScopes = ["discover", "mine", "applications", "mine", "discover"]
  const resolved = await Promise.all(observations.map(async observation => ({...observation, scope: await observation.scope, firstResult: await observation.firstResult, body: await observation.body})))
  result.scenarios.routeOrder = {status: resolved.every((item, index) => item.pathname === expectedPaths[index] && item.scope === expectedScopes[index]), observations: resolved}
  await page.goto(`${baseUrl}/work/find`, {waitUntil: "networkidle", timeout: 30000})
  await page.reload({waitUntil: "networkidle"})
  result.scenarios.refresh = {status: await page.locator("[data-work-scope]").getAttribute("data-work-scope") === "discover", scope: await page.locator("[data-work-scope]").getAttribute("data-work-scope")}
  await page.goBack({waitUntil: "networkidle"}).catch(() => undefined)
  result.scenarios.browserErrors = {status: result.browserErrors.length === 0, errors: result.browserErrors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
  }
  result.scenarios.browserErrors = {status: result.browserErrors.length === 0, errors: result.browserErrors}
  if (Object.values(result.scenarios).some(entry => entry?.status === false)) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  fs.mkdirSync(new URL("../../../../docs/runtime-evidence/", import.meta.url), {recursive: true})
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
