<script setup lang="ts">
import {onMounted, ref} from "vue"
import type {CircleContactDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import ModuleTabs from "../components/ModuleTabs.vue"
const tabs = [{id: "overview", label: "Your people", route: "/people", backendScope: "people.overview", emptyState: "Find someone you know."}, {id: "find", label: "Find people", route: "/people/find", backendScope: "people.find", emptyState: "Search for someone you trust."}, {id: "circles", label: "Circles", route: "/people/circles", backendScope: "people.circles", emptyState: "Create your first circle."}, {id: "requests", label: "Requests", route: "/people/requests", backendScope: "people.requests", emptyState: "No connection requests need attention."}, {id: "settings", label: "Settings", route: "/people/settings", backendScope: "people.settings", emptyState: "No additional settings are available."}]
const people = ref<CircleContactDTO[]>([])
const isLoading = ref(true)
const error = ref("")
const load = async () => { isLoading.value = true; error.value = ""; try { people.value = (await userShellApi.getCircleConnections()).items } catch { error.value = "Could not load your connections." } finally { isLoading.value = false } }
onMounted(() => void load())
</script>
<template><section class="people-overview" aria-label="People overview" data-people-overview="true" data-mental-model="connections-decisions-circles"><ModuleTabs :tabs="tabs" active-id="overview" /><AppStatus v-if="isLoading" message="Loading your people." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="!people.length" message="You have no connections yet. Find someone you know to get started." /><section v-else class="people-overview__list" aria-label="Connected people"><SurfaceRow v-for="person in people" :key="person.userId" :row="{id: String(person.userId), title: person.username, description: person.circleSummaryLabel || person.profileDescription || 'Connected person', badge: 'Connected', to: `/people/${person.userId}`}" /></section></section></template>
<style scoped>.people-overview{display:grid;gap:var(--space-4);min-width:0}.people-overview__list{display:grid;gap:var(--space-2)}.people-overview__list :deep(.surface-row){border:1px solid var(--border-subtle);border-radius:var(--radius-card);background:var(--surface-raised)}</style>
