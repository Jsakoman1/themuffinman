import {chromium} from "playwright"
import {attachBrowserErrorCollection, authenticateSeededUser, inspectOverflow, runtimeEvidenceRoot, runtimeUrls, withBrowser, writeRuntimeEvidence} from "./runtime-harness.mjs"

const {baseUrl, apiUrl} = runtimeUrls()
const mode = process.env.BUSINESS_RESOURCE_RUNTIME_MODE === "mobile" ? "mobile" : "desktop"
const viewport = mode === "mobile" ? {width: 390, height: 844} : {width: 1440, height: 1000}
const evidenceRoot = runtimeEvidenceRoot()
const evidencePath = `${evidenceRoot}business-resource-owner-${mode}-runtime.json`
const screenshotPath = `${evidenceRoot}business-resource-owner-${mode}.png`
const suffix = `${mode}-${Date.now()}`
const names = {pool: `Runtime rooms ${suffix}`, poolUpdated: `Runtime treatment rooms ${suffix}`, resource: `Runtime room ${suffix}`, resourceUpdated: `Runtime room A ${suffix}`}
const keys = {pool: `runtime-pool-${suffix}`, resource: `runtime-resource-${suffix}`}
const result = {evidenceVersion: "business-resource-owner-runtime-v1", capturedAt: new Date().toISOString(), mode, viewport, scenarios: {}, browserErrors: [], result: "passed"}

