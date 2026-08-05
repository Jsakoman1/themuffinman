import assert from "node:assert/strict"
import fs from "node:fs"
import os from "node:os"
import path from "node:path"
import {attachBrowserErrorCollection, defaultRuntimeViewports, runtimeUrls, withBrowser, writeRuntimeEvidence} from "./runtime-harness.mjs"

assert.deepEqual(runtimeUrls({}), {baseUrl: "http://localhost:5173", apiUrl: "http://localhost:8080"})
assert.deepEqual(runtimeUrls({FRONTEND_BASE_URL: "http://web:4173", BACKEND_BASE_URL: "http://api:8081"}), {baseUrl: "http://web:4173", apiUrl: "http://api:8081"})
assert.deepEqual(defaultRuntimeViewports.map(({name}) => name), ["desktop", "mobile"])

let pageErrorListener
const result = {browserErrors: []}
attachBrowserErrorCollection({on(event, listener) { assert.equal(event, "pageerror"); pageErrorListener = listener }}, result, "desktop")
pageErrorListener(new Error("render failed"))
assert.deepEqual(result.browserErrors, ["desktop: render failed"])

const evidencePath = path.join(fs.mkdtempSync(path.join(os.tmpdir(), "runtime-harness-")), "evidence.json")
writeRuntimeEvidence(evidencePath, {result: "passed"})
assert.deepEqual(JSON.parse(fs.readFileSync(evidencePath, "utf8")), {result: "passed"})

const relativeEvidencePath = path.join("docs", "runtime-evidence", ".runtime-harness-self-test.json")
writeRuntimeEvidence(relativeEvidencePath, {result: "passed"})
assert.deepEqual(JSON.parse(fs.readFileSync(relativeEvidencePath, "utf8")), {result: "passed"})
fs.rmSync(relativeEvidencePath)

let closed = false
await assert.rejects(() => withBrowser(async () => ({close: async () => { closed = true }}), {headless: true}, async () => { throw new Error("expected") }), /expected/)
assert.equal(closed, true)

console.log("Runtime harness self-test passed (URLs, viewports, browser errors, relative evidence writing, and cleanup).")
