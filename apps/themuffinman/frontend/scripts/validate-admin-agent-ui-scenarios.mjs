import {readFile} from "node:fs/promises"
import path from "node:path"
import {fileURLToPath} from "node:url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const fixturePath = path.resolve(__dirname, "./fixtures/admin-agent-ui-scenarios.json")
const generatedContractPath = path.resolve(__dirname, "../src/contracts/generated/themuffinmanContract.ts")

function readGeneratedStringArray(source, exportName) {
  const match = source.match(new RegExp(`export const ${exportName} = \\[(.*?)\\] as const`, "s"))
  if (!match) {
    throw new Error(`Missing generated contract array: ${exportName}`)
  }

  return JSON.parse(`[${match[1]}]`)
}

function buildSharedFlags(response) {
  return [
    ["translation_unreliable", !response.promptTranslationReliable],
    ["ambiguity", !!response.clarificationContract?.clarificationRequired],
    ["destructive_confirmation", !!response.executionReadiness?.destructiveConfirmationRequired],
    ["multi_actor", !!response.executionReadiness?.multiActorContextRequired],
    ["current_location", !!response.executionReadiness?.currentLocationCapabilityRequired]
  ]
}

function classifyScenario(testCase) {
  if (testCase.kind === "playground") {
    const activeFlags = buildSharedFlags(testCase.response)
      .filter(([, active]) => active)
      .map(([id]) => id)
    return {
      executionBlocked: activeFlags.length > 0,
      activeBlockingFlags: activeFlags
    }
  }

  const activeFlags = [
    ...buildSharedFlags({
      ...testCase.response,
      promptTranslationReliable: true
    }),
    ["simulation_not_safe", !testCase.response.safeToExecute]
  ]
    .filter(([, active]) => active)
    .map(([id]) => id)

  return {
    executionBlocked: !testCase.response.safeToExecute || activeFlags.length > 0,
    activeBlockingFlags: activeFlags
  }
}

function assertEqual(actual, expected, message) {
  const actualJson = JSON.stringify(actual)
  const expectedJson = JSON.stringify(expected)
  if (actualJson !== expectedJson) {
    throw new Error(`${message}\nexpected: ${expectedJson}\nactual:   ${actualJson}`)
  }
}

const fixture = JSON.parse(await readFile(fixturePath, "utf8"))
const generatedContract = await readFile(generatedContractPath, "utf8")
const knownIntentIds = new Set(readGeneratedStringArray(generatedContract, "AGENT_INTENT_IDS"))
const knownEndpointIds = new Set(readGeneratedStringArray(generatedContract, "AGENT_ENDPOINT_IDS"))
const knownSafetyFlagIds = new Set(readGeneratedStringArray(generatedContract, "ADMIN_AGENT_SAFETY_FLAG_IDS"))

function assertKnownIntent(value, context) {
  if (value && !knownIntentIds.has(value)) {
    throw new Error(`${context} references unknown generated intent id: ${value}`)
  }
}

function assertKnownEndpoint(value, context) {
  if (value && !knownEndpointIds.has(value)) {
    throw new Error(`${context} references unknown generated endpoint id: ${value}`)
  }
}

function assertKnownWorkflowReferences(testCase) {
  for (const flagId of testCase.expected.activeBlockingFlags ?? []) {
    if (!knownSafetyFlagIds.has(flagId)) {
      throw new Error(`${testCase.id} references unknown generated safety flag id: ${flagId}`)
    }
  }

  if (testCase.kind !== "simulation") {
    return
  }

  assertKnownIntent(testCase.response.selectedIntentId, `${testCase.id}.response.selectedIntentId`)
  assertKnownIntent(testCase.response.intentLineage?.intentId, `${testCase.id}.response.intentLineage.intentId`)

  for (const intentId of testCase.response.intentLineage?.resolutionWorkflows ?? []) {
    assertKnownIntent(intentId, `${testCase.id}.response.intentLineage.resolutionWorkflows`)
  }

  for (const endpointId of testCase.response.intentLineage?.targetEndpoints ?? []) {
    assertKnownEndpoint(endpointId, `${testCase.id}.response.intentLineage.targetEndpoints`)
  }

  for (const endpoint of testCase.response.endpointPlan ?? []) {
    assertKnownEndpoint(endpoint.endpointId, `${testCase.id}.response.endpointPlan`)
  }
}

for (const testCase of fixture.cases) {
  assertKnownWorkflowReferences(testCase)
  const actual = classifyScenario(testCase)
  assertEqual(actual, testCase.expected, `Admin-agent UI scenario failed: ${testCase.id}`)
}

console.log(`Validated ${fixture.cases.length} admin-agent UI scenarios.`)
