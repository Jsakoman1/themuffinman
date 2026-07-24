<script setup lang="ts">
import {computed} from "vue"
import {useRoute} from "vue-router"
import ModuleTabs from "../components/ModuleTabs.vue"
import {getModuleTabs} from "../moduleTabRegistry.ts"
import ProfileLocationSettingsView from "./ProfileLocationSettingsView.vue"

const route = useRoute()
const tabs = computed(() => getModuleTabs("profile")?.tabs ?? [])
const activeId = computed(() => route.path.includes("/settings") ? "settings" : "profile")
</script>

<template>
  <section class="profile-page" aria-label="Profile and settings" data-navigation-model="sibling-tabs">
    <ModuleTabs :tabs="tabs" :active-id="activeId" />
    <p class="profile-page__mode" data-profile-mode="progressive">{{ activeId === "settings" ? "Settings are grouped by privacy, location, appearance, and account safety." : "Your profile is the public identity people see; edit details inline below." }}</p>
    <ProfileLocationSettingsView :key="activeId" />
  </section>
</template>

<style scoped>.profile-page{display:grid;gap:var(--space-4);min-width:0}.profile-page__mode{margin:0;color:var(--text-muted);font-size:var(--text-size-body)}</style>
