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
const viewportName = desktop ? "desktop" : "mobile"
const viewport = desktop ? {width: 1440, height: 1000} : {width: 390, height: 844}
const evidenceRoot = `docs/runtime-evidence/vision-direct-message-${viewportName}`
const evidencePath = `${evidenceRoot}-runtime.json`
const screenshotPath = `${evidenceRoot}.png`
const {baseUrl, apiUrl} = runtimeUrls()
const messageBody = `Disposable Vision runtime proof ${viewportName} ${Date.now()}`
const result = {
  capturedAt: new Date().toISOString(),
  environment: "workspace-owned local dev stack",
  browser: "Playwright Chromium headless",
  viewport: viewportName,
  browserErrors: [],
  scenarios: {},
  cleanup: {},
  result: "passed",
}

const authHeaders = token => ({Authorization: `Bearer ${token}`})

const submitVisionPrompt = async (page, prompt) => {
  const input = page.locator('.vision-web-host__composer input[aria-label="Ask Vision"]')
  await input.waitFor({state: "visible"})
  const responsePromise = page.waitForResponse(response => response.url().includes("/vision/conversations/turns") && response.request().method() === "POST")
  await input.fill(prompt)
  await input.press("Enter")
  const response = await responsePromise
  return {status: response.status(), payload: await response.json()}
}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    const page = await browser.newPage({viewport})
    attachBrowserErrorCollection(page, result, viewportName)
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
    const user = await authenticateSeededUser(page, apiUrl)
    const headers = authHeaders(user.token)
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle"})

    const recipientRequest = await submitVisionPrompt(page, "send a message")
    const recipientRequested = recipientRequest.status === 200
      && recipientRequest.payload.intent === "SEND_MESSAGE"
      && recipientRequest.payload.requestedSlot === "target_user"
      && recipientRequest.payload.nextAction === "ASK_FOR_SLOT"

    const recipient = await submitVisionPrompt(page, "admin")
    const recipientCollected = recipient.status === 200
      && recipient.payload.intent === "SEND_MESSAGE"
      && recipient.payload.requestedSlot === "message_body"
      && recipient.payload.nextAction === "ASK_FOR_SLOT"

    const review = await submitVisionPrompt(page, messageBody)
    const reviewReady = review.status === 200
      && review.payload.intent === "SEND_MESSAGE"
      && review.payload.nextAction === "SHOW_REVIEW"
      && review.payload.executionCandidate?.reviewReady === true
      && review.payload.executionCandidate?.confirmationRequired === true

    const confirmed = await submitVisionPrompt(page, "confirm")
    const executionSucceeded = confirmed.status === 200
      && confirmed.payload.intent === "SEND_MESSAGE"
      && confirmed.payload.nextAction === "COMPLETE"
      && /^Message sent to admin\.$/i.test(confirmed.payload.message ?? "")

    const conversationResponse = await page.request.get(`${apiUrl}/chat/conversations?query=admin&limit=20`, {headers})
    const conversationPayload = await conversationResponse.json()
    const conversation = (conversationPayload.conversations ?? []).find(item => item.otherUsername?.toLowerCase() === "admin")
    const messagesResponse = conversation
      ? await page.request.get(`${apiUrl}/chat/conversations/${conversation.conversationId}/messages?limit=50`, {headers})
      : null
    const messagesPayload = messagesResponse ? await messagesResponse.json() : {messages: []}
    const sentMessage = (messagesPayload.messages ?? []).find(message => message.messageBody === messageBody && message.ownMessage === true)
    const authoritativeReadback = conversationResponse.status() === 200
      && messagesResponse?.status() === 200
      && !!sentMessage

    await page.screenshot({path: screenshotPath, fullPage: true})
    const {overflowFree, dimensions} = await inspectOverflow(page)

    result.scenarios.directMessage = {
      status: recipientRequested && recipientCollected && reviewReady && executionSucceeded && authoritativeReadback && overflowFree ? "passed" : "failed",
      recipientRequested,
      recipientCollected,
      reviewReady,
      executionSucceeded,
      authoritativeReadback,
      overflowFree,
      dimensions,
      visionConversationId: confirmed.payload.conversationId ?? null,
      chatConversationId: conversation?.conversationId ?? null,
      sentMessageId: sentMessage?.id ?? null,
      executionMessage: confirmed.payload.message ?? null,
      stepMessages: [recipientRequest.payload.message, recipient.payload.message, review.payload.message, confirmed.payload.message],
      messageBody,
    }

    if (sentMessage && conversation) {
      const disposableMessages = (messagesPayload.messages ?? []).filter(message => message.ownMessage === true && message.messageBody?.startsWith("Disposable Vision runtime proof ") && !message.deleted)
      const deleteStatuses = []
      for (const disposableMessage of disposableMessages) {
        const deleteResponse = await page.request.delete(`${apiUrl}/chat/conversations/${conversation.conversationId}/messages/${disposableMessage.id}`, {headers})
        deleteStatuses.push(deleteResponse.status())
      }
      const cleanupReadback = await page.request.get(`${apiUrl}/chat/conversations/${conversation.conversationId}/messages?limit=50`, {headers})
      const cleanupPayload = await cleanupReadback.json()
      const cleanedMessages = (cleanupPayload.messages ?? []).filter(message => disposableMessages.some(disposable => disposable.id === message.id))
      result.cleanup = {
        status: deleteStatuses.length > 0 && deleteStatuses.every(status => status === 200) && cleanupReadback.status() === 200 && cleanedMessages.length === disposableMessages.length && cleanedMessages.every(message => message.deleted === true) ? "passed" : "failed",
        deleteStatuses,
        deletedCount: cleanedMessages.filter(message => message.deleted === true).length,
        readbackStatus: cleanupReadback.status(),
      }
    } else {
      result.cleanup = {status: "not_possible", reason: "sent message was not found"}
    }

    if (result.scenarios.directMessage.status !== "passed" || result.cleanup.status !== "passed" || result.browserErrors.length) result.result = "failed"
    await page.close()
  })
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  writeRuntimeEvidence(evidencePath, result)
}

if (result.result !== "passed") process.exitCode = 1
