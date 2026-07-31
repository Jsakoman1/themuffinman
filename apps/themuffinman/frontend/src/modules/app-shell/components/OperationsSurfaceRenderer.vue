<script setup lang="ts">
import type {ShellSurfaceSection} from "../shellSurfaceData.ts"
import SurfaceRow from "./SurfaceRow.vue"
defineProps<{sections: ShellSurfaceSection[]}>()
</script>
<template><div><section v-for="section in sections" :key="section.title"><h2>{{ section.title }}</h2><SurfaceRow v-for="row in section.rows" :key="row.id" :row="row" /></section></div></template>
