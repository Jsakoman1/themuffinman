import {chromium} from "playwright"
import {attachBrowserErrorCollection, authenticateSeededUser, inspectOverflow, runtimeEvidenceRoot, runtimeUrls, withBrowser, writeRuntimeEvidence} from "./runtime-harness.mjs"

const {baseUrl, apiUrl} = runtimeUrls()
const evidenceRoot = runtimeEvidenceRoot()
const mode = process.env.VISUAL_PARITY_VIEWPORT === "mobile" ? "mobile" : "desktop"
const viewport = mode === "mobile" ? {width: 390, height: 844} : {width: 1440, height: 1000}
const routes = ["/home", "/work/find", "/calendar", "/profile", "/things", "/chat"]
const result = {evidenceVersion: "business-style-visual-parity-v1", capturedAt: new Date().toISOString(), mode, viewport, routes: [], browserErrors: [], result: "passed"}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    const page = await browser.newPage({viewport, reducedMotion: "reduce"})
    attachBrowserErrorCollection(page, result, mode)
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
    await authenticateSeededUser(page, apiUrl)
    for (const route of routes) {
      await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle"})
      const {overflowFree, dimensions} = await inspectOverflow(page)
      result.routes.push({route, overflowFree, dimensions})
    }
    await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle"})
    await page.screenshot({path: `${evidenceRoot}business-style-visual-parity-${mode}.png`, fullPage: false})
    await page.close()
  })
  if (result.browserErrors.length || result.routes.some(route => !route.overflowFree)) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  writeRuntimeEvidence(`${evidenceRoot}business-style-visual-parity-${mode}-runtime.json`, result)
}

if (result.result !== "passed") process.exitCode = 1
