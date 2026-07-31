<script setup lang="ts">
import type {ShellSurfaceMetric, ShellSurfaceSection} from "../shellSurfaceData.ts"
import SurfaceMetricGrid from "./SurfaceMetricGrid.vue"
import SurfaceSection from "./SurfaceSection.vue"
defineProps<{metrics: ShellSurfaceMetric[]; sections: ShellSurfaceSection[]}>()
</script>
<template><SurfaceMetricGrid :metrics="metrics" /><div><SurfaceSection v-for="section in sections" :key="section.title" :section="section" /></div></template>
