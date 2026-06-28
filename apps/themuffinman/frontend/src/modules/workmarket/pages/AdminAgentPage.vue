<script setup lang="ts">
import {computed, ref} from "vue"
import {getApiErrorMessage} from "../../../api/apiErrors.ts"
import UiDashboardPage from "../../../components/ui/UiDashboardPage.vue"
import UiFieldGroup from "../../../components/ui/UiFieldGroup.vue"
import UiSurfaceSection from "../../../components/ui/UiSurfaceSection.vue"
import AdminShellHeader from "../components/admin/AdminShellHeader.vue"
import {
  buildPlaygroundSafetyViewModel,
  buildSimulationSafetyViewModel
} from "../api/adminAgentSafetyViewModel.ts"
import {
  workmarketApi,
  type AdminAgentPlaygroundResponse,
  type AdminAgentSimulationResponse
} from "../api/workmarketApi.ts"

const prompt = ref("Approve the first applicant for my quest Garden Help.")
const isSubmittingPlanner = ref(false)
const isSubmittingSimulation = ref(false)
const error = ref("")
const plannerResponse = ref<AdminAgentPlaygroundResponse | null>(null)
const simulationResponse = ref<AdminAgentSimulationResponse | null>(null)

const plannerSafety = computed(() => plannerResponse.value
  ? buildPlaygroundSafetyViewModel(plannerResponse.value)
  : null)

const simulationSafety = computed(() => simulationResponse.value
  ? buildSimulationSafetyViewModel(simulationResponse.value)
  : null)

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
</script>

<template>
  <UiDashboardPage admin>
    <AdminShellHeader
      title="Agent Playground"
      subtitle="Inspect backend planner and dry-run safety output before any future executor exists."
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
              This surface never executes mutations. Use planner output for classification and dry-run output for explicit execution blockers.
            </div>
            <div class="surface-inline">
              <button class="button button--ghost" type="button" :disabled="isSubmittingPlanner || !prompt.trim()" @click="runPlanner">
                {{ isSubmittingPlanner ? "Running planner..." : "Run planner" }}
              </button>
              <button class="button button--action" type="button" :disabled="isSubmittingSimulation || !prompt.trim()" @click="runSimulation">
                {{ isSubmittingSimulation ? "Running dry run..." : "Run dry run" }}
              </button>
            </div>
          </div>
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
    </div>
  </UiDashboardPage>
</template>
