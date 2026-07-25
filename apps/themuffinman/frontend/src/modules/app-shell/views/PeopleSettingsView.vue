<script setup lang="ts">
import AppStatus from "../components/AppStatus.vue"
</script>
<template><section class="people-settings" aria-label="People settings"><header><p class="people-settings__eyebrow">People / Settings</p><h1>People suggestions</h1><p>Suggestion preferences are shown only when the backend provides the current settings contract.</p></header><AppStatus message="People suggestion settings are not available from the backend yet." tone="warning" /></section></template>
<style scoped>.people-settings{display:grid;gap:var(--space-4);max-width:48rem}.people-settings header h1,.people-settings header p{margin:0}.people-settings header>p:last-child{margin-top:var(--space-1);color:var(--text-muted)}.people-settings__eyebrow{margin:0 0 var(--space-1);color:var(--text-soft);font-size:var(--text-size-label);font-weight:var(--text-weight-semibold);letter-spacing:var(--tracking-label);}</style>
