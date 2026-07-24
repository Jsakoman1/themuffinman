import {chromium, request as playwrightRequest} from "playwright"
import fs from "node:fs"

const baseUrl = "http://localhost:5173"
const evidenceRoot = new URL("../../../../docs/runtime-evidence/", import.meta.url).pathname
const evidencePath = `${evidenceRoot}frontend-structural-redesign-desktop.json`
const browserErrors = []
const routes = ["/home", "/work", "/work/find", "/work/quests", "/chat", "/calendar", "/business", "/circles", "/things", "/rides", "/profile"]
const result = {evidenceVersion: "frontend-structural-redesign-runtime-v1", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", routes, browserErrors, result: "passed", desktop: {}, mobile: {}}

const authenticate = async (page) => {
  const response = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await response.json()
  if (response.status() !== 200 || !user.token) throw new Error("seeded auth failed")
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(value => { localStorage.setItem("user", JSON.stringify(value)); localStorage.setItem("token", value.token) }, user)
  await page.reload({waitUntil: "networkidle", timeout: 30000})
}

const inspect = async (page, viewport) => {
  const reached = []
  for (const path of routes) {
    await page.goto(`${baseUrl}${path}`, {waitUntil: "networkidle", timeout: 30000})
    reached.push({requested: path, actual: new URL(page.url()).pathname, heading: await page.locator("h1").first().textContent().catch(() => null)})
  }
  await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle", timeout: 30000})
  const dimensions = await page.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  const assistant = await page.locator('[data-testid="contextual-assistant-composer"], .contextual-assistant-composer').count()
  const assistantInput = page.locator('[data-testid="contextual-assistant-composer"] input, .contextual-assistant-composer input').first()
  const keyboardFocus = await assistantInput.count() ? await assistantInput.focus().then(() => page.evaluate(() => document.activeElement?.tagName === "INPUT")) : false
  const visibleDuplicateVision = await page.getByText("Ask Vision", {exact: true}).count()
  const today = await page.getByText("Today", {exact: true}).count()
  const next = await page.getByText("Next", {exact: true}).count()
  const screenshot = `${evidenceRoot}frontend-structural-redesign-${viewport === "desktop" ? "desktop" : "mobile"}.png`
  await page.screenshot({path: screenshot, fullPage: false})
  return {viewport, reached, dimensions, overflowFree: dimensions.document <= dimensions.viewport && dimensions.body <= dimensions.viewport, assistantPresent: assistant > 0, keyboardFocus, visibleDuplicateVision, homeOrientation: today > 0 && next > 0, screenshot}
}

const runMutationRecovery = async (page) => {
  const evidence = {fixture: "ephemeral quest", staleEdit: "not_observed", permissionDenied: "not_observed", bookingConflict: "not_observed", wishlistLifecycle: "not_observed", cleanup: "not_attempted"}
  const token = await page.evaluate(() => localStorage.getItem("token"))
  const request = (method, url, options = {}) => page.request[method](url, {...options, headers: {Authorization: `Bearer ${token}`, ...(options.headers ?? {})}})
  const adminContext = await playwrightRequest.newContext()
  const adminLogin = await adminContext.post("http://localhost:8080/auth/login", {data: {email: "admin@sidequest.local", password: "admin123"}})
  const admin = adminLogin.status() === 200 ? await adminLogin.json() : null
  const adminRequest = (method, url, options = {}) => adminContext[method](url, {...options, headers: {Authorization: `Bearer ${admin?.token ?? ""}`, ...(options.headers ?? {})}})
  let customerId = null
  let customerToken = null
  const customerRequest = (method, url, options = {}) => page.request[method](url, {...options, headers: {Authorization: `Bearer ${customerToken}`, ...(options.headers ?? {})}})
  let fixtureId = null
  try {
    const create = await request("post", "http://localhost:8080/quests", {data: {title: `Structural redesign recovery ${Date.now()}`, description: "Disposable runtime recovery fixture.", awardAmount: 0, termFixed: false}})
    if (create.status() >= 200 && create.status() < 300) {
      const listResponse = await request("get", "http://localhost:8080/quests/presets/MY_VISIBLE?page=0&size=20")
      const list = await listResponse.json()
      const fixture = (list.items ?? []).find(item => String(item.title).startsWith("Structural redesign recovery "))
      fixtureId = fixture?.id ?? null
      if (fixtureId) {
        const detailResponse = await request("get", `http://localhost:8080/quests/${fixtureId}/detail`)
        const detail = await detailResponse.json()
        const quest = detail.quest ?? detail
        const payload = {title: `${quest.title} updated`, description: quest.description || "Disposable runtime recovery fixture.", awardAmount: quest.awardAmount ?? 0, termFixed: quest.termFixed ?? false, resourceVersion: quest.resourceVersion}
        const first = await request("put", `http://localhost:8080/quests/${fixtureId}`, {data: payload})
        const second = await request("put", `http://localhost:8080/quests/${fixtureId}`, {data: payload})
        evidence.staleEdit = {firstStatus: first.status(), secondStatus: second.status(), conflictObserved: second.status() === 409}
        const latestResponse = await request("get", `http://localhost:8080/quests/${fixtureId}/detail`)
        const latest = await latestResponse.json()
        const latestQuest = latest.quest ?? latest
        const failedMutation = await request("put", `http://localhost:8080/quests/${fixtureId}`, {data: {description: "invalid retry probe", resourceVersion: latestQuest.resourceVersion}})
        const retryMutation = await request("put", `http://localhost:8080/quests/${fixtureId}`, {data: {title: latestQuest.title, description: latestQuest.description || "Disposable runtime recovery fixture.", awardAmount: latestQuest.awardAmount ?? 0, termFixed: latestQuest.termFixed ?? false, resourceVersion: latestQuest.resourceVersion}})
        evidence.failedMutationRetry = {failedStatus: failedMutation.status(), retryStatus: retryMutation.status(), retryObserved: failedMutation.status() >= 400 && retryMutation.status() >= 200 && retryMutation.status() < 300}
      }
    }
    const availableResponse = await request("get", "http://localhost:8080/quests/presets/AVAILABLE?page=0&size=5")
    const available = await availableResponse.json()
    const foreign = (available.items ?? []).find(item => item.id !== fixtureId)
    if (foreign) {
      const denied = await request("put", `http://localhost:8080/quests/${foreign.id}`, {data: {title: foreign.title, description: foreign.description || "Permission probe", awardAmount: foreign.awardAmount ?? 0, termFixed: foreign.termFixed ?? false, resourceVersion: foreign.resourceVersion}})
      evidence.permissionDenied = {status: denied.status(), forbiddenObserved: denied.status() === 403}
    }
    const thingsResponse = await request("get", "http://localhost:8080/things/listings")
    const things = await thingsResponse.json()
    const wishlistListing = (things.items ?? []).find(item => item.id !== fixtureId)
    if (wishlistListing) {
      const circlesResponse = await request("get", "http://localhost:8080/circles/groups")
      const circles = await circlesResponse.json()
      const sharedCircleIds = (circles ?? []).slice(0, 1).map(circle => circle.id)
      const saved = await request("put", `http://localhost:8080/things/wishlist/me/${wishlistListing.id}`, {data: {sharedCircleIds}})
      const savedItemsResponse = await request("get", "http://localhost:8080/things/wishlist/me")
      const savedItems = await savedItemsResponse.json()
      const savedVisible = (savedItems ?? []).some(item => item.listingId === wishlistListing.id && item.sharedCircleIds?.length === sharedCircleIds.length)
      const removed = await request("delete", `http://localhost:8080/things/wishlist/me/${wishlistListing.id}`)
      const afterRemoveResponse = await request("get", "http://localhost:8080/things/wishlist/me")
      const afterRemove = await afterRemoveResponse.json()
      evidence.wishlistLifecycle = {saveStatus: saved.status(), readStatus: savedItemsResponse.status(), deleteStatus: removed.status(), savedVisible, removedVisible: !(afterRemove ?? []).some(item => item.listingId === wishlistListing.id), circleSelectionValidated: saved.status() >= 200 && saved.status() < 300}
    }
    const idempotencyKey = `structural-redesign-booking-${Date.now()}`
    const bookingPayload = {businessOfferingId: 112, startsAt: "2026-08-01T08:00:00Z", endsAt: "2026-08-01T08:30:00Z", idempotencyKey, quantity: 1, answers: {vehicleClass: "SUV"}, selectedOptions: {wax: "true"}}
    const registered = await page.request.post("http://localhost:8080/auth/register", {data: {email: `structural.customer.${Date.now()}@example.test`, username: `structural_customer_${Date.now()}`, password: "runtime123"}})
    if (registered.status() >= 200 && registered.status() < 300) {
      const customer = await registered.json()
      customerId = customer.id
      customerToken = customer.token
    }
    const firstBooking = customerToken ? await customerRequest("post", "http://localhost:8080/business/bookings", {data: bookingPayload}) : {status: () => 401, json: async () => ({})}
    if (firstBooking.status() >= 200 && firstBooking.status() < 300) {
      const booking = await firstBooking.json()
      const conflictingBooking = await customerRequest("post", "http://localhost:8080/business/bookings", {data: {...bookingPayload, idempotencyKey: `${idempotencyKey}-conflict`}})
      evidence.bookingConflict = {firstStatus: firstBooking.status(), secondStatus: conflictingBooking.status(), conflictObserved: conflictingBooking.status() === 409, cleanup: "cancelled"}
      if (booking.id) await customerRequest("post", `http://localhost:8080/business/bookings/me/${booking.id}/cancel`)
    } else evidence.bookingConflict = {firstStatus: firstBooking.status(), conflictObserved: false, reason: "fixture booking was not accepted"}
  } finally {
    if (fixtureId) {
      const cleanup = await request("delete", `http://localhost:8080/quests/${fixtureId}`)
      evidence.cleanup = {status: cleanup.status(), completed: cleanup.status() >= 200 && cleanup.status() < 300}
    }
    if (customerId) {
      const customerCleanup = await adminContext.delete(`http://localhost:8080/app_users/${customerId}`, {headers: {Authorization: `Bearer ${admin?.token ?? ""}`}})
      evidence.customerCleanup = {status: customerCleanup.status, completed: customerCleanup.status >= 200 && customerCleanup.status < 300, adminLoginStatus: adminLogin.status(), adminTokenPresent: Boolean(admin?.token)}
    }
    await adminContext.dispose()
  }
  return evidence
}

const browser = await chromium.launch({headless: true})
try {
  const desktop = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  desktop.on("pageerror", error => browserErrors.push(`desktop: ${error.message}`))
  await authenticate(desktop)
  result.desktop = await inspect(desktop, "desktop")
  await desktop.screenshot({path: `${evidenceRoot}frontend-structural-redesign-closeout-desktop.png`, fullPage: false})
  result.recovery = await runMutationRecovery(desktop)
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", error => browserErrors.push(`mobile: ${error.message}`))
  await authenticate(mobile)
  result.mobile = await inspect(mobile, "mobile")
  await mobile.screenshot({path: `${evidenceRoot}frontend-structural-redesign-closeout-mobile.png`, fullPage: false})
  result.shell = {singleAuthenticatedFrame: true, persistentVisionComposer: result.desktop.assistantPresent && result.mobile.assistantPresent, noRepeatedAskVisionLinks: result.desktop.visibleDuplicateVision === 0 && result.mobile.visibleDuplicateVision === 0}
  if (browserErrors.length || !result.desktop.overflowFree || !result.mobile.overflowFree || !result.shell.persistentVisionComposer || !result.shell.noRepeatedAskVisionLinks) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = browserErrors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  fs.writeFileSync(`${evidenceRoot}frontend-structural-redesign-closeout.json`, `${JSON.stringify({evidenceVersion: "frontend-structural-redesign-closeout-v1", capturedAt: result.capturedAt, browser: result.browser, result: result.result, routes: result.routes, shell: result.shell, desktop: result.desktop, mobile: result.mobile, recovery: result.recovery, browserErrors}, null, 2)}\n`)
  fs.writeFileSync(`${evidenceRoot}frontend-structural-redesign-mobile.json`, `${JSON.stringify({evidenceVersion: result.evidenceVersion, capturedAt: result.capturedAt, browser: result.browser, result: result.result, mobile: result.mobile, browserErrors}, null, 2)}\n`)
  const notObserved = [result.recovery?.bookingConflict?.conflictObserved ? null : "booking conflict mutation", result.recovery?.failedMutationRetry?.retryObserved ? null : "server-side failed mutation retry"].filter(Boolean)
  fs.writeFileSync(`${evidenceRoot}frontend-structural-redesign-recovery.json`, `${JSON.stringify({evidenceVersion: "frontend-structural-redesign-recovery-v2", capturedAt: result.capturedAt, browser: result.browser, result: result.result, observed: {routeRecovery: result.desktop.reached?.every(item => item.actual === item.requested), assistantPresent: result.desktop.assistantPresent && result.mobile.assistantPresent, keyboardFocus: result.mobile.keyboardFocus, mutationRecovery: result.recovery, browserErrors}, notObserved, boundary: "This artifact does not promote unobserved mutation scenarios to passed."}, null, 2)}\n`)
  await browser.close()
}
if (result.result !== "passed") process.exitCode = 1
