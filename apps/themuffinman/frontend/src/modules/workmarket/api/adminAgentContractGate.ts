import type {
  AdminAgentPlaygroundResponse,
  AdminAgentSimulationResponse
} from "./contracts.ts"

type AssertAllKeysCovered<T, Keys extends readonly (keyof T)[]> =
  Exclude<keyof T, Keys[number]> extends never ? true : never

const adminAgentPlaygroundResponseKeys = [
  "provider",
  "externalLlmConfigured",
  "promptSourceLanguage",
  "promptTranslationProvider",
  "promptTranslationApplied",
  "promptTranslationReliable",
  "originalPrompt",
  "translatedPrompt",
  "title",
  "summary",
  "resolutionRequirements",
  "clarificationContract",
  "executionReadiness",
  "matchedSignals",
  "unresolvedInputs",
  "warnings",
  "suggestedWorkflows",
  "nextSteps"
] as const satisfies readonly (keyof AdminAgentPlaygroundResponse)[]

const adminAgentSimulationResponseKeys = [
  "planningOnly",
  "safeToExecute",
  "promptSourceLanguage",
  "translatedPrompt",
  "selectedIntentId",
  "resolutionConfidence",
  "capabilityAssessments",
  "intentLineage",
  "endpointPlan",
  "clarificationContract",
  "executionReadiness",
  "matchedSignals",
  "unresolvedInputs",
  "blockingReasons",
  "suggestedWorkflows"
] as const satisfies readonly (keyof AdminAgentSimulationResponse)[]

const adminAgentClarificationContractKeys = [
  "clarificationRequired",
  "failClosedOnAmbiguity",
  "reason",
  "unresolvedFields"
] as const satisfies readonly (keyof AdminAgentPlaygroundResponse["clarificationContract"])[]

const adminAgentExecutionReadinessKeys = [
  "planningOnly",
  "translationReady",
  "requiresExternalTranslationProvider",
  "currentLocationCapabilityRequired",
  "currentLocationCapabilityStatus",
  "destructiveConfirmationRequired",
  "multiActorContextRequired"
] as const satisfies readonly (keyof AdminAgentPlaygroundResponse["executionReadiness"])[]

const adminAgentResolutionRequirementKeys = [
  "entityType",
  "workflowId",
  "scope",
  "selectionRule",
  "ambiguityPolicy",
  "endpointHint"
] as const satisfies readonly (keyof AdminAgentPlaygroundResponse["resolutionRequirements"][number])[]

const adminAgentResolutionConfidenceKeys = [
  "score",
  "tier",
  "reasons"
] as const satisfies readonly (keyof AdminAgentSimulationResponse["resolutionConfidence"])[]

const adminAgentCapabilityAssessmentKeys = [
  "capabilityId",
  "status",
  "reason"
] as const satisfies readonly (keyof AdminAgentSimulationResponse["capabilityAssessments"][number])[]

const adminAgentIntentLineageKeys = [
  "intentId",
  "sourcePromptExamples",
  "resolutionWorkflows",
  "targetEndpoints",
  "safetyPolicies",
  "expectedReadModels"
] as const satisfies readonly (keyof NonNullable<AdminAgentSimulationResponse["intentLineage"]>)[]

const adminAgentEndpointPlanKeys = [
  "endpointId",
  "method",
  "path"
] as const satisfies readonly (keyof AdminAgentSimulationResponse["endpointPlan"][number])[]

const _adminAgentResponseCoverage: AssertAllKeysCovered<AdminAgentPlaygroundResponse, typeof adminAgentPlaygroundResponseKeys> = true
const _adminAgentSimulationCoverage: AssertAllKeysCovered<AdminAgentSimulationResponse, typeof adminAgentSimulationResponseKeys> = true
const _clarificationCoverage: AssertAllKeysCovered<AdminAgentPlaygroundResponse["clarificationContract"], typeof adminAgentClarificationContractKeys> = true
const _executionCoverage: AssertAllKeysCovered<AdminAgentPlaygroundResponse["executionReadiness"], typeof adminAgentExecutionReadinessKeys> = true
const _resolutionCoverage: AssertAllKeysCovered<AdminAgentPlaygroundResponse["resolutionRequirements"][number], typeof adminAgentResolutionRequirementKeys> = true
const _resolutionConfidenceCoverage: AssertAllKeysCovered<AdminAgentSimulationResponse["resolutionConfidence"], typeof adminAgentResolutionConfidenceKeys> = true
const _capabilityAssessmentCoverage: AssertAllKeysCovered<AdminAgentSimulationResponse["capabilityAssessments"][number], typeof adminAgentCapabilityAssessmentKeys> = true
const _intentLineageCoverage: AssertAllKeysCovered<NonNullable<AdminAgentSimulationResponse["intentLineage"]>, typeof adminAgentIntentLineageKeys> = true
const _endpointPlanCoverage: AssertAllKeysCovered<AdminAgentSimulationResponse["endpointPlan"][number], typeof adminAgentEndpointPlanKeys> = true

export const adminAgentContractGate = {
  adminAgentPlaygroundResponseKeys,
  adminAgentSimulationResponseKeys,
  adminAgentClarificationContractKeys,
  adminAgentExecutionReadinessKeys,
  adminAgentResolutionRequirementKeys,
  adminAgentResolutionConfidenceKeys,
  adminAgentCapabilityAssessmentKeys,
  adminAgentIntentLineageKeys,
  adminAgentEndpointPlanKeys,
  adminAgentResponseCoverage: _adminAgentResponseCoverage,
  adminAgentSimulationCoverage: _adminAgentSimulationCoverage,
  clarificationCoverage: _clarificationCoverage,
  executionCoverage: _executionCoverage,
  resolutionCoverage: _resolutionCoverage,
  resolutionConfidenceCoverage: _resolutionConfidenceCoverage,
  capabilityAssessmentCoverage: _capabilityAssessmentCoverage,
  intentLineageCoverage: _intentLineageCoverage,
  endpointPlanCoverage: _endpointPlanCoverage
}
