import {chromium} from "playwright"
import {expect} from "playwright/test"
import {mkdir, writeFile} from "node:fs/promises"
import {dirname, isAbsolute, resolve} from "node:path"
import {fileURLToPath} from "node:url"

const workspaceRoot = resolve(dirname(fileURLToPath(import.meta.url)), "../../../..")
const workspaceArtifactPath = (path) => path && (isAbsolute(path) ? path : resolve(workspaceRoot, path))

const baseURL = process.env.WEB_BASE_URL || "http://localhost:5173"
const apiBaseURL = process.env.WEB_API_BASE_URL || "http://localhost:8080"
const apiOrigin = new URL(apiBaseURL).origin
const runtimeEvidencePath = workspaceArtifactPath(process.env.WEB_RUNTIME_EVIDENCE_PATH)
const desktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_DESKTOP_SCREENSHOT_PATH)
const narrowScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_NARROW_SCREENSHOT_PATH)
const workPreviewScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_WORK_PREVIEW_SCREENSHOT_PATH)
const thingsPreviewScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_THINGS_PREVIEW_SCREENSHOT_PATH)
const questDetailScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_QUEST_DETAIL_SCREENSHOT_PATH)
const thingDetailNarrowScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_THING_DETAIL_NARROW_SCREENSHOT_PATH)
const personalAttentionDesktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_PERSONAL_ATTENTION_DESKTOP_SCREENSHOT_PATH)
const pinVisibilityRecoveryScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_PIN_VISIBILITY_RECOVERY_SCREENSHOT_PATH)
const commandCenterDesktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_COMMAND_CENTER_DESKTOP_SCREENSHOT_PATH)
const commandFocusSuppressionScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_COMMAND_FOCUS_SUPPRESSION_SCREENSHOT_PATH)
const chatDesktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_CHAT_DESKTOP_SCREENSHOT_PATH)
const chatNarrowScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_CHAT_NARROW_SCREENSHOT_PATH)
const visionIdleScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_VISION_IDLE_SCREENSHOT_PATH)
const visionReviewScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_VISION_REVIEW_SCREENSHOT_PATH)
const businessBookingsDesktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_BUSINESS_BOOKINGS_DESKTOP_SCREENSHOT_PATH)
const ridesDesktopScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_RIDES_DESKTOP_SCREENSHOT_PATH)
const finalVisionScreenshotPath = workspaceArtifactPath(process.env.WEB_RUNTIME_FINAL_VISION_SCREENSHOT_PATH)
const runStartedAt = new Date().toISOString()
const suffix = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
const account = {
  email: `web-smoke-${suffix}@sidequest.test`,
  username: `websmoke${suffix}`,
  password: "SmokeTest-password-123!"
}
const contact = {
  email: `web-smoke-contact-${suffix}@sidequest.test`,
  username: `webcontact${suffix}`,
  password: "SmokeTest-password-123!"
}
const contactTwo = {
  email: `web-smoke-contact-two-${suffix}@sidequest.test`,
  username: `webcontacttwo${suffix}`,
  password: "SmokeTest-password-123!"
}
const observer = {
  email: `web-smoke-observer-${suffix}@sidequest.test`,
  username: `webobserver${suffix}`,
  password: "SmokeTest-password-123!"
}

const browser = await chromium.launch({headless: true})
const context = await browser.newContext({recordHar: {path: "./web-runtime-smoke.har", mode: "minimal"}})
const page = await context.newPage()
const main = page.locator("main")
const statuses = []
const browserDiagnostics = []
let result = "failed"
let failureMessage = null
let personalAttention = null
let commandCenter = null
let chatReconnect = null
let visionHandoff = null
let crossModuleWorkspace = null
page.on("pageerror", (error) => {
  browserDiagnostics.push(`pageerror: ${error.message}`)
  console.log(`PAGE_ERROR ${error.message}`)
})
page.on("console", (message) => {
  if (message.type() === "error") browserDiagnostics.push(`console: ${message.text()}`)
})
page.on("requestfailed", (request) => browserDiagnostics.push(`requestfailed: ${request.url()} ${request.failure()?.errorText || "unknown"}`))
const observeResponses = (observedPage) => observedPage.on("response", (response) => {
  const url = new URL(response.url())
  if (url.origin === apiOrigin) {
    statuses.push({method: response.request().method(), path: url.pathname, status: response.status()})
  }
})
observeResponses(page)

const visit = async (path) => {
  await page.goto(`${baseURL}${path}`, {waitUntil: "networkidle"})
  await expect(page.locator("body")).not.toContainText("Invalid email or password")
}

