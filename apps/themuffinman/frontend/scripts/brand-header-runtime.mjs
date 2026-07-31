import {chromium} from "playwright"
import {attachBrowserErrorCollection, authenticateSeededUser, defaultRuntimeViewports, inspectOverflow, runtimeEvidenceRoot, runtimeUrls, withBrowser, writeRuntimeEvidence} from "./runtime-harness.mjs"

const {baseUrl, apiUrl} = runtimeUrls()
const evidenceRoot = runtimeEvidenceRoot()
const result = {evidenceVersion: "brand-header-sand-v1", capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", result: "passed", viewports: [], browserErrors: []}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    for (const {name, width, height} of defaultRuntimeViewports) {
      const page = await browser.newPage({viewport: {width, height}, reducedMotion: "reduce"})
      attachBrowserErrorCollection(page, result, name)
      await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
      await authenticateSeededUser(page, apiUrl)
      await page.goto(`${baseUrl}/home`, {waitUntil: "networkidle"})
      const header = page.locator(".app-shell__brand-header")
      const logo = page.locator(".app-shell__brand-logo")
      const rail = page.locator(".app-shell__rail")
      const {overflowFree, dimensions} = await inspectOverflow(page)
      const screenshot = `${evidenceRoot}brand-header-sand-${name}.png`
      await page.screenshot({path: screenshot, fullPage: false})
      result.viewports.push({name, width, height, headerVisible: await header.isVisible(), logoVisible: await logo.isVisible(), sidebarVisible: width > 980 ? await rail.isVisible() : true, overflowFree, dimensions, screenshot})
      await page.close()
    }
  })
  if (result.browserErrors.length || result.viewports.some(viewport => !viewport.headerVisible || !viewport.logoVisible || !viewport.sidebarVisible || !viewport.overflowFree)) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  writeRuntimeEvidence(`${evidenceRoot}brand-header-sand-runtime.json`, result)
}

if (result.result !== "passed") process.exitCode = 1
