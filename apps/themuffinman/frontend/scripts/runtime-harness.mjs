import fs from "node:fs"
import pathModule from "node:path"

export const defaultRuntimeViewports = [
  {name: "desktop", width: 1440, height: 1000},
  {name: "mobile", width: 390, height: 844}
]

export function runtimeUrls(environment = process.env) {
  return {
    baseUrl: environment.FRONTEND_BASE_URL || "http://localhost:5173",
    apiUrl: environment.BACKEND_BASE_URL || "http://localhost:8080"
  }
}

export function runtimeEvidenceRoot() {
  return new URL("../../../../docs/runtime-evidence/", import.meta.url).pathname
}

export function attachBrowserErrorCollection(page, result, context = "browser") {
  page.on("pageerror", error => result.browserErrors.push(`${context}: ${error.message}`))
}

export async function authenticateSeededUser(page, apiUrl, credentials = {email: "test@test.com", password: "test123"}) {
  const login = await page.request.post(`${apiUrl}/auth/login`, {data: credentials})
  const user = await login.json()
  if (login.status() !== 200 || !user.token) throw new Error("Seeded authentication failed")
  await page.evaluate(value => {
    localStorage.setItem("user", JSON.stringify(value))
    localStorage.setItem("token", value.token)
  }, user)
  return user
}

export async function inspectOverflow(page) {
  const dimensions = await page.evaluate(() => ({viewport: innerWidth, document: document.documentElement.scrollWidth, body: document.body.scrollWidth}))
  return {overflowFree: dimensions.document <= dimensions.viewport && dimensions.body <= dimensions.viewport, dimensions}
}

export function writeRuntimeEvidence(path, evidence) {
  fs.mkdirSync(pathModule.dirname(path), {recursive: true})
  fs.writeFileSync(path, `${JSON.stringify(evidence, null, 2)}\n`)
}

export async function withBrowser(launchBrowser, options, callback) {
  const browser = await launchBrowser(options)
  try {
    return await callback(browser)
  } finally {
    await browser.close()
  }
}
