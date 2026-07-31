import {chromium} from "playwright"
import {
  attachBrowserErrorCollection,
  authenticateSeededUser,
  inspectOverflow,
  runtimeEvidenceRoot,
  runtimeUrls,
  withBrowser,
  writeRuntimeEvidence,
} from "./runtime-harness.mjs"

const {baseUrl, apiUrl} = runtimeUrls()
const mode = process.env.SIDEJOBS_RUNTIME_MODE === "mobile" ? "mobile" : "desktop"
const viewport = mode === "mobile" ? {width: 390, height: 844} : {width: 1440, height: 1000}
const evidencePath = `${runtimeEvidenceRoot()}sidejobs-human-first-${mode}-runtime.json`
const result = {evidenceVersion: "sidejobs-human-first-runtime-v1", capturedAt: new Date().toISOString(), mode, viewport, scenarios: {}, browserErrors: [], result: "passed"}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    const page = await browser.newPage({viewport, reducedMotion: "reduce"})
    attachBrowserErrorCollection(page, result, mode)
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
    await authenticateSeededUser(page, apiUrl)

    for (const [name, route, expectedText] of [["discovery", "/work/find", "Find SideJobs"], ["posting", "/work/quests/new", "Post a SideJob"], ["requests", "/work/applications", "My help requests"]]) {
      await page.goto(`${baseUrl}${route}`, {waitUntil: "networkidle", timeout: 30000})
      const {overflowFree, dimensions} = await inspectOverflow(page)
      const textVisible = (await page.getByText(expectedText, {exact: true}).count()) > 0
      result.scenarios[name] = {status: textVisible && overflowFree ? "passed" : "failed", route: new URL(page.url()).pathname, expectedText, dimensions}
    }
    await page.close()
  })
  if (result.browserErrors.length || Object.values(result.scenarios).some(scenario => scenario.status !== "passed")) result.result = "failed"
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  writeRuntimeEvidence(evidencePath, result)
}
if (result.result !== "passed") process.exitCode = 1
