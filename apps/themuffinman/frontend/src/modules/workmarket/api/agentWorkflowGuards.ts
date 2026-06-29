import {
  ADMIN_AGENT_SAFETY_FLAG_IDS,
  AGENT_ENDPOINT_IDS,
  AGENT_INTENT_IDS
} from "./contracts.ts"
import type {
  AdminAgentSafetyFlagIdGenerated,
  AdminAgentSimulationResponse,
  AgentEndpointId,
  AgentIntentId
} from "./contracts.ts"

export type AgentWorkflowReferenceIssue = {
  field: string
  value: string
  expectedCatalog: "intent" | "endpoint"
}

const knownIntentIds = new Set<string>(AGENT_INTENT_IDS)
const knownEndpointIds = new Set<string>(AGENT_ENDPOINT_IDS)
const knownSafetyFlagIds = new Set<string>(ADMIN_AGENT_SAFETY_FLAG_IDS)

export function isAgentIntentId(value: string): value is AgentIntentId {
  return knownIntentIds.has(value)
}

export function isAgentEndpointId(value: string): value is AgentEndpointId {
  return knownEndpointIds.has(value)
}

export function isAdminAgentSafetyFlagId(value: string): value is AdminAgentSafetyFlagIdGenerated {
  return knownSafetyFlagIds.has(value)
}

function collectUnknownIntent(
  issues: AgentWorkflowReferenceIssue[],
  field: string,
  value: string | null | undefined
) {
  if (value && !isAgentIntentId(value)) {
    issues.push({field, value, expectedCatalog: "intent"})
  }
}

function collectUnknownEndpoint(
  issues: AgentWorkflowReferenceIssue[],
  field: string,
  value: string | null | undefined
) {
  if (value && !isAgentEndpointId(value)) {
    issues.push({field, value, expectedCatalog: "endpoint"})
  }
}

export function collectAgentWorkflowReferenceIssues(
  response: AdminAgentSimulationResponse
): AgentWorkflowReferenceIssue[] {
  const issues: AgentWorkflowReferenceIssue[] = []

  collectUnknownIntent(issues, "selectedIntentId", response.selectedIntentId)
  collectUnknownIntent(issues, "intentLineage.intentId", response.intentLineage?.intentId)

  for (const intentId of response.intentLineage?.resolutionWorkflows ?? []) {
    collectUnknownIntent(issues, "intentLineage.resolutionWorkflows", intentId)
  }

  for (const endpointId of response.intentLineage?.targetEndpoints ?? []) {
    collectUnknownEndpoint(issues, "intentLineage.targetEndpoints", endpointId)
  }

  for (const endpoint of response.endpointPlan) {
    collectUnknownEndpoint(issues, "endpointPlan.endpointId", endpoint.endpointId)
  }

  return issues
}
