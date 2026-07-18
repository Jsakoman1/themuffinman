<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import AppLoadingState from "../components/AppLoadingState.vue"
import AppStatus from "../components/AppStatus.vue"
import CollectionToolbar from "../components/CollectionToolbar.vue"
import SurfaceContentView from "../components/SurfaceContentView.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import CollectionContextRail from "../components/CollectionContextRail.vue"
import {getAppSurfaceConfig, type AppSurfaceId} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"

const route = useRoute()
const surfaceId = computed(() => route.meta.surfaceId as AppSurfaceId)
const surface = computed(() => getAppSurfaceConfig(surfaceId.value))
const detailLabel = computed(() => route.params.conversationId ? `Conversation #${route.params.conversationId}` : "")
const {model, isLoading, error, reload} = useShellSurfaceData(surfaceId, route)
// These hubs have dedicated backend-prepared workspace data and must not fall back
// to generic section links. The remaining section hubs stay explicit navigation surfaces.
const dataWorkspaceHubIds: AppSurfaceId[] = ["calendar", "business", "profile"]
const isSectionHub = computed(() => surface.value.hubArchetype === "section-navigation" && !dataWorkspaceHubIds.includes(surface.value.id))
const hubRows = computed(() => surface.value.actions.map((action, index) => ({
  id: `${surface.value.id}-hub-action-${index}`,
  title: action.label,
  description: action.description || (action.tone === "vision" ? "Continue this context with Vision." : `Open ${action.label.toLowerCase()}.`),
  badge: action.tone === "vision" ? "Vision" : action.tone === "primary" ? "Primary" : undefined,
  to: action.to,
})))
</script>

<template>
  <section v-if="isSectionHub" class="workspace-section-hub" :data-surface-id="surface.id">
    <SurfaceHeader :config="surface" :detail-label="detailLabel" />
    <CollectionToolbar title="Workspace sections" :count="hubRows.length" :busy="isLoading" />
    <AppLoadingState v-if="isLoading" label="Loading workspace context" :rows="3" />
    <AppStatus v-else-if="error" :message="error" tone="error" retry @retry="reload" />
    <div v-else class="workspace-section-hub__workspace"><div class="workspace-section-hub__list"><SurfaceRow v-for="row in hubRows" :key="row.id" :row="row" /><AppStatus v-if="hubRows.length === 0" message="No workspace sections are available." /></div><CollectionContextRail title="Context" open-label="Show workspace context"><p>{{ model.note || 'Choose a section to continue in the canonical workspace.' }}</p></CollectionContextRail></div>
  </section>
  <SurfaceContentView
    v-else
    :config="surface"
    :metrics="model.metrics"
    :sections="model.sections"
    :loading="isLoading"
    :error="error"
    :detail-label="detailLabel"
    :note="model.note"
    :on-retry="reload"
  />
</template>

<style scoped>
.workspace-section-hub{display:grid;gap:var(--space-3);min-width:0}.workspace-section-hub__workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(18rem,var(--detail-rail-width));overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}.workspace-section-hub__list{min-width:0}.workspace-section-hub__list :deep(.surface-row:last-child){border-bottom:0}.workspace-section-hub__workspace :deep(.collection-context-rail){border-left:1px solid var(--border-subtle);border-right:0;background:var(--surface-raised)}@media(max-width:980px){.workspace-section-hub__workspace{grid-template-columns:1fr}.workspace-section-hub__workspace :deep(.collection-context-rail){border-top:1px solid var(--border-subtle);border-left:0}}
</style>
