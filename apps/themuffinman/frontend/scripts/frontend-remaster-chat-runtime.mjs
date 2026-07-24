import {chromium} from "playwright"
import fs from "node:fs"
const baseUrl = "http://localhost:5173"
const evidencePath = new URL("../../../../docs/runtime-evidence/frontend-remaster-chat-2026-07-23.json", import.meta.url).pathname
const browser = await chromium.launch({headless: true})
const errors = []
const result = {evidenceVersion: "frontend-remaster-chat-v3", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: errors, result: "passed"}
try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", e => errors.push(e.message))
  const auth = await page.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const user = await auth.json()
  await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, user)
  await page.goto(`${baseUrl}/chat`, {waitUntil: "networkidle", timeout: 30000})
  const conversationIndex = page.getByRole("complementary", {name: "Conversations"})
  const realtimeText = await page.getByRole("status").first().textContent()
  result.scenarios.chatLanding = {status: new URL(page.url()).pathname === "/chat" && Boolean(realtimeText?.includes("Realtime")) && await conversationIndex.count() === 1 ? "passed" : "failed", route: new URL(page.url()).pathname, realtimeStatus: realtimeText, hasConversations: await conversationIndex.count() === 1}
  await page.getByRole("button", {name: "New chat"}).click()
  result.scenarios.newChatEntry = {status: await page.getByLabel("Find someone to chat with").count() === 1 ? "passed" : "failed"}
  const desktopConversation = page.locator(".chat-surface__conversation").first()
  if (await desktopConversation.count() === 0) throw new Error("No conversation row available for desktop selection.")
  const desktopHref = await desktopConversation.getAttribute("href")
  await Promise.all([page.waitForURL(/\/chat\/\d+$/), desktopConversation.click()])
  await page.waitForLoadState("networkidle")
  const conversationRoute = new URL(page.url()).pathname
  const hasComposer = await page.getByRole("form", {name: "Conversation composer"}).count() === 1
  const hasBack = await page.getByRole("link", {name: "Back to Chat"}).count() === 1
  result.scenarios.conversationSelection = {status: conversationRoute.startsWith("/chat/") && hasComposer && hasBack ? "passed" : "failed", route: conversationRoute, href: desktopHref, hasComposer, hasBack}
  await Promise.all([page.waitForURL(/\/chat$/), page.getByRole("link", {name: "Back to Chat"}).click()])
  await page.waitForLoadState("networkidle")
  result.scenarios.conversationBack = {status: new URL(page.url()).pathname === "/chat" ? "passed" : "failed", route: new URL(page.url()).pathname}
  await page.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-chat-1440.png", import.meta.url).pathname, fullPage: true})
  const mobile = await browser.newPage({viewport: {width: 390, height: 844}, reducedMotion: "reduce"})
  mobile.on("pageerror", e => errors.push(e.message))
  const mobileAuth = await mobile.request.post("http://localhost:8080/auth/login", {data: {email: "test@test.com", password: "test123"}})
  const mobileUser = await mobileAuth.json()
  await mobile.goto(`${baseUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await mobile.evaluate(v => {localStorage.setItem("user", JSON.stringify(v)); localStorage.setItem("token", v.token)}, mobileUser)
  await mobile.goto(`${baseUrl}/chat`, {waitUntil: "networkidle", timeout: 30000})
  const mobileConversation = mobile.locator(".chat-surface__conversation").first()
  if (await mobileConversation.count()) {
    await Promise.all([mobile.waitForURL(/\/chat\/\d+$/), mobileConversation.click()])
    await mobile.waitForLoadState("networkidle")
    const mobilePath = new URL(mobile.url()).pathname
    const mobileBack = await mobile.getByRole("link", {name: "Back to Chat"}).count() === 1
    result.scenarios.mobileConversation = {status: mobilePath.startsWith("/chat/") && mobileBack ? "passed" : "failed", route: mobilePath, hasBack: mobileBack}
    await Promise.all([mobile.waitForURL(/\/chat$/), mobile.getByRole("link", {name: "Back to Chat"}).click()])
    await mobile.waitForLoadState("networkidle")
  } else {
    result.scenarios.mobileConversation = {status: "failed", reason: "No conversation row available for mobile selection."}
  }
  await mobile.screenshot({path: new URL("../../../../docs/runtime-evidence/frontend-remaster-chat-390.png", import.meta.url).pathname, fullPage: true})
  const widths = await mobile.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  result.scenarios.mobileChat = {status: widths.document <= widths.viewport && widths.body <= widths.viewport ? "passed" : "failed", widths}
  result.scenarios.browserErrors = {status: errors.length ? "failed" : "passed", errors}
  if (Object.values(result.scenarios).some(scenario => scenario?.status === "failed")) result.result = "failed"
} catch (e) { result.result = "failed"; result.failure = e instanceof Error ? e.message : String(e) }
finally { result.browserErrors = errors; fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`); await browser.close() }
if (result.result !== "passed") process.exitCode = 1