try {
  await visit("/register")
  await page.getByLabel("Email").fill(account.email)
  await page.getByLabel("Username").fill(account.username)
  await page.getByLabel("Password").fill(account.password)
  await page.getByRole("button", {name: "Create account"}).click()
  try {
    await page.waitForURL(/\/vision$/, {timeout: 10_000})
  } catch {
    const registrationError = await page.locator(".auth-terminal__error").textContent()
    throw new Error(`Registration did not reach Vision: ${registrationError || "no rendered error"}`)
  }
  if (visionIdleScreenshotPath) { await mkdir(dirname(visionIdleScreenshotPath), {recursive: true}); await page.screenshot({path: visionIdleScreenshotPath, fullPage: true}) }
  const contactRegistration = await context.request.post(`${apiBaseURL}/auth/register`, {data: contact})
  if (!contactRegistration.ok()) throw new Error(`Contact registration failed: ${contactRegistration.status()}`)
  const contactTwoRegistration = await context.request.post(`${apiBaseURL}/auth/register`, {data: contactTwo})
  if (!contactTwoRegistration.ok()) throw new Error(`Second contact registration failed: ${contactTwoRegistration.status()}`)
  const observerRegistration = await context.request.post(`${apiBaseURL}/auth/register`, {data: observer})
  if (!observerRegistration.ok()) throw new Error(`Observer registration failed: ${observerRegistration.status()}`)

  await visit("/home")
  await page.keyboard.press(process.platform === "darwin" ? "Meta+K" : "Control+K")
  const commandCenterPanel = page.locator(".global-search-entry__panel")
  await expect(commandCenterPanel).toBeVisible()
  await expect(commandCenterPanel.getByText("Command center · permitted routes and records only", {exact: true})).toBeVisible()
  await expect(commandCenterPanel.getByRole("button", {name: /Create a quest/})).toBeVisible()
  await expect(commandCenterPanel.getByRole("button", {name: /Ask Vision/})).toBeVisible()
  if (commandCenterDesktopScreenshotPath) { await mkdir(dirname(commandCenterDesktopScreenshotPath), {recursive: true}); await page.screenshot({path: commandCenterDesktopScreenshotPath, fullPage: true}) }
  await page.keyboard.press("Escape")
  await expect(commandCenterPanel).toBeHidden()
  await visit("/work/quests/new")
  const commandSuppressionInput = page.getByPlaceholder("What needs doing?")
  await commandSuppressionInput.focus()
  await page.keyboard.press(process.platform === "darwin" ? "Meta+K" : "Control+K")
  await expect(page.locator(".global-search-entry__panel")).toBeHidden()
  if (commandFocusSuppressionScreenshotPath) { await mkdir(dirname(commandFocusSuppressionScreenshotPath), {recursive: true}); await page.screenshot({path: commandFocusSuppressionScreenshotPath, fullPage: true}) }
  commandCenter = {openedWithKeyboard: true, catalogSeparated: true, suppressedWhileEditing: true}
  await visit("/home")
  if (desktopScreenshotPath) {
    await mkdir(dirname(desktopScreenshotPath), {recursive: true})
    await page.screenshot({path: desktopScreenshotPath, fullPage: true})
  }
  if (narrowScreenshotPath) {
    await page.setViewportSize({width: 390, height: 844})
    await page.screenshot({path: narrowScreenshotPath, fullPage: true})
    await page.setViewportSize({width: 1280, height: 900})
  }
  const moreModules = page.locator("details.app-shell__more")
  await moreModules.locator("summary").click()
  await expect(moreModules.getByRole("link", {name: "Rides", exact: true})).toBeVisible()
  await expect(moreModules.getByRole("link", {name: "Things", exact: true})).toBeVisible()
  await expect(main.getByRole("link", {name: "Create new work", exact: true})).toHaveCount(0)

  await visit("/onboarding")
  await expect(main.getByRole("heading", {name: "Get oriented", exact: true})).toBeVisible()
  await main.getByRole("button", {name: "PROFILE", exact: true}).click()
  await expect(main.locator(".state")).toContainText("PROFILE")
  await visit("/home")
  await visit("/onboarding")
  await expect(main.locator(".state")).toContainText("PROFILE")
  await main.getByRole("button", {name: "Skip setup", exact: true}).click()
  await expect(main.getByText("Onboarding skipped. Core product use remains available.", {exact: true})).toBeVisible()
  await main.getByRole("button", {name: "Reset", exact: true}).click()
  await expect(main.getByText("Onboarding reset.", {exact: true})).toBeVisible()

  await visit("/home")
  await page.locator('summary[aria-label="Open command center"]').click()
  await page.getByPlaceholder("Work, people, business, things…").fill(`saved smoke ${suffix}`)
  await page.locator(".global-search-entry form").getByRole("button", {name: "Search", exact: true}).click()
  await expect(page.getByRole("button", {name: "Save search", exact: true})).toBeVisible()
  await page.getByRole("button", {name: "Save search", exact: true}).click()
  await page.getByRole("link", {name: "Manage saved searches", exact: true}).click()
  await page.waitForURL(/\/search\/saved$/)
  await page.locator('summary[aria-label="Open command center"]').click()
  await expect(main.getByRole("heading", {name: "Saved searches", exact: true})).toBeVisible()
  const savedSearchRow = main.locator("article").filter({hasText: `saved smoke ${suffix}`})
  await expect(savedSearchRow).toBeVisible()
  await savedSearchRow.getByRole("button", {name: "Pause", exact: true}).click()
  await expect(main.getByText("Search paused.", {exact: true})).toBeVisible()
  await savedSearchRow.getByRole("button", {name: "Resume", exact: true}).click()
  await expect(main.getByText("Search resumed.", {exact: true})).toBeVisible()
  await savedSearchRow.getByRole("button", {name: "Delete", exact: true}).click()
  const deleteSavedSearchDialog = page.getByRole("dialog", {name: "Delete saved search", exact: true})
  await expect(deleteSavedSearchDialog).toBeVisible()
  await deleteSavedSearchDialog.getByRole("button", {name: "Continue", exact: true}).click()
  await expect(main.getByText(`saved smoke ${suffix}`, {exact: true})).toHaveCount(0)

  await visit("/activity")
  await expect(main.getByRole("heading", {name: "Activity", exact: true})).toBeVisible()
  await expect(main.getByText("Could not load activity.", {exact: true})).toHaveCount(0)

  await visit("/notifications")
  await expect(main.getByRole("heading", {name: "Notifications", exact: true})).toBeVisible()
  await expect(main.getByRole("heading", {name: /unread updates/})).toBeVisible()
  await expect(main.getByText("Could not load notifications.", {exact: true})).toHaveCount(0)

  await visit("/business")
  await expect(main.getByRole("heading", {name: "Business", exact: true})).toBeVisible()
  await expect(main.getByText("Set up your business profile", {exact: true})).toBeVisible()

  await visit("/work/find")
  await expect(main.getByRole("heading", {name: "Work"})).toBeVisible()
  await expect(main.getByRole("link", {name: "Offer work", exact: true})).toBeVisible()
  await expect(main.getByPlaceholder("Search work")).toBeVisible()
  await main.getByRole("link", {name: "Offer work", exact: true}).click()
  await page.waitForURL(/\/work\/(quests\/new|offer)$/)
  await expect(page.getByRole("heading", {name: "Create a quest"})).toBeVisible()
  const smokeWorkTitle = `Browser smoke work ${suffix}`
  await page.getByPlaceholder("What needs doing?").fill(smokeWorkTitle)
  await page.locator(".rich-text-editor__content .tiptap").fill("Created by the authenticated browser smoke harness.")
  await page.getByRole("button", {name: "Create quest"}).click()
  await page.waitForURL(/\/work\/quests$/)
  await expect(main.getByRole("heading", {name: "My quests", exact: true})).toBeVisible()
  await visit("/work/find")
  await page.getByPlaceholder("Search work").fill(smokeWorkTitle)
  await page.getByPlaceholder("Search work").press("Enter")
  const smokeWorkLink = main.getByRole("link", {name: smokeWorkTitle, exact: true})
  await expect(smokeWorkLink).toHaveCount(0)
  await visit("/work/quests")
  await expect(main.getByRole("heading", {name: "My quests", exact: true})).toBeVisible()
  const ownedSmokeWorkCard = main.locator("article").filter({hasText: smokeWorkTitle})
  await expect(ownedSmokeWorkCard).toBeVisible()
  await ownedSmokeWorkCard.focus()
  await ownedSmokeWorkCard.press("p")
  await expect(page.getByRole("complementary", {name: `${smokeWorkTitle} preview`})).toBeVisible()
  if (workPreviewScreenshotPath) {
    await mkdir(dirname(workPreviewScreenshotPath), {recursive: true})
    await page.screenshot({path: workPreviewScreenshotPath, fullPage: true})
  }
  await page.keyboard.press("Escape")
  await ownedSmokeWorkCard.click()
  await page.waitForURL(/\/work\/quests\/\d+$/)
  const smokeWorkPath = new URL(page.url()).pathname
  if (questDetailScreenshotPath) {
    await mkdir(dirname(questDetailScreenshotPath), {recursive: true})
    await page.screenshot({path: questDetailScreenshotPath, fullPage: true})
  }
  await page.getByRole("button", {name: "Edit", exact: true}).click()
  await page.locator(".rich-text-editor__content .tiptap").fill("Updated by the authenticated browser smoke harness.")
  await page.getByRole("button", {name: "Save", exact: true}).click()
  await expect(page.getByText("Quest updated.", {exact: true})).toBeVisible()

  const personalPinTitle = `Personal pin smoke ${suffix}`
  await visit("/work/quests/new")
  await page.getByPlaceholder("What needs doing?").fill(personalPinTitle)
  await page.locator(".rich-text-editor__content .tiptap").fill("Disposable quest proving the personal workspace pin lifecycle.")
  await page.getByRole("button", {name: "Create quest"}).click()
  await page.waitForURL(/\/work\/quests$/)
  const personalPinCard = main.locator("article").filter({hasText: personalPinTitle})
  await expect(personalPinCard).toBeVisible()
  await personalPinCard.click()
  await page.waitForURL(/\/work\/quests\/\d+$/)
  const personalPinQuestId = Number(new URL(page.url()).pathname.split("/").pop())
  if (!Number.isInteger(personalPinQuestId)) throw new Error("Personal pin smoke quest did not expose a numeric id")
  const personalPinStatus = await page.evaluate(async ({apiBaseURL, questId}) => {
    const token = localStorage.getItem("token")
    return (await fetch(`${apiBaseURL}/personal-shortcuts/me/quests/${questId}`, {method: "PUT", headers: {Authorization: `Bearer ${token}`}})).status
  }, {apiBaseURL, questId: personalPinQuestId})
  expect(personalPinStatus).toBe(200)
  await visit("/notifications")
  const pinnedShortcut = page.locator(".app-shell__rail").getByRole("link", {name: personalPinTitle, exact: true})
  await expect(pinnedShortcut).toBeVisible()
  await expect(main.getByRole("heading", {name: /unread updates/})).toBeVisible()
  if (personalAttentionDesktopScreenshotPath) {
    await mkdir(dirname(personalAttentionDesktopScreenshotPath), {recursive: true})
    await page.screenshot({path: personalAttentionDesktopScreenshotPath, fullPage: true})
  }
  const personalPinDeletionStatus = await page.evaluate(async ({apiBaseURL, questId}) => {
    const token = localStorage.getItem("token")
    return (await fetch(`${apiBaseURL}/quests/${questId}`, {method: "DELETE", headers: {Authorization: `Bearer ${token}`}})).status
  }, {apiBaseURL, questId: personalPinQuestId})
  expect(personalPinDeletionStatus).toBe(200)
  const personalShortcutsAfterDeletion = await page.evaluate(async (apiBaseURL) => {
    const token = localStorage.getItem("token")
    const response = await fetch(`${apiBaseURL}/personal-shortcuts/me`, {headers: {Authorization: `Bearer ${token}`}})
    return {status: response.status, items: response.ok ? await response.json() : null}
  }, apiBaseURL)
  expect(personalShortcutsAfterDeletion.status).toBe(200)
  expect(personalShortcutsAfterDeletion.items).toEqual([])
  await page.reload({waitUntil: "networkidle"})
  await expect(page.locator(".app-shell__rail").getByRole("link", {name: personalPinTitle, exact: true})).toHaveCount(0)
  if (pinVisibilityRecoveryScreenshotPath) {
    await mkdir(dirname(pinVisibilityRecoveryScreenshotPath), {recursive: true})
    await page.screenshot({path: pinVisibilityRecoveryScreenshotPath, fullPage: true})
  }
  personalAttention = {questId: personalPinQuestId, pinStatus: personalPinStatus, deletionStatus: personalPinDeletionStatus, postDeletionShortcutReadStatus: personalShortcutsAfterDeletion.status, postDeletionShortcutCount: personalShortcutsAfterDeletion.items.length, shortcutRemovedAfterReload: true}

  const smokeThingTitle = `Browser smoke thing ${suffix}`
  await visit("/things")
  await expect(main.getByRole("heading", {name: "Things to borrow", exact: true})).toBeVisible()
  await main.getByRole("button", {name: "List a thing", exact: true}).click()
  await page.getByLabel("Title").fill(smokeThingTitle)
  await page.getByLabel("Description").fill("A thing listed by the browser smoke owner.")
  await page.getByLabel("Condition note").fill("Good condition")
  await page.getByRole("button", {name: "Save listing", exact: true}).click()
  await expect(main.getByText("Thing listed.", {exact: true})).toBeVisible()
  await main.getByRole("button", {name: "My things", exact: true}).click()
  const smokeThingLink = main.getByRole("link").filter({hasText: smokeThingTitle})
  await expect(smokeThingLink).toBeVisible()
  const smokeThingPath = await smokeThingLink.getAttribute("href")
  if (!smokeThingPath) throw new Error("Thing listing did not expose a detail route")
  await visit(smokeThingPath)
  await expect(main.getByRole("heading", {name: smokeThingTitle, exact: true})).toBeVisible()
  if (thingDetailNarrowScreenshotPath) {
    await page.setViewportSize({width: 390, height: 844})
    await page.screenshot({path: thingDetailNarrowScreenshotPath, fullPage: true})
    await page.setViewportSize({width: 1280, height: 900})
  }
  await visit("/things")
  await main.getByRole("button", {name: "My things", exact: true}).click()
  const ownedSmokeThingCard = main.locator("article").filter({hasText: smokeThingTitle})
  await ownedSmokeThingCard.getByRole("button", {name: "Preview", exact: true}).click()
  await expect(page.getByRole("complementary", {name: `${smokeThingTitle} preview`})).toBeVisible()
  if (thingsPreviewScreenshotPath) {
    await mkdir(dirname(thingsPreviewScreenshotPath), {recursive: true})
    await page.screenshot({path: thingsPreviewScreenshotPath, fullPage: true})
  }
  await page.keyboard.press("Escape")

  await visit("/chat")
  await expect(main.getByRole("heading", {name: "Inbox"})).toBeVisible()
  await expect(page.getByRole("button", {name: "New chat"})).toBeVisible()
  await expect(page.getByRole("button", {name: "New group"})).toBeVisible()
  await page.getByRole("button", {name: "New chat"}).click()
  await expect(page.getByPlaceholder("Find someone to chat with")).toBeVisible()

  await visit("/circles")
  await expect(main.getByRole("heading", {name: "Circles", exact: true})).toBeVisible()
  await page.getByRole("button", {name: "New circle", exact: true}).click()
  await expect(page.getByPlaceholder("New circle name")).toBeVisible()
  await expect(main.getByPlaceholder("Find people")).toBeVisible()
  await expect(main.getByText("Could not load circles.")).toHaveCount(0)
  await page.getByPlaceholder("New circle name").fill(`Smoke circle ${suffix}`)
  await page.getByRole("button", {name: "Create circle", exact: true}).click()
  await expect(main.getByText("Circle created.", {exact: true})).toBeVisible()

  await visit("/people")
  await expect(main.getByRole("heading", {name: "Find people"})).toBeVisible()
  await expect(main.getByPlaceholder("Search by username or profile")).toBeVisible()
  await main.getByPlaceholder("Search by username or profile").fill(contact.username)
  await main.getByRole("button", {name: "Search", exact: true}).click()
  await expect(main.getByRole("button", {name: "Send invite", exact: true})).toBeVisible()
  await main.getByRole("button", {name: "Send invite", exact: true}).click()
  await expect(main.getByText("Invite sent", {exact: true})).toBeVisible()

  await main.getByPlaceholder("Search by username or profile").fill(contactTwo.username)
  await main.getByRole("button", {name: "Search", exact: true}).click()
  await main.getByRole("button", {name: "Send invite", exact: true}).click()
  await expect(main.getByText("Invite sent", {exact: true})).toBeVisible()

  const trustProbe = await page.evaluate(async ({username, apiBaseURL}) => {
    const token = localStorage.getItem("token")
    const headers = {"Authorization": `Bearer ${token}`, "Content-Type": "application/json"}
    const search = await fetch(`${apiBaseURL}/circles/search?q=${encodeURIComponent(username)}&page=0&size=10`, {headers})
    const body = await search.json()
    const targetUserId = body.items?.find(item => item.username === username)?.id
    if (!targetUserId) return {search: search.status, block: null, unblock: null, targetUserId: null}
    const block = await fetch(`${apiBaseURL}/circles/blocks`, {method: "POST", headers, body: JSON.stringify({blockedUserId: targetUserId})})
    const unblock = block.ok ? await fetch(`${apiBaseURL}/circles/blocks/${targetUserId}`, {method: "DELETE", headers}) : null
    return {search: search.status, block: block.status, unblock: unblock?.status ?? null, targetUserId, blockBody: await block.text()}
  }, {username: observer.username, apiBaseURL})
  if (trustProbe.block !== 200 || trustProbe.unblock !== 200) throw new Error(`Trust block probe failed: ${JSON.stringify(trustProbe)}`)
  await visit(`/people/${trustProbe.targetUserId}`)
  await expect(main.getByRole("heading", {name: observer.username, exact: true})).toBeVisible()
  const reportResponse = page.waitForResponse(response => response.url().endsWith("/trust/reports") && response.request().method() === "POST")
  page.once("dialog", dialog => dialog.type() === "prompt" ? dialog.accept("Browser smoke safety report") : dialog.accept())
  await main.getByRole("button", {name: "Report", exact: true}).click()
  expect((await reportResponse).status()).toBe(200)
  await visit("/people")

  const contactContext = await browser.newContext({recordHar: {path: "./web-runtime-smoke-contact.har", mode: "minimal"}})
  const contactPage = await contactContext.newPage()
  await contactPage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await contactPage.getByLabel("Email").fill(contact.email)
  await contactPage.getByLabel("Password").fill(contact.password)
  await contactPage.getByRole("button", {name: "Enter"}).click()
  await contactPage.waitForURL(/\/vision$/)
  await contactPage.goto(`${baseURL}/circles`, {waitUntil: "networkidle"})
  await contactPage.getByRole("button", {name: "Accept", exact: true}).click()
  await expect(contactPage.getByText("Request accepted.", {exact: true})).toBeVisible()
  await contactPage.goto(`${baseURL}${smokeWorkPath}`, {waitUntil: "networkidle"})
  await contactPage.getByRole("button", {name: "Apply on web", exact: true}).click()
  await contactPage.getByLabel("Message").fill("I can help with this browser smoke quest.")
  await contactPage.getByRole("button", {name: "Send application", exact: true}).click()
  await expect(contactPage.getByText("Application sent.", {exact: true})).toBeVisible()
  await contactPage.goto(`${baseURL}${smokeThingPath}`, {waitUntil: "networkidle"})
  await contactPage.getByPlaceholder("Add a note for the owner.").fill("I would like to borrow this for the smoke trace.")
  await contactPage.getByRole("button", {name: "Send request", exact: true}).click()
  await expect(contactPage.getByText("Borrow request sent.", {exact: true})).toBeVisible()
  await contactContext.close()

  const contactTwoContext = await browser.newContext()
  const contactTwoPage = await contactTwoContext.newPage()
  await contactTwoPage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await contactTwoPage.getByLabel("Email").fill(contactTwo.email)
  await contactTwoPage.getByLabel("Password").fill(contactTwo.password)
  await contactTwoPage.getByRole("button", {name: "Enter"}).click()
  await contactTwoPage.waitForURL(/\/vision$/)
  await contactTwoPage.goto(`${baseURL}/circles`, {waitUntil: "networkidle"})
  await contactTwoPage.getByRole("button", {name: "Accept", exact: true}).click()
  await expect(contactTwoPage.getByText("Request accepted.", {exact: true})).toBeVisible()
  await contactTwoContext.close()

  await visit("/chat")
  await page.getByRole("button", {name: "New chat"}).click()
  await page.getByPlaceholder("Find someone to chat with").fill(contact.username)
  await page.getByRole("button", {name: contact.username, exact: true}).click()
  await page.waitForURL(/\/chat\/\d+$/)
  await page.getByPlaceholder("Write a message.").fill("Browser smoke message")
  await page.getByRole("button", {name: "Send", exact: true}).click()
  await expect(page.getByText("Browser smoke message", {exact: true})).toBeVisible()
  if (chatDesktopScreenshotPath) { await mkdir(dirname(chatDesktopScreenshotPath), {recursive: true}); await page.screenshot({path: chatDesktopScreenshotPath, fullPage: true}) }
  await page.route(/\/chat\/conversations\/\d+\/(sync|refresh-hint)/, route => route.abort())
  await page.getByRole("button", {name: "Sync", exact: true}).click()
  await expect(page.getByRole("status")).toContainText("Could not sync conversation.")
  await page.unroute(/\/chat\/conversations\/\d+\/(sync|refresh-hint)/)
  await page.getByRole("button", {name: "Sync", exact: true}).click()
  await expect(page.getByRole("status")).toContainText(/Conversation synced\.|Conversation is current\./)
  await page.route(/\/chat\/conversations\/\d+\/(sync|refresh-hint)/, route => route.abort())
  await page.evaluate(() => window.dispatchEvent(new Event("online")))
  await expect(page.getByRole("status")).toContainText("Could not sync conversation.")
  await page.unroute(/\/chat\/conversations\/\d+\/(sync|refresh-hint)/)
  await page.evaluate(() => window.dispatchEvent(new Event("online")))
  await expect(page.getByRole("status")).toContainText(/Conversation synced\.|Conversation is current\./)
  if (chatNarrowScreenshotPath) { await page.setViewportSize({width: 390, height: 844}); await page.screenshot({path: chatNarrowScreenshotPath, fullPage: true}); await page.setViewportSize({width: 1280, height: 900}) }
  chatReconnect = {offlineSyncFailureShown: true, onlineServerSyncShown: true}
  await page.goto(`${baseURL}/chat`, {waitUntil: "networkidle"})
  await page.getByRole("button", {name: "New group"}).click()
  await page.getByLabel("Group name").fill(`Smoke group ${suffix}`)
  await page.getByLabel("Find people").fill(contact.username)
  await expect(page.locator("label.chat-surface__candidate").filter({hasText: contact.username})).toBeVisible()
  await page.locator("label.chat-surface__candidate").filter({hasText: contact.username}).locator("input").check()
  await page.getByLabel("Find people").fill(contactTwo.username)
  await expect(page.locator("label.chat-surface__candidate").filter({hasText: contactTwo.username})).toBeVisible()
  await page.locator("label.chat-surface__candidate").filter({hasText: contactTwo.username}).locator("input").check()
  await page.getByRole("button", {name: "Create group", exact: true}).click()
  await page.waitForURL(/\/chat\/\d+$/)
  await expect(page.getByRole("button", {name: "Leave group", exact: true})).toBeVisible()

  await visit(`${smokeWorkPath}/applications`)
  await expect(main.getByRole("heading", {name: "Applications", exact: true})).toBeVisible()
  const smokeApplicationRow = main.locator("article").filter({hasText: contact.username})
  await expect(smokeApplicationRow).toBeVisible()
  await smokeApplicationRow.getByRole("button", {name: "Approve", exact: true}).click()
  await expect(main.getByText("Application approved.", {exact: true})).toBeVisible()

  await visit("/things")
  await main.getByRole("button", {name: "My things", exact: true}).click()
  await expect(main.getByRole("heading", {name: "My things", exact: true})).toBeVisible()
  const borrowRequest = main.locator("article.request").filter({hasText: contact.username})
  await expect(borrowRequest).toBeVisible()
  await borrowRequest.getByRole("button", {name: "Approve", exact: true}).click()
  await expect(main.getByText("Borrow request approved.", {exact: true})).toBeVisible()

  await context.grantPermissions([])
  await visit("/profile/settings")
  await page.getByRole("button", {name: "Use current location", exact: true}).click()
  await expect(main.getByRole("alert")).toContainText("Location permission was denied or unavailable")
  const locationSearchResponse = page.waitForResponse(response => response.url().includes("/location/lookup") && response.request().method() === "POST")
  await page.getByPlaceholder("Search for a place").fill("Zurich")
  await locationSearchResponse
  const locationSuggestion = page.locator("ul.suggestions button").first()
  const locationConfigured = await locationSuggestion.count() > 0
  if (locationConfigured) {
    await locationSuggestion.click()
    await page.getByLabel("Location mode").selectOption("EXACT")
    await page.getByRole("combobox", {name: /Exact address visibility/}).selectOption("USERS")
    await expect(page.locator("fieldset.visibility-options input[type=checkbox]").first()).toBeVisible()
    await page.locator("fieldset.visibility-options input[type=checkbox]").first().check()
  } else {
    await expect(page.getByText("Searching…", {exact: true})).toHaveCount(0)
    await page.getByLabel("Location mode").selectOption("OFF")
  }
  await page.getByRole("button", {name: "Save profile and location", exact: true}).click()
  await expect(page.getByRole("status")).toContainText("Profile and location settings saved.")
  await page.getByLabel("Image URL").fill("https://example.com/profile-gallery-smoke.jpg")
  await page.getByLabel("Alt text").fill("Smoke profile gallery photo")
  await page.getByRole("button", {name: "Add photo", exact: true}).click()
  await expect(main.getByAltText("Smoke profile gallery photo")).toBeVisible()
  await main.getByRole("button", {name: "Remove", exact: true}).click()

  const primaryUserId = await page.evaluate(() => JSON.parse(localStorage.getItem("user") ?? "{}").id)
  if (!primaryUserId) throw new Error("Primary browser session did not expose a user id")
  const memberContext = await browser.newContext({recordHar: {path: "./web-runtime-smoke-visibility-member.har", mode: "minimal"}})
  const memberPage = await memberContext.newPage()
  await memberPage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await memberPage.getByLabel("Email").fill(contactTwo.email)
  await memberPage.getByLabel("Password").fill(contactTwo.password)
  await memberPage.getByRole("button", {name: "Enter"}).click()
  await memberPage.waitForURL(/\/vision$/)
  await memberPage.goto(`${baseURL}/people/${primaryUserId}`, {waitUntil: "networkidle"})
  await expect(memberPage.getByRole("heading", {name: account.username, exact: true})).toBeVisible()
  const allowedProfile = await memberPage.evaluate(async ({userId, apiBaseURL}) => {
    const token = localStorage.getItem("token")
    return (await (await fetch(`${apiBaseURL}/app_users/${userId}/profile-view`, {headers: {Authorization: `Bearer ${token}`}})).json()).profile.locationSettings
  }, {userId: primaryUserId, apiBaseURL})
  if (locationConfigured) expect(allowedProfile.latitude).not.toBeNull()
  await memberContext.close()

  const observerContext = await browser.newContext({recordHar: {path: "./web-runtime-smoke-visibility-outsider.har", mode: "minimal"}})
  const observerPage = await observerContext.newPage()
  await observerPage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await observerPage.getByLabel("Email").fill(observer.email)
  await observerPage.getByLabel("Password").fill(observer.password)
  await observerPage.getByRole("button", {name: "Enter"}).click()
  await observerPage.waitForURL(/\/vision$/)
  await observerPage.goto(`${baseURL}/people/${primaryUserId}`, {waitUntil: "networkidle"})
  await expect(observerPage.getByRole("heading", {name: account.username, exact: true})).toBeVisible()
  const deniedProfile = await observerPage.evaluate(async ({userId, apiBaseURL}) => {
    const token = localStorage.getItem("token")
    return (await (await fetch(`${apiBaseURL}/app_users/${userId}/profile-view`, {headers: {Authorization: `Bearer ${token}`}})).json()).profile.locationSettings
  }, {userId: primaryUserId, apiBaseURL})
  expect(deniedProfile.latitude).toBeNull()
  await observerContext.close()

  await visit("/vision")
  const visionInput = page.locator("textarea.vision-console__input")
  await expect(visionInput).toBeVisible()
  await visionInput.fill("show my profile")
  await visionInput.press("Enter")
  await expect(page.locator(".vision-console__line--agent")).toBeVisible({timeout: 15000})
  await page.reload({waitUntil: "networkidle"})
  await expect(page.locator("textarea.vision-console__input")).toBeVisible()

  await visit("/vision?prompt=show%20my%20profile&autorun=1&context=Profile%20workspace&source=shell.surface.profile&returnTo=%2Fhome")
  await expect(page.getByText("Opened from Profile workspace.", {exact: true})).toBeVisible({timeout: 15000})
  await expect(page.getByRole("link", {name: "Return", exact: true})).toHaveAttribute("href", "/home")
  if (finalVisionScreenshotPath) { await mkdir(dirname(finalVisionScreenshotPath), {recursive: true}); await page.screenshot({path: finalVisionScreenshotPath, fullPage: true}) }
  visionHandoff = {explained: true, safeReturnVisible: true}

  await visit("/vision")

  const sendVisionPrompt = async (prompt) => {
    await visionInput.fill(prompt)
    const turnResponse = page.waitForResponse(response => response.url().includes("/vision/conversations/turns") && response.request().method() === "POST")
    await visionInput.press("Enter")
    await turnResponse
    await expect(page.locator(".vision-console__line--agent")).toBeVisible({timeout: 15000})
  }
  await sendVisionPrompt("create quest")
  await sendVisionPrompt(`Garden cleanup ${suffix}`)
  await sendVisionPrompt("Clean the garden and remove the leaves.")
  await sendVisionPrompt("free")
  await sendVisionPrompt("public")
  await sendVisionPrompt("by agreement")
  await sendVisionPrompt("hide location")
  if (visionReviewScreenshotPath) { await mkdir(dirname(visionReviewScreenshotPath), {recursive: true}); await page.screenshot({path: visionReviewScreenshotPath, fullPage: true}) }
  const confirmVisionResponse = page.waitForResponse(response => response.url().includes("/vision/conversations/turns") && response.request().method() === "POST")
  await page.getByRole("button", {name: "Confirm and create", exact: true}).click()
  const confirmedVisionResponse = await confirmVisionResponse
  expect(confirmedVisionResponse.status()).toBe(200)
  await expect(page.locator(".vision-console__line--agent")).toContainText(/created|complete|execution is disabled by configuration/i, {timeout: 15000})

  await visit("/business/find")
  await expect(main.getByRole("heading", {name: "Find a business"})).toBeVisible()
  await expect(main.getByPlaceholder("Search businesses")).toBeVisible()

  await visit("/business/profile")
  await expect(main.getByRole("heading", {name: "Profile", exact: true})).toBeVisible()
  await page.getByLabel("Business name").fill(`Browser Smoke Business ${suffix}`)
  await page.getByLabel("Headline").fill("A browser smoke booking business")
  await page.getByLabel("Description").fill("Public booking surface used by the authenticated runtime smoke.")
  await page.getByLabel("Timezone").fill("Europe/Zurich")
  await page.getByLabel("Accept bookings").check()
  await page.getByRole("button", {name: "Save profile", exact: true}).click()
  await expect(page.getByText("Profile updated.", {exact: true})).toBeVisible()
  const businessSlug = await page.evaluate(async (apiBaseURL) => {
    const token = localStorage.getItem("token")
    const response = await fetch(`${apiBaseURL}/business/profiles/me`, {headers: {Authorization: `Bearer ${token}`}})
    if (!response.ok) throw new Error(`Business profile read failed: ${response.status}`)
    return (await response.json()).slug
  }, apiBaseURL)

  await visit("/business/offerings")
  await page.getByRole("button", {name: "New offering", exact: true}).click()
  await page.getByLabel("Title").fill("Smoke consultation")
  await page.getByLabel("Slug").fill("smoke-consultation")
  await page.getByLabel("Summary").fill("A short browser smoke consultation.")
  await page.getByRole("button", {name: "Save", exact: true}).click()
  await expect(page.getByText("Offering saved.", {exact: true})).toBeVisible()

  await visit("/business/calendar")
  await page.getByRole("button", {name: "New rule", exact: true}).click()
  await page.getByRole("button", {name: "Save", exact: true}).click()
  await expect(page.getByText("Availability rule saved.", {exact: true})).toBeVisible()

  const bookingContext = await browser.newContext({recordHar: {path: "./web-runtime-smoke-booking.har", mode: "minimal"}})
  const bookingPage = await bookingContext.newPage()
  observeResponses(bookingPage)
  await bookingPage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await bookingPage.getByLabel("Email").fill(contact.email)
  await bookingPage.getByLabel("Password").fill(contact.password)
  await bookingPage.getByRole("button", {name: "Enter"}).click()
  await bookingPage.waitForURL(/\/vision$/)
  await bookingPage.goto(`${baseURL}/business/public/${businessSlug}`, {waitUntil: "networkidle"})
  await expect(bookingPage.getByRole("heading", {name: `Browser Smoke Business ${suffix}`})).toBeVisible()
  await bookingPage.getByRole("button", {name: "Book", exact: true}).click()
  await bookingPage.getByLabel("Start").fill("2026-07-20T10:00")
  await bookingPage.getByRole("button", {name: "Send request", exact: true}).click()
  await expect(bookingPage.getByText("Booking request sent.", {exact: true})).toBeVisible()
  await bookingContext.close()

  await visit("/business/bookings")
  await expect(main.getByRole("heading", {name: "Bookings", exact: true})).toBeVisible()
  if (businessBookingsDesktopScreenshotPath) { await mkdir(dirname(businessBookingsDesktopScreenshotPath), {recursive: true}); await page.screenshot({path: businessBookingsDesktopScreenshotPath, fullPage: true}) }
  const bookingRow = main.locator("article").filter({hasText: contact.username})
  await expect(bookingRow).toBeVisible()
  await bookingRow.getByRole("button", {name: "Confirm", exact: true}).click()
  await expect(main.getByText("Booking updated.", {exact: true})).toBeVisible()

  await visit("/calendar")
  await expect(main.getByRole("heading", {name: "Calendar"})).toBeVisible()
  await expect(page.getByRole("button", {name: "Previous"})).toBeVisible()
  await expect(page.getByRole("button", {name: "week", exact: true})).toBeVisible()
  await expect(page.getByRole("button", {name: "day", exact: true})).toBeVisible()
  await page.getByRole("button", {name: "week", exact: true}).click()
  await expect(page.locator(".surface-content__week-grid")).toBeVisible()
  await page.getByRole("button", {name: "day", exact: true}).click()
  await expect(page.locator(".surface-content__day-view")).toBeVisible()

  await page.route("**/chat/conversations**", route => route.abort())
  await visit("/chat")
  await expect(page.getByRole("alert")).toContainText("Could not load conversations")
  await expect(page.getByRole("alert").getByRole("button", {name: "Retry"})).toBeVisible()
  await page.unroute("**/chat/conversations**")
  await visit("/chat")
  await expect(main.getByRole("heading", {name: "Inbox"})).toBeVisible()

  await page.route("**/quests/presets/AVAILABLE**", route => route.abort())
  await visit("/work/find")
  await expect(main.getByRole("alert")).toContainText("Could not load work")
  await expect(main.getByRole("alert").getByRole("button", {name: "Try again"})).toBeVisible()
  await page.unroute("**/quests/presets/AVAILABLE**")

  await page.route("**/business/profiles**", route => route.abort())
  await visit("/business/find")
  await expect(main.getByRole("alert")).toContainText("Could not load businesses")
  await expect(main.getByRole("alert").getByRole("button", {name: "Retry"})).toBeVisible()
  await page.unroute("**/business/profiles**")

  await page.route("**/business/profiles*", route => route.fulfill({status: 403, contentType: "application/json", body: JSON.stringify({code: "FORBIDDEN", message: "Business discovery is not available for this viewer."})}))
  await visit("/business/find")
  await expect(main.getByRole("alert")).toContainText("Could not load businesses")
  await page.unroute("**/business/profiles*")

  await page.route("**/circles/blocked**", route => route.abort())
  await visit("/circles")
  await expect(main.getByRole("button", {name: "New circle", exact: true})).toBeVisible()
  await expect(main.getByRole("alert")).toContainText("Some circle data")
  await page.unroute("**/circles/blocked**")

  await page.route("**/dashboard/me**", route => route.abort())
  await visit("/calendar")
  await expect(main.getByRole("alert")).toContainText("temporarily unavailable")
  await expect(main.getByRole("alert").getByRole("button", {name: "Retry"})).toBeVisible()
  await page.unroute("**/dashboard/me**")

  await visit("/rides")
  await expect(main.getByRole("heading", {name: "Find a ride"})).toBeVisible()
  await expect(main.getByText("Commute matching", {exact: true})).toBeVisible()
  if (ridesDesktopScreenshotPath) { await mkdir(dirname(ridesDesktopScreenshotPath), {recursive: true}); await page.screenshot({path: ridesDesktopScreenshotPath, fullPage: true}) }
  crossModuleWorkspace = {businessBookingsWorkspaceRows: true, ridesWorkspaceRows: true}
  const commuteToggle = main.getByLabel("Suggest compatible rides")
  await expect(commuteToggle).not.toBeChecked()
  await commuteToggle.check()
  await main.getByRole("button", {name: "Save commute preferences", exact: true}).click()
  await expect(main.getByRole("alert")).toContainText("Could not save commute preferences.")
  await main.getByLabel("Approximate home area").fill("Zurich North")
  await main.getByLabel("Approximate work area").fill("Zurich West")
  await main.getByLabel("I consent to privacy-safe commute matching").check()
  await main.getByRole("button", {name: "Save commute preferences", exact: true}).click()
  await expect(main.getByText("Commute matching enabled.", {exact: true})).toBeVisible()
  await commuteToggle.uncheck()
  await main.getByRole("button", {name: "Save commute preferences", exact: true}).click()
  await expect(main.getByText("Commute matching paused.", {exact: true})).toBeVisible()
  await main.getByRole("button", {name: "Offer a ride", exact: true}).click()
  const smokeRideOrigin = `Smoke origin ${suffix}`
  const rideDialog = page.getByRole("dialog")
  await rideDialog.getByLabel("From").fill(smokeRideOrigin)
  await rideDialog.getByLabel("To").fill(`Smoke destination ${suffix}`)
  await rideDialog.getByLabel("Departure").fill("2099-07-20T10:00")
  await rideDialog.getByLabel("Passenger seats").fill("1")
  await rideDialog.getByRole("button", {name: "Offer ride", exact: true}).click()
  await expect(main.getByText("Ride offered.", {exact: true})).toBeVisible()
  const rideCard = main.locator("article").filter({hasText: smokeRideOrigin})
  await expect(rideCard).toBeVisible()
  const contactRideContext = await browser.newContext({recordHar: {path: "./web-runtime-smoke-rides.har", mode: "minimal"}})
  const contactRidePage = await contactRideContext.newPage()
  await contactRidePage.goto(`${baseURL}/login`, {waitUntil: "networkidle"})
  await contactRidePage.getByLabel("Email").fill(contact.email)
  await contactRidePage.getByLabel("Password").fill(contact.password)
  await contactRidePage.getByRole("button", {name: "Enter"}).click()
  await contactRidePage.waitForURL(/\/vision$/)
  await contactRidePage.goto(`${baseURL}/rides`, {waitUntil: "networkidle"})
  const contactRideCard = contactRidePage.locator("article").filter({hasText: smokeRideOrigin})
  await expect(contactRideCard).toBeVisible()
  await contactRideCard.getByRole("button", {name: "Join", exact: true}).click()
  await expect(contactRidePage.getByText("Joined ride.", {exact: true})).toBeVisible()
  await contactRideContext.close()
  await visit("/rides/mine")
  const ownedRideCard = main.locator("article").filter({hasText: smokeRideOrigin})
  await expect(ownedRideCard.getByText(/1\/1 seats/)).toBeVisible()
  await expect(ownedRideCard.getByText("FULL", {exact: true})).toBeVisible()
  await ownedRideCard.getByRole("button", {name: "Start", exact: true}).click()
  await expect(main.getByText("Ride started.", {exact: true})).toBeVisible()
  await ownedRideCard.getByRole("button", {name: "Complete", exact: true}).click()
  await expect(main.getByText("Ride completed.", {exact: true})).toBeVisible()

  await visit("/vision")
  const rideVisionInput = page.locator("textarea.vision-console__input")
  await rideVisionInput.fill(`offer a ride from Vision origin ${suffix} to Vision destination ${suffix} at 2099-07-20T10:00:00Z seats 1`)
  const rideVisionReviewResponse = page.waitForResponse(response => response.url().includes("/vision/conversations/turns") && response.request().method() === "POST")
  await rideVisionInput.press("Enter")
  await rideVisionReviewResponse
  await expect(page.locator(".vision-console__line--agent")).toContainText(/offer a ride|confirm/i, {timeout: 15000})
  await page.getByRole("button", {name: "Confirm and create", exact: true}).click()
  await expect(page.locator(".vision-console__line--agent")).toContainText(/ride action completed successfully|future time|route|execution is disabled/i, {timeout: 15000})

  await visit("/activity")
  const continueActivity = main.getByRole("link", {name: "Continue", exact: true}).first()
  await expect(continueActivity).toBeVisible()
  const dismissedActivityHref = await continueActivity.getAttribute("href")
  if (!dismissedActivityHref) throw new Error("Activity resume row did not expose a safe route")
  const activityRow = continueActivity.locator("xpath=ancestor::article")
  await activityRow.getByRole("button", {name: "Dismiss", exact: true}).click()
  await visit("/activity")
  await expect(main.locator(`a[href="${dismissedActivityHref}"]`)).toHaveCount(0)

  const requiredPaths = ["/dashboard/me", "/chat/conversations", "/circles/groups", "/business/profiles", "/rides/offers"]
  for (const requiredPath of requiredPaths) {
    const matching = statuses.filter((item) => item.path === requiredPath)
    if (matching.length === 0 || matching.every((item) => item.status >= 400)) {
      throw new Error(`Runtime smoke did not observe a successful ${requiredPath} response: ${JSON.stringify(matching)}`)
    }
  }

  result = "passed"
  console.log(JSON.stringify({result, account: {username: account.username}, visited: ["/work/find", "/work/quests/new", "/chat", "/circles", "/people", "/business/find", "/business/profile", "/business/offerings", "/business/calendar", "/business/public/:slug", "/calendar", "/rides", "/rides/mine", "/vision (rides review/confirm)"], statuses}, null, 2))
} catch (error) {
  failureMessage = error instanceof Error ? error.message : String(error)
  throw error
} finally {
  if (runtimeEvidencePath) {
    await mkdir(dirname(runtimeEvidencePath), {recursive: true})
    await writeFile(runtimeEvidencePath, `${JSON.stringify({
      result,
      startedAt: runStartedAt,
      completedAt: new Date().toISOString(),
      baseURL,
      failureMessage,
      browserDiagnostics,
      screenshots: [desktopScreenshotPath, narrowScreenshotPath, workPreviewScreenshotPath, thingsPreviewScreenshotPath, questDetailScreenshotPath, thingDetailNarrowScreenshotPath, personalAttentionDesktopScreenshotPath, pinVisibilityRecoveryScreenshotPath, commandCenterDesktopScreenshotPath, commandFocusSuppressionScreenshotPath, chatDesktopScreenshotPath, chatNarrowScreenshotPath, visionIdleScreenshotPath, visionReviewScreenshotPath, businessBookingsDesktopScreenshotPath, ridesDesktopScreenshotPath, finalVisionScreenshotPath].filter(Boolean),
      personalAttention,
      commandCenter,
      chatReconnect,
      visionHandoff,
      crossModuleWorkspace,
      statuses
    }, null, 2)}\n`)
  }
  await context.close()
  await browser.close()
}
