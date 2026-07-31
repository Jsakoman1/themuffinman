<script setup lang="ts">
import type {ShellSurfaceSection} from "../shellSurfaceData.ts"
import SurfaceSection from "./SurfaceSection.vue"
defineProps<{sections: ShellSurfaceSection[]}>()
</script>
<template><div><SurfaceSection v-for="section in sections" :key="section.title" :section="section" /></div></template>
