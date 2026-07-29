import { chromium } from "playwright"
import fs from "node:fs"

const frontendUrl = "http://localhost:5173"
const backendUrl = "http://localhost:8080"
const evidencePath = process.env.WEB_RUNTIME_EVIDENCE_PATH
if (!evidencePath) throw new Error("WEB_RUNTIME_EVIDENCE_PATH is required")
const screenshotPath = process.env.WEB_VISUAL_EVIDENCE_PATH
const browser = await chromium.launch({headless: true})
const result = {capturedAt: new Date().toISOString(), browser: "Playwright Chromium headless", scenarios: {}, browserErrors: [], result: "passed"}
const browserErrors = []

try {
  const page = await browser.newPage({viewport: {width: 1440, height: 1000}, reducedMotion: "reduce"})
  page.on("pageerror", error => browserErrors.push(error.message))
  const authResponse = await page.request.post(`${backendUrl}/auth/login`, {data: {email: "test@test.com", password: "test123"}})
  const auth = await authResponse.json()
  if (authResponse.status() !== 200 || !auth.token) throw new Error("test owner authentication failed")
  const headers = {Authorization: `Bearer ${auth.token}`}
  const profilesResponse = await page.request.get(`${backendUrl}/business/profiles/me/all`, {headers})
  const profiles = await profilesResponse.json()
  const businessId = profiles[0]?.id
  if (profilesResponse.status() !== 200 || !businessId) throw new Error("test owner business profile is required")
  await page.goto(`${frontendUrl}/login`, {waitUntil: "networkidle", timeout: 30000})
  await page.evaluate(user => {
    localStorage.setItem("user", JSON.stringify(user))
    localStorage.setItem("token", user.token)
  }, auth)

  if (evidencePath.includes("booking-completion")) {
    const slug = "runtime-flexible-services"
    const date = "2026-08-01"
    const publicPageResponse = await page.request.get(`${backendUrl}/business/public/${slug}`, {headers})
    const publicPage = await publicPageResponse.json()
    if (!publicPageResponse.ok()) throw new Error("public business page is required for lifecycle runtime")
    const offerings = publicPage.offerings ?? []
    let requestOffering = offerings.find(item => item.bookingMode === "REQUEST")
    let restoreOffering = null
    if (!requestOffering) {
      const ownerOfferingsResponse = await page.request.get(`${backendUrl}/business/offerings/me`, {headers, params: {businessProfileId: String(businessId)}})
      const ownerOfferings = await ownerOfferingsResponse.json()
      const ownerOffering = ownerOfferings.items?.find(item => item.id === offerings[0]?.id)
      if (!ownerOfferingsResponse.ok() || !ownerOffering) throw new Error("an owner offering is required to prepare lifecycle runtime")
      const requestPayload = {...ownerOffering, bookingMode: "REQUEST", requiresOwnerConfirmation: true}
      const updateResponse = await page.request.put(`${backendUrl}/business/offerings/${ownerOffering.id}/me`, {headers, data: requestPayload})
      if (!updateResponse.ok()) throw new Error(`could not prepare request-based offering (${updateResponse.status()}): ${await updateResponse.text()}`)
      restoreOffering = ownerOffering
      requestOffering = {...offerings[0], bookingMode: "REQUEST"}
    }
    if (!requestOffering?.id) throw new Error("a request-based offering is required for owner confirmation lifecycle runtime")
    const offeringId = requestOffering.id
    const availabilityResponse = await page.request.get(`${backendUrl}/business/public/${slug}/availability/date`, {headers, params: {offeringId, date}})
    const availability = await availabilityResponse.json()
    if (!availabilityResponse.ok() || !availability.items?.length) throw new Error("a request-based offering with date availability is required for lifecycle runtime")
    const customerId = `runtime-customer-${Date.now()}`
    const registerResponse = await page.request.post(`${backendUrl}/auth/register`, {data: {email: `${customerId}@example.com`, username: customerId, password: "runtime-pass-123"}})
    const customer = await registerResponse.json()
    if (registerResponse.status() !== 200 || !customer.token) throw new Error("runtime customer registration failed")
    await page.evaluate(user => {
      localStorage.setItem("user", JSON.stringify(user))
      localStorage.setItem("token", user.token)
    }, customer)
    await page.goto(`${frontendUrl}/business/public/${slug}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: "Book", exact: true}).nth(offerings.findIndex(item => item.id === offeringId)).click()
    await page.locator(".booking-flow").waitFor({state: "visible", timeout: 10000})
    const requiredDetails = page.locator(".demand-field input[required]")
    for (let index = 0; index < await requiredDetails.count(); index++) await requiredDetails.nth(index).fill("Runtime completion")
    const options = page.locator(".booking-options input[type='checkbox']")
    for (let index = 0; index < await options.count(); index++) await options.nth(index).check()
    await page.getByRole("button", {name: "Continue"}).click()
    await page.getByLabel("Date").fill(date)
    await page.getByLabel("Date").dispatchEvent("change")
    const slot = page.locator(".availability-picker__slot").first()
    await slot.waitFor({state: "visible", timeout: 10000})
    await slot.click()
    await page.getByRole("button", {name: "Continue"}).click()
    await page.getByRole("heading", {name: "Review request"}).waitFor({state: "visible", timeout: 10000})
    const bookingResponse = page.waitForResponse(response => response.url().endsWith("/business/bookings") && response.request().method() === "POST")
    await page.getByRole("button", {name: "Send request"}).click()
    const bookingApiResponse = await bookingResponse
    const createdBooking = bookingApiResponse.ok() ? await bookingApiResponse.json() : null
    await Promise.race([
      page.locator(".booking-completion").waitFor({state: "visible", timeout: 10000}),
      page.locator(".app-status--error").waitFor({state: "visible", timeout: 10000})
    ])
    if (await page.locator(".app-status--error").count()) throw new Error(`booking request failed (${bookingApiResponse.status()}): ${await bookingApiResponse.text()}`)
    if (!createdBooking?.id) throw new Error("booking create response did not return an id")
    result.scenarios.bookingCompletion = {
      status: await page.getByText("View my bookings").count() === 1
        && await page.getByText("You can review the booking, its confirmation status").count() === 1 ? "passed" : "failed",
      slug,
      offeringId,
      date
    }

    const confirmedResponse = await page.request.post(`${backendUrl}/business/bookings/owner/${createdBooking.id}/confirm`, {headers})
    const confirmedBooking = confirmedResponse.ok() ? await confirmedResponse.json() : null
    if (!confirmedResponse.ok() || confirmedBooking?.status !== "CONFIRMED") {
      throw new Error(`owner confirmation failed (${confirmedResponse.status()}): ${await confirmedResponse.text()}`)
    }

    const customerHeaders = {Authorization: `Bearer ${customer.token}`}
    const customerBookingResponse = await page.request.get(`${backendUrl}/business/bookings/me/${createdBooking.id}`, {headers: customerHeaders})
    const customerBooking = customerBookingResponse.ok() ? await customerBookingResponse.json() : null
    if (!customerBookingResponse.ok() || customerBooking?.status !== "CONFIRMED") {
      throw new Error(`customer did not observe confirmed booking (${customerBookingResponse.status()}): ${await customerBookingResponse.text()}`)
    }

    const from = new Date(new Date(createdBooking.startsAt).getTime() - 60 * 60 * 1000).toISOString()
    const to = new Date(new Date(createdBooking.endsAt).getTime() + 60 * 60 * 1000).toISOString()
    const calendarResponse = await page.request.get(`${backendUrl}/business/bookings/owner/calendar`, {
      headers,
      params: {businessProfileId: String(businessId), from, to}
    })
    const calendar = calendarResponse.ok() ? await calendarResponse.json() : null
    const calendarBooking = calendar?.days?.flatMap(day => day.items ?? []).find(item => item.bookingId === createdBooking.id)
    if (!calendarResponse.ok() || calendarBooking?.status !== "CONFIRMED") {
      throw new Error(`owner calendar did not observe confirmed booking (${calendarResponse.status()}): ${await calendarResponse.text()}`)
    }
    result.scenarios.ownerConfirmationLifecycle = {
      status: "passed",
      bookingId: createdBooking.id,
      ownerStatus: confirmedBooking.status,
      customerStatus: customerBooking.status,
      calendarStatus: calendarBooking.status,
      calendarRange: {from, to}
    }
    if (restoreOffering) {
      const restoreResponse = await page.request.put(`${backendUrl}/business/offerings/${restoreOffering.id}/me`, {headers, data: restoreOffering})
      if (!restoreResponse.ok()) throw new Error(`could not restore runtime offering (${restoreResponse.status()}): ${await restoreResponse.text()}`)
    }
  } else if (evidencePath.includes("public-booking")) {
    const slug = "runtime-flexible-services"
    const offeringId = 112
    const date = "2026-08-01"
    const availabilityResponse = await page.request.get(`${backendUrl}/business/public/${slug}/availability/date`, {headers, params: {offeringId, date}})
    const availabilityPayload = await availabilityResponse.json()
    if (availabilityResponse.status() !== 200 || !availabilityPayload.items?.length) throw new Error("public date availability is required")
    await page.goto(`${frontendUrl}/business/public/${slug}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: "Book", exact: true}).first().click()
    await page.locator(".booking-flow").waitFor({state: "visible", timeout: 10000})
    const requiredDetails = page.locator(".demand-field input[required]")
    for (let index = 0; index < await requiredDetails.count(); index++) await requiredDetails.nth(index).fill("Runtime detail")
    await page.getByRole("button", {name: "Continue"}).click()
    await page.getByRole("heading", {name: "Choose a date and time"}).waitFor({state: "visible", timeout: 10000})
    await page.getByLabel("Date").fill(date)
    await page.getByLabel("Date").dispatchEvent("change")
    await page.getByRole("heading", {name: "Available times"}).waitFor({state: "visible", timeout: 10000})
    result.scenarios.publicDateFirstBooking = {
      status: await page.locator(".booking-panel input[type='date']").count() === 1
        && await page.locator(".availability-picker__slot").count() > 0
        && await page.getByText(/Times are shown in/).count() >= 1 ? "passed" : "failed",
      slug,
      offeringId,
      date,
      apiSlots: availabilityPayload.items.length
    }
    if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})
  } else if (evidencePath.includes("hours")) {
    await page.goto(`${frontendUrl}/business/settings?businessId=${businessId}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: /Working hours When people can book/}).click()
    await page.getByRole("button", {name: "Add working hours"}).click()
    await page.getByRole("heading", {name: "Add working hours"}).waitFor({state: "visible", timeout: 10000})
    const hoursDialog = page.getByRole("dialog")
    const simpleHours = await hoursDialog.getByLabel("Open from").count() === 1
      && await hoursDialog.getByLabel("Appointment spacing (minutes)").count() === 1
      && await hoursDialog.getByText("More scheduling options").count() === 1
    await page.getByRole("button", {name: "Cancel"}).click()
    await page.getByRole("link", {name: "Special dates"}).click()
    await page.getByRole("heading", {name: "Special dates"}).first().waitFor({state: "visible", timeout: 10000})
    await page.getByRole("button", {name: "Add special date"}).click()
    result.scenarios.workingHours = {
      status: simpleHours
        && await page.getByLabel("What changes?").count() === 1
        && await page.getByText("Closed — customers cannot book").count() === 1 ? "passed" : "failed",
      businessId
    }
    if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})
  } else if (evidencePath.includes("settings")) {
    await page.goto(`${frontendUrl}/business/settings?businessId=${businessId}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: /Public page What customers see/}).waitFor({state: "visible", timeout: 10000})
    await page.getByRole("button", {name: /Working hours When people can book/}).click()
    await page.locator(".business-profile__section-card > .business-profile__section-heading").getByRole("heading", {name: "Working hours"}).waitFor({state: "visible", timeout: 10000})
    await page.getByRole("button", {name: /Booking rules How requests are handled/}).click()
    await page.getByRole("heading", {name: "Booking rules"}).waitFor({state: "visible", timeout: 10000})
    await page.getByRole("button", {name: "Save booking rules"}).click()
    await page.getByText("Booking rules updated.").waitFor({state: "visible", timeout: 10000})
    result.scenarios.settingsSections = {
      status: await page.locator(".business-settings-nav button").count() === 4
        && await page.getByText("Confirm each request myself").count() === 1
        && await page.getByRole("button", {name: "Save booking rules"}).count() === 1
        && await page.getByText("Booking rules updated.").count() === 1 ? "passed" : "failed",
      businessId
    }
    if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})
  } else if (evidencePath.includes("advanced-service")) {
    const offeringsResponse = await page.request.get(`${backendUrl}/business/offerings/me`, {headers, params: {businessProfileId: businessId}})
    const offerings = await offeringsResponse.json()
    const offeringId = offerings.items?.[0]?.id
    if (offeringsResponse.status() !== 200 || !offeringId) throw new Error("test owner offering is required")
    await page.goto(`${frontendUrl}/business/service-setup?businessId=${businessId}&offeringId=${offeringId}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: "Customer questions"}).click()
    await page.getByRole("heading", {name: "Customer questions"}).waitFor({state: "visible", timeout: 10000})
    const addQuestion = page.getByRole("button", {name: "Add customer question"})
    await addQuestion.click()
    result.scenarios.advancedService = {
      status: await page.getByText("Ask only information you genuinely need before the appointment.").count() === 1
        && await page.getByLabel("Question 1").count() === 1
        && await page.getByText("Optional choices").count() === 1 ? "passed" : "failed",
      businessId,
      offeringId
    }
    if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})
  } else if (evidencePath.includes("service-basics")) {
    await page.goto(`${frontendUrl}/business/offerings?businessId=${businessId}`, {waitUntil: "networkidle", timeout: 30000})
    await page.getByRole("button", {name: "New offering"}).click()
    await page.getByRole("heading", {name: "Start with the essentials"}).waitFor({state: "visible", timeout: 10000})
    result.scenarios.serviceBasics = {
      status: await page.getByLabel("Service name").count() === 1
        && await page.getByLabel("Short description").count() === 1
        && await page.locator(".offerings-surface__form-section").count() === 2
        && await page.locator("details.offerings-surface__advanced").count() === 1 ? "passed" : "failed",
      businessId
    }
    if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})
  } else {
  await page.goto(`${frontendUrl}/business`, {waitUntil: "networkidle", timeout: 30000})
  await page.getByRole("button", {name: "Add business"}).click()
  await page.getByRole("heading", {name: "Tell customers the essentials"}).waitFor({state: "visible", timeout: 10000})
  result.scenarios.createBusiness = {
    status: await page.getByLabel("Business name").count() === 1
      && await page.getByLabel("What do you offer?").count() === 1
      && await page.getByLabel("City or service area").count() === 1
      && await page.getByLabel("Your local time zone").inputValue() === "Europe/Zurich" ? "passed" : "failed"
  }
  if (screenshotPath) await page.screenshot({path: screenshotPath, fullPage: true})

  await page.getByRole("button", {name: "Cancel"}).click()
  await page.goto(`${frontendUrl}/business/profile?businessId=${businessId}`, {waitUntil: "networkidle", timeout: 30000})
  await page.getByRole("heading", {name: /Finish your first setup|Your business can accept bookings/}).waitFor({state: "visible", timeout: 10000})
  result.scenarios.ownerChecklist = {
    status: await page.getByText("Add a service").count() === 1 && await page.getByText("Set your working hours").count() === 1 ? "passed" : "failed",
    businessId
  }
  }
  result.scenarios.browserErrors = {status: browserErrors.length ? "failed" : "passed", errors: browserErrors}
  if (Object.values(result.scenarios).some(scenario => scenario.status === "failed")) result.result = "failed"
  await page.close()
} catch (error) {
  result.result = "failed"
  result.failure = error instanceof Error ? error.message : String(error)
} finally {
  result.browserErrors = browserErrors
  fs.writeFileSync(evidencePath, `${JSON.stringify(result, null, 2)}\n`)
  await browser.close()
}

if (result.result !== "passed") process.exitCode = 1
