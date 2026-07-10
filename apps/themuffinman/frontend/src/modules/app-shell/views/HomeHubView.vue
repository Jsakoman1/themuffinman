<script setup lang="ts">
import {useRoute} from "vue-router"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
import {useShellSurfaceData} from "../shellSurfaceData.ts"
import SurfaceContentView from "../components/SurfaceContentView.vue"

const route = useRoute()
const surface = getAppSurfaceConfig("home")
const {model, isLoading, error} = useShellSurfaceData("home", route)
</script>

<template>
  <SurfaceContentView
    :config="surface"
    :metrics="model.metrics"
    :sections="model.sections"
    :loading="isLoading"
    :error="error"
    :note="model.note"
  />
</template>
