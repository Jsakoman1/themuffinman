<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import SurfaceContentView from "../components/SurfaceContentView.vue"
import {getAppSurfaceConfig, type AppSurfaceId} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"

const route = useRoute()
const surfaceId = computed(() => route.meta.surfaceId as AppSurfaceId)
const surface = computed(() => getAppSurfaceConfig(surfaceId.value))
const detailLabel = computed(() => route.params.conversationId ? `Conversation #${route.params.conversationId}` : "")
const {model, isLoading, error, reload} = useShellSurfaceData(surfaceId, route)
</script>

<template>
  <SurfaceContentView
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
