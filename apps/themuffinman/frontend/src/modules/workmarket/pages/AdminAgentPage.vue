<script setup lang="ts">
import {computed, ref} from "vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import UiAppShellPage from "../../../components/ui/UiAppShellPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import {
  buildPlaygroundSafetyViewModel,
  buildSimulationSafetyViewModel
} from "../api/adminAgentSafetyViewModel.ts"
import {adminAgentContractGate} from "../api/adminAgentContractGate.ts"
import {collectAgentWorkflowReferenceIssues} from "../api/agentWorkflowGuards.ts"
import {
  workmarketApi,
  type AdminAgentExecutionResponse,
  type AdminAgentPlaygroundResponse,
  type AdminAgentSimulationResponse
} from "../api/workmarketApi.ts"

const prompt = ref("Approve the first applicant for my quest Garden Help.")
const isSubmittingPlanner = ref(false)
const isSubmittingSimulation = ref(false)
const isSubmittingExecution = ref(false)
const error = ref("")
const plannerResponse = ref<AdminAgentPlaygroundResponse | null>(null)
const simulationResponse = ref<AdminAgentSimulationResponse | null>(null)
const executionResponse = ref<AdminAgentExecutionResponse | null>(null)

const plannerSafety = computed(() => plannerResponse.value
  ? buildPlaygroundSafetyViewModel(plannerResponse.value)
  : null)

const simulationSafety = computed(() => simulationResponse.value
  ? buildSimulationSafetyViewModel(simulationResponse.value)
  : null)

const simulationWorkflowReferenceIssues = computed(() => simulationResponse.value
  ? collectAgentWorkflowReferenceIssues(simulationResponse.value)
  : [])

const contractGateSummary = computed(() => ({
  playgroundFields: adminAgentContractGate.adminAgentPlaygroundResponseKeys.length,
  simulationFields: adminAgentContractGate.adminAgentSimulationResponseKeys.length,
  knownIntents: adminAgentContractGate.knownIntentIds.length,
  knownEndpoints: adminAgentContractGate.knownEndpointIds.length
}))

const runPlanner = async () => {
  isSubmittingPlanner.value = true
  error.value = ""

  try {
    plannerResponse.value = await workmarketApi.runAdminAgentPrompt({
      prompt: prompt.value
    })
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not run agent planner.")
  } finally {
    isSubmittingPlanner.value = false
  }
}

const runSimulation = async () => {
  isSubmittingSimulation.value = true
  error.value = ""

  try {
    simulationResponse.value = await workmarketApi.runAdminAgentSimulation({
      prompt: prompt.value
    })
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not run agent dry-run simulation.")
  } finally {
    isSubmittingSimulation.value = false
  }
}

const runExecution = async (confirmed: boolean) => {
  isSubmittingExecution.value = true
  error.value = ""

  try {
    executionResponse.value = await workmarketApi.runAdminAgentExecution({
      prompt: prompt.value,
      confirmed
    })
  } catch (requestError) {
    error.value = getApiErrorMessage(requestError, "Could not run agent execution.")
  } finally {
    isSubmittingExecution.value = false
  }
}
</script>

