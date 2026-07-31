<script setup lang="ts">
defineProps<{serviceTitle?: string; startsAt?: string; timezone?: string}>()
</script>
<template><aside v-if="serviceTitle" class="selection-summary" aria-label="Your booking selection"><strong>{{ serviceTitle }}</strong><span v-if="startsAt">Selected time: {{ new Date(startsAt).toLocaleString(undefined, {timeZone: timezone}) }}</span><span v-else>Choose a time to continue.</span><span v-if="timezone" class="selection-summary__timezone">Times use {{ timezone }}.</span></aside></template>
<style scoped>.selection-summary{display:grid;gap:var(--space-1);padding:var(--space-2);border-left:3px solid var(--accent);background:var(--surface-sunken)}.selection-summary span{color:var(--text-muted);font-size:var(--text-size-meta)}</style>
