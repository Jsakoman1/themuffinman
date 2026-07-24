<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import ThingsDiscoveryView from "./ThingsDiscoveryView.vue"
import RidesView from "./RidesView.vue"

const route = useRoute()
const tabs = computed(() => getModuleTabs("share")?.tabs ?? [])
const isRides = computed(() => route.path.includes("/share/rides"))
const isRequests = computed(() => route.path.includes("/share/requests"))
const activeId = computed(() => isRides.value ? "rides" : isRequests.value ? "requests" : "things")
</script>

<template>
  <section class="share-page" aria-label="Share" data-navigation-model="shared-tabs">
    <ModuleTabs :tabs="tabs" :active-id="activeId" />
    <RidesView v-if="isRides" />
    <ThingsDiscoveryView v-else />
  </section>
</template>

<style scoped>.share-page{display:grid;gap:var(--space-4);min-width:0}</style>