try {
  await withBrowser(options => chromium.launch(options), {headless: true}, async browser => {
    const page = await browser.newPage({viewport, reducedMotion: "reduce"})
    attachBrowserErrorCollection(page, result, mode)
    await page.goto(`${baseUrl}/login`, {waitUntil: "networkidle"})
    const user = await authenticateSeededUser(page, apiUrl)
    const headers = {Authorization: `Bearer ${user.token}`}
    const profilesResponse = await page.request.get(`${apiUrl}/business/profiles/me/all`, {headers})
    const profiles = await profilesResponse.json()
    const businessId = profiles[0]?.id
    if (!profilesResponse.ok() || !businessId) throw new Error("Seeded owner business profile is required")
    const offeringsResponse = await page.request.get(`${apiUrl}/business/offerings/me`, {headers, params: {businessProfileId: String(businessId)}})
    const offerings = await offeringsResponse.json()
    const offeringId = offerings.items?.[0]?.id
    if (!offeringsResponse.ok() || !offeringId) throw new Error("Seeded owner offering is required")

    const cleanup = async () => {
      const response = await page.request.get(`${apiUrl}/business/resources/profile/${businessId}/me`, {headers})
      if (!response.ok()) return
      const configuration = await response.json()
      for (const requirement of configuration.requirements.filter(item => item.businessOfferingId === offeringId && item.resourceType === "ROOM")) {
        if (configuration.pools.some(pool => pool.id === requirement.resourcePoolId && pool.poolKey === keys.pool)) await page.request.delete(`${apiUrl}/business/resources/profile/${businessId}/requirements/${requirement.id}/me`, {headers})
      }
      for (const resource of configuration.resources.filter(item => item.resourceKey === keys.resource)) await page.request.delete(`${apiUrl}/business/resources/profile/${businessId}/resources/${resource.id}/me`, {headers})
      for (const pool of configuration.pools.filter(item => item.poolKey === keys.pool)) await page.request.delete(`${apiUrl}/business/resources/profile/${businessId}/pools/${pool.id}/me`, {headers})
    }

    await cleanup()
    try {
      await page.goto(`${baseUrl}/business/service-setup?businessId=${businessId}&offeringId=${offeringId}`, {waitUntil: "networkidle", timeout: 30000})
      await page.getByRole("button", {name: "Step 5: Resources", exact: true}).click()
      await page.getByTestId("business-resource-editor").waitFor({state: "visible", timeout: 10000})

      const poolForm = page.getByTestId("add-resource-pool-form")
      await poolForm.getByLabel("Pool name").fill(names.pool)
      await poolForm.getByLabel("Internal key").fill(keys.pool)
      await poolForm.getByLabel("Type").fill("ROOM")
      await poolForm.getByLabel("Available at once").fill("2")
      await poolForm.getByLabel("Customer-facing name").fill("Treatment room")
      await poolForm.getByRole("button", {name: "Add pool"}).click()
      await page.getByText("Pool added.", {exact: true}).waitFor()
      let configuration = await (await page.request.get(`${apiUrl}/business/resources/profile/${businessId}/me`, {headers})).json()
      const poolId = configuration.pools.find(item => item.poolKey === keys.pool)?.id
      if (!poolId) throw new Error("Created pool was not returned by the owner contract")

      let poolCard = page.getByTestId(`resource-pool-${poolId}`)
      await poolCard.getByRole("button", {name: "Edit"}).click()
      await poolCard.getByLabel("Pool name").fill(names.poolUpdated)
      await poolCard.getByRole("button", {name: "Save pool"}).click()
      await page.getByText("Pool updated.", {exact: true}).waitFor()

      const resourceForm = page.getByTestId("add-resource-form")
      await resourceForm.getByLabel("Resource name").fill(names.resource)
      await resourceForm.getByLabel("Internal key").fill(keys.resource)
      await resourceForm.getByLabel("Type").fill("ROOM")
      await resourceForm.getByLabel("Pool").selectOption(String(poolId))
      await resourceForm.getByLabel("Customer-facing name").fill("Private room")
      await resourceForm.getByRole("button", {name: "Add resource"}).click()
      await page.getByText("Resource added.", {exact: true}).waitFor()
      configuration = await (await page.request.get(`${apiUrl}/business/resources/profile/${businessId}/me`, {headers})).json()
      const resourceId = configuration.resources.find(item => item.resourceKey === keys.resource)?.id
      if (!resourceId) throw new Error("Created resource was not returned by the owner contract")

      let resourceCard = page.getByTestId(`resource-${resourceId}`)
      await resourceCard.getByRole("button", {name: "Edit"}).click()
      await resourceCard.getByLabel("Resource name").fill(names.resourceUpdated)
      await resourceCard.getByRole("button", {name: "Save resource"}).click()
      await page.getByText("Resource updated.", {exact: true}).waitFor()

      const requirementForm = page.getByTestId("add-resource-requirement-form")
      await requirementForm.getByLabel("Pool").selectOption(String(poolId))
      await requirementForm.getByLabel("Type").fill("ROOM")
      await requirementForm.getByLabel("How many").fill("1")
      await requirementForm.getByRole("button", {name: "Add requirement"}).click()
      await page.getByText("Service requirement added.", {exact: true}).waitFor()
      configuration = await (await page.request.get(`${apiUrl}/business/resources/profile/${businessId}/me`, {headers})).json()
      const requirementId = configuration.requirements.find(item => item.businessOfferingId === offeringId && item.resourcePoolId === poolId && item.resourceType === "ROOM")?.id
      if (!requirementId) throw new Error("Created requirement was not returned by the owner contract")

      const requirementCard = page.getByTestId(`resource-requirement-${requirementId}`)
      await requirementCard.getByRole("button", {name: "Edit"}).click()
      await requirementCard.getByLabel("How many").fill("2")
      await requirementCard.getByRole("button", {name: "Save requirement"}).click()
      await page.getByText("Service requirement updated.", {exact: true}).waitFor()

      resourceCard = page.getByTestId(`resource-${resourceId}`)
      await resourceCard.getByRole("button", {name: "Deactivate"}).click()
      await page.getByText("Resource deactivated. Existing booking history is unchanged.", {exact: true}).waitFor()
      await resourceCard.getByText("Inactive", {exact: true}).waitFor()
      const configuredOverflow = await inspectOverflow(page)
      await page.screenshot({path: screenshotPath, fullPage: false})

      await resourceCard.getByRole("button", {name: "Activate"}).click()
      await page.getByText("Resource activated.", {exact: true}).waitFor()
      await requirementCard.getByRole("button", {name: "Remove"}).click()
      await page.getByRole("dialog").getByRole("button", {name: "Continue"}).click()
      await page.getByText("Service requirement removed.", {exact: true}).waitFor()
      await resourceCard.getByRole("button", {name: "Delete"}).click()
      await page.getByRole("dialog").getByRole("button", {name: "Continue"}).click()
      await page.getByText("Resource deleted.", {exact: true}).waitFor()
      poolCard = page.getByTestId(`resource-pool-${poolId}`)
      await poolCard.getByRole("button", {name: "Delete"}).click()
      await page.getByRole("dialog").getByRole("button", {name: "Continue"}).click()
      await page.getByText("Pool deleted.", {exact: true}).waitFor()

      const finalResponse = await page.request.get(`${apiUrl}/business/resources/profile/${businessId}/me`, {headers})
      const finalConfiguration = await finalResponse.json()
      const cleaned = finalResponse.ok()
        && !finalConfiguration.pools.some(item => item.id === poolId)
        && !finalConfiguration.resources.some(item => item.id === resourceId)
        && !finalConfiguration.requirements.some(item => item.id === requirementId)
      const finalOverflow = await inspectOverflow(page)
      result.scenarios.ownerResourceLifecycle = {
        status: configuredOverflow.overflowFree && finalOverflow.overflowFree && cleaned ? "passed" : "failed",
        route: new URL(page.url()).pathname,
        businessId,
        offeringId,
        operations: ["create", "edit", "deactivate", "activate", "delete"],
        entities: ["pool", "resource", "offering requirement"],
        configuredDimensions: configuredOverflow.dimensions,
        finalDimensions: finalOverflow.dimensions,
        cleanupVerified: cleaned,
      }
    } finally {
      await cleanup()
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