<template>
  <UiAppShellPage admin>
    <AdminShellHeader
      title="Agent Playground"
      subtitle="Inspect planner and dry-run output, then preview or confirm the first guarded admin execution capability."
    />

    <div class="admin-overview">
      <UiSurfaceSection title="Prompt" compact>
        <div class="form-stack">
          <UiFieldGroup label="Admin prompt">
            <textarea
              v-model="prompt"
              class="input"
              rows="8"
              placeholder="Describe the agent task you want to test..."
            />
          </UiFieldGroup>

          <div class="surface-inline-spread">
            <div class="muted">
              Planner and dry-run remain the default surfaces. Direct execution is limited to synthetic quest batch generation behind explicit confirmation.
            </div>
            <div class="surface-inline">
              <button class="button button--ghost" type="button" :disabled="isSubmittingPlanner || !prompt.trim()" @click="runPlanner">
                {{ isSubmittingPlanner ? "Running planner..." : "Run planner" }}
              </button>
              <button class="button button--action" type="button" :disabled="isSubmittingSimulation || !prompt.trim()" @click="runSimulation">
                {{ isSubmittingSimulation ? "Running dry run..." : "Run dry run" }}
              </button>
              <button class="button button--ghost" type="button" :disabled="isSubmittingExecution || !prompt.trim()" @click="runExecution(false)">
                {{ isSubmittingExecution ? "Preparing..." : "Preview execute" }}
              </button>
              <button
                class="button button--danger"
                type="button"
                :disabled="isSubmittingExecution || !executionResponse?.confirmationRequired"
                @click="runExecution(true)"
              >
                {{ isSubmittingExecution ? "Executing..." : "Confirm execute" }}
              </button>
            </div>
          </div>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection title="Contract gate" compact>
        <div class="surface-inline-spread">
          <strong>Planner fields</strong>
          <span>{{ contractGateSummary.playgroundFields }}</span>
        </div>
        <div class="surface-inline-spread">
          <strong>Simulation fields</strong>
          <span>{{ contractGateSummary.simulationFields }}</span>
        </div>
        <div class="surface-inline-spread">
          <strong>Known intents</strong>
          <span>{{ contractGateSummary.knownIntents }}</span>
        </div>
        <div class="surface-inline-spread">
          <strong>Known endpoints</strong>
          <span>{{ contractGateSummary.knownEndpoints }}</span>
        </div>
      </UiSurfaceSection>

      <div v-if="error" class="alert alert--error">{{ error }}</div>

      <UiSurfaceSection v-if="plannerResponse && plannerSafety" :title="plannerResponse.title" compact>
        <div class="surface-stack">
          <div class="surface-inline-spread">
            <strong>Planner safety</strong>
            <span :class="plannerSafety.summaryTone === 'safe' ? 'badge badge--success' : 'badge badge--danger'">
              {{ plannerSafety.executionBlocked ? "Blocked" : "Safe as planner output" }}
            </span>
          </div>

          <div class="surface-inline-spread">
            <strong>Provider</strong>
            <span class="badge badge--warning">{{ plannerResponse.provider }}</span>
          </div>

          <div class="surface-inline-spread">
            <strong>Prompt language</strong>
            <span>{{ plannerResponse.promptSourceLanguage }}</span>
          </div>

          <div class="surface-inline-spread">
            <strong>Translation</strong>
            <span>{{ plannerResponse.promptTranslationProvider }} / {{ plannerResponse.promptTranslationReliable ? "Reliable" : "Unreliable" }}</span>
          </div>

          <div class="surface-stack">
            <strong>Blocking contract</strong>
            <ul class="stack">
              <li v-for="flag in plannerSafety.activeBlockingFlags" :key="flag.id">
                <strong>{{ flag.label }}:</strong> {{ flag.detail }}
              </li>
            </ul>
          </div>

          <div class="surface-stack">
            <strong>Clarification contract</strong>
            <div class="muted">{{ plannerResponse.clarificationContract.reason }}</div>
          </div>

          <div v-if="plannerResponse.resolutionRequirements.length" class="surface-stack">
            <strong>Resolution requirements</strong>
            <ul class="stack">
              <li v-for="item in plannerResponse.resolutionRequirements" :key="`${item.entityType}-${item.workflowId}`">
                <code>{{ item.entityType }}</code> via <code>{{ item.workflowId }}</code> / {{ item.selectionRule }}
              </li>
            </ul>
          </div>

          <div class="surface-stack">
            <strong>Execution readiness</strong>
            <ul class="stack">
              <li>Planning only: {{ plannerResponse.executionReadiness.planningOnly ? "Yes" : "No" }}</li>
              <li>Translation ready: {{ plannerResponse.executionReadiness.translationReady ? "Yes" : "No" }}</li>
              <li>Current location capability: {{ plannerResponse.executionReadiness.currentLocationCapabilityStatus }}</li>
              <li>Destructive confirmation required: {{ plannerResponse.executionReadiness.destructiveConfirmationRequired ? "Yes" : "No" }}</li>
              <li>Multi-actor context required: {{ plannerResponse.executionReadiness.multiActorContextRequired ? "Yes" : "No" }}</li>
            </ul>
          </div>

          <div v-if="plannerResponse.directExecutionSummary" class="surface-stack">
            <strong>Direct execution</strong>
            <div class="muted">{{ plannerResponse.directExecutionSummary }}</div>
            <div v-if="plannerResponse.directExecutionCapabilityId">
              Capability: <code>{{ plannerResponse.directExecutionCapabilityId }}</code>
            </div>
          </div>

          <div class="muted">{{ plannerResponse.summary }}</div>

          <div v-if="plannerResponse.matchedSignals.length" class="surface-stack">
            <strong>Matched signals</strong>
            <ul class="stack">
              <li v-for="signal in plannerResponse.matchedSignals" :key="signal"><code>{{ signal }}</code></li>
            </ul>
          </div>

          <div v-if="plannerResponse.unresolvedInputs.length" class="surface-stack">
            <strong>Unresolved inputs</strong>
            <ul class="stack">
              <li v-for="item in plannerResponse.unresolvedInputs" :key="item">{{ item }}</li>
            </ul>
          </div>

          <div v-if="plannerResponse.warnings.length" class="surface-stack">
            <strong>Warnings</strong>
            <ul class="stack">
              <li v-for="warning in plannerResponse.warnings" :key="warning">{{ warning }}</li>
            </ul>
          </div>

          <div class="surface-stack">
            <strong>Suggested workflows</strong>
            <ul class="stack">
              <li v-for="workflow in plannerResponse.suggestedWorkflows" :key="workflow"><code>{{ workflow }}</code></li>
            </ul>
          </div>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection v-if="simulationResponse && simulationSafety" title="Dry-run simulation" compact>
        <div class="surface-stack">
          <div class="surface-inline-spread">
            <strong>Execution safety</strong>
            <span :class="simulationSafety.summaryTone === 'safe' ? 'badge badge--success' : 'badge badge--danger'">
              {{ simulationResponse.safeToExecute ? "Safe to execute" : "Execution blocked" }}
            </span>
          </div>

          <div class="surface-inline-spread">
            <strong>Selected intent</strong>
            <span><code>{{ simulationResponse.selectedIntentId ?? "none" }}</code></span>
          </div>

          <div v-if="simulationWorkflowReferenceIssues.length" class="surface-stack">
            <strong>Contract warnings</strong>
            <ul class="stack">
              <li v-for="issue in simulationWorkflowReferenceIssues" :key="`${issue.field}-${issue.value}`">
                <code>{{ issue.field }}</code> references unknown {{ issue.expectedCatalog }} <code>{{ issue.value }}</code>
              </li>
            </ul>
          </div>

          <div class="surface-inline-spread">
            <strong>Resolution confidence</strong>
            <span>{{ simulationResponse.resolutionConfidence.tier }} / {{ simulationResponse.resolutionConfidence.score }}</span>
          </div>

          <div class="surface-stack">
            <strong>Execution blockers</strong>
            <ul class="stack">
              <li v-for="flag in simulationSafety.activeBlockingFlags" :key="flag.id">
                <strong>{{ flag.label }}:</strong> {{ flag.detail }}
              </li>
            </ul>
          </div>

          <div v-if="simulationResponse.blockingReasons.length" class="surface-stack">
            <strong>Blocking reasons</strong>
            <ul class="stack">
              <li v-for="reason in simulationResponse.blockingReasons" :key="reason">{{ reason }}</li>
            </ul>
          </div>

          <div v-if="simulationResponse.capabilityAssessments.length" class="surface-stack">
            <strong>Capability assessments</strong>
            <ul class="stack">
              <li v-for="capability in simulationResponse.capabilityAssessments" :key="capability.capabilityId">
                <code>{{ capability.capabilityId }}</code> / {{ capability.status }} / {{ capability.reason }}
              </li>
            </ul>
          </div>

          <div v-if="simulationResponse.intentLineage" class="surface-stack">
            <strong>Intent lineage</strong>
            <div class="muted">
              Resolution: <code>{{ simulationResponse.intentLineage.resolutionWorkflows.join(", ") }}</code>
            </div>
            <div class="muted">
              Endpoints: <code>{{ simulationResponse.intentLineage.targetEndpoints.join(", ") }}</code>
            </div>
            <div class="muted">
              Safety: <code>{{ simulationResponse.intentLineage.safetyPolicies.join(", ") }}</code>
            </div>
          </div>

          <div v-if="simulationResponse.endpointPlan.length" class="surface-stack">
            <strong>Endpoint plan</strong>
            <ul class="stack">
              <li v-for="endpoint in simulationResponse.endpointPlan" :key="endpoint.endpointId">
                <code>{{ endpoint.method }}</code> <code>{{ endpoint.path }}</code> / {{ endpoint.endpointId }}
              </li>
            </ul>
          </div>

          <div v-if="simulationResponse.unresolvedInputs.length" class="surface-stack">
            <strong>Unresolved inputs</strong>
            <ul class="stack">
              <li v-for="item in simulationResponse.unresolvedInputs" :key="item">{{ item }}</li>
            </ul>
          </div>
        </div>
      </UiSurfaceSection>

      <UiSurfaceSection v-if="executionResponse" title="Direct execution" compact>
        <div class="surface-stack">
          <div class="surface-inline-spread">
            <strong>Status</strong>
            <span :class="executionResponse.executed ? 'badge badge--success' : executionResponse.confirmationRequired ? 'badge badge--warning' : 'badge badge--danger'">
              {{ executionResponse.executed ? "Executed" : executionResponse.confirmationRequired ? "Awaiting confirmation" : "Blocked" }}
            </span>
          </div>

          <div class="muted">{{ executionResponse.summary }}</div>

          <div class="surface-inline-spread">
            <strong>Capability</strong>
            <span><code>{{ executionResponse.capabilityId }}</code></span>
          </div>

          <div class="surface-inline-spread">
            <strong>Target user</strong>
            <span>{{ executionResponse.targetUserLabel ?? "Not resolved" }}</span>
          </div>

          <div class="surface-inline-spread">
            <strong>Batch size</strong>
            <span>{{ executionResponse.effectiveCount ?? 0 }} / {{ executionResponse.requestedCount ?? 0 }}</span>
          </div>

          <div v-if="executionResponse.topic" class="surface-inline-spread">
            <strong>Topic</strong>
            <span>{{ executionResponse.topic }}</span>
          </div>

          <div v-if="executionResponse.blockingReasons.length" class="surface-stack">
            <strong>Blocking reasons</strong>
            <ul class="stack">
              <li v-for="reason in executionResponse.blockingReasons" :key="reason">{{ reason }}</li>
            </ul>
          </div>

          <div v-if="executionResponse.warnings.length" class="surface-stack">
            <strong>Warnings</strong>
            <ul class="stack">
              <li v-for="warning in executionResponse.warnings" :key="warning">{{ warning }}</li>
            </ul>
          </div>

          <div v-if="executionResponse.questTitles.length" class="surface-stack">
            <strong>{{ executionResponse.executed ? "Created quests" : "Execution preview" }}</strong>
            <ul class="stack">
              <li v-for="title in executionResponse.questTitles" :key="title">{{ title }}</li>
            </ul>
          </div>
        </div>
      </UiSurfaceSection>
    </div>
  </UiAppShellPage>
</template>
