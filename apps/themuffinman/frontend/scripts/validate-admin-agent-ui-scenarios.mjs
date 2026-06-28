import {readFile} from "node:fs/promises"
import path from "node:path"
import {fileURLToPath} from "node:url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const fixturePath = path.resolve(__dirname, "./fixtures/admin-agent-ui-scenarios.json")

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

for (const testCase of fixture.cases) {
  const actual = classifyScenario(testCase)
  assertEqual(actual, testCase.expected, `Admin-agent UI scenario failed: ${testCase.id}`)
}

console.log(`Validated ${fixture.cases.length} admin-agent UI scenarios.`)
