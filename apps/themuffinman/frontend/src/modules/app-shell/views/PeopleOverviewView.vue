<script setup lang="ts">
import {onMounted, ref} from "vue"
import {RouterLink} from "vue-router"
import type {CircleContactDTO} from "../../../contracts/index.ts"
import {userShellApi} from "../api/userShellApi.ts"
import AppStatus from "../components/AppStatus.vue"
import SurfaceRow from "../components/SurfaceRow.vue"
import SurfaceHeader from "../components/SurfaceHeader.vue"
import {getAppSurfaceConfig} from "../shellDefinitions.ts"
const people = ref<CircleContactDTO[]>([])
const isLoading = ref(true)
const error = ref("")
const surface = getAppSurfaceConfig("people")
const load = async () => { isLoading.value = true; error.value = ""; try { people.value = (await userShellApi.getCircleConnections()).items } catch { error.value = "Could not load your connections." } finally { isLoading.value = false } }
onMounted(() => void load())
</script>
<template><section class="people-overview" aria-label="People overview" data-people-overview="true" data-mental-model="connections-decisions-circles"><SurfaceHeader :config="surface" title="Your people" description="Your accepted connections. Review invitations and manage sharing circles separately." /><nav class="people-overview__actions" aria-label="People actions"><RouterLink to="/people/find">Find people</RouterLink><RouterLink to="/people/requests">Review requests</RouterLink><RouterLink to="/people/circles">Manage circles</RouterLink><RouterLink to="/people/settings">People settings</RouterLink></nav><AppStatus v-if="isLoading" message="Loading your people." busy /><AppStatus v-else-if="error" :message="error" tone="error" retry @retry="load" /><AppStatus v-else-if="!people.length" message="You have no connections yet. Find someone you know to get started." /><section v-else class="people-overview__list" aria-label="Connected people"><SurfaceRow v-for="person in people" :key="person.userId" :row="{id: String(person.userId), title: person.username, description: person.circleSummaryLabel || person.profileDescription || 'Connected person', badge: 'Connected', to: `/people/${person.userId}`}" /></section></section></template>
<style scoped>.people-overview{display:grid;gap:var(--space-4);min-width:0}.people-overview__header{display:flex;align-items:flex-start;justify-content:space-between;gap:var(--space-3)}.people-overview__header h1,.people-overview__header p{margin:0}.people-overview__header h1{font-size:clamp(1.8rem,3vw,2.6rem);letter-spacing:var(--tracking-tight)}.people-overview__header>div>p:last-child{margin-top:var(--space-1);color:var(--text-muted)}.people-overview__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);text-transform:uppercase}.people-overview__settings{font-size:1.25rem;text-decoration:none}.people-overview__actions{display:flex;gap:var(--space-2);flex-wrap:wrap}.people-overview__actions a{padding:var(--space-2) var(--space-3);border:1px solid var(--border-subtle);border-radius:var(--radius-control);background:var(--surface-base);color:var(--text);text-decoration:none}.people-overview__actions a:hover{background:var(--surface-hover)}.people-overview__list{display:grid;overflow:hidden;border:1px solid var(--border-subtle);border-radius:var(--radius-surface);background:var(--surface-base)}</style>
