import type {
  AdminAgentSafetyFlagIdGenerated,
  AdminAgentPlaygroundResponse,
  AdminAgentSimulationResponse
} from "./contracts.ts"

export type AdminAgentSafetyFlagId = AdminAgentSafetyFlagIdGenerated

export type AdminAgentSafetyFlag = {
  id: AdminAgentSafetyFlagId
  label: string
  active: boolean
  executionBlocking: boolean
  detail: string
}

export type AdminAgentSafetyViewModel = {
  mode: "playground" | "simulation"
  summaryTone: "safe" | "blocked"
  informationalSignals: string[]
  blockingFlags: AdminAgentSafetyFlag[]
  activeBlockingFlags: AdminAgentSafetyFlag[]
  executionBlocked: boolean
}

const buildSharedFlags = (
  translationReliable: boolean,
  clarificationRequired: boolean,
  ambiguityReason: string,
  destructiveConfirmationRequired: boolean,
  multiActorContextRequired: boolean,
  currentLocationCapabilityRequired: boolean
): AdminAgentSafetyFlag[] => [
  {
    id: "translation_unreliable",
    label: "Translation unreliable",
    active: !translationReliable,
    executionBlocking: true,
    detail: "The prompt should not be trusted for execution until reliable translation is available."
  },
  {
    id: "ambiguity",
    label: "Clarification required",
    active: clarificationRequired,
    executionBlocking: true,
    detail: ambiguityReason
  },
  {
    id: "destructive_confirmation",
    label: "Destructive confirmation required",
    active: destructiveConfirmationRequired,
    executionBlocking: true,
    detail: "The target is destructive and still requires explicit confirmation."
  },
  {
    id: "multi_actor",
    label: "Second actor required",
    active: multiActorContextRequired,
    executionBlocking: true,
    detail: "Another authenticated actor, acceptance, or authority is still required."
  },
  {
    id: "current_location",
    label: "Current location capability required",
    active: currentLocationCapabilityRequired,
    executionBlocking: true,
    detail: "Trusted device coordinates or reverse-lookup backed location input are still missing."
  }
]

export function buildPlaygroundSafetyViewModel(
  response: AdminAgentPlaygroundResponse
): AdminAgentSafetyViewModel {
  const blockingFlags = buildSharedFlags(
    response.promptTranslationReliable,
    response.clarificationContract.clarificationRequired,
    response.clarificationContract.reason,
    response.executionReadiness.destructiveConfirmationRequired,
    response.executionReadiness.multiActorContextRequired,
    response.executionReadiness.currentLocationCapabilityRequired
  )
  const activeBlockingFlags = blockingFlags.filter((item) => item.active)

  return {
    mode: "playground",
    summaryTone: activeBlockingFlags.length === 0 ? "safe" : "blocked",
    informationalSignals: [
      response.executionReadiness.translationReady ? "translation_ready" : "translation_not_ready",
      response.executionReadiness.currentLocationCapabilityStatus
    ],
    blockingFlags,
    activeBlockingFlags,
    executionBlocked: activeBlockingFlags.some((item) => item.executionBlocking)
  }
}

export function buildSimulationSafetyViewModel(
  response: AdminAgentSimulationResponse
): AdminAgentSafetyViewModel {
  const blockingFlags = [
    ...buildSharedFlags(
      true,
      response.clarificationContract.clarificationRequired,
      response.clarificationContract.reason,
      response.executionReadiness.destructiveConfirmationRequired,
      response.executionReadiness.multiActorContextRequired,
      response.executionReadiness.currentLocationCapabilityRequired
    ),
    {
      id: "simulation_not_safe" as const,
      label: "Dry run is not safe to execute",
      active: !response.safeToExecute,
      executionBlocking: true,
      detail: response.blockingReasons.join(", ") || "Dry-run simulation marked the request as unsafe."
    }
  ]
  const activeBlockingFlags = blockingFlags.filter((item) => item.active)

  return {
    mode: "simulation",
    summaryTone: response.safeToExecute && activeBlockingFlags.length === 0 ? "safe" : "blocked",
    informationalSignals: [
      `resolution_${response.resolutionConfidence.tier}`,
      response.selectedIntentId ?? "no_selected_intent"
    ],
    blockingFlags,
    activeBlockingFlags,
    executionBlocked: !response.safeToExecute || activeBlockingFlags.some((item) => item.executionBlocking)
  }
}
