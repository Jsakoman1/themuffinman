<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import SurfaceContentView from "../components/SurfaceContentView.vue"
import {getAppSurfaceConfig, type AppSurfaceId} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"

const route = useRoute()

const surfaceId = computed(() => route.meta.surfaceId as AppSurfaceId)
const surface = computed(() => getAppSurfaceConfig(surfaceId.value))
const detailLabel = computed(() => {
  if (route.params.conversationId) {
    return `Conversation #${route.params.conversationId}`
  }

  return ""
})

const {model, isLoading, error} = useShellSurfaceData(surfaceId, route)
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
  />
</template>
